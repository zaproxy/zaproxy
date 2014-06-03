/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/05/15 Support for exclusions
// ZAP: 2012/02/11 Re-ordered icons, added spider icon and notify via SiteMap
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods and
// removed unnecessary casts.
// ZAP: 2012/05/15 Changed the method parse() to get the session description.
// ZAP: 2012/06/11 Changed the JavaDoc of the method isNewState().
// ZAP: 2012/07/29 Issue 43: Added support for Scope
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/08/07 Added method for getting all Nodes in Scope
// ZAP: 2012/08/29 Issue 250 Support for authentication management
// ZAP: 2012/10/02 Issue 385: Added support for Contexts
// ZAP: 2012/10/03 Issue 388: Added support for technologies
// ZAP: 2012/10/08 Issue 391: Performance improvements
// ZAP: 2012/12/14 Issue 438: Validate regexs as part of API enhancements
// ZAP: 2013/04/16 Issue 638: Persist and snapshot sessions instead of saving them
// ZAP: 2013/08/27 Issue 772: Restructuring of Saving/Loading Context Data
// ZAP: 2013/09/26 Issue 747: Error opening session files on directories with special characters
// ZAP: 2013/11/16 Issue 869: Differentiate proxied requests from (ZAP) user requests
// ZAP: 2014/01/06 Issue 965: Support 'single page' apps and 'non standard' parameter separators
// ZAP: 2014/01/31 Issue 979: Sites and Alerts trees can get corrupted - load session on EventDispatchThread
// ZAP: 2014-02-04 Added GlobalExcludeURL functionality:  Issue: TODO - insert bug/issue list here.
// ZAP: 2014/03/23 Issue 997: Session.open complains about improper use of addPath
// ZAP: 2014/03/23 Issue 999: History loaded in wrong order
// ZAP: 2014/05/26 Added listeners for contexts changed events.

package org.parosproxy.paros.model;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.FileXML;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.db.RecordSessionUrl;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.xml.sax.SAXException;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.extension.ascan.ExtensionActiveScan;
import org.zaproxy.zap.extension.spider.ExtensionSpider;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ParameterParser;
import org.zaproxy.zap.model.StandardParameterParser;
import org.zaproxy.zap.model.Tech;


public class Session extends FileXML {
	
    // ZAP: Added logger
    private static Logger log = Logger.getLogger(Session.class);

	private static final String ROOT = "session";
	
	private static final String SESSION_DESC = "sessionDesc";
	private static final String SESSION_ID = "sessionId";
	private static final String SESSION_NAME = "sessionName";
	
	private static final String[] PATH_SESSION_DESC = {ROOT, SESSION_DESC};	
	private static final String[] PATH_SESSION_ID = {ROOT, SESSION_ID};
	private static final String[] PATH_SESSION_NAME = {ROOT, SESSION_NAME};

	// other runtime members
	private Model model = null;
	private String fileName = "";
	private String sessionDesc = "";
	private List<String> excludeFromProxyRegexs = new ArrayList<>();
	private List<String> excludeFromScanRegexs = new ArrayList<>();
	private List<String> excludeFromSpiderRegexs = new ArrayList<>();
	// ZAP: Added globalExcludeURLRegexs code.
	private List<String> globalExcludeURLRegexs = new ArrayList<>();

    private List<Context> contexts = new ArrayList<>();
    private int nextContextIndex = 1;

	// parameters in XML
	private long sessionId = 0;
	private String sessionName = "";
	private SiteMap siteTree = null;
	
	private ParameterParser defaultParamParser = new StandardParameterParser();
	
	/**
	 * Constructor for the current session.  The current system time will be used as the session ID.
	 * @param model
	 */
	protected Session(Model model) {
		super(ROOT);

		// add session variable here
		setSessionId(System.currentTimeMillis());
		setSessionName(Constant.messages.getString("session.untitled"));
		setSessionDesc("");

		// create default object
		this.siteTree = SiteMap.createTree(model);
		
		this.model = model;
		
		discardContexts();
		// Always start with one context
	    getNewContext();

	}
	
	private void discardContexts() {
	    if (View.isInitialised()) {
	    	View.getSingleton().discardContexts();
		}
	    this.contexts.clear();
	    for(OnContextsChangedListener l:contextsChangedListeners)
	    	l.contextsChanged();
	    nextContextIndex = 1;
	}

	protected void discard() {
	    try {
	        model.getDb().getTableHistory().deleteHistorySession(getSessionId());
        } catch (SQLException e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }
		discardContexts();
	}

	protected void close() {
		discardContexts();
	}
	
    /**
     * @return Returns the sessionDesc.
     */
    public String getSessionDesc() {
        return sessionDesc;
    }
	
	/**
	 * @return Returns the sessionId.
	 */
	public long getSessionId() {
		return sessionId;
	}
	/**
	 * @return Returns the name.
	 */
	public String getSessionName() {
		return sessionName;
	}
    /**
     * @return Returns the siteTree.
     */
    public SiteMap getSiteTree() {
        return siteTree;
    }

    /**
     * Tells whether this session is in a new state or not. A session is in a
     * new state if it was never saved or it was not loaded from an existing
     * session.
     * 
     * @return {@code true} if this session is in a new state, {@code false}
     *         otherwise.
     */
    // ZAP: Changed the JavaDoc.
    public boolean isNewState() {
        return fileName.equals("");
    }

    
    protected void open(final File file, final SessionListener callback) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Exception thrownException = null;
                try {
                    open(file.getAbsolutePath());
                } catch (Exception e) {
                    thrownException = e;
                }
                if (callback != null) {
                    callback.sessionOpened(file, thrownException);
                }
            }
        });
        t.setPriority(Thread.NORM_PRIORITY-2);
        t.start();
    }

	protected void open(String fileName) throws SQLException, SAXException, IOException, Exception {

		readAndParseFile(new File(fileName).toURI().toASCIIString());
		model.getDb().close(false);
		model.getDb().open(fileName);
		this.fileName = fileName;
		
		//historyList.removeAllElements();

		SiteNode newRoot = new SiteNode(siteTree, -1, Constant.messages.getString("tab.sites"));
		siteTree.setRoot(newRoot);

		// update history reference
		List<Integer> list = model.getDb().getTableHistory().getHistoryIdsOfHistType(
			getSessionId(), HistoryReference.TYPE_PROXIED, HistoryReference.TYPE_ZAP_USER);
		HistoryReference historyRef = null;

		discardContexts();
		
	    // Load the session urls
	    this.setExcludeFromProxyRegexs(
	    		sessionUrlListToStingList(model.getDb().getTableSessionUrl().getUrlsForType(RecordSessionUrl.TYPE_EXCLUDE_FROM_PROXY)));

	    this.setExcludeFromScanRegexs(
	    		sessionUrlListToStingList(model.getDb().getTableSessionUrl().getUrlsForType(RecordSessionUrl.TYPE_EXCLUDE_FROM_SCAN)));

	    this.setExcludeFromSpiderRegexs(
	    		sessionUrlListToStingList(model.getDb().getTableSessionUrl().getUrlsForType(RecordSessionUrl.TYPE_EXCLUDE_FROM_SPIDER)));
	    
	    
		for (int i=0; i<list.size(); i++) {
			// ZAP: Removed unnecessary cast.
			int historyId = list.get(i).intValue();

			try {
				historyRef = new HistoryReference(historyId);

				if (View.isInitialised()) {
					final HistoryReference hRef = historyRef;
					final HttpMessage msg = historyRef.getHttpMessage();
					EventQueue.invokeAndWait(new Runnable() {

						@Override
						public void run() {
							SiteNode sn = getSiteTree().addPath(hRef, msg);
							if (sn != null) {
								sn.setIncludedInScope(isIncludedInScope(sn), false);
								sn.setExcludedFromScope(isExcludedFromScope(sn), false);
							}
						}
					});
				} else {
					SiteNode sn = getSiteTree().addPath(historyRef);
					if (sn != null) {
						sn.setIncludedInScope(this.isIncludedInScope(sn), false);
						sn.setExcludedFromScope(this.isExcludedFromScope(sn), false);
					}
				}
				// ZAP: Load alerts from db
				historyRef.loadAlerts();

				if (i % 100 == 99) Thread.yield();
			} catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
			}
			
		}
		
		// update siteTree reference
		list = model.getDb().getTableHistory().getHistoryIdsOfHistType(getSessionId(), HistoryReference.TYPE_SPIDER);
		
		for (int i=0; i<list.size(); i++) {
			// ZAP: Removed unnecessary cast.
			int historyId = list.get(i).intValue();

			try {
				historyRef = new HistoryReference(historyId);

				if (View.isInitialised()) {
					final HistoryReference hRef = historyRef;
					final HttpMessage msg = historyRef.getHttpMessage();
					EventQueue.invokeAndWait(new Runnable() {

						@Override
						public void run() {
							getSiteTree().addPath(hRef, msg);
						}
					});
				} else {
					getSiteTree().addPath(historyRef);
				}

				if (i % 100 == 99) Thread.yield();

			} catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
			}
			
			
		}
	    List<RecordContext> contextData = model.getDb().getTableContext().getAllData();
	    for (RecordContext data : contextData) {
	    	Context ctx = this.getContext(data.getContextId());
	    	if (ctx == null) {
	    		ctx = new Context(this, data.getContextId());
	    		this.addContext(ctx);
	    		if (nextContextIndex <= data.getContextId()) {
	    			nextContextIndex = data.getContextId() + 1;
	    		}
	    	}
	    	switch (data.getType()) {
	    		case RecordContext.TYPE_NAME:			ctx.setName(data.getData());
	    												if (View.isInitialised() && !ctx.getName().equals(String.valueOf(ctx.getIndex()))) {
	    													View.getSingleton().renameContext(ctx);
	    												}
	    												break;
	    		case RecordContext.TYPE_DESCRIPTION:	ctx.setDescription(data.getData()); break;
	    		case RecordContext.TYPE_INCLUDE:		ctx.addIncludeInContextRegex(data.getData()); break;
	    		case RecordContext.TYPE_EXCLUDE:		ctx.addExcludeFromContextRegex(data.getData()); break;
	    		case RecordContext.TYPE_IN_SCOPE:		ctx.setInScope(Boolean.parseBoolean(data.getData())); break;
	    		case RecordContext.TYPE_INCLUDE_TECH:	ctx.getTechSet().include(new Tech(data.getData())); break;
	    		case RecordContext.TYPE_EXCLUDE_TECH:	ctx.getTechSet().exclude(new Tech(data.getData())); break;
	    	}
	    }
		for (Context ctx : contexts) {
	    	try {
	    		// Set up the URL parameter parser
				List<String> strs = this.getContextDataStrings(ctx.getIndex(), RecordContext.TYPE_URL_PARSER_CLASSNAME);
				if (strs.size() == 1) {
					Class<?> c = ExtensionFactory.getAddOnLoader().loadClass(strs.get(0));
					if (c == null) {
						log.error("Failed to load URL parser for context " + ctx.getIndex() + " : " + strs.get(0));
					} else {
						ParameterParser parser = (ParameterParser) c.getConstructor().newInstance();
						strs = this.getContextDataStrings(ctx.getIndex(), RecordContext.TYPE_URL_PARSER_CONFIG);
				    	if (strs.size() == 1) {
				    		parser.init(strs.get(0));
				    	}
				    	ctx.setUrlParamParser(parser);
					}
				}
			} catch (Exception e) {
				log.error("Failed to load URL parser for context " + ctx.getIndex(), e);
			}
	    	try {
	    		// Set up the URL parameter parser
				List<String> strs = this.getContextDataStrings(ctx.getIndex(), RecordContext.TYPE_POST_PARSER_CLASSNAME);
				if (strs.size() == 1) {
					Class<?> c = ExtensionFactory.getAddOnLoader().loadClass(strs.get(0));
					if (c == null) {
						log.error("Failed to load POST parser for context " + ctx.getIndex() + " : " + strs.get(0));
					} else {
						ParameterParser parser = (ParameterParser) c.getConstructor().newInstance();
						strs = this.getContextDataStrings(ctx.getIndex(), RecordContext.TYPE_POST_PARSER_CONFIG);
				    	if (strs.size() == 1) {
				    		parser.init(strs.get(0));
				    	}
				    	ctx.setPostParamParser(parser);
					}
				}
			} catch (Exception e) {
				log.error("Failed to load POST parser for context " + ctx.getIndex(), e);
			}
		}
		
		if (View.isInitialised()) {
		    // ZAP: expand root
		    View.getSingleton().getSiteTreePanel().expandRoot();
		}
	    this.refreshScope();

		System.gc();
	}
	
	private List<String> sessionUrlListToStingList(List<RecordSessionUrl> rsuList) {
	    List<String> urlList = new ArrayList<>(rsuList.size());
	    for (RecordSessionUrl url : rsuList) {
	    	urlList.add(url.getUrl());
	    }
	    return urlList;
	}
	
	@Override
	protected void parse() throws Exception {
	    
	    long tempSessionId = 0;
	    String tempSessionName = "";
	    String tempSessionDesc = "";
	    
	    // use temp variable to check.  Exception will be flagged if any error.
		tempSessionId = Long.parseLong(getValue(SESSION_ID));
		tempSessionName = getValue(SESSION_NAME);
		// ZAP: Changed to get the session description and use the variable
		// tempSessionDesc.
		tempSessionDesc = getValue(SESSION_DESC);

		// set member variable after here
		sessionId = tempSessionId;
		sessionName = tempSessionName;
		sessionDesc = tempSessionDesc;
		


	}

	/**
	 * Asynchronous call to save a session.
	 * @param fileName
	 * @param callback
	 */
    protected void save(final String fileName, final SessionListener callback) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Exception thrownException = null;
                try {
                    save(fileName);
                } catch (Exception e) {
                	// ZAP: Log exceptions
                	log.warn(e.getMessage(), e);
                    thrownException = e;
                }
                if (callback != null) {
                    callback.sessionSaved(thrownException);
                }
            }
        });
        t.setPriority(Thread.NORM_PRIORITY-2);
        t.start();
    }
    
    /**
     * Synchronous call to save a session.
     * @param fileName
     * @throws Exception
     */
	protected void save(String fileName) throws Exception {
	    saveFile(fileName);
		if (isNewState()) {
		    model.moveSessionDb(fileName);
		} else {
		    if (!this.fileName.equals(fileName)) {
		        // copy file to new fileName
		        model.copySessionDb(this.fileName, fileName);
		    }
		}
	    this.fileName = fileName;
		
		synchronized (siteTree) {
		    saveSiteTree((SiteNode) siteTree.getRoot());
		}
		
		model.getDb().getTableSession().update(getSessionId(), getSessionName());
	}
	
	/**
	 * Asynchronous call to snapshot a session.
	 * @param fileName
	 * @param callback
	 */
    protected void snapshot(final String fileName, final SessionListener callback) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Exception thrownException = null;
                try {
                    snapshot(fileName);
                } catch (Exception e) {
                	// ZAP: Log exceptions
                	log.warn(e.getMessage(), e);
                    thrownException = e;
                }
                if (callback != null) {
                    callback.sessionSnapshot(thrownException);
                }
            }
        });
        t.setPriority(Thread.NORM_PRIORITY-2);
        t.start();
    }
    
    /**
     * Synchronous call to snapshot a session.
     * @param fileName
     * @throws Exception
     */
	protected void snapshot(String fileName) throws Exception {
	    saveFile(fileName);
        model.snapshotSessionDb(this.fileName, fileName);
	}

    /**
     * @param sessionDesc The sessionDesc to set.
     */
    public void setSessionDesc(String sessionDesc) {
        this.sessionDesc = sessionDesc;
		setValue(PATH_SESSION_DESC, sessionDesc);
    }
	
	/**
	 * @param sessionId The sessionId to set.
	 */
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
		//setText(SESSION_ID, Long.toString(sessionId));
		setValue(PATH_SESSION_ID, Long.toString(sessionId));

	}
	/**
	 * @param name The name to set.
	 */
	public void setSessionName(String name) {
		this.sessionName = name;
		//setText(SESSION_NAME, name);
		setValue(PATH_SESSION_NAME, name);
		
	}

    
    public String getFileName() {
        return fileName;
    }
    
    private void saveSiteTree(SiteNode node) {
        HttpMessage msg = null;

        if (!node.isRoot()) {
            if (node.getHistoryReference().getHistoryType() < 0) {
                // -ve means to be saved
                saveNodeMsg(msg);
            }
        }
        
        for (int i=0; i<node.getChildCount(); i++) {
            try {
                saveSiteTree((SiteNode) node.getChildAt(i));
            } catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
            }
        }
        
    }
    
    private void saveNodeMsg(HttpMessage msg) {
        // nothing need to be done
    }
    
    public String getSessionFolder() {
        String result = "";
        if (fileName.equals("")) {
//            result = Constant.FOLDER_SESSION;
            result = Constant.getInstance().FOLDER_SESSION;
        } else {
            File file = new File(fileName);
            result = file.getParent();
        }
        return result;
    }

	public List<String> getExcludeFromProxyRegexs() {
		return excludeFromProxyRegexs;
	}
	
	
	private List<String> stripEmptyLines(List<String> list) {
		List<String> slist = new ArrayList<>();
		for (String str : list) {
			if (str.length() > 0) {
				slist.add(str);
			}
		}
		return slist;
	}
	
	private void refreshScope(SiteNode node) {
		if (node == null) {
			return;
		}
		if (node.isIncludedInScope() == ! this.isIncludedInScope(node)) {
			// Its 'scope' state has changed, so switch it!
			node.setIncludedInScope(!node.isIncludedInScope(), false);
		}
		if (node.isExcludedFromScope() == ! this.isExcludedFromScope(node)) {
			// Its 'scope' state has changed, so switch it!
			node.setExcludedFromScope(!node.isExcludedFromScope(), false);
		}
		// Recurse down
		if (node.getChildCount() > 0) {
	    	SiteNode c = (SiteNode) node.getFirstChild();
	    	while (c != null) {
	    		refreshScope(c);
	    		c = (SiteNode) node.getChildAfter(c);
	    	}
		}
	}

	private void refreshScope() {
        if (EventQueue.isDispatchThread()) {
        	refreshScope((SiteNode) siteTree.getRoot());
        	Control.getSingleton().sessionScopeChanged();
        } else {
            try {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    	refreshScope((SiteNode) siteTree.getRoot());
                    	Control.getSingleton().sessionScopeChanged();
                    }
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
	}

	protected boolean isIncludedInScope(SiteNode sn) {
		if (sn == null) {
			return false;
		}
		return isIncludedInScope(sn.getHierarchicNodeName());
	}
	
	private boolean isIncludedInScope(String url) {
		if (url == null) {
			return false;
		}
		if (url.indexOf("?") > 0) {
			// Strip off any parameters
			url = url.substring(0, url.indexOf("?"));
		}
		for (Context context : contexts) {
			if (context.isInScope() && context.isIncluded(url)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isExcludedFromScope(SiteNode sn) {
		if (sn == null) {
			return false;
		}
		return isExcludedFromScope(sn.getHierarchicNodeName());
	}
	
	private boolean isExcludedFromScope(String url) {
		if (url == null) {
			return false;
		}
		if (url.indexOf("?") > 0) {
			// Strip off any parameters
			url = url.substring(0, url.indexOf("?"));
		}
		for (Context context : contexts) {
			if (context.isInScope() && context.isExcluded(url)) {
				return true;
			}
		}
		return false;
	}

	public boolean isInScope(HistoryReference href) {
		if (href == null) {
			return false;
		}
		if (href.getSiteNode() != null) {
			return this.isInScope(href.getSiteNode());
		}
		try {
			return this.isInScope(href.getURI().toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
		}
		return false;
	}
	
	public boolean isInScope(SiteNode sn) {
		if (sn == null) {
			return false;
		}
		return isInScope(sn.getHierarchicNodeName());
	}
	
	public boolean isInScope(String url) {
		if (url.indexOf("?") > 0) {
			// String off any parameters
			url = url.substring(0, url.indexOf("?"));
		}
		if (! this.isIncludedInScope(url)) {
			// Not explicitly included
			return false;
		}
		// Check to see if its explicitly excluded
		return ! this.isExcludedFromScope(url);
	}

	/**
	 * Gets the nodes from the site tree which are "In Scope". Searches recursively starting from
	 * the root node. Should be used with care, as it is time-consuming, querying the database for
	 * every node in the Site Tree.
	 * 
	 * @return the nodes in scope from site tree
	 */
	public List<SiteNode> getNodesInScopeFromSiteTree() {
		List<SiteNode> nodes = new LinkedList<>();
		SiteNode rootNode = (SiteNode) getSiteTree().getRoot();
		fillNodesInScope(rootNode, nodes);
		return nodes;
	}
	
	/**
	 * Fills a given list with nodes in scope, searching recursively.
	 * 
	 * @param rootNode the root node
	 * @param nodesList the nodes list
	 */
	private void fillNodesInScope(SiteNode rootNode, List<SiteNode> nodesList) {
		@SuppressWarnings("unchecked")
		Enumeration<SiteNode> en = rootNode.children();
		while (en.hasMoreElements()) {
			SiteNode sn = en.nextElement();
			if (isInScope(sn))
				nodesList.add(sn);
			fillNodesInScope(sn, nodesList);
		}
	}
	
	/**
	 * Gets the nodes from the site tree which are "In Scope" in a given context. Searches recursively
	 * starting from the root node. Should be used with care, as it is time-consuming, querying the database
	 * for every node in the Site Tree.
	 * 
	 * @param context the context
	 * @return the nodes in scope from site tree
	 */
	public List<SiteNode> getNodesInContextFromSiteTree(Context context) {
		List<SiteNode> nodes = new LinkedList<>();
		SiteNode rootNode = (SiteNode) getSiteTree().getRoot();
		fillNodesInContext(rootNode, nodes, context);
		return nodes;
	}
	
	/**
	 * Fills a given list with nodes in context, searching recursively.
	 * 
	 * @param rootNode the root node
	 * @param nodesList the nodes list
	 * @param context the context
	 */
	private void fillNodesInContext(SiteNode rootNode, List<SiteNode> nodesList, Context context) {
		@SuppressWarnings("unchecked")
		Enumeration<SiteNode> en = rootNode.children();
		while (en.hasMoreElements()) {
			SiteNode sn = en.nextElement();
			if (context.isInContext(sn))
				nodesList.add(sn);
			fillNodesInContext(sn, nodesList, context);
		}
	}

	public void setExcludeFromProxyRegexs(List<String> ignoredRegexs) throws SQLException {
		// Validate its a valid regex first
	    for (String url : ignoredRegexs) {
			Pattern.compile(url, Pattern.CASE_INSENSITIVE);
	    }

		this.excludeFromProxyRegexs = stripEmptyLines(ignoredRegexs);

		// ZAP: Added fullList & globalExcludeURLRegexs code.
	    List<String> fullList = new ArrayList<String>();
	    fullList.addAll(this.excludeFromProxyRegexs);
	    fullList.addAll(this.globalExcludeURLRegexs);
	    
		Control.getSingleton().setExcludeFromProxyUrls(fullList);
		
		// For debugging the GlobalExcludeURL functionality. 
		/*log.warn("setExcludeFromProxyRegexs  (ignored, session.proxy, session.global, fullList");
	    log.warn(ignoredRegexs.toString());
	    log.warn(excludeFromProxyRegexs.toString());
	    log.warn(globalExcludeURLRegexs.toString());
	    log.warn(fullList);
	    */
	    
	    
		model.getDb().getTableSessionUrl().setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_PROXY, this.excludeFromProxyRegexs);
		// Thought for GlobalExcludeURL; we can create addUrls() and call that too - but I don't think it is needed.
	}

	public void addExcludeFromProxyRegex(String ignoredRegex) throws SQLException {
		// Validate its a valid regex first
		Pattern.compile(ignoredRegex, Pattern.CASE_INSENSITIVE);
		
		this.excludeFromProxyRegexs.add(ignoredRegex);
		Control.getSingleton().setExcludeFromProxyUrls(this.excludeFromProxyRegexs);
		model.getDb().getTableSessionUrl().setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_PROXY, this.excludeFromProxyRegexs);
	}

	public List<String> getExcludeFromScanRegexs() {
		return excludeFromScanRegexs;
	}

	public void addExcludeFromScanRegexs(String ignoredRegex) throws SQLException {
		// Validate its a valid regex first
		Pattern.compile(ignoredRegex, Pattern.CASE_INSENSITIVE);
		
		this.excludeFromScanRegexs.add(ignoredRegex);
		ExtensionActiveScan extAscan = 
			(ExtensionActiveScan) Control.getSingleton().getExtensionLoader().getExtension(ExtensionActiveScan.NAME);
		if (extAscan != null) {
			// ZAP: Added fullList & globalExcludeURLRegexs code.
		    List<String> fullList = new ArrayList<String>();
		    fullList.addAll(this.excludeFromScanRegexs);
		    fullList.addAll(this.globalExcludeURLRegexs);

			extAscan.setExcludeList(fullList);
		}
		model.getDb().getTableSessionUrl().setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_SCAN, this.excludeFromScanRegexs);
	}

	public void setExcludeFromScanRegexs(List<String> ignoredRegexs) throws SQLException {
		// Validate its a valid regex first
	    for (String url : ignoredRegexs) {
			Pattern.compile(url, Pattern.CASE_INSENSITIVE);
	    }

		this.excludeFromScanRegexs = stripEmptyLines(ignoredRegexs);
		ExtensionActiveScan extAscan = 
			(ExtensionActiveScan) Control.getSingleton().getExtensionLoader().getExtension(ExtensionActiveScan.NAME);
		if (extAscan != null) {
			// ZAP: Added fullList & globalExcludeURLRegexs code.
		    List<String> fullList = new ArrayList<String>();
		    fullList.addAll(this.excludeFromScanRegexs);
		    fullList.addAll(this.globalExcludeURLRegexs);

			extAscan.setExcludeList(fullList);
		}
		model.getDb().getTableSessionUrl().setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_SCAN, this.excludeFromScanRegexs);
	}

	public List<String> getExcludeFromSpiderRegexs() {
		return excludeFromSpiderRegexs;
	}

	public void addExcludeFromSpiderRegex(String ignoredRegex) throws SQLException {
		// Validate its a valid regex first
		Pattern.compile(ignoredRegex, Pattern.CASE_INSENSITIVE);

		this.excludeFromSpiderRegexs.add(ignoredRegex);
		ExtensionSpider extSpider = 
			(ExtensionSpider) Control.getSingleton().getExtensionLoader().getExtension(ExtensionSpider.NAME);
		if (extSpider != null) {
			// ZAP: Added fullList & globalExcludeURLRegexs code.
		    List<String> fullList = new ArrayList<String>();
		    fullList.addAll(this.excludeFromSpiderRegexs);
		    fullList.addAll(this.globalExcludeURLRegexs);

		    extSpider.setExcludeList(fullList);
		}
		model.getDb().getTableSessionUrl().setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_SPIDER, this.excludeFromSpiderRegexs);
	}


	public void setExcludeFromSpiderRegexs(List<String> ignoredRegexs) throws SQLException {
		// Validate its a valid regex first
	    for (String url : ignoredRegexs) {
			Pattern.compile(url, Pattern.CASE_INSENSITIVE);
	    }

		this.excludeFromSpiderRegexs = stripEmptyLines(ignoredRegexs);
		ExtensionSpider extSpider = 
			(ExtensionSpider) Control.getSingleton().getExtensionLoader().getExtension(ExtensionSpider.NAME);
		if (extSpider != null) {
			// ZAP: Added fullList & globalExcludeURLRegexs code.
		    List<String> fullList = new ArrayList<String>();
		    fullList.addAll(this.excludeFromSpiderRegexs);
		    fullList.addAll(this.globalExcludeURLRegexs);

		    extSpider.setExcludeList(fullList);
		}
		model.getDb().getTableSessionUrl().setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_SPIDER, this.excludeFromSpiderRegexs);
	}

	/** TODO The GlobalExcludeURL functionality is currently alpha and subject to change.  */
	// ZAP: Added function.
	public void forceGlobalExcludeURLRefresh() throws SQLException {
		List<String> temp;
		
		temp = getExcludeFromProxyRegexs();
	    log.info(">>> forceGlobalExcludeURLRefresh: " + temp.toString());
		setExcludeFromProxyRegexs(temp);
		
		temp = getExcludeFromScanRegexs();
	    log.info(">>> forceGlobalExcludeURLRefresh: " + temp.toString());
		setExcludeFromScanRegexs(temp);
		
		temp = getExcludeFromSpiderRegexs();
	    log.info(">>> forceGlobalExcludeURLRefresh: " + temp.toString());
		setExcludeFromSpiderRegexs(temp);
	}

	/** TODO The GlobalExcludeURL functionality is currently alpha and subject to change.  */
	// ZAP: Added function.
	public List<String> getGlobalExcludeURLRegexs() {
		return globalExcludeURLRegexs;
	}

	/** TODO The GlobalExcludeURL functionality is currently alpha and subject to change.  */
	// ZAP: Added function.
	public void addGlobalExcludeURLRegexs(String ignoredRegex) throws SQLException {
		// Validate its a valid regex first
		Pattern.compile(ignoredRegex, Pattern.CASE_INSENSITIVE);
    
		this.globalExcludeURLRegexs.add(ignoredRegex);
		
		// This probably isn't needed in the active session, need input here. 
		//model.getDb().getTableSessionUrl().setUrls(RecordSessionUrl.TYPE_GLOBAL_EXCLUDE_URL, this.globalExcludeURLRegexs);
	}

	/** TODO The GlobalExcludeURL functionality is currently alpha and subject to change.  */
	// ZAP: Added function.
	public void setGlobalExcludeURLRegexs(List<String> ignoredRegexs) throws SQLException {
		// Validate its a valid regex first
	    for (String url : ignoredRegexs) {
			Pattern.compile(url, Pattern.CASE_INSENSITIVE);
	    }
	    log.info(">>> setGlobalExcludeURLRegexs" );

		this.globalExcludeURLRegexs = stripEmptyLines(ignoredRegexs);

		// This probably isn't needed in the active session, need input here. 
		//model.getDb().getTableSessionUrl().setUrls(RecordSessionUrl.TYPE_GLOBAL_EXCLUDE_URL, this.globalExcludeURLRegexs);
	    log.info("<<< setGlobalExcludeURLRegexs" );
	}
	
	public void setSessionUrls(int type, List<String> urls) throws SQLException {
		model.getDb().getTableSessionUrl().setUrls(type, urls);
	}
	
	public void setSessionUrl(int type, String url) throws SQLException {
		List<String> list = new ArrayList<>(1);
		list.add(url);
		this.setSessionUrls(type, list);
	}

	public List<String> getSessionUrls(int type) throws SQLException {
		List<RecordSessionUrl> urls = model.getDb().getTableSessionUrl().getUrlsForType(type);
		List<String> list = new ArrayList<>(urls.size());
		for (RecordSessionUrl url : urls) {
			list.add(url.getUrl());
		}
		return list;
	}
	
	public List<String> getContextDataStrings(int contextId, int type) throws SQLException {
	    List<RecordContext> dataList = model.getDb().getTableContext().getDataForContextAndType(contextId, type);
		List<String> list = new ArrayList<>();
		for (RecordContext data : dataList) {
			list.add(data.getData());
		}
		return list;
	}

	public void setContextData(int contextId, int type, String data) throws SQLException {
		List<String> list = new ArrayList<>();
		list.add(data);
		this.setContextData(contextId, type, list);
	}

	public void setContextData(int contextId, int type, List<String> dataList) throws SQLException {
		model.getDb().getTableContext().setData(contextId, type, dataList);
	}
	
	private List<String> techListToStringList (TreeSet<Tech> techList) {
		List<String> strList = new ArrayList<>();
		Iterator<Tech> iter = techList.iterator();
		while (iter.hasNext()) {
			strList.add(iter.next().toString());
		}
		return strList;
	}
	
	public void saveContext (Context c) {
		try {
			this.setContextData(c.getIndex(), RecordContext.TYPE_NAME, c.getName());
			this.setContextData(c.getIndex(), RecordContext.TYPE_DESCRIPTION, c.getDescription());
			this.setContextData(c.getIndex(), RecordContext.TYPE_IN_SCOPE, Boolean.toString(c.isInScope()));
			this.setContextData(c.getIndex(), RecordContext.TYPE_INCLUDE, c.getIncludeInContextRegexs());
			this.setContextData(c.getIndex(), RecordContext.TYPE_EXCLUDE, c.getExcludeFromContextRegexs());
			this.setContextData(c.getIndex(), RecordContext.TYPE_INCLUDE_TECH, techListToStringList(c.getTechSet().getIncludeTech()));
			this.setContextData(c.getIndex(), RecordContext.TYPE_EXCLUDE_TECH, techListToStringList(c.getTechSet().getExcludeTech()));
			this.setContextData(c.getIndex(), RecordContext.TYPE_URL_PARSER_CLASSNAME, 
					c.getUrlParamParser().getClass().getCanonicalName());
			this.setContextData(c.getIndex(), RecordContext.TYPE_URL_PARSER_CONFIG, c.getUrlParamParser().getConfig());
			this.setContextData(c.getIndex(), RecordContext.TYPE_POST_PARSER_CLASSNAME, 
					c.getPostParamParser().getClass().getCanonicalName());
			this.setContextData(c.getIndex(), RecordContext.TYPE_POST_PARSER_CONFIG, c.getPostParamParser().getConfig());
			model.saveContext(c);
		} catch (SQLException e) {
            log.error(e.getMessage(), e);
		}
		
		if (View.isInitialised()) {
			refreshScope();
		}
	}
	
	public void saveAllContexts(){
		for(Context c: contexts)
			this.saveContext(c);
	}
	
	public Context getNewContext() {
		Context c = new Context(this, this.nextContextIndex++);
		this.addContext(c);
		return c;
	}

	public void addContext(Context c) {
		this.contexts.add(c);
		this.model.loadContext(c);

		for (OnContextsChangedListener l : contextsChangedListeners)
			l.contextAdded(c);
		
		if (View.isInitialised()) {
			View.getSingleton().addContext(c);
		}
	}

	public Context getContext(int index) {
		for (Context context : contexts) {
			if (context.getIndex() == index) {
				return context;
			}
		}
		return null;
	}

	public Context getContext(String name) {
		for (Context context : contexts) {
			if (context.getName().equals(name)) {
				return context;
			}
		}
		return null;
	}

	public List<Context> getContexts() {
		return contexts;
	}

	public List<Context> getContextsForNode(SiteNode sn) {
		if (sn == null) {
			return new ArrayList<>();
		}
		return getContextsForUrl(sn.getHierarchicNodeName());
	}
	
	public List<Context> getContextsForUrl(String url) {
		List<Context> ctxList = new ArrayList<>();
		if (url.indexOf("?") > 0) {
			// String off any parameters
			url = url.substring(0, url.indexOf("?"));
		}
		for (Context context : contexts) {
			if (context.isInContext(url)) {
				ctxList.add(context);
			}
		}
		return ctxList;
	}

	/**
	 * Returns the url parameter parser associated with the first context found that includes the URL,
	 * or the default parser if it is not
	 * in a context
	 * @param url
	 * @return
	 */
	public ParameterParser getUrlParamParser(String url) {
		List<Context> contexts = getContextsForUrl(url);
		if (contexts.size() > 0) {
			return contexts.get(0).getUrlParamParser();
		}
		return this.defaultParamParser;
	}

	/**
	 * Returns the form parameter parser associated with the first context found that includes the URL,
	 * or the default parser if it is not
	 * in a context
	 * @param url
	 * @return
	 */
	public ParameterParser getFormParamParser(String url) {
		List<Context> contexts = getContextsForUrl(url);
		if (contexts.size() > 0) {
			return contexts.get(0).getPostParamParser();
		}
		return this.defaultParamParser;
	}

	/**
	 * Returns the specified parameters for the given message based on the parser associated with the
	 * first context found that includes the URL for the message, or the default parser if it is not
	 * in a context
	 * @param msg
	 * @param type
	 * @return
	 */
	public Map<String, String> getParams(HttpMessage msg, HtmlParameter.Type type) {
		switch (type) {
		case form:	return this.getFormParamParser(msg.getRequestHeader().getURI().toString()).getParams(msg, type);
		case url:	return this.getUrlParamParser(msg.getRequestHeader().getURI().toString()).getParams(msg, type);
		default:
					throw new InvalidParameterException("Type not supported: " + type);
		}
	}

	/**
	 * Returns the URL parameters for the given URL based on the parser associated with the
	 * first context found that includes the URL, or the default parser if it is not
	 * in a context
	 * @param uri
	 * @return
	 * @throws URIException
	 */
	public Map<String, String> getUrlParams(URI uri) throws URIException {
		return this.getUrlParamParser(uri.toString()).parse(uri.getQuery());
	}

	/**
	 * Returns the FORM parameters for the given URL based on the parser associated with the
	 * first context found that includes the URL, or the default parser if it is not
	 * in a context
	 * @param uri
	 * @param formData
	 * @return
	 * @throws URIException
	 */
	public Map<String, String> getFormParams(URI uri, String formData) throws URIException {
		return this.getFormParamParser(uri.toString()).parse(formData);
	}

	public List<String> getTreePath(URI uri) throws URIException {
		return this.getUrlParamParser(uri.toString()).getTreePath(uri);
	}

	
	// ZAP: Added listeners for contexts changed events.
	// TODO: Might be better structured elsewhere, so maybe just a temporary solution.
	private List<OnContextsChangedListener> contextsChangedListeners = new LinkedList<>();

	public void addOnContextsChangedListener(OnContextsChangedListener l) {
		contextsChangedListeners.add(l);
	}

	public void removeOnContextsChangedListener(OnContextsChangedListener l) {
		contextsChangedListeners.remove(l);
	}
	
	/**
	 * Listener notified whenever the registered list of contexts changes.
	 */
	public interface OnContextsChangedListener {

		/**
		 * Called whenever a new context is created and added.
		 */
		public void contextAdded(Context context);

		/**
		 * Called whenever the whole contexts list was changed.
		 */
		public void contextsChanged();
	}
}
 

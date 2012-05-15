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

package org.parosproxy.paros.model;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.FileXML;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.RecordSessionUrl;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.xml.sax.SAXException;
import org.zaproxy.zap.extension.ascan.ExtensionActiveScan;
import org.zaproxy.zap.extension.spider.ExtensionSpider;


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
	private List<String> excludeFromProxyRegexs = new ArrayList<String>();
	private List<String> excludeFromScanRegexs = new ArrayList<String>();
	private List<String> excludeFromSpiderRegexs = new ArrayList<String>();
	
	// parameters in XML
	private long sessionId = 0;
	private String sessionName = "";
	private SiteMap siteTree = null;
	
	/**
	 * Constructor for the current session.  The current system time will be used as the session ID.
	 * @param sessionId
	 */
	protected Session(Model model) {
		super(ROOT);

		/*try {
			parseFile("xml/untitledsession.xml");
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		// add session variable here
		setSessionId(System.currentTimeMillis());
		setSessionName(Constant.messages.getString("session.untitled"));
		setSessionDesc("");
		
		// create default object
		this.siteTree = SiteMap.createTree(model);
		
		this.model = model;
		
	}
	
	protected void discard() {
	    try {
	        model.getDb().getTableHistory().deleteHistorySession(getSessionId());
        } catch (SQLException e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }
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
     * @return Returns the newState.
     */
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

		readAndParseFile(fileName);
		model.getDb().close(false);
		model.getDb().open(fileName);
		this.fileName = fileName;
		
		//historyList.removeAllElements();

		SiteNode newRoot = new SiteNode(siteTree, -1, Constant.messages.getString("tab.sites"));
		siteTree.setRoot(newRoot);

		// update history reference
		List<Integer> list = model.getDb().getTableHistory().getHistoryList(getSessionId(), HistoryReference.TYPE_MANUAL);
		HistoryReference historyRef = null;
		
		for (int i=0; i<list.size(); i++) {
			// ZAP: Removed unnecessary cast.
			int historyId = list.get(i).intValue();

			try {
				historyRef = new HistoryReference(historyId);
				getSiteTree().addPath(historyRef);
				// ZAP: Load alerts from db
				historyRef.loadAlerts();

				if (i % 100 == 99) Thread.yield();
			} catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
			};
			
		}
		
		// update siteTree reference
		list = model.getDb().getTableHistory().getHistoryList(getSessionId(), HistoryReference.TYPE_SPIDER);
		
		for (int i=0; i<list.size(); i++) {
			// ZAP: Removed unnecessary cast.
			int historyId = list.get(i).intValue();

			try {
				historyRef = new HistoryReference(historyId);
				getSiteTree().addPath(historyRef);

				if (i % 100 == 99) Thread.yield();

			} catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
			};
			
			
		}
		
		// ZAP: expand root
	    View.getSingleton().getSiteTreePanel().expandRoot();

	    // Load the session urls
	    List<RecordSessionUrl> ignoreUrls = model.getDb().getTableSessionUrl().getUrlsForType(RecordSessionUrl.TYPE_EXCLUDE_FROM_PROXY);
	    List<String> urls = new ArrayList<String>(ignoreUrls.size());
	    for (RecordSessionUrl url : ignoreUrls) {
	    	urls.add(url.getUrl());
	    }
	    this.setExcludeFromProxyRegexs(urls);

	    ignoreUrls = model.getDb().getTableSessionUrl().getUrlsForType(RecordSessionUrl.TYPE_EXCLUDE_FROM_SCAN);
	    urls = new ArrayList<String>(ignoreUrls.size());
	    for (RecordSessionUrl url : ignoreUrls) {
	    	urls.add(url.getUrl());
	    }
	    this.setExcludeFromScanRegexs(urls);

	    ignoreUrls = model.getDb().getTableSessionUrl().getUrlsForType(RecordSessionUrl.TYPE_EXCLUDE_FROM_SPIDER);
	    urls = new ArrayList<String>(ignoreUrls.size());
	    for (RecordSessionUrl url : ignoreUrls) {
	    	urls.add(url.getUrl());
	    }
	    this.setExcludeFromSpiderRegexs(urls);

		System.gc();
		
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

	public void setExcludeFromProxyRegexs(List<String> ignoredRegexs) throws SQLException {
		this.excludeFromProxyRegexs = ignoredRegexs;
		Control.getSingleton().setExcludeFromProxyUrls(ignoredRegexs);
		model.getDb().getTableSessionUrl().setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_PROXY, ignoredRegexs);
	}

	public List<String> getExcludeFromScanRegexs() {
		return excludeFromScanRegexs;
	}

	public void setExcludeFromScanRegexs(List<String> ignoredRegexs) throws SQLException {
		this.excludeFromScanRegexs = ignoredRegexs;
		ExtensionActiveScan extAscan = 
			(ExtensionActiveScan) Control.getSingleton().getExtensionLoader().getExtension(ExtensionActiveScan.NAME);
		if (extAscan != null) {
			extAscan.setExcludeList(ignoredRegexs);
		}
		model.getDb().getTableSessionUrl().setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_SCAN, ignoredRegexs);
	}

	public List<String> getExcludeFromSpiderRegexs() {
		return excludeFromSpiderRegexs;
	}

	public void setExcludeFromSpiderRegexs(List<String> ignoredRegexs) throws SQLException {
		this.excludeFromSpiderRegexs = ignoredRegexs;
		ExtensionSpider extSpider = 
			(ExtensionSpider) Control.getSingleton().getExtensionLoader().getExtension(ExtensionSpider.NAME);
		if (extSpider != null) {
			extSpider.setExcludeList(ignoredRegexs);
		}
		model.getDb().getTableSessionUrl().setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_SPIDER, ignoredRegexs);
	}

}

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
// ZAP: 2011/09/19 Handle multipart node name
// ZAP: 2011/12/04 Support deleting alerts
// ZAP: 2012/02/11 Re-ordered icons, added spider icon and notify via SiteMap
// ZAP: 2012/03/03 Moved popups to stdmenus extension
// ZAP: 2012/03/11 Issue 280: Escape URLs in sites tree
// ZAP: 2012/03/15 Changed the methods getQueryParamString and createReference to 
//      use the class StringBuilder instead of StringBuffer 
// ZAP: 2012/07/03 Issue 320: AScan can miss subtrees if invoked via the API
// ZAP: 2012/07/29 Issue 43: Added support for Scope
// ZAP: 2012/08/29 Issue 250 Support for authentication management
// ZAP: 2013/01/29 Handle structural nodes in findNode
// ZAP: 2013/09/26 Issue 656: Content-length: 0 in GET requests
// ZAP: 2014/01/06 Issue 965: Support 'single page' apps and 'non standard' parameter separators
// ZAP: 2014/01/16 Issue 979: Sites and Alerts trees can get corrupted
// ZAP: 2014/03/23 Issue 997: Session.open complains about improper use of addPath
// ZAP: 2014/04/10 Initialise the root SiteNode with a reference to SiteMap
// ZAP: 2014/04/10 Allow to delete history ID to SiteNode map entries
// ZAP: 2014/06/16 Issue 1227: Active scanner sends GET requests with content in request body
// ZAP: 2014/09/22 Issue 1345: Support Attack mode
// ZAP: 2014/11/18 Issue 1408: Extend the structural parameter handling to forms param
// ZAP: 2014/11/27 Issue 1416: Allow spider to be restricted by the number of children
// ZAP: 2014/12/17 Issue 1174: Support a Site filter
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative implementations
// ZAP: 2015/04/02 Issue 1582: Low memory option
// ZAP: 2015/08/19 Change to cope with deprecation of HttpMessage.getParamNameSet(HtmlParameter.Type, String)
// ZAP: 2015/08/19 Issue 1784: NullPointerException when active scanning through the API with a target without scheme
// ZAP: 2015/10/21 Issue 1576: Support data driven content
// ZAP: 2015/11/05 Change findNode(..) methods to match top level nodes
// ZAP: 2015/11/09 Fix NullPointerException when creating a HistoryReference with a request URI without path
// ZAP: 2016/04/21 Issue 2342: Checks non-empty method for deletion of SiteNodes via API 
// ZAP: 2016/04/28 Issue 1171: Raise site and node add or remove events
// ZAP: 2016/07/07 Do not add the message to past history if it already belongs to the node

package org.parosproxy.paros.model;

import java.awt.EventQueue;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpStatusCode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.view.SiteTreeFilter;

public class SiteMap extends DefaultTreeModel {

	private static final long serialVersionUID = 2311091007687218751L;
	
	private enum EventType {ADD, REMOVE};
	
	private static Map<Integer, SiteNode> hrefMap = new HashMap<>();

	private Model model = null;
	
	private SiteTreeFilter filter = null;

    // ZAP: Added log
    private static Logger log = Logger.getLogger(SiteMap.class);
    
    public static SiteMap createTree(Model model) {
        SiteMap siteMap = new SiteMap(null, model);
        SiteNode root = new SiteNode(siteMap, -1, Constant.messages.getString("tab.sites"));
        siteMap.setRoot(root);
        hrefMap = new HashMap<>();
        return siteMap;
    }
    
    public SiteMap(SiteNode root, Model model) {        
        super(root);
        this.model = model;
    }

    /**
     * Return the a HttpMessage of the same type under the tree path.
     * @param msg
     * @return	null = not found
     */
    public synchronized HttpMessage pollPath(HttpMessage msg) {
        SiteNode resultNode = null;
        URI uri = msg.getRequestHeader().getURI();
        
        SiteNode parent = (SiteNode) getRoot();
        String folder;
        
        try {
            String host = getHostName(uri);
            
            // no host yet
            parent = findChild(parent, host);
            if (parent == null) {
                return null;
        	}
            List<String> path = model.getSession().getTreePath(uri);
            if (path.size() == 0) {
            	// Its a top level node
            	resultNode = parent;
            }
            for (int i=0; i < path.size(); i++) {
            	folder = path.get(i);
                if (folder != null && !folder.equals("")) {
                    if (i == path.size()-1) {
                        String leafName = getLeafName(folder, msg);
                        resultNode = findChild(parent, leafName);
                    } else {
                        parent = findChild(parent, folder);
                        if (parent == null) {
                            return null;
                        }
                    }
                }
            }
        } catch (URIException e) {
            // ZAP: Added error
            log.error(e.getMessage(), e);
        }
        
        if (resultNode == null || resultNode.getHistoryReference() == null) {
            return null;
        }
        
        HttpMessage nodeMsg = null;
        try {
            nodeMsg = resultNode.getHistoryReference().getHttpMessage();
        } catch (Exception e) {
            // ZAP: Added error
            log.error(e.getMessage(), e);
        }
        return nodeMsg;
    }

    public SiteNode findNode(HttpMessage msg) {
    	return this.findNode(msg, false);
    }

    
    public synchronized SiteNode findNode(HttpMessage msg, boolean matchStructural) {
    	if (Constant.isLowMemoryOptionSet()) {
    		throw new InvalidParameterException("SiteMap should not be accessed when the low memory option is set");
    	}
    	if (msg == null || msg.getRequestHeader() == null) {
    		return null;
    	}
        SiteNode resultNode = null;
        URI uri = msg.getRequestHeader().getURI();
        
        SiteNode parent = (SiteNode) getRoot();
        String folder = "";
        
        try {
            
        	String host = getHostName(uri);
        	
            // no host yet
            parent = findChild(parent, host);
            if (parent == null) {
                return null;
        	}
            
            List<String> path = model.getSession().getTreePath(msg);
            if (path.size() == 0) {
            	// Its a top level node
            	resultNode = parent;
            }
            for (int i=0; i < path.size(); i++) {
            	folder = path.get(i);
                if (folder != null && !folder.equals("")) {
                    if (i == path.size()-1) {
                    	if (matchStructural) {
                            resultNode = findChild(parent, folder);
                    	} else {
                    		String leafName = getLeafName(folder, msg);
                    		resultNode = findChild(parent, leafName);
                    	}
                    } else {
                        parent = findChild(parent, folder);
                        if (parent == null) {
                            return null;
                        }
                    }
                }
            }
        } catch (URIException e) {
            log.error(e.getMessage(), e);
        }
        
        return resultNode;
    }

    public synchronized SiteNode findNode(URI uri) {
    	// Look for 'structural' nodes first
    	SiteNode node = this.findNode(uri, null, null);
    	if (node != null) {
    		return node;
    	}
    	return this.findNode(uri, "GET", null);
    }
    
    public synchronized SiteNode findNode(URI uri, String method, String postData) {
    	if (Constant.isLowMemoryOptionSet()) {
    		throw new InvalidParameterException("SiteMap should not be accessed when the low memory option is set");
    	}
        SiteNode resultNode = null;
        String folder = "";
        
        try {
        	String host = getHostName(uri);
            
            // no host yet
            resultNode = findChild((SiteNode) getRoot(), host);
            if (resultNode == null) {
                return null;
        	}
            
            List<String> path = model.getSession().getTreePath(uri);
            for (int i=0; i < path.size(); i++) {
            	folder = path.get(i);
                
                if (folder != null && !folder.equals("")) {
                    if (i == path.size()-1) {
                        String leafName = getLeafName(folder, uri, method, postData);
                        resultNode = findChild(resultNode, leafName);
                    } else {
                    	resultNode = findChild(resultNode, folder);
                        if (resultNode == null) {
                            return null;
                        }
                    }
                }
            }
        } catch (URIException e) {
            log.error(e.getMessage(), e);
        }
        
        return resultNode;
    }

    /*
     * Find the closest parent for the message - no new nodes will be created
     */
    public synchronized SiteNode findClosestParent(HttpMessage msg) {
    	if (msg == null || msg.getRequestHeader() == null) {
    		return null;
    	}
    	return this.findClosestParent(msg.getRequestHeader().getURI());
    }
    
    /*
     * Find the closest parent for the uri - no new nodes will be created
     */
    public synchronized SiteNode findClosestParent(URI uri) {
    	if (uri == null) {
    		return null;
    	}
        SiteNode lastParent = null;
        SiteNode parent = (SiteNode) getRoot();
        String folder = "";
        
        try {
        	String host = getHostName(uri);
        	
            // no host yet
            parent = findChild(parent, host);
            if (parent == null) {
                return null;
        	}
            lastParent = parent;
            
            List<String> path = model.getSession().getTreePath(uri);
            for (int i=0; i < path.size(); i++) {
            	folder = path.get(i);
                if (folder != null && !folder.equals("")) {
                    if (i == path.size()-1) {
                        lastParent = parent;
                    } else {
                        parent = findChild(parent, folder);
                        if (parent == null) {
                            break;
                        }
                        lastParent = parent;
                    }
                }
            }
        } catch (URIException e) {
            log.error(e.getMessage(), e);
        }
        
        return lastParent;
    }

    /**
     * Add the HistoryReference into the SiteMap.
     * This method will rely on reading message from the History table.
     * Note that this method must only be called on the EventDispatchThread
     * @param ref
     */
    public synchronized SiteNode addPath(HistoryReference ref) {
    	if (Constant.isLowMemoryOptionSet()) {
    		throw new InvalidParameterException("SiteMap should not be accessed when the low memory option is set");
    	}

        HttpMessage msg = null;
        try {
            msg = ref.getHttpMessage();
        } catch (Exception e) {
            // ZAP: Added error
            log.error(e.getMessage(), e);
            return null;
        }
        
        return addPath(ref, msg);
    }
    
    /**
     * Add the HistoryReference with the corresponding HttpMessage into the SiteMap.
     * This method saves the msg to be read from the reference table.  Use 
     * this method if the HttpMessage is known.
     * Note that this method must only be called on the EventDispatchThread
     * @param msg
     * @return 
     */
    public SiteNode addPath(HistoryReference ref, HttpMessage msg) {
    	if (Constant.isLowMemoryOptionSet()) {
    		throw new InvalidParameterException("SiteMap should not be accessed when the low memory option is set");
    	}

    	if (View.isInitialised() && Constant.isDevBuild() && ! EventQueue.isDispatchThread()) {
    		// In developer mode log an error if we're not on the EDT
    		// Adding to the site tree on GUI ('initial') threads causes problems
    		log.error("SiteMap.addPath not on EDT " + Thread.currentThread().getName(), new Exception());
    	}
        
        URI uri = msg.getRequestHeader().getURI();
        log.debug("addPath " + uri.toString());
        
        SiteNode parent = (SiteNode) getRoot();
        SiteNode leaf = null;
        String folder = "";
        
        try {
            
        	String host = getHostName(uri);
            
            // add host
            parent = findAndAddChild(parent, host, ref, msg);
                     
            List<String> path = model.getSession().getTreePath(msg);
            for (int i=0; i < path.size(); i++) {
            	folder = path.get(i);
                if (folder != null && !folder.equals("")) {
                    if (i == path.size()-1) {
                        leaf = findAndAddLeaf(parent, folder, ref, msg);
                        ref.setSiteNode(leaf);
                    } else {
                        parent = findAndAddChild(parent, folder, ref, msg);
                    }
                }
            }
            if (leaf == null) {
            	// No leaf found, which means the parent was really the leaf
            	// The parent will have been added with a 'blank' href, so replace it with the real one
            	parent.setHistoryReference(ref);
            	leaf = parent;
            }
            
        } catch (Exception e) {
            // ZAP: Added error
            log.error("Exception adding " + uri.toString() + " " + e.getMessage(), e);
        }
        
        if (hrefMap.get(ref.getHistoryId()) == null) {
            hrefMap.put(ref.getHistoryId(), leaf);
        }

        return leaf;
    }
    
    private SiteNode findAndAddChild(SiteNode parent, String nodeName, HistoryReference baseRef, HttpMessage baseMsg) throws URIException, HttpMalformedHeaderException, NullPointerException, DatabaseException {
    	log.debug("findAndAddChild " + parent.getNodeName() + " / " + nodeName);    	
        SiteNode result = findChild(parent, nodeName);
        if (result == null) {
        	SiteNode newNode =null;
        	if(!baseRef.getCustomIcons().isEmpty()) {
                newNode = new SiteNode(this, baseRef.getHistoryType(), nodeName);
                newNode.setCustomIcons(baseRef.getCustomIcons(), baseRef.getClearIfManual());
        	} else {
        		newNode = new SiteNode(this, baseRef.getHistoryType(), nodeName);
        	}
            
            int pos = parent.getChildCount();
            for (int i=0; i< parent.getChildCount(); i++) {
            	if (((SiteNode)parent.getChildAt(i)).isParentOf(nodeName)) {
                    pos = i;
                    break;
                }
            }
            insertNodeInto(newNode, parent, pos);

            result = newNode;
            result.setHistoryReference(createReference(result, baseRef, baseMsg));

            // Check if its in or out of scope - has to be done after the node is entered into the tree
            newNode.setIncludedInScope(model.getSession().isIncludedInScope(newNode), true);
            newNode.setExcludedFromScope(model.getSession().isExcludedFromScope(newNode), true);
            hrefMap.put(result.getHistoryReference().getHistoryId(), result);

            applyFilter(newNode);
            handleEvent(parent, result, EventType.ADD);

        }
        // ZAP: Cope with getSiteNode() returning null
        if (baseRef.getSiteNode() == null) {
        	baseRef.setSiteNode(result);
        }
        return result;
    }
    
    private SiteNode findChild(SiteNode parent, String nodeName) {
    	// ZAP: Added debug
    	log.debug("findChild " + parent.getNodeName() + " / " + nodeName);
    	
        for (int i=0; i<parent.getChildCount(); i++) {
            SiteNode child = (SiteNode) parent.getChildAt(i);
            if (child.getNodeName().equals(nodeName)) {
                return child;
            }
        }
        return null;
    }
    
    private SiteNode findAndAddLeaf(SiteNode parent, String nodeName, HistoryReference ref, HttpMessage msg) {
    	// ZAP: Added debug
    	log.debug("findAndAddLeaf " + parent.getNodeName() + " / " + nodeName);    	

        String leafName = getLeafName(nodeName, msg);
        SiteNode node = findChild(parent, leafName);
        if (node == null) {
        	if(!ref.getCustomIcons().isEmpty()){
        		node = new SiteNode(this, ref.getHistoryType(), leafName);
        		node.setCustomIcons(ref.getCustomIcons(), ref.getClearIfManual());
        	} else {
        		node = new SiteNode(this, ref.getHistoryType(), leafName);
        	}
            node.setHistoryReference(ref);
            
            hrefMap.put(ref.getHistoryId(), node);
            
            int pos = parent.getChildCount();
            for (int i=0; i< parent.getChildCount(); i++) {
            	if (((SiteNode)parent.getChildAt(i)).isParentOf(nodeName)) {
                    pos = i;
                    break;
                }
            }
            // ZAP: cope with getSiteNode() returning null
            if (ref.getSiteNode() == null) {
            	ref.setSiteNode(node);
            }

            insertNodeInto(node, parent, pos);
            
            // Check if its in or out of scope - has to be done after the node is entered into the tree
            node.setIncludedInScope(model.getSession().isIncludedInScope(node), true);
            node.setExcludedFromScope(model.getSession().isExcludedFromScope(node), true);

            this.applyFilter(node);

            handleEvent(parent, node, EventType.ADD);            
        } else if (hrefMap.get(ref.getHistoryId()) != node) {
           
            // do not replace if
            // - use local copy (304). only use if 200

            if (msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK) {
                // replace node HistoryReference to this new child if this is a spidered record.
                node.setHistoryReference(ref);
                ref.setSiteNode(node);
            } else {
                node.getPastHistoryReference().add(ref);
                ref.setSiteNode(node);
            }
            hrefMap.put(ref.getHistoryId(), node);
        }
        return node;
    }
    
	private String getLeafName(String nodeName, HttpMessage msg) {
        // add \u007f to make GET/POST node appear at the end.
        //String leafName = "\u007f" + msg.getRequestHeader().getMethod()+":"+nodeName;
        String leafName = msg.getRequestHeader().getMethod()+":"+nodeName;
        
        leafName = leafName + getQueryParamString(msg.getParamNameSet(HtmlParameter.Type.url));
        
        // also handle POST method query in body
        if (msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
        	String contentType = msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
        	if (contentType != null && contentType.startsWith("multipart/form-data")) {
        		leafName = leafName + "(multipart/form-data)";
        	} else {
        		leafName = leafName + getQueryParamString(msg.getParamNameSet(HtmlParameter.Type.form));
        	}
        }
        
        return leafName;
        
    }
    
    private String getLeafName(String nodeName, URI uri, String method, String postData) {
        String leafName;
        
        if (method != null && !method.isEmpty()) {
        	leafName = method + ":" + nodeName;
        } else {
        	leafName = nodeName;
        }
        
        try {
            leafName = leafName + getQueryParamString(model.getSession().getUrlParams(uri));

            // also handle POST method query in body
            if (method != null && method.equalsIgnoreCase(HttpRequestHeader.POST)) {
                leafName = leafName + getQueryParamString(model.getSession().getFormParams(uri, postData));
            }
        } catch (URIException e) {
            // ZAP: Added error
            log.error(e.getMessage(), e);
        }
        return leafName;
        
    }
    
    private String getQueryParamString(Map<String, String> map) {
	    TreeSet<String> set = new TreeSet<>();
	    for (Entry<String, String> entry : map.entrySet()) {
	    	set.add(entry.getKey());	    
	    }
    	return this.getQueryParamString(set);
    }

    private String getQueryParamString(SortedSet<String> querySet) {
    	StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = querySet.iterator();
        for (int i=0; iterator.hasNext(); i++) {
            String name = iterator.next();
            if (name == null) {
                continue;
            }
            if (i > 0) {
                sb.append(',');
            }
            if (name.length() > 40) {
            	// Truncate
            	name = name.substring(0, 40);
            }
            sb.append(name);
        }

        String result = "";
        if (sb.length()>0) {
        	result = sb.insert(0, '(').append(')').toString();
        } 
        
        return result;
    }
    
    private HistoryReference createReference(SiteNode node, HistoryReference baseRef, HttpMessage base) throws HttpMalformedHeaderException, DatabaseException, URIException, NullPointerException {
        TreeNode[] path = node.getPath();
        StringBuilder sb = new StringBuilder();
        String nodeName;
        String uriPath = baseRef.getURI().getPath();
        if (uriPath == null) {
            uriPath = "";
        }
        String [] origPath = uriPath.split("/");
        for (int i=1; i<path.length; i++) {
        	// ZAP Cope with error counts in the node names
        	nodeName = ((SiteNode)path[i]).getNodeName();
        	if (((SiteNode)path[i]).isDataDriven()) {
            	// Retrieve original name..
            	if (origPath.length > i-1) {
            		log.debug("Replace Data Driven element " + nodeName + " with " + origPath[i-1]);
            		sb.append(origPath[i-1]);
            	} else {
            		log.error("Failed to determine original node name for element " + i +
            				nodeName + " original request: " + baseRef.getURI().toString());
                    sb.append(nodeName);
            	}
            } else {
                sb.append(nodeName);
            }
            if (i<path.length-1) {
                sb.append('/');
            }
        }
        HttpMessage newMsg = base.cloneRequest();
        
        // ZAP: Prevents a possible URIException, because the passed string is not escaped.
		URI uri = new URI(sb.toString(), false);
        newMsg.getRequestHeader().setURI(uri);
        newMsg.getRequestHeader().setMethod(HttpRequestHeader.GET);
        newMsg.getRequestBody().setBody("");
        newMsg.getRequestHeader().setHeader(HttpHeader.CONTENT_TYPE, null);
        newMsg.getRequestHeader().setHeader(HttpHeader.CONTENT_LENGTH, null);
        
		//HistoryReference historyRef = new HistoryReference(model.getSession(), baseRef.getHistoryType(), newMsg);
		HistoryReference historyRef = new HistoryReference(model.getSession(), HistoryReference.TYPE_TEMPORARY, newMsg);
		
        return historyRef;
    }

    public SiteNode getSiteNode (int href) {
    	return hrefMap.get(href);
    }
    
    public void removeHistoryReference(int historyId) {
        hrefMap.remove(Integer.valueOf(historyId));
    }

    // returns a representation of the host name in the site map
	private String getHostName(URI uri) throws URIException {
		StringBuilder host = new StringBuilder(); 				
		
		String scheme = uri.getScheme();
		if (scheme == null) {
			scheme = "http";
		} else {
			scheme = scheme.toLowerCase();
		}
		host.append(scheme).append("://").append(uri.getHost());
		
		int port = uri.getPort();		
		if (port != -1 &&
				((port == 80 && !"http".equals(scheme)) ||
				(port == 443 && !"https".equals(scheme) ||
				(port != 80 && port != 443)))) {
			host.append(":").append(port);
		}
		
		return host.toString();
	}
	
	/**
	 * Set the filter for the sites tree
	 * @param filter
	 */
	public void setFilter (SiteTreeFilter filter) {
		this.filter = filter;
		SiteNode root = (SiteNode) getRoot();
		setFilter(filter, root);
		// Never filter the root node
		root.setFiltered(false);
	}
	
	private boolean setFilter (SiteTreeFilter filter, SiteNode node) {
		boolean filtered = ! filter.matches(node); 
		for (int i=0; i < node.getChildCount(); i++) {
			if (!setFilter(filter, (SiteNode)node.getChildAt(i))) {
				// Always shoe a node if at least one of its children as not filtered
				filtered = false;
			}
		}
		node.setFiltered(filtered);
		return filtered;
	}

	/**
	 * Clear the sites tree filter - all nodes will become visible
	 */
	public void clearFilter () {
		this.filter = null;
		clearFilter((SiteNode) getRoot());
	}
	
	private void clearFilter (SiteNode node) {
		node.setFiltered(false);
		for (int i=0; i < node.getChildCount(); i++) {
			clearFilter((SiteNode)node.getChildAt(i));
		}
	}

	/**
	 * Applies the current filter (if there is one) to the node.
	 * This should be called anytime a change is made to a node that could affect its visibility in the filtered tree
	 * @param node
	 */
	protected void applyFilter (SiteNode node) {
    	if (filter != null) {
    		boolean filtered = this.setFilter(filter, node);
    		SiteNode parent = node.getParent();
    		if (parent != null && ! filtered && parent.isFiltered()) {
    			// This node is no longer filtered but its parent is, unfilter the parent so it becomes visible
    			this.clearParentFilter(parent);
    		}
    	} else {
    		node.setFiltered(false);
    	}
	}

	/**
	 * Recurse up the tree setting all of the parent nodes to unfiltered
	 * @param parent
	 */
	private void clearParentFilter (SiteNode parent) {
		if (parent != null) {
			parent.setFiltered(false);
			clearParentFilter(parent.getParent());
		}
	}
	
	@Override
	public void removeNodeFromParent(MutableTreeNode node) {
		SiteNode parent=(SiteNode)node.getParent();
		super.removeNodeFromParent(node);
		handleEvent(parent, (SiteNode)node, EventType.REMOVE);
	}

	/**
	 * Handles the publishing of the add or remove event. Node events are always published.
	 * Site events are only published when the parent of the node is the root of the tree.
	 * 
	 * @param parent relevant parent node
	 * @param node the site node the action is being carried out for
	 * @param eventType the type of event occurring (ADD or REMOVE)
	 * @see EventType
	 * @since 2.5.0
	 */
	private void handleEvent(SiteNode parent, SiteNode node, EventType eventType) {
		switch (eventType) {
		case ADD:
			publishEvent(SiteMapEventPublisher.SITE_NODE_ADDED_EVENT, node);
			if (parent == getRoot()) {
				publishEvent(SiteMapEventPublisher.SITE_ADDED_EVENT, node);
			}
			break;
		case REMOVE:
			publishEvent(SiteMapEventPublisher.SITE_NODE_REMOVED_EVENT, node);
			if(parent == getRoot()) {
				publishEvent(SiteMapEventPublisher.SITE_REMOVED_EVENT, node);
			}
		}
	}
	
	/**
	 * Publish the event being carried out.
	 * 
	 * @param event the event that is happening
	 * @param node the node being acted upon
	 * @since 2.5.0
	 */
	private static void publishEvent(String event, SiteNode node) {
		ZAP.getEventBus().publishSyncEvent(SiteMapEventPublisher.getPublisher(), new Event(SiteMapEventPublisher.getPublisher(), event, new Target(node)));
	}
}

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

package org.parosproxy.paros.model;

import java.awt.EventQueue;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpStatusCode;
import org.parosproxy.paros.view.View;

public class SiteMap extends DefaultTreeModel {

	private static final long serialVersionUID = 2311091007687218751L;
	
	private static Map<Integer, SiteNode> hrefMap = new HashMap<>();

	private Model model = null;

    // ZAP: Added log
    private static Logger log = Logger.getLogger(SiteMap.class);
    
    public static SiteMap createTree(Model model) {
        SiteNode root = new SiteNode(null, -1, Constant.messages.getString("tab.sites"));
        hrefMap = new HashMap<>();
        return new SiteMap(root, model);        
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
            
            List<String> path = model.getSession().getTreePath(uri);
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

    /**
     * Add the HistoryReference into the SiteMap.
     * This method will rely on reading message from the History table.
     * Note that this method must only be called on the EventDispatchThread
     * @param ref
     */
    public synchronized SiteNode addPath(HistoryReference ref) {

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
                        
            List<String> path = model.getSession().getTreePath(uri);
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
    
    private SiteNode findAndAddChild(SiteNode parent, String nodeName, HistoryReference baseRef, HttpMessage baseMsg) throws URIException, HttpMalformedHeaderException, NullPointerException, SQLException {
    	// ZAP: Added debug
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
        } else {
           
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
        
        String query = "";

        try {
            query = msg.getRequestHeader().getURI().getQuery();
        } catch (URIException e) {
            // ZAP: Added error
            log.error(e.getMessage(), e);
        }
        if (query == null) {
            query = "";
        }
        leafName = leafName + getQueryParamString(msg.getParamNameSet(HtmlParameter.Type.url, query));
        
        // also handle POST method query in body
        query = "";
        if (msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
        	String contentType = msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
        	if (contentType != null && contentType.startsWith("multipart/form-data")) {
        		leafName = leafName + "(multipart/form-data)";
        	} else {
        		query = msg.getRequestBody().toString();
        		leafName = leafName + getQueryParamString(msg.getParamNameSet(HtmlParameter.Type.form, query));
        	}
        }
        
        return leafName;
        
    }
    
    private String getLeafName(String nodeName, URI uri, String method, String postData) {
        String leafName;
        
        if (method != null) {
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
    
    private HistoryReference createReference(SiteNode node, HistoryReference baseRef, HttpMessage base) throws HttpMalformedHeaderException, SQLException, URIException, NullPointerException {
        TreeNode[] path = node.getPath();
        StringBuilder sb = new StringBuilder();
        String nodeName;
        for (int i=1; i<path.length; i++) {
        	// ZAP Cope with error counts in the node names
        	nodeName = ((SiteNode)path[i]).getNodeName();
            sb.append(nodeName);
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
        newMsg.getRequestHeader().setHeader(HttpHeader.CONTENT_LENGTH, null);
        
		//HistoryReference historyRef = new HistoryReference(model.getSession(), baseRef.getHistoryType(), newMsg);
		HistoryReference historyRef = new HistoryReference(model.getSession(), HistoryReference.TYPE_TEMPORARY, newMsg);
		
        return historyRef;
    }

    public SiteNode getSiteNode (int href) {
    	return hrefMap.get(href);
    }
    
    // returns a representation of the host name in the site map
	private String getHostName(URI uri) throws URIException {
		StringBuilder host = new StringBuilder(); 				
		
		String scheme = uri.getScheme().toLowerCase();
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
}

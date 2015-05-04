/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordStructure;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

public class SessionStructure {

	public static final String ROOT = "Root";

    private static final Logger log = Logger.getLogger(SessionStructure.class);

    public static StructuralNode addPath(Session session, HistoryReference ref, HttpMessage msg) {
    	if (!Constant.isLowMemoryOptionSet()) {
   			return new StructuralSiteNode(session.getSiteTree().addPath(ref, msg));
    	} else {
	    	try {
				List<String> paths = session.getTreePath(msg);
	        	String host = getHostName(msg.getRequestHeader().getURI());
				
	        	return new StructuralTableNode(
	        			addStructure (session, host, msg, paths, paths.size(), ref.getHistoryId())); 
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return null;
			}
    	}
    }
    
	public static StructuralNode find(long sessionId, URI uri, String method, String postData) throws DatabaseException, URIException {
		Model model = Model.getSingleton();
    	if (!Constant.isLowMemoryOptionSet()) {
			SiteNode node = model.getSession().getSiteTree().findNode(uri, method, postData);
			if (node == null) {
				return null;
			}
			return new StructuralSiteNode(node);
		}

		String nodeName = getNodeName(sessionId, uri, method, postData);
		RecordStructure rs = model.getDb().getTableStructure().find(sessionId, nodeName, method);
		if (rs == null) {
			return null;
		}
		return new StructuralTableNode(rs);
	}

	private static String getNodeName(long sessionId, URI uri, String method, String postData) throws URIException {
		
		Session session = Model.getSingleton().getSession();
		List<String> paths = session.getTreePath(uri);
		
    	String host = getHostName(uri);
		String nodeUrl = pathsToUrl(host, paths, paths.size());
		
		if (postData != null) {
			String params = getParams(session, uri, postData);
			if (params.length() > 0) {
				nodeUrl = nodeUrl + " " + params;
			}
		}
		return nodeUrl;

	}

	private static String getNodeName(Session session, String host, HttpMessage msg, 
    		List<String> paths, int size) throws URIException {
		String nodeUrl = pathsToUrl(host, paths, size);
		
		if (msg != null) {
			String params = getParams(session, msg);
			if (params.length() > 0) {
				nodeUrl = nodeUrl + " " + params;
			}
		}
		return nodeUrl;
	}

    private static RecordStructure addStructure (Session session, String host, HttpMessage msg, 
    		List<String> paths, int size, int historyId) throws DatabaseException, URIException {
		//String nodeUrl = pathsToUrl(host, paths, size);
		String nodeName = getNodeName(session, host, msg, paths, size);
		String parentName = pathsToUrl(host, paths, size-1);
		String url = "";
		
		if (msg != null) {
			url = msg.getRequestHeader().getURI().toString();
			String params = getParams(session, msg);
			if (params.length() > 0) {
				nodeName = nodeName + " " + params;
			}
		}
		
		String method = HttpRequestHeader.GET;
		if (msg != null) {
			method = msg.getRequestHeader().getMethod();
		}
		
		RecordStructure msgRs = Model.getSingleton().getDb().getTableStructure().find(session.getSessionId(), nodeName, method);
		if (msgRs == null) {
			long parentId = -1;
			if (!nodeName.equals("Root")) {
				HttpMessage tmpMsg = null;
				int parentHistoryId = -1;
				if (!parentName.equals("Root")) {
					tmpMsg = getTempHttpMessage(session, parentName, msg);
					parentHistoryId = tmpMsg.getHistoryRef().getHistoryId();
				}
				RecordStructure parentRs = addStructure(session, host, tmpMsg, paths, size-1, parentHistoryId); 
				parentId = parentRs.getStructureId();
			}
			msgRs = Model.getSingleton().getDb().getTableStructure().insert(
					session.getSessionId(), parentId, historyId, nodeName, url, method);
		}

		return msgRs;
    	
    }
    
    private static HttpMessage getTempHttpMessage(Session session, String url, HttpMessage base) {
        try {
			HttpMessage newMsg = base.cloneRequest();
			URI uri = new URI(url, false);
			newMsg.getRequestHeader().setURI(uri);
			newMsg.getRequestHeader().setMethod(HttpRequestHeader.GET);
			newMsg.getRequestBody().setBody("");
			newMsg.getRequestHeader().setHeader(HttpHeader.CONTENT_TYPE, null);
			newMsg.getRequestHeader().setHeader(HttpHeader.CONTENT_LENGTH, null);
			
			HistoryReference historyRef = new HistoryReference(session, HistoryReference.TYPE_TEMPORARY, newMsg);
			newMsg.setHistoryRef(historyRef);

			return newMsg;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
        return null;
    }

    private static String pathsToUrl(String host, List<String> paths, int size) {
    	if (size < 0) {
    		return ROOT;
    	}
    	StringBuilder sb = new StringBuilder();
    	sb.append(host);
    	int i = 1;
    	for (String path : paths) {
    		if (i > size) {
    			break;
    		}
    		if (sb.length() > 0) {
    			sb.append("/");
    		}
    		sb.append(path);
    		i++;
    	}
    	return sb.toString();
    	
    }

	private static String getHostName(URI uri) throws URIException {
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

	private static String getParams(Session session, HttpMessage msg) throws URIException {
        // add \u007f to make GET/POST node appear at the end.
        //String leafName = "\u007f" + msg.getRequestHeader().getMethod()+":"+nodeName;
		/*
        String leafName = "";
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
        */
        String postData = null;
        if (msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
        	String contentType = msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
        	if (contentType != null && contentType.startsWith("multipart/form-data")) {
        		postData = "(multipart/form-data)";
        	} else {
        		postData = msg.getRequestBody().toString();
        	}
        }
        
        return getParams(session, msg.getRequestHeader().getURI(), postData);
    }

	private static String getParams(Session session, URI uri, String postData) throws URIException {
        String leafName = "";
        String query = "";

        try {
            query = uri.getQuery();
        } catch (URIException e) {
            log.error(e.getMessage(), e);
        }
        if (query == null) {
            query = "";
        }
        leafName = leafName + getQueryParamString(session.getUrlParams(uri));
        
        // also handle POST method query in body
        query = "";
        if (postData != null && postData.length() > 0) {
        	if (postData.equals("multipart/form-data")) {
        		leafName = leafName + "(multipart/form-data)";
        	} else {
        		leafName = leafName + getQueryParamString(session.getFormParams(uri, postData));
        	}
        }
        
        return leafName;
        
    }
    private static String getQueryParamString(Map<String, String> map) {
	    TreeSet<String> set = new TreeSet<>();
	    for (Entry<String, String> entry : map.entrySet()) {
	    	set.add(entry.getKey());	    
	    }
    	return getQueryParamString(set);
    }

	private static String getQueryParamString(SortedSet<String> querySet) {
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
}

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
package org.parosproxy.paros.core.scanner;

import java.io.IOException;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.network.HttpStatusCode;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Analyser {
	
	/** remove HTML HEAD as this may contain expiry time which dynamic changes */
	private static final String p_REMOVE_HEADER = "(?m)(?i)(?s)<HEAD>.*?</HEAD>";
	private static final Pattern patternNotFound = Pattern.compile("(\\bnot\\b(found|exist))|(\\b404\\berror\\b)|(\\berror\\b404\\b)", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);

    private static Random	staticRandomGenerator = 	new Random();
	private static final String[] staticSuffixList = { ".cfm", ".jsp", ".php", ".asp", ".aspx", ".dll", ".exe", ".pl"};

	private HttpSender	httpSender = null;
	private TreeMap		mapVisited = new TreeMap();
	private boolean		isStop = false;
	
	public Analyser() {
        
    }
    
    public Analyser(HttpSender httpSender) {
        this.httpSender = httpSender;
    }
	
    public boolean isStop() {
        return isStop;
    }
    
    public void stop() {
        isStop = true;
    }
    
    public void start(SiteNode node) {
        inOrderAnalyse(node);
    }
    
	private void addAnalysedHost(URI uri, HttpMessage msg, int errorIndicator) {
        mapVisited.put(uri.toString(), new SampleResponse(msg, errorIndicator));
	}
	
	/**
	Analyse a single folder entity.  Results are stored into mAnalysedEntityTable.
	*/
	private void analyse(SiteNode node) throws Exception {

		// if analysed already, return;
		// move to host part
		if (node.getHistoryReference() == null) {
		    return;
		}
		
		HttpMessage baseMsg = (HttpMessage) node.getHistoryReference().getHttpMessage();
		URI baseUri = (URI) baseMsg.getRequestHeader().getURI().clone();

		baseUri.setQuery(null);
        //System.out.println("analysing: " + baseUri.toString());

		
		// already exist one.  no need to test
		if (mapVisited.get(baseUri.toString()) != null) {
			return;
		}

		String path = getRandomPathSuffix(node, baseUri);
		HttpMessage msg = baseMsg.cloneRequest();
		
		URI uri = (URI) baseUri.clone();
		uri.setPath(path);
		msg.getRequestHeader().setURI(uri);
        //System.out.println("analysing 2: " + uri);
        
		sendAndReceive(msg);

		// standard RFC response, no further check is needed
		
		
		if (msg.getResponseHeader().getStatusCode() == HttpStatusCode.NOT_FOUND) {
			addAnalysedHost(baseUri, msg, SampleResponse.ERROR_PAGE_RFC);
			return;
		}

		if (HttpStatusCode.isRedirection(msg.getResponseHeader().getStatusCode())) {
			addAnalysedHost(baseUri, msg, SampleResponse.ERROR_PAGE_REDIRECT);
			return;
		}
		
		if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
			addAnalysedHost(baseUri, msg, SampleResponse.ERROR_PAGE_NON_RFC);
			return;
		}
	
		HttpMessage msg2 = baseMsg.cloneRequest();
		URI uri2 = msg2.getRequestHeader().getURI();
		String path2 = getRandomPathSuffix(node, uri2);
		uri2 = (URI) baseUri.clone();
		uri2.setPath(path2);
		msg2.getRequestHeader().setURI(uri2);
		sendAndReceive(msg2);

		// remove HTML HEAD as this may contain expiry time which dynamic changes		
		String resBody1 = msg.getResponseBody().toString().replaceAll(p_REMOVE_HEADER, "");
		String resBody2 = msg2.getResponseBody().toString().replaceAll(p_REMOVE_HEADER, "");

		// check if page is static.  If so, remember this static page
		if (resBody1.equals(resBody2)) {
		    msg.getResponseBody().setBody(resBody1);
			addAnalysedHost(baseUri, msg, SampleResponse.ERROR_PAGE_STATIC);
			return;
		}

		// else check if page is dynamic but deterministic
		resBody1 = resBody1.replaceAll(getPathRegex(uri), "").replaceAll("\\s[012]\\d:[0-5]\\d:[0-5]\\d\\s","");
		resBody2 = resBody2.replaceAll(getPathRegex(uri2), "").replaceAll("\\s[012]\\d:[0-5]\\d:[0-5]\\d\\s","");
		if (resBody1.equals(resBody2)) {
		    msg.getResponseBody().setBody(resBody1);
			addAnalysedHost(baseUri, msg, SampleResponse.ERROR_PAGE_DYNAMIC_BUT_DETERMINISTIC);
			return;
		}

		// else mark app "undeterministic".
		addAnalysedHost(baseUri, msg, SampleResponse.ERROR_PAGE_UNDETERMINISTIC);
	
	}
	
	/**
	Get a suffix from the children which exists in staticSuffixList.
	An option is provided to check recursively.    Note that the immediate
	children are always checked first before further recursive check is done.
	@param	entity	The current entity.
	@param	performRecursiveCheck	True = get recursively the suffix from all the children.
	@return	The suffix ".xxx" is returned.  If there is no suffix found, an empty string is returned.
	*/
	private String getChildSuffix(SiteNode node, boolean performRecursiveCheck) {

		String resultSuffix = "";
		String suffix = null;
		SiteNode child = null;
        HistoryReference ref = null;
		HttpMessage msg = null;
		try {

			for (int i=0; i<staticSuffixList.length; i++) {
				suffix = staticSuffixList[i];
				for (int j=0; j<node.getChildCount(); j++) {
					child = (SiteNode) node.getChildAt(j);
                    ref = child.getHistoryReference();
					try {
					    msg = ref.getHttpMessage();
                        if (msg.getRequestHeader().getURI().getPath().endsWith(suffix)) {
					        return suffix;
					    }
					} catch (Exception e) {
					}
				}
			}
			
			if (performRecursiveCheck) {
				for (int j=0; j<node.getChildCount(); j++) {
					resultSuffix = getChildSuffix((SiteNode) node.getChildAt(j), performRecursiveCheck);
					if (!resultSuffix.equals("")) {
						return resultSuffix;
					}
				}
			}
														
		} catch (Exception e) {
		}
		
		return resultSuffix;
	}
	
	private String getPathRegex(URI uri) throws URIException {
	    URI newUri = (URI) uri.clone();
	    String query = newUri.getQuery();
	    StringBuffer sb = new StringBuffer(100);
		
		// case should be sensitive
		//sb.append("(?i)");

	    newUri.setQuery(null);
	    
		sb.append(newUri.toString().replaceAll("\\.", "\\."));
		if (query != null) {
			String queryPattern = "(\\?" + query + ")?";
			sb.append(queryPattern);
		}
		
		return sb.toString();
	}

	/**
	Get a random path relative to the current entity.  Whenever possible, use a suffix exist in the children
	according to a priority of staticSuffixList.
	@param	entity	The current entity.
	@param	uri		The uri of the current entity.
	@return	A random path (eg /folder1/folder2/1234567.chm) relative the entity.
	 * @throws URIException
	*/	
	private String getRandomPathSuffix(SiteNode node, URI uri) throws URIException {
		String resultSuffix = getChildSuffix(node, true);
		
		String path = "";
		path = (uri.getPath() == null) ? "" : uri.getPath();
		path = path + (path.endsWith("/") ? "" : "/") + Long.toString(Math.abs(staticRandomGenerator.nextLong()));
		path = path + resultSuffix;

		return path;

	}
	
	/**
	Analyse node (should be a folder unless it is host level) in-order.
	*/
	private void inOrderAnalyse(SiteNode node) {
	    
		SiteNode tmp = null;
		
		if (isStop) {
		    return;
		}
		
		if (node == null) {
			return;
		}

		// analyse entity if not root and not leaf.
		// Leaf is not analysed because only folder entity is used to determine if path exist.
		try {
			if (!node.isRoot()) {
				if (!node.isLeaf() || node.isLeaf() && ((SiteNode) node.getParent()).isRoot()) {
					analyse(node);
				}
			}
		} catch (Exception e) {

		}
				
		for (int i=0; i<node.getChildCount() && !isStop(); i++) {
			try {
				tmp = (SiteNode) node.getChildAt(i);
				inOrderAnalyse(tmp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    
    public boolean isFileExist(HttpMessage msg) {
        
        if (msg.getResponseHeader().isEmpty()) {
            return false;
        }
        
		// RFC
        if (msg.getResponseHeader().getStatusCode() == HttpStatusCode.NOT_FOUND) {
            return false;
        }

        URI uri = (URI) msg.getRequestHeader().getURI().clone();
        try {
            // strip off last part of path - use folder only
            uri.setQuery(null);
            String path = uri.getPath();
            path = path.replaceAll("/[^/]*$","");
            uri.setPath(path);
        } catch (Exception e1) {}
    
        String sUri = uri.toString();        
        
		// get sample with same relative path position when possible.
		// if not exist, use the host only	
		SampleResponse sample = (SampleResponse) mapVisited.get(sUri);
		if (sample == null) {
		    try {
                uri.setPath(null);
            } catch (URIException e2) {}
		    String sHostOnly = uri.toString();
			sample = (SampleResponse) mapVisited.get(sHostOnly);
		}
		
		// check if any analysed result.

		if (sample == null) {
			if (msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK) {
				// no anlaysed result to confirm, assume file exist and return
				return true;
			} else {
				return false;
			}
		}
		
		// check for redirect response.  If redirect to same location, then file does not exist
		if (HttpStatusCode.isRedirection(msg.getResponseHeader().getStatusCode())) {
			try {
				if (sample.getMessage().getResponseHeader().getStatusCode() == msg.getResponseHeader().getStatusCode()) {
					String location = msg.getResponseHeader().getHeader(HttpHeader.LOCATION);
					if (location != null && location.equals(sample.getMessage().getResponseHeader().getHeader(HttpHeader.LOCATION))) {
						return false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		
		// Not success code
		if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
			return false;
		}

		// remain only OK response here

		// nothing more to determine.  Check for possible not found page pattern.
		Matcher matcher = patternNotFound.matcher(msg.getResponseBody().toString());
		if (matcher.find()) {
			return false;
		}
		
		// static response
		String body = msg.getResponseBody().toString().replaceAll(p_REMOVE_HEADER, "");
		if (sample.getErrorPageType() == SampleResponse.ERROR_PAGE_STATIC) {
			if (sample.getMessage().getResponseBody().toString().equals(body)) {
				return false;
			}
			return true;
		}

		uri = msg.getRequestHeader().getURI();
		try {
			if (sample.getErrorPageType() == SampleResponse.ERROR_PAGE_DYNAMIC_BUT_DETERMINISTIC) {
				body = msg.getResponseBody().toString().replaceAll(getPathRegex(uri), "").replaceAll("\\s[012]\\d:[0-5]\\d:[0-5]\\d\\s","");
				// ZAP: FindBugs fix - added call to HttpBody.toString() 
				if (sample.getMessage().getResponseBody().toString().equals(body)) {
					return false;
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();

		}



        return true;
    }
	
	private void sendAndReceive(HttpMessage msg) throws HttpException, IOException {
	    httpSender.sendAndReceive(msg, true);
	}
	

	
}


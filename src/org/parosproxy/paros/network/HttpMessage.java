/*
 * Created on 22 Jun 2004.
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2006 Chinotec Technologies Company
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
// ZAP: 2011/10/29 Fixed cookie parsing
// ZAP: 2012/03/15 Changed to use the classes HttpRequestBody and HttpResponseBody.
// Changed to use the byte[] body. Changed to use the class StringBuilder instead
// of StringBuffer. Reworked some methods.
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/06/11 Added method boolean isWebSocketUpgrade()
// ZAP: 2012/07/02 Implement Message interface for more flexibility.
// ZAP: 2012/06/24 Added method to add Cookies of type java.net.HttpCookie to request header
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/08/09 Added HttpSession field 

package org.parosproxy.paros.network;

import java.net.HttpCookie;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httpsessions.HttpSession;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.network.HttpResponseBody;


/**
 * Representation of a HTTP message request (header and body) and response (header and body) pair.
 * 
 */
public class HttpMessage implements Message {

	private static Pattern staticPatternParam = Pattern.compile("&", Pattern.CASE_INSENSITIVE);
	// Not yet supported
	//private static Pattern staticPatternParam2 = Pattern.compile(";", Pattern.CASE_INSENSITIVE);

	private HttpRequestHeader mReqHeader = new HttpRequestHeader();
	private HttpRequestBody mReqBody = new HttpRequestBody();
	private HttpResponseHeader mResHeader = new HttpResponseHeader();
	private HttpResponseBody mResBody = new HttpResponseBody();
	private Object userObject = null;
	private int timeElapsed = 0;
	private long timeSent = 0;
    //private String tag = "";
    // ZAP: Added note to HttpMessage
    private String note = "";
    // ZAP: Support for multiple tags
    private Vector<String> tags = new Vector<String>();
    // ZAP: Added historyRef
    private HistoryReference historyRef = null;
    // ZAP: Added logger
    private static Logger log = Logger.getLogger(HttpMessage.class);
    // ZAP: Added HttpSession
	private HttpSession httpSession = null;


    public HistoryReference getHistoryRef() {
		return historyRef;
	}

	public void setHistoryRef(HistoryReference historyRef) {
		this.historyRef = historyRef;
	}
	
	/**
	 * Gets the http session associated with this message.
	 * 
	 * @return the http session
	 */
	public HttpSession getHttpSession(){
		return this.httpSession;
	}
	
	/**
	 * Sets the http session associated with this message.
	 * 
	 * @param session the new http session
	 */
	public void setHttpSession(HttpSession session) {
		this.httpSession = session;
	}

	/**
	 * Constructor for a empty HTTP message.
	 *
	 */
	public HttpMessage() {
	}
	
	public HttpMessage(URI uri) throws HttpMalformedHeaderException {
	    setRequestHeader(new HttpRequestHeader(HttpRequestHeader.GET, uri, HttpHeader.HTTP11));
	}

	public HttpMessage(HttpRequestHeader reqHeader) {
	    setRequestHeader(reqHeader);
	}
	
	public HttpMessage(HttpRequestHeader reqHeader, HttpRequestBody reqBody) {
		setRequestHeader(reqHeader);
		setRequestBody(reqBody);
	}

	/**
	 * Constructor for a HTTP message with given request and response pair.
	 * @param reqHeader
	 * @param reqBody
	 * @param resHeader
	 * @param resBody
	 */
	public HttpMessage(HttpRequestHeader reqHeader, HttpRequestBody reqBody,
			HttpResponseHeader resHeader, HttpResponseBody resBody) {
		setRequestHeader(reqHeader);
		setRequestBody(reqBody);
		setResponseHeader(resHeader);
		setResponseBody(resBody);		
	}
	
	public HttpMessage(String reqHeader, byte[] reqBody, String resHeader, byte[] resBody) throws HttpMalformedHeaderException {
		setRequestHeader(reqHeader);
		setRequestBody(reqBody);
		if (resHeader != null && !resHeader.equals("")) {
		    setResponseHeader(resHeader);
		    setResponseBody(resBody);
		}
	}

	/**
	 * Get the request header of this message.
	 * @return
	 */
	public HttpRequestHeader getRequestHeader() {
		return mReqHeader;
	}
	
	/**
	 * Set the request header of this message.
	 * @param reqHeader
	 */
	public void setRequestHeader(HttpRequestHeader reqHeader) {
		mReqHeader = reqHeader;
	}

	/**
	 * Get the response header of this message.
	 * @return response.  if getResponseHeader().toString() is "" then it has not been read yet.
	 */
	public HttpResponseHeader getResponseHeader() {
		return mResHeader;
	}

	/**
	 * Set the response header of this message.
	 * @param resHeader
	 */
	public void setResponseHeader(HttpResponseHeader resHeader) {
		mResHeader = resHeader;
	}

	/**
	 * Get the request body of this message.
	 * @return Null = response header not exist yet.
	 */
	public HttpRequestBody getRequestBody() {
		return mReqBody;
	}

	/**
	 * Set the request body of this message.
	 * @param reqBody
	 */
	public void setRequestBody(HttpRequestBody reqBody) {
		mReqBody = reqBody;
	}

	/**
	 * Get the response body of this message.
	 * @return
	 */
	public HttpResponseBody getResponseBody() {
		return mResBody;
	}

	/**
	 * Set the response body of this message.
	 * @param resBody
	 */
	public void setResponseBody(HttpResponseBody resBody) {
		mResBody = resBody;
	    getResponseBody().setCharset(getResponseHeader().getCharset());

	}
	
	public void setRequestHeader(String reqHeader) throws HttpMalformedHeaderException {
		HttpRequestHeader newHeader = new HttpRequestHeader(reqHeader);
		setRequestHeader(newHeader);
	}
	
	public void setResponseHeader(String resHeader) throws HttpMalformedHeaderException {
		HttpResponseHeader newHeader = new HttpResponseHeader(resHeader);
		setResponseHeader(newHeader);

	}

	public void setRequestBody(String body) {
	    getRequestBody().setCharset(getRequestHeader().getCharset());
		getRequestBody().setBody(body);

	}
	
	public void setRequestBody(byte[] body) {
		getRequestBody().setBody(body);
	    getRequestBody().setCharset(getRequestHeader().getCharset());

	}

	public void setResponseBody(String body) {
	    if (mReqBody == null) {
	        mReqBody = new HttpRequestBody();
	    }
	    getResponseBody().setCharset(getResponseHeader().getCharset());
		getResponseBody().setBody(body);

	}
	
	public void setResponseBody(byte[] body) {
	    if (mReqBody == null) {
	        mReqBody = new HttpRequestBody();
	    }
		getResponseBody().setBody(body);
	    getResponseBody().setCharset(getResponseHeader().getCharset());
	}

	/**
	 * Compare if 2 message is the same.  2 messages are the same if:
	 * Host, port, path and query param and VALUEs are the same.  For POST request, the body must be the same.
	 * @param object
	 * @return
	 */
	@Override
	public boolean equals(Object object) {

	    if (!(object instanceof HttpMessage)) {
	        return false;
	    }
	    
	    HttpMessage msg = (HttpMessage) object;
	    boolean result = false;
	    
	    // compare method
	    if (!this.getRequestHeader().getMethod().equalsIgnoreCase(msg.getRequestHeader().getMethod())) {
	        return false;
	    }
	    
	    // compare host, port and URI
	    URI uri1 = this.getRequestHeader().getURI();
	    URI uri2 = msg.getRequestHeader().getURI();

	    
	    try {
            if (uri1.getHost() == null || uri2.getHost() == null || !uri1.getHost().equalsIgnoreCase(uri2.getHost())) {
                return false;
            }
            
            if (uri1.getPort() != uri2.getPort()) {
                return false;
            }
            
            String pathQuery1 = uri1.getPathQuery();
            String pathQuery2 = uri2.getPathQuery();

            if (pathQuery1 == null && pathQuery2 == null) {
                return true;
            } else if (pathQuery1 != null && pathQuery2 != null) {
                return pathQuery1.equalsIgnoreCase(pathQuery2);
            } else if (pathQuery1 == null || pathQuery2 == null) {
                return false;
            }
            
            if (this.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
                return this.getRequestBody().equals(msg.getRequestBody());
            }
            
            result = true;
            
        } catch (URIException e) {
            try {
                result = this.getRequestHeader().getURI().toString().equalsIgnoreCase(msg.getRequestHeader().getURI().toString());
            } catch (Exception e1) {
				// ZAP: log error
				log.error(e.getMessage(), e);
            }
        }

        return result;
	}

	/**
	 * 2 messages are equal type if the host, port, path and query names are equal.
	 * Even though the query value may differ.
	 * @param msg
	 * @return
	 */
	public boolean equalType(HttpMessage msg) {
	    boolean result = false;

	    // compare method
	    if (!this.getRequestHeader().getMethod().equalsIgnoreCase(msg.getRequestHeader().getMethod())) {
	        return false;
	    }
	    
	    // compare host, port and URI
	    URI uri1 = this.getRequestHeader().getURI();
	    URI uri2 = msg.getRequestHeader().getURI();

	    
	    try {
            if (uri1.getHost() == null || uri2.getHost() == null || !uri1.getHost().equalsIgnoreCase(uri2.getHost())) {
                return false;
            }
            
            if (uri1.getPort() != uri2.getPort()) {
                return false;
            }
            
            String path1 = uri1.getPath();
            String path2 = uri2.getPath();

            if (path1 == null && path2 == null) {
                return true;
            }
            
            if (path1 != null && path2 != null && !path1.equalsIgnoreCase(path2)) {
                return false;
            } else {
                if (path1 == null || path2 == null) {
                    return false;
                }
            }

            if (!queryEquals(msg)) {
                return false;
            }
            
            result = true;
            
        } catch (URIException e) {
			// ZAP: log error
			log.error(e.getMessage(), e);
        }
        
	    return result;
	}
	
	private boolean queryEquals(HttpMessage msg) throws URIException {
	    boolean result = false;
	    
	    URI uri1 = this.getRequestHeader().getURI();
	    URI uri2 = msg.getRequestHeader().getURI();

	    String query1 = "";
	    String query2 = "";

	    SortedSet<String> set1 = null;
	    SortedSet<String> set2 = null;

        // compare the URI query part.  2 msg is consider same param set here.
        if (uri1.getQuery() != null) query1 = uri1.getQuery();
        if (uri2.getQuery() != null) query2 = uri2.getQuery();

        set1 = getParamNameSet(query1);
	    set2 = getParamNameSet(query2);

	    if (!set1.equals(set2)) {
	        return false;
	    }


        // compare here if this is a POST
        //the POST body part must also be the same set
	    if (getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
	        
	        query1 = this.getRequestBody().toString();
	        query2 = msg.getRequestBody().toString();
	        set1 = getParamNameSet(query1);
		    set2 = getParamNameSet(query2);	        

		    if (!set1.equals(set2)) {
		        return false;
		    }
	    }

	    result = true;
	    

	    
	    return result;
	}
	
	public TreeSet<String> getParamNameSet(String params) {
	    TreeSet<String> set = new TreeSet<String>();
	    String[] keyValue = staticPatternParam.split(params);
		String key = null;
		int pos = 0;
		for (int i=0; i<keyValue.length; i++) {
			key = null;
			pos = keyValue[i].indexOf('=');
			try {
				if (pos > 0) {
					// param found

					key = keyValue[i].substring(0,pos);

					//!!! note: this means param not separated by & and = is not parsed
				} else {
					key = keyValue[i];
				}
				
				if (key != null) {
					set.add(key);
				}
			} catch (Exception e) {
				// ZAP: log error
				log.error(e.getMessage(), e);
			}
		}
		
		return set;
	}

	// ZAP: Introduced HtmlParameter
	private TreeSet<HtmlParameter> getParamsSet(HtmlParameter.Type type, String params) {
		TreeSet<HtmlParameter> set = new TreeSet<HtmlParameter>();
		//!!! note: this means param not separated by & is not parsed
	    String[] keyValue = staticPatternParam.split(params);
	    // TODO need to parse the header to split out params if separated by semicolons
	    /*
	    if (keyValue.length == 0) {
		    String[] keyValue2 = staticPatternParam2.split(params);
	    	if (keyValue2.length > 1) {
	    		// Looks like the parameters are probably split using semicolons instead of &
	    		keyValue = keyValue2;
	    	}
	    }
	    */
		String key = null;
		String value = null;
		int pos = 0;
		for (int i=0; i<keyValue.length; i++) {
			key = null;
			value = null;
			pos = keyValue[i].indexOf('=');
			try {
				if (pos > 0) {
					// key=value type param found
					key = keyValue[i].substring(0,pos);
					value = keyValue[i].substring(pos+1);
					set.add(new HtmlParameter (type, key, value));
				} else if (keyValue[i].length() > 0) {
					set.add(new HtmlParameter (type, keyValue[i], ""));
				}
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return set;
	}
	
	// ZAP: Added getParamNames
	public String [] getParamNames() {
		Vector<String> v = new Vector<String>();
		try {
			// Get the params names from the query
			String query = this.getRequestHeader().getURI().getQuery();
			if (query == null) {
				query = "";
			}
			SortedSet<String> pns = this.getParamNameSet(query);
			Iterator<String> iterator = pns.iterator();
			while (iterator.hasNext()) {
				String name = iterator.next();
				if (name != null) {
					v.add(name);
				}
			}
			if (getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
				// Get the param names from the POST
				query = this.getRequestBody().toString();
				if (query == null) {
					query = "";
				}
				pns = this.getParamNameSet(query);
			    iterator = pns.iterator();
			    while (iterator.hasNext()) {
					String name = iterator.next();
			        if (name != null) {
			        	v.add(name);
			        }
					
				}
			}
		} catch (URIException e) {
			log.error(e.getMessage(), e);
		}
		String [] a = new String [v.size()];
		v.toArray(a);
		return a;
	}
	
	// ZAP: Added getUrlParams
	public TreeSet<HtmlParameter> getUrlParams() {
		// Get the params names from the query
		String query = null;
		try {
			query = this.getRequestHeader().getURI().getQuery();
		} catch (URIException e) {
			log.error(e.getMessage(), e);
		}
		if (query == null) {
			query = "";
		}
		return this.getParamsSet(HtmlParameter.Type.url, query);
	}
	
	// ZAP: Added getFormParams
	public TreeSet<HtmlParameter> getFormParams() {
		String query = null;
		// use the body even if it's not POST, this allows the user to add/edit the POST
		// params when the method is other than POST.
		//if (getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
			// Get the param names from the POST
			query = this.getRequestBody().toString();
		//}
		if (query == null) {
			query = "";
		}
		return this.getParamsSet(HtmlParameter.Type.form, query);
	}
	
	public void setCookieParamsAsString(String data) {
		this.getRequestHeader().setHeader(HttpHeader.COOKIE, data);
	}
	
	public String getCookieParamsAsString() {
		List<String> cookies = new LinkedList<String>();
        if (! this.getRequestHeader().isEmpty()) {
        	addAll(cookies,this.getRequestHeader().getHeaders(HttpHeader.COOKIE));
        }
        if (! this.getResponseHeader().isEmpty()) {
        	addAll(cookies,this.getResponseHeader().getHeaders(HttpHeader.SET_COOKIE));
        	addAll(cookies,this.getResponseHeader().getHeaders(HttpHeader.SET_COOKIE2));
        }
		
        // Fix error requesting cookies, but there are none
        if (cookies.isEmpty()) {
        	return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (String header: cookies) {
        	sb.append(header);
        }
        return sb.toString();
	}
	
	private void addAll (List<String> dest, Vector<String> src) {
		if (src != null) {
			dest.addAll(src);
		}
	}
	
	// ZAP: Added getCookieParams
	public TreeSet<HtmlParameter> getCookieParams() {
		TreeSet<HtmlParameter> set = new TreeSet<HtmlParameter>();
		
	    if (! this.getRequestHeader().isEmpty()) {
	    	set.addAll(this.getRequestHeader().getCookieParams());
        }
	    if (! this.getResponseHeader().isEmpty()) {
	    	set.addAll(this.getResponseHeader().getCookieParams());
	    }

		return set;
	}
	
    /**
     * @return Returns the userObject.
     */
    public Object getUserObject() {
        return userObject;
    }
    /**
     * @param userObject The userObject to set.
     */
    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }
    
    public HttpMessage cloneAll() {
        HttpMessage newMsg = cloneRequest();
        
        if (!this.getResponseHeader().isEmpty()) {
            try {
                newMsg.getResponseHeader().setMessage(this.getResponseHeader().toString());
            } catch (HttpMalformedHeaderException e) {
            }
            newMsg.setResponseBody(this.getResponseBody().getBytes());
        }

        return newMsg;
    }
    
    public HttpMessage cloneRequest() {
        HttpMessage newMsg = new HttpMessage();
        if (!this.getRequestHeader().isEmpty()) {
            try {
                newMsg.getRequestHeader().setMessage(this.getRequestHeader().toString());
            } catch (HttpMalformedHeaderException e) {
                e.printStackTrace();
            }
            newMsg.setRequestBody(this.getRequestBody().getBytes());
        }
        return newMsg;
    }
    /**
     * @return Get the elapsed time (time difference) between the request is sent and all response is received.  In millis.
     * The value is zero if the response is not received.
     */
    public int getTimeElapsedMillis() {
        return timeElapsed;
    }

    /**
     * Set the elapsed time (time difference) between the request is sent and all response is received.  In millis.
     * @param timeElapsed
     */
    public void setTimeElapsedMillis(int timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    /**
     * Get the starting time when the request is going to be sent.  This is the System.currentTimeMillis before the message is sent.
     * The value is zero if the request is not sent yet.
     */
    public long getTimeSentMillis() {
        return timeSent;
    }
    /**
     * Set the time when the request is sent.
     * @param timeSent The timeSent to set.
     */
    public void setTimeSentMillis(long timeSent) {
        this.timeSent = timeSent;
    }
    
    /*
     * @return Returns the tag.
     *
    public String getTag() {
        return tag;
    }
    */

    /*
     * @param tag The tag to set.
     *
    public void setTag(String tag) {
        this.tag = tag;
    }
    */
    public Vector<String> getTags() {
    	return this.tags;
    }
    
    public void addTag (String tag) {
    	this.tags.add(tag);
    }
    
    public void removeTag (String tag) {
    	this.tags.remove(tag);
    }
    
    /**
     * @return Returns the note.
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note The note to set.
     */
    public void setNote(String note) {
        this.note = note;
    }

	public void mutateHttpMethod(String method) {
		// String header = reqPanel.getTxtHeader().getText();
		String header = getRequestHeader().toString();
		try {
			HttpRequestHeader hrh = new HttpRequestHeader(header);
			
			URI uri = hrh.getURI();
			// String body = reqPanel.getTxtBody().getText();
			String body = getRequestBody().toString();
			String prevMethod = hrh.getMethod();
			if (prevMethod.equalsIgnoreCase(method)) {
				return;
			}
			if (prevMethod.equals(HttpRequestHeader.POST)) {
				// Was POST, move all params onto the URL
				if (body != null && body.length() > 0) {
					StringBuilder sb = new StringBuilder();
					if (uri.getQuery() != null) {
						sb.append(uri.getQuery());
					}

					String[] params = body.split("&");
					for (String param : params) {
						if (sb.length() > 0) {
							sb.append('&');
						}
						String[] nv = param.split("=");
						if (nv.length == 1) {
							// This effectively strips out the equals if theres
							// no value
							sb.append(nv[0]);
						} else {
							sb.append(param);
						}
					}
					uri.setQuery(sb.toString());

				}
				hrh.setURI(uri);
				// Clear the body
				body = "";

			} else if (method.equals(HttpRequestHeader.POST)) {
				// To be a port, move all URL query params into the body
				String query = uri.getQuery();
				if (query != null) {
					StringBuilder sb = new StringBuilder();
					String[] params = query.split("&");
					for (String param : params) {
						if (sb.length() > 0) {
							sb.append('&');
						}
						sb.append(param);
						String[] nv = param.split("=");
						if (nv.length == 1) {
							// Cope with URL params with no values e.g.
							// http://www.example.com/test?key
							sb.append('=');
						}
					}
					body = sb.toString();
					uri.setQuery(null);
					hrh.setURI(uri);
				}
			}
			hrh.setMethod(method);

			getRequestHeader().setMessage(hrh.toString());
			getRequestBody().setBody(body);
		} catch (HttpMalformedHeaderException e) {
			// Ignore?
			log.error(e.getMessage(), e);
		} catch (URIException e) {
			log.error(e.getMessage(), e);
		}

	}

	// Construct new POST Body from parameter in the postParams argument
	// in the Request Body
	public void setFormParams(TreeSet<HtmlParameter> postParams) {
		// TODO: Maybe update content length etc?
		mReqBody.setFormParams(postParams);
	}

	// Construct new URL from get Request, based on the getParams argument
	// in the Request Header
	public void setGetParams(TreeSet<HtmlParameter> getParams) {
		mReqHeader.setGetParams(getParams);
	}

	// Rewrite cookie line in the Request Header,
	// based on values in cookieParams
	public void setCookieParams(TreeSet<HtmlParameter> cookieParams) {
		mReqHeader.setCookieParams(cookieParams);
	}

	/**
	 * ZAP: New method checking for connection upgrade.
	 * 
	 * @return True if this connection should be upgraded to WebSockets.
	 */
	public boolean isWebSocketUpgrade() {
		if (!getResponseHeader().isEmpty()) {
			String connectionHeader = getResponseHeader().getHeader("connection");
			String upgradeHeader = getResponseHeader().getHeader("upgrade");
			
			if (connectionHeader != null && connectionHeader.equalsIgnoreCase("upgrade")) {
				if (upgradeHeader != null && upgradeHeader.equalsIgnoreCase("websocket")) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	// Rewrite cookie line in the Request Header,
	// based on values in cookies
	public void setCookies(List<HttpCookie> cookies) {
		mReqHeader.setCookies(cookies);
	}
	
	@Override
	public boolean isInScope() {
		return Model.getSingleton().getSession().isInScope(this.getRequestHeader().getURI().toString());
	}
}
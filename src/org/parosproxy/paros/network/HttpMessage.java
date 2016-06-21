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
// ZAP: 2012/10/08 Issue 391: Performance improvements
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/04/08 Issue 605: Force intercepts via header
// ZAP: 2013/07/25 Added support for sending the message from the perspective of a User
// ZAP: 2013/09/26 Issue 716: ZAP flags its own HTTP responses
// ZAP: 2013/11/16 Issue 867: HttpMessage#getFormParams should return an empty TreeSet if
// the request body is not "x-www-form-urlencoded"
// ZAP: 2014/01/06 Issue 965: Support 'single page' apps and 'non standard' parameter separators
// ZAP: 2014/03/23 Tidy up, do not allow to set null request/response headers/bodies.
// ZAP: 2014/03/28 Issue 1127: 	Allow scripts to generate breaks
// ZAP: 2014/06/16 Issue 1217: Table format does not display information when charset is
// present in Content-Type header
// ZAP: 2015/02/09 Fix NullPointerException in equals(Object) when comparing with empty messages
// ZAP: 2015/08/07 Issue 1768: Update to use a more recent default user agent
// ZAP: 2015/08/19 Deprecate/change methods with unused parameters 
// ZAP: 2016/05/31 Implement hashCode()

package org.parosproxy.paros.network;

import java.net.HttpCookie;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httpsessions.HttpSession;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.network.HttpResponseBody;
import org.zaproxy.zap.users.User;


/**
 * Representation of a HTTP message request (header and body) and response (header and body) pair.
 * 
 */
public class HttpMessage implements Message {

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
    // ZAP: Added historyRef
    private HistoryReference historyRef = null;
    // ZAP: Added logger
    private static Logger log = Logger.getLogger(HttpMessage.class);
    // ZAP: Added HttpSession
	private HttpSession httpSession = null;
	// ZAP: Added support for requesting the message to be sent as a particular User
	private User requestUser;
	// Can be set by scripts to force a break
	private boolean forceIntercept = false;

    /**
     * Flag that indicates if the response has been received or not from the target host.
     * <p>
     * Default is {@code false}.
     */
    private boolean responseFromTargetHost = false;


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
		this(uri, null);
	}
	
	public HttpMessage(URI uri, ConnectionParam params) throws HttpMalformedHeaderException {
	    setRequestHeader(new HttpRequestHeader(HttpRequestHeader.GET, uri, HttpHeader.HTTP11, params));
	}

	/**
	 * Constructs an HTTP message with the given request header.
	 *
	 * @param reqHeader the request header
	 * @throws IllegalArgumentException if the parameter {@code reqHeader} is {@code null}.
	 */
	public HttpMessage(HttpRequestHeader reqHeader) {
	    setRequestHeader(reqHeader);
	}
	
	/**
	 * Constructs an HTTP message with the given request header and request body.
	 *
	 * @param reqHeader the request header
	 * @param reqBody the request body
	 * @throws IllegalArgumentException if the parameter {@code reqHeader} or {@code reqBody} are {@code null}.
	 */
	public HttpMessage(HttpRequestHeader reqHeader, HttpRequestBody reqBody) {
		setRequestHeader(reqHeader);
		setRequestBody(reqBody);
	}

	/**
	 * Constructor for a HTTP message with given request and response pair.
	 * @param reqHeader the request header
	 * @param reqBody the request body
	 * @param resHeader the response header
	 * @param resBody the response body
	 * @throws IllegalArgumentException if one of the parameters is {@code null}.
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
	 * Gets the request header of this message.
	 * 
	 * @return the request header, never {@code null}
	 */
	public HttpRequestHeader getRequestHeader() {
		return mReqHeader;
	}
	
	/**
	 * Sets the request header of this message.
	 * 
	 * @param reqHeader the new request header
	 * @throws IllegalArgumentException if parameter {@code reqHeader} is {@code null}.
	 */
	public void setRequestHeader(HttpRequestHeader reqHeader) {
		if (reqHeader == null) {
			throw new IllegalArgumentException("The parameter reqHeader must not be null.");
		}
		mReqHeader = reqHeader;
	}

	/**
	 * Gets the response header of this message.
	 * <p>
	 * To know if a response has been set call the method {@code HttpResponseHeader#isEmpty()} on the returned response header.
	 * The response header is initially empty.
	 * </p>
	 * 
	 * @return the response header, never {@code null}
	 * @see HttpResponseHeader#isEmpty()
	 */
	public HttpResponseHeader getResponseHeader() {
		return mResHeader;
	}

	/**
	 * Sets the response header of this message.
	 * 
	 * @param resHeader the new response header
	 * @throws IllegalArgumentException if parameter {@code resHeader} is {@code null}.
	 */
	public void setResponseHeader(HttpResponseHeader resHeader) {
		if (resHeader == null) {
			throw new IllegalArgumentException("The parameter resHeader must not be null.");
		}
		mResHeader = resHeader;
	}

	/**
	 * Gets the request body of this message.
	 * 
	 * @return the request body, never {@code null}
	 */
	public HttpRequestBody getRequestBody() {
		return mReqBody;
	}

	/**
	 * Sets the request body of this message.
	 * 
	 * @param reqBody the new request body
	 * @throws IllegalArgumentException if parameter {@code reqBody} is {@code null}.
	 */
	public void setRequestBody(HttpRequestBody reqBody) {
		if (reqBody == null) {
			throw new IllegalArgumentException("The parameter reqBody must not be null.");
		}
		mReqBody = reqBody;
	}

	/**
	 * Gets the response body of this message.
	 * 
	 * @return the response body, never {@code null}
	 */
	public HttpResponseBody getResponseBody() {
		return mResBody;
	}

	/**
	 * Sets the response body of this message.
	 * 
	 * @param resBody the new response body
	 * @throws IllegalArgumentException if parameter {@code resBody} is {@code null}.
	 */
	public void setResponseBody(HttpResponseBody resBody) {
		if (resBody == null) {
			throw new IllegalArgumentException("The parameter resBody must not be null.");
		}
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
	    getResponseBody().setCharset(getResponseHeader().getCharset());
		getResponseBody().setBody(body);

	}
	
	public void setResponseBody(byte[] body) {
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
	    
        if (uri1 == null) {
            if (uri2 != null) {
                return false;
            }
            return true;
        } else if (uri2 == null) {
            return false;
        }

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getRequestHeader().getMethod().toLowerCase(Locale.ROOT).hashCode();
		URI uri = getRequestHeader().getURI();
		if (uri != null) {
			result = prime * result + uri.getPort();
			try {
				result = prime * result + (uri.getRawHost() == null ? 0 : uri.getHost().toLowerCase(Locale.ROOT).hashCode());
			} catch (URIException e) {
				log.error("Failed to obtain the host for hashCode calculation: " + uri.toString(), e);
			}
			result = prime * result
					+ ((uri.getRawPathQuery() == null) ? 0 : uri.getEscapedPathQuery().toLowerCase(Locale.ROOT).hashCode());
		}

		if (getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
			result = prime * result + getRequestBody().hashCode();
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
	
	private boolean queryEquals(HttpMessage msg) {
	    boolean result = false;
	    
	    SortedSet<String> set1 = null;
	    SortedSet<String> set2 = null;

        // compare the URI query part.  2 msg is consider same param set here.
        set1 = getParamNameSet(HtmlParameter.Type.url);
	    set2 = getParamNameSet(HtmlParameter.Type.url);

	    if (!set1.equals(set2)) {
	        return false;
	    }


        // compare here if this is a POST
        //the POST body part must also be the same set
	    if (getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
	        
	        set1 = getParamNameSet(HtmlParameter.Type.form);
		    set2 = getParamNameSet(HtmlParameter.Type.form);	        

		    if (!set1.equals(set2)) {
		        return false;
		    }
	    }

	    result = true;
	    

	    
	    return result;
	}
	
	/**
	 * @deprecated (2.4.2) Use {@link #getParamNameSet(org.parosproxy.paros.network.HtmlParameter.Type)} instead, it will
	 *             be removed in a following release.
	 */
	@Deprecated
	@SuppressWarnings("javadoc")
	public TreeSet<String> getParamNameSet(HtmlParameter.Type type, String params) {
		return getParamNameSet(type);
	}

	/**
	 * Returns the names of the parameters of the given {@code type}.
	 *
	 * @param type the type of the parameters that will be extracted from the message
	 * @return a {@code TreeSet} with the names of the parameters of the given {@code type}, never {@code null}
	 * @since 2.4.2
	 */
	public TreeSet<String> getParamNameSet(HtmlParameter.Type type) {
	    TreeSet<String> set = new TreeSet<>();
		Map<String, String> paramMap = Model.getSingleton().getSession().getParams(this, type);

		for (Entry<String, String> param : paramMap.entrySet()) {
			set.add(param.getKey());
		}
		return set;
	}

	private TreeSet<HtmlParameter> getParamsSet(HtmlParameter.Type type) {
		TreeSet<HtmlParameter> set = new TreeSet<>();
		Map<String, String> paramMap = Model.getSingleton().getSession().getParams(this, type);

		for (Entry<String, String> param : paramMap.entrySet()) {
			set.add(new HtmlParameter(type, param.getKey(), param.getValue()));
		}
		return set;
	}
	
	// ZAP: Added getParamNames
	public String [] getParamNames() {
		Vector<String> v = new Vector<>();
		// Get the params names from the query
		SortedSet<String> pns = this.getParamNameSet(HtmlParameter.Type.url);
		Iterator<String> iterator = pns.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			if (name != null) {
				v.add(name);
			}
		}
		if (getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
			// Get the param names from the POST
			pns = this.getParamNameSet(HtmlParameter.Type.form);
		    iterator = pns.iterator();
		    while (iterator.hasNext()) {
				String name = iterator.next();
		        if (name != null) {
		        	v.add(name);
		        }
				
			}
		}
		String [] a = new String [v.size()];
		v.toArray(a);
		return a;
	}
	
	// ZAP: Added getUrlParams
	public TreeSet<HtmlParameter> getUrlParams() {
		return this.getParamsSet(HtmlParameter.Type.url);
	}
	
	// ZAP: Added getFormParams
	public TreeSet<HtmlParameter> getFormParams() {
		final String contentType = mReqHeader.getHeader(HttpRequestHeader.CONTENT_TYPE);
		if (contentType == null
				|| !StringUtils.startsWithIgnoreCase(contentType.trim(), HttpHeader.FORM_URLENCODED_CONTENT_TYPE)) {
			return new TreeSet<>();
		}
		return this.getParamsSet(HtmlParameter.Type.form);
	}
	
	public void setCookieParamsAsString(String data) {
		this.getRequestHeader().setHeader(HttpHeader.COOKIE, data);
	}
	
	public String getCookieParamsAsString() {
		List<String> cookies = new LinkedList<>();
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
		TreeSet<HtmlParameter> set = new TreeSet<>();
		
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
                log.error(e.getMessage(), e);
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

	/**
	 * ZAP: New method checking if connection is a Server-Sent Events stream.
	 * 
	 * @return
	 */
	public boolean isEventStream() {
		boolean isEventStream = false;
		if (!getResponseHeader().isEmpty()) {
			String contentTypeHeader = getResponseHeader().getHeader("content-type");
			if (contentTypeHeader != null && contentTypeHeader.equals("text/event-stream")) {
				// response is an SSE stream
				isEventStream = true;
			}
		} else {
			// response not available
			// is request for event-stream?
			String acceptHeader = getRequestHeader().getHeader("Accept");
			if (acceptHeader != null && acceptHeader.equals("text/event-stream")) {
				// request is for an SSE stream
				isEventStream = true;
			}
		}
		
		return isEventStream;
	}

	@Override
	public boolean isForceIntercept() {
		String vals = this.getRequestHeader().getHeader(HttpHeader.X_SECURITY_PROXY);
		if (vals != null) {
			for (String val : vals.split(",")) {
				if (HttpHeader.SEC_PROXY_INTERCEPT.equalsIgnoreCase(val.trim())) {
					// The browser told us to do it Your Honour
					return true;
				}
			}
		}
		return forceIntercept;
	}
	
	public void setForceIntercept(boolean force) {
		this.forceIntercept = force;
	}

	/**
	 * Gets the request user.
	 *
	 * @return the request user
	 */
	public User getRequestingUser() {
		return requestUser;
	}

	/**
	 * Sets the requesting user. When sending the message, if a requesting user has been set, the message
	 * will be modified so that it will be sent as from the point of view of this particular user.
	 * 
	 * @param requestUser the new request user
	 */
	public void setRequestingUser(User requestUser) {
		this.requestUser = requestUser;
	}
	
	
    /**
     * Tells whether or not the response has been received from the target host.
     * <p>
     * <strong>Note:</strong> No distinction is done between responses from intermediate proxy servers (if any) and the target
     * host.
     * </p>
     * 
     * @return {@code true} if the response has been received from the target host, {@code false} otherwise.
     */
    public boolean isResponseFromTargetHost() {
        return this.responseFromTargetHost;
    }

    /**
     * Sets if the response has been received or not from the target host.
     * 
     * @param responseFromTargetHost {@code true} if the response has been received from the target host, {@code false}
     *            otherwise.
     */
    public void setResponseFromTargetHost(final boolean responseFromTargetHost) {
        this.responseFromTargetHost = responseFromTargetHost;
    }
}
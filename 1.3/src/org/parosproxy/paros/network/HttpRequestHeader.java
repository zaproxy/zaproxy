/*
 * Created on Jun 14, 2004
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
// ZAP: 2011/08/04 Changed to support Logging
 
package org.parosproxy.paros.network;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequestHeader extends HttpHeader {

	private static final long serialVersionUID = 4156598327921777493L;
	// method list
	public final static String OPTIONS	= "OPTIONS";
	public final static String GET 		= "GET";
	public final static String HEAD		= "HEAD";
	public final static String POST		= "POST";
	public final static String PUT		= "PUT";
	public final static String DELETE	= "DELETE";
	public final static String TRACE	= "TRACE";
	public final static String CONNECT	= "CONNECT";
	
	// ZAP: Added method array
	public final static String [] METHODS = {OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT};

	public final static String HOST = "Host";

	private static final Pattern patternRequestLine
		= Pattern.compile(p_METHOD + p_SP + p_URI + p_SP + p_VERSION, Pattern.CASE_INSENSITIVE);
	// private static final Pattern patternHostHeader
	//	= Pattern.compile("([^:]+)\\s*?:?\\s*?(\\d*?)");
	private static final Pattern patternImage
		= Pattern.compile("\\.(jpg|jpeg|gif|tiff|tif|png)\\z", Pattern.CASE_INSENSITIVE);
	private static final Pattern patternPartialRequestLine
		= Pattern.compile("\\A *(OPTIONS|GET|HEAD|POST|PUT|DELETE|TRACE|CONNECT)\\b", Pattern.CASE_INSENSITIVE);
	
	private String	mMethod = "";
	private URI		mUri = null;
	private String	mHostName = "";
	private int		mHostPort = 80;
	private boolean	mIsSecure = false;

	private Log log = null;

	/**
	 * Constructor for an empty header.
	 *
	 */
	public HttpRequestHeader() {
		// ZAP: added local log variable
		log = LogFactory.getLog(this.getClass());
	    clear();
	}

	/**
	 * Constructor of a request header with the string.  
	 * @param data
	 * @param isSecure If this request header is secure.  URL will be converted to HTTPS if secure = true.
	 * @throws HttpMalformedHeaderException
	 */
    public HttpRequestHeader(String data, boolean isSecure) throws HttpMalformedHeaderException {
		this();
		setMessage(data, isSecure);
    }

	/**
	 * Constructor of a request header with the string.  Whether this is a secure header depends
	 * on the URL given.
	 * @param data
	 * @throws HttpMalformedHeaderException
	 */

    public HttpRequestHeader(String data) throws HttpMalformedHeaderException {
    	this();
    	setMessage(data);
    }
    
    public void clear() {
        super.clear();

		mMethod = "";
		mUri = null;
		mHostName = "";
		mHostPort = 80;
		mMsgHeader = "";
        
    }
    
    public HttpRequestHeader(String method, URI uri, String version) throws HttpMalformedHeaderException {
        this(method + " " + uri.toString() + " " + version.toUpperCase() + CRLF + CRLF);
        try {
            setHeader(HOST, uri.getHost() + (uri.getPort() > 0? ":" + Integer.toString(uri.getPort()):""));
        } catch (URIException e) {
            e.printStackTrace();
        }
		setHeader(USER_AGENT, "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0;)");
		setHeader(PRAGMA,"no-cache");
		setHeader(CONTENT_TYPE, "application/x-www-form-urlencoded");
		setHeader(ACCEPT_ENCODING,null);
		if (method.equalsIgnoreCase(HTTP11)) {
		    setContentLength(0);
		}
		
    }
    /**
     * Set this requeset header with the given message.
     * @param data* @param isSecure If this request header is secure.  URL will be converted to HTTPS if secure = true.
     * @throws HttpMalformedHeaderException
     */
    public void setMessage(String data, boolean isSecure) throws HttpMalformedHeaderException {
		super.setMessage(data);
		try {
        	if (!parse(isSecure)) {
        		mMalformedHeader = true;
        	}
    	} catch (Exception e) {
    		mMalformedHeader = true;
    	}

    	if (mMalformedHeader) {
    		throw new HttpMalformedHeaderException();
    	}

    }

    /**
     * Set this request header with the given message.  Whether this is a secure header depends
     * on the URL given.
     */
    public void setMessage(String data) throws HttpMalformedHeaderException {
    	this.setMessage(data, false);
    }

    /**
     * Get the HTTP method (GET, POST ... etc).
     * @return
     */
    public String getMethod() {
        return mMethod;
    }

    /**
     * Set the HTTP method of this request header.
     * @param method
     */
    public void setMethod(String method) {
    	mMethod = method.toUpperCase();
    }

    /**
     * Get the URI of this request header.
     * @return
     */
	public URI getURI() {
		return mUri;
	}
	
	/**
	 * Set the URI of this request header.
	 * 
	 * @param uri
	 * @throws URIException
	 * @throws NullPointerException
	 */
	public void setURI(URI uri) throws URIException, NullPointerException {

		if (uri.getScheme() == null || uri.getScheme().equals("")) {
			mUri = new URI(HTTP + "://" + getHeader(HOST) + "/" + mUri.toString(), true);
		} else {
		    mUri = uri;
		}
		
		if (uri.getScheme().equalsIgnoreCase(HTTPS)) {
			mIsSecure = true;
		} else {
			mIsSecure = false;
		}
	}

	/**
	 * Get if this request header is under secure connection.
	 * @return
	 */
	public boolean getSecure() {
		return mIsSecure;
	}
	
	/**
	 * Set if this request header is under secure connection.
	 * @param isSecure
	 * @throws URIException
	 * @throws NullPointerException
	 */
	public void setSecure(boolean isSecure) throws URIException, NullPointerException {
		mIsSecure = isSecure;

		if (mUri == null) {
			// mUri not yet set
			return;
		}
		
		// check if URI consistent
		if (getSecure()&& mUri.getScheme().equalsIgnoreCase(HTTP)) {
			mUri = new URI(mUri.toString().replaceFirst(HTTP, HTTPS), true);
			return;
		}

		if (!getSecure()&& mUri.getScheme().equalsIgnoreCase(HTTPS)) {
			mUri = new URI(mUri.toString().replaceFirst(HTTPS, HTTP), true);
			return;
		}


	}

	/**
	 * Set the HTTP version of this request header.
	 */	
	public void setVersion(String version) {
		mVersion = version.toUpperCase();
	}

	/**
	 * Get the content length in this request header.
	 * If the content length is undetermined, 0 will be returned.
	 */
	public int getContentLength() {
		if (mContentLength == -1) {
			return 0;
		}
		return mContentLength;
	}

	/**
	 * Parse this request header.
	 * @param isSecure
	 * @return
	 * @throws URIException
	 * @throws NullPointerException
	 */
    protected boolean parse(boolean isSecure) throws URIException, NullPointerException {
    	//throws Exception {
    	    			
    	mIsSecure = isSecure;
    	Matcher matcher = patternRequestLine.matcher(mStartLine);
		if (!matcher.find()) {
			mMalformedHeader = true;
			return false;
		}
		
		mMethod = matcher.group(1);
		String sUri	= matcher.group(2);
		mVersion	= matcher.group(3);
		
		
        if (!mVersion.equalsIgnoreCase(HTTP09) && !mVersion.equalsIgnoreCase(HTTP10) && !mVersion.equalsIgnoreCase(HTTP11)) {
        	mMalformedHeader = true;
        	return false;
        }

        mUri		= parseURI(sUri);
        
		if (mUri.getScheme() == null || mUri.getScheme().equals("")) {
			mUri = new URI(HTTP + "://" + getHeader(HOST) + mUri.toString(), true);
		}

		if (getSecure() && mUri.getScheme().equalsIgnoreCase(HTTP)) {
			mUri = new URI(mUri.toString().replaceFirst(HTTP, HTTPS), true);
		}
		
		if (mUri.getScheme().equalsIgnoreCase(HTTPS)) {
			setSecure(true);
		}
		
		String hostHeader = null;
		if (mMethod.equalsIgnoreCase(CONNECT)) {
			hostHeader = sUri;
			parseHostName(hostHeader);
		} else {
			mHostName = mUri.getHost();
			mHostPort = mUri.getPort();
		}
		return true;
	}
    
	private void parseHostName(String hostHeader) {
		// no host header given but a valid host name already exist.  
		if (hostHeader == null) {
			return;
		}
		int pos = 0;
		if ((pos = hostHeader.indexOf(':', 2)) > -1) {
			mHostName = hostHeader.substring(0,pos).trim();
			try {
				mHostPort = Integer.parseInt(hostHeader.substring(pos+1));
			} catch (NumberFormatException e) {
			}
		} else {
			mHostName = hostHeader.trim();
		}

	}
	
	/**
	 * Get the host name in this request header.
	 * @return Host name.
	 */
	public String getHostName() {
		String hostName = mHostName;
		try {
			// ZAP: fixed cases, where host name is null
			hostName = ((mUri.getHost() != null) ? mUri.getHost() : mHostName);
		} catch (URIException e) {
			if (log.isWarnEnabled()) {
				log.warn(e);
			}
		}
		return hostName;
	}

	/**
	 * Get the host port.
	 * @return Host port.
	 */
	public int getHostPort() {
		int port = mUri.getPort();
		return port;
	}	

	/**
	 * Return if this request header is a image request basing on the path suffix.
	 */
	public boolean isImage() {
		try {
			return (patternImage.matcher(getURI().getPath()).find());
		} catch (URIException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Return if the data given is a request header basing on the first start line.
	 * @param data
	 * @return
	 */
	public static boolean isRequestLine(String data) {
		return patternPartialRequestLine.matcher(data).find();	
	}

	/**
	 * Return the prime header (first line).
	 */
	public String getPrimeHeader() {
		return getMethod() + " " + getURI().toString() + " " + getVersion();
	}
	
	/*
	private static final char[] DELIM_UNWISE_CHAR = {
        '<', '>', '#', '"', ' ',
        '{', '}', '|', '\\', '^', '[', ']', '`'
	};
	*/

	private static final String DELIM = "<>#\"";
	private static final String UNWISE = "{}|\\^[]`";
	private static final String DELIM_UNWISE = DELIM + UNWISE;
	
	public static URI parseURI(String sUri) throws URIException {
	    URI uri = null;
	    
	    int len = sUri.length();
	    StringBuffer sb = new StringBuffer(len);
        char[] charray = new char[1];
        String s = null;

	    for (int i=0; i<len; i++) {
	        char ch = sUri.charAt(i);
	        //String ch = sUri.substring(i, i+1);
	        if (DELIM_UNWISE.indexOf(ch) >= 0) {
	            // check if unwise or delim in RFC.  If so, encode it.
	            charray[0] = ch;
	            s = new String(charray);
	            try {
	                s = URLEncoder.encode(s, "UTF8");
	            } catch (UnsupportedEncodingException e1) {
	            }
	            sb.append(s);
	        } else if (ch == '%') {

	            // % is exception - no encoding to be done because some server may not handle
	            // correctly when % is invalid. 
	            // 
	            
	            //sb.append(ch);

	            // if % followed by hex, no encode.

	            try {
		            String hex = sUri.substring(i+1,i+3);
	                int parsed = Integer.parseInt(hex, 16);
	                sb.append(ch);
	            } catch (Exception e) {
		            charray[0] = ch;
		            s = new String(charray);
		            try {
		                s = URLEncoder.encode(s, "UTF8");
		            } catch (UnsupportedEncodingException e1) {
		            }
		            sb.append(s);
	            }
	        } else if (ch == ' ') {
	            // if URLencode, '+' will be appended.
	            sb.append("%20");
	        } else {
	            sb.append(ch);
	        }
	    }
	    uri = new URI(sb.toString(), true);
		return uri;
	}

}
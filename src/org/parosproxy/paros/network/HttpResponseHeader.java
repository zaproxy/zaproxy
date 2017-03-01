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
// ZAP: 2012/03/15 Added the @Override annotation to the appropriate methods.
// Moved to this class the method getCookieParams().
// ZAP: 2012/06/24 Added new method of getting cookies from the request header.
// ZAP: 2012/07/11 Added method to check if response type is text/html (isHtml())
// ZAP: 2012/08/06 Modified isText() to also consider javascript as text 
// ZAP: 2013/02/12 Modified isText() to also consider atom+xml as text
// ZAP: 2013/03/08 Improved parse error reporting
// ZAP: 2014/02/21 i1046: The getHttpCookies() method in the HttpResponseHeader does not properly set the domain
// ZAP: 2014/04/09 i1145: Cookie parsing error if a comma is used
// ZAP: 2015/02/26 Include json as a text content type
// ZAP: 2016/06/17 Remove redundant initialisations of instance variables

package org.parosproxy.paros.network;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class HttpResponseHeader extends HttpHeader {

	private static final long serialVersionUID = 2812716126742059785L;
    private static final Logger log = Logger.getLogger(HttpResponseHeader.class);

	public static final String HTTP_CLIENT_BAD_REQUEST = "HTTP/1.0 400 Bad request" + CRLF + CRLF;
	private static final String _CONTENT_TYPE_IMAGE = "image";
	private static final String _CONTENT_TYPE_TEXT = "text";
	private static final String _CONTENT_TYPE_HTML = "html";
	private static final String _CONTENT_TYPE_JAVASCRIPT = "javascript";
	private static final String _CONTENT_TYPE_JSON = "json";
	private static final String _CONTENT_TYPE_XML = "xml"; 

	
	static final Pattern patternStatusLine
		= Pattern.compile(p_VERSION + p_SP + p_STATUS_CODE + " *" + p_REASON_PHRASE, Pattern.CASE_INSENSITIVE);
	private static final Pattern patternPartialStatusLine 
		= Pattern.compile("\\A *" + p_VERSION, Pattern.CASE_INSENSITIVE);

    private String mStatusCodeString;
    private int mStatusCode;
    private String mReasonPhrase;
	
    public HttpResponseHeader() {
        mStatusCodeString = "";
        mReasonPhrase = "";
    }

    public HttpResponseHeader(String data) throws HttpMalformedHeaderException {
        super(data);
    }

    @Override
    public void clear() {
        super.clear();
        mStatusCodeString = "";
        mStatusCode = 0;
        mReasonPhrase	= "";
        
    }
    
    @Override
	public void setMessage(String data) throws HttpMalformedHeaderException {
		super.setMessage(data);
		try {
        	parse();
    	} catch (HttpMalformedHeaderException e) {
        	mMalformedHeader = true;
        	throw e;
    	}
	}
	
    @Override
	public void setVersion(String version) {
		mVersion = version.toUpperCase();
	}

    public int getStatusCode() {
        return mStatusCode;
    }

    public String getReasonPhrase() {
        return mReasonPhrase;
    }

    private void parse() throws HttpMalformedHeaderException {

		Matcher matcher = patternStatusLine.matcher(mStartLine);
		if (!matcher.find()) {
			mMalformedHeader = true;
			throw new HttpMalformedHeaderException("Failed to find pattern: " + patternStatusLine);
		}
		
		mVersion 			= matcher.group(1);
		mStatusCodeString	= matcher.group(2);
		String tmp 			= matcher.group(3);

		mReasonPhrase = (tmp != null) ? tmp : "";
		 
        if (!mVersion.equalsIgnoreCase(HTTP10) && !mVersion.equalsIgnoreCase(HTTP11)) {
			mMalformedHeader = true;
			throw new HttpMalformedHeaderException("Unexpected version: " + mVersion);
			//return false;
		}

    	try {
    		mStatusCode = Integer.parseInt(mStatusCodeString);
     	} catch (NumberFormatException e) {
     		mMalformedHeader = true;
			throw new HttpMalformedHeaderException("Unexpected status code: " + mStatusCodeString);
     	}
    }

    @Override
	public int getContentLength() {
		int len = super.getContentLength();

		if ((mStatusCode >= 100 && mStatusCode < 200) || mStatusCode == HttpStatusCode.NO_CONTENT || mStatusCode == HttpStatusCode.NOT_MODIFIED) {
			return 0;
		} else if (mStatusCode >= 200 && mStatusCode < 300) {
			return len;
		} else if (len > 0) {
			return len;
		} else {
			return 0;
		}
	}

	public static HttpResponseHeader getError(String msg) {
		HttpResponseHeader res = null;
		try {
			res = new HttpResponseHeader(msg);
		} catch (HttpMalformedHeaderException e) {
		}
		return res;
	}

	@Override
	public boolean isImage() {
		String contentType = getHeader(CONTENT_TYPE.toUpperCase());

		if (contentType != null) {
			if (contentType.toLowerCase().indexOf(_CONTENT_TYPE_IMAGE) > -1) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isText() {
		String contentType = getHeader(CONTENT_TYPE.toUpperCase());

		if (contentType != null) {
			if (contentType.toLowerCase().indexOf(_CONTENT_TYPE_TEXT) > -1) {
				return true;
			} else if (contentType.toLowerCase().indexOf(_CONTENT_TYPE_HTML) > -1) {
				return true;
			} else if (contentType.toLowerCase().indexOf(_CONTENT_TYPE_JAVASCRIPT) > -1) {
				return true;
			} else if (contentType.toLowerCase().indexOf(_CONTENT_TYPE_JSON) > -1) {
				return true;
			} else if (contentType.toLowerCase().indexOf(_CONTENT_TYPE_XML) > -1) { 
				return true; 
			}

		}
		return false;
	}
	
	public boolean isHtml() {
		String contentType = getHeader(CONTENT_TYPE.toUpperCase());

		if (contentType != null) {
			if (contentType.toLowerCase().indexOf(_CONTENT_TYPE_HTML) > -1) {
				return true;
			}
		}
		return false;
		
	}
	
	// ZAP: Added method
	public boolean isXml() {
		String contentType = getHeader(CONTENT_TYPE.toUpperCase());

		if (contentType != null) {
			if (contentType.toLowerCase().indexOf(_CONTENT_TYPE_XML) > -1) {
				return true;
			}
		}
		return false;
		
	}
	
	
	public boolean isJavaScript() {
		String contentType = getHeader(CONTENT_TYPE.toUpperCase());

		if (contentType != null) {
			if (contentType.toLowerCase().indexOf(_CONTENT_TYPE_JAVASCRIPT) > -1) {
				return true;
			}
		}
		return false;
	}

	public static boolean isStatusLine(String data) {
		return patternPartialStatusLine.matcher(data).find();
	}
	
	@Override
	public String getPrimeHeader() {
		String prime = getVersion() + " " + getStatusCode();
		if (getReasonPhrase() != null && !getReasonPhrase().equals("")) {
			prime = prime + " " + getReasonPhrase();
		}
		return prime;
	}
	
	// ZAP: Added method for working directly with HttpCookie
	
	/**
	 * Parses the response headers and build a lis of all the http cookies set. For the cookies whose domain
	 * could not be determined, the {@code defaultDomain} is set.
	 * 
	 * @param defaultDomain the default domain
	 * @return the http cookies
	 */
	public List<HttpCookie> getHttpCookies(String defaultDomain) {
		List<HttpCookie> cookies = new LinkedList<>();

		Vector<String> cookiesS = getHeaders(HttpHeader.SET_COOKIE);
		
		if (cookiesS != null) {
			for (String c : cookiesS) {
				cookies.addAll(parseCookieString(c, defaultDomain));
			}
		}

		cookiesS = getHeaders(HttpHeader.SET_COOKIE2);
		if (cookiesS != null) {
			for (String c : cookiesS) {
				cookies.addAll(parseCookieString(c, defaultDomain));
			}
		}
		
		return cookies;
	}
	
	private List<HttpCookie> parseCookieString (String c, String defaultDomain) {
		try {
			List<HttpCookie> parsedCookies = HttpCookie.parse(c);
			if (defaultDomain != null) {
				for (HttpCookie cookie : parsedCookies) {
					if (cookie.getDomain() == null) {
						cookie.setDomain(defaultDomain);
					}
				}
			}
			return parsedCookies;
		} catch (IllegalArgumentException e) {
			if (c.indexOf(',') >= 0) {
				try {
					// Some sites seem to use comma separators, which HttpCookie doesnt like, try replacing them
					List<HttpCookie> parsedCookies = HttpCookie.parse(c.replace(',', ';'));
					if (defaultDomain != null) {
						for (HttpCookie cookie : parsedCookies) {
							if (cookie.getDomain() == null) {
								cookie.setDomain(defaultDomain);
							}
						}
					}
					return parsedCookies;
				} catch (IllegalArgumentException e2) {
					log.error("Failed to parse cookie: " + c, e);
				}
			}
		}
		return new ArrayList<HttpCookie>();
	}
	
	/**
	 * Parses the response headers and build a lis of all the http cookies set. <br/>
	 * NOTE: For the cookies whose domain could not be determined, no domain is set, so this must be taken
	 * into account.
	 * 
	 * @return the http cookies
	 * @deprecated Use the {@link #getHttpCookies(String)} method to take into account the default domain for
	 *             cookie
	 */
	@Deprecated
	public List<HttpCookie> getHttpCookies(){
		return getHttpCookies(null);
	}

	// ZAP: Added method.
	public TreeSet<HtmlParameter> getCookieParams() {
		TreeSet<HtmlParameter> set = new TreeSet<>();
		
		Vector<String> cookies = getHeaders(HttpHeader.SET_COOKIE);
    	if (cookies != null) {
    		Iterator<String> it = cookies.iterator();
    		while (it.hasNext()) {
				set.add(new HtmlParameter(it.next()));
			}
    	}
    	
    	Vector<String> cookies2 = getHeaders(HttpHeader.SET_COOKIE2);
    	if (cookies2 != null) {
    		Iterator<String> it = cookies2.iterator();
    		while (it.hasNext()) {
				set.add(new HtmlParameter(it.next()));
			}
    	}
    	return set;
	}


}
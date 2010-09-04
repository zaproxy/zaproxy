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
package org.parosproxy.paros.network;

import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


abstract public class HttpHeader implements java.io.Serializable{


	public static final String CRLF 			= "\r\n";
	public static final String LF 				= "\n";
	public static final String CONTENT_LENGTH 	= "Content-length";
	public static final String TRANSFER_ENCODING = "Transfer-encoding";
	public static final String CONTENT_ENCODING = "Content-encoding";
	public static final String CONTENT_TYPE 	= "Content-Type";
	public static final String PROXY_CONNECTION = "Proxy-Connection";
	public static final String PROXY_AUTHENTICATE = "Proxy-authenticate";
	public static final String CONNECTION		= "Connection";
	public static final String AUTHORIZATION	= "Authorization";
	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
	public static final String LOCATION				= "Location";
	public static final String IF_MODIFIED_SINCE	= "If-Modified-Since";
	public static final String IF_NONE_MATCH		= "If-None-Match";
	public static final String USER_AGENT		= "User-Agent";
	public static final String ACCEPT_ENCODING = "Accept-Encoding";
	public static final String CACHE_CONTROL	= "Cache-control";
	public static final String PRAGMA			= "Pragma";
	public static final String REFERER			= "Referer";
	
	public static final String HTTP09 	= "HTTP/0.9";
	public static final String HTTP10 	= "HTTP/1.0";
	public static final String HTTP11 	= "HTTP/1.1";
	public static final String _CLOSE 			= "Close";
	public static final String _KEEP_ALIVE 		= "Keep-alive";
	public static final String _CHUNKED			= "Chunked";
	
	public static final String SCHEME_HTTP		= "http://";
	public static final String SCHEME_HTTPS		= "https://";
	public static final String HTTP				= "http";
	public static final String HTTPS			= "https";

	public static final Pattern patternCRLF			= Pattern.compile("\\r\\n", Pattern.MULTILINE);
	public static final Pattern patternLF				= Pattern.compile("\\n", Pattern.MULTILINE);
	
	private static final Pattern patternCharset = Pattern.compile("charset *= *([^;\\s]+)", Pattern.CASE_INSENSITIVE);

	protected static final String p_TEXT		= "[^\\x00-\\x1f\\r\\n]*"; 
	protected static final String p_METHOD		= "(\\w+)";
	protected static final String p_SP			= " +";
	// protected static final String p_URI			= "(\\S+)";
	// allow space in URI for encoding to %20
	protected static final String p_URI			= "([^\\r\\n]+)";
	protected static final String p_VERSION		= "(HTTP/\\d+\\.\\d+)";
	protected static final String p_STATUS_CODE	= "(\\d{3})";
	protected static final String p_REASON_PHRASE = "(" + p_TEXT + ")";
	
	protected String mStartLine = "";
	protected String mMsgHeader = "";
	protected boolean mMalformedHeader = false;
	protected Hashtable mHeaderFields = new Hashtable();
	protected int mContentLength = -1;
	protected String mLineDelimiter = CRLF;
	protected String mVersion = "";
	
	public HttpHeader() {
		init();
	}


	/**
	 * Construct a HttpHeader from a given String.
	 * @param data
	 * @throws HttpMalformedHeaderException
	 */
	public HttpHeader(String data) throws HttpMalformedHeaderException {
		this();
		setMessage(data);
	}

	/**
	 * Inititialization.
	 */
	private void init() {
		mHeaderFields = new Hashtable();
		mStartLine = "";
		mMsgHeader = "";
		mMalformedHeader = false;
		mContentLength = -1;
		mLineDelimiter = CRLF;
		mVersion = "";
	}

	/**
	 * Set and parse this HTTP header with the string given.
	 * 
	 * @param data
	 * @throws HttpMalformedHeaderException
	 */
	public void setMessage(String data) throws HttpMalformedHeaderException {
		init(); 

		mMsgHeader = data;
		try {
			if (!this.parse(data)) {
				mMalformedHeader = true;
			}
		} catch (Exception e) {
			mMalformedHeader = true;
		}

		if (mMalformedHeader) {
			throw new HttpMalformedHeaderException();
		}

	}
	
	public void clear() {
	    init();
	}

	/**
	 * Get the first header value using the name given.  If there are multiple occurence,
	 * only the first one will be returned as String.
	 * @param name
	 * @return the header value.  null if not found.
	 */
	public String getHeader(String name) {
		Vector v = getHeaders(name);
		if (v == null) {
			return null;
		}
		
		return (String) (v.firstElement());
	}
    
	/**
	 * Get headers with the name.  Multiple value can be returned.
	 * @param name
	 * @return a vector holding the value as string.
	 */
    public Vector getHeaders(String name) {
    	Vector v = (Vector) mHeaderFields.get(name.toUpperCase());
    	return v;
    }

    /**
     * Add a header with the name and value given.  It will be appended to the header string.
     * @param name
     * @param val
     */
	public void addHeader(String name, String val) {
		mMsgHeader = mMsgHeader + name + ": " + val + mLineDelimiter;
		addInternalHeaderFields(name, val);
	}

	/**
	 * Set a header name and value.
	 * If the name is not found, it will be added.
	 * If the value is null, the header will be removed.
	 * @param name
	 * @param value
	 */
    public void setHeader(String name, String value) {
//		int pos = 0;
//		int crlfpos = 0;
		Pattern pattern = null;

		if (getHeaders(name) == null && value != null) {
			// header value not found, append to end
			addHeader(name, value);
		} else {
			try {
				pattern = getHeaderRegex(name);
				Matcher matcher = pattern.matcher(mMsgHeader);
				if (value == null) {
					// delete header
					mMsgHeader = matcher.replaceAll("");
				} else {
					// replace header
					String newString = name + ": " + value + mLineDelimiter;
					mMsgHeader = matcher.replaceAll(newString);
				}

				// set into hashtable
				replaceInternalHeaderFields(name, value);
				
			}
			catch (Exception e) {
			}
			
		}
    }
    
    private Pattern getHeaderRegex(String name) throws PatternSyntaxException
	{
		return Pattern.compile("^ *"+ name + " *: *[^\\r\\n]*" + mLineDelimiter, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	}

    /**
     * Return the HTTP version (eg HTTP/1.0, HTTP/1.1)
     * @return
     */
	public String getVersion() {
		return mVersion;
	}

	/**
	 * Set the HTTP version of this header.
	 * @param version
	 */
	abstract public void setVersion(String version);

	/**
	 * Get the content length of this header.
	 *  
	 * @return content length.  -1 means content length not set.
	 */
	public int getContentLength() {
        return mContentLength;
    }

	/**
	 * Set the content length of this header.
	 * @param len
	 */
	public void setContentLength(int len) {
		if (mContentLength != len) {
			setHeader(CONTENT_LENGTH, Integer.toString(len));
			mContentLength = len;
		}
	}

	/**
	 * Check if this header expect connection to be closed.  HTTP/1.0 default to close.  HTTP/1.1 default to keep-alive.
	 * @return
	 */
   	public boolean isConnectionClose() {
		boolean result = true;
		if (mMalformedHeader) {
			return true;
		}

		if (isHttp10()) {
			// HTTP 1.0 default to close unless keep alive.
			result = true;
			try {
				if (getHeader(CONNECTION).equalsIgnoreCase(_KEEP_ALIVE) || getHeader(PROXY_CONNECTION).equalsIgnoreCase(_KEEP_ALIVE)) {
					return false;
				}
			} catch (NullPointerException e) {
			}

		} else if (isHttp11()) {
			// HTTP 1.1 default to keep alive unless close.
			result = false;
			try {
				if (getHeader(CONNECTION).equalsIgnoreCase(_CLOSE)) {
					return true;
				} else if  (getHeader(PROXY_CONNECTION).equalsIgnoreCase(_CLOSE)) {
					return true;
				}
			} catch (NullPointerException e) {
			}
		}
		return result;
	}

   	/**
   	 * Check if this header is HTTP 1.0.
   	 * @return true if HTTP 1.0.
   	 */
	public boolean isHttp10() {
		if (mVersion.equalsIgnoreCase(HTTP10)) {
			return true;
		}
		return false;
	}

	/**
	 * Check if this header is HTTP 1.1.
	 * @return true if HTTP 1.0.
	 */
	public boolean isHttp11() {
		if (mVersion.equalsIgnoreCase(HTTP11)) {
			return true;
		}
		return false;
	}

	/**
	 * Check if Transfer Encoding Chunked is set in this header.
	 * @return true if transfer encoding chunked is set.
	 */
    public boolean isTransferEncodingChunked() {
    	String transferEncoding = getHeader(TRANSFER_ENCODING);
    	if (transferEncoding != null && transferEncoding.equalsIgnoreCase(_CHUNKED)) {
    		return true;
    	}
    	return false;
    }

    /**
     * Parse this Http header using the String given.
     * @param data String to be parsed to form this header.
     * @return
     * @throws Exception
     */
    protected boolean parse(String data) throws Exception {

        String 	token = null,
				name = null,
				value = null;
        int pos = 0;
        Pattern pattern = null;

        if(data == null || data.equals("")) {
            return true;
        }

        if ((pos = data.indexOf(CRLF)) < 0) {
        	if ((pos = data.indexOf(LF)) < 0) {
        		return false;
        	} else {
        		mLineDelimiter = LF;
        		pattern = patternLF;
        	}
        } else {
        	mLineDelimiter = CRLF;
        	pattern = patternCRLF;
        }
        
		String[] split = pattern.split(data);
		mStartLine = split[0];

		StringBuffer sb = new StringBuffer(2048);
		for (int i=1; i<split.length; i++)
		{
			token = split[i];
			if (token.equals("")) {
				continue;
			}
			
            if((pos = token.indexOf(":")) < 0) {
				mMalformedHeader = true;
                return false;
            }
            name  = token.substring(0, pos).trim();
            value = token.substring(pos +1).trim();

            if(name.equalsIgnoreCase(CONTENT_LENGTH)) {
            	try {
                	mContentLength = Integer.parseInt(value);
            	} catch (NumberFormatException nfe){}
            }
			
            /*
            if (name.equalsIgnoreCase(PROXY_CONNECTION)) {
            	sb.append(name + ": " + _CLOSE + mLineDelimiter);
            } else if (name.equalsIgnoreCase(CONNECTION)) {
            	sb.append(name + ": " + _CLOSE + mLineDelimiter);
            } else {
            */
			sb.append(name + ": " + value + mLineDelimiter);
			//}
			
			addInternalHeaderFields(name, value);
		}

        mMsgHeader = sb.toString();
		return true;
	}

    /**
     * Replace the header stored in internal hashtable
     * @param name
     * @param value
     */
	private void replaceInternalHeaderFields(String name, String value) {
		String key = name.toUpperCase();
		Vector v = getHeaders(key);
		if (v == null) {
			v = new Vector();
			mHeaderFields.put(key, v);
		}

		if (value != null) {
		    v.clear();
			v.add(value);
		} else {
			mHeaderFields.remove(key);
		}
	}

	/**
	 * Add the header stored in internal hashtable
	 * @param name
	 * @param value
	 */
	private void addInternalHeaderFields(String name, String value) {
		String key = name.toUpperCase();
		Vector v = getHeaders(key);
		if (v == null) {
			v = new Vector();
			mHeaderFields.put(key, v);
		}

		if (value != null) {
			v.add(value);
		} else {
			mHeaderFields.remove(key);
		}
	}

	
	/**
	 * Get if this is a malformed header.
	 * @return
	 */
    public boolean isMalformedHeader() {
        return mMalformedHeader;
    }
    
    /**
     * Get a string representation of this header.
     */
    public String toString() {
		return getPrimeHeader() + mLineDelimiter + mMsgHeader + mLineDelimiter;
    }

    /**
     * Get the prime header.
     * @return startline for request, statusline for response.
     */
    abstract public String getPrimeHeader();

    /**
     * Get if this is a image header.
     * @return true if image.
     */
    public boolean isImage() {
    	return false;
    }

    /**
     * Get if this is a text header.
     * @return true if text.
     */
    public boolean isText() {
    	return true;
    }

    /**
     * Get the line delimiter of this header.
     * @return
     */
    public String getLineDelimiter() {
    	return mLineDelimiter;
    }

    /**
     * Get the headers as string.  All the headers name value pair is concatenated and delimited.
     * @return Eg "Host: www.example.com\r\nUser-agent: some agent\r\n"
     */
    public String getHeadersAsString() {
    	return mMsgHeader;
    }

    public boolean isEmpty() {
        if (mMsgHeader == null || mMsgHeader.equals("")) {
            return true;
        }
        
        return false;
    }
    
	public String getCharset() {
	    String contentType = getHeader(CONTENT_TYPE);
	    String charset = "";
	    if (contentType == null) {
	        return null;
	    }
	    
	    Matcher matcher = patternCharset.matcher(contentType);
	    if (matcher.find()) {
	        charset = matcher.group(1);
	    }
	    return charset;
	}
}

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpResponseHeader extends HttpHeader {

	public static final String HTTP_CLIENT_BAD_REQUEST = "HTTP/1.0 400 Bad request" + CRLF + CRLF;
	private static final String _CONTENT_TYPE_IMAGE = "image";
	private static final String _CONTENT_TYPE_TEXT = "text";
	private static final String _CONTENT_TYPE_HTML = "html";
	
	
	static final Pattern patternStatusLine
		= Pattern.compile(p_VERSION + p_SP + p_STATUS_CODE + " *" + p_REASON_PHRASE, Pattern.CASE_INSENSITIVE);
	private static final Pattern patternPartialStatusLine 
		= Pattern.compile("\\A *" + p_VERSION, Pattern.CASE_INSENSITIVE);

    private String mStatusCodeString = "";
    private int mStatusCode = 0;
    private String mReasonPhrase	= "";
	
    public HttpResponseHeader() {
		clear();
    }

    public HttpResponseHeader(String data) throws HttpMalformedHeaderException {
        this();
        setMessage(data);
    }

    public void clear() {
        super.clear();
        mStatusCodeString = "";
        mStatusCode = 0;
        mReasonPhrase	= "";
        
    }
	public void setMessage(String data) throws HttpMalformedHeaderException {
		super.setMessage(data);
		try {
        	if (!parse())
        		mMalformedHeader = true;
    	} catch (Exception e) {
        	mMalformedHeader = true;
    	}

    	if (mMalformedHeader) {
    		throw new HttpMalformedHeaderException();
    	}

	}
	
	public void setVersion(String version) {
		mVersion = version.toUpperCase();
	}

    public int getStatusCode() {
        return mStatusCode;
    }

    public String getReasonPhrase() {
        return mReasonPhrase;
    }

    public boolean parse() throws Exception {

		Matcher matcher = patternStatusLine.matcher(mStartLine);
		if (!matcher.find()) {
			mMalformedHeader = true;
			return false;
		}
		
		mVersion 			= matcher.group(1);
		mStatusCodeString	= matcher.group(2);
		String tmp 			= matcher.group(3);

		if (tmp != null) {
			mReasonPhrase		= tmp;
		}
		 
        if (!mVersion.equalsIgnoreCase(HTTP10) && !mVersion.equalsIgnoreCase(HTTP11)) {
			mMalformedHeader = true;
			return false;
		}

    	try {
    		mStatusCode = Integer.parseInt(mStatusCodeString);
     	} catch (NumberFormatException e) {
     		mMalformedHeader = true;
     		return false;
     	}
        return true;
    }

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


	public boolean isImage() {
		String contentType = getHeader(CONTENT_TYPE.toUpperCase());

		if (contentType != null) {
			if (contentType.toLowerCase().indexOf(_CONTENT_TYPE_IMAGE) > -1) {
				return true;
			}
		}

		return false;

	}

	public boolean isText() {
		String contentType = getHeader(CONTENT_TYPE.toUpperCase());

		if (contentType != null) {
			if (contentType.toLowerCase().indexOf(_CONTENT_TYPE_TEXT) > -1) {
				return true;
			} else if (contentType.toLowerCase().indexOf(_CONTENT_TYPE_HTML) > -1) {
				return true;
			}
		}

		return false;

	}

	public static boolean isStatusLine(String data) {
		return patternPartialStatusLine.matcher(data).find();
	}
	
	public String getPrimeHeader() {
		String prime = getVersion() + " " + getStatusCode();
		if (getReasonPhrase() != null && !getReasonPhrase().equals("")) {
			prime = prime + " " + getReasonPhrase();
		}
		return prime;
	}
	


}
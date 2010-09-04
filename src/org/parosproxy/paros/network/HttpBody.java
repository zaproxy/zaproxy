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

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract a HTTP body in request or response messages.
 */
public class HttpBody {

	private StringBuffer body = null;
	public static final String STORAGE_CHARSET = "8859_1";
	public static final String DEFAULT_CHARSET = "8859_1";
	private String charset = DEFAULT_CHARSET;
	private boolean isChangedCharset = false;
    private String cacheString = null;
	//private static Pattern patternCharset = Pattern.compile("<META +[^>]+charset=['\"]*([^>'\"])+['\"]*>", Pattern.CASE_INSENSITIVE| Pattern.MULTILINE);
	private static final Pattern patternCharset = Pattern.compile("<META +[^>]+charset *= *['\\x22]?([^>'\\x22;]+)['\\x22]? *>", Pattern.CASE_INSENSITIVE);
	
	public HttpBody() {
	    body = new StringBuffer();
        cacheString = null;
	}

	/**
	 *	Preallocate HttpBody of a certain size.  A maximum of 256K is fixed to avoid overwhelming
	 * 	by incorrect contentlength.
	 */
	public HttpBody(int capacity) {
	    
	    if (capacity>0) {
	        if (capacity > 128000) {
	            capacity = 128000;
	        }
	        body = new StringBuffer(capacity);
	    } else {
	        body = new StringBuffer();
	    }
        cacheString = null;

	}
	
	/**
	 * Construct a HttpBody from a String.
	 * @param data
	 */
	public HttpBody(String data) {
	    this();
		if (data == null)
			return;
		setBody(data);
        cacheString = null;

	}

	public void setBody(byte[] buf) {
	    //body.setLength(0);
	    body = new StringBuffer(buf.length);
	    append(buf);
        cacheString = null;

	}
	
	/**
	 * Set this HttpBody to store the data supplied.
	 * @param data
	 * @throws UnsupportedEncodingException
	 */
	public void setBody(String data)  {
	    byte[] buf = null;
	    try {
		    //data = new String(data.getBytes(getCharset()), STORAGE_CHARSET);
		    buf = data.getBytes(getCharset());

		} catch (UnsupportedEncodingException e) {
		    e.printStackTrace();
		}
		setBody(buf);
        cacheString = null;

	}

	/**
	 * Append a byte array with certain length to this body.
	 * @param buf
	 * @param len
	 */
	public void append(byte[] buf, int len) {
		if (buf == null)
			return;
		
		String temp = null;
		try {
			temp = new String(buf, 0, len, STORAGE_CHARSET);
		} catch (Exception e) {
			temp = new String(buf, 0, len);
		}
		body.append(temp);
        cacheString = null;

	}
	
	/**
	 * Append a byte array to this body.
	 * @param buf
	 */
	public void append(byte[] buf) {
		if (buf == null)
			return;
		append(buf, buf.length);
        cacheString = null;

	}

	/**
	 * Append a String to this body.
	 * @param buf
	 */
	public void append(String buf) {
		if (buf == null)
			return;

		body.append(buf);
        cacheString = null;

	}

	/**
	 * Get the content of the body as String.
	 */
	public String toString() {

        if (cacheString != null) {
            return cacheString;
        }
        
	    if (isChangedCharset) {
            cacheString = toString(getCharset());
            isChangedCharset = false;
            return cacheString;
	    }
	    
        // undetermined charset;
	    String html = body.toString();

	    Matcher matcher = patternCharset.matcher(html);
		if (matcher.find()) {
			setCharset(matcher.group(1));
		} else {
		    String utf8 = toUTF8();
		    if (utf8 != null) {
                // assume to be UTF8
		        setCharset("UTF8");
                isChangedCharset = false;
                cacheString = utf8;
                return cacheString;
		    }
		    
		}
	    cacheString = toString(getCharset());
		return cacheString;
	}
	
	public String toString(String charset) {
	    String result = "";
		try {
		    if (charset.equalsIgnoreCase(STORAGE_CHARSET)) {
		        result = body.toString();
		    } else {
		        result = new String(getBytes(), charset);
		    }
		} catch (UnsupportedEncodingException e) {
		    result = body.toString();
		}
		return result;
	    
	}
	
	public byte[] getBytes() {
		byte[] result = null;
		try {
			result = body.toString().getBytes(STORAGE_CHARSET);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Current length of the body.
	 * @return
	 */
	public int length() {
		return body.length();
	}

	/**
	 * Set the current length of this body.  If the current content is
	 * longer, the excessive data will be truncated.
	 * @param length
	 */
	public void setLength(int length) {
		body.setLength(length);
        cacheString = null;

	}
	
	
	
    /**
     * @return Returns the charset.
     */
    public String getCharset() {
        return charset;
    }
    /**
     * @param charset The charset to set.
     */
    public void setCharset(String charset) {
        if (charset == null || charset.equals("")) {
            return;
        }
        this.charset = charset;
        isChangedCharset = true;
    }
    
    private String toUTF8() {
        byte[] buf1 = getBytes();
        String utf8 = null;
        byte[] buf2 = null;
        try {
            utf8 = new String(buf1, "UTF8");
            buf2 = utf8.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("UTF8 not supported.  Using 8859_1 instead.");
            return body.toString();
        }

        if (buf1.length != buf2.length) {
            return null;
        }

//        for(int i=0; i<buf1.length; i++) {
//            if (buf1[i] != buf2[i]) {
//                return null;
//            }
//        }
        
        return utf8;
    }
    
    
}
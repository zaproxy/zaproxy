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
// ZAP: 2012/03/15 Changed to use byte[] instead of StringBuffer.

package org.parosproxy.paros.network;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.log4j.Logger;

/**
 * Abstract a HTTP body in request or response messages.
 */
public abstract class HttpBody {

	private static final Logger log = Logger.getLogger(HttpBody.class);
	
	public static final String DEFAULT_CHARSET = "8859_1";

	private byte[] body;
	private int pos;
    private String cachedString;
	private String charset = DEFAULT_CHARSET;
	protected boolean isChangedCharset;
    
	public HttpBody() {
		body = new byte[0];
	}

	/**
	 *	Preallocate HttpBody of a certain size.  A maximum of 128K is fixed to avoid overwhelming
	 * 	by incorrect contentlength.
	 */
	public HttpBody(int capacity) {
	    if (capacity > 0) {
	        if (capacity > 128000) {
	            capacity = 128000;
	        }
	        body = new byte[capacity];
	    } else {
	    	body = new byte[0];
	    }
        pos = 0;
	}
	
	/**
	 * Construct a HttpBody from a String.
	 * @param data
	 */
	public HttpBody(String data) {
		setBody(data);
	}

	public void setBody(byte[] buf) {
		if (buf == null) {
			return;
		}
		cachedString = null;
		
		body = new byte[buf.length];
		System.arraycopy(buf, 0, body, 0, buf.length);
		
		pos = body.length;
	}
	
	/**
	 * Set this HttpBody to store the data supplied.
	 * @param data
	 */
	public void setBody(String data)  {
		if (data == null) {
			return ;
		}
		
		cachedString = null;
		
		try {
			body = data.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
		
		pos = body.length;
	}

	/**
	 * Append a byte array with certain length to this body.
	 * @param buf
	 * @param len
	 */
	public void append(byte[] buf, int len) {
		if (buf == null) {
			return;
		}
		
		if (pos + len > body.length) {
			byte[] newBody = new byte[body.length+len];
			System.arraycopy(body, 0, newBody, 0, body.length);
			System.arraycopy(buf, 0, newBody, body.length, len);
			body = newBody;
			pos = body.length;
		} else {
			System.arraycopy(buf, 0, body, pos, len);
			pos += len;
		}
		
        cachedString = null;
	}
	
	/**
	 * Append a byte array to this body.
	 * 
	 * @param buf
	 */
	public void append(byte[] buf) {
		if (buf == null) {
			return;
		}
		append(buf, buf.length);
	}

	/**
	 * Append a String to this body.
	 * 
	 * @param buf
	 */
	public void append(String buf) {
		if (buf == null) {
			return;
		}
	    try {
	    	append(buf.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Get the content of the body as String.
	 */
	@Override
	public String toString() {
        if (cachedString != null) {
            return cachedString;
        }
        
        cachedString = createCachedString(charset);

        return cachedString;
	}
	
	protected String createCachedString(String charset) {
		String result = "";
		
		try {
			if (isChangedCharset) {
				result = new String(getBytes(), charset);
				isChangedCharset = false;
			} else {
				result = new String(getBytes(), DEFAULT_CHARSET);
				isChangedCharset = false;
			}
		} catch (UnsupportedEncodingException e1) {
			log.error(e1.getMessage(), e1);
			
			try {
				result = new String(getBytes(), DEFAULT_CHARSET);
			} catch(UnsupportedEncodingException e2) {
				log.error(e2.getMessage(), e2);
			}
		}
		return result;
	}
	
	/**
	 * Get the contents of the body as an array of bytes.
	 * 
	 * The returned array of bytes mustn't be modified.
	 * Is returned a reference instead of a copy to avoid more memory allocations.
	 * 
	 * @return a reference to the content of this body as <code>byte[]</code>.
	 */
	public byte[] getBytes() {
		return body;
	}

	/**
	 * Current length of the body.
	 * 
	 * @return the current length of the body.
	 */
	public int length() {
		return body.length;
	}

	/**
	 * Set the current length of this body.  If the current content is
	 * longer, the excessive data will be truncated.
	 * 
	 * @param length the new length to set.
	 */
	public void setLength(int length) {
		if (body.length != length) {
			pos = Math.min(body.length, length);
	
			byte[] newBody = new byte[length];
			System.arraycopy(body, 0, newBody, 0, length);
			body = newBody;
			
			cachedString = null;
		}
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
        if (charset == null || charset.equals("") || charset.equalsIgnoreCase(this.charset)) {
            return;
        }
        this.charset = charset;
        this.isChangedCharset = true;
        this.cachedString = null;
    }
    
	@Override
	public int hashCode() {
		return 31 + Arrays.hashCode(body);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		HttpBody otherBody = (HttpBody) object;
		if (!Arrays.equals(body, otherBody.body)) {
			return false;
		}
		return true;
	}
	
    
}
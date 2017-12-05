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
// ZAP: 2012/03/15 Changed to allow the modifying of the charset and the break lines while
//      encoding of the Base64. Changed to use the updated Base64 class. Changed to use 
//      StringBuilder instead of StringBuffer. Replaced some string concatenations with calls to the 
//      method append of the class StringBuilder.
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2014/03/03 Issue 1012: Support HTML and JavaScript encoding

package org.parosproxy.paros.extension.encoder;

import org.apache.commons.lang.StringEscapeUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import java.lang.*;

public class Encoder {
    
	private static final Logger logger = Logger.getLogger(Encoder.class);

	private int base64EncodeOptions;
	private String base64Charset;

	public Encoder() {
		setBase64DoBreakLines(true);

		this.base64Charset = "UTF-8";
	}
	
    public String getURLEncode(String msg) {
        String result = "";
        try {
            result = URLEncoder.encode(msg, "UTF8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }
    
	public String getURLDecode(String msg) {
	    String result = "";
        try {
            result = URLDecoder.decode(msg, "UTF8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
	}
	
	public byte[] getHashSHA1(byte[] buf) throws NoSuchAlgorithmException {
		
	    MessageDigest sha = MessageDigest.getInstance("SHA-1");
	    sha.update(buf);
	    return sha.digest();
	}

	public byte[] getHashMD5(byte[] buf) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(buf);
        return md5.digest();
    }
	
	public String getHexString(byte[] buf) {
		StringBuilder sb = new StringBuilder(20);
		for (int i=0; i<buf.length; i++) {
			int digit = buf[i] & 0xFF;
			String hexDigit = Integer.toHexString(digit).toUpperCase();
			if (hexDigit.length() == 1) {
				sb.append('0');
			}
			sb.append(hexDigit);
		}
		return sb.toString();
			
	}
	
	public String getHTMLString(String msg){
		return StringEscapeUtils.escapeHtml(msg);
	}
	
	public String getJavaScriptString(String msg){
		return StringEscapeUtils.escapeJavaScript(msg);
	}

	public byte[] getBytes(String buf) {
	    byte[] result = null;
	    try {
	        result = buf.getBytes(base64Charset);
	    } catch (UnsupportedEncodingException e) { 
            logger.error(e.getMessage(), e);
        }
	    return result;
	}
	
	public String getBase64Encode(String msg) throws NullPointerException, IOException {
	    return Base64.encodeBytes(getBytes(msg), base64EncodeOptions);
	}
	
	public String getBase64Decode(String msg) throws IllegalArgumentException, IOException {
		return new String(Base64.decode(msg, Base64.NO_OPTIONS), base64Charset);
	}
	
	public String getIllegalUTF8Encode(String msg, int bytes) {
		char [] input_array = msg.toCharArray();
		
		if (bytes != 4 && bytes != 3) {
			bytes = 2;
		}
		
		//numbers of characters * number of bytes * ("%" + Hex + Hex) 
		StringBuilder sbResult = new StringBuilder(input_array.length * bytes * 3);
		for(char c : input_array) {
			
			if (bytes == 4) {
				sbResult.append('%').append(Integer.toHexString(0xff & ((byte) 0xf0)));
				sbResult.append('%').append(Integer.toHexString(0xff & ((byte) 0x80)));
				sbResult.append('%').append(Integer.toHexString(0xff & ((byte) (0x80 | ((c & 0x7f)>>6)))));
				sbResult.append('%').append(Integer.toHexString(0xff & ((byte) (0x80 | (c & 0x3f)))));
				
			} else if (bytes == 3) {
				sbResult.append('%').append(Integer.toHexString(0xff & ((byte) 0xe0)));
				sbResult.append('%').append(Integer.toHexString(0xff & ((byte) (0x80 | ((c & 0x7f)>>6)))));
				sbResult.append('%').append(Integer.toHexString(0xff & ((byte) (0x80 | (c & 0x3f)))));
			} else {
				sbResult.append('%').append(Integer.toHexString(0xff & ((byte) (0xc0 | ((c & 0x7f)>>6)))));
				sbResult.append('%').append(Integer.toHexString(0xff & ((byte) (0x80 | (c & 0x3f)))));
			}
		}
		
		return sbResult.toString();
	}


	public String getUnicodeEncode(String str) {
        str = (str == null ? "" : str);
        String tmp;
        StringBuffer sb = new StringBuffer(1000);
        char c;
        int i, j;
        sb.setLength(0);
        for (i = 0; i < str.length(); i++)
        {
            c = str.charAt(i);
            sb.append("%u");
            j = (c >>>8); //取出高8位
            tmp = Integer.toHexString(j);
            if (tmp.length() == 1)
                sb.append("0");
            sb.append(tmp);
            j = (c & 0xFF); //取出低8位
            tmp = Integer.toHexString(j);
            if (tmp.length() == 1)
                sb.append("0");
            sb.append(tmp);

        }
        return (new String(sb));
    }

    public String getUnicodeDecode(String str) {
        str = (str == null ? "" : str);
        if (str.indexOf("%u") == -1)//如果不是unicode码则原样返回
            return str;

        StringBuffer sb = new StringBuffer(1000);

        for (int i = 0; i <= str.length() - 6;)
        {
            String strTemp = str.substring(i, i + 6);
            String value = strTemp.substring(2);
            int c = 0;
            for (int j = 0; j < value.length(); j++)
            {
                char tempChar = value.charAt(j);
                int t = 0;
                switch (tempChar)
                {
                    case 'a':
                        t = 10;
                        break;
                    case 'b':
                        t = 11;
                        break;
                    case 'c':
                        t = 12;
                        break;
                    case 'd':
                        t = 13;
                        break;
                    case 'e':
                        t = 14;
                        break;
                    case 'f':
                        t = 15;
                        break;
                    default:
                        t = tempChar - 48;
                        break;
                }

                c += t * ((int) Math.pow(16, (value.length() - j - 1)));
            }
            sb.append((char) c);
            i = i + 6;
        }
        return sb.toString();
    }
	
	public void setBase64DoBreakLines(boolean doBreakLines) {
		if (doBreakLines) {
			base64EncodeOptions = Base64.DO_BREAK_LINES;
		} else {
			base64EncodeOptions = Base64.NO_OPTIONS;
		}
	}
	
	public void setBase64Charset(String charset) {
		base64Charset = charset;
	}
	
}

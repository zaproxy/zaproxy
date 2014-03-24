/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package org.zaproxy.zap.network;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpBody;

public class HttpResponseBody extends HttpBody {

	private static final Logger log = Logger.getLogger(HttpResponseBody.class);
	
	//private static Pattern patternCharset = Pattern.compile("<META +[^>]+charset=['\"]*([^>'\"])+['\"]*>", Pattern.CASE_INSENSITIVE| Pattern.MULTILINE);
	private static final Pattern patternCharset = Pattern.compile("<META +[^>]+charset *= *['\\x22]?([^>'\\x22;]+)['\\x22]? *[/]?>", Pattern.CASE_INSENSITIVE);
	
	
	public HttpResponseBody() {
		super();
	}

	public HttpResponseBody(int capacity) {
		super(capacity);
	}

	public HttpResponseBody(String data) {
		super(data);
	}

	@Override
	public String createCachedString(String charset) {
		String result = null;
		
		if (isChangedCharset) {
			try {
				result = new String(getBytes(), charset);
				isChangedCharset = false;
			} catch (UnsupportedEncodingException e) {
				log.error("Unable to encode with the \"Content-Type\" charset: " + e.getMessage());
			}
		}
		
		if (result == null) {
			result = createCachedStringWithMetaCharset();
		}
		
		return result;
	}
	
	private String createCachedStringWithMetaCharset() {
		String result = null;
		String resultDefaultCharset = null;
		
		try{
			resultDefaultCharset = new String(getBytes(), DEFAULT_CHARSET);
			Matcher matcher = patternCharset.matcher(resultDefaultCharset);
			if (matcher.find()) {
				final String charset = matcher.group(1);
				result = new String(getBytes(), charset);
				setCharset(charset);
				isChangedCharset = false;
			} else {
				String utf8 = toUTF8();
				if (utf8 != null) {
					// assume to be UTF8
					setCharset("UTF8");
					isChangedCharset = false;
					result = utf8;
				} else {
					result = resultDefaultCharset;
				}
			}
		} catch(UnsupportedEncodingException e) {
			log.error("Unable to encode with the (X)HTML meta charset: " + e.getMessage());
			log.warn("Using default charset: 8859_1");
			
			result = resultDefaultCharset;
		}
		
		return result;
	}
	
	private String toUTF8() {
		String utf8 = null;
		
		byte[] buf1 = getBytes();
		int length2 = 0;
		
		try {
			utf8 = new String(buf1, "UTF8");
			length2 = utf8.getBytes("UTF8").length;
		} catch (UnsupportedEncodingException e) {
			log.warn("UTF8 not supported. Using 8859_1 instead.");
			return null;
		}
		
		if (buf1.length != length2) {
			return null;
		}
		
		//for(int i=0; i<buf1.length; i++) {
		//	if (buf1[i] != buf2[i]) {
		//		return null;
		//	}
		//}
		
		return utf8;
	}
	
}

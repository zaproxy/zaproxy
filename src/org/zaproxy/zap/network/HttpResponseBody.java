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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpBody;

public class HttpResponseBody extends HttpBody {

	private static final Logger log = Logger.getLogger(HttpResponseBody.class);
	
	//private static Pattern patternCharset = Pattern.compile("<META +[^>]+charset=['\"]*([^>'\"])+['\"]*>", Pattern.CASE_INSENSITIVE| Pattern.MULTILINE);
	private static final Pattern patternCharset = Pattern.compile("<META +[^>]+charset *= *['\\x22]?([^>'\\x22;]+)['\\x22]? *[/]?>", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Constructs a {@code HttpResponseBody} with no contents (that is, zero length).
	 */
	public HttpResponseBody() {
		super();
	}

	/**
	 * Constructs a {@code HttpResponseBody} with the given initial capacity.
	 * <p>
	 * The initial capacity is limited to prevent allocating big arrays.
	 * 
	 * @param capacity the initial capacity
	 * @see HttpBody#LIMIT_INITIAL_CAPACITY
	 */
	public HttpResponseBody(int capacity) {
		super(capacity);
	}

	/**
	 * Constructs a {@code HttpResponseBody} with the given {@code contents}, using default charset for {@code String} related
	 * operations.
	 * <p>
	 * If the given {@code contents} are {@code null} the {@code HttpResponseBody} will have no content.
	 * <p>
	 * <strong>Note:</strong> If the contents are not representable with the default charset it might lead to data loss.
	 * 
	 * @param contents the contents of the body, might be {@code null}
	 * @see #HttpResponseBody(byte[])
	 * @see HttpBody#DEFAULT_CHARSET
	 */
	public HttpResponseBody(String contents) {
		super(contents);
	}

	/**
	 * Constructs a {@code HttpResponseBody} with the given {@code contents}.
	 * <p>
	 * If the given {@code contents} are {@code null} the {@code HttpResponseBody} will have no content.
	 * 
	 * @param contents the contents of the body, might be {@code null}
	 * @since 2.5.0
	 */
	public HttpResponseBody(byte[] contents) {
		super(contents);
	}

	@Override
	protected String createString(Charset currentCharset) {
		if (currentCharset != null) {
			return super.createString(currentCharset);
		}
		return createStringWithMetaCharset();
	}
	
	private String createStringWithMetaCharset() {
		String result = null;
		String resultDefaultCharset = null;
		
		try{
			resultDefaultCharset = new String(getBytes(), 0, getPos(), StandardCharsets.ISO_8859_1);
			Matcher matcher = patternCharset.matcher(resultDefaultCharset);
			if (matcher.find()) {
				final String charset = matcher.group(1);
				result = new String(getBytes(), 0, getPos(), charset);
				setCharset(charset);
			} else {
				String utf8 = toUTF8();
				if (utf8 != null) {
					// assume to be UTF8
					setCharset(StandardCharsets.UTF_8.name());
					result = utf8;
				} else {
					result = resultDefaultCharset;
				}
			}
		} catch(UnsupportedEncodingException e) {
			log.error("Unable to encode with the (X)HTML meta charset: " + e.getMessage());
			log.warn("Using default charset: " + DEFAULT_CHARSET);
			
			result = resultDefaultCharset;
		}
		
		return result;
	}
	
	private String toUTF8() {
		String utf8 = new String(getBytes(), 0, getPos(), StandardCharsets.UTF_8);
		int length2 = utf8.getBytes(StandardCharsets.UTF_8).length;
		
		if (getPos() != length2) {
			return null;
		}
		
		return utf8;
	}
	
}

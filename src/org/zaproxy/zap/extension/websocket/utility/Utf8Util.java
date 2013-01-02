/*
 * This class incorporates an inner class Utf8StringBuilder copied
 * from jetty trunk trunk @r1179.
 */

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
package org.zaproxy.zap.extension.websocket.utility;

import java.nio.charset.Charset;

/**
 * Encode or decode from byte[] to Utf8 and vice versa.
 */
public abstract class Utf8Util {
	
	/**
	 * Used for en- & decoding from bytes to String and vice versa.
	 */
	protected static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	/**
	 * Helper method to encode payload into UTF-8 string.
	 * 
	 * @param utf8bytes 
	 * @return readable representation
	 * @throws InvalidUtf8Exception 
	 */
	public static String encodePayloadToUtf8(byte[] utf8bytes) throws InvalidUtf8Exception {
		return encodePayloadToUtf8(utf8bytes, 0, utf8bytes.length);
	}
	
	/**
	 * Helper method to encode payload into UTF-8 string.
	 * 
	 * @param utf8bytes
	 * @param offset
	 * @param length
	 * @return readable representation
	 * @throws InvalidUtf8Exception 
	 */
	public static String encodePayloadToUtf8(byte[] utf8bytes, int offset, int length) throws InvalidUtf8Exception {
		try {
			Utf8StringBuilder builder = new Utf8StringBuilder(length);
			builder.append(utf8bytes, offset, length);

			return builder.toString();
		} catch (IllegalArgumentException e) {
			if (e.getMessage().equals("!utf8")) {
				throw new InvalidUtf8Exception("Given bytes are no valid UTF-8!");
			}
			throw e;
			
		} catch (IllegalStateException e) {
			if (e.getMessage().equals("!utf8")) {
				throw new InvalidUtf8Exception("Given bytes are no valid UTF-8!");
			}
			throw e;
		}
	}
	
	/**
	 * Helper method that takes an UTF-8 string and returns its byte
	 * representation.
	 * 
	 * @param utf8string
	 * @return byte representation
	 */
	public static byte[] decodePayloadFromUtf8(String utf8string) {
		return utf8string.getBytes(UTF8_CHARSET);
	}
}

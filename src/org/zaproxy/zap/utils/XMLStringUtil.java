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
package org.zaproxy.zap.utils;

import java.util.HashSet;

public class XMLStringUtil {

	private static HashSet<Character> illegalChrSet = new HashSet<>();
	
	static {
		final String illegalChrs = "\u0000\u0001\u0002\u0003\u0004\u0005" +
	            "\u0006\u0007\u0008\u000B\u000C\u000E\u000F\u0010\u0011\u0012" +
	            "\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C" +
	            "\u001D\u001E\u001F\uFFFE\uFFFF";
		
		for (int i=0; i < illegalChrs.length(); i++) {
			illegalChrSet.add(illegalChrs.charAt(i));
		}
	}

	public static String escapeControlChrs(String str) {
		if (str == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(str.length());
		for (int i=0; i < str.length(); i++) {
			char chr = str.charAt(i);
			if (illegalChrSet.contains(chr)) {
				sb.append("\\x");
				sb.append(String.format("%04x", (int) chr));
			} else {
				sb.append(chr);
			}
		}
		
		return sb.toString();
	}
	
	public static String removeControlChrs(String str) {
		if (str == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(str.length());
		for (int i=0; i < str.length(); i++) {
			char chr = str.charAt(i);
			if (! illegalChrSet.contains(chr)) {
				sb.append(chr);
			}
		}
		
		return sb.toString();
	}

}

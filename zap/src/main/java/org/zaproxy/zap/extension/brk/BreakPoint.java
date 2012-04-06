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
package org.zaproxy.zap.extension.brk;

import java.util.regex.Pattern;

public class BreakPoint implements java.lang.Comparable<BreakPoint>{

	private String url;
	private boolean enabled;
	private Pattern pattern;

	public BreakPoint(String url) {
		this.url = url;
		this.enabled = true;

		compilePattern();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		
		compilePattern();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean match(String url) {
		return pattern.matcher(url).find();
	}

	private void compilePattern() {
		String str = url;

		str = str.replaceAll("\\.", "\\\\.");
		str = str.replaceAll("\\*", ".*?").replaceAll("(;+$)|(^;+)", "");
		str = "(" + str.replaceAll(";+", "|") + ")$";

		pattern = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof BreakPoint) {
			return url.equals(((BreakPoint)object).url);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
		return 78;
	}
	
	@Override
	public int compareTo(BreakPoint breakPoint) {
		return url.compareTo(breakPoint.url);
	}

}

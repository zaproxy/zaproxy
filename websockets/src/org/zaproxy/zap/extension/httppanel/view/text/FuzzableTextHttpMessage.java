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
package org.zaproxy.zap.extension.httppanel.view.text;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.FuzzableMessage;

public class FuzzableTextHttpMessage implements FuzzableMessage {

	public enum Location {HEADER, BODY};
	
	private final HttpMessage httpMessage;
	private final Location location;
	private final int start;
	private final int end;
	
	public FuzzableTextHttpMessage(HttpMessage httpMessage, Location location, int start, int end) {
		this.httpMessage = httpMessage.cloneAll();
		this.location = location;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public HttpMessage getMessage() {
		return httpMessage;
	}

	@Override
	public HttpMessage fuzz(String fuzzString) throws Exception {
		HttpMessage fuzzedHttpMessage = httpMessage.cloneRequest();
		
		String orig;
		if (location == Location.HEADER) {
			orig = fuzzedHttpMessage.getRequestHeader().toString();
		} else {
			orig = fuzzedHttpMessage.getRequestBody().toString();
		}
		
		StringBuilder sb = new StringBuilder(start + fuzzString.length() + orig.length() - end);
		
		sb.append(orig.substring(0, start));
		sb.append(fuzzString);
		sb.append(orig.substring(end));
		
		if (location == Location.HEADER) {
	        fuzzedHttpMessage.setRequestHeader(sb.toString());
		} else {
			fuzzedHttpMessage.setRequestBody(sb.toString());
		}
		
		return fuzzedHttpMessage;
	}

}

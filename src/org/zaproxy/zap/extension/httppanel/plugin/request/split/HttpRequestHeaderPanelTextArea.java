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
package org.zaproxy.zap.extension.httppanel.plugin.request.split;

import javax.swing.text.BadLocationException;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextArea;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpRequestHeaderPanelTextArea extends HttpPanelTextArea {

	private static final long serialVersionUID = -4532294585338584747L;
	
	//private static final String HTTP_REQUEST_HEADER = "HTTP Request Header";
	
	//private static final String SYNTAX_STYLE_HTTP_REQUEST_HEADER = "text/http-request-header";

	private static RequestHeaderTokenMakerFactory tokenMakerFactory = null;
	
	public HttpRequestHeaderPanelTextArea(HttpMessage httpMessage) {
		super(httpMessage);
		
		//addSyntaxStyle(HTTP_REQUEST_HEADER, SYNTAX_STYLE_HTTP_REQUEST_HEADER);
		
		//setSyntaxEditingStyle(SYNTAX_STYLE_HTTP_REQUEST_HEADER);
	}
	
	@Override
	public SearchMatch getTextSelection() {
		//This only happens in the Request/Response Header
		//As we replace all \r\n with \n we must add one character
		//for each line until the line where the selection is.
		int start = getSelectionStart();
		try {
			start += getLineOfOffset(start);
		} catch (BadLocationException e) {
			//Shouldn't happen.
		}

		int end = getSelectionEnd();
		try {
			end += getLineOfOffset(end);
		} catch (BadLocationException e) {
			//Shouldn't happen.
		}
		
		SearchMatch sm = new SearchMatch(
				getHttpMessage(),
				SearchMatch.Location.REQUEST_HEAD,
				start,
				end);
		
		return sm;
	}
	
	@Override
	public void highlight(SearchMatch sm) {
		if (!SearchMatch.Location.REQUEST_HEAD.equals(sm.getLocation())) {
			return;
		}
		
		//As we replace all \r\n with \n we must subtract one character
		//for each line until the line where the selection is.
		int t = 0;
		String header = sm.getMessage().getRequestHeader().toString();
		
		int pos = 0;
		while ((pos = header.indexOf("\r\n", pos)) != -1 && pos < sm.getStart()) {
			pos += 2;
			++t;
		}
		
		int len = this.getText().length();
		if (sm.getStart()-t > len || sm.getEnd()-t > len) {
			return;
		}
		
		highlight(sm.getStart()-t, sm.getEnd()-t);
	}
	
	@Override
	protected synchronized CustomTokenMakerFactory getTokenMakerFactory() {
		if (tokenMakerFactory == null) {
			tokenMakerFactory = new RequestHeaderTokenMakerFactory();
		}
		return tokenMakerFactory;
	}
	
	private static class RequestHeaderTokenMakerFactory extends CustomTokenMakerFactory {
		
		public RequestHeaderTokenMakerFactory() {
			//String pkg = "";
			
			//putMapping(SYNTAX_STYLE_HTTP_REQUEST_HEADER, pkg + "HttpRequestHeaderTokenMaker");
		}
	}
}

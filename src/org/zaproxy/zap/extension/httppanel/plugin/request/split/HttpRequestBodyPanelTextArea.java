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

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.AutoDetectSyntaxHttpPanelTextArea;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpRequestBodyPanelTextArea extends AutoDetectSyntaxHttpPanelTextArea {

	private static final long serialVersionUID = -2102275261139781996L;

	private static final String X_WWW_FORM_URLENCODED = Constant.messages.getString("http.panel.text.syntax.xWwwFormUrlencoded");

	private static final String SYNTAX_STYLE_X_WWW_FORM = "application/x-www-form-urlencoded";
	
	private static RequestBodyTokenMakerFactory tokenMakerFactory = null;

	public HttpRequestBodyPanelTextArea(HttpMessage httpMessage) {
		super(httpMessage);

		addSyntaxStyle(X_WWW_FORM_URLENCODED, SYNTAX_STYLE_X_WWW_FORM);
	}

	@Override
	public SearchMatch getTextSelection() {
		SearchMatch sm = new SearchMatch(
				getHttpMessage(),
				SearchMatch.Location.REQUEST_BODY,
				getSelectionStart(),
				getSelectionEnd());
				
		return sm;
	}
	
	@Override
	public void highlight(SearchMatch sm) {
		if (!SearchMatch.Location.REQUEST_BODY.equals(sm.getLocation())) {
			return;
		}
		
		int len = getText().length();
		if (sm.getStart() > len || sm.getEnd() > len) {
			return;
		}
		
		highlight(sm.getStart(), sm.getEnd());
	}
	
	@Override
	protected String detectSyntax(HttpMessage httpMessage) {
		String syntax = null;
		if (httpMessage != null && httpMessage.getRequestHeader() != null) {
			String contentType = httpMessage.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
			if(contentType != null && !contentType.isEmpty()) {
				contentType = contentType.toLowerCase();
				final int pos = contentType.indexOf(';');
				if (pos != -1) {
					contentType = contentType.substring(0, pos).trim();
				}
				syntax = contentType;
			}
		}
		return syntax;
	}
	
	@Override
	protected synchronized CustomTokenMakerFactory getTokenMakerFactory() {
		if (tokenMakerFactory == null) {
			tokenMakerFactory = new RequestBodyTokenMakerFactory();
		}
		return tokenMakerFactory;
	}
	
	private static class RequestBodyTokenMakerFactory extends CustomTokenMakerFactory {
		
		public RequestBodyTokenMakerFactory() {
			String pkg = "org.zaproxy.zap.extension.httppanel.view.text.lexers.";
			
			putMapping(SYNTAX_STYLE_X_WWW_FORM, pkg + "WwwFormTokenMaker");
		}
	}
}

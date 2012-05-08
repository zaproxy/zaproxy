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
package org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.components.split.request;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.fuzz.FuzzableComponent;
import org.zaproxy.zap.extension.fuzz.FuzzableHttpMessage;
import org.zaproxy.zap.extension.httppanel.view.models.request.RequestBodyStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.text.FuzzableTextHttpMessage;
import org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.AutoDetectSyntaxHttpPanelTextArea;
import org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpRequestBodyPanelSyntaxHighlightTextView extends HttpPanelSyntaxHighlightTextView {

	public HttpRequestBodyPanelSyntaxHighlightTextView(RequestBodyStringHttpPanelViewModel model) {
		super(model);
	}
	
	@Override
	protected HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
		return new HttpRequestBodyPanelSyntaxHighlightTextArea();
	}
	
	private static class HttpRequestBodyPanelSyntaxHighlightTextArea extends AutoDetectSyntaxHttpPanelTextArea implements FuzzableComponent {

		private static final long serialVersionUID = -2102275261139781996L;

		private static final String X_WWW_FORM_URLENCODED = Constant.messages.getString("http.panel.view.syntaxtext.syntax.xWwwFormUrlencoded");

		private static final String SYNTAX_STYLE_X_WWW_FORM = "application/x-www-form-urlencoded";
		
		private static RequestBodyTokenMakerFactory tokenMakerFactory = null;

		public HttpRequestBodyPanelSyntaxHighlightTextArea() {
			addSyntaxStyle(X_WWW_FORM_URLENCODED, SYNTAX_STYLE_X_WWW_FORM);
		}

		@Override
		public boolean canFuzz() {
			//Currently do not allow to fuzz if the text area is editable, because the HttpMessage used is not updated with the changes.
			if (isEditable()) {
				return false;
			}
			
			final String selectedText = getSelectedText();
			if (selectedText == null || selectedText.isEmpty()) {
				return false;
			}
			
			return true;
		}
		
		@Override
		public String getFuzzTarget() {
			return getSelectedText();
		}
		
		@Override
		public FuzzableHttpMessage getFuzzableHttpMessage() {
			return new FuzzableTextHttpMessage(getHttpMessage(), FuzzableTextHttpMessage.Location.BODY, getSelectionStart(), getSelectionEnd());
		}

		@Override
		public void search(Pattern p, List<SearchMatch> matches) {
			Matcher m = p.matcher(getText());
			while (m.find()) {
				matches.add(new SearchMatch(SearchMatch.Location.REQUEST_BODY, m.start(), m.end()));
			}
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
				String pkg = "org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.lexers.";
				
				putMapping(SYNTAX_STYLE_X_WWW_FORM, pkg + "WwwFormTokenMaker");
			}
		}
	}

}


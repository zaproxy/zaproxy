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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.split.request;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.multiFuzz.FuzzableComponent;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestBodyStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.AutoDetectSyntaxHttpPanelTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.util.CaretVisibilityEnforcerOnFocusGain;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpRequestBodyPanelSyntaxHighlightTextView extends HttpPanelSyntaxHighlightTextView {

	public HttpRequestBodyPanelSyntaxHighlightTextView(RequestBodyStringHttpPanelViewModel model) {
		super(model);
	}
	
	@Override
	protected HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
		return new HttpRequestBodyPanelSyntaxHighlightTextArea();
	}
	
	private static class HttpRequestBodyPanelSyntaxHighlightTextArea extends AutoDetectSyntaxHttpPanelTextArea implements FuzzableComponent<HttpMessage> {

		private static final long serialVersionUID = -2102275261139781996L;

		private static final String X_WWW_FORM_URLENCODED = Constant.messages.getString("http.panel.view.syntaxtext.syntax.xWwwFormUrlencoded");
		private static final String JAVASCRIPT = Constant.messages.getString("http.panel.view.syntaxtext.syntax.javascript");
		private static final String JSON = Constant.messages.getString("http.panel.view.syntaxtext.syntax.json");
		private static final String XML = Constant.messages.getString("http.panel.view.syntaxtext.syntax.xml");
		
		private static final String SYNTAX_STYLE_X_WWW_FORM = "application/x-www-form-urlencoded";
		
		private static RequestBodyTokenMakerFactory tokenMakerFactory = null;

		private CaretVisibilityEnforcerOnFocusGain caretVisiblityEnforcer;
		
		public HttpRequestBodyPanelSyntaxHighlightTextArea() {
			addSyntaxStyle(X_WWW_FORM_URLENCODED, SYNTAX_STYLE_X_WWW_FORM);
			addSyntaxStyle(JAVASCRIPT, SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
			addSyntaxStyle(JSON, SyntaxConstants.SYNTAX_STYLE_JSON);
			addSyntaxStyle(XML, SyntaxConstants.SYNTAX_STYLE_XML);
			
			caretVisiblityEnforcer = new CaretVisibilityEnforcerOnFocusGain(this);
		}
		
		@Override
		public void setMessage(Message aMessage) {
			super.setMessage(aMessage);
			
			caretVisiblityEnforcer.setEnforceVisibilityOnFocusGain(aMessage != null);
		}
        
        @Override
        public Class<? extends Message> getMessageClass() {
            return HttpMessage.class;
        }

		@Override
		public boolean canFuzz() {
			if (getMessage() == null) {
				return false;
			}
			
			//Currently do not allow to fuzz if the text area is editable, because the HttpMessage used is not updated with the changes.
			return !isEditable();
		}
		
		@Override
		public HttpMessage getFuzzableMessage() {
			return (HttpMessage)getMessage();
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
					contentType = contentType.toLowerCase(Locale.ENGLISH);
					final int pos = contentType.indexOf(';');
					if (pos != -1) {
						contentType = contentType.substring(0, pos).trim();
					}
					if (contentType.contains("javascript")) {
						syntax = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
					} else if(contentType.contains("json")) {
						syntax = SyntaxConstants.SYNTAX_STYLE_JSON;
					} else if (contentType.contains("xml")) {
						syntax = SyntaxConstants.SYNTAX_STYLE_XML;
					} else {
						syntax = contentType;
					}
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
				String pkg = "org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.lexers.";
				
				putMapping(SYNTAX_STYLE_X_WWW_FORM, pkg + "WwwFormTokenMaker");
				
				pkg = "org.fife.ui.rsyntaxtextarea.modes.";
				putMapping(SYNTAX_STYLE_JAVASCRIPT, pkg + "JavaScriptTokenMaker");
				putMapping(SYNTAX_STYLE_JSON, pkg + "JsonTokenMaker");
				putMapping(SYNTAX_STYLE_XML, pkg + "XMLTokenMaker");
			}
		}
	}

}


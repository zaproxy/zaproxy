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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.split.response;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseBodyStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.AutoDetectSyntaxHttpPanelTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpResponseBodyPanelSyntaxHighlightTextView extends HttpPanelSyntaxHighlightTextView {

	public HttpResponseBodyPanelSyntaxHighlightTextView(ResponseBodyStringHttpPanelViewModel model) {
		super(model);
	}
	
	@Override
	protected HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
		return new HttpResponseBodyPanelSyntaxHighlightTextArea();
	}
	
	private static class HttpResponseBodyPanelSyntaxHighlightTextArea extends AutoDetectSyntaxHttpPanelTextArea {

		private static final long serialVersionUID = -8952571125337022950L;
		
		private static final String CSS = Constant.messages.getString("http.panel.view.syntaxtext.syntax.css");
		private static final String HTML = Constant.messages.getString("http.panel.view.syntaxtext.syntax.html");
		private static final String JAVASCRIPT = Constant.messages.getString("http.panel.view.syntaxtext.syntax.javascript");
		private static final String JSON = Constant.messages.getString("http.panel.view.syntaxtext.syntax.json");
		private static final String XML = Constant.messages.getString("http.panel.view.syntaxtext.syntax.xml");
		
		private static ResponseBodyTokenMakerFactory tokenMakerFactory = null;
		
		public HttpResponseBodyPanelSyntaxHighlightTextArea() {
			addSyntaxStyle(CSS, SyntaxConstants.SYNTAX_STYLE_CSS);
			addSyntaxStyle(HTML, SyntaxConstants.SYNTAX_STYLE_HTML);
			addSyntaxStyle(JAVASCRIPT, SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
			addSyntaxStyle(JSON, SyntaxConstants.SYNTAX_STYLE_JSON);
			addSyntaxStyle(XML, SyntaxConstants.SYNTAX_STYLE_XML);
		}

		@Override
		public void search(Pattern p, List<SearchMatch> matches) {
			Matcher m = p.matcher(getText());
			while (m.find()) {
				matches.add(new SearchMatch(SearchMatch.Location.RESPONSE_BODY, m.start(), m.end()));
			}
		}
		
		@Override
		public void highlight(SearchMatch sm) {
			if (!SearchMatch.Location.RESPONSE_BODY.equals(sm.getLocation())) {
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
			if (httpMessage != null) {
				String contentType = httpMessage.getResponseHeader().getHeader(HttpHeader.CONTENT_TYPE);
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
					} else if (contentType.contains("xhtml")) {
						syntax = SyntaxConstants.SYNTAX_STYLE_HTML;
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
				tokenMakerFactory = new ResponseBodyTokenMakerFactory();
			}
			return tokenMakerFactory;
		}
		
		private static class ResponseBodyTokenMakerFactory extends CustomTokenMakerFactory {
			
			public ResponseBodyTokenMakerFactory() {
				String pkg = "org.fife.ui.rsyntaxtextarea.modes.";
				
				putMapping(SYNTAX_STYLE_CSS, pkg + "CSSTokenMaker");
				putMapping(SYNTAX_STYLE_HTML, pkg + "HTMLTokenMaker");
				putMapping(SYNTAX_STYLE_JAVASCRIPT, pkg + "JavaScriptTokenMaker");
				putMapping(SYNTAX_STYLE_JSON, pkg + "JsonTokenMaker");
				putMapping(SYNTAX_STYLE_XML, pkg + "XMLTokenMaker");
			}
		}
	}
}

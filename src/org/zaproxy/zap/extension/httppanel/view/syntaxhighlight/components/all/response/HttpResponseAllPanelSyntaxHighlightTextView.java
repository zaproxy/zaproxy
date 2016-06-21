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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.all.response;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.AutoDetectSyntaxHttpPanelTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpResponseAllPanelSyntaxHighlightTextView extends HttpPanelSyntaxHighlightTextView {

	private static final String CSS = Constant.messages.getString("http.panel.view.syntaxtext.syntax.css");
	private static final String HTML = Constant.messages.getString("http.panel.view.syntaxtext.syntax.html");
	private static final String JAVASCRIPT = Constant.messages.getString("http.panel.view.syntaxtext.syntax.javascript");
	private static final String JSON = Constant.messages.getString("http.panel.view.syntaxtext.syntax.json");
	private static final String XML = Constant.messages.getString("http.panel.view.syntaxtext.syntax.xml");

	public HttpResponseAllPanelSyntaxHighlightTextView(ResponseStringHttpPanelViewModel model) {
		super(model);
	}
	
	@Override
	protected HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
		return new HttpResponseAllPanelSyntaxHighlightTextArea();
	}
	
	private static class HttpResponseAllPanelSyntaxHighlightTextArea extends AutoDetectSyntaxHttpPanelTextArea {

		private static final long serialVersionUID = 3665478428546560762L;
		
		private static final Logger log = Logger.getLogger(HttpResponseAllPanelSyntaxHighlightTextArea.class);

		//private static final String HTTP_RESPONSE_HEADER_AND_BODY = "HTTP Response Header and Body";

		//private static final String SYNTAX_STYLE_HTTP_RESPONSE_HEADER_AND_BODY = "text/http-response-header-body";

		private static ResponseAllTokenMakerFactory tokenMakerFactory = null;

		public HttpResponseAllPanelSyntaxHighlightTextArea() {
			//addSyntaxStyle(HTTP_RESPONSE_HEADER_AND_BODY, SYNTAX_STYLE_HTTP_RESPONSE_HEADER_AND_BODY);
			addSyntaxStyle(CSS, SyntaxConstants.SYNTAX_STYLE_CSS);
			addSyntaxStyle(HTML, SyntaxConstants.SYNTAX_STYLE_HTML);
			addSyntaxStyle(JAVASCRIPT, SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
			addSyntaxStyle(JSON, SyntaxConstants.SYNTAX_STYLE_JSON);
			addSyntaxStyle(XML, SyntaxConstants.SYNTAX_STYLE_XML);
		}
		
		@Override
		public void search(Pattern p, List<SearchMatch> matches) {
			HttpMessage httpMessage = (HttpMessage)getMessage();
			//This only happens in the Request/Response Header
			//As we replace all \r\n with \n we must add one character
			//for each line until the line where the selection is.
			int tHeader = 0;
			String header = httpMessage.getResponseHeader().toString();
			int pos = 0;
			while ((pos = header.indexOf("\r\n", pos)) != -1) {
				pos += 2;
				++tHeader;
			}
			
			int headerLen = header.length();
			
			Matcher m = p.matcher(getText());
			int start;
			int end;
			while (m.find()) {
				start = m.start();
				end = m.end();
				
				if (start+tHeader < headerLen) {
					try {
						start += getLineOfOffset(start);
					} catch (BadLocationException e) {
						//Shouldn't happen, but in case it does log it and return.
						log.error(e.getMessage(), e);
						return;
					}
					try {
						end += getLineOfOffset(end);
					} catch (BadLocationException e) {
						//Shouldn't happen, but in case it does log it and return.
						log.error(e.getMessage(), e);
						return;
					}
					if (end > headerLen) {
						end = headerLen;
					}
					matches.add(new SearchMatch(SearchMatch.Location.RESPONSE_HEAD, start, end));
				} else {
					start += tHeader - headerLen;
					end += tHeader - headerLen;
				
					matches.add(new SearchMatch(SearchMatch.Location.RESPONSE_BODY, start, end));
				}
			}
		}
		
		@Override
		public void highlight(SearchMatch sm) {
			if (!(SearchMatch.Location.RESPONSE_HEAD.equals(sm.getLocation()) ||
				SearchMatch.Location.RESPONSE_BODY.equals(sm.getLocation()))) {
				return;
			}
			
			final boolean isBody = SearchMatch.Location.RESPONSE_BODY.equals(sm.getLocation());
			
			//As we replace all \r\n with \n we must subtract one character
			//for each line until the line where the selection is.
			int t = 0;
			String header = sm.getMessage().getResponseHeader().toString();
			int pos = 0;
			while ((pos = header.indexOf("\r\n", pos)) != -1) {
				pos += 2;
				
				if (!isBody && pos > sm.getStart()) {
					break;
				}
				
				++t;
			}
			
			int start = sm.getStart()-t;
			int end = sm.getEnd()-t;
			
			if (isBody) {
				start += header.length();
				end += header.length();
			}
			
			int len = this.getText().length();
			if (start > len || end > len) {
				return;
			}
			
			highlight(start, end);
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
				tokenMakerFactory = new ResponseAllTokenMakerFactory();
			}
			return tokenMakerFactory;
		}
		
		private static class ResponseAllTokenMakerFactory extends CustomTokenMakerFactory {
			
			public ResponseAllTokenMakerFactory() {
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
	

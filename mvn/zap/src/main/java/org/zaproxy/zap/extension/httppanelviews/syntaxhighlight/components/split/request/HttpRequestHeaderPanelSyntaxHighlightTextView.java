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

import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.fuzz.FuzzableComponent;
import org.zaproxy.zap.extension.fuzz.FuzzableHttpMessage;
import org.zaproxy.zap.extension.httppanel.view.models.request.RequestHeaderStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.text.FuzzableTextHttpMessage;
import org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpRequestHeaderPanelSyntaxHighlightTextView extends HttpPanelSyntaxHighlightTextView {

	public HttpRequestHeaderPanelSyntaxHighlightTextView(RequestHeaderStringHttpPanelViewModel model) {
		super(model);
	}
	
	@Override
	protected HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
		return new HttpRequestHeaderPanelSyntaxHighlightTextArea();
	}
	
	private static class HttpRequestHeaderPanelSyntaxHighlightTextArea extends HttpPanelSyntaxHighlightTextArea implements FuzzableComponent {

		private static final long serialVersionUID = -4532294585338584747L;
		
		private static final Logger log = Logger.getLogger(HttpRequestHeaderPanelSyntaxHighlightTextArea.class);
		
		//private static final String HTTP_REQUEST_HEADER = "HTTP Request Header";
		
		//private static final String SYNTAX_STYLE_HTTP_REQUEST_HEADER = "text/http-request-header";

		private static RequestHeaderTokenMakerFactory tokenMakerFactory = null;
		
		public HttpRequestHeaderPanelSyntaxHighlightTextArea() {
			//addSyntaxStyle(HTTP_REQUEST_HEADER, SYNTAX_STYLE_HTTP_REQUEST_HEADER);
			
			//setSyntaxEditingStyle(SYNTAX_STYLE_HTTP_REQUEST_HEADER);
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
			int start = getSelectionStart();
			try {
				start += getLineOfOffset(start);
			} catch (BadLocationException e) {
				//Shouldn't happen, but in case it does log it and return.
				log.error(e.getMessage(), e);
				return new FuzzableTextHttpMessage(getHttpMessage(), FuzzableTextHttpMessage.Location.HEADER, 0, 0);
			}

			int end = getSelectionEnd();
			try {
				end += getLineOfOffset(end);
			} catch (BadLocationException e) {
				//Shouldn't happen, but in case it does log it and return.
				log.error(e.getMessage(), e);
				return new FuzzableTextHttpMessage(getHttpMessage(), FuzzableTextHttpMessage.Location.HEADER, 0, 0);
			}
			
			return new FuzzableTextHttpMessage(getHttpMessage(), FuzzableTextHttpMessage.Location.HEADER, start, end);
		}
		
		@Override
		public void search(Pattern p, List<SearchMatch> matches) {
			int start;
			int end;
			Matcher m = p.matcher(getText());
			while (m.find()) {

				//This only happens in the Request/Response Header
				//As we replace all \r\n with \n we must add one character
				//for each line until the line where the match is.
				start = m.start();
				try {
					start += getLineOfOffset(start);
				} catch (BadLocationException e) {
					//Shouldn't happen, but in case it does log it and return.
					log.error(e.getMessage(), e);
					return;
				}

				end = m.end();
				try {
					end += getLineOfOffset(end);
				} catch (BadLocationException e) {
					//Shouldn't happen, but in case it does log it and return.
					log.error(e.getMessage(), e);
					return;
				}
				
				matches.add(new SearchMatch(SearchMatch.Location.REQUEST_HEAD, start, end));
			}
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
				//String pkg = "org.zaproxy.zap.extension.httppanel.view.text.lexers.";
				
				//putMapping(SYNTAX_STYLE_HTTP_REQUEST_HEADER, pkg + "HttpRequestHeaderTokenMaker");
			}
		}
	}

}


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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.all.request;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.fuzz.FuzzableComponent;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.FuzzableMessage;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.text.FuzzableTextHttpMessage;
import org.zaproxy.zap.extension.httppanel.view.util.CaretVisibilityEnforcerOnFocusGain;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpRequestAllPanelSyntaxHighlightTextView extends HttpPanelSyntaxHighlightTextView {
	
	public HttpRequestAllPanelSyntaxHighlightTextView(RequestStringHttpPanelViewModel model) {
		super(model);
	}
	
	@Override
	protected HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
		return new HttpRequestAllPanelSyntaxHighlightTextArea();
	}
	
	protected static class HttpRequestAllPanelSyntaxHighlightTextArea extends HttpPanelSyntaxHighlightTextArea implements FuzzableComponent {

		private static final long serialVersionUID = 923466158533211593L;
		
		private static final Logger log = Logger.getLogger(HttpRequestAllPanelSyntaxHighlightTextArea.class);
		
		//private static final String HTTP_REQUEST_HEADER_AND_BODY = "HTTP Request Header and Body";

		//private static final String SYNTAX_STYLE_HTTP_REQUEST_HEADER_AND_BODY = "text/http-request-header-body";
		
		private static RequestAllTokenMakerFactory tokenMakerFactory = null;

		private CaretVisibilityEnforcerOnFocusGain caretVisiblityEnforcer;
		
		public HttpRequestAllPanelSyntaxHighlightTextArea() {
			//addSyntaxStyle(HTTP_REQUEST_HEADER_AND_BODY, SYNTAX_STYLE_HTTP_REQUEST_HEADER_AND_BODY);
		
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
		public FuzzableMessage getFuzzableMessage() {
			HttpMessage httpMessage = (HttpMessage)getMessage();
			//This only happens in the Request/Response Header
			//As we replace all \r\n with \n we must add one character
			//for each line until the line where the selection is.
			int tHeader = 0;
			String header = httpMessage.getRequestHeader().toString();
			int pos = 0;
			while ((pos = header.indexOf("\r\n", pos)) != -1) {
				pos += 2;
				++tHeader;
			}

			int start = getSelectionStart();
			int end = getSelectionEnd();
			FuzzableTextHttpMessage.Location location;
			
			int headerLen = header.length();
			if (start + tHeader < headerLen) {
				try {
					start += getLineOfOffset(start);
				} catch (BadLocationException e) {
					//Shouldn't happen, but in case it does log it and return.
					log.error(e.getMessage(), e);
					return new FuzzableTextHttpMessage((HttpMessage)getMessage(), FuzzableTextHttpMessage.Location.HEADER, 0, 0);
				}
				
				try {
					end += getLineOfOffset(end);
				} catch (BadLocationException e) {
					//Shouldn't happen, but in case it does log it and return.
					log.error(e.getMessage(), e);
					return new FuzzableTextHttpMessage((HttpMessage)getMessage(), FuzzableTextHttpMessage.Location.HEADER, start, 0);
				}
				
				if (end > headerLen) {
					end = headerLen;
				}
				location = FuzzableTextHttpMessage.Location.HEADER;
			} else {
				start += tHeader - headerLen;
				end += tHeader - headerLen;
				
				location = FuzzableTextHttpMessage.Location.BODY;
			}
			
			return new FuzzableTextHttpMessage((HttpMessage)getMessage(), location, start, end);
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
		public String getFuzzTarget() {
			final String selectedText = getSelectedText();
			if (selectedText != null) {
				return selectedText;
			}
			return "";
		}
		
		@Override
		public void search(Pattern p, List<SearchMatch> matches) {
			HttpMessage httpMessage = (HttpMessage)getMessage();
			//This only happens in the Request/Response Header
			//As we replace all \r\n with \n we must add one character
			//for each line until the line where the selection is.
			int tHeader = 0;
			String header = httpMessage.getRequestHeader().toString();
			int pos = 0;
			while ((pos = header.indexOf("\r\n", pos)) != -1) {
				pos += 2;
				++tHeader;
			}
			
			final int headerLen = header.length();
			final int diff = tHeader - headerLen;
			
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
					matches.add(new SearchMatch(SearchMatch.Location.REQUEST_HEAD, start, end));
				} else {
					start += diff;
					end += diff;
				
					matches.add(new SearchMatch(SearchMatch.Location.REQUEST_BODY, start, end));
				}
			}
		}
		
		@Override
		public void highlight(SearchMatch sm) {
			if (!(SearchMatch.Location.REQUEST_HEAD.equals(sm.getLocation()) ||
				SearchMatch.Location.REQUEST_BODY.equals(sm.getLocation()))) {
				return;
			}
			
			final boolean isBody = SearchMatch.Location.REQUEST_BODY.equals(sm.getLocation());
			
			//As we replace all \r\n with \n we must subtract one character
			//for each line until the line where the selection is.
			int t = 0;
			String header = sm.getMessage().getRequestHeader().toString();
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
		protected synchronized CustomTokenMakerFactory getTokenMakerFactory() {
			if (tokenMakerFactory == null) {
				tokenMakerFactory = new RequestAllTokenMakerFactory();
			}
			return tokenMakerFactory;
		}
		
		private static class RequestAllTokenMakerFactory extends CustomTokenMakerFactory {
			
			public RequestAllTokenMakerFactory() {
				//String pkg = "";

				//putMapping(SYNTAX_STYLE_HTTP_REQUEST_HEADER_AND_BODY, pkg + "HttpRequestHeaderAndBodyTokenMaker");
			}
		}
	}
}

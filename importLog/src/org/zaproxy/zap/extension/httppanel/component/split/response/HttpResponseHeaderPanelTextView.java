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
package org.zaproxy.zap.extension.httppanel.component.split.response;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseHeaderStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextArea;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextView;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpResponseHeaderPanelTextView extends HttpPanelTextView {
	
	public HttpResponseHeaderPanelTextView(ResponseHeaderStringHttpPanelViewModel model) {
		super(model);
	}
	
	@Override
	protected HttpPanelTextArea createHttpPanelTextArea() {
		return new HttpResponseHeaderPanelTextArea();
	}
	
	private static class HttpResponseHeaderPanelTextArea extends HttpPanelTextArea {

		private static final long serialVersionUID = -787753390999658000L;

		private static final Logger log = Logger.getLogger(HttpResponseHeaderPanelTextArea.class);

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
				
				matches.add(new SearchMatch(SearchMatch.Location.RESPONSE_HEAD, start, end));
			}
		}
		
		@Override
		public void highlight(SearchMatch sm) {
			if (!SearchMatch.Location.RESPONSE_HEAD.equals(sm.getLocation())) {
				return;
			}
			
			//As we replace all \r\n with \n we must subtract one character
			//for each line until the line where the selection is.
			int t = 0;
			String header = sm.getMessage().getResponseHeader().toString();
			
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
	}
}


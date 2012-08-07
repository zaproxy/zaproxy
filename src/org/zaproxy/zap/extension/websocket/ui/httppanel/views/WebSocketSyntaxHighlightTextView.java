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
package org.zaproxy.zap.extension.websocket.ui.httppanel.views;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.extension.fuzz.FuzzableComponent;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.FuzzableMessage;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.extension.websocket.ExtensionWebSocket;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.ui.httppanel.models.StringWebSocketPanelViewModel;

public class WebSocketSyntaxHighlightTextView extends HttpPanelSyntaxHighlightTextView {

	public WebSocketSyntaxHighlightTextView(StringWebSocketPanelViewModel model) {
		super(model);
	}
	
	@Override
	protected HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
		return new WebSocketSyntaxHighlightTextArea();
	}
	
	protected static class WebSocketSyntaxHighlightTextArea extends HttpPanelSyntaxHighlightTextArea implements FuzzableComponent {

        private static final long serialVersionUID = -6469629120424801024L;

        private static final String CSS = Constant.messages.getString("http.panel.view.syntaxtext.syntax.css");
        private static final String HTML = Constant.messages.getString("http.panel.view.syntaxtext.syntax.html");
        private static final String JAVASCRIPT = Constant.messages.getString("http.panel.view.syntaxtext.syntax.javascript");
        private static final String XML = Constant.messages.getString("http.panel.view.syntaxtext.syntax.xml");
        
        private static WebSocketTokenMakerFactory tokenMakerFactory = null;

		private final ExtensionWebSocket extWebSocket;
		
		public WebSocketSyntaxHighlightTextArea() {
			// Nice-2-Have: JSON support
            addSyntaxStyle(CSS, SyntaxConstants.SYNTAX_STYLE_CSS);
            addSyntaxStyle(HTML, SyntaxConstants.SYNTAX_STYLE_HTML);
            addSyntaxStyle(JAVASCRIPT, SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
            addSyntaxStyle(XML, SyntaxConstants.SYNTAX_STYLE_XML);
            
    		this.extWebSocket = (ExtensionWebSocket) Control.getSingleton().getExtensionLoader().getExtension(ExtensionWebSocket.NAME);
		}

	    @Override
	    public Class<? extends Message> getMessageClass() {
	        return WebSocketMessageDTO.class;
	    }
	    
	    @Override
	    public boolean canFuzz() {
            // Currently do not allow to fuzz if the text area is editable,
            // because the Message used is not updated with the changes.
	        if (isEditable()) {
	            return false;
	        }
	        
	        WebSocketMessageDTO message = (WebSocketMessageDTO) getMessage();
	        
	        // do not allow to fuzz if there is no active connection
	        if (!extWebSocket.isConnected(message.channel.id)) {
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
        public FuzzableMessage getFuzzableMessage() {
            return new WebSocketFuzzableTextMessage((WebSocketMessageDTO)getMessage(), getSelectionStart(), getSelectionEnd());
        }

        @Override
        public void search(Pattern p, List<SearchMatch> matches) {
            Matcher m = p.matcher(getText());
            while (m.find()) {
                matches.add(new SearchMatch(null, m.start(), m.end()));
            }
        }
        
        @Override
        public void highlight(SearchMatch sm) {
            int len = getText().length();
            if (sm.getStart() > len || sm.getEnd() > len) {
                return;
            }
            
            highlight(sm.getStart(), sm.getEnd());
        }
		
		@Override
		protected synchronized CustomTokenMakerFactory getTokenMakerFactory() {
			if (tokenMakerFactory == null) {
				tokenMakerFactory = new WebSocketTokenMakerFactory();
			}
			return tokenMakerFactory;
		}
		
		private static class WebSocketTokenMakerFactory extends CustomTokenMakerFactory {
			
			public WebSocketTokenMakerFactory() {
                String pkg = "org.fife.ui.rsyntaxtextarea.modes.";
                
                putMapping(SYNTAX_STYLE_CSS, pkg + "CSSTokenMaker");
                putMapping(SYNTAX_STYLE_HTML, pkg + "HTMLTokenMaker");
                putMapping(SYNTAX_STYLE_JAVASCRIPT, pkg + "JavaScriptTokenMaker");
                putMapping(SYNTAX_STYLE_XML, pkg + "XMLTokenMaker");
			}
		}
	}
}

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
package org.zaproxy.zap.extension.websocket.fuzz;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.websocket.WebSocketException;
import org.zaproxy.zap.extension.websocket.ui.WebSocketMessagesPopupMenuItem;
import org.zaproxy.zap.extension.websocket.ui.WebSocketPanel;

/**
 * Used in Fuzz tab, when you click on some message with right mouse button.
 * When you choose this item, then you'll be directed to the WebSockets tab,
 * showing the current message. You'll be able to view possible responses to
 * your sent message.
 */
public class ShowFuzzMessageInWebSocketsTabMenuItem extends WebSocketMessagesPopupMenuItem {
	private static final long serialVersionUID = -7781388150752101L;

	private static final Logger logger = Logger.getLogger(ShowFuzzMessageInWebSocketsTabMenuItem.class);
	
	private WebSocketPanel wsTab;
    
    public ShowFuzzMessageInWebSocketsTabMenuItem(WebSocketPanel wsTab) {
        super();
        this.wsTab = wsTab;
    }

	@Override
	protected String getMenuText() {
		return Constant.messages.getString("websocket.fuzz.show_in_tab");
	}

	@Override
	protected void performAction() {
		logger.info("Fuzzed message selected to view in WebSocketsTab");
		try {
			wsTab.showMessage(getSelectedMessageDTO());
		} catch (WebSocketException e) {
			View.getSingleton().showWarningDialog("Unable to show fuzz message in WebSockets tab!");
		}
	}
	
	@Override
	protected String getInvokerName() {
		return "fuzz.websocket.table";
	}
}

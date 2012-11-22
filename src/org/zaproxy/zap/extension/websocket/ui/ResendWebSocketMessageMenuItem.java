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
package org.zaproxy.zap.extension.websocket.ui;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.manualsend.ManualWebSocketSendEditorDialog;

/**
 * Menu Item for Popup. Used in WebSockets tab, when you click on some message
 * with right mouse button.
 */
public class ResendWebSocketMessageMenuItem extends WebSocketMessagesPopupMenuItem {
    
	private static final long serialVersionUID = 356209537052284999L;
	
	private final ManualWebSocketSendEditorDialog dialog;
    
    public ResendWebSocketMessageMenuItem(ManualWebSocketSendEditorDialog dialog) {
        super();

        this.dialog = dialog;
    }

	@Override
	protected String getMenuText() {
		return Constant.messages.getString("websocket.manual_send.resend.menu");
	}

	@Override
	protected void performAction() {
		WebSocketMessageDTO message = getSelectedMessageDTO();
		if (message == null) {
			// do nothing
			return;
		}
		
		dialog.setTitle(Constant.messages.getString("manReq.resend.popup"));
		dialog.setMessage(message);
		dialog.setVisible(true);
	}
	
	@Override
	protected String getInvokerName() {
		return WebSocketMessagesView.PANEL_NAME;
	}
	
	@Override
	public boolean isSafe() {
		return false;
	}
}

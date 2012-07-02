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
package org.zaproxy.zap.extension.websocket.brk;

import org.zaproxy.zap.extension.brk.ExtensionBreak;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.ui.WebSocketMessageDAO;
import org.zaproxy.zap.extension.websocket.ui.WebSocketTableModel;

public class WebSocketProxyListenerBreak implements WebSocketObserver {

	private ExtensionBreak extension = null;

	public WebSocketProxyListenerBreak(ExtensionBreak extension) {
	    this.extension = extension;
	}
	
    @Override
    public int getObservingOrder() {
        // Should be the last one to be notified before saving/sending the message.
        return 150;
    }


    @Override
    public boolean onMessageFrame(int channelId, WebSocketMessage message) {
        WebSocketMessage.Direction direction = message.getDirection();
        
        // Hack to create a WebSocketMessageDAO as is the "Message" that the Request/Response panel component for the WebSocket use.
        WebSocketMessageDAO dao = message.getDAO();
        
        if (direction == WebSocketMessage.Direction.OUTGOING) {
            if (extension.messageReceivedFromClient(dao)) {
                // As is the DAO that is shown and modified in the
                // Request/Response panels we must set the content to message
                // here.
                message.setReadablePayload(dao.payload);
                return true;
            }
        } else if (direction == WebSocketMessage.Direction.INCOMING) {
            if (extension.messageReceivedFromServer(dao)) {
                message.setReadablePayload(dao.payload);
                return true;
            }
        }

        return false;
    }
    
    private String byteArrayToHexString(byte[] byteArray) {
        StringBuffer hexString = new StringBuffer("");

        for (int i = 0; i < byteArray.length; i++) {
            String hexCharacter = Integer.toHexString(byteArray[i]);

            if (hexCharacter.length() < 2) {
                hexCharacter = "0" + Integer.toHexString(byteArray[i]);
            }
            
            if (i < 10) {
                hexString.append(" " + hexCharacter.toUpperCase());
            } else {
                hexString.append(" " + hexCharacter.toUpperCase() + "\n");
                i = 0;
            }
        }
        
        return hexString.toString();
    }
}

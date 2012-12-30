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

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.brk.BreakpointMessageHandler;
import org.zaproxy.zap.extension.websocket.ExtensionWebSocket;
import org.zaproxy.zap.extension.websocket.WebSocketException;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.extension.websocket.WebSocketProxy.State;
import org.zaproxy.zap.extension.websocket.db.WebSocketStorage;
import org.zaproxy.zap.extension.websocket.fuzz.WebSocketFuzzMessageDTO;

/**
 * Gets notified about WebSocket messages and checks if breakpoint applies.
 */
public class WebSocketProxyListenerBreak implements WebSocketObserver {

	private static final Logger logger = Logger.getLogger(WebSocketProxyListenerBreak.class);

	private BreakpointMessageHandler wsBrkMessageHandler;

	private ExtensionWebSocket extension;
	
	public static final int WEBSOCKET_OBSERVING_ORDER = WebSocketStorage.WEBSOCKET_OBSERVING_ORDER - 5;

	public WebSocketProxyListenerBreak(ExtensionWebSocket extension, BreakpointMessageHandler messageHandler) {
	    this.extension = extension;
	    this.wsBrkMessageHandler = messageHandler;
	}
	
    @Override
    public int getObservingOrder() {
		// should be the last one to be notified before saving/showing the
		// message
        return WEBSOCKET_OBSERVING_ORDER;
    }

    @Override
    public boolean onMessageFrame(int channelId, WebSocketMessage wsMessage) {
    	boolean continueNotifying = false;
        WebSocketMessageDTO message = wsMessage.getDTO();

		if (!extension.isSafe(message)) {
			// not safe => do not catch
			return true;
		}
		
		// message is safe => no need to set onlyIfInScope parameter to true
        
        if (message instanceof WebSocketFuzzMessageDTO) {
        	// as this message was sent by some fuzzer, do not catch it
        	continueNotifying = true;
        	return continueNotifying;
        }
    	
        if (!wsMessage.isFinished()) {
        	boolean isRequest = (wsMessage.getDirection().equals(Direction.OUTGOING));

        	// already safe => onlyIfInScope can be false
        	if (wsBrkMessageHandler.isBreakpoint(message, isRequest, false)) {
            	// prevent forwarding unfinished message when there is a breakpoint
            	// wait until all frames are received, before processing
            	// (showing/saving/etc.)
        		continueNotifying = false;
        	} else {
        		// gain performance by allowing each frame to be forwarded
        		// immediately, as this frame is not changed
        		continueNotifying = true;
        	}
        	
        	return continueNotifying;
        }

        if (message.isOutgoing) {
        	// already safe => onlyIfInScope can be false
            if (wsBrkMessageHandler.handleMessageReceivedFromClient(message, false)) {
                // As the DTO that is shown and modified in the
                // Request/Response panels we must set the content to message
                // here.
            	setPayload(wsMessage, message.payload);
            	continueNotifying = true;
            }
        } else {
        	// already safe => onlyIfInScope can be false
            if (wsBrkMessageHandler.handleMessageReceivedFromServer(message, false)) {
            	setPayload(wsMessage, message.payload);
            	continueNotifying = true;
            }
        }

        return continueNotifying;
    }

	@Override
	public void onStateChange(State state, WebSocketProxy proxy) {
		// no need to do something on state change
	}

	private void setPayload(WebSocketMessage message, Object payload) {
		try {
			if (payload instanceof String) {
				message.setReadablePayload((String) payload);
			} else if (payload instanceof byte[]) {
				message.setPayload((byte[]) payload);
			}
		} catch (WebSocketException e) {
			logger.error(e);
		}
	}
}

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

package org.zaproxy.zap.extension.websocket.manualsend;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.manualrequest.MessageSender;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.extension.websocket.WebSocketProxy.State;
import org.zaproxy.zap.extension.websocket.ui.WebSocketPanel;

/**
 * Knows how to send {@link HttpMessage} objects. Contains a list of valid
 * WebSocket channels.
 */
public class WebSocketPanelSender implements MessageSender, WebSocketObserver {

    private static final Logger logger = Logger.getLogger(WebSocketPanelSender.class);

	private Map<Integer, WebSocketProxy> connectedProxies;

    public WebSocketPanelSender() {
    	connectedProxies = new HashMap<>();
    }
    
    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.manualrequest.MessageSender#sendAndReceiveMessage()
     */
    @Override
    public void handleSendMessage(Message aMessage) throws WebServiceException {
        final WebSocketMessageDTO websocketMessage = (WebSocketMessageDTO)aMessage;
    	
        if (websocketMessage.channel == null || websocketMessage.channel.id == null) {
    		logger.warn("Invalid WebSocket channel selected. Unable to send manual crafted message!");
    		throw new WebServiceException(Constant.messages.getString("websocket.manual_send.fail.invalid_channel") 
    				+ " " + Constant.messages.getString("websocket.manual_send.fail"));
    	}
        
        if (websocketMessage.opcode == null) {
    		logger.warn("Invalid WebSocket opcode selected. Unable to send manual crafted message!");
    		throw new WebServiceException(Constant.messages.getString("websocket.manual_send.fail.invalid_opcode") 
    				+ " " + Constant.messages.getString("websocket.manual_send.fail"));
    	}
    	
        try {
        	WebSocketProxy wsProxy = getDelegate(websocketMessage.channel.id);
        	if (websocketMessage.isOutgoing == false && wsProxy.isClientMode()) {
        		logger.warn("Invalid WebSocket direction 'incoming' selected for Proxy in Client Mode. Unable to send manual crafted message!");
        		throw new WebServiceException(Constant.messages.getString("websocket.manual_send.fail.invalid_direction_client_mode") 
        				+ " " + Constant.messages.getString("websocket.manual_send.fail"));
        	}
        	wsProxy.sendAndNotify(websocketMessage);
        } catch (final IOException ioe) {
        	logger.warn(ioe.getMessage(), ioe);
            throw new WebServiceException("IO error in sending WebSocket message.");
        }
    }
    
    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.manualrequest.MessageSender#cleanup()
     */
    @Override
    public void cleanup() {
        
    }
    
    private WebSocketProxy getDelegate(Integer channelId) throws WebServiceException {
    	if (!connectedProxies.containsKey(channelId)) {
    		logger.warn("Selected WebSocket channel is not connected. Unable to send manual crafted message!");
    		throw new WebServiceException(Constant.messages.getString("websocket.manual_send.fail.disconnected_channel") 
    				+ " " + Constant.messages.getString("websocket.manual_send.fail"));
    	}
        return connectedProxies.get(channelId);
    }

	@Override
	public int getObservingOrder() {
		return WebSocketPanel.WEBSOCKET_OBSERVING_ORDER+1;
	}

	@Override
	public boolean onMessageFrame(int channelId, WebSocketMessage message) {
		return true;
	}

	@Override
	public void onStateChange(State state, WebSocketProxy proxy) {
		if (state.equals(WebSocketProxy.State.OPEN)) {
			connectedProxies.put(proxy.getChannelId(), proxy);
		} else if (state.equals(WebSocketProxy.State.CLOSING)) {
			connectedProxies.remove(proxy.getChannelId());
		}
	}    
}

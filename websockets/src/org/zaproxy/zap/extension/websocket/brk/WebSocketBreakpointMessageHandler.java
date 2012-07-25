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

import org.zaproxy.zap.extension.brk.BreakPanel;
import org.zaproxy.zap.extension.brk.BreakpointMessageHandler;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDAO;
import org.zaproxy.zap.extension.websocket.ui.OptionsParamWebSocket;

/**
 * Wraps WebSocket specific options to determine if breakpoint should be applied
 * on given {@link WebSocketMessageDAO}.
 */
public class WebSocketBreakpointMessageHandler extends BreakpointMessageHandler {

	private OptionsParamWebSocket config;

	public WebSocketBreakpointMessageHandler(BreakPanel aBreakPanel, OptionsParamWebSocket config) {
		super(aBreakPanel);
		this.config = config;
	}

	/**
	 * Only break on all requests when 'Break on all' is enabled for WebSockets.
	 * 
	 * @param aMessage
	 * @param isRequest
	 * @return
	 */
	@Override
	protected boolean isBreakOnAllRequests(Message aMessage, boolean isRequest) {
		if (super.isBreakOnAllRequests(aMessage, isRequest)) {
			return isBreakOnAllWebSocket(aMessage, isRequest);
		}
    	return false;
	}

	/**
	 * Only break on all responses when 'Break on all' is enabled for WebSockets.
	 * 
	 * @param aMessage
	 * @param isRequest
	 * @return
	 */
	@Override
	protected boolean isBreakOnAllResponses(Message aMessage, boolean isRequest) {
		if (super.isBreakOnAllResponses(aMessage, isRequest)) {
			return isBreakOnAllWebSocket(aMessage, isRequest);
		}
    	return false;
	}

	/**
	 * Only break on stepping if opcode is allowed.
	 * 
	 * @param aMessage
	 * @param isRequest
	 * @return
	 */
	@Override
	protected boolean isBreakOnStepping(Message aMessage, boolean isRequest) {
		if (aMessage instanceof WebSocketMessageDAO && super.isBreakOnStepping((Message) aMessage, isRequest)) {
			return isBreakOnOpcode(((WebSocketMessageDAO) aMessage).opcode);
		}
		return false;
	}
	
	/**
	 * Helper that determines if breakpoint should be applied for 'All
	 * Requests/Responses' on this {@link WebSocketMessageDAO}.
	 * 
	 * @param aMessage
	 * @param isRequest
	 * @return
	 */
	private boolean isBreakOnAllWebSocket(Message aMessage, boolean isRequest) {
		if (aMessage instanceof WebSocketMessageDAO && config.isBreakOnAll()) {
			return isBreakOnOpcode(((WebSocketMessageDAO) aMessage).opcode);
		}
		return false;
	}

	/**
	 * Check out if breakpoint should be applied on given
	 * {@link WebSocketMessageDAO#opcode}.
	 * 
	 * @param opcode
	 * @return
	 */
	private boolean isBreakOnOpcode(Integer opcode) {
		if (config.isBreakOnPingPong()) {
			// break on every message type
			return true;
		} else {
			// break only on non-ping/pong
			boolean isPing = opcode.equals(WebSocketMessage.OPCODE_PING);
			boolean isPong = opcode.equals(WebSocketMessage.OPCODE_PONG);
			
			if (!isPing && !isPong) {
				return true;
			}
		}
		return false;
	}
}

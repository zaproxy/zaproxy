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
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.ui.OptionsParamWebSocket;

/**
 * Wraps WebSocket specific options to determine if breakpoint should be applied
 * on given {@link WebSocketMessageDTO}.
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
	 * @return True if it breaks on all requests.
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
	 * @return True if it breaks on all responses.
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
	 * @return True if it breaks on stepping through action.
	 */
	@Override
	protected boolean isBreakOnStepping(Message aMessage, boolean isRequest) {
		if (aMessage instanceof WebSocketMessageDTO && super.isBreakOnStepping(aMessage, isRequest)) {
			return isBreakOnOpcode(((WebSocketMessageDTO) aMessage).opcode);
		}
		return false;
	}
	
	/**
	 * Helper that determines if breakpoint should be applied for 'All
	 * Requests/Responses' on this {@link WebSocketMessageDTO}.
	 * 
	 * @param aMessage
	 * @param isRequest
	 * @return True if it should break on the HTTP request/response breakpoints.
	 */
	private boolean isBreakOnAllWebSocket(Message aMessage, boolean isRequest) {
		if (aMessage instanceof WebSocketMessageDTO && config.isBreakOnAll()) {
			return isBreakOnOpcode(((WebSocketMessageDTO) aMessage).opcode);
		}
		return false;
	}

	/**
	 * Check out if breakpoint should be applied on given
	 * {@link WebSocketMessageDTO#opcode}.
	 * 
	 * @param opcode
	 * @return True if it should break on given opcode.
	 */
	private boolean isBreakOnOpcode(Integer opcode) {
		boolean shouldBreak = false;
		if (config.isBreakOnPingPong()) {
			// break on every message type
			shouldBreak = true;
		} else {
			// break only on non-ping/pong
			boolean isPing = opcode.equals(WebSocketMessage.OPCODE_PING);
			boolean isPong = opcode.equals(WebSocketMessage.OPCODE_PONG);
			
			if (!isPing && !isPong) {
				shouldBreak = true;
			}
		}
		return shouldBreak;
	}
}

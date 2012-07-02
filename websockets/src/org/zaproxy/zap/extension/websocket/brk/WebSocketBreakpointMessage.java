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

import org.zaproxy.zap.extension.brk.AbstractBreakPointMessage;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.websocket.ui.WebSocketMessageDAO;

public class WebSocketBreakpointMessage extends AbstractBreakPointMessage {

    private static final String TYPE = "WebSocket";
    
    // Define the criteria for WebSocket breakpoints.
    
    // Break on specified opcode.
	private String opcode;

	public WebSocketBreakpointMessage(String opcode) {
		this.opcode = opcode;
	}

	@Override
    public String getType() {
        return TYPE;
    }
	
	public String getOpcode() {
		return opcode;
	}

	public void setOpcode(String opcode) {
	    this.opcode = opcode;
	}

	@Override
	public boolean match(Message aMessage) {
	    if (aMessage instanceof WebSocketMessageDAO) {
	        WebSocketMessageDAO msg = (WebSocketMessageDAO)aMessage;
	        if (msg.readableOpcode.equals(opcode)) {
	            return true;
	        }
	    }
	    
		return false;
	}

    @Override
    public String getDisplayMessage() {
        return "Break on opcode: " + opcode;
    }

}

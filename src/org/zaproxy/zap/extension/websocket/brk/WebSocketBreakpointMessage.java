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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.brk.AbstractBreakPointMessage;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;

public class WebSocketBreakpointMessage extends AbstractBreakPointMessage {

    private static final String TYPE = "WebSocket";
    
    /**
     * Break on specified opcode or on all if null.
     */
	private String opcode;
	
	/**
	 * Break on specified channel id or on all channels if null.
	 */
	private Integer channelId;
	
	/**
	 * Break on specified pattern that has to match on the
	 * {@link WebSocketMessageDTO#payload} or arbitrary payload if null.
	 */
	private Pattern payloadPattern;

	/**
	 * Break on specified directions {@link Direction#INCOMING} or/and
	 * {@link Direction#OUTGOING} or on arbitrary directions if null.
	 */
	private Direction direction;

	public WebSocketBreakpointMessage(String opcode, Integer channelId, String payloadPattern, Direction direction) throws PatternSyntaxException {
		setOpcode(opcode);
		setChannelId(channelId);
		setPayloadPattern(payloadPattern);
		setDirection(direction);
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

	public Integer getChannelId() {
		return channelId;
	}

	public void setChannelId(Integer channelId) {
		this.channelId = channelId;
	}

	public String getPayloadPattern() {
		if (payloadPattern != null) {
			return payloadPattern.pattern();
		}
		return null;
	}

	/**
	 * Catch {@link PatternSyntaxException} in dialog & show warning. You can do
	 * this by <code>View.getSingleton().showWarningDialog(message)</code>.
	 * 
	 * @param payloadPattern
	 * @throws PatternSyntaxException
	 */
	public void setPayloadPattern(String payloadPattern) throws PatternSyntaxException {
		if (payloadPattern == null || payloadPattern.length() == 0) {
			this.payloadPattern = null;
		} else {
			this.payloadPattern = Pattern.compile(payloadPattern, Pattern.MULTILINE);
		}
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		if (direction == null) {
			this.direction = null;
		} else {
			this.direction = direction;
		}
	}

	@Override
	public boolean match(Message aMessage, boolean onlyIfInScope) {
	    if (aMessage instanceof WebSocketMessageDTO) {
	        WebSocketMessageDTO msg = (WebSocketMessageDTO)aMessage;
	        
	        if (opcode != null) {
		        if (!msg.readableOpcode.equals(opcode)) {
		        	return false;
		        }
	        }
	        
	        if (channelId != null) {
	        	if (!channelId.equals(msg.channel.id)) {
		        	return false;
		        }
	        }
	        
	        if (payloadPattern != null) {
	        	if (msg.payload instanceof String) {
		        	Matcher m = payloadPattern.matcher((String) msg.payload);
		        	if (!m.find()) {
		        		// when m.matches() is used, the whole string has to match
			        	return false;
			        }
	        	} else {
	        		// binary messages are not affected by pattern
	        		return false;
	        	}
	        }
	        
	        if (direction != null) {
	        	if (msg.isOutgoing && !direction.equals(Direction.OUTGOING)) {
		        	return false;
		        } else if (!msg.isOutgoing && !direction.equals(Direction.INCOMING)) {
		        	return false;
		        }
	        }
	        
	        return true;
	    }
	    
		return false;
	}

    @Override
    public String getDisplayMessage() {
    	String message = "";
    	
    	if (opcode != null) {
    		message += Constant.messages.getString("websocket.brk.add.opcode") + " " + opcode + "; ";
        }
        
        if (channelId != null) {
    		message += Constant.messages.getString("websocket.brk.add.channel") + " #" + channelId + "; ";
        }
        
        if (payloadPattern != null) {
    		message += Constant.messages.getString("websocket.brk.add.pattern") + " " + payloadPattern.pattern() + "; ";
        }
        
        if (direction != null) {
    		message += Constant.messages.getString("websocket.brk.add.direction") + " " + direction + "; ";
        }
        
        if (message.isEmpty()) {
        	return Constant.messages.getString("websocket.brk.add.break_on_all");
        }
        
        return Constant.messages.getString("websocket.brk.add.break_on_custom") + " " + message;
    }
}

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

import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;

/**
 * Used as filter for the {@link WebSocketPanel} restricting types of messages
 * shown in WebSockets tab.
 */
public class WebSocketMessagesViewFilter {

	/**
	 * Contains a sublist of {@link WebSocketMessage#OPCODES} or nothing, if not
	 * applied.
	 */
	private List<Integer> opcodeList;
	
	private Direction direction;
	
	private boolean isShowJustInScope = false;
	
	public void setOpcodes(List<Integer> list) {
		opcodeList = list;
	}
	
	/**
	 * @return Null if all opcodes are allowed
	 */
	public List<Integer> getOpcodes() {
		return opcodeList;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/**
	 * @return Null if both directions should be shown
	 */
	public Direction getDirection() {
		return direction;
	}

	public void setShowJustInScope(boolean isShowJustInScope) {
		this.isShowJustInScope = isShowJustInScope;
	}

	public boolean getShowJustInScope() {
		return isShowJustInScope;
	}

	/**
	 * Resets this filter. Message will no longer be blacklisted.
	 */
	public void reset() {
		opcodeList = null;
		direction = null;
	}
	
	/**
	 * Checks if the given entry is affected by this filter, i.e. is filtered
	 * out.
	 * 
	 * @param message
	 * @return True if the given entry is filtered out, false if valid.
	 */
	public boolean isBlacklisted(WebSocketMessageDTO message) {
		if (isShowJustInScope && !message.channel.isInScope()) {
			return true;
		}
		
		if (opcodeList != null) {
			if (!opcodeList.contains(message.opcode)) {
				return true;
			}
		}
		
		if (direction != null) {
			if (message.isOutgoing && !direction.equals(Direction.OUTGOING)) {
				return true;
			} else if (!message.isOutgoing && !direction.equals(Direction.INCOMING)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * @return short description of applied filter
	 */
	public String toShortString() {
		return toString(false);
	}
	
	/**
	 * @return description of applied filter
	 */
	public String toLongString() {
		return toString(true);
	}
	
	/**
	 * @param shouldIncludeValues
	 * @return description of the applied filters
	 */
	private String toString(boolean shouldIncludeValues) {
		StringBuilder sb = new StringBuilder();

		boolean empty = true;

		if (opcodeList != null) {
			empty = false;
			sb.append(Constant.messages.getString("websocket.filter.label.opcodes"));
			
			if (shouldIncludeValues) {
				sb.append(": ");

				for (Integer opcode : opcodeList) {
					sb.append(WebSocketMessage.opcode2string(opcode));
					sb.append(" ");
				}
			} else {
				sb.append(" ");
			}
		}

		if (direction != null) {
			empty = false;
			sb.append(Constant.messages.getString("websocket.filter.label.direction"));
			
			if (shouldIncludeValues) {
				sb.append(": ");
				sb.append(Constant.messages.getString("websocket.filter.label.direction_" + direction.toString().toLowerCase()));
				sb.append(" ");
			} else {
				sb.append(" ");
			}
		}

		sb.insert(0, " ");
		
		if (empty) {
			sb.insert(0, Constant.messages.getString("websocket.filter.label.off"));
		} else {
			sb.insert(0, Constant.messages.getString("websocket.filter.label.on"));			
		}

		sb.insert(0, " ");
		sb.insert(0, Constant.messages.getString("websocket.filter.label.filter"));
		
		return sb.toString();
	}
}

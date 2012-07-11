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

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;

/**
 * Used as filter for the {@link WebSocketPanel}, which is applied in the
 * {@link WebSocketPanelCellRenderer}.
 */
public class WebSocketModelFilter {

	/**
	 * Contains a sublist of {@link WebSocketMessage#OPCODES} or nothing, if not
	 * applied.
	 */
	private List<String> opcodeList = new ArrayList<String>();
	private Direction direction = null;
	
	/**
	 * Filter specific messages.
	 * 
	 * @param list
	 */
	public void setOpcodes(List<String> list) {
		opcodeList.clear();
		if (list != null) {
			opcodeList.addAll(list);
		}
	}

	/**
	 * Resets this filter. Message will no longer be blacklisted.
	 */
	public void reset() {
		opcodeList.clear();
	}
	
	/**
	 * Checks if the given entry is affected by this filter, i.e. is filtered
	 * out.
	 * 
	 * @param message
	 * @return True if the given entry is filtered out, false if valid.
	 */
	public boolean isBlacklisted(WebSocketMessageDAO message) {
		if (opcodeList.size() > 0) {
			if (!opcodeList.contains(message.readableOpcode)) {
				return true;
			}
		}
		
		if (direction != null) {
			if (!message.direction.equals(direction)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Returns short description of applied filter.
	 * 
	 * @return
	 */
	public String toShortString() {
		return toString(false);
	}
	
	/**
	 * Returns description of applied filter.
	 * 
	 * @return
	 */
	public String toLongString() {
		return toString(true);
	}
	
	/**
	 * Helper method that renders a description of the applied filters.
	 * 
	 * @param shouldIncludeValues
	 * @return
	 */
	private String toString(boolean shouldIncludeValues) {
		StringBuilder sb = new StringBuilder();

		boolean empty = true;

		if (opcodeList.size() > 0) {
			empty = false;
			sb.append(Constant.messages.getString("websocket.filter.label.opcodes"));
			
			if (shouldIncludeValues) {
				sb.append(": ");

				for (String opcode : opcodeList) {
					sb.append(opcode);
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

	public void setDirection(Direction direction) {
		this.direction  = direction;
	}
}

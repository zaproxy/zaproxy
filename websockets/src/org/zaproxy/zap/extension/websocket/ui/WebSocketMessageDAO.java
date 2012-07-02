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

import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;

/**
 * Data Access Object used for displaying WebSockets communication. Intended to
 * decouple user interface representation from version specific
 * {@link WebSocketMessage}.
 */
public class WebSocketMessageDAO implements Message {

	/**
	 * WebSocket channel id
	 */
	public int channelId;

	/**
	 * consecutive number
	 */
	public int id;

	/**
	 * Used for sorting, containing time in milliseconds.
	 */
	public long timestamp;

	/**
	 * When the message was finished (it might contain of several frames).
	 */
	public String dateTime;

	/**
	 * You can retrieve a textual version of this opcode via:
	 * {@link WebSocketMessage#opcode2string(int)}.
	 */
	public int opcode;

	/**
	 * Textual representation of {@link WebSocketMessageDAO#opcode}.
	 */
	public String readableOpcode;

	/**
	 * Might be either a string (readable representation for TEXT frames) OR
	 * byte[].
	 */
	public String payload;

	/**
	 * For close messages, there is always a reason.
	 */
	public int closeCode;

	/**
	 * Outgoing or incoming message.
	 */
	public WebSocketMessage.Direction direction;

	/**
	 * Number of the payload bytes.
	 */
	public int payloadLength;

	/**
	 * Useful representation for debugging purposes.
	 */
	public String toString() {
		return "Id=" + id + ";Opcode=" + readableOpcode + ";Bytes="
				+ payloadLength;
	}
}
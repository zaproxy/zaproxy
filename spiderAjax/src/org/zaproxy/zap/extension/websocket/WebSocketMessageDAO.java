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
package org.zaproxy.zap.extension.websocket;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.time.FastDateFormat;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.httppanel.Message;

/**
 * Data Access Object used for displaying WebSockets communication. Intended to
 * decouple user interface representation from version specific
 * {@link WebSocketMessage}.
 */
public class WebSocketMessageDAO implements Message {

	/**
	 * WebSocket channel id
	 */
	public Integer channelId;

	/**
	 * consecutive number
	 */
	public Integer messageId;

	/**
	 * Used for sorting, containing time of arrival in milliseconds.
	 */
	public Long timestamp;

	/**
	 * When the message was finished (it might contain of several frames).
	 */
	public String dateTime;

	/**
	 * You can retrieve a textual version of this opcode via:
	 * {@link WebSocketMessage#opcode2string(int)}.
	 */
	public Integer opcode;

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
	public Integer closeCode;

	/**
	 * Outgoing or incoming message.
	 */
	public Boolean isOutgoing;

	/**
	 * Number of the payload bytes.
	 */
	public Integer payloadLength;

	/**
	 * Temporary object holding arbitrary values.
	 */
	public volatile Object tempUserObj;

	/**
	 * Useful representation for debugging purposes.
	 */
	public String toString() {
		return "Id=" + messageId + ";Opcode=" + readableOpcode + ";Bytes="
				+ payloadLength;
	}
	
	/**
	 * Used to format {@link WebSocketMessage#timestamp} in user's locale.
	 */
	protected static final FastDateFormat dateFormatter;
	
	/**
	 * Use the static initializer for setting up one date formatter for all
	 * instances.
	 */
	static {
		// milliseconds are added later (via usage java.sql.Timestamp.getNanos())
		dateFormatter = FastDateFormat.getDateTimeInstance(
				SimpleDateFormat.SHORT, SimpleDateFormat.MEDIUM,
				Constant.getLocale());
	}

	/**
	 * Helper to set {@link WebSocketMessageDAO#timestamp} and
	 * {@link WebSocketMessageDAO#dateTime}.
	 * 
	 * @param ts
	 */
	public void setTime(Timestamp ts) {
		timestamp = ts.getTime() + (ts.getNanos() / 1000000000);
		
		synchronized (dateFormatter) {
			dateTime = dateFormatter.format(ts);
		}
		
		String nanos = (ts.getNanos() + "").replaceAll("0+$", "");
		if (nanos.length() == 0) {
			nanos = "0";
		}
		
		dateTime = dateTime.replaceFirst("([0-9]+:[0-9]+:[0-9]+)", "$1." + nanos);
	}
}
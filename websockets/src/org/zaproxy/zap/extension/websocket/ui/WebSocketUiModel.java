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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.time.FastDateFormat;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;

/**
 * This model takes an arbitrary number of {@link WebSocketMessage} instances,
 * extracts necessary information for the user interface out of it and stores
 * them in a {@link List}. TODO: Use {@link WebSocketTable}.
 */
public class WebSocketUiModel {
	
	/**
	 * This list holds all WebSocket messages for this view model.
	 */
	private List<WebSocketMessageDAO> messages;
	
	/**
	 * Add a unique id to each message of one view model.
	 */
	private AtomicInteger messageCounter;
	
	/**
	 * Used to display {@link WebSocketMessageDAO#timestamp} in user's locale.
	 */
	private static FastDateFormat dateFormatter;
	
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
	 * Data Access Object used for displaying WebSockets communication.
	 */
	public class WebSocketMessageDAO {
		/**
		 * consecutive number
		 */
		public String id;
		
		/**
		 * When the message was finished (it might contain of several frames).
		 */
		public String timestamp;
		
		/**
		 * You can retrieve a textual version of this opcode via:
		 * {@link WebSocketMessage#opcode2string(int)}.
		 */
		public String opcode;
		
		/**
		 * Might be either a string (readable representation for TEXT frames) OR byte[].
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
		public String payloadLength;
		
		/**
		 * Sets the consecutive number.
		 */
		public WebSocketMessageDAO() {
			id = "#" + messageCounter.incrementAndGet();
		}
	}
	
	public WebSocketUiModel() {
		messages = new ArrayList<WebSocketMessageDAO>();
		messageCounter = new AtomicInteger(0);
	}
	
	/**
	 * Returns all messages for this model,
	 * each with a unique id.
	 * 
	 * @return
	 */
	public List<WebSocketMessageDAO> getMessages() {
		return messages;
	}

	/**
	 * Extracts necessary data from the {@link WebSocketMessage} object and
	 * stores only necessary informations.
	 * 
	 * @param message
	 */
	public void addWebSocketMessage(WebSocketMessage message) {
		WebSocketMessageDAO dao = new WebSocketMessageDAO();
		
		String dateTime = dateFormatter.format(message.getTimestamp());
		String nanos = message.getTimestamp().getNanos() + "";
		dao.timestamp = dateTime.replaceFirst("([0-9]+:[0-9]+:[0-9]+)", "$1." + nanos.replaceAll("0+$", ""));
		
		dao.opcode = message.getOpcode() + "=" + message.getOpcodeString();
		
		if (message.isText()) {
			dao.payload = message.getReadablePayload();
		} else if (message.isBinary()) {
			dao.payload = byteArrayToHexString(message.getPayload());
		} else if (message.getOpcode() == WebSocketMessage.OPCODE_CLOSE) {
			dao.closeCode = message.getCloseCode();
		}
		
		dao.direction = message.getDirection();
		
		dao.payloadLength = message.getPayloadLength() + "";
		
		messages.add(dao);
	}
	
	private byte[] hexStringToByteArray(String hexStr) {
		int len = hexStr.length() - 1;
		byte[] data = new byte[(len / 2) + 1];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4) + Character
					.digit(hexStr.charAt(i + 1), 16));
		}
		return data;
	}
	
	private String byteArrayToHexString(byte[] byteArray) {
		StringBuffer hexString = new StringBuffer("");

		for (int i = 0; i < byteArray.length; i++) {
			String hexCharacter = Integer.toHexString(byteArray[i]);

			if (hexCharacter.length() < 2) {
				hexCharacter = "0" + Integer.toHexString(byteArray[i]);
			}
			
			if (i < 10) {
				hexString.append(" " + hexCharacter.toUpperCase());
			} else {
				hexString.append(" " + hexCharacter.toUpperCase() + "\n");
				i = 0;
			}
		}
		
		return hexString.toString();
	}


	/**
	 * Resets the messages list.
	 */
	public void clear() {
		messages.clear();
	}
}

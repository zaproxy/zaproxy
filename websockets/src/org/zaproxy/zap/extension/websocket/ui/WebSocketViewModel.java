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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;

public class WebSocketViewModel {
	
	/**
	 * This list holds all WebSocket messages for this view model.
	 */
	private List<WebSocketViewMessage> messages;
	
	/**
	 * Add a unique id to each message of one view model.
	 */
	private AtomicInteger messageCounter;
	
	/**
	 * Data Access Object used for displaying WebSockets communication.
	 */
	public class WebSocketViewMessage {
		public Timestamp timestamp;
		public int id;
		public byte[] header;
		public byte[] payload;
		public WebSocketMessage.Direction direction;
		
		public WebSocketViewMessage(byte[] header, byte[] payload, WebSocketMessage.Direction direction, Timestamp timestamp) {
			this.id = messageCounter.getAndIncrement();
			this.header = header;
			this.payload = payload;
			this.direction = direction;
			this.timestamp = timestamp;
		}
	}
	
	public WebSocketViewModel() {
		messages = new ArrayList<WebSocketViewMessage>();
		messageCounter = new AtomicInteger(0);
	}
	
	/**
	 * Returns all messages for this model,
	 * each with a unique id.
	 * 
	 * @return
	 */
	public List<WebSocketViewMessage> getMessages() {
		return messages;
	}

	/**
	 * Extracts necessary data from the {@link WebSocketMessage} object and
	 * stores only necessary informations.
	 * 
	 * @param message
	 */
	public void addWebSocketMessage(WebSocketMessage message) {
		addMessage(message.getHeader(), message.getPayload(), message.getDirection(), message.getTimestamp());
	}

	/**
	 * Helper to create one {@link WebSocketViewMessage}.
	 * 
	 * @param header
	 * @param payload
	 * @param direction
	 */
	private void addMessage(byte[] header, byte[] payload, Direction direction, Timestamp timestamp) {
		messages.add(new WebSocketViewMessage(header, payload, direction, timestamp));
	}

	/**
	 * Resets the messages list.
	 */
	public void clear() {
		messages.clear();
	}
}

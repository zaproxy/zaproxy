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
package org.zaproxy.zap.extension.websocket.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.extension.websocket.WebSocketProxy.State;

/**
 * Listens to all WebSocket messages and utilizes {@link TableWebSocket} to
 * store messages in database.
 */
public class WebSocketStorage implements WebSocketObserver {

	private static final Logger logger = Logger
			.getLogger(WebSocketStorage.class);

	// determines when messages are stored in databases
	public static final int WEBSOCKET_OBSERVING_ORDER = 100;

	private TableWebSocket table;

	public WebSocketStorage(TableWebSocket table) {
		this.table = table;
	}

	@Override
	public int getObservingOrder() {
		return WEBSOCKET_OBSERVING_ORDER;
	}

	@Override
	public boolean onMessageFrame(int channelId, WebSocketMessage wsMessage) {
		if (wsMessage.isFinished()) {
			WebSocketMessageDTO message = wsMessage.getDTO();

			try {
				table.insertMessage(message);
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}

		// forward message frame to other observers and then send through
		return true;
	}

	public TableWebSocket getTable() {
		return table;
	}
	
	public void setTable(TableWebSocket table) {
		this.table = table;
	}

	@Override
	public void onStateChange(State state, WebSocketProxy proxy) {
		if (state.equals(State.OPEN) || state.equals(State.CLOSED) || state.equals(State.INCLUDED)) {
			try {
				if (table != null) {
					table.insertOrUpdateChannel(proxy.getDTO());
				} else if (!state.equals(State.CLOSED)) {
					logger.warn("Could not update state of WebSocket channel to '" + state.toString() + "'!");
				}
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		} else if (state.equals(State.EXCLUDED)) {
			// when proxy is excluded from ZAP, then messages are forwarded
			// but not stored - all existing communication is deleted
            try {
				table.purgeChannel(proxy.getChannelId());
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}

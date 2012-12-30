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
package org.zaproxy.zap.extension.websocket.filter;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.extension.websocket.WebSocketProxy.State;

/**
 * Keeps track of all {@link WebSocketFilter} instances and informs them on
 * message arrival.
 */
public class WebSocketFilterListener implements WebSocketObserver {

	private static final Logger logger = Logger.getLogger(WebSocketFilterListener.class);
	
	public final int WEBSOCKET_OBSERVING_ORDER = 0;

	private Vector<WebSocketFilter> wsFilter;
	
	public WebSocketFilterListener() {
		wsFilter = new Vector<>();
	}
	
	public void addFilter(WebSocketFilter filter) {
		wsFilter.add(filter);
	}

	@Override
	public int getObservingOrder() {
		return WEBSOCKET_OBSERVING_ORDER;
	}

	@Override
	public boolean onMessageFrame(int channelId, WebSocketMessage message) {
		for (WebSocketFilter filter : wsFilter) {
			try {
				if (filter.isEnabled()) {
					filter.onWebSocketPayload(message);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return true;
	}

	@Override
	public void onStateChange(State state, WebSocketProxy proxy) {
		// do nothing
	}

	public void reset() {
		for (WebSocketFilter filter : wsFilter) {
			filter.reset();
		}
	}
}

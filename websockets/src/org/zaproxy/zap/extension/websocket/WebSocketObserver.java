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

import org.zaproxy.zap.extension.websocket.WebSocketProxy.State;

/**
 * Provides a callback mechanism to get notified of WebSocket messages.
 * You can add your observer via {@link WebSocketProxy#addObserver(WebSocketObserver)}.
 */
public interface WebSocketObserver {

	/**
	 * The lowest ordering value will receive the message first.
	 * 
	 * @return
	 */
	public int getObservingOrder();
	
	/**
	 * Called by the observed class ({@link WebSocketProxy}) when a new part of
	 * a message arrives. Use {@link WebSocketMessage#isFinished()} to determine
	 * if it is ready to process. If false is returned, the given message part will
	 * not be further processed (i.e. forwarded).
	 * 
	 * @param channelId
	 * @param message
	 * @return
	 */
	public boolean onMessageFrame(int channelId, WebSocketMessage message);
	
	/**
	 * Called by the observed class ({@link WebSocketProxy}) when its internal
	 * {@link WebSocketProxy#state} changes.
	 * 
	 * @param state
	 * @param proxy
	 */
	public void onStateChange(State state, WebSocketProxy proxy);
}

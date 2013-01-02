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

import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.websocket.WebSocketProxy.State;

/**
 * Provides a callback mechanism to get notified of WebSocket messages.
 * <p>
 * You can add your observer to a specific channel via
 * {@link WebSocketProxy#addObserver(WebSocketObserver)}. Alternatively you can
 * set up your observer for all channels, that come into existence in the
 * future. Call either
 * {@link ExtensionWebSocket#addAllChannelObserver(WebSocketObserver)} direct or
 * use {@link ExtensionHook#addWebSocketObserver(WebSocketObserver)}.
 * </p>
 */
public interface WebSocketObserver {

	/**
	 * The observer with the lowest ordering value will receive the message
	 * first.
	 * 
	 * @return observing order
	 */
	int getObservingOrder();
	
	/**
	 * Called by the observed class ({@link WebSocketProxy}) when a new part of
	 * a message arrives.
	 * <p>
	 * Use {@link WebSocketMessage#isFinished()} to determine if it is ready to
	 * process. If false is returned, the given message part will not be further
	 * processed (i.e. forwarded).
	 * 
	 * @param channelId
	 * @param message
	 *            contains message parts received so far
	 * @return True for continuing to notify and forwarding message
	 */
	boolean onMessageFrame(int channelId, WebSocketMessage message);
	
	/**
	 * Called by the observed class ({@link WebSocketProxy}) when its internal
	 * {@link WebSocketProxy#state} changes.
	 * <p>
	 * This state does not only represent all possible WebSocket connection
	 * states, but also state changes that affect how messages are processed.
	 * 
	 * @param state new state
	 * @param proxy
	 */
	void onStateChange(State state, WebSocketProxy proxy);
}

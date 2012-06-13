/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
	 * This method will be called by the observed class ({@link WebSocketProxy}
	 * ). If it returns false, the given message will not be further processed
	 * (i.e. forwarded).
	 * 
	 * @param message The given message might not be finished so far.
	 * @return False if it shouldn't be passed to further observers, nor forwarded.
	 */
	public boolean onMessageFrame(WebSocketMessage message);
}

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

import org.parosproxy.paros.extension.filter.Filter;
import org.zaproxy.zap.extension.websocket.WebSocketException;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;

/**
 * WebSocket specific {@link Filter} instance, that is called on message
 * arrival.
 */
public interface WebSocketFilter extends Filter {

    /**
     * Filter WebSocket communication.
     * 
     * @param message
     * @throws WebSocketException 
     */
    void onWebSocketPayload(WebSocketMessage message) throws WebSocketException;
    
    /**
     * Clean up dialogues or other settings. Call it on session change.
     */
    void reset();
}

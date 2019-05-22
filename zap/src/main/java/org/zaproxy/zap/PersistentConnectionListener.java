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
package org.zaproxy.zap;

import java.net.Socket;

import org.parosproxy.paros.core.proxy.ArrangeableProxyListener;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Proxy listener that targets long living connections, such as WebSockets or
 * Server-Sent Events.
 */
public interface PersistentConnectionListener extends ArrangeableProxyListener {
	
	/**
	 * Allows to keep the connection open and perform advanced operations
	 * on the Socket. Consider WebSocket connections or Server-Sent Events.
	 * 
	 * @param httpMessage Contains request and response.
	 * @param inSocket Contains TCP connection to browser.
	 * @param method May contain TCP connection to server.
	 * 
	 * @return True if connection is took over, false if not interested.
	 */
	boolean onHandshakeResponse(HttpMessage httpMessage, Socket inSocket, ZapGetMethod method);
}

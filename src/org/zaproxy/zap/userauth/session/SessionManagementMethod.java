/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.userauth.session;

import java.util.List;

import org.parosproxy.paros.network.HttpMessage;

/**
 * The {@link SessionManagementMethod} represents a session management method that can be used to
 * manage an existing Web Session corresponding to an entity (user) interacting with a particular
 * WebApplication.
 */
public interface SessionManagementMethod {

	/**
	 * Checks if the session management method is fully configured.
	 * 
	 * @return true, if is configured
	 */
	public boolean isConfigured();

	/**
	 * Clones the current session management method, creating a deep-copy of it.
	 * 
	 * @return a deep copy of the session management method
	 */
	public SessionManagementMethod duplicate();

	/**
	 * Identifies the web session, from a list of existing sessions, which matches the given
	 * HttpMesage. The callers of this method <b>must</b> make sure that the provided list contains
	 * sessions that are the same type and are adequate.
	 * 
	 * @param sessions the list of existing sessions for this Session Management method.
	 * @param msg the http message
	 * @return the matching Web Session, if any, or {@code null} otherwise
	 * @throws UnsupportedWebSessionException if the web session type is unsupported
	 */
	public WebSession identifyMatchingWebSession(List<WebSession> sessions, HttpMessage msg)
			throws UnsupportedWebSessionException;

	/**
	 * Extracts the web session information from a Http Message, creating a {@link WebSession}
	 * object corresponding to the Session Management Method.
	 * <p>
	 * This method should not store the extracted web session. Future calls to
	 * {@link SessionManagementMethod#setWebSession(WebSession)} will be made.
	 * </p>
	 * 
	 * @param msg the msg
	 * @return the web session
	 */
	public WebSession extractWebSession(HttpMessage msg);

	/**
	 * Clears any tokens or elements that can link the HttpMessage provided as a parameter to a
	 * WebSession.
	 * 
	 * @param msg the http message
	 */
	public void clearWebSessionIdentifiers(HttpMessage msg);

	/**
	 * Modifies a message so its Request Header/Body matches the given web session, according to
	 * this session management method.
	 * 
	 * @param message the message
	 * @param session the session
	 * @throws UnsupportedWebSessionException if the web session type is unsupported
	 */
	public void processMessageToMatchSession(HttpMessage message, WebSession session)
			throws UnsupportedWebSessionException;

	/**
	 * Thrown when an unsupported type of web session is used with a {@link SessionManagementMethod}
	 * .
	 */
	public class UnsupportedWebSessionException extends RuntimeException {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 4802501809913124766L;

		public UnsupportedWebSessionException(String message) {
			super(message);
		}
	}
}

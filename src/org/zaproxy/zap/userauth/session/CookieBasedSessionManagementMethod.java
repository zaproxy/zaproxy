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

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httpsessions.HttpSession;

/**
 * The implementation for a {@link SessionManagementMethod} that for web applications that use
 * cookies for session management.
 */
public class CookieBasedSessionManagementMethod implements SessionManagementMethod {

	private static final Logger log = Logger.getLogger(CookieBasedSessionManagementMethod.class);

	/** The session. */
	private HttpSession session;

	private static final String NAME = Constant.messages.getString("userauth.session.cb.name");

	@Override
	public String toString() {
		return NAME;
	}

	/**
	 * Sets the session.
	 * 
	 * @param session the new session
	 */
	public void setSession(HttpSession session) {
		this.session = session;
	}

	/**
	 * A factory for creating CookieBasedSessionManagementMethod objects.
	 */
	public static class CookieBasedSessionManagementMethodFactory extends
			SessionManagementMethodFactory<CookieBasedSessionManagementMethod> {

		/** The Constant METHOD_NAME. */
		private static final String METHOD_NAME = Constant.messages.getString("userauth.session.cb.name");

		@Override
		public CookieBasedSessionManagementMethod buildSessionManagementMethod() {
			return new CookieBasedSessionManagementMethod();
		}

		@Override
		public String getName() {
			return METHOD_NAME;
		}

		@Override
		public AbstractSessionManagementMethodOptionsPanel<CookieBasedSessionManagementMethod> buildOptionsPanel(
				CookieBasedSessionManagementMethod existingMethod, int contextId) {
			// No need for a configuration panel yet
			return null;
		}

		@Override
		public boolean hasOptionsPanel() {
			return false;
		}

		@Override
		public boolean isFactoryForMethod(Class<? extends SessionManagementMethod> methodClass) {
			return CookieBasedSessionManagementMethod.class == methodClass;
		}

	}

	@Override
	public boolean isConfigured() {
		// Always configured
		return true;
	}

	@Override
	public String getStatusDescription() {
		// No options panel, so no need for status description
		return "";
	}

	@Override
	public WebSession extractWebSession(HttpMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWebSession(WebSession session) throws UnsupportedWebSessionException {
		if (!(session instanceof HttpSession))
			throw new UnsupportedWebSessionException(
					"The WebSession type provided is unsupported. Cookie based session management only supports "
							+ HttpSession.class + " type of WebSession.");
		this.session = (HttpSession) session;
	}

	@Override
	public void processMessageToMatchSession(HttpMessage message) {
		if (message.getHttpSession() != session) {
			if (log.isDebugEnabled())
				log.debug("Modifying message to match User session: " + session);
			CookieBasedSessionManagementHelper.processMessageToMatchSession(message, session);
		}
	}

	@Override
	public boolean isAuthenticated() {
		return session!=null;
	}

}

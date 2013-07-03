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

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.httpsessions.HttpSession;

/**
 * The implementation for a {@link SessionManagementMethod} that for web applications that use
 * cookies for session management.
 */
public class CookieBasedSessionManagementMethod implements SessionManagementMethod {

	/** The session. */
	private HttpSession session;

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

		/* (non-Javadoc)
		 * @see org.zaproxy.zap.userauth.session.SessionManagementMethodFactory#buildSessionManagementMethod()
		 */
		@Override
		public CookieBasedSessionManagementMethod buildSessionManagementMethod() {
			return new CookieBasedSessionManagementMethod();
		}

		/* (non-Javadoc)
		 * @see org.zaproxy.zap.userauth.session.SessionManagementMethodFactory#getName()
		 */
		@Override
		public String getName() {
			return METHOD_NAME;
		}

		/* (non-Javadoc)
		 * @see org.zaproxy.zap.userauth.session.SessionManagementMethodFactory#buildOptionsPanel(int)
		 */
		@Override
		public AbstractSessionManagementMethodOptionsPanel<CookieBasedSessionManagementMethod> buildOptionsPanel(
				int contextId) {
			// No need for a configuration panel yet
			return null;
		}

	}

}

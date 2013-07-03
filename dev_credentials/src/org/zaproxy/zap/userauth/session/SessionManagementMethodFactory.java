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


/**
 * A factory for creating {@link SessionManagementMethod} objects.<br/>
 * <br/>
 * The implementors of new Session Management Methods should also implement a corresponding factory.
 * The system automatically detects and loads Sesssion Management Factories and, through them, the
 * corresponding session management methods.
 * 
 * @param <T> the type of the authentication method
 */
public abstract class SessionManagementMethodFactory<T extends SessionManagementMethod> {

	/**
	 * Builds an implementation for a session management method.
	 * 
	 * @return the session management method
	 */
	public abstract T buildSessionManagementMethod();

	/**
	 * Gets the name of the session management method.
	 * 
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * Builds the options panel that can be used to fully configure a session management method.
	 * 
	 * @param contextId the context id
	 * @return the abstract session method options panel
	 */
	public abstract AbstractSessionManagementMethodOptionsPanel<T> buildOptionsPanel(int contextId);

	@Override
	public String toString() {
		return getName();
	}
}

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
 * A type of session management method. This class also acts as a factory for creating
 * {@link SessionManagementMethod} objects.
 * <p>
 * The implementors of new Session Management Methods should also implement a corresponding type.
 * The system automatically detects and loads {@link SessionManagementMethodType} classes and,
 * through them, the corresponding session management methods.
 * </p>
 * 
 * @param <T> the corresponding session management method
 */
public abstract class SessionManagementMethodType<T extends SessionManagementMethod> {

	/**
	 * Builds a new, empty, session management method. The session mangement method should then be
	 * configured through its corresponding Options panel.
	 * 
	 * @param contextId the context id
	 * @return the session management method
	 * @see SessionManagementMethodType#buildOptionsPanel(SessionManagementMethod, int)
	 */
	public abstract SessionManagementMethod createSessionManagementMethod(int contextId);

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
	 * @see SessionManagementMethodType#hasOptionsPanel
	 */
	public abstract AbstractSessionManagementMethodOptionsPanel<T> buildOptionsPanel(T existingMethod,
			int contextId);

	/**
	 * Checks if the corresponding {@link SessionManagementMethod} has an options panel that can be
	 * used for configuration.
	 * 
	 * @see SessionManagementMethodType#buildOptionsPanel
	 * 
	 * @return true, if successful
	 */
	public abstract boolean hasOptionsPanel();

	/**
	 * Checks if is this is a factory for the Session Management Method provided as parameter.
	 * 
	 * @param methodClass the method class
	 * @return true, if is factory for method
	 */
	public abstract boolean isFactoryForMethod(Class<? extends SessionManagementMethod> methodClass);

	@Override
	public String toString() {
		return getName();
	}
}

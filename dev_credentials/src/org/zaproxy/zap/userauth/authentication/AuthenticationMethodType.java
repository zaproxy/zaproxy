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
package org.zaproxy.zap.userauth.authentication;

import org.zaproxy.zap.model.Context;

/**
 * A type of authentication method. This class also acts as a factory for creating
 * {@link AuthenticationMethod} objects and for creating the options panels used for configuring
 * both the {@link AuthenticationMethod} and the {@link AuthenticationCredentials}.
 * <p>
 * The implementors of new authentication methods should also implement a corresponding
 * {@link AuthenticationMethodType}. The system automatically detects and loads Authentication
 * Method Types and, through them, the corresponding Authentication methods.
 * </p>
 * 
 * @param <T> the type of the authentication method
 */
public abstract class AuthenticationMethodType<T extends AuthenticationMethod<T>> {

	/**
	 * Builds a new, empty, authentication method. The authentication method should then be
	 * configured through the Options panel.
	 * 
	 * @return the authentication method
	 */
	public abstract AuthenticationMethod<T> createAuthenticationMethod(int contextId);

	/**
	 * Gets the name of the authentication method.
	 * 
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * Builds the options panel that can be used to fully configure the authentication method.
	 *
	 * @param authenticationMethod the authentication method to be configured by the panel
	 * @param uiSharedContext the shared context on which the panel should work
	 * @return the abstract authentication method options panel
	 * @see AuthenticationMethodType#hasOptionsPanel()
	 */
	public abstract AbstractAuthenticationMethodOptionsPanel<T> buildOptionsPanel(T authenticationMethod,
			Context uiSharedContext);

	/**
	 * Checks if the corresponding {@link AuthenticationMethod} has an options panel that can be
	 * used for configuration.
	 * 
	 * @see AuthenticationMethodFactory#buildOptionsPanel(int);
	 * 
	 * @return true, if it needs one
	 */
	public abstract boolean hasOptionsPanel();

	/**
	 * Builds the options panel that can be used to fully configure an.
	 *
	 * @param credentials the credentials
	 * @param uiSharedContext the shared context on which the panel should work
	 * @return the abstract credentials options panel
	 * {@link AuthenticationCredentials} object.
	 */
	public abstract AbstractCredentialsOptionsPanel<? extends AuthenticationCredentials> buildCredentialsOptionsPanel(
			AuthenticationCredentials credentials, Context uiSharedContext);

	/**
	 * Checks if the corresponding {@link AuthenticationCredentials} has an options panel that can
	 * be used for its configuration.
	 * 
	 * @return true, if it needs one
	 */
	public abstract boolean hasCredentialsOptionsPanel();

	/**
	 * Checks if is this is a factory for the Authentication Method provided as parameter.
	 * 
	 * @param methodClass the method class
	 * @return true, if is factory for method
	 */
	public abstract boolean isFactoryForMethod(Class<? extends AuthenticationMethod<?>> methodClass);

	@Override
	public String toString() {
		return getName();
	}

}

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

import javax.swing.JPanel;

import org.zaproxy.zap.userauth.authentication.AuthenticationMethodType.UnsupportedAuthenticationMethodException;

/**
 * An Options Panel that is used to configure all the settings corresponding to an
 * {@link AuthenticationMethod}.
 * 
 * <p>
 * This panel will be displayed to users in a separate dialog.
 * </p>
 * 
 * @param <T> the authentication method type
 */
public abstract class AbstractAuthenticationMethodOptionsPanel<T extends AuthenticationMethod<T>> extends
		JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9003182467823059637L;

	/** The method. */
	protected T authenticationMethod;

	/**
	 * Validate the fields.
	 * 
	 * @return true, if successful
	 */
	public abstract boolean validateFields();

	/**
	 * Save the changes from the panel in the authentication method. After this method call, calls
	 * to {@link AbstractAuthenticationMethodOptionsPanel#getMethod()} should return the
	 * {@link AuthenticationMethod} with the saved changes.
	 */
	public abstract void saveMethod();

	/**
	 * Binds (loads) data from an existing Authentication method in the panel. After this method, to
	 * {@link AbstractAuthenticationMethodOptionsPanel#getMethod()} should return the same object,
	 * eventually with some changes (if
	 * {@link AbstractAuthenticationMethodOptionsPanel#saveMethod()} was called).
	 * 
	 * @param method the method to be loaded/shown in the panel.
	 */
	public abstract void bindMethod(AuthenticationMethod<T> method)
			throws UnsupportedAuthenticationMethodException;

	/**
	 * Gets the corresponding authentication method.
	 * 
	 * @return the method
	 */
	public T getMethod() {
		return authenticationMethod;
	}

}

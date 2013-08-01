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

import org.zaproxy.zap.userauth.session.SessionManagementMethod;
import org.zaproxy.zap.userauth.session.WebSession;

/**
 * The AuthenticationMethod represents an authentication method that can be used to authenticate an
 * entity in a particular WebApplication.
 */
public interface AuthenticationMethod<SELF extends AuthenticationMethod<SELF>> {

	/**
	 * Checks if the authentication method is fully configured.
	 * 
	 * @return true, if is configured
	 */
	public boolean isConfigured();

	/**
	 * Creates a new, empty, Authentication Credentials object corresponding to this type of
	 * Authentication method.
	 *
	 * @return the authentication credentials
	 */
	public AuthenticationCredentials createAuthenticationCredentials();

	/**
	 * Performs an authentication in a web application, returning an authenticated
	 * {@link WebSession}.
	 * 
	 * @param sessionManagementMethod the set up session management method is provided so it can be
	 *            used, if needed, to automatically extract session information from Http Messages.
	 * @param credentials the credentials
	 * @return an authenticated web session
	 * 
	 */
	public WebSession authenticate(SessionManagementMethod sessionManagementMethod,
			AuthenticationCredentials credentials) throws UnsupportedAuthenticationCredentialsException;

	/**
	 * Thrown when an unsupported type of credentials is used with a {@link AuthenticationMethod} .
	 */
	public class UnsupportedAuthenticationCredentialsException extends RuntimeException {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 4802501809913124766L;

		public UnsupportedAuthenticationCredentialsException(String message) {
			super(message);
		}
	}

}

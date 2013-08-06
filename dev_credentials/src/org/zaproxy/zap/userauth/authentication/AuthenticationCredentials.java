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


/**
 * The Credentials is an entity, corresponding to an Authentication Method, that has all the
 * information required to create a new authenticated WebSession in a web application , for one of
 * the web application's 'users'.
 * <p>
 * For example, in the case of form based authentication, the AuthenticationMethod 'knows' which is
 * the login URL, which are the names of the fields it has to submit for user and password, while
 * the Authenticator is an 'instance' corresponding to an user, which knows it's password and
 * username.
 * </p>
 * 
 * @param <T> the type of the corresponding authentication method
 */
public interface AuthenticationCredentials {

	/**
	 * Checks if the credentials object is fully configured.
	 * 
	 * @return true, if is configured
	 */
	public boolean isConfigured();

	// /**
	// * Gets a string describing the status (the configuration) of the authenticator.
	// *
	// * @return the status description
	// */
	// public String getStatusDescription();

}

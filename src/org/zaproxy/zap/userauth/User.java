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
package org.zaproxy.zap.userauth;

import java.util.List;

import org.zaproxy.zap.userauth.authentication.AuthenticationMethod;
import org.zaproxy.zap.userauth.session.SessionManagementMethod;
import org.zaproxy.zap.utils.Enableable;

/**
 * ZAP representation of a web application user.
 */
public class User extends Enableable {

	public SessionManagementMethod getSessionManagementMethod() {
		return sessionManagementMethod;
	}

	/** The name. */
	private String name;

	/** The corresponding context id. */
	private int contextId;

	/** The authentication method. */
	private AuthenticationMethod authenticationMethod;

	/** The session management method. */
	private SessionManagementMethod sessionManagementMethod;

	/** The roles corresponding to this user. */
	private List<Role> roles;

	public User(int contextId, String name) {
		super();
		this.contextId = contextId;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public AuthenticationMethod getAuthenticationMethod() {
		return authenticationMethod;
	}

	public void setSessionManagementMethod(SessionManagementMethod sessionManagementMethod) {
		this.sessionManagementMethod = sessionManagementMethod;
	}

	public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
		this.authenticationMethod = authenticationMethod;
	}

}

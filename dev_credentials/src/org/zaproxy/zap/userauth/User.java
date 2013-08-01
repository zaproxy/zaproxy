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

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.userauth.authentication.AuthenticationCredentials;
import org.zaproxy.zap.utils.Enableable;

/**
 * ZAP representation of a web application user.
 */
public class User extends Enableable {

	private static final Logger log = Logger.getLogger(User.class);

	/** The name. */
	private String name;

	/** The corresponding context id. */
	private int contextId;

	/** The roles corresponding to this user. */
	private List<Role> roles;
	
	/** The authentication credentials that can be used for configuring the user. */
	private AuthenticationCredentials authenticationCredentials;

	public User(int contextId, String name) {
		super();
		this.contextId = contextId;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", contextId=" + contextId + ", enabled=" + isEnabled() + "]";
	}

	/**
	 * Modifies a message so its Request Header/Body matches the web session corresponding to this
	 * user.
	 * 
	 * @param message the message
	 */
	public void processMessageToMatchUser(HttpMessage message) {
		// // If the user is not yet authenticated, authenticate now
		// if (!this.sessionManagementMethod.isAuthenticated()) {
		// log.info("Authenticating user: " + this.name);
		// WebSession newSession = this.authenticationMethod.authenticate();
		// this.sessionManagementMethod.setWebSession(newSession);
		// }
		// // Modify the message accordingly
		// this.sessionManagementMethod.processMessageToMatchSession(message);
	}
}

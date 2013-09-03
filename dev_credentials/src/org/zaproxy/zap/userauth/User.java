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
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.authentication.AuthenticationCredentials;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethod;
import org.zaproxy.zap.userauth.session.SessionManagementMethod;
import org.zaproxy.zap.userauth.session.WebSession;
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

	/** The authenticated session. */
	private WebSession authenticatedSession;

	/** The authentication credentials that can be used for configuring the user. */
	private AuthenticationCredentials authenticationCredentials;

	/** The context. */
	private Context context;

	public User(int contextId, String name) {
		super();
		this.contextId = contextId;
		this.name = name;
	}

	/**
	 * Gets the name of the user.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", contextId=" + contextId + ", enabled=" + isEnabled() + "]";
	}

	/**
	 * Gets the context to which this user corresponds.
	 * 
	 * @return the context
	 */
	private Context getContext() {
		if (context == null) {
			context = Model.getSingleton().getSession().getContext(this.contextId);
		}
		return context;
	}

	/**
	 * Modifies a message so its Request Header/Body matches the web session corresponding to this
	 * user.
	 * 
	 * @param message the message
	 */
	public void processMessageToMatchUser(HttpMessage message) {
		// If the user is not yet authenticated, authenticate now
		// Make sure there are no simultaneous authentications for the same user
		synchronized (this) {
			if (!this.hasAuthenticatedSession()) {
				this.authenticate();
				if (!this.hasAuthenticatedSession()) {
					log.info("Authentication failed for user: " + name);
					return;
				}
			}
		}

		// Modify the message accordingly
		getContext().getSessionManagementMethod().processMessageToMatchSession(message, authenticatedSession);
	}

	/**
	 * Gets the configured authentication credentials of this user.
	 * 
	 * @return the authentication credentials
	 */
	public AuthenticationCredentials getAuthenticationCredentials() {
		return authenticationCredentials;
	}

	/**
	 * Sets the authentication credentials for the user. These will be used to authenticate the
	 * user, if necessary.
	 * 
	 * @param authenticationCredentials the new authentication credentials
	 */
	public void setAuthenticationCredentials(AuthenticationCredentials authenticationCredentials) {
		this.authenticationCredentials = authenticationCredentials;
	}

	/**
	 * Checks if the user has a corresponding authenticated session
	 * 
	 * @return true, if is authenticated
	 */
	public boolean hasAuthenticatedSession() {
		return authenticatedSession != null;
	}

	/**
	 * Resets the existing authenticated session, causing subsequent calls to
	 * {@link #processMessageToMatchUser(HttpMessage)} to reauthenticate.
	 */
	public void resetAuthenticatedSession() {
		authenticatedSession = null;
	}

	/**
	 * Checks if the response received by the Http Message corresponds to this user.
	 * 
	 * @param msg the msg
	 * @return true, if is authenticated
	 */
	public boolean isAuthenticated(HttpMessage msg) {
		return getContext().getAuthenticationMethod().isAuthenticated(msg);
	}

	/**
	 * Authenticates the user, using its authentication credentials and the authentication method
	 * corresponding to its Context.
	 * 
	 * @see SessionManagementMethod
	 * @see AuthenticationMethod
	 * @see Context
	 */
	public void authenticate() {
		log.info("Authenticating user: " + this.name);
		this.authenticatedSession = getContext().getAuthenticationMethod().authenticate(
				getContext().getSessionManagementMethod(), this.authenticationCredentials);
	}

}

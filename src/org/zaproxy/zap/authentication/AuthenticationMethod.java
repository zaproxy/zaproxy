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
package org.zaproxy.zap.authentication;

import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.Stats;

/**
 * The {@code AuthenticationMethod} represents an authentication method that can be used to authenticate an
 * entity in a particular web application.
 */
public abstract class AuthenticationMethod {

	public static final String CONTEXT_CONFIG_AUTH = Context.CONTEXT_CONFIG + ".authentication";
	public static final String CONTEXT_CONFIG_AUTH_TYPE = CONTEXT_CONFIG_AUTH + ".type";
	public static final String CONTEXT_CONFIG_AUTH_LOGGEDIN = CONTEXT_CONFIG_AUTH + ".loggedin";
	public static final String CONTEXT_CONFIG_AUTH_LOGGEDOUT = CONTEXT_CONFIG_AUTH + ".loggedout";

	public static final String AUTH_STATE_LOGGED_IN_STATS = "stats.auth.state.loggedin";
	public static final String AUTH_STATE_LOGGED_OUT_STATS = "stats.auth.state.loggedout";
	public static final String AUTH_STATE_NO_INDICATOR_STATS = "stats.auth.state.noindicator";
	public static final String AUTH_STATE_UNKNOWN_STATS = "stats.auth.state.unknown";

	/**
	 * Checks if the authentication method is fully configured.
	 * 
	 * @return true, if is configured
	 */
	public abstract boolean isConfigured();

	/**
	 * Clones the current authentication method, creating a deep-copy of it.
	 * 
	 * @return a deep copy of the authentication method
	 */
	@Override
	public AuthenticationMethod clone() {
		AuthenticationMethod method = duplicate();
		method.loggedInIndicatorPattern = this.loggedInIndicatorPattern;
		method.loggedOutIndicatorPattern = this.loggedOutIndicatorPattern;
		return method;
	}

	/**
	 * Internal method for cloning the current authentication method, creating a deep-copy of it.
	 * 
	 * @return a deep copy of the authentication method
	 */
	protected abstract AuthenticationMethod duplicate();

	/**
	 * Validates that the creation of authentication credentials is possible, returning {@code true} if it is, {@code false}
	 * otherwise.
	 * <p>
	 * If view is enabled the user should be informed that it's not possible to create authentication credentials.
	 * <p>
	 * Default implementation returns, always, {@code true}.
	 * 
	 * @return {@code true} if the creation of authentication credentials is possible, {@code false} otherwise
	 * @see #createAuthenticationCredentials()
	 * @since 2.4.3
	 */
	public boolean validateCreationOfAuthenticationCredentials() {
		return true;
	}

	/**
	 * Creates a new, empty, Authentication Credentials object corresponding to this type of
	 * Authentication method.
	 * 
	 * @return the authentication credentials
	 * @see #validateCreationOfAuthenticationCredentials()
	 */
	public abstract AuthenticationCredentials createAuthenticationCredentials();

	/**
	 * Gets the {@link AuthenticationMethodType} corresponding to this authentication method.
	 * <p>
	 * Implementations may return new instantiations at every call, so performance considerations
	 * should be taken by users.
	 * </p>
	 * 
	 * @return the type
	 */
	public abstract AuthenticationMethodType getType();

	/**
	 * Performs an authentication in a web application, returning an authenticated.
	 * 
	 * @param sessionManagementMethod the set up session management method is provided so it can be
	 *            used, if needed, to automatically extract session information from Http Messages.
	 * @param credentials the credentials
	 * @param user the user to authenticate
	 * @return an authenticated web session
	 * @throws UnsupportedAuthenticationCredentialsException the unsupported authentication
	 *             credentials exception {@link WebSession}.
	 */
	public abstract WebSession authenticate(SessionManagementMethod sessionManagementMethod,
			AuthenticationCredentials credentials, User user)
			throws UnsupportedAuthenticationCredentialsException;

	/**
	 * Gets an api response that represents the Authentication Method.
	 * 
	 * @return the api response representation
	 */
	public abstract ApiResponse getApiResponseRepresentation();

	/**
	 * Called when the Authentication Method is persisted/saved in a Context. For example, in this
	 * method, UI elements can be marked accordingly.Description
	 */
	public void onMethodPersisted() {

	}

	/**
	 * Called when the Authentication Method is discarded from/not used in a Context. For example,
	 * in this method, UI elements can be (un)marked accordingly.
	 */
	public void onMethodDiscarded() {

	}

	/** The logged in indicator pattern. */
	protected Pattern loggedInIndicatorPattern = null;

	/** The logged out indicator pattern. */
	protected Pattern loggedOutIndicatorPattern = null;

	/**
	 * Checks if the response received by the Http Message corresponds to an authenticated Web
	 * Session.
	 * <p>
	 * If none of the indicators are set up, the method defaults to returning true, so that no
	 * authentications are tried when there is no way to check authentication. A message is also
	 * shown on the output console in this case.
	 * </p>
	 * 
	 * @param msg the http message
	 * @return true, if is authenticated or no indicators have been set, and false otherwise
	 */
	public boolean isAuthenticated(HttpMessage msg) {
		if (msg == null || msg.getResponseBody() == null) {
			return false;
		}
		// Assume logged in if nothing was set up
		if (loggedInIndicatorPattern == null && loggedOutIndicatorPattern == null) {
			try {
				Stats.incCounter(SessionStructure.getHostName(msg), AUTH_STATE_NO_INDICATOR_STATS);
			} catch (URIException e) {
				// Ignore
			}
			if (View.isInitialised()) {
				// Let the user know this
				View.getSingleton()
						.getOutputPanel()
						.append(Constant.messages.getString("authentication.output.indicatorsNotSet", msg
								.getRequestHeader().getURI())
								+ "\n");
			}
			return true;
		}

		String body = msg.getResponseBody().toString();
		String header = msg.getResponseHeader().toString();

		if (loggedInIndicatorPattern != null
				&& (loggedInIndicatorPattern.matcher(body).find() || loggedInIndicatorPattern.matcher(header)
						.find())) {
			// Looks like we're authenticated
			try {
				Stats.incCounter(SessionStructure.getHostName(msg), AUTH_STATE_LOGGED_IN_STATS);
			} catch (URIException e) {
				// Ignore
			}
			return true;
		}

		if (loggedOutIndicatorPattern != null && !loggedOutIndicatorPattern.matcher(body).find()
				&& !loggedOutIndicatorPattern.matcher(header).find()) {
			// Cant find the unauthenticated indicator, assume we're authenticated but record as unknown
			try {
				Stats.incCounter(SessionStructure.getHostName(msg), AUTH_STATE_UNKNOWN_STATS);
			} catch (URIException e) {
				// Ignore
			}
			return true;
		}
		// Not looking good...
		try {
			Stats.incCounter(SessionStructure.getHostName(msg), AUTH_STATE_LOGGED_OUT_STATS);
		} catch (URIException e) {
			// Ignore
		}
		return false;
	}

	/**
	 * Gets the logged in indicator pattern.
	 * 
	 * @return the logged in indicator pattern
	 */
	public Pattern getLoggedInIndicatorPattern() {
		return loggedInIndicatorPattern;
	}

	/**
	 * Sets the logged in indicator pattern.
	 * 
	 * @param loggedInIndicatorPattern the new logged in indicator pattern
	 */
	public void setLoggedInIndicatorPattern(String loggedInIndicatorPattern) {
		if (loggedInIndicatorPattern == null || loggedInIndicatorPattern.trim().length() == 0) {
			this.loggedInIndicatorPattern = null;
		} else {
			this.loggedInIndicatorPattern = Pattern.compile(loggedInIndicatorPattern);
		}
	}

	/**
	 * Gets the logged out indicator pattern.
	 * 
	 * @return the logged out indicator pattern
	 */
	public Pattern getLoggedOutIndicatorPattern() {
		return loggedOutIndicatorPattern;
	}

	/**
	 * Sets the logged out indicator pattern.
	 * 
	 * @param loggedOutIndicatorPattern the new logged out indicator pattern
	 */
	public void setLoggedOutIndicatorPattern(String loggedOutIndicatorPattern) {
		if (loggedOutIndicatorPattern == null || loggedOutIndicatorPattern.trim().length() == 0) {
			this.loggedOutIndicatorPattern = null;
		} else {
			this.loggedOutIndicatorPattern = Pattern.compile(loggedOutIndicatorPattern);
		}
	}

	/**
	 * Checks if another method is of the same type.
	 * 
	 * @param other the other
	 * @return true, if is same type
	 */
	public boolean isSameType(AuthenticationMethod other) {
		if (other == null)
			return false;
		return other.getClass().equals(this.getClass());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((loggedInIndicatorPattern == null) ? 0 : loggedInIndicatorPattern.pattern().hashCode());
		result = prime * result
				+ ((loggedOutIndicatorPattern == null) ? 0 : loggedOutIndicatorPattern.pattern().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuthenticationMethod other = (AuthenticationMethod) obj;
		if (loggedInIndicatorPattern == null) {
			if (other.loggedInIndicatorPattern != null)
				return false;
		} else if (!loggedInIndicatorPattern.pattern().equals(other.loggedInIndicatorPattern.pattern()))
			return false;
		if (loggedOutIndicatorPattern == null) {
			if (other.loggedOutIndicatorPattern != null)
				return false;
		} else if (!loggedOutIndicatorPattern.pattern().equals(other.loggedOutIndicatorPattern.pattern()))
			return false;
		return true;
	}

	/**
	 * Thrown when an unsupported type of credentials is used with a {@link AuthenticationMethod} .
	 */
	public static class UnsupportedAuthenticationCredentialsException extends RuntimeException {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 4802501809913124766L;

		/**
		 * Instantiates a new unsupported authentication credentials exception.
		 * 
		 * @param message the message
		 */
		public UnsupportedAuthenticationCredentialsException(String message) {
			super(message);
		}
	}

}

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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethodFactory;
import org.zaproxy.zap.userauth.session.SessionManagementMethodFactory;

/**
 * The Manager that handles all the information related to {@link User Users}, {@link Role Roles}
 * and authentications in various {@link Context Contexts}. This class also handles the loading of
 * {@link AuthenticationMethodFactory} and {@link SessionManagementMethodFactory} classes in the
 * system using the AddOnLoader ({@link ExtensionFactory#getAddOnLoader()}).
 */
public class UserAuthManager {

	private static final Logger log = Logger.getLogger(UserAuthManager.class);

	List<AuthenticationMethodFactory<?>> authenticationMethodFactories;
	List<SessionManagementMethodFactory<?>> sessionManagementMethodFactories;

	/**
	 * Load authentication method factories using reflection.
	 */
	private void loadAuthenticationMethodFactories() {
		// Load the method factories as raw types (only way supported) and put them in a list of
		// parameterized methods
		@SuppressWarnings("rawtypes")
		List<AuthenticationMethodFactory> rawFactories = ExtensionFactory.getAddOnLoader().getImplementors(
				"org.zaproxy.zap", AuthenticationMethodFactory.class);
		authenticationMethodFactories = new ArrayList<AuthenticationMethodFactory<?>>(rawFactories.size());
		for (AuthenticationMethodFactory<?> a : rawFactories) {
			authenticationMethodFactories.add(a);
		}

		if (log.isInfoEnabled()) {
			log.info("Loaded authentication method factories: " + authenticationMethodFactories);
		}
	}

	/**
	 * Load session management method factories using reflection.
	 */
	private void loadSesssionManagementMethodFactories() {
		// Load the method factories as raw types (only way supported) and put them in a list of
		// parameterized methods
		@SuppressWarnings("rawtypes")
		List<SessionManagementMethodFactory> rawFactories = ExtensionFactory.getAddOnLoader()
				.getImplementors("org.zaproxy.zap", SessionManagementMethodFactory.class);
		sessionManagementMethodFactories = new ArrayList<SessionManagementMethodFactory<?>>(
				rawFactories.size());
		for (SessionManagementMethodFactory<?> sm : rawFactories) {
			sessionManagementMethodFactories.add(sm);
		}

		if (log.isInfoEnabled()) {
			log.info("Loaded session management method factories: " + sessionManagementMethodFactories);
		}
	}

	/**
	 * Gets all the registered/loaded authentication method factories.
	 * 
	 * @return the authentication method factories
	 */
	public List<AuthenticationMethodFactory<?>> getAuthenticationMethodFactories() {
		return authenticationMethodFactories;
	}

	/**
	 * Gets all the registered/loaded session management method factories.
	 * 
	 * @return the session management method factories
	 */
	public List<SessionManagementMethodFactory<?>> getSessionManagementMethodFactories() {
		return sessionManagementMethodFactories;
	}

	/** The instance. */
	private static UserAuthManager instance;

	/**
	 * Gets the single instance of UserAuthManager.
	 * 
	 * @return single instance of UserAuthManager
	 */
	public static UserAuthManager getInstance() {
		if (instance == null) {
			instance = new UserAuthManager();
			instance.loadAuthenticationMethodFactories();
			instance.loadSesssionManagementMethodFactories();
		}
		return instance;
	}
}

package org.zaproxy.zap.userauth;

import java.util.ArrayList;
import java.util.List;

import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethodFactory;
import org.zaproxy.zap.userauth.authentication.ManualAuthenticationMethod;
import org.zaproxy.zap.userauth.session.CookieBasedSessionManagementMethod;
import org.zaproxy.zap.userauth.session.SessionManagementMethodFactory;

/**
 * The Manager that handles all the information related to {@link User Users}, {@link Role Roles}
 * and authentications in various {@link Context Contexts}.
 */
public class UserAuthManager {

	List<AuthenticationMethodFactory<?>> authenticationMethods;
	List<SessionManagementMethodFactory<?>> sessionManagementMethods;

	public void loadAuthenticationMethodFactories() {
		authenticationMethods = new ArrayList<AuthenticationMethodFactory<?>>();
		// TODO: Load factories using reflection
		authenticationMethods.add(new ManualAuthenticationMethod.ManualAuthenticationMethodFactory());
	}

	public void loadSesssionManagementMethodFactories() {
		sessionManagementMethods = new ArrayList<SessionManagementMethodFactory<?>>();
		// TODO: Load factories using reflection
		sessionManagementMethods
				.add(new CookieBasedSessionManagementMethod.CookieBasedSessionManagementMethodFactory());
	}

	public List<AuthenticationMethodFactory<?>> getAuthenticationMethodFactories() {
		return authenticationMethods;
	}

	public List<SessionManagementMethodFactory<?>> getSessionManagementMethodFactories() {
		return sessionManagementMethods;
	}

	private static UserAuthManager instance;

	public static UserAuthManager getInstance() {
		if (instance == null) {
			instance = new UserAuthManager();
			instance.loadAuthenticationMethodFactories();
			instance.loadSesssionManagementMethodFactories();
		}
		return instance;
	}
}

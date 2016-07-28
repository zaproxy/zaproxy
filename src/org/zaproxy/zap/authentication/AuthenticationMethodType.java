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

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;

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
 */
public abstract class AuthenticationMethodType {

	/**
	 * Builds a new, empty, authentication method. The authentication method should then be
	 * configured through the Options panel.
	 * 
	 * @param contextId the ID of the context for which the authentication method is being created
	 * @return the authentication method
	 * @see #buildOptionsPanel(Context)
	 */
	public abstract AuthenticationMethod createAuthenticationMethod(int contextId);

	/**
	 * Gets the name of the authentication method.
	 * 
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * Gets the unique identifier of this Authentication Method Type. It has to be unique among all
	 * Authentication Method Types.
	 * 
	 * @return the unique identifier
	 */
	public abstract int getUniqueIdentifier();

	/**
	 * Builds the options panel that can be used to fully configure an authentication method.
	 * <p>
	 * This method just builds an empty options panel. For binding an existing method to the panel,
	 * {@link AbstractAuthenticationMethodOptionsPanel#bindMethod(AuthenticationMethod)} should be
	 * used.
	 * </p>
	 * 
	 * @param uiSharedContext the shared context on which the panel should work
	 * @return the abstract authentication method options panel
	 * @see #hasOptionsPanel()
	 */
	public abstract AbstractAuthenticationMethodOptionsPanel buildOptionsPanel(Context uiSharedContext);

	/**
	 * Checks if the corresponding {@link AuthenticationMethod} has an options panel that can be
	 * used for configuration.
	 * 
	 * @return true, if it needs one
	 * @see #buildOptionsPanel(Context)
	 */
	public abstract boolean hasOptionsPanel();

	/**
	 * Builds the options panel that can be used to fully configure an.
	 * 
	 * @param credentials the credentials
	 * @param uiSharedContext the shared context on which the panel should work
	 * @return the abstract credentials options panel {@link AuthenticationCredentials} object.
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
	 * Checks if is this is a type for the Authentication Method provided as parameter.
	 * 
	 * @param method the method
	 * @return true, if is factory for method
	 */
	public abstract boolean isTypeForMethod(AuthenticationMethod method);

	/**
	 * Hooks the Authentication Method Type with other components of ZAP, if needed. This method
	 * should is called only ones, when authentication types are loaded.
	 * <p>
	 * For example, PopupMenus can be registered.
	 * </p>
	 * 
	 * @param extensionHook the extension hook
	 */
	public abstract void hook(ExtensionHook extensionHook);

	/**
	 * Loads an authentication method from the Session. The implementation depends on the
	 * Authentication method type.
	 * 
	 * @param session the session
	 * @param contextId the ID of the context
	 * @return the authentication method
	 * @throws DatabaseException if an error occurred while loading the authentication method
	 */
	public abstract AuthenticationMethod loadMethodFromSession(Session session, int contextId)
			throws DatabaseException;

	/**
	 * Persists the authentication method to the session.
	 * 
	 * @param session the session
	 * @param contextId the context id
	 * @param authMethod the auth method to persist
	 * @throws UnsupportedAuthenticationMethodException the unsupported authentication method
	 *             exception
	 * @throws DatabaseException if an error occurred while persisting the authentication method
	 */
	public abstract void persistMethodToSession(Session session, int contextId,
			AuthenticationMethod authMethod) throws DatabaseException;

	/**
	 * Export the specified method to the configuration
	 * @param config the {@code Configuration} where to export/save the authentication method
	 * @param authMethod the authentication method to be exported
	 */
	public abstract void exportData(Configuration config, AuthenticationMethod authMethod);

	/**
	 * Import the method from the configuration
	 * @param config the {@code Configuration} from where to import/load the authentication method
	 * @param authMethod where to set the imported authentication method data
	 * @throws ConfigurationException if an error occurred while reading the authentication method data 
	 */
	public abstract void importData(Configuration config, AuthenticationMethod authMethod) throws ConfigurationException;

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Creates a new, empty, Authentication Credentials object corresponding to this type of
	 * Authentication method.
	 * 
	 * @return the authentication credentials
	 */
	public abstract AuthenticationCredentials createAuthenticationCredentials();

	/**
	 * Gets the {@link ApiAction} that can be used to set an authentication method of this type for
	 * a context.
	 * <p>
	 * This api action will be handled by executing the
	 * {@link ApiDynamicActionImplementor#handleAction(JSONObject)} method.
	 * </p>
	 * <p>
	 * In the {@link ApiDynamicActionImplementor#handleAction(JSONObject)} method, if the new type
	 * of method differs from the current type of method, a call should be made to
	 * {@link #apiChangedAuthenticationMethodForContext(int)} to make sure changes are propagated.
	 * </p>
	 * 
	 * @return the api action, or null if there is no way to set this method type through the API
	 */
	public abstract ApiDynamicActionImplementor getSetMethodForContextApiAction();

	/**
	 * Gets the {@link ApiDynamicActionImplementor} that can be used to set the authentication
	 * credentials of the corresponding type for an {@link User} of a context.
	 * <p>
	 * This api action will be handled by executing the
	 * {@link ApiDynamicActionImplementor#handleAction(JSONObject)} method.
	 * </p>
	 * 
	 * @return the api action, or null if there is no way to set this method type through the API
	 */
	public abstract ApiDynamicActionImplementor getSetCredentialsForUserApiAction();

	/**
	 * Called when the authentication method for a context is changed through an API call.
	 * <p>
	 * Makes sure the Users are wiped so the authentication credentials for them match the Method.
	 * </p>
	 * 
	 * @param contextId the context id
	 */
	protected static void apiChangedAuthenticationMethodForContext(int contextId) {
		// Make sure the Users are wiped so the authentication credentials for them match the
		// Method.
		ExtensionUserManagement usersExtension = (ExtensionUserManagement) Control.getSingleton()
				.getExtensionLoader().getExtension(ExtensionUserManagement.NAME);
		if (usersExtension != null) {
			if (usersExtension.getContextUserAuthManager(contextId).getUsers().size() > 0) {
				usersExtension.removeContextUsers(contextId);
			}
		}
	}

	@Override
	public int hashCode() {
		return getUniqueIdentifier();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	/**
	 * Thrown when an unsupported type of credentials is used with a {@link AuthenticationMethod} .
	 */
	public static class UnsupportedAuthenticationMethodException extends RuntimeException {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -9023988233854612561L;

		public UnsupportedAuthenticationMethodException(String message) {
			super(message);
		}

	}

}

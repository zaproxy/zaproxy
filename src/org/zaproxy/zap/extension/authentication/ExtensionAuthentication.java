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
package org.zaproxy.zap.extension.authentication;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.authentication.AuthenticationMethod;
import org.zaproxy.zap.authentication.AuthenticationMethodType;
import org.zaproxy.zap.authentication.FormBasedAuthenticationMethodType;
import org.zaproxy.zap.authentication.FormBasedAuthenticationMethodType.FormBasedAuthenticationMethod;
import org.zaproxy.zap.authentication.HttpAuthenticationMethodType;
import org.zaproxy.zap.authentication.ManualAuthenticationMethodType;
import org.zaproxy.zap.authentication.ScriptBasedAuthenticationMethodType;
import org.zaproxy.zap.extension.stdmenus.PopupContextMenuItemFactory;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextPanelFactory;

/**
 * The Extension that handles Authentication methods, in correlation with Contexts.
 */
public class ExtensionAuthentication extends ExtensionAdaptor implements ContextPanelFactory,
		ContextDataFactory {

	public static final int EXTENSION_ORDER = 52;
	
	/** The NAME of the extension. */
	public static final String NAME = "ExtensionAuthentication";

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(ExtensionAuthentication.class);

	/** The automatically loaded authentication method types. */
	List<AuthenticationMethodType> authenticationMethodTypes;

	/** The context panels map. */
	private Map<Integer, ContextAuthenticationPanel> contextPanelsMap = new HashMap<>();

	private PopupContextMenuItemFactory popupFlagLoggedInIndicatorMenuFactory;

	private PopupContextMenuItemFactory popupFlagLoggedOutIndicatorMenuFactory;

	AuthenticationAPI api;

	public ExtensionAuthentication() {
		super();
		initialize();
	}

	/**
	 * Initialize the extension.
	 */
	private void initialize() {
		this.setName(NAME);
		this.setOrder(EXTENSION_ORDER);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);
		// Register this as a context data factory
		Model.getSingleton().addContextDataFactory(this);

		if (getView() != null) {
			extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagLoggedInIndicatorMenu());
			extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagLoggedOutIndicatorMenu());

			// Factory for generating Session Context UserAuth panels
			getView().addContextPanelFactory(this);
		}

		// Load the Authentication and Session Management methods
		this.loadAuthenticationMethodTypes(extensionHook);

		// Register the api
		this.api = new AuthenticationAPI(this);
		extensionHook.addApiImplementor(api);
	}

	@Override
	public AbstractContextPropertiesPanel getContextPanel(Context context) {
		ContextAuthenticationPanel panel = this.contextPanelsMap.get(context.getIndex());
		if (panel == null) {
			panel = new ContextAuthenticationPanel(this, context);
			this.contextPanelsMap.put(context.getIndex(), panel);
		}
		return panel;
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	/**
	 * Gets the popup menu for flagging the "Logged in" pattern.
	 * 
	 * @return the popup menu
	 */
	private PopupContextMenuItemFactory getPopupFlagLoggedInIndicatorMenu() {
		if (this.popupFlagLoggedInIndicatorMenuFactory == null) {
			popupFlagLoggedInIndicatorMenuFactory = new PopupContextMenuItemFactory("dd - "
					+ Constant.messages.getString("context.flag.popup")) {

				private static final long serialVersionUID = 2453839120088204122L;

				@Override
				public ExtensionPopupMenuItem getContextMenu(Context context, String parentMenu) {
					return new PopupFlagLoggedInIndicatorMenu(context);
				}

			};
		}
		return this.popupFlagLoggedInIndicatorMenuFactory;
	}

	/**
	 * Gets the popup menu for flagging the "Logged out" pattern.
	 * 
	 * @return the popup menu
	 */
	private PopupContextMenuItemFactory getPopupFlagLoggedOutIndicatorMenu() {
		if (this.popupFlagLoggedOutIndicatorMenuFactory == null) {
			popupFlagLoggedOutIndicatorMenuFactory = new PopupContextMenuItemFactory("dd - "
					+ Constant.messages.getString("context.flag.popup")) {

				private static final long serialVersionUID = 2453839120088204123L;

				@Override
				public ExtensionPopupMenuItem getContextMenu(Context context, String parentMenu) {
					return new PopupFlagLoggedOutIndicatorMenu(context);
				}

			};
		}
		return this.popupFlagLoggedOutIndicatorMenuFactory;
	}

	/**
	 * Loads the authentication method types and hooks them up.
	 * 
	 * @param hook the extension hook
	 */
	private void loadAuthenticationMethodTypes(ExtensionHook hook) {
		this.authenticationMethodTypes = new ArrayList<>();
		this.authenticationMethodTypes.add(new FormBasedAuthenticationMethodType());
		this.authenticationMethodTypes.add(new HttpAuthenticationMethodType());
		this.authenticationMethodTypes.add(new ManualAuthenticationMethodType());
		this.authenticationMethodTypes.add(new ScriptBasedAuthenticationMethodType());

		for (AuthenticationMethodType a : authenticationMethodTypes) {
			a.hook(hook);
		}

		if (log.isInfoEnabled()) {
			log.info("Loaded authentication method types: " + authenticationMethodTypes);
		}
	}

	/**
	 * Gets all the registered/loaded authentication method types.
	 * 
	 * @return the authentication method types
	 */
	public List<AuthenticationMethodType> getAuthenticationMethodTypes() {
		return authenticationMethodTypes;
	}

	/**
	 * Gets the authentication method type for a given identifier.
	 * 
	 * @param id the id
	 * @return the authentication method type for identifier
	 */
	public AuthenticationMethodType getAuthenticationMethodTypeForIdentifier(int id) {
		for (AuthenticationMethodType t : getAuthenticationMethodTypes())
			if (t.getUniqueIdentifier() == id)
				return t;
		return null;
	}

	/**
	 * Gets the URI for the login request that corresponds to a given context, if any.
	 * 
	 * @param ctx the context
	 * @return the login request uri for context, or <code>null</code>, if the context does not have
	 *         a 'login request' configured
	 */
	public URI getLoginRequestURIForContext(Context ctx) {
		if (!(ctx.getAuthenticationMethod() instanceof FormBasedAuthenticationMethod))
			return null;
		FormBasedAuthenticationMethod method = (FormBasedAuthenticationMethod) ctx.getAuthenticationMethod();
		try {
			return new URI(method.getLoginRequestURL(), false);
		} catch (URIException | NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void loadContextData(Session session, Context context) {
		try {
			List<String> typeL = session.getContextDataStrings(context.getIndex(),
					RecordContext.TYPE_AUTH_METHOD_TYPE);
			if (typeL != null && typeL.size() > 0) {
				AuthenticationMethodType t = getAuthenticationMethodTypeForIdentifier(Integer.parseInt(typeL
						.get(0)));
				if (t != null) {
					context.setAuthenticationMethod(t.loadMethodFromSession(session, context.getIndex()));

					List<String> loginIndicatorL = session.getContextDataStrings(context.getIndex(),
							RecordContext.TYPE_AUTH_METHOD_LOGGEDIN_INDICATOR);
					if (loginIndicatorL != null && loginIndicatorL.size() > 0)
						context.getAuthenticationMethod().setLoggedInIndicatorPattern(loginIndicatorL.get(0));

					List<String> logoutIndicatorL = session.getContextDataStrings(context.getIndex(),
							RecordContext.TYPE_AUTH_METHOD_LOGGEDOUT_INDICATOR);
					if (logoutIndicatorL != null && logoutIndicatorL.size() > 0)
						context.getAuthenticationMethod().setLoggedOutIndicatorPattern(
								logoutIndicatorL.get(0));
				}
			}

		} catch (DatabaseException e) {
			log.error("Unable to load Authentication method.", e);
		}

	}

	@Override
	public void persistContextData(Session session, Context context) {
		try {
			AuthenticationMethodType t = context.getAuthenticationMethod().getType();
			session.setContextData(context.getIndex(), RecordContext.TYPE_AUTH_METHOD_TYPE,
					Integer.toString(t.getUniqueIdentifier()));

			if (context.getAuthenticationMethod().getLoggedInIndicatorPattern() != null)
				session.setContextData(context.getIndex(), RecordContext.TYPE_AUTH_METHOD_LOGGEDIN_INDICATOR,
						context.getAuthenticationMethod().getLoggedInIndicatorPattern().toString());

			if (context.getAuthenticationMethod().getLoggedOutIndicatorPattern() != null)
				session.setContextData(context.getIndex(),
						RecordContext.TYPE_AUTH_METHOD_LOGGEDOUT_INDICATOR, context.getAuthenticationMethod()
								.getLoggedOutIndicatorPattern().toString());

			t.persistMethodToSession(session, context.getIndex(), context.getAuthenticationMethod());
		} catch (DatabaseException e) {
			log.error("Unable to persist Authentication method.", e);
		}
	}

	@Override
	public void discardContexts() {
		contextPanelsMap.clear();
	}

	@Override
	public void discardContext(Context ctx) {
		contextPanelsMap.remove(ctx.getIndex());
	}

	@Override
	public void exportContextData(Context ctx, Configuration config) {
		config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_TYPE, ctx.getAuthenticationMethod().getType().getUniqueIdentifier());
		if (ctx.getAuthenticationMethod().getLoggedInIndicatorPattern() != null) {
			config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDIN, 
					ctx.getAuthenticationMethod().getLoggedInIndicatorPattern().toString());
		}
		if (ctx.getAuthenticationMethod().getLoggedOutIndicatorPattern() != null) {
			config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDOUT, 
					ctx.getAuthenticationMethod().getLoggedOutIndicatorPattern().toString());
		}
		ctx.getAuthenticationMethod().getType().exportData(config, ctx.getAuthenticationMethod());

	}
	
	@Override
	public void importContextData(Context ctx, Configuration config) throws ConfigurationException {
		ctx.setAuthenticationMethod(
				getAuthenticationMethodTypeForIdentifier(
						config.getInt(AuthenticationMethod.CONTEXT_CONFIG_AUTH_TYPE)).createAuthenticationMethod(ctx.getIndex()));
		String str = config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDIN, "");
		if (str.length() > 0) {
			ctx.getAuthenticationMethod().setLoggedInIndicatorPattern(str);
		}
		str = config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDOUT, "");
		if (str.length() > 0) {
			ctx.getAuthenticationMethod().setLoggedOutIndicatorPattern(str);
		}
		ctx.getAuthenticationMethod().getType().importData(config, ctx.getAuthenticationMethod());

	}

}

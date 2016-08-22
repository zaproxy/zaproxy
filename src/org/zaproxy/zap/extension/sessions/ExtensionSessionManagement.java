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
package org.zaproxy.zap.extension.sessions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.session.CookieBasedSessionManagementMethodType;
import org.zaproxy.zap.session.HttpAuthSessionManagementMethodType;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.SessionManagementMethodType;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextPanelFactory;

/**
 * The Extension that handles Session Management methods.
 */
public class ExtensionSessionManagement extends ExtensionAdaptor implements ContextPanelFactory,
		ContextDataFactory {

	/** The NAME of the extension. */
	public static final String NAME = "ExtensionSessionManagement";

	public static final String CONTEXT_CONFIG_SESSION = Context.CONTEXT_CONFIG + ".session";
	public static final String CONTEXT_CONFIG_SESSION_TYPE = CONTEXT_CONFIG_SESSION + ".type";

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(ExtensionSessionManagement.class);

	/** The automatically loaded session management method types. */
	List<SessionManagementMethodType> sessionManagementMethodTypes;

	/** The map of context panels. */
	private Map<Integer, ContextSessionManagementPanel> contextPanelsMap = new HashMap<>();

	private SessionManagementAPI api;

	public ExtensionSessionManagement() {
		super();
		initialize();
	}

	/**
	 * Initialize the extension.
	 */
	private void initialize() {
		this.setName(NAME);
		this.setOrder(103);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);
		// Register this as a context data factory
		Model.getSingleton().addContextDataFactory(this);

		if (getView() != null) {
			// Factory for generating Session Context UserAuth panels
			getView().addContextPanelFactory(this);
		}

		// Load the Session Management methods
		this.loadSesssionManagementMethodTypes(extensionHook);

		// Register the api
		this.api = new SessionManagementAPI(this);
		extensionHook.addApiImplementor(api);
	}

	@Override
	public AbstractContextPropertiesPanel getContextPanel(Context context) {
		ContextSessionManagementPanel panel = this.contextPanelsMap.get(context.getIndex());
		if (panel == null) {
			panel = new ContextSessionManagementPanel(this, context);
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

	public List<SessionManagementMethodType> getSessionManagementMethodTypes() {
		return sessionManagementMethodTypes;
	}

	/**
	 * Loads session management method types and hooks them up.
	 * 
	 * @param extensionHook the extension hook
	 */
	private void loadSesssionManagementMethodTypes(ExtensionHook extensionHook) {
		this.sessionManagementMethodTypes = new ArrayList<>();
		this.sessionManagementMethodTypes.add(new CookieBasedSessionManagementMethodType());
		this.sessionManagementMethodTypes.add(new HttpAuthSessionManagementMethodType());

		for (SessionManagementMethodType t : sessionManagementMethodTypes) {
			t.hook(extensionHook);
		}

		if (log.isInfoEnabled()) {
			log.info("Loaded session management method types: " + sessionManagementMethodTypes);
		}
	}

	@Override
	public void discardContexts() {
		this.contextPanelsMap.clear();
	}

	@Override
	public void discardContext(Context c) {
		this.contextPanelsMap.remove(c.getIndex());
	}

	/**
	 * Gets the session management method type for a given identifier.
	 * 
	 * @param id the id
	 * @return the session management method type for identifier
	 */
	public SessionManagementMethodType getSessionManagementMethodTypeForIdentifier(int id) {
		for (SessionManagementMethodType t : getSessionManagementMethodTypes())
			if (t.getUniqueIdentifier() == id)
				return t;
		return null;
	}

	@Override
	public void loadContextData(Session session, Context context) {
		try {
			List<String> typeL = session.getContextDataStrings(context.getIndex(),
					RecordContext.TYPE_SESSION_MANAGEMENT_TYPE);
			if (typeL != null && typeL.size() > 0) {
				SessionManagementMethodType t = getSessionManagementMethodTypeForIdentifier(Integer
						.parseInt(typeL.get(0)));
				if (t != null) {
					context.setSessionManagementMethod(t.loadMethodFromSession(session, context.getIndex()));
				}
			}
		} catch (DatabaseException e) {
			log.error("Unable to load Session Management method.", e);
		}

	}

	@Override
	public void persistContextData(Session session, Context context) {
		try {
			SessionManagementMethodType t = context.getSessionManagementMethod().getType();
			session.setContextData(context.getIndex(), RecordContext.TYPE_SESSION_MANAGEMENT_TYPE,
					Integer.toString(t.getUniqueIdentifier()));
			t.persistMethodToSession(session, context.getIndex(), context.getSessionManagementMethod());
		} catch (DatabaseException e) {
			log.error("Unable to persist Session Management method.", e);
		}
	}

	@Override
	public void exportContextData(Context ctx, Configuration config) {
		config.setProperty(CONTEXT_CONFIG_SESSION_TYPE, ctx.getSessionManagementMethod().getType().getUniqueIdentifier());
	}

	@Override
	public void importContextData(Context ctx, Configuration config) throws ConfigurationException {
		SessionManagementMethodType t = getSessionManagementMethodTypeForIdentifier(config.getInt(CONTEXT_CONFIG_SESSION_TYPE));
		if (t != null) {
			SessionManagementMethod method = t.createSessionManagementMethod(ctx.getIndex());
			t.importData(config, method);
			ctx.setSessionManagementMethod(method);
		}
	}

}

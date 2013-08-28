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
package org.zaproxy.zap.extension.userauth.sessions;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.userauth.session.SessionManagementMethodType;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextPanelFactory;

/**
 * The Extension that handles Session Management methods.
 */
public class ExtensionSessionManagement extends ExtensionAdaptor implements ContextPanelFactory,
		ContextDataFactory {

	/** The NAME of the extension. */
	public static final String NAME = "ExtensionSessionManagement";

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(ExtensionSessionManagement.class);

	/** The automatically loaded session management method types. */
	List<SessionManagementMethodType> sessionManagementMethodTypes;

	/** The map of context panels. */
	private Map<Integer, ContextSessionManagementPanel> contextPanelsMap = new HashMap<>();

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

		// TODO: Prepare API
		// this.api = new AuthAPI(this);
		// API.getInstance().registerApiImplementor(api);
		// HttpSender.addListener(this);
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
	 * Load session management method types using reflection.
	 * 
	 * @param extensionHook the extension hook
	 */
	private void loadSesssionManagementMethodTypes(ExtensionHook extensionHook) {
		this.sessionManagementMethodTypes = ExtensionFactory.getAddOnLoader().getImplementors(
				"org.zaproxy.zap", SessionManagementMethodType.class);

		for (SessionManagementMethodType t : sessionManagementMethodTypes)
			t.hook(extensionHook);

		if (log.isInfoEnabled()) {
			log.info("Loaded session management method types: " + sessionManagementMethodTypes);
		}
	}

	@Override
	public void discardContexts() {
		// TODO Auto-generated method stub
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
				if (t != null)
					context.setSessionManagementMethod(t.loadMethodFromSession(session, context.getIndex()));
			}
		} catch (SQLException e) {
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
		} catch (SQLException e) {
			log.error("Unable to persist Session Management method.", e);
		}
	}

}

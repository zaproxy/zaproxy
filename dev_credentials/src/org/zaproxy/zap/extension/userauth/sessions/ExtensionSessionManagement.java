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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
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

	@Override
	public void loadContextData(Context ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveContextData(Context ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void discardContexts() {
		// TODO Auto-generated method stub
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

}

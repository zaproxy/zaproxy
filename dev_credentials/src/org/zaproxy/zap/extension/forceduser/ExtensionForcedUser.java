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
package org.zaproxy.zap.extension.forceduser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.userauth.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.network.HttpSenderListener;
import org.zaproxy.zap.userauth.User;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextPanelFactory;

/**
 * The ForcedUser Extension allows ZAP user to force all requests that correspond to a given Context
 * to be sent from the point of view of a User.
 */
public class ExtensionForcedUser extends ExtensionAdaptor implements ContextPanelFactory, HttpSenderListener {

	/** The Constant EXTENSION DEPENDENCIES. */
	private static final List<Class<?>> EXTENSION_DEPENDENCIES;
	static {
		// Prepare a list of Extensions on which this extension depends
		List<Class<?>> dependencies = new ArrayList<>();
		dependencies.add(ExtensionUserManagement.class);
		EXTENSION_DEPENDENCIES = Collections.unmodifiableList(dependencies);
	}

	/** The NAME of the extension. */
	public static final String NAME = "ExtensionForcedUser";

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(ExtensionForcedUser.class);

	/** The map of context panels. */
	private Map<Integer, ContextForcedUserPanel> contextPanelsMap = new HashMap<>();

	private Map<Integer, User> contextForcedUsersMap = new HashMap<>();

	private ExtensionUserManagement extensionUserManagement;

	private boolean forcedUserModeEnabled = true;

	/**
	 * Instantiates a new forced user extension.
	 */
	public ExtensionForcedUser() {
		super();
		initialize();
	}

	/**
	 * Initialize the extension.
	 */
	private void initialize() {
		this.setName(NAME);
		this.setOrder(202);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);

		if (getView() != null) {
			// Factory for generating Session Context UserAuth panels
			getView().addContextPanelFactory(this);
		}

		// Register as Http Sender listener
		HttpSender.addListener(this);
	}

	protected ExtensionUserManagement getUserManagementExtension() {
		if (extensionUserManagement == null) {
			extensionUserManagement = (ExtensionUserManagement) Control.getSingleton().getExtensionLoader()
					.getExtension(ExtensionUserManagement.NAME);
		}
		return extensionUserManagement;
	}

	/**
	 * Sets the forced user for a context.
	 * 
	 * @param contextId the context id
	 * @param user the user
	 */
	public void setForcedUser(int contextId, User user) {
		this.contextForcedUsersMap.put(contextId, user);
	}

	/**
	 * Gets the forced user for a context.
	 * 
	 * @param contextId the context id
	 * @return the forced user
	 */
	public User getForcedUser(int contextId) {
		return this.contextForcedUsersMap.get(contextId);
	}

	@Override
	public List<Class<?>> getDependencies() {
		return EXTENSION_DEPENDENCIES;
	}

	@Override
	public AbstractContextPropertiesPanel getContextPanel(Context context) {
		ContextForcedUserPanel panel = this.contextPanelsMap.get(context.getIndex());
		if (panel == null) {
			panel = new ContextForcedUserPanel(this, context.getIndex());
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
	public void discardContexts() {
		this.contextForcedUsersMap.clear();
		this.contextPanelsMap.clear();
	}

	@Override
	public int getListenerOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onHttpRequestSend(HttpMessage msg, int initiator) {
		if (!forcedUserModeEnabled || msg.getResponseBody() == null || msg.getRequestHeader().isImage()
				|| (initiator == HttpSender.AUTHENTICATION_INITIATOR)) {
			// Not relevant
			return;
		}

		// The message is already being sent from the POV of another user
		if (msg.getRequestingUser() != null)
			return;

		// Is the message in any of the contexts?
		List<Context> contexts = Model.getSingleton().getSession().getContexts();
		User requestingUser = null;
		for (Context context : contexts) {
			if (context.isInContext(msg.getRequestHeader().getURI().toString())) {
				// Is there enough info
				if (contextForcedUsersMap.containsKey(context.getIndex())) {
					requestingUser = contextForcedUsersMap.get(context.getIndex());
					break;
				}
			}
		}

		if (requestingUser == null)
			return;

		if (log.isDebugEnabled()) {
			log.debug("Modifying request message (" + msg.getRequestHeader().getURI() + ") to match user: "
					+ requestingUser);
		}
		msg.setRequestingUser(requestingUser);
	}

	@Override
	public void onHttpResponseReceive(HttpMessage msg, int initiator) {
		// TODO Auto-generated method stub

	}

}

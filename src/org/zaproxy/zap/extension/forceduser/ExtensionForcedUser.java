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

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.network.HttpSenderListener;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextPanelFactory;
import org.zaproxy.zap.view.ZapToggleButton;

/**
 * The ForcedUser Extension allows ZAP user to force all requests that correspond to a given Context
 * to be sent from the point of view of a User.
 */
public class ExtensionForcedUser extends ExtensionAdaptor implements ContextPanelFactory, HttpSenderListener,
		ContextDataFactory {

	/** The Constant EXTENSION DEPENDENCIES. */
	private static final List<Class<?>> EXTENSION_DEPENDENCIES;
	static {
		// Prepare a list of Extensions on which this extension depends
		List<Class<?>> dependencies = new ArrayList<>(1);
		dependencies.add(ExtensionUserManagement.class);
		EXTENSION_DEPENDENCIES = Collections.unmodifiableList(dependencies);
	}

	private static final String FORCED_USER_MODE_OFF_ICON_RESOURCE = "/resource/icon/16/forcedUserOff.png";
	private static final String FORCED_USER_MODE_ON_ICON_RESOURCE = "/resource/icon/16/forcedUserOn.png";
	private static final String BUTTON_LABEL_ON = Constant.messages.getString("forceduser.toolbar.button.on");
	private static final String BUTTON_LABEL_OFF = Constant.messages
			.getString("forceduser.toolbar.button.off");
	private static final String BUTTON_LABEL_DISABLED = Constant.messages
			.getString("forceduser.toolbar.button.disabled");

	/** The NAME of the extension. */
	public static final String NAME = "ExtensionForcedUser";

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(ExtensionForcedUser.class);

	/** The map of context panels. */
	private Map<Integer, ContextForcedUserPanel> contextPanelsMap = new HashMap<>();

	/** The map of forced users for each context. */
	private Map<Integer, User> contextForcedUsersMap = new HashMap<>();

	private ExtensionUserManagement extensionUserManagement;

	private boolean forcedUserModeEnabled = false;

	private ZapToggleButton forcedUserModeButton;
	private ForcedUserAPI api;

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

		// Register this where needed
		Model.getSingleton().addContextDataFactory(this);

		if (getView() != null) {
			// Factory for generating Session Context UserAuth panels
			getView().addContextPanelFactory(this);

			View.getSingleton().addMainToolbarButton(getForcedUserModeToggleButton());
		}

		// Register as Http Sender listener
		HttpSender.addListener(this);

		// Prepare API
		this.api = new ForcedUserAPI(this);
		extensionHook.addApiImplementor(api);
	}

	private void updateForcedUserModeToggleButtonEnabledState() {
		if (getView() != null) {
			forcedUserModeButton.setSelected(forcedUserModeEnabled);
		}
	}

	protected void setForcedUserModeEnabled(boolean forcedUserModeEnabled) {
		this.forcedUserModeEnabled = forcedUserModeEnabled;
		updateForcedUserModeToggleButtonEnabledState();
	}

	private void setForcedUserModeToggleButtonState(boolean enabled) {
		if (enabled) {
			updateForcedUserModeToggleButtonEnabledState();
			this.getForcedUserModeToggleButton().setEnabled(true);
		} else {
			this.forcedUserModeEnabled = false;
			this.getForcedUserModeToggleButton().setSelected(false);
			this.getForcedUserModeToggleButton().setEnabled(false);
		}
	}

	private void updateForcedUserModeToggleButtonState() {
		if (contextForcedUsersMap.isEmpty()) {
			if (this.getForcedUserModeToggleButton().isEnabled())
				this.setForcedUserModeToggleButtonState(false);
		} else {
			if (!this.getForcedUserModeToggleButton().isEnabled())
				this.setForcedUserModeToggleButtonState(true);
		}
	}

	private JToggleButton getForcedUserModeToggleButton() {
		if (forcedUserModeButton == null) {
			forcedUserModeButton = new ZapToggleButton();
			forcedUserModeButton.setIcon(new ImageIcon(ExtensionForcedUser.class
					.getResource(FORCED_USER_MODE_OFF_ICON_RESOURCE)));
			forcedUserModeButton.setSelectedIcon(new ImageIcon(ExtensionForcedUser.class
					.getResource(FORCED_USER_MODE_ON_ICON_RESOURCE)));
			forcedUserModeButton.setToolTipText(BUTTON_LABEL_OFF);
			forcedUserModeButton.setSelectedToolTipText(BUTTON_LABEL_ON);
			forcedUserModeButton.setDisabledToolTipText(BUTTON_LABEL_DISABLED);
			forcedUserModeButton.setEnabled(false); // Disable until login and one indicator flagged

			forcedUserModeButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setForcedUserModeEnabled(getForcedUserModeToggleButton().isSelected());
				}
			});
		}
		return forcedUserModeButton;
	}

	protected ExtensionUserManagement getUserManagementExtension() {
		if (extensionUserManagement == null) {
			extensionUserManagement = (ExtensionUserManagement) Control.getSingleton().getExtensionLoader()
					.getExtension(ExtensionUserManagement.NAME);
		}
		return extensionUserManagement;
	}

	public boolean isForcedUserModeEnabled() {
		return forcedUserModeEnabled;
	}

	/**
	 * Sets the forced user for a context.
	 * 
	 * @param contextId the context id
	 * @param user the user
	 */
	public void setForcedUser(int contextId, User user) {
		if (user != null)
			this.contextForcedUsersMap.put(contextId, user);
		else
			this.contextForcedUsersMap.remove(contextId);
		this.updateForcedUserModeToggleButtonState();
	}

	/**
	 * Sets the forced user for a context, based on the user id.
	 * 
	 * @param contextId the context id
	 * @param userId the user id
	 * @throws IllegalStateException if no user was found that matches the provided id.
	 */
	public void setForcedUser(int contextId, int userId) throws IllegalStateException {
		User user = getUserManagementExtension().getContextUserAuthManager(contextId).getUserById(userId);
		if (user == null)
			throw new IllegalStateException("No user matching the provided id was found.");
		setForcedUser(contextId, user);
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
	public int getOrder() {
		// Make sure we load this extension after the user management extension so that we hook
		// after it so that we register as a ContextData factory later so that our loadContextData
		// is called after the Users' Extension so that the forced user was already loaded after a
		// session loading
		return ExtensionUserManagement.EXTENSION_ORDER + 10;
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public void discardContexts() {
		this.contextForcedUsersMap.clear();
		this.contextPanelsMap.clear();
		// Make sure the status of the toggle button is properly updated when changing the session
		updateForcedUserModeToggleButtonState();
	}

	@Override
	public void discardContext(Context ctx) {
		this.contextForcedUsersMap.remove(ctx.getIndex());
		this.contextPanelsMap.remove(ctx.getIndex());
		// Make sure the status of the toggle button is properly updated when changing the session
		updateForcedUserModeToggleButtonState();
	}

	@Override
	public int getListenerOrder() {
		// Later so any modifications or requested users are visible
		return 9998;
	}

	@Override
	public void onHttpRequestSend(HttpMessage msg, int initiator, HttpSender sender) {
		if (!forcedUserModeEnabled || msg.getResponseBody() == null || msg.getRequestHeader().isImage()
				|| (initiator == HttpSender.AUTHENTICATION_INITIATOR || initiator == HttpSender.CHECK_FOR_UPDATES_INITIATOR)) {
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

		if (requestingUser == null || !requestingUser.isEnabled())
			return;

		if (log.isDebugEnabled()) {
			log.debug("Modifying request message (" + msg.getRequestHeader().getURI() + ") to match user: "
					+ requestingUser);
		}
		msg.setRequestingUser(requestingUser);
	}

	@Override
	public void onHttpResponseReceive(HttpMessage msg, int initiator, HttpSender sender) {
		// Nothing to do
	}

	@Override
	public void loadContextData(Session session, Context context) {
		try {
			// Load the forced user id for this context
			List<String> forcedUserS = session.getContextDataStrings(context.getIndex(),
					RecordContext.TYPE_FORCED_USER_ID);
			if (forcedUserS != null && forcedUserS.size() > 0) {
				int forcedUserId = Integer.parseInt(forcedUserS.get(0));
				setForcedUser(context.getIndex(), forcedUserId);
			}
		} catch (Exception e) {
			log.error("Unable to load forced user.", e);
		}
	}

	@Override
	public void persistContextData(Session session, Context context) {
		try {
			// Save only if we have anything to save
			if (getForcedUser(context.getIndex()) != null) {
				session.setContextData(context.getIndex(), RecordContext.TYPE_FORCED_USER_ID,
						Integer.toString(getForcedUser(context.getIndex()).getId()));
				// Note: Do not persist whether the 'Forced User Mode' is enabled as there's no need
				// for this and the mode can be easily enabled/disabled directly
			} else {
				// If we don't have a forced user, force deletion of any previous values
				session.clearContextDataForType(context.getIndex(), RecordContext.TYPE_FORCED_USER_ID);
			}
		} catch (Exception e) {
			log.error("Unable to persist forced user.", e);
		}
	}

	@Override
	public void exportContextData(Context ctx, Configuration config) {
		User user = getForcedUser(ctx.getIndex());
		if (user != null) {
			config.setProperty("context.forceduser", user.getId());
		} else {
			config.setProperty("context.forceduser", -1);
		}
	}

	@Override
	public void importContextData(Context ctx, Configuration config) {
		int id = config.getInt("context.forceduser");
		if (id >= 0) {
			this.setForcedUser(ctx.getIndex(), id);
		}
	}


	/**
	 * No database tables used, so all supported
	 */
	@Override
	public boolean supportsDb(String type) {
    	return true;
    }
}

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

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.session.SessionManagementMethod;
import org.zaproxy.zap.userauth.session.SessionManagementMethodType;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The Context Panel shown for configuring a Context's session management method.
 */
public class ContextSessionManagementPanel extends AbstractContextPropertiesPanel {

	/** The Constant PANEL NAME. */
	private static final String PANEL_NAME = Constant.messages.getString("sessionmanagement.panel.title");

	private static final Logger log = Logger.getLogger(ContextSessionManagementPanel.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6125457981814742851L;

	/** The extension. */
	private ExtensionSessionManagement extension;

	/** The context. */
	private Context uiCommonContext;
	
	/** The context id. */
	private int contextId;

	/** The session management methods combo box. */
	private JComboBox<SessionManagementMethodType<?>> sessionManagementMethodsComboBox;

	/** The session management method status panel. */
	private JPanel sessionManagementMethodStatusPanel;

	private SessionManagementMethod selectedMethod;

	/**
	 * Instantiates a new context session management panel.
	 * 
	 * @param extension the extension
	 * @param context the context
	 */
	public ContextSessionManagementPanel(ExtensionSessionManagement extension, Context context) {
		super();
		this.contextId = context.getIndex();
		this.extension = extension;
		initialize();
	}

	/**
	 * Initialize the panel.
	 */
	private void initialize() {
		this.setLayout(new CardLayout());
		this.setName(contextId + ": " + PANEL_NAME);
		this.setLayout(new GridBagLayout());

		this.add(new JLabel(Constant.messages.getString("sessionmanagement.panel.label.description")),
				LayoutHelper.getGBC(0, 0, 1, 1.0D, new Insets(0, 5, 0, 5)));

		// Session management combo box
		this.add(new JLabel(Constant.messages.getString("sessionmanagement.panel.label.typeSelect")),
				LayoutHelper.getGBC(0, 2, 1, 1.0D, new Insets(20, 5, 5, 5)));
		this.add(getSessionManagementMethodsComboBox(),
				LayoutHelper.getGBC(0, 3, 1, 1.0D, new Insets(0, 5, 0, 5)));

		// Padding
		this.add(new JLabel(), LayoutHelper.getGBC(0, 99, 1, 1.0D, 1.0D));
	}

	/**
	 * Builds a panel showing that no method configuration is needed.
	 * 
	 * @return the no method configuration panel
	 */
	private JPanel buildNoMethodConfigurationPanel() {
		// No caching is done as their is no need for a 'lifetime' reference to one as most likely
		// will either not be used again, or is the displayed panel.
		JPanel noConfigPanel = new JPanel();
		noConfigPanel.add(
				new JLabel(Constant.messages.getString("sessionmanagement.panel.label.noConfigPanel")),
				getMethodPanelConstraints());

		return noConfigPanel;
	}

	/**
	 * Gets the method's panel grid bag constraints.
	 * 
	 * @return the method panel constraints
	 */
	private GridBagConstraints getMethodPanelConstraints() {
		return LayoutHelper.getGBC(0, 4, 1, 1.0D, 1.0, GridBagConstraints.HORIZONTAL,
				new Insets(10, 2, 10, 4));
	}

	/**
	 * Changes the shown method's configuration panel (used to display brief info about the method
	 * and configure it) with a new one. If {@code null} is provided as a parameter, a message is
	 * shown stating that no configuration is need for the currently selected method.
	 * 
	 * @param newPanel the new panel, if any, or null to just remove the existing one and show the
	 *            panel for no configuration (as returned by
	 *            {@link ContextSessionManagementPanel#buildNoMethodConfigurationPanel()}).
	 */
	private void changeMethodConfigPanel(JPanel newPanel) {
		// Remove oldPanel, if it exists
		if (sessionManagementMethodStatusPanel != null)
			this.remove(sessionManagementMethodStatusPanel);

		// if there's no new panel to replace it with
		if (newPanel == null)
			this.add(buildNoMethodConfigurationPanel(), getMethodPanelConstraints());
		else
			// if there's a panel, place it there
			this.add(newPanel, getMethodPanelConstraints());

		this.revalidate();
		this.sessionManagementMethodStatusPanel = newPanel;
	}

	/**
	 * Gets the session management methods combo box.
	 * 
	 * @return the session management methods combo box
	 */
	protected JComboBox<SessionManagementMethodType<?>> getSessionManagementMethodsComboBox() {
		if (sessionManagementMethodsComboBox == null) {
			Vector<SessionManagementMethodType<?>> methods = new Vector<>(
					extension.getSessionManagementMethodTypes());
			sessionManagementMethodsComboBox = new JComboBox<>(methods);
			sessionManagementMethodsComboBox.setSelectedItem(null);

			
			// TODO: Save the changes in the UI common Context
			
			// Prepare the listener for the change of selection
			sessionManagementMethodsComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						// Prepare the new session management method
						log.debug("Selected new Session Management type: " + e.getItem());
						SessionManagementMethodType type = ((SessionManagementMethodType) e.getItem());
						// If no session management method was previously selected or it's a
						// different class, create it now
						if (selectedMethod == null || !type.isFactoryForMethod(selectedMethod.getClass())) {
							// Create the new session management method
							selectedMethod = type.createSessionManagementMethod(contextId);
						}

						// Show the status panel and configuration button, if needed
						if (type.hasOptionsPanel()) {
							changeMethodConfigPanel(type.buildOptionsPanel(selectedMethod, uiCommonContext));
						} else {
							changeMethodConfigPanel(null);
						}
					}
				}
			});
		}
		return sessionManagementMethodsComboBox;

	}

	@Override
	public String getHelpIndex() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initContextData(Session session, Context uiCommonContext) {
		this.uiCommonContext=uiCommonContext;
		selectedMethod = uiCommonContext.getSessionManagementMethod();
		if (log.isDebugEnabled())
			log.debug("Initializing configuration panel for session management method: " + selectedMethod
					+ " for context " + uiCommonContext.getName());

		// If something was already configured, find the type and set the UI accordingly
		if (selectedMethod != null) {
			// Select what needs to be selected
			for (SessionManagementMethodType<?> type : extension.getSessionManagementMethodTypes())
				if (type.isFactoryForMethod(selectedMethod.getClass())) {
					// Selecting the type here will also force the selection listener to run and
					// change the config panel accordingly
					getSessionManagementMethodsComboBox().setSelectedItem(type);
					break;
				}
		}
	}

	@Override
	public void validateContextData(Session session) throws Exception {
		if (selectedMethod == null)
			throw new IllegalStateException(
					"A valid session management method has to be selected for Context " + uiCommonContext.getName());
		if (!selectedMethod.isConfigured())
			throw new IllegalStateException(
					"The session management method has not been properly configured for Context "
							+ uiCommonContext.getName());
	}

	@Override
	public void saveContextData(Session session) throws Exception {
		session.getContext(contextId).setSessionManagementMethod(selectedMethod);
		
	}

	@Override
	public int getContextIndex() {
		return contextId;
	}

}

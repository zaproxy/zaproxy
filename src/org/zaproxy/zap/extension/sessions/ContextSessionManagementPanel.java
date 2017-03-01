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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.session.AbstractSessionManagementMethodOptionsPanel;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.SessionManagementMethodType;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The Context Panel shown for configuring a Context's session management method.
 */
public class ContextSessionManagementPanel extends AbstractContextPropertiesPanel {

	/** The Constant PANEL NAME. */
	private static final String PANEL_NAME = Constant.messages.getString("sessionmanagement.panel.title");
	private static final String CONFIG_NOT_NEEDED = Constant.messages
			.getString("sessionmanagement.panel.label.noConfigPanel");

	private static final Logger log = Logger.getLogger(ContextSessionManagementPanel.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6125457981814742851L;

	/** The extension. */
	private ExtensionSessionManagement extension;

	/** The session management methods combo box. */
	private JComboBox<SessionManagementMethodType> sessionManagementMethodsComboBox;

	/** The selected method that is being configured. */
	private SessionManagementMethod selectedMethod;

	/** The shown method type. */
	private SessionManagementMethodType shownMethodType;

	/** The shown method's config panel. */
	private AbstractSessionManagementMethodOptionsPanel shownConfigPanel;

	/** The container panel for the configuration. */
	private JPanel configContainerPanel;

	/**
	 * Instantiates a new context session management panel.
	 * 
	 * @param extension the extension
	 * @param context the context
	 */
	public ContextSessionManagementPanel(ExtensionSessionManagement extension, Context context) {
		super(context.getIndex());
		this.extension = extension;
		initialize();
	}

	/**
	 * Initialize the panel.
	 */
	private void initialize() {
		this.setLayout(new CardLayout());
		this.setName(getContextIndex() + ": " + PANEL_NAME);
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(2, 2, 2, 2));

		this.add(new JLabel(Constant.messages.getString("sessionmanagement.panel.label.description")),
				LayoutHelper.getGBC(0, 0, 1, 1.0D));

		// Session management combo box
		this.add(new JLabel(Constant.messages.getString("sessionmanagement.panel.label.typeSelect")),
				LayoutHelper.getGBC(0, 1, 1, 1.0D, new Insets(20, 0, 5, 5)));
		this.add(getSessionManagementMethodsComboBox(), LayoutHelper.getGBC(0, 2, 1, 1.0D));

		// Method config panel container
		this.add(getConfigContainerPanel(), LayoutHelper.getGBC(0, 3, 1, 1.0d, new Insets(10, 0, 10, 0)));

		// Padding
		this.add(new JLabel(), LayoutHelper.getGBC(0, 99, 1, 1.0D, 1.0D));
	}

	/**
	 * Changes the shown method's configuration panel (used to display brief info about the method
	 * and configure it) with a new one, based on a new method type. If {@code null} is provided as
	 * a parameter, nothing is shown. If the provided method type does not require configuration, a
	 * simple message is shown stating that no configuration is needed.
	 * 
	 * @param newMethodType the new method type. If null, nothing is shown.
	 */
	private void changeMethodConfigPanel(SessionManagementMethodType newMethodType) {
		// If there's no new method, don't display anything
		if (newMethodType == null) {
			getConfigContainerPanel().removeAll();
			getConfigContainerPanel().setVisible(false);
			this.shownMethodType = null;
			this.shownConfigPanel = null;
			return;
		}

		// If a panel of the correct type is already shown, do nothing
		if (shownMethodType != null && newMethodType.getClass().equals(shownMethodType.getClass())) {
			return;
		}

		log.debug("Creating new panel for configuring: " + newMethodType.getName());
		this.getConfigContainerPanel().removeAll();

		// show the panel according to whether the session management type needs configuration
		if (newMethodType.hasOptionsPanel()) {
			this.shownConfigPanel = newMethodType.buildOptionsPanel(getUISharedContext());
			getConfigContainerPanel().add(this.shownConfigPanel, BorderLayout.CENTER);
		} else {
			this.shownConfigPanel = null;
			getConfigContainerPanel().add(new JLabel("<html><p>" + CONFIG_NOT_NEEDED + "</p></html>"),
					BorderLayout.CENTER);
		}
		this.shownMethodType = newMethodType;

		this.getConfigContainerPanel().setVisible(true);
		this.getConfigContainerPanel().revalidate();
	}

	/**
	 * Gets the session management methods combo box.
	 * 
	 * @return the session management methods combo box
	 */
	protected JComboBox<SessionManagementMethodType> getSessionManagementMethodsComboBox() {
		if (sessionManagementMethodsComboBox == null) {
			Vector<SessionManagementMethodType> methods = new Vector<>(
					extension.getSessionManagementMethodTypes());
			sessionManagementMethodsComboBox = new JComboBox<>(methods);
			sessionManagementMethodsComboBox.setSelectedItem(null);

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
						if (selectedMethod == null || !type.isTypeForMethod(selectedMethod)) {
							// Create the new session management method
							selectedMethod = type.createSessionManagementMethod(getContextIndex());
						}

						// Show the status panel and configuration button, if needed
						changeMethodConfigPanel(type);
						if (type.hasOptionsPanel())
							shownConfigPanel.bindMethod(selectedMethod);
					}
				}
			});
		}
		return sessionManagementMethodsComboBox;

	}

	/**
	 * Gets the configuration container panel.
	 * 
	 * @return the config container panel
	 */
	private JPanel getConfigContainerPanel() {
		if (configContainerPanel == null) {
			configContainerPanel = new JPanel(new BorderLayout());
			configContainerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
					Constant.messages.getString("sessionmanagement.panel.config.title"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, 
					FontUtils.getFont(FontUtils.Size.standard), java.awt.Color.black));
		}
		return configContainerPanel;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.contexts";
	}

	@Override
	public void initContextData(Session session, Context uiSharedContext) {
		selectedMethod = uiSharedContext.getSessionManagementMethod();
		if (log.isDebugEnabled())
			log.debug("Initializing configuration panel for session management method: " + selectedMethod
					+ " for context " + uiSharedContext.getName());

		// If something was already configured, find the type and set the UI accordingly
		if (selectedMethod != null) {
			// If the proper type is already selected, just rebind the data
			if (shownMethodType != null && shownMethodType.isTypeForMethod(selectedMethod)) {
				if (shownMethodType.hasOptionsPanel())
					shownConfigPanel.bindMethod(selectedMethod);
				return;
			}

			// Select what needs to be selected
			for (SessionManagementMethodType type : extension.getSessionManagementMethodTypes())
				if (type.isTypeForMethod(selectedMethod)) {
					// Selecting the type here will also force the selection listener to run and
					// change the config panel accordingly
					getSessionManagementMethodsComboBox().setSelectedItem(type);
					break;
				}
		}
	}

	@Override
	public void validateContextData(Session session) throws IllegalStateException {
		if (selectedMethod == null)
			throw new IllegalStateException(
					"A valid session management method has to be selected for Context "
							+ getUISharedContext().getName());
		if (shownConfigPanel != null)
			shownConfigPanel.validateFields();
	}

	@Override
	public void saveContextData(Session session) throws Exception {
		if (shownConfigPanel != null)
			shownConfigPanel.saveMethod();
		session.getContext(getContextIndex()).setSessionManagementMethod(selectedMethod);

	}

	@Override
	public void saveTemporaryContextData(Context uiSharedContext) {
		if (shownConfigPanel != null)
			shownConfigPanel.saveMethod();
		uiSharedContext.setSessionManagementMethod(selectedMethod);
	}

}

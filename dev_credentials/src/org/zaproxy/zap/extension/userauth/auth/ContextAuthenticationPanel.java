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
package org.zaproxy.zap.extension.userauth.auth;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.authentication.AbstractAuthenticationMethodOptionsPanel;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethod;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethodType;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.SummaryAndConfigPanel;

/**
 * The Context Panel shown for configuring a Context's authentication methods.
 */
public class ContextAuthenticationPanel extends AbstractContextPropertiesPanel {

	private static final String CONFIG_NOT_NEEDED = Constant.messages
			.getString("sessionmanagement.panel.label.noConfigPanel");

	private static final String METHOD_NOT_CONFIGURED = Constant.messages
			.getString("authentication.panel.label.notConfigured");

	private static final String SUMMARY_TITLE = Constant.messages
			.getString("authentication.panel.label.summaryTitle");

	private static final String CONFIG_BUTTON_LABEL = Constant.messages
			.getString("authentication.panel.button.config");

	/** The Constant PANEL NAME. */
	private static final String PANEL_NAME = Constant.messages.getString("authentication.panel.title");

	private static final Logger log = Logger.getLogger(ContextAuthenticationPanel.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -898084998156067286L;

	/** The extension. */
	private ExtensionAuthentication extension;

	/** The context. */
	private int contextId;

	/** The ui common context. */
	private Context uiCommonContext;

	/** The authentication method types combo box. */
	private JComboBox<AuthenticationMethodType<?>> authenticationMethodsComboBox;

	/** The authentication method summary panel. */
	private SummaryAndConfigPanel authenticationMethodSummaryPanel;

	/** The selected authentication method. */
	private AuthenticationMethod<?> selectedAuthenticationMethod;

	private AuthenticationMethodType<?> shownAuthMethodType;

	private AbstractAuthenticationMethodOptionsPanel<?> authConfigPanel;

	private JPanel configContainerPanel;

	/**
	 * Instantiates a new context authentication configuration panel.
	 * 
	 * @param extension the extension
	 * @param context the context
	 */
	public ContextAuthenticationPanel(ExtensionAuthentication extension, Context context) {
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
		this.setBorder(new EmptyBorder(2, 2, 2, 2));

		this.add(new JLabel(Constant.messages.getString("authentication.panel.label.description")),
				LayoutHelper.getGBC(0, 0, 2, 1.0D));

		// Method type combo box
		this.add(new JLabel(Constant.messages.getString("authentication.panel.label.typeSelect")),
				LayoutHelper.getGBC(0, 2, 2, 1.0D, new Insets(20, 0, 5, 0)));
		this.add(getAuthenticationMethodsComboBox(), LayoutHelper.getGBC(0, 3, 2, 1.0D));

		// Method config panel container
		this.add(getConfigContainerPanel(), LayoutHelper.getGBC(0, 4, 2, 1.0d, new Insets(10, 0, 10, 0)));

		// Padding
		this.add(new JLabel(), LayoutHelper.getGBC(0, 99, 1, 1.0D, 1.0D));
	}

	/**
	 * Changes the shown method's configuration panel (used to display brief info about the method
	 * and configure it) with a new one, based on a new method type. If {@code null} is provided as
	 * a parameter, nothing is shown. If the provided method type does not require configuration, a
	 * simple message is shown stating that no configuration is needed.
	 * 
	 * @param newMethodType the new method type. If null, nothing is shown. If does not require
	 *            config, a message is shown, on a panel returned by
	 *            {@link ContextAuthenticationPanel#getNoMethodConfigurationPanel()}).
	 */
	private void changeMethodConfigPanel(AuthenticationMethodType<?> newMethodType) {
		// If there's no new method, don't display anything
		if (newMethodType == null) {
			getConfigContainerPanel().removeAll();
			getConfigContainerPanel().setVisible(false);
			this.shownAuthMethodType = null;
			return;
		}

		// If a panel of the correct type is already shown, do nothing
		if (shownAuthMethodType != null && newMethodType.getClass().equals(shownAuthMethodType.getClass())) {
			return;
		}

		log.info("Creating new panel for configuring: " + newMethodType.getName());
		this.getConfigContainerPanel().removeAll();

		// show the panel according to whether the authentication type needs configuration
		if (newMethodType.hasOptionsPanel()) {
			authConfigPanel = newMethodType.buildOptionsPanel(uiCommonContext);
			getConfigContainerPanel().add(authConfigPanel, BorderLayout.CENTER);
		} else {
			authConfigPanel = null;
			getConfigContainerPanel().add(new JLabel("<html><p>" + CONFIG_NOT_NEEDED + "</p></html>"),
					BorderLayout.CENTER);
		}

		this.getConfigContainerPanel().setVisible(true);
		this.getConfigContainerPanel().revalidate();
	}

	/**
	 * Gets the authentication method types combo box.
	 * 
	 * @return the authentication methods combo box
	 */
	protected JComboBox<AuthenticationMethodType<?>> getAuthenticationMethodsComboBox() {
		if (authenticationMethodsComboBox == null) {
			Vector<AuthenticationMethodType<?>> methods = new Vector<>(
					extension.getAuthenticationMethodTypes());
			authenticationMethodsComboBox = new JComboBox<>(methods);
			authenticationMethodsComboBox.setSelectedItem(null);

			// Prepare the listener for the change of selection
			authenticationMethodsComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						// Prepare the new authentication method
						log.debug("Selected new Authentication type: " + e.getItem());
						AuthenticationMethodType type = ((AuthenticationMethodType) e.getItem());

						// If no authentication method was previously selected or it's a different
						// class, create a new authentication method object
						if (selectedAuthenticationMethod == null
								|| !type.isFactoryForMethod(selectedAuthenticationMethod.getClass())) {
							selectedAuthenticationMethod = type.createAuthenticationMethod(contextId);
							uiCommonContext.setAuthenticationMethod(selectedAuthenticationMethod);
						}

						// Show the configuration panel
						changeMethodConfigPanel(type);
						if (type.hasOptionsPanel()) {
							authConfigPanel
									.bindMethod((AuthenticationMethod<?>) selectedAuthenticationMethod);
						}
					}
				}
			});
		}
		return authenticationMethodsComboBox;
	}

	private JPanel getConfigContainerPanel() {
		if (configContainerPanel == null) {
			configContainerPanel = new JPanel(new BorderLayout());
			configContainerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
					Constant.messages.getString("authentication.dialog.title"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog",
							java.awt.Font.BOLD, 12), java.awt.Color.black));
			configContainerPanel.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					log.info("Unfocused");

				}

				@Override
				public void focusGained(FocusEvent e) {
					log.info("Focused");

				}
			});
		}
		return configContainerPanel;
	}

	@Override
	public String getHelpIndex() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initContextData(Session session, Context uiCommonContext) {
		this.uiCommonContext = uiCommonContext;
		selectedAuthenticationMethod = uiCommonContext.getAuthenticationMethod();
		if (log.isDebugEnabled())
			log.debug("Initializing configuration panel for authentication method: "
					+ selectedAuthenticationMethod + " for context " + uiCommonContext.getName());

		// If something was already configured, find the type and set the UI accordingly
		if (selectedAuthenticationMethod != null) {
			// Select what needs to be selected
			for (AuthenticationMethodType type : extension.getAuthenticationMethodTypes())
				if (type.isFactoryForMethod(selectedAuthenticationMethod.getClass())) {
					// Selecting the type here will also force the selection listener to run and
					// change the config panel accordingly
					getAuthenticationMethodsComboBox().setSelectedItem(type);
					break;
				}
		}
	}

	@Override
	public void validateContextData(Session session) throws Exception {
		if (authConfigPanel != null && !authConfigPanel.validateFields())
			throw new IllegalStateException(
					"The selected authentication method has not been properly configured for Context "
							+ uiCommonContext.getName());
	}

	@Override
	public void saveContextData(Session session) throws Exception {
		authConfigPanel.saveMethod();
		session.getContext(contextId).setAuthenticationMethod(selectedAuthenticationMethod);
	}

	@Override
	public int getContextIndex() {
		return contextId;
	}

}

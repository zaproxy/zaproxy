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
package org.zaproxy.zap.extension.userauth;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.userauth.User;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethod;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethodFactory;
import org.zaproxy.zap.userauth.session.SessionManagementMethod;
import org.zaproxy.zap.userauth.session.SessionManagementMethodFactory;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractFormDialog;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The Dialog used for adding a new user.
 */
public class DialogAddUser extends AbstractFormDialog {

	private static final String AUTHENTICATION_METHOD_NOT_CONFIGURED = Constant.messages
			.getString("userauth.user.dialog.add.authentication.notconfigured");

	private static final String SESSION_MANAGEMENT_METHOD_NOT_CONFIGURED = Constant.messages
			.getString("userauth.user.dialog.add.session.notconfigured");

	protected static final Logger log = Logger.getLogger(DialogAddUser.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7369728566299090079L;

	private static final String DIALOG_TITLE = Constant.messages.getString("userauth.user.dialog.add.title");

	private static final String CONFIRM_BUTTON_LABEL = Constant.messages
			.getString("userauth.user.dialog.add.button.confirm");

	protected ExtensionUserAuthentication extension;

	@Override
	protected boolean validateFields() {
		// TODO: Check name collision
		if (selectedAuthenticationMethod == null || !selectedAuthenticationMethod.isConfigured())
			return false;
		if (selectedSessionManagementMethod == null || !selectedSessionManagementMethod.isConfigured())
			return false;
		return true;
	}

	@Override
	protected void performAction() {
		user = new User(this.contextId, nameTextField.getText());
		user.setEnabled(enabledCheckBox.isSelected());
		user.setAuthenticationMethod(selectedAuthenticationMethod);
		user.setSessionManagementMethod(selectedSessionManagementMethod);
		if (log.isInfoEnabled())
			log.info("Saving user: " + user);
	}

	@Override
	protected void clearFields() {
		nameTextField.setText("");
		nameTextField.discardAllEdits();
		sessionManagementMethodsComboBox.setSelectedItem(null);
		authenticationMethodsComboBox.setSelectedItem(null);

		getAuthenticationMethodStatusPanel().setVisible(false);
		authenticationMethodStatusLabel.setText(AUTHENTICATION_METHOD_NOT_CONFIGURED);

		getSessionManagementMethodStatusPanel().setVisible(false);
		sessionManagementMethodStatusLabel.setText(SESSION_MANAGEMENT_METHOD_NOT_CONFIGURED);
	}

	@Override
	protected void init() {
		log.info("Initializing add user dialog...");
		getNameTextField().setText("");
		getEnabledCheckBox().setSelected(true);
		getAuthenticationMethodStatusPanel().setVisible(false);
		user = null;
		selectedAuthenticationMethod = null;
		selectedSessionManagementMethod = null;
	}

	private ZapTextField nameTextField;
	private JCheckBox enabledCheckBox;

	protected User user;

	protected AuthenticationMethod selectedAuthenticationMethod;
	protected SessionManagementMethod selectedSessionManagementMethod;

	private int contextId;
	private JPanel fieldsPanel;

	private JPanel authenticationMethodStatusPanel;
	private JPanel sessionManagementMethodStatusPanel;

	private JComboBox<SessionManagementMethodFactory<?>> sessionManagementMethodsComboBox;

	private JComboBox<AuthenticationMethodFactory<?>> authenticationMethodsComboBox;

	private JLabel authenticationMethodStatusLabel;
	private JLabel sessionManagementMethodStatusLabel;

	public DialogAddUser(Dialog owner, ExtensionUserAuthentication extension, int contextId) {
		super(owner, DIALOG_TITLE, false);
		this.contextId = contextId;
		this.extension = extension;
		initView();
	}

	public DialogAddUser(Dialog owner, ExtensionUserAuthentication extension, String title, int contextId) {
		super(owner, title, false);
		this.contextId = contextId;
		this.extension = extension;
		initView();
	}

	@Override
	protected JPanel getFieldsPanel() {
		if (fieldsPanel == null) {
			fieldsPanel = new JPanel();

			fieldsPanel.setLayout(new GridBagLayout());
			fieldsPanel.setName("Dialog Add User");
			Insets insets = new Insets(4, 8, 2, 4);

			JLabel nameLabel = new JLabel(
					Constant.messages.getString("userauth.user.dialog.add.field.label.name"));
			JLabel enabledLabel = new JLabel(
					Constant.messages.getString("userauth.user.dialog.add.field.label.enabled"));
			JLabel sessionManagementLabel = new JLabel(
					Constant.messages.getString("userauth.user.dialog.add.field.label.sessionmanagement"));
			JLabel authenticationLabel = new JLabel(
					Constant.messages.getString("userauth.user.dialog.add.field.label.authentication"));

			fieldsPanel.add(nameLabel, LayoutHelper.getGBC(0, 0, 1, 0.5D, insets));
			fieldsPanel.add(getNameTextField(), LayoutHelper.getGBC(1, 0, 1, 0.5D, insets));

			fieldsPanel.add(enabledLabel, LayoutHelper.getGBC(0, 1, 1, 0.5D, insets));
			fieldsPanel.add(getEnabledCheckBox(), LayoutHelper.getGBC(1, 1, 1, 0.5D, insets));

			fieldsPanel.add(new JSeparator(), LayoutHelper.getGBC(0, 2, 2, 0.5D, insets));
			fieldsPanel.add(sessionManagementLabel, LayoutHelper.getGBC(0, 3, 1, 0.5D, insets));
			fieldsPanel
					.add(getSessionManagementMethodsComboBox(), LayoutHelper.getGBC(1, 3, 1, 0.5D, insets));

			fieldsPanel
					.add(getSessionManagementMethodStatusPanel(), LayoutHelper.getGBC(0, 4, 2, 1D, insets));

			fieldsPanel.add(new JSeparator(), LayoutHelper.getGBC(0, 5, 2, 0.5D, insets));
			fieldsPanel.add(authenticationLabel, LayoutHelper.getGBC(0, 6, 1, 0.5D, insets));
			fieldsPanel.add(getAuthenticationMethodsComboBox(), LayoutHelper.getGBC(1, 6, 1, 0.5D, insets));

			fieldsPanel.add(getAuthenticationMethodStatusPanel(), LayoutHelper.getGBC(0, 7, 2, 1D, insets));
		}
		return fieldsPanel;
	}

	/**
	 * Gets the authentication method status panel that is used to display brief info about the
	 * authentication method and configure it.
	 * 
	 * @return the authentication method status panel
	 */
	protected JPanel getSessionManagementMethodStatusPanel() {
		if (authenticationMethodStatusPanel == null) {
			// Status label
			sessionManagementMethodStatusPanel = new JPanel(new BorderLayout());
			sessionManagementMethodStatusLabel = new JLabel(SESSION_MANAGEMENT_METHOD_NOT_CONFIGURED);
			sessionManagementMethodStatusPanel.add(sessionManagementMethodStatusLabel, BorderLayout.WEST);

			// Configure button
			JButton sessionManagementMethodConfigButton = new JButton("Configure");
			sessionManagementMethodStatusPanel.add(sessionManagementMethodConfigButton, BorderLayout.EAST);
			sessionManagementMethodConfigButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// If the configure button has been clicked, get the corresponding factory and
					// start the configure dialog
					SessionManagementMethodFactory<?> factory = (SessionManagementMethodFactory<?>) getSessionManagementMethodsComboBox()
							.getSelectedItem();
					@SuppressWarnings({ "rawtypes", "unchecked" })
					DialogConfigSessionManagementMethod dialog = new DialogConfigSessionManagementMethod(View
							.getSingleton().getOptionsDialog(null), "Edit", factory,
							selectedSessionManagementMethod, contextId);
					dialog.pack();
					dialog.setVisible(true);

					// Store the authentication method
					if (dialog.getSessionManagementMethod() != null) {
						SessionManagementMethod configuredMethod = dialog.getSessionManagementMethod();
						if (configuredMethod.isConfigured()) {
							sessionManagementMethodStatusLabel.setText(configuredMethod
									.getStatusDescription());
						}
						checkValidAndEnableConfirmButton();
					}

				}
			});
			sessionManagementMethodStatusPanel.setVisible(false);
		}
		return sessionManagementMethodStatusPanel;
	}

	/**
	 * Gets the authentication method status panel that is used to display brief info about the
	 * authentication method and configure it.
	 * 
	 * @return the authentication method status panel
	 */
	protected JPanel getAuthenticationMethodStatusPanel() {
		if (authenticationMethodStatusPanel == null) {
			// Status label
			authenticationMethodStatusPanel = new JPanel(new BorderLayout());
			authenticationMethodStatusLabel = new JLabel(AUTHENTICATION_METHOD_NOT_CONFIGURED);
			authenticationMethodStatusPanel.add(authenticationMethodStatusLabel, BorderLayout.WEST);

			// Configure button
			JButton authenticationMethodConfigButton = new JButton("Configure");
			authenticationMethodStatusPanel.add(authenticationMethodConfigButton, BorderLayout.EAST);
			authenticationMethodConfigButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// If the configure button has been clicked, get the corresponding factory and
					// start the configure dialog
					AuthenticationMethodFactory<?> factory = (AuthenticationMethodFactory<?>) getAuthenticationMethodsComboBox()
							.getSelectedItem();
					@SuppressWarnings({ "rawtypes", "unchecked" })
					DialogConfigAuthenticationMethod dialog = new DialogConfigAuthenticationMethod(View
							.getSingleton().getOptionsDialog(null), "Edit", factory,
							selectedAuthenticationMethod, contextId);
					dialog.pack();
					dialog.setVisible(true);

					// Store the authentication method
					if (dialog.getAuthenticationMethod() != null) {
						AuthenticationMethod configuredMethod = dialog.getAuthenticationMethod();
						if (configuredMethod.isConfigured()) {
							authenticationMethodStatusLabel.setText(configuredMethod.getStatusDescription());
						}
						checkValidAndEnableConfirmButton();
					}

				}
			});
			authenticationMethodStatusPanel.setVisible(false);
		}
		return authenticationMethodStatusPanel;
	}

	protected ZapTextField getNameTextField() {
		if (nameTextField == null) {
			nameTextField = new ZapTextField(25);
			nameTextField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void removeUpdate(DocumentEvent e) {
					checkValidAndEnableConfirmButton();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					checkValidAndEnableConfirmButton();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					checkValidAndEnableConfirmButton();
				}

			});
		}

		return nameTextField;
	}

	private void checkValidAndEnableConfirmButton() {
		setConfirmButtonEnabled(getNameTextField().getDocument().getLength() > 0
				&& getSessionManagementMethodsComboBox().getSelectedIndex() >= 0
				&& selectedAuthenticationMethod != null && selectedAuthenticationMethod.isConfigured());
	}

	protected JCheckBox getEnabledCheckBox() {
		if (enabledCheckBox == null) {
			enabledCheckBox = new JCheckBox();
		}

		return enabledCheckBox;
	}

	protected JComboBox<SessionManagementMethodFactory<?>> getSessionManagementMethodsComboBox() {
		if (sessionManagementMethodsComboBox == null) {
			Vector<SessionManagementMethodFactory<?>> methods = new Vector<>(
					extension.getSessionManagementMethodFactories());
			sessionManagementMethodsComboBox = new JComboBox<>(methods);
			sessionManagementMethodsComboBox.setSelectedItem(null);

			// Prepare the listener for the change of selection
			sessionManagementMethodsComboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						// Prepare the new session management method
						log.debug("Selected new Session Management method: " + e.getItem());
						SessionManagementMethodFactory<?> factory = ((SessionManagementMethodFactory<?>) e
								.getItem());
						// If no session management method was previously selected or it's a
						// different
						// class
						if (selectedSessionManagementMethod == null
								|| !factory.isFactoryForMethod(selectedSessionManagementMethod.getClass())) {
							selectedSessionManagementMethod = factory.buildSessionManagementMethod();
						}

						// Show the status panel and configuration button, if needed
						if (factory.hasOptionsPanel()) {
							getSessionManagementMethodStatusPanel().setVisible(true);
							if (selectedSessionManagementMethod.isConfigured()) {
								sessionManagementMethodStatusLabel.setText(selectedSessionManagementMethod
										.getStatusDescription());
							} else {
								sessionManagementMethodStatusLabel
										.setText(SESSION_MANAGEMENT_METHOD_NOT_CONFIGURED);
							}
						} else {
							getSessionManagementMethodStatusPanel().setVisible(false);
						}
						DialogAddUser.this.pack();
					}
					checkValidAndEnableConfirmButton();
				}
			});
		}
		return sessionManagementMethodsComboBox;

	}

	/**
	 * Gets the combo box used for selecting the type of authentication method.
	 * 
	 * @return the authentication methods combo box
	 */
	protected JComboBox<AuthenticationMethodFactory<?>> getAuthenticationMethodsComboBox() {
		if (authenticationMethodsComboBox == null) {
			Vector<AuthenticationMethodFactory<?>> methods = new Vector<>(
					extension.getAuthenticationMethodFactories());
			authenticationMethodsComboBox = new JComboBox<>(methods);
			authenticationMethodsComboBox.setSelectedItem(null);

			// Prepare the listener for the change of selection
			authenticationMethodsComboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						// Prepare the new authentication method
						log.debug("Selected new Authentication method: " + e.getItem());
						AuthenticationMethodFactory<?> factory = ((AuthenticationMethodFactory<?>) e
								.getItem());
						// If no authentication method was previously selected or it's a different
						// class
						if (selectedAuthenticationMethod == null
								|| !factory.isFactoryForMethod(selectedAuthenticationMethod.getClass())) {
							selectedAuthenticationMethod = factory.buildAuthenticationMethod();
						}

						// Show the status panel and configuration button, if needed
						if (factory.hasOptionsPanel()) {
							getAuthenticationMethodStatusPanel().setVisible(true);
							if (selectedAuthenticationMethod.isConfigured()) {
								authenticationMethodStatusLabel.setText(selectedAuthenticationMethod
										.getStatusDescription());
							} else {
								authenticationMethodStatusLabel.setText(AUTHENTICATION_METHOD_NOT_CONFIGURED);
							}
						} else {
							getAuthenticationMethodStatusPanel().setVisible(false);
						}
						DialogAddUser.this.pack();
					}
					checkValidAndEnableConfirmButton();
				}
			});
		}
		return authenticationMethodsComboBox;

	}

	@Override
	protected String getConfirmButtonLabel() {
		return CONFIRM_BUTTON_LABEL;
	}

	/**
	 * Gets the user defined in the dialog, if any.
	 * 
	 * @return the user, if correctly built or null, otherwise
	 */
	public User getUser() {
		return user;
	}

	public void clear() {
		this.user = null;
		this.selectedAuthenticationMethod = null;
		this.selectedSessionManagementMethod = null;
	}

}

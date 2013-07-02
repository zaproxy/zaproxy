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
import org.zaproxy.zap.userauth.UserAuthManager;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethodFactory;
import org.zaproxy.zap.userauth.authentication.ManualAuthenticationMethod;
import org.zaproxy.zap.userauth.session.SessionManagementMethodFactory;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractFormDialog;
import org.zaproxy.zap.view.LayoutHelper;

public class DialogAddUser extends AbstractFormDialog {

	private static final Logger log = Logger.getLogger(DialogAddUser.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7369728566299090079L;

	private static final String DIALOG_TITLE = Constant.messages.getString("userauth.user.dialog.add.title");

	private static final String CONFIRM_BUTTON_LABEL = Constant.messages
			.getString("userauth.user.dialog.add.button.confirm");

	@Override
	protected boolean validateFields() {
		// TODO: Check name collision
		return true;
	}

	@Override
	protected void performAction() {
		user = new User(this.contextId, nameTextField.getText());
		user.setEnabled(enabledCheckBox.isEnabled());
	}

	@Override
	protected void clearFields() {
		nameTextField.setText("");
		nameTextField.discardAllEdits();
		sessionManagementMethodsComboBox.setSelectedItem(null);
		authenticationMethodsComboBox.setSelectedItem(null);
	}

	@Override
	protected void init() {
		getNameTextField().setText("");
		getEnabledCheckBox().setSelected(true);
		user = null;
	}

	private ZapTextField nameTextField;
	private JCheckBox enabledCheckBox;

	protected User user;

	private int contextId;
	private JPanel fieldsPanel;

	private JPanel authenticationMethodStatusPanel;

	private JComboBox<SessionManagementMethodFactory<?>> sessionManagementMethodsComboBox;

	private JComboBox<AuthenticationMethodFactory<?>> authenticationMethodsComboBox;

	private JLabel authenticationMethodStatusLabel;

	public DialogAddUser(Dialog owner, int contextId) {
		super(owner, DIALOG_TITLE);
		this.contextId = contextId;
	}

	public DialogAddUser(Dialog owner, String title, int contextId) {
		super(owner, title);
		this.contextId = contextId;
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

			fieldsPanel.add(new JSeparator(), LayoutHelper.getGBC(0, 4, 2, 0.5D, insets));
			fieldsPanel.add(authenticationLabel, LayoutHelper.getGBC(0, 5, 1, 0.5D, insets));
			fieldsPanel.add(getAuthenticationMethodsComboBox(), LayoutHelper.getGBC(1, 5, 1, 0.5D, insets));

			fieldsPanel.add(getAuthenticationMethodStatusPanel(), LayoutHelper.getGBC(0, 6, 2, 1D, insets));
		}
		return fieldsPanel;
	}

	protected JPanel getAuthenticationMethodStatusPanel() {
		if (authenticationMethodStatusPanel == null) {
			authenticationMethodStatusPanel = new JPanel(new BorderLayout());
			authenticationMethodStatusLabel = new JLabel("<Authentication method not configured>");
			authenticationMethodStatusPanel.add(authenticationMethodStatusLabel, BorderLayout.WEST);
			JButton authenticationMethodConfigButton = new JButton("Configure");
			authenticationMethodStatusPanel.add(authenticationMethodConfigButton, BorderLayout.EAST);
			authenticationMethodConfigButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					AuthenticationMethodFactory<?> factory = (AuthenticationMethodFactory<?>) getAuthenticationMethodsComboBox()
							.getSelectedItem();
					DialogEditAuthenticationMethod dialog = new DialogEditAuthenticationMethod(View
							.getSingleton().getOptionsDialog(null), "Edit", factory, contextId);
					dialog.pack();
					dialog.setVisible(true);

					if (dialog.getAuthenticationMethod() != null)
						authenticationMethodStatusLabel.setText(dialog.getAuthenticationMethod()
								.getStatusDescription());
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
				&& getAuthenticationMethodsComboBox().getSelectedIndex() >= 0);
	}

	protected JCheckBox getEnabledCheckBox() {
		if (enabledCheckBox == null) {
			enabledCheckBox = new JCheckBox();
		}

		return enabledCheckBox;
	}

	protected JComboBox<SessionManagementMethodFactory<?>> getSessionManagementMethodsComboBox() {
		if (sessionManagementMethodsComboBox == null) {
			Vector<SessionManagementMethodFactory<?>> methods = new Vector<>(UserAuthManager.getInstance()
					.getSessionManagementMethodFactories());
			sessionManagementMethodsComboBox = new JComboBox<>(methods);
			sessionManagementMethodsComboBox.setSelectedItem(null);

			// Prepare the listener for the change of selection
			sessionManagementMethodsComboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					checkValidAndEnableConfirmButton();
					if (e.getStateChange() == ItemEvent.SELECTED) {
						log.debug("Selected new Sesion Management method: " + e.getItem());
						SessionManagementMethodFactory<?> factory = (SessionManagementMethodFactory<?>) e
								.getItem();
					}
				}
			});
		}
		return sessionManagementMethodsComboBox;

	}

	protected JComboBox<AuthenticationMethodFactory<?>> getAuthenticationMethodsComboBox() {
		if (authenticationMethodsComboBox == null) {
			Vector<AuthenticationMethodFactory<?>> methods = new Vector<>(UserAuthManager.getInstance()
					.getAuthenticationMethodFactories());
			authenticationMethodsComboBox = new JComboBox<>(methods);
			authenticationMethodsComboBox.setSelectedItem(null);

			// Prepare the listener for the change of selection
			authenticationMethodsComboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					checkValidAndEnableConfirmButton();
					if (e.getStateChange() == ItemEvent.SELECTED) {
						log.debug("Selected new Authentication method: " + e.getItem());
						getAuthenticationMethodStatusPanel().setVisible(true);
						DialogAddUser.this.pack();
					}
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
	}

}

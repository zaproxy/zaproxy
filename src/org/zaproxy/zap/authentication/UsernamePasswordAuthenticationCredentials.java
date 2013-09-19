package org.zaproxy.zap.authentication;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.apache.commons.codec.binary.Base64;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The credentials implementation for use in systems that require a username and password
 * combination for authentication.
 */
class UsernamePasswordAuthenticationCredentials implements AuthenticationCredentials {

	private static final String API_NAME = "UsernamePasswordAuthenticationCredentials";

	private static String FIELD_SEPARATOR = "~";
	String username;
	String password;

	@Override
	public boolean isConfigured() {
		return username != null && password != null;
	}

	@Override
	public String encode(String parentStringSeparator) {
		assert (!FIELD_SEPARATOR.equals(parentStringSeparator));
		StringBuilder out = new StringBuilder();
		out.append(Base64.encodeBase64String(username.getBytes())).append(FIELD_SEPARATOR);
		out.append(Base64.encodeBase64String(password.getBytes())).append(FIELD_SEPARATOR);
		return out.toString();
	}

	@Override
	public void decode(String encodedCredentials) {
		String[] pieces = encodedCredentials.split(FIELD_SEPARATOR);
		this.username = new String(Base64.decodeBase64(pieces[0]));
		this.password = new String(Base64.decodeBase64(pieces[1]));
	}

	/**
	 * The Options Panel used for configuring a {@link UsernamePasswordAuthenticationCredentials}.
	 */
	protected static class UsernamePasswordAuthenticationCredentialsOptionsPanel extends
			AbstractCredentialsOptionsPanel<UsernamePasswordAuthenticationCredentials> {

		private static final long serialVersionUID = 8881019014296985804L;

		private static final String USERNAME_LABEL = Constant.messages
				.getString("authentication.method.fb.credentials.field.label.user");
		private static final String PASSWORD_LABEL = Constant.messages
				.getString("authentication.method.fb.credentials.field.label.pass");

		private ZapTextField usernameTextField;
		private JPasswordField passwordTextField;

		public UsernamePasswordAuthenticationCredentialsOptionsPanel(
				UsernamePasswordAuthenticationCredentials credentials) {
			super(credentials);
			initialize();
		}

		private void initialize() {
			this.setLayout(new GridBagLayout());

			this.add(new JLabel(USERNAME_LABEL), LayoutHelper.getGBC(0, 0, 1, 0.0d));
			this.usernameTextField = new ZapTextField();
			if (this.getCredentials().username != null)
				this.usernameTextField.setText(this.getCredentials().username);
			this.add(this.usernameTextField, LayoutHelper.getGBC(1, 0, 1, 0.0d, new Insets(0, 4, 0, 0)));

			this.add(new JLabel(PASSWORD_LABEL), LayoutHelper.getGBC(0, 1, 1, 0.0d));
			this.passwordTextField = new JPasswordField();
			if (this.getCredentials().password != null)
				this.passwordTextField.setText(this.getCredentials().password);
			this.add(this.passwordTextField, LayoutHelper.getGBC(1, 1, 1, 1.0d, new Insets(0, 4, 0, 0)));
		}

		@Override
		public boolean validateFields() {
			if (usernameTextField.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this, Constant.messages
						.getString("authentication.method.fb.credentials.dialog.error.user.text"),
						Constant.messages.getString("authentication.method.fb.dialog.error.title"),
						JOptionPane.WARNING_MESSAGE);
				usernameTextField.requestFocusInWindow();
				return false;
			}
			return true;
		}

		@Override
		public void saveCredentials() {
			getCredentials().username = usernameTextField.getText();
			getCredentials().password = new String(passwordTextField.getPassword());
		}

	}

	@Override
	public ApiResponse getApiResponseRepresentation() {
		Map<String, String> values = new HashMap<>();
		values.put("type", API_NAME);
		values.put("username", username);
		values.put("password", password);
		return new ApiResponseSet("credentials", values);
	}
}
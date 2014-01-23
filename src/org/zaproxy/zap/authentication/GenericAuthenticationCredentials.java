package org.zaproxy.zap.authentication;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.view.DynamicFieldsPanel;

public class GenericAuthenticationCredentials implements AuthenticationCredentials {

	private static String FIELD_SEPARATOR = "~";
	private String[] paramNames;
	private Map<String, String> paramValues;

	public GenericAuthenticationCredentials(String[] paramNames) {
		super();
		this.paramNames = paramNames;
		this.paramValues = new HashMap<String, String>(paramNames.length);
	}

	public String get(String paramName) {
		return paramValues.get(paramName);
	}

	public String set(String paramName, String paramValue) {
		return paramValues.put(paramName, paramValue);
	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public String encode(String parentFieldSeparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void decode(String encodedCredentials) {
		// TODO Auto-generated method stub

	}

	@Override
	public ApiResponse getApiResponseRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * The Options Panel used for configuring a {@link GenericAuthenticationCredentials}.
	 */
	protected static class GenericAuthenticationCredentialsOptionsPanel extends
			AbstractCredentialsOptionsPanel<GenericAuthenticationCredentials> {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -6486907666459197059L;

		/** The dynamic fields panel. */
		private DynamicFieldsPanel fieldsPanel;

		public GenericAuthenticationCredentialsOptionsPanel(GenericAuthenticationCredentials credentials) {
			super(credentials);
			initialize();
		}

		/**
		 * Initialize the options panel, creating the views.
		 */
		private void initialize() {
			this.setLayout(new BorderLayout());

			this.fieldsPanel = new DynamicFieldsPanel(credentials.paramNames);
			this.fieldsPanel.bindFieldValues(this.credentials.paramValues);
			this.add(fieldsPanel, BorderLayout.CENTER);
		}

		@Override
		public boolean validateFields() {
			try {
				this.fieldsPanel.validateFields();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(),
						Constant.messages.getString("authentication.method.fb.dialog.error.title"),
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			return true;
		}

		@Override
		public void saveCredentials() {
			this.credentials.paramValues = new HashMap<>(this.fieldsPanel.getFieldValues());
		}
	}

}

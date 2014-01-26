package org.zaproxy.zap.authentication;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.utils.EncodingUtils;
import org.zaproxy.zap.view.DynamicFieldsPanel;

public class GenericAuthenticationCredentials implements AuthenticationCredentials {

	private String[] paramNames;
	private Map<String, String> paramValues;

	public GenericAuthenticationCredentials(String[] paramNames) {
		super();
		this.paramNames = paramNames;
		this.paramValues = new HashMap<String, String>(paramNames.length);
	}

	public String getParam(String paramName) {
		return paramValues.get(paramName);
	}

	public String setParam(String paramName, String paramValue) {
		return paramValues.put(paramName, paramValue);
	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public String encode(String parentFieldSeparator) {
		return EncodingUtils.mapToString(paramValues);
	}

	@Override
	public void decode(String encodedCredentials) {
		this.paramValues = EncodingUtils.stringToMap(encodedCredentials);
		this.paramNames = this.paramValues.keySet().toArray(new String[this.paramValues.size()]);
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

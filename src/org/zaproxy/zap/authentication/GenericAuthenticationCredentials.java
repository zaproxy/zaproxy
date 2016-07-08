package org.zaproxy.zap.authentication;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import net.sf.json.JSONObject;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.extension.users.UsersAPI;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;
import org.zaproxy.zap.utils.EncodingUtils;
import org.zaproxy.zap.view.DynamicFieldsPanel;

public class GenericAuthenticationCredentials implements AuthenticationCredentials {

	private static final String API_NAME = "GenericAuthenticationCredentials";

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
		Map<String, String> values = new HashMap<>(paramValues);
		values.put("type", API_NAME);
		return new ApiResponseSet("credentials", values);
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

	private static final String ACTION_SET_CREDENTIALS = "scriptBasedAuthenticationCredentials";
	private static final String PARAM_CONFIG_PARAMS = "authenticationCredentialsParams";

	/**
	 * Gets the api action for setting a {@link GenericAuthenticationCredentials} for an User.
	 * 
	 * @param methodType the method type for which this is called
	 * @return api action implementation
	 */
	public static ApiDynamicActionImplementor getSetCredentialsForUserApiAction(
			final AuthenticationMethodType methodType) {
		return new ApiDynamicActionImplementor(ACTION_SET_CREDENTIALS, null,
				new String[] { PARAM_CONFIG_PARAMS }) {

			@Override
			public void handleAction(JSONObject params) throws ApiException {
				Context context = ApiUtils.getContextByParamId(params, UsersAPI.PARAM_CONTEXT_ID);
				int userId = ApiUtils.getIntParam(params, UsersAPI.PARAM_USER_ID);
				// Make sure the type of authentication method is compatible
				if (!methodType.isTypeForMethod(context.getAuthenticationMethod()))
					throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER,
							"User's credentials should match authentication method type of the context: "
									+ context.getAuthenticationMethod().getType().getName());

				// NOTE: no need to check if extension is loaded as this method is called only if
				// the Users extension is loaded
				ExtensionUserManagement extensionUserManagement = (ExtensionUserManagement) Control
						.getSingleton().getExtensionLoader().getExtension(ExtensionUserManagement.NAME);
				User user = extensionUserManagement.getContextUserAuthManager(context.getIndex())
						.getUserById(userId);
				if (user == null)
					throw new ApiException(ApiException.Type.USER_NOT_FOUND, UsersAPI.PARAM_USER_ID);
				// Build and set the credentials
				GenericAuthenticationCredentials credentials = (GenericAuthenticationCredentials) context
						.getAuthenticationMethod().createAuthenticationCredentials();
				for (String paramName : credentials.paramNames)
					credentials.setParam(paramName, ApiUtils.getNonEmptyStringParam(params, paramName));
				user.setAuthenticationCredentials(credentials);
			}
		};
	}

}

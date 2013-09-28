package org.zaproxy.zap.authentication;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.authentication.UsernamePasswordAuthenticationCredentials.UsernamePasswordAuthenticationCredentialsOptionsPanel;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.authentication.AuthenticationAPI;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;
import org.zaproxy.zap.utils.ZapPortNumberSpinner;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The implementation for an {@link AuthenticationMethodType} where the Users are authenticated
 * through HTTP Authentication.
 * 
 * @see {@link http://www.w3.org/Protocols/HTTP/1.0/spec.html#AA}
 */
public class HttpAuthenticationMethodType extends AuthenticationMethodType {

	private static final Logger log = Logger.getLogger(HttpAuthenticationMethodType.class);

	/** The unique identifier of the method. */
	private static final int METHOD_IDENTIFIER = 3;

	/** The human readable Authentication method's name. */
	private static final String METHOD_NAME = Constant.messages.getString("authentication.method.http.name");

	/** The Authentication method's name used in the API. */
	private static final String API_METHOD_NAME = "httpAuthentication";

	/**
	 * The implementation for an {@link AuthenticationMethodType} where the Users are authenticated
	 * through HTTP Authentication.
	 */
	private static class HttpAuthenticationMethod extends AuthenticationMethod {

		public HttpAuthenticationMethod() {
			super();
		}

		private String hostname;
		private int port = 80;
		private String realm;

		@Override
		public boolean isConfigured() {
			return hostname != null && !hostname.isEmpty() && realm != null && !realm.isEmpty();
		}

		@Override
		protected AuthenticationMethod duplicate() {
			HttpAuthenticationMethod method = new HttpAuthenticationMethod();
			method.hostname = this.hostname;
			method.port = this.port;
			method.realm = this.realm;
			return method;
		}

		@Override
		public AuthenticationCredentials createAuthenticationCredentials() {
			return new UsernamePasswordAuthenticationCredentials();
		}

		@Override
		public AuthenticationMethodType getType() {
			return new HttpAuthenticationMethodType();
		}

		@Override
		public WebSession authenticate(SessionManagementMethod sessionManagementMethod,
				AuthenticationCredentials credentials, User user)
				throws UnsupportedAuthenticationCredentialsException {
			// Nothing to do?
			return null;
		}

		@Override
		public ApiResponse getApiResponseRepresentation() {
			Map<String, String> values = new HashMap<>();
			values.put("methodName", API_METHOD_NAME);
			values.put("host", this.hostname);
			values.put("port", Integer.toString(this.port));
			values.put("realm", this.realm);
			return new ApiResponseSet("method", values);
		}

	}

	/**
	 * The Options Panel used for configuring a {@link HttpAuthenticationMethod}.
	 */
	private static class HttpAuthenticationMethodOptionsPanel extends
			AbstractAuthenticationMethodOptionsPanel {

		private static final long serialVersionUID = 4341092284683481288L;

		private static final String HOSTNAME_LABEL = Constant.messages
				.getString("authentication.method.http.field.label.hostname");
		private static final String PORT_LABEL = Constant.messages
				.getString("authentication.method.http.field.label.port");
		private static final String REALM_LABEL = Constant.messages
				.getString("authentication.method.http.field.label.realm");

		private ZapTextField hostnameField;
		private ZapTextField realmField;
		private ZapPortNumberSpinner portNumberSpinner;
		private HttpAuthenticationMethod method;

		public HttpAuthenticationMethodOptionsPanel() {
			super();
			initialize();
		}

		private void initialize() {
			this.setLayout(new GridBagLayout());

			this.add(new JLabel(HOSTNAME_LABEL), LayoutHelper.getGBC(0, 0, 1, 0.0d));
			this.hostnameField = new ZapTextField();
			this.add(this.hostnameField, LayoutHelper.getGBC(1, 0, 1, 1.0d, new Insets(0, 0, 0, 10)));

			this.add(new JLabel(PORT_LABEL), LayoutHelper.getGBC(2, 0, 1, 0.0d));
			this.portNumberSpinner = new ZapPortNumberSpinner(80);
			this.add(this.portNumberSpinner, LayoutHelper.getGBC(3, 0, 1, 0.0d));

			this.add(new JLabel(REALM_LABEL), LayoutHelper.getGBC(0, 1, 1, 0.0d));
			this.realmField = new ZapTextField();
			this.add(this.realmField, LayoutHelper.getGBC(1, 1, 1, 1.0d, new Insets(0, 0, 0, 10)));
		}

		@Override
		public void validateFields() throws IllegalStateException {
			try {
				new URL(hostnameField.getText());
			} catch (Exception ex) {
				hostnameField.requestFocusInWindow();
				throw new IllegalStateException(
						Constant.messages.getString("authentication.method.http.dialog.error.url.text"));
			}
			if (realmField.getText().isEmpty()) {
				realmField.requestFocus();
				throw new IllegalStateException(
						Constant.messages.getString("authentication.method.http.dialog.error.realm.text"));
			}
		}

		@Override
		public void saveMethod() {
			getMethod().hostname = hostnameField.getText();
			getMethod().port = portNumberSpinner.getValue();
			getMethod().realm = realmField.getText();
		}

		@Override
		public void bindMethod(AuthenticationMethod method) throws UnsupportedAuthenticationMethodException {
			this.method = (HttpAuthenticationMethod) method;
			this.hostnameField.setText(this.method.hostname);
			this.portNumberSpinner.setValue(this.method.port);
			this.realmField.setText(this.method.realm);

		}

		@Override
		public HttpAuthenticationMethod getMethod() {
			return method;
		}
	}

	@Override
	public HttpAuthenticationMethod createAuthenticationMethod(int contextId) {
		return new HttpAuthenticationMethod();
	}

	@Override
	public String getName() {
		return METHOD_NAME;
	}

	@Override
	public int getUniqueIdentifier() {
		return METHOD_IDENTIFIER;
	}

	@Override
	public AbstractAuthenticationMethodOptionsPanel buildOptionsPanel(Context uiSharedContext) {
		return new HttpAuthenticationMethodOptionsPanel();
	}

	@Override
	public boolean hasOptionsPanel() {
		return true;
	}

	@Override
	public AbstractCredentialsOptionsPanel<? extends AuthenticationCredentials> buildCredentialsOptionsPanel(
			AuthenticationCredentials credentials, Context uiSharedContext) {
		return new UsernamePasswordAuthenticationCredentialsOptionsPanel(
				(UsernamePasswordAuthenticationCredentials) credentials);
	}

	@Override
	public boolean hasCredentialsOptionsPanel() {
		return true;
	}

	@Override
	public boolean isTypeForMethod(AuthenticationMethod method) {
		return (method instanceof HttpAuthenticationMethod);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		// Nothing to hook
	}

	@Override
	public AuthenticationMethod loadMethodFromSession(Session session, int contextId) throws SQLException {
		HttpAuthenticationMethod method = createAuthenticationMethod(contextId);

		List<String> hostnames = session.getContextDataStrings(contextId,
				RecordContext.TYPE_AUTH_METHOD_FIELD_1);
		if (hostnames != null && hostnames.size() > 0) {
			method.hostname = hostnames.get(0);
		}
		List<String> realms = session
				.getContextDataStrings(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_2);
		if (realms != null && realms.size() > 0) {
			method.realm = realms.get(0);
		}

		List<String> ports = session.getContextDataStrings(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_3);
		if (ports != null && ports.size() > 0) {
			try {
				method.port = Integer.parseInt(ports.get(0));
			} catch (Exception ex) {
				log.error("Unable to load HttpAuthenticationMethod. ", ex);
			}
		}

		return method;
	}

	@Override
	public void persistMethodToSession(Session session, int contextId, AuthenticationMethod authMethod)
			throws UnsupportedAuthenticationMethodException, SQLException {
		if (!(authMethod instanceof HttpAuthenticationMethod))
			throw new UnsupportedAuthenticationMethodException("Http Authentication type only supports: "
					+ HttpAuthenticationMethod.class);

		HttpAuthenticationMethod method = (HttpAuthenticationMethod) authMethod;
		session.setContextData(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_1, method.hostname);
		session.setContextData(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_2, method.realm);
		session.setContextData(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_3,
				Integer.toString(method.port));
	}

	/* API related constants and methods. */
	private static final String PARAM_HOSTNAME = "hostname";
	private static final String PARAM_REALM = "realm";
	private static final String PARAM_PORT = "port";

	@Override
	public AuthenticationCredentials createAuthenticationCredentials() {
		return new UsernamePasswordAuthenticationCredentials();
	}

	@Override
	public ApiDynamicActionImplementor getSetMethodForContextApiAction() {
		return new ApiDynamicActionImplementor(API_METHOD_NAME, new String[] { PARAM_HOSTNAME, PARAM_REALM },
				new String[] { PARAM_PORT }) {

			@Override
			public void handleAction(JSONObject params) throws ApiException {
				Context context = ApiUtils.getContextByParamId(params, AuthenticationAPI.PARAM_CONTEXT_ID);
				HttpAuthenticationMethod method = createAuthenticationMethod(context.getIndex());

				method.hostname = ApiUtils.getNonEmptyStringParam(params, PARAM_HOSTNAME);
				try {
					new URL(method.hostname);
				} catch (Exception ex) {
					throw new ApiException(ApiException.Type.BAD_FORMAT, PARAM_HOSTNAME);
				}

				method.realm = ApiUtils.getNonEmptyStringParam(params, PARAM_REALM);

				if (params.containsKey(PARAM_PORT))
					try {
						String portString = params.getString(PARAM_PORT);
						method.port = Integer.parseInt(portString);
					} catch (Exception ex) {
						throw new ApiException(ApiException.Type.BAD_FORMAT, PARAM_PORT);
					}
				
				if(!context.getAuthenticationMethod().isSameType(method))
					apiChangedAuthenticationMethodForContext(context.getIndex());
				context.setAuthenticationMethod(method);

			}
		};
	}

	@Override
	public ApiDynamicActionImplementor getSetCredentialsForUserApiAction() {
		return UsernamePasswordAuthenticationCredentials.getSetCredentialsForUserApiAction(this);
	}

}

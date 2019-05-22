package org.zaproxy.zap.session;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.HttpState;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.sessions.SessionManagementAPI;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.ApiUtils;

/**
 * The type corresponding to a {@link SessionManagementMethod} for web applications that use uses
 * Http Authentication, so sessions are 'managed' through continuous authentication.
 */
public class HttpAuthSessionManagementMethodType extends SessionManagementMethodType {

	private static final int METHOD_IDENTIFIER = 1;
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(HttpAuthSessionManagementMethod.class);

	/** The Constant METHOD_NAME. */
	private static final String METHOD_NAME = Constant.messages.getString("sessionmanagement.method.ha.name");

	private static final String API_METHOD_NAME = "httpAuthSessionManagement";

	public static class HttpAuthSessionManagementMethod implements SessionManagementMethod {

		@Override
		public boolean isConfigured() {
			// Always configured
			return true;
		}

		@Override
		public SessionManagementMethodType getType() {
			return new HttpAuthSessionManagementMethodType();
		}

		@Override
		public WebSession extractWebSession(HttpMessage msg) {
			return new HttpAuthSession();
		}

		@Override
		public WebSession createEmptyWebSession() {
			return new HttpAuthSession();
		}

		@Override
		public void clearWebSessionIdentifiers(HttpMessage msg) {
			// Do nothing
		}

		@Override
		public ApiResponse getApiResponseRepresentation() {
			return new ApiResponseElement("methodName", API_METHOD_NAME);
		}

		@Override
		public void processMessageToMatchSession(HttpMessage message, WebSession session)
				throws UnsupportedWebSessionException {
			// Do nothing
		}

		@Override
		public SessionManagementMethod clone() {
			return new HttpAuthSessionManagementMethod();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}

	}

	public static class HttpAuthSession extends WebSession {

		private static int generatedNameIndex;

		public HttpAuthSession(String name) {
			super(name, new HttpState());
		}

		public HttpAuthSession() {
			super("Http Auth Session " + generatedNameIndex++, new HttpState());
		}
	}

	@Override
	public SessionManagementMethod createSessionManagementMethod(int contextId) {
		return new HttpAuthSessionManagementMethod();
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
	public AbstractSessionManagementMethodOptionsPanel buildOptionsPanel(Context uiSharedContext) {
		// Nothing to configure
		return null;
	}

	@Override
	public boolean hasOptionsPanel() {
		return false;
	}

	@Override
	public boolean isTypeForMethod(SessionManagementMethod method) {
		return method instanceof HttpAuthSessionManagementMethod;
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		// Nothing to hook
	}

	@Override
	public SessionManagementMethod loadMethodFromSession(Session session, int contextId) throws DatabaseException {
		return new HttpAuthSessionManagementMethod();
	}

	@Override
	public void persistMethodToSession(Session session, int contextId, SessionManagementMethod method)
			throws UnsupportedSessionManagementMethodException, DatabaseException {
		// Nothing to persist

	}

	@Override
	public void exportData(Configuration config, SessionManagementMethod sessionMethod) {
		// nothing to do
	}

	@Override
	public void importData(Configuration config, SessionManagementMethod sessionMethod) throws ConfigurationException {
		// nothing to do
	}

	@Override
	public ApiDynamicActionImplementor getSetMethodForContextApiAction() {
		return new ApiDynamicActionImplementor(API_METHOD_NAME, null, null) {

			@Override
			public void handleAction(JSONObject params) throws ApiException {
				Context context = ApiUtils.getContextByParamId(params, SessionManagementAPI.PARAM_CONTEXT_ID);
				context.setSessionManagementMethod(createSessionManagementMethod(context.getIndex()));
			}
		};
	}

}

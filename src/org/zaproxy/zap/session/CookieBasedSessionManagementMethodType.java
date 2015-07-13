package org.zaproxy.zap.session;

import java.lang.ref.WeakReference;
import java.net.HttpCookie;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpState;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.httpsessions.ExtensionHttpSessions;
import org.zaproxy.zap.extension.httpsessions.HttpSessionTokensSet;
import org.zaproxy.zap.extension.sessions.SessionManagementAPI;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.ApiUtils;

/**
 * The type corresponding to a {@link SessionManagementMethod} for web applications that use cookies
 * for session management.
 */
public class CookieBasedSessionManagementMethodType extends SessionManagementMethodType {

	private static final int METHOD_IDENTIFIER = 0;
	private static final Logger log = Logger.getLogger(CookieBasedSessionManagementMethod.class);

	/** The Constant METHOD_NAME. */
	private static final String METHOD_NAME = Constant.messages.getString("sessionmanagement.method.cb.name");

	/**
	 * The implementation for a {@link SessionManagementMethod} that for web applications that use
	 * cookies for session management.
	 */
	public static class CookieBasedSessionManagementMethod implements SessionManagementMethod {

		private int contextId;

		private Context context;

		private static WeakReference<ExtensionHttpSessions> extHttpSessions;

		public CookieBasedSessionManagementMethod(int contextId) {
			this.contextId = contextId;
		}

		@Override
		public String toString() {
			return CookieBasedSessionManagementMethodType.METHOD_NAME;
		}

		@Override
		public boolean isConfigured() {
			// Always configured
			return true;
		}

		private Cookie convertCookie(HttpCookie cookie) {
			Cookie c = new Cookie(cookie.getDomain(), cookie.getName(), cookie.getValue(), cookie.getPath(),
					(int) cookie.getMaxAge(), cookie.getSecure());
			c.setVersion(cookie.getVersion());
			c.setComment(cookie.getComment());
			return c;
		}

		@Override
		public WebSession extractWebSession(HttpMessage msg) {
			if (msg.getRequestingUser() != null)
				return msg.getRequestingUser().getAuthenticatedSession();
			else {
				// Make sure any cookies in the message are put in the session
				CookieBasedSession session = new CookieBasedSession();
				for (HttpCookie c : msg.getRequestHeader().getHttpCookies())
					session.getHttpState().addCookie(convertCookie(c));
				// Use the messages hostname as default domain when generating SET cookies
				for (HttpCookie c : msg.getResponseHeader().getHttpCookies(msg.getRequestHeader().getHostName()))
					session.getHttpState().addCookie(convertCookie(c));
				return session;
			}
		}

		@Override
		public void processMessageToMatchSession(HttpMessage message, WebSession session)
				throws UnsupportedWebSessionException {
			if (session.getHttpState() == null)
				return;
			
			session.getHttpState().purgeExpiredCookies();

			// Remove any cookies that will be added by the HttpState from the message
			List<HttpCookie> cookies = message.getRequestHeader().getHttpCookies();
			Iterator<HttpCookie> it = cookies.iterator();
			
			while (it.hasNext()) {
				HttpCookie c = it.next();
				for (Cookie sc : session.getHttpState().getCookies())
					if (sc.getName().equals(c.getName())) {
						it.remove();
						break;
					}
			}
			message.setCookies(cookies);
		}

		private ExtensionHttpSessions getHttpSessionsExtension() {
			if (extHttpSessions == null || extHttpSessions.get() == null) {
				extHttpSessions = new WeakReference<>( Control
						.getSingleton().getExtensionLoader().getExtension(ExtensionHttpSessions.class));
				if (extHttpSessions == null)
					log.error("An error occured while loading the ExtensionHttpSessions.");
			}
			return extHttpSessions.get();
		}

		private Context getContext() {
			if (context == null) {
				context = Model.getSingleton().getSession().getContext(contextId);
			}
			return context;
		}

		@Override
		public SessionManagementMethod clone() {
			return new CookieBasedSessionManagementMethod(contextId);
		}

		@Override
		public void clearWebSessionIdentifiers(HttpMessage msg) {
			HttpSessionTokensSet tokens = getHttpSessionsExtension().getHttpSessionTokensSetForContext(
					getContext());
			if (tokens == null) {
				log.info("No tokens to clear.");
				return;
			}
			List<HttpCookie> requestCookies = msg.getRequestHeader().getHttpCookies();
			Iterator<HttpCookie> it = requestCookies.iterator();
			while (it.hasNext())
				if (tokens.isSessionToken(it.next().getName()))
					it.remove();
			msg.setCookies(requestCookies);
		}

		@Override
		public SessionManagementMethodType getType() {
			return new CookieBasedSessionManagementMethodType();
		}

		@Override
		public ApiResponse getApiResponseRepresentation() {
			return new ApiResponseElement("methodName", API_METHOD_NAME);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + contextId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CookieBasedSessionManagementMethod other = (CookieBasedSessionManagementMethod) obj;
			if (contextId != other.contextId)
				return false;
			return true;
		}

		@Override
		public WebSession createEmptyWebSession() {
			return new CookieBasedSession();
		}
	}

	/**
	 * The implementation for a {@link WebSession} used in cases where sessions are managed using
	 * cookies.
	 */
	public static class CookieBasedSession extends WebSession {

		private static int generatedNameIndex;

		public CookieBasedSession(String name) {
			super(name, new HttpState());
		}

		public CookieBasedSession() {
			super("Cookie Based Session " + generatedNameIndex++, new HttpState());
		}

	}

	@Override
	public CookieBasedSessionManagementMethod createSessionManagementMethod(int contextId) {
		return new CookieBasedSessionManagementMethod(contextId);
	}

	@Override
	public String getName() {
		return METHOD_NAME;
	}

	@Override
	public AbstractSessionManagementMethodOptionsPanel buildOptionsPanel(Context uiSharedContext) {
		// No need for a configuration panel yet
		return null;
	}

	@Override
	public boolean hasOptionsPanel() {
		return false;
	}

	@Override
	public boolean isTypeForMethod(SessionManagementMethod method) {
		return (method instanceof CookieBasedSessionManagementMethod);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		// No need for hooking anything
	}

	@Override
	public int getUniqueIdentifier() {
		return METHOD_IDENTIFIER;
	}

	@Override
	public SessionManagementMethod loadMethodFromSession(Session session, int contextId) throws DatabaseException {
		return new CookieBasedSessionManagementMethod(contextId);
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
	
	private static final String API_METHOD_NAME = "cookieBasedSessionManagement";

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
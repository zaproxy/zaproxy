package org.zaproxy.zap.extension.httpsessions;

import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;

public class HttpSessionsAPI extends ApiImplementor {

	private static final Logger log = Logger.getLogger(HttpSessionsAPI.class);

	/** The Constant PREFIX defining the name/prefix of the api. */
	private static final String PREFIX = "http_sessions";

	/**
	 * The Constant ACTION_SET_ACTIVE_SESSION that defines the action of setting a new active
	 * session for a site.
	 */
	private static final String ACTION_SET_ACTIVE_SESSION = "set_active_session";

	/**
	 * The Constant ACTION_UNSET_ACTIVE_SESSION that defines the action of unsetting an ctive
	 * session for a site.
	 */
	private static final String ACTION_UNSET_ACTIVE_SESSION = "unset_active_session";

	/**
	 * The Constant ACTION_PARAM_SITE defining the mandatory parameter required for setting data
	 * regarding the sessions for a site.
	 */
	private static final String ACTION_PARAM_SITE = "site";

	/**
	 * The Constant ACTION_PARAM_SESSION defining the mandatory parameter required for setting data
	 * regarding a particular.
	 */
	private static final String ACTION_PARAM_SESSION = "session";

	/**
	 * The Constant VIEW_SESSIONS that defines the view which describes the current existing
	 * sessions for a site.
	 */
	private static final String VIEW_SESSIONS = "sessions_list";

	/**
	 * The Constant VIEW_SESSIONS that defines the view which describes the current active session
	 * for a site.
	 */
	private static final String VIEW_ACTIVE_SESSION = "active_session";

	/**
	 * The Constant VIEW_SESSIONS_PARAM_SITE defining the mandatory parameter required for viewing
	 * data regarding the sessions.
	 */
	private static final String VIEW_PARAM_SITE = "site";

	/** The extension. */
	private ExtensionHttpSessions extension;

	/**
	 * Instantiates a new http sessions api implementor.
	 * 
	 * @param extension the extension
	 */
	public HttpSessionsAPI(ExtensionHttpSessions extension) {
		super();
		this.extension = extension;

		// Register the actions
		this.addApiAction(new ApiAction(ACTION_SET_ACTIVE_SESSION, new String[] { ACTION_PARAM_SITE,
				ACTION_PARAM_SESSION }));
		this.addApiAction(new ApiAction(ACTION_UNSET_ACTIVE_SESSION, new String[] { ACTION_PARAM_SITE }));

		// Register the views
		this.addApiView(new ApiView(VIEW_SESSIONS, new String[] { VIEW_PARAM_SITE }));
		this.addApiView(new ApiView(VIEW_ACTIVE_SESSION, new String[] { VIEW_PARAM_SITE }));
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		if (log.isDebugEnabled()) {
			log.debug("Request for handleApiAction: " + name + " (params: " + params.toString() + ")");
		}

		HttpSessionsSite site;
		boolean ok = false;
		switch (name) {
		case ACTION_SET_ACTIVE_SESSION:
			site = extension.getHttpSessionsSite(params.getString(ACTION_PARAM_SITE));
			String sname = params.getString(ACTION_PARAM_SESSION);
			for (HttpSession session : site.getHttpSessions())
				if (session.getName().equals(sname)) {
					site.setActiveSession(session);
					ok = true;
					break;
				}
			break;
		case ACTION_UNSET_ACTIVE_SESSION:
			site = extension.getHttpSessionsSite(params.getString(ACTION_PARAM_SITE));
			site.unsetActiveSession();
			ok = true;
			break;
		default:
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
		if (ok) {
			return ApiResponseElement.OK;
		} else {
			return ApiResponseElement.FAIL;
		}
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
		if (log.isDebugEnabled()) {
			log.debug("Request for handleApiView: " + name + " (params: " + params.toString() + ")");
		}
		ApiResponse result;
		HttpSessionsSite site;
		switch (name) {
		case VIEW_SESSIONS:
			// Get existing sessions
			site = extension.getHttpSessionsSite(params.getString(VIEW_PARAM_SITE));
			Set<HttpSession> sessions = site.getHttpSessions();
			if (log.isDebugEnabled())
				log.debug("API View for sessions for " + params.getString(VIEW_PARAM_SITE) + ": " + site);

			// Build the response
			ApiResponseList response = new ApiResponseList(name);
			for (HttpSession session : sessions) {
				ApiResponseList sessionResult = new ApiResponseList("session");
				sessionResult.addItem(new ApiResponseElement("name", session.getName()));
				sessionResult.addItem(new ApiResponseSet("tokens", session.getTokenValuesUnmodifiableMap()));
				sessionResult.addItem(new ApiResponseElement("messages_matched", Integer.toString(session
						.getMessagesMatched())));
				response.addItem(sessionResult);
			}
			result = response;
			break;
		case VIEW_ACTIVE_SESSION:
			// Get existing sessions
			site = extension.getHttpSessionsSite(params.getString(VIEW_PARAM_SITE));
			if (log.isDebugEnabled()) {
				log.debug("API View for active session for " + params.getString(VIEW_PARAM_SITE) + ": " + site);
			}

			if (site.getActiveSession() != null)
				result = new ApiResponseElement("active_sesion", site.getActiveSession().getName());
			else
				result = new ApiResponseElement("active_sesion", "");
			break;
		default:
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}
}

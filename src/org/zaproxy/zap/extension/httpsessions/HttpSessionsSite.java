/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.httpsessions;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;

/**
 * The Class SiteHttpSessions stores all the information regarding the sessions for a particular
 * Site.
 */
public class HttpSessionsSite {

	/** The extension. */
	private ExtensionHttpSessions extension;

	/** The site. */
	private String site;

	/** The sessions. */
	private LinkedHashSet<HttpSession> sessions;

	/** The active session. */
	private HttpSession activeSession;

	/** The model associated with this site. */
	private HttpSessionsTableModel model;

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(HttpSessionsSite.class);

	/** The last session id. */
	private static int lastSessionID = 0;

	/**
	 * Instantiates a new site http sessions object.
	 * 
	 * @param extension the extension
	 * @param site the site
	 */
	public HttpSessionsSite(ExtensionHttpSessions extension, String site) {
		super();
		this.extension = extension;
		this.site = site;
		this.sessions = new LinkedHashSet<HttpSession>();
		this.model = new HttpSessionsTableModel();
		this.activeSession = null;
	}

	/**
	 * Adds a new http session to this site.
	 * 
	 * @param session the session
	 */
	public void addHttpSession(HttpSession session) {
		this.sessions.add(session);
		this.model.addHttpSession(session);
	}

	/**
	 * Removes an existing session.
	 * 
	 * @param session the session
	 */
	public void removeHttpSession(HttpSession session) {
		if (session == activeSession)
			activeSession = null;
		this.sessions.remove(session);
		this.model.removeHttpSession(session);
	}

	/**
	 * Gets the site.
	 * 
	 * @return the site
	 */
	public String getSite() {
		return site;
	}

	/**
	 * Sets the site.
	 * 
	 * @param site the new site
	 */
	public void setSite(String site) {
		this.site = site;
	}

	/**
	 * Gets the active session.
	 * 
	 * @return the active session
	 */
	public HttpSession getActiveSession() {
		return activeSession;
	}

	/**
	 * Sets the active session.
	 * 
	 * @param activeSession the new active session
	 */
	public void setActiveSession(HttpSession activeSession) {
		if (log.isInfoEnabled())
			log.info("Setting new active session for site '" + site + "': " + activeSession);

		if (this.activeSession != null)
			this.activeSession.setActive(false);
		this.activeSession = activeSession;
		activeSession.setActive(true);
	}

	/**
	 * Unset any active session for this site.
	 */
	public void unsetActiveSession() {
		if (log.isInfoEnabled())
			log.info("Setting no active session for site '" + site + "'.");

		if (this.activeSession != null) {
			this.activeSession.setActive(false);
			this.activeSession = null;
		}
	}

	/**
	 * Creates a new empty session.
	 */
	public void createEmptySession() {
		HttpSession session = new HttpSession("Session " + (lastSessionID++));
		this.addHttpSession(session);
		this.setActiveSession(session);
	}

	/**
	 * Gets the model.
	 * 
	 * @return the model
	 */
	public HttpSessionsTableModel getModel() {
		return model;
	}

	/**
	 * Process the http request message before being sent.
	 * 
	 * @param message the message
	 */
	public void processHttpRequestMessage(HttpMessage message) {
		// Get the session tokens for this site
		String[] tokens = extension.getSessionTokens(getSite());
		HashSet<String> tokensSet = extension.getSessionTokensSet(getSite());
		// No tokens for this site, so no processing
		if (tokens == null) {
			log.debug("No session tokens for: " + this.getSite());
			return;
		}

		// Get the session, based on the request header
		List<HttpCookie> requestCookies = message.getRequestHeader().getHttpCookies();
		HttpSession session = getMatchingHttpSession(requestCookies, tokens);
		if (log.isDebugEnabled())
			log.debug("Matching session for request message (for site " + getSite() + "): " + session);

		// If any session is active (forced), change the necessary cookies
		if (activeSession != null && activeSession != session) {
			// Iterate through the cookies in the request
			Iterator<HttpCookie> it = requestCookies.iterator();
			while (it.hasNext()) {
				HttpCookie cookie = it.next();
				String cookieName = cookie.getName().toLowerCase();

				// If the cookie is a token
				if (tokensSet.contains(cookieName)) {
					String tokenValue = activeSession.getTokenValue(cookieName);
					log.debug("Changing value of token '" + cookieName + "' to: " + tokenValue);

					// Change it's value to the one in the active session, if any
					if (tokenValue != null)
						cookie.setValue(tokenValue);
					// Or delete it, if the active session does not have a token value
					else
						it.remove();
					// Remove the token from the token set so we know what tokens still have to be
					// added
					tokensSet.remove(cookieName);
				}
			}

			// Iterate through the tokens that are not present in the request and set the proper
			// value
			for (String token : tokensSet) {
				String tokenValue = activeSession.getTokenValue(token);
				// Change it's value to the one in the active session, if any
				if (tokenValue != null) {
					log.debug("Adding token '" + token + " with value: " + tokenValue);
					HttpCookie cookie = new HttpCookie(token, tokenValue);
					requestCookies.add(cookie);
				}
			}

			// Update the cookies in the message
			message.getRequestHeader().setCookies(requestCookies);
		} else {
			if (activeSession == session)
				log.debug("Session of request message is the same as the active session, so no request changes needed.");
			else
				log.debug("No active session is selected.");

		}
	}

	/**
	 * Process the http response message received after a request.
	 * 
	 * @param message the message
	 * @return true, if anything has been modified in any of the sessions
	 */
	public boolean processHttpResponseMessage(HttpMessage message) {

		// Get the session tokens for this site
		String[] tokens = extension.getSessionTokens(getSite());
		// No tokens for this site, so no processing
		if (tokens == null) {
			log.debug("No session tokens for: " + this.getSite());
			return false;
		}
		// Create an auxiliary map of token values and insert keys for every token
		HashMap<String, String> tokenValues = new HashMap<String, String>();
		HashSet<String> tokensSet = extension.getSessionTokensSet(getSite());

		// Get new values for tokens, if any
		List<HttpCookie> cookiesToSet = message.getResponseHeader().getHttpCookies();
		for (HttpCookie cookie : cookiesToSet) {
			if (tokensSet.contains(cookie.getName().toLowerCase()))
				tokenValues.put(cookie.getName().toLowerCase(), cookie.getValue());
		}

		// Get the session, based on the request header
		List<HttpCookie> requestCookies = message.getRequestHeader().getHttpCookies();
		HttpSession session = getMatchingHttpSession(requestCookies, tokens);
		if (log.isDebugEnabled())
			log.debug("Matching session for response message (from site " + getSite() + "): " + session);

		// If the session didn't exist, create it now
		if (session == null) {
			session = new HttpSession("Session " + (lastSessionID++));
			this.addHttpSession(session);

			// Add all the existing tokens from the request, if they don't replace one in the
			// response
			for (HttpCookie cookie : requestCookies) {
				String cookieName = cookie.getName().toLowerCase();
				if (tokensSet.contains(cookieName))
					if (!tokenValues.containsKey(cookieName))
						tokenValues.put(cookieName, cookie.getValue());
			}
		}

		// Update the session
		if (!tokenValues.isEmpty()) {
			for (Entry<String, String> tv : tokenValues.entrySet()) {
				session.setTokenValue(tv.getKey(), tv.getValue());
			}
			return true;
		}
		return false;
	}

	/**
	 * Gets the matching http session for a particular message containing a list of cookies.
	 * 
	 * @param tokens the tokens
	 * @param cookies the cookies
	 * @return the matching http session, if any, or null if no existing session was found to match
	 *         all the tokens
	 */
	private HttpSession getMatchingHttpSession(List<HttpCookie> cookies, String[] tokens) {

		// Pre-checks
		if (sessions == null || sessions.isEmpty())
			return null;

		LinkedList<HttpSession> matchingSessions = new LinkedList<HttpSession>(sessions);
		for (String token : tokens) {
			// Get the corresponding cookie from the cookies list
			HttpCookie matchingCookie = null;
			for (HttpCookie cookie : cookies)
				if (cookie.getName().equalsIgnoreCase(token)) {
					matchingCookie = cookie;
					break;
				}

			// TODO: Modified so that even even there is no cookie for this token, if there is a
			// session without this token, it matches
			// if (matchingCookie == null) {
			// log.debug("No sessions matching as no cookie is matching for token '" + token +
			// "' for site: "
			// + getSite());
			// return null;
			// }

			// Filter the sessions that do not match the cookie value
			Iterator<HttpSession> it = matchingSessions.iterator();
			while (it.hasNext()) {
				if (!it.next().matchesToken(token, matchingCookie))
					it.remove();
			}
		}

		// Return the matching session
		if (matchingSessions.size() == 1) {
			if (matchingSessions.size() > 1) {
				log.warn("Multiple sessions matching the cookies from response for site: " + getSite()
						+ ". Using first one.");
			}
			return matchingSessions.getFirst();
		}
		return null;

	}

	@Override
	public String toString() {
		return "HttpSessionsSite [site=" + site + ", activeSession=" + activeSession + ", sessions=" + sessions + "]";
	}
}

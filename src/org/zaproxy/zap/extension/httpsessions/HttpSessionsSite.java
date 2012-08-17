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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;

/**
 * The Class SiteHttpSessions stores all the information regarding the sessions for a particular
 * Site.
 */
public class HttpSessionsSite {

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(HttpSessionsSite.class);

	/** The last session id. */
	private static int lastGeneratedSessionID = 0;

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
		session.invalidate();
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
	 * @return the active session or <code>null</code>, if no session is set as active
	 * @see #setActiveSession(HttpSession)
	 */
	public HttpSession getActiveSession() {
		return activeSession;
	}

	/**
	 * Sets the active session.
	 * 
	 * @param activeSession the new active session.
	 * @see #getActiveSession()
	 * @see #unsetActiveSession()
	 * @throws IllegalArgumentException If the session provided as parameter is null.
	 */
	public void setActiveSession(HttpSession activeSession) {
		if (log.isInfoEnabled())
			log.info("Setting new active session for site '" + site + "': " + activeSession);
		if (activeSession == null)
			throw new IllegalArgumentException(
					"When settting an active session, a non-null session has to be provided.");

		if (this.activeSession != null) {
			this.activeSession.setActive(false);
			// If the active session was one with no tokens, delete it, as it will probably not
			// match anything from this point forward
			if (this.activeSession.getTokenValuesCount() == 0)
				this.removeHttpSession(this.activeSession);
			else
				// Notify the model that the session is updated
				model.fireHttpSessionUpdated(this.activeSession);
		}
		this.activeSession = activeSession;
		activeSession.setActive(true);
		// Notify the model that the session is updated
		model.fireHttpSessionUpdated(activeSession);
	}

	/**
	 * Unset any active session for this site.
	 * 
	 * @see #setActiveSession(HttpSession)
	 */
	public void unsetActiveSession() {
		if (log.isInfoEnabled())
			log.info("Setting no active session for site '" + site + "'.");

		if (this.activeSession != null) {
			this.activeSession.setActive(false);
			// If the active session was one with no tokens, delete it, at it will probably not
			// match anything from this point forward
			if (this.activeSession.getTokenValuesCount() == 0)
				this.removeHttpSession(this.activeSession);
			else
				// Notify the model that the session is updated
				model.fireHttpSessionUpdated(this.activeSession);

			this.activeSession = null;
		}
	}

	/**
	 * Creates a new empty session.
	 */
	public void createEmptySession() {
		// TODO: Allow the user to specify it?
		HttpSession session = new HttpSession(Constant.messages.getString("httpsessions.session.defaultName")
				+ (lastGeneratedSessionID++));
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
		Set<String> tokensSet = extension.getHttpSessionTokens(getSite());

		// TODO: Check for default tokens

		// No tokens for this site, so no processing
		if (tokensSet == null) {
			log.debug("No session tokens for: " + this.getSite());
			return;
		}

		// Get the session, based on the request header
		List<HttpCookie> requestCookies = message.getRequestHeader().getHttpCookies();
		HttpSession session = getMatchingHttpSession(requestCookies, tokensSet);
		if (log.isDebugEnabled())
			log.debug("Matching session for request message (for site " + getSite() + "): " + session);

		// If any session is active (forced), change the necessary cookies
		if (activeSession != null && activeSession != session) {

			// Make a copy of the session tokens set, as they will be modified
			tokensSet = new LinkedHashSet<String>(tokensSet);

			// Iterate through the cookies in the request
			Iterator<HttpCookie> it = requestCookies.iterator();
			while (it.hasNext()) {
				HttpCookie cookie = it.next();
				String cookieName = cookie.getName().toLowerCase(Locale.ENGLISH);

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
			// Store the session in the HttpMessage for caching purpose
			message.setHttpSession(activeSession);

			// Update the cookies in the message
			message.getRequestHeader().setCookies(requestCookies);
		} else {
			if (activeSession == session)
				log.debug("Session of request message is the same as the active session, so no request changes needed.");
			else
				log.debug("No active session is selected.");

			// Store the session in the HttpMessage for caching purpose
			message.setHttpSession(session);
		}
	}

	/**
	 * Process the http response message received after a request.
	 * 
	 * @param message the message
	 */
	public void processHttpResponseMessage(HttpMessage message) {

		// Get the session tokens for this site
		Set<String> tokensSet = extension.getHttpSessionTokens(getSite());

		// TODO: Process default tokens

		// No tokens for this site, so no processing
		if (tokensSet == null) {
			log.debug("No session tokens for: " + this.getSite());
			return;
		}
		// Create an auxiliary map of token values and insert keys for every token
		HashMap<String, String> tokenValues = new HashMap<String, String>();

		// Get new values for tokens, if any
		List<HttpCookie> cookiesToSet = message.getResponseHeader().getHttpCookies();
		for (HttpCookie cookie : cookiesToSet) {
			final String lcCookieName = cookie.getName().toLowerCase(Locale.ENGLISH);
			if (tokensSet.contains(lcCookieName))
				tokenValues.put(lcCookieName, cookie.getValue());
		}

		// Get the cookies present in the request
		List<HttpCookie> requestCookies = message.getRequestHeader().getHttpCookies();

		// Get the session, based on the request header
		HttpSession session = message.getHttpSession();
		if (session == null || !session.isValid()) {
			session = getMatchingHttpSession(requestCookies, tokensSet);
			if (log.isDebugEnabled())
				log.debug("Matching session for response message (from site " + getSite() + "): " + session);
		} else {
			if (log.isDebugEnabled())
				log.debug("Matching cached session for response message (from site " + getSite() + "): " + session);
		}

		// If the session didn't exist, create it now
		if (session == null) {
			session = new HttpSession("Session " + (lastGeneratedSessionID++));
			this.addHttpSession(session);

			// Add all the existing tokens from the request, if they don't replace one in the
			// response
			for (HttpCookie cookie : requestCookies) {
				String cookieName = cookie.getName().toLowerCase(Locale.ENGLISH);
				if (tokensSet.contains(cookieName))
					if (!tokenValues.containsKey(cookieName))
						tokenValues.put(cookieName, cookie.getValue());
			}
			log.info("Created a new session as no match was found: " + session);
		}

		// Update the session
		if (!tokenValues.isEmpty()) {
			for (Entry<String, String> tv : tokenValues.entrySet()) {
				session.setTokenValue(tv.getKey(), tv.getValue());
			}
		}

		// Update the count of messages matched
		session.setMessagesMatched(session.getMessagesMatched() + 1);

		this.model.fireHttpSessionUpdated(session);
	}

	/**
	 * Gets the matching http session for a particular message containing a list of cookies.
	 * 
	 * @param tokens the tokens
	 * @param cookies the cookies present in the request header of the message
	 * @return the matching http session, if any, or null if no existing session was found to match
	 *         all the tokens
	 */
	private HttpSession getMatchingHttpSession(List<HttpCookie> cookies, final Set<String> tokens) {

		// Pre-checks
		if (sessions.isEmpty())
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

			// Filter the sessions that do not match the cookie value
			Iterator<HttpSession> it = matchingSessions.iterator();
			while (it.hasNext()) {
				if (!it.next().matchesToken(token, matchingCookie))
					it.remove();
			}
		}

		// Return the matching session
		if (matchingSessions.size() >= 1) {
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

	/**
	 * Cleans up the sessions, eliminating the given session token.
	 * 
	 * @param token the session token
	 */
	protected void cleanupSessionToken(String token) {
		// Empty check
		if (sessions.isEmpty())
			return;

		if (log.isDebugEnabled())
			log.debug("Removing duplicates and cleaning up sessions for site - token: " + site + " - " + token);

		// If there are no more session tokens, delete all sessions
		Set<String> siteTokens = extension.getHttpSessionTokens(site);
		if (siteTokens == null) {
			log.info("No more session tokens. Removing all sessions...");
			// Invalidate all sessions
			for (HttpSession session : this.sessions)
				session.invalidate();

			// Remove all sessions
			this.sessions.clear();
			this.activeSession = null;
			this.model.removeAllElements();
			return;
		}

		// Iterate through all the sessions, eliminate the given token and eliminate any duplicates
		HashMap<String, HttpSession> uniqueSession = new HashMap<String, HttpSession>(sessions.size());
		LinkedList<HttpSession> toDelete = new LinkedList<HttpSession>();
		for (HttpSession session : this.sessions) {
			// Eliminate the token
			session.removeToken(token);
			if (session.getTokenValuesCount() == 0 && !session.isActive()) {
				toDelete.add(session);
				continue;
			} else
				model.fireHttpSessionUpdated(session);

			// If there is already a session with these tokens, mark one of them for deletion
			if (uniqueSession.containsKey(session.getTokenValuesString())) {
				HttpSession prevSession = uniqueSession.get(session.getTokenValuesString());
				// If the latter session is active, put it into the map and delete the other
				if (session.isActive()) {
					toDelete.add(prevSession);
					session.setMessagesMatched(session.getMessagesMatched() + prevSession.getMessagesMatched());
				} else {
					toDelete.add(session);
					prevSession.setMessagesMatched(session.getMessagesMatched() + prevSession.getMessagesMatched());
				}
			}
			// If it's the first one with these token values, keep it
			else {
				uniqueSession.put(session.getTokenValuesString(), session);
			}
		}

		// Delete the duplicate sessions
		if (log.isInfoEnabled())
			log.info("Removing duplicate or empty sessions: " + toDelete);
		Iterator<HttpSession> it = toDelete.iterator();
		while (it.hasNext()) {
			HttpSession ses = it.next();
			ses.invalidate();
			sessions.remove(ses);
			model.removeHttpSession(ses);
		}
	}
}

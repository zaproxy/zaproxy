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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.Cookie;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.session.CookieBasedSessionManagementHelper;

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

	/** The sessions as a LinkedHashSet. */
	private Set<HttpSession> sessions;

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
		this.sessions = new LinkedHashSet<>();
		this.model = new HttpSessionsTableModel(this);
		this.activeSession = null;
	}

	/**
	 * Adds a new http session to this site.
	 * 
	 * @param session the session
	 */
	public void addHttpSession(HttpSession session) {
		synchronized (this.sessions) {
			this.sessions.add(session);
		}
		this.model.addHttpSession(session);
	}

	/**
	 * Removes an existing session.
	 * 
	 * @param session the session
	 */
	public void removeHttpSession(HttpSession session) {
		if (session == activeSession) {
			activeSession = null;
		}
		synchronized (this.sessions) {
			this.sessions.remove(session);
		}
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
		if (log.isInfoEnabled()) {
			log.info("Setting new active session for site '" + site + "': " + activeSession);
		}
		if (activeSession == null) {
			throw new IllegalArgumentException(
					"When settting an active session, a non-null session has to be provided.");
		}

		if (this.activeSession == activeSession) {
			return;
		}

		if (this.activeSession != null) {
			this.activeSession.setActive(false);
			// If the active session was one with no tokens, delete it, as it will probably not
			// match anything from this point forward
			if (this.activeSession.getTokenValuesCount() == 0) {
				this.removeHttpSession(this.activeSession);
			} else {
				// Notify the model that the session is updated
				model.fireHttpSessionUpdated(this.activeSession);
			}
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
		if (log.isInfoEnabled()) {
			log.info("Setting no active session for site '" + site + "'.");
		}

		if (this.activeSession != null) {
			this.activeSession.setActive(false);
			// If the active session was one with no tokens, delete it, at it will probably not
			// match anything from this point forward
			if (this.activeSession.getTokenValuesCount() == 0) {
				this.removeHttpSession(this.activeSession);
			} else {
				// Notify the model that the session is updated
				model.fireHttpSessionUpdated(this.activeSession);
			}

			this.activeSession = null;
		}
	}

	/**
	 * Generates a unique session name.
	 * <p>
	 * The generated name is guaranteed to be unique compared to existing session names. If a
	 * generated name is already in use (happens if the user creates a session with a name that is
	 * equal to the ones generated) a new one will be generated until it's unique.
	 * <p>
	 * The generated session name is composed by the (internationalised) word "Session" appended
	 * with a space character and an (unique sequential) integer identifier. Each time the method is
	 * called the integer identifier is incremented, at least, by 1 unit.
	 * <p>
	 * Example session names generated:
	 * <p>
	 * 
	 * <pre>
	 * Session 0
	 * Session 1
	 * Session 2
	 * </pre>
	 * 
	 * @return the generated unique session name
	 * @see #lastGeneratedSessionID
	 */
	private String generateUniqueSessionName() {
		String name;
		do {
			name = MessageFormat.format(Constant.messages.getString("httpsessions.session.defaultName"),
					Integer.valueOf(lastGeneratedSessionID++));
		} while (!isSessionNameUnique(name));

		return name;
	}

	/**
	 * Tells whether the given session {@code name} is unique or not, compared to existing session
	 * names.
	 * 
	 * @param name the session name that will be checked
	 * @return {@code true} if the session name is unique, {@code false} otherwise
	 * @see #sessions
	 */
	private boolean isSessionNameUnique(final String name) {
		synchronized (this.sessions) {
			for (HttpSession session : sessions) {
				if (name.equals(session.getName())) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Validates that the session {@code name} is not {@code null} or an empty string.
	 * 
	 * @param name the session name to be validated
	 * @throws IllegalArgumentException if the {@code name} is {@code null} or an empty string
	 */
	private static void validateSessionName(final String name) {
		if (name == null) {
			throw new IllegalArgumentException("Session name must not be null.");
		}
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Session name must not be empty.");
		}
	}

	/**
	 * Creates an empty session with the given {@code name} and sets it as the active session.
	 * <p>
	 * <strong>Note:</strong> It's responsibility of the caller to ensure that no session with the
	 * given {@code name} already exists.
	 * 
	 * @param name the name of the session that will be created and set as the active session
	 * @throws IllegalArgumentException if the {@code name} is {@code null} or an empty string
	 * @see #addHttpSession(HttpSession)
	 * @see #setActiveSession(HttpSession)
	 * @see #isSessionNameUnique(String)
	 */
	private void createEmptySessionAndSetAsActive(final String name) {
		validateSessionName(name);

		final HttpSession session = new HttpSession(name, extension.getHttpSessionTokensSet(getSite()));
		addHttpSession(session);
		setActiveSession(session);
	}

	/**
	 * Creates an empty session with the given {@code name}.
	 * <p>
	 * The newly created session is set as the active session.
	 * <p>
	 * <strong>Note:</strong> If a session with the given {@code name} already exists no action is
	 * taken.
	 * 
	 * @param name the name of the session
	 * @throws IllegalArgumentException if the {@code name} is {@code null} or an empty string
	 * @see #setActiveSession(HttpSession)
	 */
	public void createEmptySession(final String name) {
		validateSessionName(name);

		if (!isSessionNameUnique(name)) {
			return;
		}
		createEmptySessionAndSetAsActive(name);
	}

	/**
	 * Creates a new empty session.
	 * <p>
	 * The newly created session is set as the active session.
	 * 
	 * @see #setActiveSession(HttpSession)
	 */
	public void createEmptySession() {
		createEmptySessionAndSetAsActive(generateUniqueSessionName());
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
		HttpSessionTokensSet siteTokensSet = extension.getHttpSessionTokensSet(getSite());

		// No tokens for this site, so no processing
		if (siteTokensSet == null) {
			log.debug("No session tokens for: " + this.getSite());
			return;
		}

		// Get the matching session, based on the request header
		List<HttpCookie> requestCookies = message.getRequestHeader().getHttpCookies();
		HttpSession session = getMatchingHttpSession(requestCookies, siteTokensSet);
		if (log.isDebugEnabled()) {
			log.debug("Matching session for request message (for site " + getSite() + "): " + session);
		}

		// If any session is active (forced), change the necessary cookies
		if (activeSession != null && activeSession != session) {
			CookieBasedSessionManagementHelper.processMessageToMatchSession(message, requestCookies,
					activeSession);
		} else {
			if (activeSession == session) {
				log.debug("Session of request message is the same as the active session, so no request changes needed.");
			} else {
				log.debug("No active session is selected.");
			}

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
		HttpSessionTokensSet siteTokensSet = extension.getHttpSessionTokensSet(getSite());

		// No tokens for this site, so no processing
		if (siteTokensSet == null) {
			log.debug("No session tokens for: " + this.getSite());
			return;
		}
		// Create an auxiliary map of token values and insert keys for every token
		Map<String, Cookie> tokenValues = new HashMap<>();

		// Get new values that were set for tokens (e.g. using SET-COOKIE headers), if any
		
		List<HttpCookie> cookiesToSet = message.getResponseHeader().getHttpCookies(message.getRequestHeader().getHostName());
		for (HttpCookie cookie : cookiesToSet) {
			String lcCookieName = cookie.getName();
			if (siteTokensSet.isSessionToken(lcCookieName)) {
				try {
					// Use 0 if max-age less than -1, Cookie class does not accept negative (expired) max-age (-1 has special
					// meaning).
					long maxAge = cookie.getMaxAge() < -1 ? 0 : cookie.getMaxAge();
					Cookie ck = new Cookie(cookie.getDomain(),lcCookieName,cookie.getValue(),cookie.getPath(),(int) maxAge,cookie.getSecure());				
					tokenValues.put(lcCookieName, ck);
				} catch (IllegalArgumentException e) {
					log.warn("Failed to create cookie [" + cookie + "] for site [" + getSite() + "]: " + e.getMessage());
				}
			}
		}

		// Get the cookies present in the request
		List<HttpCookie> requestCookies = message.getRequestHeader().getHttpCookies();

		// XXX When an empty HttpSession is set in the message and the response
		// contains session cookies, the empty HttpSession is reused which
		// causes the number of messages matched to be incorrect.

		// Get the session, based on the request header
		HttpSession session = message.getHttpSession();
		if (session == null || !session.isValid()) {
			session = getMatchingHttpSession(requestCookies, siteTokensSet);
			if (log.isDebugEnabled()) {
				log.debug("Matching session for response message (from site " + getSite() + "): " + session);
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Matching cached session for response message (from site " + getSite() + "): "
						+ session);
			}
		}

		boolean newSession = false;
		// If the session didn't exist, create it now
		if (session == null) {
			session = new HttpSession(generateUniqueSessionName(),
					extension.getHttpSessionTokensSet(getSite()));
			this.addHttpSession(session);

			// Add all the existing tokens from the request, if they don't replace one in the
			// response
			for (HttpCookie cookie : requestCookies) {
				String cookieName = cookie.getName();
				if (siteTokensSet.isSessionToken(cookieName)) {
					if (!tokenValues.containsKey(cookieName)) {
						
						// We must ensure that a cookie as always a valid domain and path in order to be able to reuse it.
						// HttpClient will discard invalid cookies
						
						String domain = cookie.getDomain();
						if (domain == null) {
							domain = message.getRequestHeader().getHostName();
						}
						
						String path = cookie.getPath();
						if (path == null) {
							path = "/"; // Default path
						}
							
						Cookie ck = new Cookie(domain, cookieName, cookie.getValue(), path,	(int) cookie.getMaxAge(), cookie.getSecure());
						tokenValues.put(cookieName,ck);
					}
				}
			}
			newSession = true;
		}

		// Update the session
		if (!tokenValues.isEmpty()) {
			for (Entry<String, Cookie> tv : tokenValues.entrySet()) {
				session.setTokenValue(tv.getKey(), tv.getValue());
			}
		}

		if (newSession && log.isDebugEnabled()) {
			log.debug("Created a new session as no match was found: " + session);
		}

		// Update the count of messages matched
		session.setMessagesMatched(session.getMessagesMatched() + 1);

		this.model.fireHttpSessionUpdated(session);
		
		// Store the session in the HttpMessage for caching purpose
		message.setHttpSession(session);
	}

	/**
	 * Gets the matching http session for a particular message containing a list of cookies.
	 * 
	 * @param siteTokens the tokens
	 * @param cookies the cookies present in the request header of the message
	 * @return the matching http session, if any, or null if no existing session was found to match
	 *         all the tokens
	 */
	private HttpSession getMatchingHttpSession(List<HttpCookie> cookies, final HttpSessionTokensSet siteTokens) {
		Collection<HttpSession> sessionsCopy;
		synchronized (sessions) {
			sessionsCopy = new ArrayList<>(sessions);
		}
		return CookieBasedSessionManagementHelper.getMatchingHttpSession(sessionsCopy, cookies, siteTokens);
	}

	@Override
	public String toString() {
		return "HttpSessionsSite [site=" + site + ", activeSession=" + activeSession + ", sessions="
				+ sessions + "]";
	}

	/**
	 * Cleans up the sessions, eliminating the given session token.
	 * 
	 * @param token the session token
	 */
	protected void cleanupSessionToken(String token) {
		// Empty check
		if (sessions.isEmpty()) {
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Removing duplicates and cleaning up sessions for site - token: " + site + " - "
					+ token);
		}

		synchronized (this.sessions) {
			// If there are no more session tokens, delete all sessions
			HttpSessionTokensSet siteTokensSet = extension.getHttpSessionTokensSet(site);
			if (siteTokensSet == null) {
				log.info("No more session tokens. Removing all sessions...");
				// Invalidate all sessions
				for (HttpSession session : this.sessions) {
					session.invalidate();
				}
	
				// Remove all sessions
				this.sessions.clear();
				this.activeSession = null;
				this.model.removeAllElements();
				return;
			}
	
			// Iterate through all the sessions, eliminate the given token and eliminate any duplicates
			Map<String, HttpSession> uniqueSession = new HashMap<>(sessions.size());
			List<HttpSession> toDelete = new LinkedList<>();
			for (HttpSession session : this.sessions) {
				// Eliminate the token
				session.removeToken(token);
				if (session.getTokenValuesCount() == 0 && !session.isActive()) {
					toDelete.add(session);
					continue;
				} else {
					model.fireHttpSessionUpdated(session);
				}
	
				// If there is already a session with these tokens, mark one of them for deletion
				if (uniqueSession.containsKey(session.getTokenValuesString())) {
					HttpSession prevSession = uniqueSession.get(session.getTokenValuesString());
					// If the latter session is active, put it into the map and delete the other
					if (session.isActive()) {
						toDelete.add(prevSession);
						session.setMessagesMatched(session.getMessagesMatched()
								+ prevSession.getMessagesMatched());
					} else {
						toDelete.add(session);
						prevSession.setMessagesMatched(session.getMessagesMatched()
								+ prevSession.getMessagesMatched());
					}
				}
				// If it's the first one with these token values, keep it
				else {
					uniqueSession.put(session.getTokenValuesString(), session);
				}
			}
	
			// Delete the duplicate sessions
			if (log.isInfoEnabled()) {
				log.info("Removing duplicate or empty sessions: " + toDelete);
			}
			Iterator<HttpSession> it = toDelete.iterator();
			while (it.hasNext()) {
				HttpSession ses = it.next();
				ses.invalidate();
				sessions.remove(ses);
				model.removeHttpSession(ses);
			}
		}
	}

	/**
	 * Gets an unmodifiable set of the http sessions. Attempts to modify the returned set, whether
	 * direct or via its iterator, result in an UnsupportedOperationException.
	 * 
	 * @return the http sessions
	 */
	protected Set<HttpSession> getHttpSessions() {
		synchronized (this.sessions) {
			return Collections.unmodifiableSet(sessions);
		}
	}

	/**
	 * Gets the http session with a particular name, if any, or {@code null} otherwise.
	 * 
	 * @param name the name
	 * @return the http session with a given name, or null, if no such session exists
	 */
	protected HttpSession getHttpSession(String name) {
		synchronized (this.sessions) {
			for (HttpSession session : sessions) {
				if (session.getName().equals(name)) {
					return session;
				}
			}
		}
		return null;
	}

	/**
	 * Renames a http session, making sure the new name is unique for the site.
	 * 
	 * @param oldName the old name
	 * @param newName the new name
	 * @return true, if successful
	 */
	public boolean renameHttpSession(String oldName, String newName) {
		// Check new name validity
		if (newName == null || newName.isEmpty()) {
			log.warn("Trying to rename session from " + oldName + " illegal name: " + newName);
			return false;
		}

		// Check existing old name
		HttpSession session = getHttpSession(oldName);
		if (session == null) {
			return false;
		}

		// Check new name uniqueness
		if (getHttpSession(newName) != null) {
			log.warn("Trying to rename session from " + oldName + " to already existing: " + newName);
			return false;
		}

		// Rename the session and notify model
		session.setName(newName);
		this.model.fireHttpSessionUpdated(session);

		return true;
	}

	static void resetLastGeneratedSessionId() {
		lastGeneratedSessionID = 0;
	}
}

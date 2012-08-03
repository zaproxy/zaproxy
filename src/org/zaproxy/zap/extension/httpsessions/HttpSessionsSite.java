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
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.http.cookie.Cookie;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HtmlParameter;
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
	LinkedHashSet<HttpSession> sessions;

	/** The active session. */
	HttpSession activeSession;

	/** The model associated with this site. */
	HttpSessionsTableModel model;

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
		this.activeSession.setActive(false);
		this.activeSession = activeSession;
		activeSession.setActive(true);
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
		// No tokens for this site, so no processing
		if (tokens == null) {
			log.debug("No session tokens for: " + this.getSite());
			return;
		}

		// Get the session
		HttpSession session = getMatchingHttpSession(message, tokens);
		if (log.isDebugEnabled())
			log.debug("Matching session for request message (for site " + getSite() + "): " + session);
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
		List<HttpCookie> cookies = message.getResponseHeader().getHttpCookies();
		for (HttpCookie cookie : cookies) {
			if (tokensSet.contains(cookie.getName().toLowerCase()))
				tokenValues.put(cookie.getName().toLowerCase(), cookie.getValue());
		}

		// Get the session
		HttpSession session = getMatchingHttpSession(message, tokens);
		if (log.isDebugEnabled())
			log.debug("Matching session for response message (from site " + getSite() + "): " + session);

		// If the session didn't exist, create it now
		if (session == null) {
			session = new HttpSession("Session " + (lastSessionID++));
			this.addHttpSession(session);

			// Add all the existing tokens from the request, if they don't replace one in the
			// response
			TreeSet<HtmlParameter> cookiesReq = message.getRequestHeader().getCookieParams();
			for (HtmlParameter cookie : cookiesReq)
				if (tokensSet.contains(cookie.getName().toLowerCase()))
					if (!tokenValues.containsKey(cookie.getName().toLowerCase()))
						tokenValues.put(cookie.getName().toLowerCase(), cookie.getValue());
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
	 * Gets the matching http session for a particular message.
	 * 
	 * @param message the message
	 * @param tokens the tokens
	 * @return the matching http session
	 */
	private HttpSession getMatchingHttpSession(HttpMessage message, String[] tokens) {

		// Pre-checks
		if (sessions == null || sessions.isEmpty())
			return null;

		// Get the token values from the message
		TreeSet<HtmlParameter> cookies = message.getRequestHeader().getCookieParams();
		LinkedHashSet<HttpSession> matchingSessions = new LinkedHashSet<HttpSession>(sessions);
		for (String token : tokens) {
			// Get the corresponding cookie from the cookies list
			HtmlParameter matchingCookie = null;
			for (HtmlParameter cookie : cookies)
				if (cookie.getName().equalsIgnoreCase(token)) {
					matchingCookie = cookie;
					break;
				}
			if (matchingCookie == null) {
				log.debug("No sessions matching as no cookie is matching for site: " + getSite());
				return null;
			}

			// Filter the sessions that do not match the cookie value
			Iterator<HttpSession> it = matchingSessions.iterator();
			while (it.hasNext()) {
				if (!it.next().matchesCookie(matchingCookie))
					it.remove();
			}
		}

		// Return the matching session
		if (matchingSessions.size() == 1) {
			if (matchingSessions.size() > 1) {
				log.warn("Multiple sessions matching the cookies from response for site: " + getSite()
						+ ". Using first one.");
			}
			return matchingSessions.iterator().next();
		}
		return null;

	}
}

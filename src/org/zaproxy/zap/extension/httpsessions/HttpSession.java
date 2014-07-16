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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;

/**
 * The Class HttpSession defines the data that is stored regarding an existing HTTP session on a
 * particular site.
 * <p>
 * The session can be invalidated and should only be used while it is valid.
 * </p>
 */
public class HttpSession {

	/** The name. */
	private String name;

	/** Whether it is active. */
	private boolean active;

	/** The session tokens' values for this session. */
	private Map<String, Cookie> tokenValues;

	/** Whether this session is valid. */
	private boolean valid;

	/** The number of http messages that matched this session. */
	private int messagesMatched;
	
	private HttpSessionTokensSet tokenNames;

	/**
	 * Instantiates a new http session.
	 *
	 * @param name the name
	 * @param tokenNames the token names
	 */
	public HttpSession(String name, HttpSessionTokensSet tokenNames) {
		super();
		this.name = name;
		this.active = false;
		this.valid = true;
		this.messagesMatched = 0;
		this.tokenValues = new HashMap<>(1);
		this.tokenNames=tokenNames;
	}

	public HttpSessionTokensSet getTokensNames() {
		return tokenNames;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Checks if it is active.
	 * 
	 * @return true, if is active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets whether it is active.
	 * 
	 * @param active the new active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Sets a particular value for a session token. If the value is null, that token is deleted from
	 * the session.
	 * 
	 * @param tokenName the token name
	 * @param value the new value of the token, or null, if the token has to be deleted
	 */
	public void setTokenValue(String tokenName, Cookie value) {
		if (value == null) {
			tokenValues.remove(tokenName);
		} else {
			tokenValues.put(tokenName, value);
		}
	}

	/**
	 * Gets the token value.
	 * 
	 * @param tokenName the token name
	 * @return the token value
	 */
	public String getTokenValue(String tokenName) {
		Cookie ck = tokenValues.get(tokenName);
		if (ck != null) {
			return ck.getValue();
		}
		return null;
	}

	/**
	 * Checks if a particular cookie has the same value as one of the token values in the HTTP
	 * session. If the {@literal cookie} parameter is null, the session matches the token if it does
	 * not have a value for the corresponding token.
	 * 
	 * @param tokenName the token name
	 * @param cookie the cookie
	 * @return true, if true
	 */
	public boolean matchesToken(String tokenName, HttpCookie cookie) {
		// Check if the cookie is null
		if (cookie == null) {
			return tokenValues.containsKey(tokenName) ? false : true;
		}

		// Check the value of the token from the cookie
		String tokenValue = getTokenValue(tokenName);
		if (tokenValue != null && tokenValue.equals(cookie.getValue())) {
			return true;
		}
		return false;
	}

	/**
	 * Removes a given token.
	 * 
	 * @param tokenName the token name
	 */
	public void removeToken(String tokenName) {
		tokenValues.remove(tokenName);
	}

	@Override
	public String toString() {
		return "HttpSession [name=" + name + ", active=" + active + ", tokenValues='" + getTokenValuesString() + "']";
	}

	/**
	 * Gets the token values string representation.
	 * 
	 * @return the token values string
	 */
	public String getTokenValuesString() {
		if (tokenValues.isEmpty()) {
			return "";
		}
		StringBuilder buf = new StringBuilder();

		for (Map.Entry<String, Cookie> entry : tokenValues.entrySet()) {
			buf.append(entry.getKey()).append('=').append(entry.getValue().getValue()).append(';');
		}
		buf.deleteCharAt(buf.length() - 1);

		return buf.toString();

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HttpSession other = (HttpSession) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if the session is still valid.
	 * 
	 * @return true, if is valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Invalidates the session.
	 */
	public void invalidate() {
		this.valid = false;
	}

	/**
	 * Gets the token values count.
	 * 
	 * @return the token values count
	 */
	public int getTokenValuesCount() {
		return this.tokenValues.size();
	}

	/**
	 * Gets an unmodifiable view of the token values map. . Query operations on the returned map
	 * "read through" to the specified map, and attempts to modify the returned map, whether direct
	 * or via its collection views, result in an {@link UnsupportedOperationException}.
	 * 
	 * @return the token values unmodifiable map
	 */
	public Map<String, Cookie> getTokenValuesUnmodifiableMap() {
		return Collections.unmodifiableMap(tokenValues);
	}

	/**
	 * Gets the number of http messages that matched this session.
	 * 
	 * @return the messages matched
	 */
	public int getMessagesMatched() {
		return messagesMatched;
	}

	/**
	 * Sets the number of http messages that matched this session.
	 * 
	 * @param messagesMatched the new messages matched count
	 */
	public void setMessagesMatched(int messagesMatched) {
		this.messagesMatched = messagesMatched;
	}

}

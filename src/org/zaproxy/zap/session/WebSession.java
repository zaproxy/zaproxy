package org.zaproxy.zap.session;

import org.parosproxy.paros.network.HttpMessage;

public interface WebSession {

	/**
	 * Modifies a message so its Request Header/Body match the web session.
	 * 
	 * @param message the message
	 */
	public void processMessageToMatch(HttpMessage message);

	/**
	 * Gets the name of the web session, if set.
	 * 
	 * @return the name
	 */
	public String getName();
}

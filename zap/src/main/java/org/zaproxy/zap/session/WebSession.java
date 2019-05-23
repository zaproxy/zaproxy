package org.zaproxy.zap.session;

import org.apache.commons.httpclient.HttpState;

/**
 * A WebSession is the ZAP implementation for a session maintained during the communication with a
 * webapp/website.
 */
public abstract class WebSession {

	private String name;
	private HttpState state;

	/**
	 * Instantiates a new web session.
	 * 
	 * @param name the name
	 * @param state the state
	 */
	public WebSession(String name, HttpState state) {
		this.name = name;
		this.state = state;
	}

	/**
	 * Gets the http state that will be used to send messages corresponding to this session.
	 * 
	 * @return the http state
	 */
	public HttpState getHttpState() {
		return state;
	}

	/**
	 * Gets the name of the web session, if set.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}

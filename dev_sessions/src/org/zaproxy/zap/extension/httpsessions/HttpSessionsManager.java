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

import java.util.HashMap;
import java.util.Map;

import org.parosproxy.paros.network.HttpMessage;

/**
 * The Class HttpSessionsManager handles the existing s.
 */
public class HttpSessionsManager {

	/** The extension. */
	ExtensionHttpSessions extension;

	/** The map of sessions corresponding to each site. */
	Map<String, HttpSessionsSite> sessions;

	/**
	 * Instantiates a new HTTP sessions manager.
	 * 
	 * @param extension the extension
	 */
	public HttpSessionsManager(ExtensionHttpSessions extension) {
		super();
		this.extension = extension;
		this.sessions = new HashMap<String, HttpSessionsSite>();
	}

	/**
	 * Gets the http sessions for a particular site.
	 * 
	 * @param site the site
	 * @return the http sessions site container
	 */
	public HttpSessionsSite getHttpSessionsSite(String site) {
		HttpSessionsSite hss = sessions.get(site);
		if (hss == null) {
			hss = new HttpSessionsSite(extension, site);
			sessions.put(site, hss);
		}
		return hss;
	}

	public void processHttpRequestMessage(HttpMessage message, String site) {

	}

	public void processHttpResponseMessage(HttpMessage message, String site) {

	}
}

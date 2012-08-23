/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.ConversionException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

/**
 * The HttpSessionsParam is used to store the parameters (options) for the
 * {@link ExtensionHttpSessions} and related classes.
 */
public class HttpSessionsParam extends AbstractParam {

	/** The Constant defining the key for the default session tokens used in the application. */
	private static final String DEFAULT_TOKENS_KEY = "httpsessions.token";

	/** The Constant PROXY_ONLY_KEY defining the key for the enabledProxyOnly option. */
	private static final String PROXY_ONLY_KEY = "httpsessions.proxyOnly";

	/** The default tokens used when there are no saved tokens in the file. */
	private static final String[] DEFAULT_TOKENS = { "asp.net_sessionid", "aspsessionid", "siteserver", "cfid",
			"cftoken", "jsessionid", "phpsessid", "sessid", "sid", "viewstate", "zenid" };

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(HttpSessionsParam.class);

	/** The default tokens. */
	private List<String> defaultTokens = null;

	/** Whether the HttpSessions extension is enabled for the proxy only. */
	private boolean enabledProxyOnly = false;

	/**
	 * Instantiates a new http sessions param.
	 * 
	 */
	public HttpSessionsParam() {
	}

	/* (non-Javadoc)
	 * 
	 * @see org.parosproxy.paros.common.FileXML#parse() */
	@Override
	protected void parse() {
		// Parse the default token names
		try {
			this.defaultTokens = new ArrayList<String>(Arrays.asList(getConfig().getStringArray(DEFAULT_TOKENS_KEY)));
		} catch (ConversionException e) {
			this.defaultTokens = new ArrayList<String>(DEFAULT_TOKENS.length);
			log.error("Error while parsing config file: " + e.getMessage(), e);
		}
		if (this.defaultTokens.size() == 0) {
			this.defaultTokens.addAll(Arrays.asList(DEFAULT_TOKENS));
		}

		try {
			this.enabledProxyOnly = getConfig().getBoolean(PROXY_ONLY_KEY, false);
		} catch (ConversionException e) {
			log.error("Error while parsing config file: " + e.getMessage(), e);
		}
	}

	/**
	 * Gets the default tokens.
	 * <p>
	 * The list of default session tokens returned is read-only view of the internal default session
	 * tokens representation and any modifications will result in
	 * {@link UnsupportedOperationException}.
	 * </p>
	 * 
	 * @return the default tokens
	 */
	public final List<String> getDefaultTokens() {
		return Collections.unmodifiableList(defaultTokens);
	}

	/**
	 * Sets the default tokens.
	 * 
	 * @param tokens the new default tokens
	 */
	public void setDefaultTokens(final List<String> tokens) {
		this.defaultTokens = tokens;
		getConfig().setProperty(DEFAULT_TOKENS_KEY, tokens);
	}

	/**
	 * Checks if the extension is only processing Proxy messages.
	 * 
	 * @return true, if is enabled for proxy only
	 */
	public boolean isEnabledProxyOnly() {
		return enabledProxyOnly;
	}

	/**
	 * Sets if the extension is only processing Proxy messages.
	 * 
	 * @param enabledProxyOnly the new enabled proxy only status
	 */
	public void setEnabledProxyOnly(boolean enabledProxyOnly) {
		this.enabledProxyOnly = enabledProxyOnly;
		getConfig().setProperty(PROXY_ONLY_KEY, Boolean.valueOf(enabledProxyOnly));
	}
}

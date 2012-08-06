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
import java.util.List;

import org.apache.commons.configuration.ConversionException;
import org.parosproxy.paros.common.AbstractParam;

/**
 * The HttpSessionsParam is used to store the parameters (options) for the
 * {@link ExtensionHttpSessions} and related classes.
 */
public class HttpSessionsParam extends AbstractParam {

	/** The Constant defining the key for the default session tokens used in the application. */
	private static final String DEFAULT_TOKENS_KEY = "httpsessions.token";

	/** The default tokens used when there are no saved tokens in the file.. */
	private static final String[] DEFAULT_TOKENS = { "asp.net_sessionid", "aspsessionid", "siteserver", "cfid",
			"cftoken", "jsessionid", "phpsessid", "sessid", "sid", "viewstate", "zenid" };

	/** The default tokens. */
	private List<String> defaultTokens = null;

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
		}
		if (this.defaultTokens.size() == 0) {
			this.defaultTokens.addAll(Arrays.asList(DEFAULT_TOKENS));
		}
	}

	/**
	 * Gets the default tokens. The returned list should not be modified.
	 * 
	 * @return the default tokens
	 */
	public final List<String> getDefaultTokens() {
		return defaultTokens;
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
}

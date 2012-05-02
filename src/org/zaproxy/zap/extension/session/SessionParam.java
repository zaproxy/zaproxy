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
package org.zaproxy.zap.extension.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.ConversionException;
import org.parosproxy.paros.common.AbstractParam;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SessionParam extends AbstractParam {

	private static final String TOKENS_KEY = "session.tokens";
	private static final String[] DEFAULT_TOKENS = {
		"asp.net_sessionid",
		"aspsessionid",
		"siteserver",
		"cfid",
		"cftoken",
		"jsessionid",
		"phpsessid",
		"sessid",
		"sid",
		"viewstate",
		"zenid"
	};
	
	private List<String> tokens = null;
	
    /**
     * @param rootElementName
     */
    public SessionParam() {
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.common.FileXML#parse()
     */
    @Override
	protected void parse(){
    	try {
    		this.tokens = new ArrayList<String>(Arrays.asList(getConfig().getStringArray(TOKENS_KEY)));
    	} catch (ConversionException e) {
    		this.tokens = new ArrayList<String>(DEFAULT_TOKENS.length);
    	}
    	if (this.tokens.size() == 0) {
    		this.tokens.addAll(Arrays.asList(DEFAULT_TOKENS));
    	}
    }

	public List<String> getTokens() {
		return tokens;
	}

	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
		getConfig().setProperty(TOKENS_KEY, tokens);
	}

	public void addToken(String param) {
		this.tokens.add(param);
	}

	public void removeToken(String name) {
		this.tokens.remove(name);
	}
	
}

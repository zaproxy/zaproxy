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
package org.zaproxy.zap.extension.anticsrf;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.common.AbstractParam;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AntiCsrfParam extends AbstractParam {

	private static final String TOKENS = "anticsrf.tokens";
		
	private List<String> tokens = null;
	
    /**
     * @param rootElementName
     */
    public AntiCsrfParam() {
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.common.FileXML#parse()
     */
    @SuppressWarnings("unchecked")
	protected void parse(){
    	try {
    		this.tokens = getConfig().getList(TOKENS);
System.out.println("SB parse got tokens: " + tokens.size());
    	} catch (Exception e) {
System.out.println("SB parse failed: " + e);
    		this.tokens = new ArrayList<String>();
    	}
    }

	public List<String> getTokens() {
		return tokens;
	}

	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
		getConfig().setProperty(TOKENS, tokens);
	}
	
}

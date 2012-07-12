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
package org.zaproxy.zap.scanner.plugin;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;
import org.zaproxy.zap.model.Vulnerabilities;
import org.zaproxy.zap.model.Vulnerability;


public class TestRedirect extends AbstractAppParamPlugin {

    private static final String REDIR1 = "http://www.owasp.org";
    private static final String REDIR2 = "http://www.google.com";
    
    private static Vulnerability vuln = Vulnerabilities.getVulnerability("wasc_38");
    
    private static Logger log = Logger.getLogger(TestRedirect.class);
	
    @Override
    public int getId() {
        return 20010;
    }

    @Override
    public String getName() {
    	if (vuln != null) {
    		return vuln.getAlert();
    	}
        return "URL Redirector Abuse";
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getDependency()
     */
    @Override
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getDescription()
     */
    @Override
    public String getDescription() {
    	if (vuln != null) {
    		return vuln.getDescription();
    	}
    	return "Failed to load vulnerability description from file";
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getCategory()
     */
    @Override
    public int getCategory() {
        return Category.MISC;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getSolution()
     */
    @Override
    public String getSolution() {
    	if (vuln != null) {
    		return vuln.getSolution();
    	}
    	return "Failed to load vulnerability solution from file";
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getReference()
     */
    @Override
    public String getReference() {
    	if (vuln != null) {
    		StringBuilder sb = new StringBuilder();
    		for (String ref : vuln.getReferences()) {
    			if (sb.length() > 0) {
    				sb.append('\n');
    			}
    			sb.append(ref);
    		}
    		return sb.toString();
    	}
    	return "Failed to load vulnerability reference from file";
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.AbstractTest#init()
     */
    @Override
    public void init() {

    }
    


    @Override
    public void scan(HttpMessage msg, String param, String value) {
		try {
			String redirect = REDIR1;
			if (msg.getRequestHeader().getURI().toString().startsWith(REDIR1)) {
				// Dont try a redirect to the first target, we're scanning the first target ;)
				redirect = REDIR2;
			}
			
			setParameter(msg, param, redirect);
            sendAndReceive(msg, false, false);	// Dont follow redirects!
            
            if (HttpStatusCode.isRedirection(msg.getResponseHeader().getStatusCode())) {
            	// Its a redirect, did it go to the target?
            	if (msg.getResponseHeader().getHeader(HttpHeader.LOCATION).startsWith(redirect)) {
            		// It did, so the param is vulnerable
            		bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, redirect, null, msg);
            	}
    			return;
            }

        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        }	

	}
}

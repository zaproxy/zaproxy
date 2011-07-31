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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    
    private static Log log = LogFactory.getLog(TestRedirect.class);
	
    public int getId() {
        return 20010;
    }

    public String getName() {
    	if (vuln != null) {
    		return vuln.getAlert();
    	}
        return "URL Redirector Abuse";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDependency()
     */
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDescription()
     */
    public String getDescription() {
    	if (vuln != null) {
    		return vuln.getDescription();
    	}
    	return "Failed to load vulnerability description from file";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getCategory()
     */
    public int getCategory() {
        return Category.MISC;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getSolution()
     */
    public String getSolution() {
    	if (vuln != null) {
    		return vuln.getSolution();
    	}
    	return "Failed to load vulnerability solution from file";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getReference()
     */
    public String getReference() {
    	if (vuln != null) {
    		StringBuffer sb = new StringBuffer();
    		for (String ref : vuln.getReferences()) {
    			if (sb.length() > 0) {
    				sb.append("\n");
    			}
    			sb.append(ref);
    		}
    		return sb.toString();
    	}
    	return "Failed to load vulnerability reference from file";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.AbstractTest#init()
     */
    public void init() {

    }
    


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
            		bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, null, msg);
            	}
    			return;
            }

        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        }	

	}
}

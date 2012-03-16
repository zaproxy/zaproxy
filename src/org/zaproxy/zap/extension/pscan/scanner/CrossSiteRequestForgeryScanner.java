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
package org.zaproxy.zap.extension.pscan.scanner;

import java.util.Date;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PassiveScanner;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.zaproxy.zap.model.Vulnerabilities;
import org.zaproxy.zap.model.Vulnerability;


public class CrossSiteRequestForgeryScanner extends PluginPassiveScanner implements PassiveScanner {

    private static Vulnerability vuln = Vulnerabilities.getVulnerability("wasc_9");
	private PassiveScanThread parent = null;
	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void setParent (PassiveScanThread parent) {
		this.parent = parent;
	}

	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		// Ignore
	}

	private int getId() {
		return 10014;
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		Date start = new Date();
		
		ExtensionAntiCSRF extAntiCSRF = 
			(ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);
		
		if (extAntiCSRF == null) {
			return;
		}

		List<Element> formElements = source.getAllElements(HTMLElementName.FORM);
		List<String> tokenNames = extAntiCSRF.getParam().getTokens();
		boolean foundCsrfToken = false;
		
		if (formElements != null && formElements.size() > 0) {
			
			// Loop through all of the FORM tags
			logger.debug("Found " + formElements.size() + " forms");
			
			for (Element formElement : formElements) {
				List<Element> inputElements = formElement.getAllElements(HTMLElementName.INPUT);
				
				if (inputElements != null && inputElements.size() > 0) {
					// Loop through all of the INPUT elements
					logger.debug("Found " + inputElements.size() + " inputs");
					for (Element inputElement : inputElements) {
						String attId = inputElement.getAttributeValue("ID");
						if (attId != null) {
							for (String tokenName : tokenNames) {
								if (tokenName.equalsIgnoreCase(attId)) {
									foundCsrfToken = true;
									break;
								}
							}
						}
						String name = inputElement.getAttributeValue("NAME");
						if (name != null) {
							for (String tokenName : tokenNames) {
								if (tokenName.equalsIgnoreCase(name)) {
									foundCsrfToken = true;
									break;
								}
							}
						}
					}
				}
				if (foundCsrfToken) {
					break;
				}
			}
			if (!foundCsrfToken) {
			    Alert alert = new Alert(getId(), Alert.RISK_MEDIUM, Alert.WARNING, 
				    	getName());
				    	alert.setDetail(
				    		getDescription(), 
				    		msg.getRequestHeader().getURI().toString(),
				    		"", 
				    		"", 
				    		"",
				    		getSolution(), 
				            getReference(), 
				            msg);

			    	parent.raiseAlert(id, alert);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("\tScan of record " + id + " took " + ((new Date()).getTime() - start.getTime()) + " ms");
		}
		
	}

	@Override
	public String getName() {
    	if (vuln != null) {
    		return vuln.getAlert();
    	}
    	return "Cross Site Request Forgery";
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

}

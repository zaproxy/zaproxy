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

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PassiveScanner;

public class AntiCsrfDetectScanner implements PassiveScanner {

	private PassiveScanThread parent = null;
	private Logger logger = Logger.getLogger(this.getClass());
	private List<String> tokenNames = new ArrayList<String>();
	private AntiCsrfParam param = null;

	@Override
	public void setParent (PassiveScanThread parent) {
		this.parent = parent;
	}

	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		// Ignore
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		List<Element> formElements = source.getAllElements(HTMLElementName.FORM);

		ExtensionAntiCSRF extAntiCSRF = 
			(ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);

		if (formElements != null && formElements.size() > 0) {
			// Loop through all of the FORM tags
			logger.debug("Found " + formElements.size() + " forms");
			
			for (Element formElement : formElements) {
				List<Element> inputElements = formElement.getAllElements(HTMLElementName.INPUT);
				
				if (inputElements != null && inputElements.size() > 0) {
					// Loop through all of the INPUT elements
					logger.debug("Found " + inputElements.size() + " inputs");
					for (Element inputElement : inputElements) {
						String name = inputElement.getAttributeValue("NAME");
						if (name != null) {
							for (String tokenName : this.getTokenNames()) {
								if (tokenName.equalsIgnoreCase(name)) {
									
									if (parent != null) {
										parent.addTag(id, ExtensionAntiCSRF.TAG);
									}
									extAntiCSRF.registerAntiCsrfToken(
											new AntiCsrfToken(msg, name, inputElement.getAttributeValue("VALUE")));
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return "Anti CSRF Token Detection";
	}

	public List<String> getTokenNames() {
		if (tokenNames.size() == 0) {
			AntiCsrfParam param = this.getParam();
			if (param != null) {
				tokenNames.addAll(param.getTokens());
			}
		}
		return tokenNames;
	}
	
	private AntiCsrfParam getParam() {
		if (param == null) {
			ExtensionAntiCSRF extAntiCSRF = 
				(ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);
			param = extAntiCSRF.getParam();
		}
		return param;
	}

	public void setTokenNames(List<String> tokenNames) {
		this.tokenNames = tokenNames;
	}

}

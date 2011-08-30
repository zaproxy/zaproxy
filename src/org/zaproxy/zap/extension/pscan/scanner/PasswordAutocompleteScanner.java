package org.zaproxy.zap.extension.pscan.scanner;

import java.util.Date;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PassiveScanner;


public class PasswordAutocompleteScanner extends PluginPassiveScanner implements PassiveScanner {

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
		return 40001;
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		Date start = new Date();
		
		List<Element> formElements = source.getAllElements(HTMLElementName.FORM);
		
		if (formElements != null && formElements.size() > 0) {
			// Loop through all of the FORM tags
			logger.debug("Found " + formElements.size() + " forms");
			
			for (Element formElement : formElements) {
				String autoComplete = formElement.getAttributeValue("AUTOCOMPLETE");
				
				if (autoComplete == null || ! autoComplete.equalsIgnoreCase("OFF")) {
					// The form doesnt have autocomplete turned to off
					List<Element> inputElements = formElement.getAllElements(HTMLElementName.INPUT);
					
					if (inputElements != null && inputElements.size() > 0) {
						// Loop through all of the INPUT elements
						logger.debug("Found " + inputElements.size() + " inputs");
						for (Element inputElement : inputElements) {
							String type = inputElement.getAttributeValue("TYPE");
							if (type != null && type.equalsIgnoreCase("PASSWORD")) {
								
							    Alert alert = new Alert(getId(), Alert.RISK_LOW, Alert.WARNING, 
							    	"Password Autocomplete in browser");
							    	alert.setDetail(
							    		"AUTOCOMPLETE attribute is not disabled in HTML FORM/INPUT element containing password type input.  Passwords may be stored in browsers and retrieved.", 
							    		msg.getRequestHeader().getURI().toString(),
							    		inputElement.getName(), 
							    		"", 
							    		"Turn off AUTOCOMPLETE attribute in form or individual input elements containing password by using AUTOCOMPLETE='OFF'", 
							            "http://msdn.microsoft.com/library/default.asp?url=/workshop/author/forms/autocomplete_ovr.asp", 
							            msg);

						    	parent.raiseAlert(id, alert);
							}
						}
					}
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("\tScan of record " + id + " took " + ((new Date()).getTime() - start.getTime()) + " ms");
		}
		
	}

	@Override
	public String getName() {
		return "Password Autocomplete in browser";
	}
}

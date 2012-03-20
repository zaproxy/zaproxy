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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.*;

import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PassiveScanner;

public class InformationDisclosureInURL extends PluginPassiveScanner implements PassiveScanner {

	private PassiveScanThread parent = null;
	private String urlSensitiveInformationFile = "xml/URL-information-disclosure-messages.txt";
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		if (msg.getRequestHeader().getURI().toString().indexOf("?") > 0) {
			String parameter;
			if ((parameter = doesURLContainsSensitiveInformation(msg.getRequestHeader().getURI().toString())) != null) {
				this.raiseAlert(msg, id, "the URL contains potentially sensitive information", parameter);
			}
			if ((parameter = isCreditCard(msg.getRequestHeader().getURI().toString())) != null) {
				this.raiseAlert(msg, id, "the URL contains credit card information", parameter);
			}
			if ((parameter = isEmailAddress(msg.getRequestHeader().getURI().toString())) != null) {
				this.raiseAlert(msg, id, "the URL contains email address(es)", parameter);
			}
			if ((parameter = isUsSSN(msg.getRequestHeader().getURI().toString())) != null) {
				this.raiseAlert(msg, id, "the URL contains US Social Security Number(s)", parameter);
			}
		}
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		
	}
	
	private void raiseAlert(HttpMessage msg, int id, String param, String other) {
		Alert alert = new Alert(getId(), Alert.RISK_INFO, Alert.WARNING, 
		    	getName());
		    	alert.setDetail(
		    			"The request appeared to contain sensitive information leaked in the URL. This can violate PCI and most organizational compliance policies. You can configure the list of strings for this check to add or remove values specific to your environment", 
		    	    msg.getRequestHeader().getURI().toString(),
		    	    param,
		    	    other, 
		    	    "",
		    	    "Do not pass sensitive information in URI's", 
		            "", 
		            msg);
	
    	parent.raiseAlert(id, alert);
	}
	
	private String doesURLContainsSensitiveInformation (String URL) {
		String line = null;
		
		try {
			// TODO cache this :)
			BufferedReader reader = new BufferedReader(new FileReader(urlSensitiveInformationFile));
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#") && URL.toLowerCase().contains(line.toLowerCase())) {
					reader.close();
					return line;
				}
			}
		} catch (IOException e) {
			logger.debug("Error on opening/reading URL information disclosure file. Error:" + e.getMessage());
		}
		return null;
	}

	@Override
	public void setParent(PassiveScanThread parent) {
		this.parent = parent;
	}

	@Override
	public String getName() {
		return "Information disclosure - sensitive informations in URL";
	}
	
	private int getId() {
		return 10024;
	}
	
	private String isEmailAddress(String emailAddress) {
		Pattern emailAddressPattern = Pattern.compile("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+<\\.[A-Z]{2,4}\\b");
		Matcher matcher = emailAddressPattern.matcher(emailAddress);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}
	
	private String isCreditCard(String creditCard) {
		Pattern creditCardPattern = Pattern.compile("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})\\b");
		Matcher matcher = creditCardPattern.matcher(creditCard);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}
	
	private String isUsSSN(String usSSN) {
		Pattern usSSNPattern = Pattern.compile("\\b[0-9]{3}-[0-9]{2}-[0-9]{4}\\b");
		Matcher matcher = usSSNPattern.matcher(usSSN);
		if (matcher.find()){
			return matcher.group();
		}
		return null;
	}
	
	private static void testUrl (String url) {
		InformationDisclosureInURL idiu = new InformationDisclosureInURL();
		System.out.println("Test URL: " + url);
		if (idiu.doesURLContainsSensitiveInformation(url) != null) {
			System.out.println(" contains sensitive info: " + idiu.doesURLContainsSensitiveInformation(url));
		}
		if (idiu.isCreditCard(url) != null) {
			System.out.println(" contains credit card: " + idiu.isCreditCard(url));
		}
		if (idiu.isEmailAddress(url) != null) {
			System.out.println(" contains email addr: " + idiu.isEmailAddress(url));
		}
		if (idiu.isUsSSN(url) != null) {
			System.out.println(" contains SSN: " + idiu.isUsSSN(url));
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("These should pass:");
		testUrl("http://www.google.com");
		testUrl("http://www.google.com/this/should/be/safe");

		System.out.println("These should fail:");
		testUrl("http://www.google.com/aaa?email=test@test.com");
		testUrl("http://www.google.com/bbb?aaa=bbb&email=test@test.com");
		testUrl("http://www.google.com/ccc?user=tester");
		testUrl("http://www.google.com/ddd?ssn=987-65-4320");
		testUrl("http://www.google.com/eee?ccn=378282246310005");
		
		
	}


}

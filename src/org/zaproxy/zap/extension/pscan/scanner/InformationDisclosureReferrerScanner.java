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
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PassiveScanner;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

public class InformationDisclosureReferrerScanner extends PluginPassiveScanner implements PassiveScanner {

	private PassiveScanThread parent = null;
	private String URLSensitiveInformationFile = "xml/URL-information-disclosure-messages.txt";
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		if (!isRequestedURLSameDomainAsHTTPReferrer(msg.getRequestHeader().getHostName(), msg.getRequestHeader().getHeader(HttpHeader.REFERER))) {
			Vector<String> referrer = msg.getRequestHeader().getHeaders(HttpHeader.REFERER);
			if (referrer.indexOf("?") > 0) {
				String parameter;
				if ((parameter = DoesURLContainsSensitiveInformation(referrer.toString())) != null) {
					this.raiseAlert(msg, id, parameter);
				}
				if (isCreditCard(msg.getRequestHeader().getURI().toString())) {
					this.raiseAlert(msg, id, "the URL in the referrer contains credit card informations");
				}
				if (isEmailAddress(msg.getRequestHeader().getURI().toString())) {
					this.raiseAlert(msg, id, "the URL in the referrer contains email address(es)");
				}
				if (isUsSSN(msg.getRequestHeader().getURI().toString())) {
					this.raiseAlert(msg, id, "the URL in the referrer contains US Social Security Number(s)");
				}
			}
		}
	}
	
	private boolean isRequestedURLSameDomainAsHTTPReferrer (String host, String referrerURL){
		boolean result = false;
		if(referrerURL.toLowerCase().contains(host.toLowerCase())){
			result = true;
		}
		return result;
	}
	
	private void raiseAlert(HttpMessage msg, int id, String infoDisclosureInURL) {
		Alert alert = new Alert(getId(), Alert.RISK_INFO, Alert.WARNING, 
		    	getName());
		    	alert.setDetail(
		    			"The HTTP Header may have leaked a potentially sensitive parameter to another domain. This can violate PCI and most organizational compliance policies. You can configure the list of strings for this check to add or remove values specific to your environment", 
		    	    msg.getRequestHeader().getURI().toString(),
		    	    infoDisclosureInURL,
		    	    "", 
		    	    "",
		    	    "Do not pass sensitive information in URI's", 
		            "", 
		            msg);
	
    	parent.raiseAlert(id, alert);
	}
	
	private String DoesURLContainsSensitiveInformation (String URL) {
		String line = null;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(URLSensitiveInformationFile));
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
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {

	}

	private int getId() {
		return 10025;
	}
	
	@Override
	public void setParent(PassiveScanThread parent) {
		this.parent = parent;
	}

	@Override
	public String getName() {
		return "Information disclosure - sensitive informations on HTTP Referrer header";
	}
	
	private boolean isEmailAddress(String emailAddress) {
		Pattern emailAddressPattern = Pattern.compile("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+<\\.[A-Z]{2,4}\\b");
		Matcher matcher = emailAddressPattern.matcher(emailAddress);
		if (matcher.find()) {
			return true;
		}
		return false;
	}
	
	private boolean isCreditCard(String creditCard) {
		Pattern creditCardPattern = Pattern.compile("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})\\b");
		Matcher matcher = creditCardPattern.matcher(creditCard);
		if (matcher.find()) {
			return true;
		}
		return false;
	}
	
	private boolean isUsSSN(String usSSN) {
		Pattern usSSNPattern = Pattern.compile("\\b[0-9]{3}-[0-9]{2}-[0-9]{4}\\b");
		Matcher matcher = usSSNPattern.matcher(usSSN);
		if (matcher.find()){
			return true;
		}
		return false;
	}
}

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

import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpBody;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PassiveScanner;

public class InformationDisclosureInURL extends PluginPassiveScanner implements PassiveScanner {

	private PassiveScanThread parent = null;
	private String URLSensitiveInformationFile = "xml/URL-information-disclosure-messages.txt";
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		if (msg.getRequestHeader().getURI().toString().indexOf("?") > 0) {
			if (DoesURLContainsSensitiveInformation(msg.getRequestHeader().getURI().toString())) {
				this.raiseAlert(msg, id, "");
			}
		}
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		
	}
	
	private void raiseAlert(HttpMessage msg, int id, String infoDisclosureInURL) {
		Alert alert = new Alert(getId(), Alert.RISK_INFO, Alert.WARNING, 
		    	getName());
		    	alert.setDetail(
		    			"The request appeared to contain sensitive information leaked in the URL. This can violate PCI and most organizational compliance policies. You can configure the list of strings for this check to add or remove values specific to your environment", 
		    	    msg.getRequestHeader().getURI().toString(),
		    	    infoDisclosureInURL,
		    	    "", 
		    	    "",
		    	    "Do not pass sensitive information in URI's", 
		            "", 
		            msg);
	
    	parent.raiseAlert(id, alert);
	}
	
	private boolean DoesURLContainsSensitiveInformation (String URL) {
		String line = null;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(URLSensitiveInformationFile));
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#") && URL.toLowerCase().contains(line.toLowerCase())) {
					return true;
				}
			}
		} catch (IOException e) {
			logger.debug("Error on opening/reading URL information disclosure file. Error:" + e.getMessage());
		}
		return false;
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

}

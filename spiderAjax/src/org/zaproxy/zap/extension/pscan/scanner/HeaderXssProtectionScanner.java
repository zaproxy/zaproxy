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

import java.util.Vector;

import net.htmlparser.jericho.Source;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;

public class HeaderXssProtectionScanner extends PluginPassiveScanner {

	private PassiveScanThread parent = null;
	
	@Override
	public void setParent(PassiveScanThread parent) {
		this.parent = parent;
	}

	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		if (msg.getResponseBody().length() > 0 && msg.getResponseHeader().isText()){
			Vector<String> xssHeaderProtection = msg.getResponseHeader().getHeaders(HttpHeader.X_XSS_PROTECTION);
			if (xssHeaderProtection != null) {
				for (String xssHeaderProtectionParam : xssHeaderProtection) {
					if (xssHeaderProtectionParam.toLowerCase().indexOf("1") < 0) {
						this.raiseAlert(msg, id, xssHeaderProtectionParam);
					}
				}
			}
		}
	}
	
	private void raiseAlert(HttpMessage msg, int id, String xssHeaderProtection) {
		Alert alert = new Alert(getId(), Alert.RISK_INFO, Alert.WARNING, 
		    	getName());
		    	alert.setDetail(
		    			"The x-xss-protection header has been disabled by the web application", 
		    	    msg.getRequestHeader().getURI().toString(),
		    	    xssHeaderProtection,
		    	    "", 
		    	    "",
		    	    "Enable the IE's XSS filter. If it must be disabled for any reasons, ensure that the application is properly protected against XSS vulnerability", 
		            "https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet", 
		            msg);
	
    	parent.raiseAlert(id, alert);
	}

	private int getId() {
		return 10016;
	}
	
	@Override
	public String getName() {
		return "IE8's XSS protection filter not disabled";
	}
	
}

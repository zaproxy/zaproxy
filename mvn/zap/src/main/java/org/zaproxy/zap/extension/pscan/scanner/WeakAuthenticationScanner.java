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
import org.zaproxy.zap.extension.pscan.PassiveScanner;


public class WeakAuthenticationScanner extends PluginPassiveScanner implements PassiveScanner {

	private PassiveScanThread parent = null;
	//private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void setParent (PassiveScanThread parent) {
		this.parent = parent;
	}

	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		// Ignore
	}

	private int getId() {
		return 10013;
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		if (msg.getRequestHeader().getSecure()) {
			// If SSL is used then the use of 'weak' authentication methods isnt really an issue
			return;
		}
		
		Vector<String> authHeaders = msg.getResponseHeader().getHeaders(HttpHeader.WWW_AUTHENTICATE);
		
		if (authHeaders != null) {
			for (String auth : authHeaders) {
				if (auth.toLowerCase().indexOf("basic") > -1 || auth.toLowerCase().indexOf("digest") > -1) {
				    Alert alert = new Alert(getId(), Alert.RISK_MEDIUM, Alert.WARNING, 
			    		"Weak HTTP authentication over an unsecured connection");
				    	alert.setDetail(
				    		"HTTP basic or digest authentication has been used over an unsecured connection. " +
				    		"The credentials can be read and then reused by someone with access to the network. ",
				    		msg.getRequestHeader().getURI().toString(),
				    		"", HttpHeader.WWW_AUTHENTICATE + ": " + auth, 
				    		"",
				    		"Protect the connection using HTTPS or use a stronger authentication mechanism", 
				            "www.owasp.org/index.php/Category:Authentication_Vulnerability", 
				            msg);
		
				    parent.raiseAlert(id, alert);
				}
			}
		}
	}
	
	@Override
	public String getName() {
		return "Weak authentication";
	}
}

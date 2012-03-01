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
import org.parosproxy.paros.network.HttpStatusCode;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PassiveScanner;

public class XFrameOptionScanner extends PluginPassiveScanner implements PassiveScanner  {

	private PassiveScanThread parent = null;
	
	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		if (msg.getResponseBody().length() > 0 && msg.getResponseHeader().isText() && HttpStatusCode.isSuccess(msg.getResponseHeader().getStatusCode())){
			Vector<String> xFrameOption = msg.getResponseHeader().getHeaders(HttpHeader.X_FRAME_OPTION);
			if (xFrameOption != null) {
				for (String xFrameOptionParam : xFrameOption) {
					if (xFrameOptionParam.toLowerCase().indexOf("deny") < 0 && xFrameOptionParam.toLowerCase().indexOf("sameorigin") < 0) {
						this.raiseAlert(msg, id, xFrameOptionParam, false);
					}
				}
			} else {
				this.raiseAlert(msg, id, "", true);
			}
		}
	}

	private void raiseAlert(HttpMessage msg, int id, String xFrameOption, boolean isXFrameOptionsMissing) {
		String issue = "X-Frame-Options header was not set for defense against 'ClickJacking' attacks";
		if (isXFrameOptionsMissing){
			issue = "X-Frame-Options header is not included in the HTTP response to protect against 'ClickJacking' attacks";
		}
		Alert alert = new Alert(getId(), Alert.RISK_INFO, Alert.WARNING, 
		    	getName());
		    	alert.setDetail(
		    			issue, 
		    	    msg.getRequestHeader().getURI().toString(),
		    	    xFrameOption,
		    	    "", 
		    	    "",
		    	    "Most modern Web browsers support the X-Frame-Options HTTP header, ensure it's set on all web pages returned by your site", 
		            "", 
		            msg);
	
    	parent.raiseAlert(id, alert);
	}
	
	@Override
	public void setParent(PassiveScanThread parent) {
		this.parent = parent;
	}

	@Override
	public String getName() {
		return "X-Frame-Options header not set";
	}

	private int getId() {
		return 10020;
	}
}

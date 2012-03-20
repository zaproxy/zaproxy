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


public class CacheControlScanner extends PluginPassiveScanner {

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
		if (msg.getRequestHeader().getSecure() && !msg.getRequestHeader().getURI().toString().toLowerCase().endsWith(".css") && !msg.getResponseHeader().isImage() && msg.getResponseBody().length() > 0) {
			Vector<String> cacheControl = msg.getResponseHeader().getHeaders(HttpHeader.CACHE_CONTROL);
			if (cacheControl != null) {
				for (String cacheControlDirective : cacheControl) {
					if (cacheControlDirective.toLowerCase().indexOf("no-store") < 0 || cacheControlDirective.toLowerCase().indexOf("no-cache") < 0 || cacheControlDirective.toLowerCase().indexOf("must-revalidate") < 0 || cacheControlDirective.toLowerCase().indexOf("private") < 0) {
						this.raiseAlert(msg, id, cacheControlDirective);
					}
				}
			}
			
			Vector<String> pragma = msg.getResponseHeader().getHeaders(HttpHeader.PRAGMA);
			if (pragma != null) {
				for (String pragmaDirective : pragma) {
					if (pragmaDirective.toLowerCase().indexOf("no-cache") < 0){
						this.raiseAlert(msg, id, pragmaDirective);
					}
				}
			}
		}
		
	}

	private void raiseAlert(HttpMessage msg, int id, String cacheControl) {
	    Alert alert = new Alert(getId(), Alert.RISK_LOW, Alert.WARNING, 
		    	getName());
		    	alert.setDetail(
		    	    "The cache-control and pragma HTTPHeader have not been set properly allowing the browser and proxies to cache content", 
		    	    msg.getRequestHeader().getURI().toString(),
		    	    cacheControl,
		    	    "", "", 
		    	    "Whenever possible ensure the cache-control HTTPHeader is set with no-cache, no-store, must-revalidate, private, and the pragma HTTPHeader is set with no-cache.", 
		            "https://www.owasp.org/index.php/Session_Management_Cheat_Sheet#Web_Content_Caching", 
		            msg);
	
    	parent.raiseAlert(id, alert);
	}
	
	private int getId() {
		return 10015;
	}
	
	@Override
	public String getName() {
		return "Incomplete or no cache-control and pragma HTTPHeader set";
	}

}

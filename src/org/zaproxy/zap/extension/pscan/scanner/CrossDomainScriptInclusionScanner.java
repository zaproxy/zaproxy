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

import java.util.List;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;


public class CrossDomainScriptInclusionScanner extends PluginPassiveScanner {

	private PassiveScanThread parent = null;
	private static final Logger logger = Logger.getLogger(CrossDomainScriptInclusionScanner.class);
	
	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		if (msg.getResponseBody().length() > 0 && msg.getResponseHeader().isText()){
			List<Element> sourceElements = source.getAllElements(HTMLElementName.SCRIPT);
			if (sourceElements != null) {
				for (Element sourceElement : sourceElements) {
					String src = sourceElement.getAttributeValue("src");
						if (src != null && isScriptFromOtherDomain(msg.getRequestHeader().getHostName(), src)) {
							this.raiseAlert(msg, id, src);
						}	
				}	
			}
		}
	}

	private void raiseAlert(HttpMessage msg, int id, String crossDomainScript) {
		Alert alert = new Alert(getId(), Alert.RISK_LOW, Alert.WARNING, 
		    	getName());
		    	alert.setDetail(
		    			"The page at the following URL includes one or more script files from a third-party domain", 
		    	    msg.getRequestHeader().getURI().toString(),
		    	    crossDomainScript,
		    	    "", 
		    	    "",
		    	    "Ensure JavaScript source files are loaded from only trusted sources, and the sources can't be controlled by end users of the application", 
		            "", 
		            msg);
	
    	parent.raiseAlert(id, alert);
	}
	
	@Override
	public void setParent(PassiveScanThread parent) {
		this.parent = parent;		
	}
	
	private int getId() {
		return 10017;
	}

	@Override
	public String getName() {
		return "Cross-domain JavaScript source file inclusion";
	}

	private boolean isScriptFromOtherDomain (String host, String scriptURL){
		if (!scriptURL.startsWith("//") && (scriptURL.startsWith("/") || scriptURL.startsWith("./") || scriptURL.startsWith("../"))) {
			return false;
		}
		boolean result = false;
		try {
			URI scriptURI = new URI(scriptURL, true);
			String scriptHost = scriptURI.getHost();
			if(scriptHost != null && !scriptHost.toLowerCase().equals(host.toLowerCase())){
				result = true;
			} 
		}catch (URIException e) {
			logger.debug("Error: " + e.getMessage());
		}
		return result;
	}
}

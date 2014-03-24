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
package org.zaproxy.zap.extension.script;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.network.HttpMessage;


public class ProxyListenerScript implements ProxyListener {

    // Should be the last one but one before the listener that saves the HttpMessage to the db 
    // so that anything can be changed
    public static final int PROXY_LISTENER_ORDER = ProxyListenerLog.PROXY_LISTENER_ORDER - 2;
	
	private ExtensionScript extension = null;

	private static final Logger logger = Logger.getLogger(ProxyListenerScript.class);

	public ProxyListenerScript(ExtensionScript extension) {
	    this.extension = extension;
	}
	
	@Override
	public int getArrangeableListenerOrder() {
		return PROXY_LISTENER_ORDER;
	}
	
	@Override
	public boolean onHttpRequestSend(HttpMessage msg) {
		for (ScriptWrapper script : extension.getScripts("proxy")) {
			if (script.isEnabled()) {
				try {
					if (! extension.invokeProxyScript(script, msg, true)) {
						// The script is telling us to drop this request
						return false;
					}
					
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		// No scripts, or they all passed
		return true;
	}

	@Override
	public boolean onHttpResponseReceive(HttpMessage msg) {
		for (ScriptWrapper script : extension.getScripts("proxy")) {
			if (script.isEnabled()) {
				try {
					if (! extension.invokeProxyScript(script, msg, false)) {
						// The script is telling us to drop this response
						return false;
					}
					
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		// No scripts, or they all passed
		return true;
	}
    
}

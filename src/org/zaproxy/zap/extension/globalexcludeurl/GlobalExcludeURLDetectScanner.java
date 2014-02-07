/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * Copyright 2014 Jay Ball - Aspect Security
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
package org.zaproxy.zap.extension.globalexcludeurl;

import java.util.List;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PassiveScanner;

/** TODO The GlobalExcludeURL functionality is currently alpha and subject to change.  */
public class GlobalExcludeURLDetectScanner implements PassiveScanner {

	private PassiveScanThread parent = null;

	@Override
	public void setParent (PassiveScanThread parent) {
		this.parent = parent;
	}

	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		// Ignore
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		ExtensionGlobalExcludeURL extAntiCSRF = 
			(ExtensionGlobalExcludeURL) Control.getSingleton().getExtensionLoader().getExtension(ExtensionGlobalExcludeURL.NAME);

		if (extAntiCSRF == null) {
			return;
		}

		List<GlobalExcludeURLToken> list = extAntiCSRF.getTokensFromResponse(msg, source);
		for (GlobalExcludeURLToken token : list) {
			if (parent != null) {
				parent.addTag(id, ExtensionGlobalExcludeURL.TAG);
			}
			extAntiCSRF.registerAntiCsrfToken(token);
		}
	}

	@Override
	public String getName() {
		return "Anti CSRF Token Detection";
	}

	@Override
	public boolean isEnabled() {
		// Always enabled
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {
		// Ignore
	}

	@Override
	public AlertThreshold getLevel() {
		// Always this level
		return AlertThreshold.MEDIUM;
	}

	@Override
	public void setLevel(AlertThreshold level) {
		// Ignore
	}

}

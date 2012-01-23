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
package org.zaproxy.zap.extension.httppanel.view.text;

import org.parosproxy.paros.network.HttpMessage;

abstract public class AutoDetectSyntaxHttpPanelTextArea extends HttpPanelTextArea {
	
	private static final long serialVersionUID = 293746373028878338L;
	
	private boolean isAutoDetectSyntax;
	
	public AutoDetectSyntaxHttpPanelTextArea(HttpMessage httpMessage) {
		super(httpMessage);
		
		isAutoDetectSyntax = true;
	}
	
	@Override
	public void setHttpMessage(HttpMessage httpMessage) {
		super.setHttpMessage(httpMessage);

		if (isAutoDetectSyntax) {
			detectAndSetSyntax();
		}
	}
	
	public boolean isAutoDetectSyntax() {
		return isAutoDetectSyntax;
	}

	public void setAutoDetectSyntax(boolean enabled) {
		isAutoDetectSyntax = enabled;
		if (isAutoDetectSyntax) {
			detectAndSetSyntax();
		}
	}

	private void detectAndSetSyntax() {
		final String syntax = detectSyntax(getHttpMessage());
		setSyntaxEditingStyle(syntax);
	}

	abstract protected String detectSyntax(HttpMessage httpMessage);
}

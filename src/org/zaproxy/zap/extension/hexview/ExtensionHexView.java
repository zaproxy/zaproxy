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
package org.zaproxy.zap.extension.hexview;

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.zaproxy.zap.view.HttpPanelManager;

public class ExtensionHexView extends ExtensionAdaptor {

	public ExtensionHexView() {
		super();
		initialize();
	}

	public ExtensionHexView(String name) {
		super(name);
	}

	private void initialize() {
		HttpPanelManager.getInstance().addPanelView(new HttpPanelHexView());
	}

}

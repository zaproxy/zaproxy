/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 ZAP development team
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
package org.zaproxy.zap.extension.ext;

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

public class ExtensionExtension extends ExtensionAdaptor {

	/*
	 * TODO For dev, not just this c;lass;)
	 * Record ext state in config file
	 * Disable exts on load based on config
	 * Add descs for all used exts
	 */
	
	public static final String NAME = "ExtensionExtension"; 
	
	private OptionsExtensionPanel optionsExceptionsPanel = null;
	
	public ExtensionExtension() {
		super();
		initialize();
	}

	private void initialize() {
        this.setName(NAME);
        //this.setOrder(0);
	}

	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

	    if (getView() != null) {
	        extensionHook.getHookView().addOptionPanel(getOptionsExtensionPanel());
	    }

	}

	private OptionsExtensionPanel getOptionsExtensionPanel() {
		if (optionsExceptionsPanel == null) {
			optionsExceptionsPanel = new OptionsExtensionPanel(this);
		}
		return optionsExceptionsPanel;
	}
}

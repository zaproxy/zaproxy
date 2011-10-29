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
package org.zaproxy.zap.extension.session;

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;

public class ExtensionSession extends ExtensionAdaptor implements SessionChangedListener {

	public static final String NAME = "ExtensionSession"; 
	
	private OptionsSessionPanel optionsSessionPanel = null;
	
	public ExtensionSession() {
		super();
		initialize();
	}

	private void initialize() {
        this.setName(NAME);

	}

	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

	    extensionHook.addSessionListener(this);

	    if (getView() != null) {
	        extensionHook.getHookView().addOptionPanel(getOptionsSessionPanel());
	    }

	}

	private OptionsSessionPanel getOptionsSessionPanel() {
		if (optionsSessionPanel == null) {
			optionsSessionPanel = new OptionsSessionPanel();
		}
		return optionsSessionPanel;
	}
	

	@Override
	public void sessionChanged(Session session) {
	}

	public SessionParam getParam() {
        return Model.getSingleton().getOptionsParam().getSessionParam();
	}

	public boolean isSessionToken(String name) {
		if (name == null) {
			return false;
		}
		return this.getParam().getTokens().contains(name.toLowerCase());
	}
	
}

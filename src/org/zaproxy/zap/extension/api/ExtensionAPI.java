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
package org.zaproxy.zap.extension.api;

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;

public class ExtensionAPI extends ExtensionAdaptor implements SessionChangedListener {

	private OptionsApiPanel optionsApiPanel = null;
	
    public ExtensionAPI() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionAPI(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionAPI");
        this.setOrder(10);

        API.getInstance().registerApiImplementor(new CoreAPI());
	}

	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
        extensionHook.addSessionListener(this);
	    if (getView() != null) {
	    	extensionHook.getHookView().addOptionPanel(getOptionsAPIPanel());
	    }

	}

	private OptionsApiPanel getOptionsAPIPanel() {
		if (optionsApiPanel== null) {
			optionsApiPanel = new OptionsApiPanel();
		}
		return optionsApiPanel;
	}

	@Override
	public void sessionChanged(Session session) {
		//API.getInstance().setSession(session);
	}

}

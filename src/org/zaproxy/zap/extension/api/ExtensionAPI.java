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

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JMenuItem;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.DesktopUtils;

public class ExtensionAPI extends ExtensionAdaptor implements SessionChangedListener {

	public static final String API_URL = "http://zap/";
	
	private OptionsApiPanel optionsApiPanel = null;
	private JMenuItem menuAPI = null;
	
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

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
        extensionHook.addSessionListener(this);
	    if (getView() != null) {
	    	extensionHook.getHookView().addOptionPanel(getOptionsAPIPanel());
	    	extensionHook.getHookMenu().addToolsMenuItem(getMenuAPI());
	    }

	}

	private OptionsApiPanel getOptionsAPIPanel() {
		if (optionsApiPanel== null) {
			optionsApiPanel = new OptionsApiPanel();
		}
		return optionsApiPanel;
	}

	private JMenuItem getMenuAPI() {
		if (menuAPI == null) {
			menuAPI = new JMenuItem();
			menuAPI.setText(Constant.messages.getString("api.menu.tools.url"));
			menuAPI.setEnabled(DesktopUtils.canOpenUrlInBrowser());
			
			menuAPI.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (API.getInstance().isEnabled()) {
						DesktopUtils.openUrlInBrowser(API_URL);
					} else {
						View.getSingleton().showWarningDialog(Constant.messages.getString("api.warning.enable"));
					}
				}
			});

		}
		return menuAPI;
	}

	@Override
	public void sessionChanged(Session session) {
		//API.getInstance().setSession(session);
	}

	@Override
	public void sessionAboutToChange(Session session) {
	}
	
	@Override
	public void sessionScopeChanged(Session session) {
	}
	
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("api.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	@Override
	public void sessionModeChanged(Mode mode) {
		// Ignore
	}
}

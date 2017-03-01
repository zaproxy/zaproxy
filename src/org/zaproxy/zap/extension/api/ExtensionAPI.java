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

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;

import javax.swing.JOptionPane;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.DesktopUtils;
import org.zaproxy.zap.view.ZapMenuItem;

public class ExtensionAPI extends ExtensionAdaptor {

	public static final String NAME = "ExtensionAPI";
	public static final String API_URL = "http://zap/";
	
	private OptionsApiPanel optionsApiPanel = null;
	private ZapMenuItem menuAPI = null;
    private CoreAPI coreApi = null;
	
    public ExtensionAPI() {
        super(NAME);
        this.setOrder(10);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	    	extensionHook.getHookView().addOptionPanel(getOptionsAPIPanel());
	    	extensionHook.getHookMenu().addToolsMenuItem(getMenuAPI());
	    }
        
        coreApi = new CoreAPI();
        coreApi.addApiOptions(extensionHook.getModel().getOptionsParam().getConnectionParam());

        extensionHook.addApiImplementor(coreApi);
        extensionHook.addApiImplementor(new ContextAPI());

	}

	private OptionsApiPanel getOptionsAPIPanel() {
		if (optionsApiPanel== null) {
			optionsApiPanel = new OptionsApiPanel();
		}
		return optionsApiPanel;
	}
	
	public static String generateApiKey() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);

	}

	private ZapMenuItem getMenuAPI() {
		if (menuAPI == null) {
			menuAPI = new ZapMenuItem("api.menu.tools.url");
			menuAPI.setEnabled(DesktopUtils.canOpenUrlInBrowser());
			
			menuAPI.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (!API.getInstance().isEnabled()) {
						String title = Constant.messages.getString("api.dialogue.browseApiNotEnabled.title");
						String message = Constant.messages.getString("api.dialogue.browseApiNotEnabled.message");
						String confirmButtonLabel = Constant.messages.getString("api.dialogue.browseApiNotEnabled.button.confirm.label");
						String cancelButtonLabel = Constant.messages.getString("api.dialogue.browseApiNotEnabled.button.cancel.label");
						
						int option = JOptionPane.showOptionDialog(View.getSingleton().getMainFrame(),
								message, title,
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
								null, new String[] { confirmButtonLabel, cancelButtonLabel}, null);
						
						if (option != JOptionPane.YES_OPTION) {
							return;
						}
						Model.getSingleton().getOptionsParam().getApiParam().setEnabled(true);
					}

					DesktopUtils.openUrlInBrowser(API_URL);
				}
			});

		}
		return menuAPI;
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
	
	public CoreAPI getCoreAPI() {
		return this.coreApi;
	}

    @Override
    public boolean supportsDb(String type) {
    	return true;
    }

    @Override
    public boolean supportsLowMemory() {
    	return true;
    }
}

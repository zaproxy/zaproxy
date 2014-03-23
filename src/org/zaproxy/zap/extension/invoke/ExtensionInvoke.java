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
package org.zaproxy.zap.extension.invoke;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.AbstractParamPanel;

public class ExtensionInvoke extends ExtensionAdaptor {

    private PopupMenuInvokers popupMenuInvokers;

	private OptionsInvokePanel optionsInvokePanel;
	
	/**
     * 
     */
    public ExtensionInvoke() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionInvoke(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setName("ExtensionInvoke");
        this.setOrder(46);
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	 
	    if (getView() != null) {
	        List<InvokableApp> apps = Model.getSingleton().getOptionsParam().getInvokeParam().getListInvokeEnabled();
	        
	        popupMenuInvokers = new PopupMenuInvokers();
	        popupMenuInvokers.setApps(apps);
	        
	        @SuppressWarnings("unused")
			ExtensionHookView pv = extensionHook.getHookView();
	        extensionHook.getHookView().addOptionPanel(getOptionsInvokePanel());

	    	extensionHook.getHookMenu().addPopupMenuItem(popupMenuInvokers);
		    
	    }
	}

	private AbstractParamPanel getOptionsInvokePanel() {
		if (optionsInvokePanel == null) {
			optionsInvokePanel = new OptionsInvokePanel(this);
		}
		return optionsInvokePanel;
	}

	protected void replaceInvokeMenus(List<InvokableApp> apps) {
		popupMenuInvokers.setApps(apps);
	}
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("invoke.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}
}

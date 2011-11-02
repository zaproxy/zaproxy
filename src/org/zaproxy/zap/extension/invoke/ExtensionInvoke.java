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

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;

public class ExtensionInvoke extends ExtensionAdaptor {

	private List <PopupMenuInvoke> invokeMenus = new ArrayList<PopupMenuInvoke>();
	private PopupMenuInvokeConfigure confPopup = null;
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
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionInvoke");
        
        List<InvokableApp> apps = Model.getSingleton().getOptionsParam().getInvokeParam().getListInvoke();
        
        for (InvokableApp app: apps) {
            PopupMenuInvoke pmi = new PopupMenuInvoke(app.getDisplayName());
            pmi.setCommand(app.getFullCommand());
            pmi.setWorkingDir(app.getWorkingDirectory());
            pmi.setParameters(app.getParameters());
            pmi.setCaptureOutput(app.isCaptureOutput());
            pmi.setOutputNote(app.isOutputNote());
            this.invokeMenus.add(pmi);
        	
        }
        confPopup = new PopupMenuInvokeConfigure();
	}
	
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	 
	    if (getView() != null) {
	        @SuppressWarnings("unused")
			ExtensionHookView pv = extensionHook.getHookView();
	        extensionHook.getHookView().addOptionPanel(getOptionsInvokePanel());

		    for (PopupMenuInvoke pmi : invokeMenus) {
		    	extensionHook.getHookMenu().addPopupMenuItem(pmi);
		    }

	    	extensionHook.getHookMenu().addPopupMenuItem(confPopup);
		    
	    }
	}

	private AbstractParamPanel getOptionsInvokePanel() {
		if (optionsInvokePanel == null) {
			optionsInvokePanel = new OptionsInvokePanel(this);
		}
		return optionsInvokePanel;
	}

	protected void replaceInvokeMenus(List<InvokableApp> apps) {
		// Delete existing ones
		View.getSingleton().getPopupMenu().removeMenu(confPopup);
    	for (PopupMenuInvoke pmi : invokeMenus) {
    		View.getSingleton().getPopupMenu().removeMenu(pmi);
	    }
        this.invokeMenus.clear();

        // Add the new ones
		for (InvokableApp app : apps) {
            PopupMenuInvoke pmi = new PopupMenuInvoke(app.getDisplayName());
            pmi.setCommand(app.getFullCommand());
            pmi.setWorkingDir(app.getWorkingDirectory());
            pmi.setParameters(app.getParameters());
            pmi.setCaptureOutput(app.isCaptureOutput());
            pmi.setOutputNote(app.isOutputNote());
    		View.getSingleton().getPopupMenu().addMenu(pmi);
            this.invokeMenus.add(pmi);
		}
		View.getSingleton().getPopupMenu().addMenu(confPopup);
	}
}

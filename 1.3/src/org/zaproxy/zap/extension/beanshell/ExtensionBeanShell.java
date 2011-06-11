/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Original code contributed by Stephen de Vries
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

package org.zaproxy.zap.extension.beanshell;

import javax.swing.JMenuItem;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionBeanShell extends ExtensionAdaptor {

	private BeanShellConsoleFrame beanShellConsoleDialog = null;
	private JMenuItem menuBeanShell = null;
	
    /**
     * 
     */
    public ExtensionBeanShell() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionBeanShell(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionBeanShell");
			
	}
	
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        ExtensionHookView pv = extensionHook.getHookView();	        
	        extensionHook.getHookMenu().addToolsMenuItem(getMenuBeanShell());
	    }
	}
	   
	private JMenuItem getMenuBeanShell() {
		if (menuBeanShell == null) {
		    menuBeanShell = new JMenuItem();
		    menuBeanShell.setText(Constant.messages.getString("beanshell.menu.title"));
		    menuBeanShell.addActionListener(new java.awt.event.ActionListener() { 
		    	public void actionPerformed(java.awt.event.ActionEvent e) {
		    	    BeanShellConsoleFrame dialog = getBeanShellConsoleDialog();
		    	    dialog.setVisible(true);
		    	}
		    });
		}
		return menuBeanShell;
		
	}

	
	BeanShellConsoleFrame getBeanShellConsoleDialog() {
		if (beanShellConsoleDialog == null) {
			beanShellConsoleDialog = new BeanShellConsoleFrame(getView().getMainFrame(), false, this);
			beanShellConsoleDialog.setView(getView());
			beanShellConsoleDialog.setSize(600, 600);
			beanShellConsoleDialog.setTitle(Constant.messages.getString("beanshell.title"));
		}
		return beanShellConsoleDialog;
	}
	

}

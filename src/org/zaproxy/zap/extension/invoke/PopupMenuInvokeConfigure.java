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

import java.awt.Component;

import javax.swing.JList;
import javax.swing.JTree;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionPopupMenu;

public class PopupMenuInvokeConfigure extends ExtensionPopupMenu {

	private static final long serialVersionUID = 1L;
    /**
     * @param label
     */
    public PopupMenuInvokeConfigure() {
        super();
        this.initialize();
    }

    @Override
    public boolean isSubMenu() {
    	return true;
    }
    
    @Override
    public String getParentMenuName() {
    	return Constant.messages.getString("invoke.site.popup");
    }

    @Override
    public int getParentMenuIndex() {
    	return 1;
    }

    public boolean precedeWithSeparator() {
    	return true;
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("invoke.config.popup"));
        
        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {
        		// Implement
        		Control.getSingleton().getMenuToolsControl().options(Constant.messages.getString("invoke.options.title"));
        	}
        });
	}
	
    @Override
    public boolean isEnableForComponent(Component invoker) {
		if (invoker instanceof JTree && invoker.getName().equals("treeSite")) {
            return true;
        } else if (invoker instanceof JList && invoker.getName().equals("ListLog")) {
            return true;
		}
        return false;
    }
}

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
package org.zaproxy.zap.extension.auth;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.text.JTextComponent;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;

public class PopupFlagLoggedInIndicatorMenu extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 1L;
    private JFrame parentFrame = null;
	private ExtensionAuth extension = null;
	private String selectedText = null;
    
    /**
	 * This method initializes 
	 * 
	 */
	public PopupFlagLoggedInIndicatorMenu(ExtensionAuth ext) {
		super();
		this.extension = ext;
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("auth.popup.login.indicate"));
		this.addActionListener(new java.awt.event.ActionListener() { 

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {    
                extension.setLoggedInIndicationRegex(selectedText);
    			// Show the relevant session dialog
    	        View.getSingleton().showSessionDialog(Model.getSingleton().getSession(), SessionAuthenticationPanel.PANEL_NAME);
			}
		});

	}
	
	@Override
	public boolean isSubMenu() {
		return true;
	}
   
	@Override
	public String getParentMenuName() {
		return Constant.messages.getString("flag.site.popup");
	}

	@Override
	public int getParentMenuIndex() {
		return FLAG_MENU_INDEX;
	}

	
    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker instanceof JTextComponent) {
        	JTextComponent txt = (JTextComponent) invoker;
			Container c = txt.getParent();
			boolean responsePanel = false;
			while (!(c instanceof JFrame)) {
				if (c instanceof HttpPanelResponse) {
					responsePanel = true;
					break;
				}
			    c = c.getParent();
			}
			if (! responsePanel) {
	        	selectedText = null;
	            return false;
			}

        	selectedText = txt.getSelectedText();
        	if (selectedText == null || selectedText.length() == 0) {
        		this.setEnabled(false);
        	} else {
        		this.setEnabled(true);
        	}
        	
            return true;
        } else {
        	selectedText = null;
            return false;
        }

    }

    /**
     * @return Returns the parentFrame.
     */
    public JFrame getParentFrame() {
        return parentFrame;
    }

    /**
     * @param parentFrame The parentFrame to set.
     */
    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    
}

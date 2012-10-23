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
package org.zaproxy.zap.extension.auth;

import java.awt.Component;
import java.awt.Container;
import java.text.MessageFormat;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.model.Context;

public class PopupFlagLoggedOutIndicatorMenu extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 1L;
    private JFrame parentFrame = null;
	private String selectedText = null;
	private int contextId;
    
    /**
	 * This method initializes 
	 * 
	 */
	public PopupFlagLoggedOutIndicatorMenu(Context ctx) {
		this.contextId = ctx.getIndex();
		
		this.setText(MessageFormat.format(Constant.messages.getString("auth.popup.logout.indicate"), ctx.getName()));
		this.addActionListener(new java.awt.event.ActionListener() { 

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				performAction();
			}
		});
	}

	public void performAction() {
	}

	@Override
	public boolean isSubMenu() {
		return true;
	}
   
	@Override
	public String getParentMenuName() {
		return Constant.messages.getString("context.flag.popup");
	}

	@Override
	public int getParentMenuIndex() {
		return CONTEXT_FLAG_MENU_INDEX;
	}

	
    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker instanceof JTextComponent) {
        	JTextComponent txt = (JTextComponent) invoker;

			boolean responsePanel = (SwingUtilities.getAncestorOfClass(HttpPanelResponse.class, txt) != null);

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

    public String getSelectedText() {
		return selectedText;
	}
    
	public int getContextId() {
		return contextId;
	}
    
}

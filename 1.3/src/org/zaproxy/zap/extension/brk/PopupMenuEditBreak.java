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
package org.zaproxy.zap.extension.brk;

import java.awt.Component;

import javax.swing.JList;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenu;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupMenuEditBreak extends ExtensionPopupMenu {

	private static final long serialVersionUID = 1L;

    private ExtensionBreak extension;

	private BreakEditDialog dialog = null;

	public BreakEditDialog getDialog() {
		return dialog;
	}

	/**
     * 
     */
    public PopupMenuEditBreak() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuEditBreak(String label) {
        super(label);
    }

	public void setExtension(ExtensionBreak extension) {
		this.extension = extension;
	}

    void showDialog(String msg) {
		dialog = new BreakEditDialog(extension.getView().getMainFrame(), false);
		dialog.setPlugin(this.extension);
		dialog.setVisible(true);
		dialog.getTxtDisplay().setText(msg);
		dialog.setCurrentBreakPoint(msg);
		
    }

    /**
	 * This method initialises this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("brk.edit.popup"));
        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {    
        		
                showDialog(extension.getSelectedBreakPoint());
        	}
        });

			
	}

    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals(BreakPointsPanel.PANEL_NAME)) {
            try {
                JList list = (JList) invoker;
                if (list.getSelectedIndex() >= 0) {
                    this.setEnabled(true);
                } else {
                    this.setEnabled(false);
                }
            } catch (Exception e) {}
            return true;
        }
        return false;
    }
    
}

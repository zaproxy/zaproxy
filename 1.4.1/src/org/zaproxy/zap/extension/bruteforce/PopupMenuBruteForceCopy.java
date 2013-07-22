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
package org.zaproxy.zap.extension.bruteforce;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;

public class PopupMenuBruteForceCopy extends ExtensionPopupMenuItem implements ClipboardOwner {

	private static final long serialVersionUID = 1L;
	private ExtensionBruteForce extension = null;
    private static Logger log = Logger.getLogger(PopupMenuBruteForceCopy.class);
    
    /**
     * 
     */
    public PopupMenuBruteForceCopy() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuBruteForceCopy(String label) {
        super(label);
 		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("bruteforce.copy.popup"));

        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {
                
                Object[] objs = extension.getBruteForcePanel().getBruteForceList().getSelectedValues();
                if (objs== null) {
                    return;
                }
                
                StringBuilder sb = new StringBuilder();
                for (Object obj: objs) {
                	BruteForceItem item = (BruteForceItem)obj;

                	sb.append(item.getUrl());
                	sb.append('\n');
                }
                setClipboardContents(sb.toString());
        	}
        });
	}
	
	private void setClipboardContents (String str) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents( new StringSelection(str), this );
	}
	
    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals(BruteForcePanel.PANEL_NAME)) {
            try {
                Object[] obj = extension.getBruteForcePanel().getBruteForceList().getSelectedValues();

                if (obj.length >= 1) {
                    this.setEnabled(true);
                } else {
                    this.setEnabled(false);
                }

            } catch (Exception e) {
            	log.error(e.getMessage(), e);
            }
            return true;
        }
        return false;
    }

        
    void setExtension(ExtensionBruteForce extension) {
        this.extension = extension;
    }

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// Do nothing
	}

}

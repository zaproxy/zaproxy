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

import org.apache.commons.httpclient.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenu;
import org.parosproxy.paros.model.HistoryReference;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupMenuAddBreakHistory extends ExtensionPopupMenu {

	private static final long serialVersionUID = 1L;
	private ExtensionBreak extension = null;
    private JList listLog = null;
    private static Log log = LogFactory.getLog(PopupMenuAddBreakHistory.class);
    
    /**
     * 
     */
    public PopupMenuAddBreakHistory() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuAddBreakHistory(String label) {
        super(label);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("brk.add.popup"));

        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {
                
                Object[] obj = listLog.getSelectedValues();
                if (obj.length != 1) {
                    return;
                }
                
                try {
                    HistoryReference ref = (HistoryReference) obj[0];
                    URI uri = ref.getHttpMessage().getRequestHeader().getURI();
                    
                    extension.showBreakAddDialog(uri.getURI());
                    
                } catch (Exception e1) {
                    extension.getView().showWarningDialog(Constant.messages.getString("brk.add.error.history"));
                }
        	}
        });

			
	}
	
    public boolean isEnableForComponent(Component invoker) {
        
        if (invoker.getName() != null && invoker.getName().equals("ListLog")) {
            try {
                JList list = (JList) invoker;
                listLog = list;
                Object[] obj = listLog.getSelectedValues();

                if (obj.length == 1) {
                    this.setEnabled(true);
                } else {
                    this.setEnabled(false);
                }

            } catch (Exception e) {
            	log.warn(e.getMessage(), e);
            }
            return true;
            
        }
        return false;
    }

        
    void setExtension(ExtensionBreak extension) {
        this.extension = extension;
    }

}

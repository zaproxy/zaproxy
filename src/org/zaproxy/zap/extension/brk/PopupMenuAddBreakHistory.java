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
import java.util.List;

import javax.swing.JList;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.HistoryReference;



public class PopupMenuAddBreakHistory extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 1L;
	private ExtensionBreak extension = null;
    private JList<HistoryReference> listLog = null;
    private static Logger log = Logger.getLogger(PopupMenuAddBreakHistory.class);
    
    
    public PopupMenuAddBreakHistory() {
        super();
 		initialize();
    }

    
    public PopupMenuAddBreakHistory(String label) {
        super(label);
    }

	
	private void initialize() {
        this.setText(Constant.messages.getString("brk.add.popup"));

        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {
                
                List<HistoryReference> values = listLog.getSelectedValuesList();
                if (values.size() != 1) {
                    return;
                }
                
                try {
                    extension.addUiBreakpoint(values.get(0).getHttpMessage());
                    
                } catch (Exception e1) {
                    extension.getView().showWarningDialog(Constant.messages.getString("brk.add.error.history"));
                }
        	}
        });
	}
	
    @Override
    public boolean isEnableForComponent(Component invoker) {
        
        if (invoker.getName() != null && invoker.getName().equals("ListLog")) {
            try {
                @SuppressWarnings("unchecked")
                JList<HistoryReference> list = (JList<HistoryReference>) invoker;
                
                listLog = list;
                List<HistoryReference> values = listLog.getSelectedValuesList();

                if (values.size() == 1 && extension.canAddBreakpoint()) {
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

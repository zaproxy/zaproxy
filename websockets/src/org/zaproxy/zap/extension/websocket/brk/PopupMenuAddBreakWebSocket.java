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
package org.zaproxy.zap.extension.websocket.brk;

import java.awt.Component;

import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.zaproxy.zap.extension.brk.ExtensionBreak;
import org.zaproxy.zap.extension.websocket.ui.WebSocketMessageDAO;
import org.zaproxy.zap.extension.websocket.ui.WebSocketTableModel;

public class PopupMenuAddBreakWebSocket extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 1L;
	private ExtensionBreak extension = null;
    private JTable tableWebSocket = null;
    private static Logger log = Logger.getLogger(PopupMenuAddBreakWebSocket.class);
    
    public PopupMenuAddBreakWebSocket() {
        super();
 		initialize();
    }

    public PopupMenuAddBreakWebSocket(String label) {
        super(label);
    }

	private void initialize() {
        this.setText(Constant.messages.getString("brk.add.popup"));

        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {
                
                int[] rows = tableWebSocket.getSelectedRows();
                if (rows.length != 1) {
                    return;
                }
                
                try {
                    WebSocketTableModel model = (WebSocketTableModel)tableWebSocket.getModel();
                    WebSocketMessageDAO dao = model.getMessages().get(rows[0]);
                    extension.addUiBreakpoint(dao);
                    
                } catch (Exception e1) {
                    extension.getView().showWarningDialog(Constant.messages.getString("brk.add.error.history"));
                }
        	}
        });
	}
	
    @Override
    public boolean isEnableForComponent(Component invoker) {
        
        if (invoker.getName() != null && invoker.getName().equals("websocket.table")) {
            try {
                tableWebSocket = (JTable) invoker;
                int[] rows = tableWebSocket.getSelectedRows();
                if (rows.length == 1 && extension.canAddBreakpoint()) {
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

        
    public void setExtension(ExtensionBreak extension) {
        this.extension = extension;
    }
}

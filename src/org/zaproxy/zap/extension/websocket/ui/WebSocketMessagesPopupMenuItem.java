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
package org.zaproxy.zap.extension.websocket.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.ui.WebSocketMessagesViewModel;

/**
 * Menu Item for a right click menu on the {@link WebSocketMessagesView}.
 */
abstract public class WebSocketMessagesPopupMenuItem extends ExtensionPopupMenuItem {
	private static final long serialVersionUID = 4774753835401981588L;

	private static final Logger logger = Logger.getLogger(WebSocketMessagesPopupMenuItem.class);
	
	/**
	 * Will be set by
	 * {@link WebSocketMessagesPopupMenuItem#isEnableForComponent(Component)}.
	 */
    private JTable messagesView = null;
    
    public WebSocketMessagesPopupMenuItem() {
        super();
 		initialize();
    }

	private void initialize() {
        setText(getMenuText());
        final WebSocketMessagesPopupMenuItem item = this;
        addActionListener(new ActionListener() {

        	@Override
        	public void actionPerformed(ActionEvent evt) {
        		try {
					item.performAction();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
        	}
        });
	}
    
	/**
	 * Title that appears as item text.
	 * 
	 * @return I18n name of this item.
	 */
    abstract protected String getMenuText();

    /**
     * What happens if choosen?
     * @throws Exception 
     */
	abstract protected void performAction() throws Exception;

	/**
	 * Which panel is allowed to show this popup item?
	 * 
	 * @return Name of caller.
	 */
	abstract protected String getInvokerName();
	
	/**
	 * More fine-grained control about enable-status of this item. Called by
	 * {@link WebSocketMessagesPopupMenuItem#isEnableForComponent(Component)}.
	 * 
	 * @return True
	 */
	protected boolean isEnabledExtended() {
		return true;
	}

	protected WebSocketMessageDTO getSelectedMessageDTO() {
		int index = getSelectedRow();
		if (index == -1) {
			return null;
		}
		
		WebSocketMessagesViewModel model = (WebSocketMessagesViewModel) messagesView.getModel();
        WebSocketMessageDTO message = model.getDTO(index);
        return message;
	}

	private int getSelectedRow() {
		int[] rows = messagesView.getSelectedRows();
	    if (rows.length != 1) {
	        return -1;
	    }
	    return rows[0];
	}
	
    @Override
    public final boolean isEnableForComponent(Component invoker) {        
        if (invoker.getName() != null && invoker.getName().equals(getInvokerName())) {
            try {
                messagesView = (JTable) invoker;
                int[] rows = messagesView.getSelectedRows();
                if (rows.length == 1 && isEnabledExtended()) {
                    setEnabled(true);
                } else {
                    setEnabled(false);
                }
            } catch (Exception e) {
            	logger.warn(e.getMessage(), e);
            }
            return true;
        }
        return false;
    }
}

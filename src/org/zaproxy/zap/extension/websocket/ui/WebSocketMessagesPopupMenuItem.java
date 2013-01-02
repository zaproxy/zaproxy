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
import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;

/**
 * Menu Item for a right click menu on the {@link WebSocketMessagesView}.
 */
public abstract class WebSocketMessagesPopupMenuItem extends ExtensionPopupMenuItem {
	private static final long serialVersionUID = 4774753835401981588L;

	private static final Logger logger = Logger.getLogger(WebSocketMessagesPopupMenuItem.class);
	
	/**
	 * Will be set by
	 * {@link WebSocketMessagesPopupMenuItem#isEnableForComponent(Component)}.
	 */
	private WebSocketPopupHelper wsPopupHelper;
    
    public WebSocketMessagesPopupMenuItem() {
        super();
 		initialize();
    }
    
    public WebSocketMessagesPopupMenuItem(String label) {
        super(label);
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
    protected abstract String getMenuText();

    /**
     * What happens if choosen?
     * @throws Exception 
     */
	protected abstract void performAction() throws WebServiceException;

	/**
	 * Which panel is allowed to show this popup item?
	 * 
	 * @return Name of caller.
	 */
	protected abstract String getInvokerName();
	
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
		return wsPopupHelper.getSelectedMessage();
	}
	
    @Override
    public final boolean isEnableForComponent(Component invoker) {        
        if (invoker.getName() != null && invoker.getName().equals(getInvokerName())) {
            try {
                wsPopupHelper = new WebSocketPopupHelper((JTable) invoker);
                boolean isOneSelected = wsPopupHelper.isOneRowSelected();
                if (isOneSelected && isEnabledExtended()) {
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

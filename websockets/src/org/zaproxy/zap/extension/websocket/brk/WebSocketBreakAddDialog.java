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

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.ui.WebSocketMessageDAO;

public class WebSocketBreakAddDialog extends WebSocketBreakDialog {

	private static final long serialVersionUID = 1L;
	private ActionListener actionListenerCancel;
	private ActionListener actionListenerSubmit;

	public WebSocketBreakAddDialog(WebSocketBreakpointsUiManagerInterface breakPointsManager, ComboBoxModel channelSelectModel) throws HeadlessException {
		super(breakPointsManager, channelSelectModel);
	}

	@Override
	protected String getBtnSubmitText() {
		return Constant.messages.getString("brk.add.button.add");
	}

	@Override
	protected String getDialogTitle() {
		return Constant.messages.getString("brk.add.title");
	}

	@Override
	protected ActionListener getActionListenerCancel() {
		if (actionListenerCancel == null) {
			actionListenerCancel = new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
				    breakPointsManager.hideAddDialog();
				}
			};
		}
		return actionListenerCancel;
	}

	@Override
	protected ActionListener getActionListenerSubmit() {
		if (actionListenerSubmit == null) {
			actionListenerSubmit = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String opcode = getSelectedOpcode();
					Integer channelId = getSelectedChannelId();
					String payloadPattern = getPayloadPattern();
					
				    breakPointsManager.addBreakpoint(new WebSocketBreakpointMessage(opcode, channelId, payloadPattern));
				    breakPointsManager.hideAddDialog();
				}
			};
		}
		return actionListenerSubmit;
	}
    
    public void setMessage(WebSocketMessageDAO aMessage) {
    	if (aMessage.readableOpcode != null) {
    		getOpcodeSelect().setSelectedItem(aMessage.readableOpcode);
    	} else {
    		getOpcodeSelect().setSelectedIndex(0);
    	}
    	
    	Integer channelId = aMessage.channelId;
    	if (channelId != null) {
    		setSelectedChannel(channelId);
    	} else {
    		getChannelSelect().setSelectedIndex(0);
    	}
    	
    	getPayloadPatternField().setText("");
    }
}

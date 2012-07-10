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

public class WebSocketBreakDialogEdit extends WebSocketBreakDialog {
	private static final long serialVersionUID = 1L;

	private ActionListener actionListenerCancel;
	private ActionListener actionListenerSubmit;
	
	private WebSocketBreakpointMessage breakpoint;

	public WebSocketBreakDialogEdit(WebSocketBreakpointsUiManagerInterface breakPointsManager, ComboBoxModel channelSelectModel) throws HeadlessException {
		super(breakPointsManager, channelSelectModel);
	}

	@Override
	protected String getBtnSubmitText() {
		return Constant.messages.getString("brk.edit.button.save");
	}

	@Override
	protected String getDialogTitle() {
		return Constant.messages.getString("brk.edit.title");
	}

	@Override
	protected ActionListener getActionListenerCancel() {
		if (actionListenerCancel == null) {
			actionListenerCancel = new ActionListener() { 

                @Override
                public void actionPerformed(ActionEvent e) {
                    breakpoint = null;
                    breakPointsManager.hideEditDialog();
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
				    breakPointsManager.editBreakpoint(breakpoint, getWebSocketBreakpointMessage());
                    breakpoint = null;
                    breakPointsManager.hideEditDialog();
				}
			};
		}
		return actionListenerSubmit;
	}

	public void setBreakpoint(WebSocketBreakpointMessage breakpoint) {
        resetDialogValues();
        
        this.breakpoint = breakpoint;
        
        setDialogValues(breakpoint.getOpcode(), breakpoint.getChannelId(), breakpoint.getPayloadPattern(), breakpoint.getDirection());
    }
}

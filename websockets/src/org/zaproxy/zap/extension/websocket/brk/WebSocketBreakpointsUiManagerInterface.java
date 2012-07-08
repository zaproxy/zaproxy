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

import org.zaproxy.zap.extension.brk.BreakpointMessageInterface;
import org.zaproxy.zap.extension.brk.BreakpointsUiManagerInterface;
import org.zaproxy.zap.extension.brk.ExtensionBreak;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.websocket.ExtensionWebSocket;
import org.zaproxy.zap.extension.websocket.ui.WebSocketMessageDAO;

public class WebSocketBreakpointsUiManagerInterface implements BreakpointsUiManagerInterface {

    private WebSocketBreakAddDialog addDialog = null;
    private WebSocketBreakEditDialog editDialog = null;

    private ExtensionBreak extensionBreak;
    private ExtensionWebSocket extensionWebSocket;
    
    public WebSocketBreakpointsUiManagerInterface(ExtensionBreak extensionBreak, ExtensionWebSocket extensionWebSocket) {
        this.extensionBreak = extensionBreak;
        this.extensionWebSocket = extensionWebSocket;
    }
    
    @Override
    public Class<WebSocketMessageDAO> getMessageClass() {
        return WebSocketMessageDAO.class;
    }
    
    @Override
    public Class<WebSocketBreakpointMessage> getBreakpointClass() {
        return WebSocketBreakpointMessage.class;
    }
    
    @Override
    public String getType() {
        return "WebSocket";
    }

    @Override
    public void handleAddBreakpoint(Message aMessage) {
        extensionBreak.dialogShown(ExtensionBreak.DialogType.ADD);
        showAddDialog(aMessage);
    }

    void addBreakpoint(WebSocketBreakpointMessage breakpoint) {
        extensionBreak.addBreakpoint(breakpoint);
    }

    @Override
    public void handleEditBreakpoint(BreakpointMessageInterface breakpoint) {
        extensionBreak.dialogShown(ExtensionBreak.DialogType.EDIT);
        showEditDialog((WebSocketBreakpointMessage)breakpoint);
    }

    void editBreakpoint(BreakpointMessageInterface oldBreakpoint, BreakpointMessageInterface newBreakpoint) {
        extensionBreak.editBreakpoint(oldBreakpoint, newBreakpoint);
    }

    @Override
    public void handleRemoveBreakpoint(BreakpointMessageInterface breakpoint) {
        extensionBreak.removeBreakpoint(breakpoint);
    }

    @Override
    public void reset() {
    }
    
    private void populateAddDialogAndSetVisible(Message aMessage) {
        addDialog.setMessage((WebSocketMessageDAO)aMessage);
        addDialog.setVisible(true);
    }
    
    private void showAddDialog(Message aMessage) {
        if (addDialog == null) {
            addDialog = new WebSocketBreakAddDialog(this, extensionWebSocket.getChannelComboBoxModel());
            populateAddDialogAndSetVisible(aMessage);
        } else if (!addDialog.isVisible()) {
            populateAddDialogAndSetVisible(aMessage);
        }
    }

    void hideAddDialog() {
        addDialog.dispose();
        extensionBreak.dialogClosed();
    }
    
    private void populateEditDialogAndSetVisible(WebSocketBreakpointMessage breakpoint) {
        editDialog.setBreakpoint(breakpoint);
        editDialog.setVisible(true);
    }
    
    private void showEditDialog(WebSocketBreakpointMessage breakpoint) {
        if (editDialog == null) {
            editDialog = new WebSocketBreakEditDialog(this, extensionWebSocket.getChannelComboBoxModel());
            populateEditDialogAndSetVisible(breakpoint);
        } else if (!editDialog.isVisible()) {
            populateEditDialogAndSetVisible(breakpoint);
        }
    }

    void hideEditDialog() {
        editDialog.dispose();
        extensionBreak.dialogClosed();
    }
}

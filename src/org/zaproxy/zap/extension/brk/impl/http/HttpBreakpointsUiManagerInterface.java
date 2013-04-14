/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.brk.impl.http;

import org.parosproxy.paros.extension.ExtensionHookMenu;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.brk.BreakpointMessageInterface;
import org.zaproxy.zap.extension.brk.BreakpointsUiManagerInterface;
import org.zaproxy.zap.extension.brk.ExtensionBreak;
import org.zaproxy.zap.extension.httppanel.Message;


public class HttpBreakpointsUiManagerInterface implements BreakpointsUiManagerInterface {

    private BreakAddDialog addDialog = null;
    private BreakEditDialog editDialog = null;
    
    private ExtensionBreak extensionBreak;

    private PopupMenuAddBreakSites popupMenuAddBreakSites = null;
    private PopupMenuAddBreakHistory popupMenuAddBreakHistory = null;
    
    public HttpBreakpointsUiManagerInterface(ExtensionHookMenu hookMenu, ExtensionBreak extensionBreak) {
        this.extensionBreak = extensionBreak;
        
        hookMenu.addPopupMenuItem(getPopupMenuAddBreakSites());
        hookMenu.addPopupMenuItem(getPopupMenuAddBreakHistory());
    }
    
    @Override
    public Class<HttpMessage> getMessageClass() {
        return HttpMessage.class;
    }
    
    @Override
    public Class<HttpBreakpointMessage> getBreakpointClass() {
        return HttpBreakpointMessage.class;
    }
    
    @Override
    public String getType() {
        return "HTTP";
    }

    @Override
    public void handleAddBreakpoint(Message aMessage) {
        extensionBreak.dialogShown(ExtensionBreak.DialogType.ADD);
        showAddDialog(aMessage);
    }
    
    public void handleAddBreakpoint(String url) {
        extensionBreak.dialogShown(ExtensionBreak.DialogType.ADD);
        showAddDialog(url);
    }

    void addBreakpoint(HttpBreakpointMessage breakpoint) {
        extensionBreak.addBreakpoint(breakpoint);
    }

    @Override
    public void handleEditBreakpoint(BreakpointMessageInterface breakpoint) {
        extensionBreak.dialogShown(ExtensionBreak.DialogType.EDIT);
        showEditDialog((HttpBreakpointMessage)breakpoint);
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
        addDialog.setMessage((HttpMessage)aMessage);
        addDialog.setVisible(true);
    }
    
    private void populateAddDialogAndSetVisible(String url) {
        addDialog.setUrl(url);
        addDialog.setVisible(true);
    }
    
    private void showAddDialog(Message aMessage) {
        if (addDialog == null) {
            addDialog = new BreakAddDialog(this);
            populateAddDialogAndSetVisible(aMessage);
        } else if (!addDialog.isVisible()) {
            populateAddDialogAndSetVisible(aMessage);
        }
    }
    
    private void showAddDialog(String url) {
        if (addDialog == null) {
            addDialog = new BreakAddDialog(this);
            populateAddDialogAndSetVisible(url);
        } else if (!addDialog.isVisible()) {
            populateAddDialogAndSetVisible(url);
        }
    }

    void hideAddDialog() {
        addDialog.dispose();
        extensionBreak.dialogClosed();
    }
    
    private void populateEditDialogAndSetVisible(HttpBreakpointMessage breakpoint) {
        editDialog.setBreakpoint(breakpoint);
        editDialog.setVisible(true);
    }
    
    private void showEditDialog(HttpBreakpointMessage breakpoint) {
        if (editDialog == null) {
            editDialog = new BreakEditDialog(this);
            populateEditDialogAndSetVisible(breakpoint);
        } else if (!editDialog.isVisible()) {
            populateEditDialogAndSetVisible(breakpoint);
        }
    }

    void hideEditDialog() {
        editDialog.dispose();
        extensionBreak.dialogClosed();
    }

    private PopupMenuAddBreakSites getPopupMenuAddBreakSites() {
        if (popupMenuAddBreakSites == null) {
            popupMenuAddBreakSites = new PopupMenuAddBreakSites(extensionBreak, this);
        }
        return popupMenuAddBreakSites;
    }
    
    private PopupMenuAddBreakHistory getPopupMenuAddBreakHistory() {
        if (popupMenuAddBreakHistory == null) {
            popupMenuAddBreakHistory = new PopupMenuAddBreakHistory(extensionBreak);
        }
        return popupMenuAddBreakHistory;
    }
    
}

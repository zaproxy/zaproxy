
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
            popupMenuAddBreakHistory = new PopupMenuAddBreakHistory();
            popupMenuAddBreakHistory.setExtension(extensionBreak);
        }
        return popupMenuAddBreakHistory;
    }
    
}

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

import java.awt.Dimension;

import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.ExtensionHookMenu;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.brk.BreakpointMessageInterface;
import org.zaproxy.zap.extension.brk.BreakpointsUiManagerInterface;
import org.zaproxy.zap.extension.brk.ExtensionBreak;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.model.StructuralSiteNode;


public class HttpBreakpointsUiManagerInterface implements BreakpointsUiManagerInterface {

    private BreakAddEditDialog breakDialog = null;
    
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
        showAddDialog(aMessage);
    }
    
    public void handleAddBreakpoint(String url) {
        showAddDialog(url, HttpBreakpointMessage.Match.regex);
    }

    void addBreakpoint(HttpBreakpointMessage breakpoint) {
        extensionBreak.addBreakpoint(breakpoint);
    }

    @Override
    public void handleEditBreakpoint(BreakpointMessageInterface breakpoint) {
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
    
    private void populateAddDialogAndSetVisible(
    		String url, HttpBreakpointMessage.Match match) {
    	breakDialog.init(
    			new HttpBreakpointMessage(url, HttpBreakpointMessage.Location.url, 
    					match, false, true), 
    			true);
    	breakDialog.setVisible(true);
    }
    
    private void showAddDialog(Message aMessage) {
    	HttpBreakpointMessage.Match match = HttpBreakpointMessage.Match.regex;
    	HttpMessage msg = (HttpMessage) aMessage;
    	String regex = "";
    	
    	if (msg.getHistoryRef() != null && 
    			msg.getHistoryRef().getSiteNode() != null) {
        	try {
				regex = new StructuralSiteNode(
						msg.getHistoryRef().getSiteNode()).getRegexPattern(false);
			} catch (DatabaseException e) {
				// Ignore
			}
    	}
    	if (regex.length() == 0 && msg.getRequestHeader().getURI() != null) {
    		// Just use the escaped url
    		regex = msg.getRequestHeader().getURI().toString();
    		match = HttpBreakpointMessage.Match.contains;
    	}
        this.showAddDialog(regex, match);
    }
    
    private void showAddDialog(String url, HttpBreakpointMessage.Match match) {
        if (breakDialog == null) {
        	breakDialog = new BreakAddEditDialog(this, View.getSingleton().getMainFrame(), new Dimension(407, 255));
        }
        populateAddDialogAndSetVisible(url, match);
    }
    
    private void populateEditDialogAndSetVisible(HttpBreakpointMessage breakpoint) {
    	breakDialog.init(breakpoint, false);
    	breakDialog.setVisible(true);
    }
    
    private void showEditDialog(HttpBreakpointMessage breakpoint) {
        if (breakDialog == null) {
        	breakDialog = new BreakAddEditDialog(this, View.getSingleton().getMainFrame(), new Dimension(407, 255));
        }
        populateEditDialogAndSetVisible(breakpoint);
    }

    private PopupMenuAddBreakSites getPopupMenuAddBreakSites() {
        if (popupMenuAddBreakSites == null) {
            popupMenuAddBreakSites = new PopupMenuAddBreakSites(this);
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

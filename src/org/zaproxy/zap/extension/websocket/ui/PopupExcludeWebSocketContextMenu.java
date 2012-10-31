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
package org.zaproxy.zap.extension.websocket.ui;

import java.awt.Component;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.zaproxy.zap.extension.stdmenus.PopupContextExcludeMenu;
import org.zaproxy.zap.model.Context;

/**
 * Allows to reuse the existing 'Exclude in Context' menu for WebSocket URLs.
 */
public class PopupExcludeWebSocketContextMenu extends PopupContextExcludeMenu {
	
	private static final long serialVersionUID = -5195960463302710531L;
	
	static final String MENU_NAME = Constant.messages.getString("websocket.context.exclude.popup");
	
    @Override
    public String getParentMenuName() {
    	return MENU_NAME;
    }
	
	@Override
	public boolean isEnableForComponent(Component invoker) {
		String invokerName = invoker.getName();
		if (invokerName != null && invokerName.equals(WebSocketMessagesView.PANEL_NAME)) {
	        reCreateSubMenu();
	        return true;
	    }
		return false;
	}
	
    @Override
    protected ExtensionPopupMenuItem createPopupExcludeFromContextMenu(Context context) {
    	return new PopupExcludeWebSocketFromContextMenu(context);
	}
}

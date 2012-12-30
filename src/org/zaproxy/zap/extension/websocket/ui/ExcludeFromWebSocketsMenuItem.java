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

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.websocket.ExtensionWebSocket;
import org.zaproxy.zap.extension.websocket.WebSocketChannelDTO;
import org.zaproxy.zap.extension.websocket.WebSocketException;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.db.TableWebSocket;

/**
 * Menu Item for Popup. Used in WebSockets tab, when you click on some message
 * with right mouse button.
 */
public class ExcludeFromWebSocketsMenuItem extends WebSocketMessagesPopupMenuItem {
	private static final long serialVersionUID = 2208451830578743381L;

	private static final Logger logger = Logger.getLogger(ExcludeFromWebSocketsMenuItem.class);
    
    private final TableWebSocket table;

	private ExtensionWebSocket extWs;
    
    public ExcludeFromWebSocketsMenuItem(ExtensionWebSocket extWs, TableWebSocket table) {
        super();

        this.extWs = extWs;
        this.table = table;
    }

	@Override
	protected String getMenuText() {
		return Constant.messages.getString("websocket.session.exclude.title");
	}

	@Override
	protected void performAction() {
		String regex = buildRegexForSelectedChannel();
        if (regex != null) {
        	List<String> ignoreList = extWs.getChannelIgnoreList();
        	ignoreList.add(regex);
            try {
				extWs.setChannelIgnoreList(ignoreList);
			} catch (WebSocketException e) {
				logger.error(e.getMessage(), e);
			}
            View.getSingleton().showSessionDialog(Model.getSingleton().getSession(),
            		SessionExcludeFromWebSocket.PANEL_NAME);
        }
	}
	
	@Override
	protected boolean isEnabledExtended() {
		boolean isEnabled = true;
		WebSocketChannelDTO channel = getSelectedChannelDTO();
		if (channel != null && extWs.isChannelIgnored(channel)) {
			// already ignored, do not enable menu item
			isEnabled = false;
		}
		return isEnabled;
	}
	
	@Override
	protected String getInvokerName() {
		return WebSocketMessagesView.PANEL_NAME;
	}

	protected String buildRegexForSelectedChannel() {
		WebSocketChannelDTO channel = getSelectedChannelDTO();
        if (channel == null) {
        	return null;
        }
    	
    	return "\\Q" + channel.getFullUri() + "\\E";
	}

	private WebSocketChannelDTO getSelectedChannelDTO() {
		WebSocketMessageDTO message = getSelectedMessageDTO();
		if (message == null) {
			return null;
		}
		
		WebSocketChannelDTO channel = new WebSocketChannelDTO();
		channel.id = message.channel.id;
		
		try {
			List<WebSocketChannelDTO> channels = table.getChannels(channel);
			if (channels.size() == 1) {
				return channels.get(0);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		
		return null;
	}
	
	@Override
	public boolean isSafe() {
		return true;
	}
}

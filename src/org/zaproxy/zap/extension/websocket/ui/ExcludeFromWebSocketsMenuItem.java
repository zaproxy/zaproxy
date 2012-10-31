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
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.websocket.WebSocketChannelDTO;
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
    
    public ExcludeFromWebSocketsMenuItem(TableWebSocket table) {
        super();

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
        	Session session = Model.getSingleton().getSession();
            session.getExcludeFromWebSocketRegexs().add(regex);
            
            View.getSingleton().showSessionDialog(session, ExcludeFromWebSocketSessionPanel.PANEL_NAME);
        }
	}
	
	@Override
	protected boolean isEnabledExtended() {
		String regex = buildRegexForSelectedChannel();
    	if (regex != null) {
    		// it may or may not be excluded => test if this entry is in list
    		// if contained, then it is already excluded => disable item
    		return !Model.getSingleton().getSession().getExcludeFromWebSocketRegexs().contains(regex);
    	}
		return true;
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
        
    	StringBuilder regex = new StringBuilder();
    	regex.append("\\Q");
    	if (channel.host.matches("[^:]/")) {
    		regex.append(channel.host.replaceFirst("([^:])/", "$1:" + channel.port + "/"));
    	} else {
    		regex.append(channel.host);
    		regex.append(":");
    		regex.append(channel.port);
    	}
    	regex.append("\\E");
    	
    	return regex.toString();
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

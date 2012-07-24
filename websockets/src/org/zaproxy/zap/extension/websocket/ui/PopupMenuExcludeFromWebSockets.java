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
import java.sql.SQLException;
import java.util.List;

import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.websocket.WebSocketChannelDAO;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDAO;
import org.zaproxy.zap.extension.websocket.db.TableWebSocket;
import org.zaproxy.zap.extension.websocket.ui.WebSocketTableModel;

/**
 * Menu Item for Popup. Used in WebSockets tab, when you click on some message
 * with right mouse button.
 */
public class PopupMenuExcludeFromWebSockets extends ExtensionPopupMenuItem {
    private static final Logger logger = Logger.getLogger(PopupMenuExcludeFromWebSockets.class);
	private static final long serialVersionUID = 1L;
	
	// will be set by isEnableForComponent()
    private JTable messagesLog = null;
    
    private final TableWebSocket table;
    
    public PopupMenuExcludeFromWebSockets(TableWebSocket table) {
        super();

        this.table = table;
        
 		initialize();
    }

	private void initialize() {
        setText(Constant.messages.getString("websocket.session.exclude.title"));
        addActionListener(new ActionListener() {

        	@Override
        	public void actionPerformed(ActionEvent evt) {
        		String regex = buildRegexForSelectedChannel();
                if (regex != null) {
                	Session session = Model.getSingleton().getSession();
	                session.getExcludeFromWebSocketRegexs().add(regex);
	                
	                View.getSingleton().showSessionDialog(session, SessionExcludeFromWebSocketPanel.PANEL_NAME);
                }
        	}
        });
	}

	protected String buildRegexForSelectedChannel() {
		WebSocketChannelDAO dao = getSelectedChannelDAO();
        if (dao == null) {
        	return null;
        }
        
    	StringBuilder regex = new StringBuilder();
    	regex.append("\\Q");
    	if (dao.host.matches("[^:]/")) {
    		regex.append(dao.host.replaceFirst("([^:])/", "$1:" + dao.port + "/"));
    	} else {
    		regex.append(dao.host);
    		regex.append(":");
    		regex.append(dao.port);
    	}
    	regex.append("\\E");
    	
    	return regex.toString();
	}

	private WebSocketChannelDAO getSelectedChannelDAO() {
		WebSocketMessageDAO msgDao = getSelectedMessageDAO();
		if (msgDao == null) {
			return null;
		}
		WebSocketChannelDAO channelDao = new WebSocketChannelDAO();
		channelDao.channelId = msgDao.channelId;
		
		try {
			List<WebSocketChannelDAO> resultDAOs = table.getChannels(channelDao);
			if (resultDAOs.size() == 1) {
				return resultDAOs.get(0);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		
		return null;
	}

	private WebSocketMessageDAO getSelectedMessageDAO() {
		int index = getSelectedRow();
		if (index == -1) {
			return null;
		}
		
		WebSocketTableModel model = (WebSocketTableModel) messagesLog.getModel();
        WebSocketMessageDAO dao = model.getDAO(index);
        return dao;
	}

	private int getSelectedRow() {
		int[] rows = messagesLog.getSelectedRows();
	    if (rows.length != 1) {
	        return -1;
	    }
	    return rows[0];
	}
	
    @Override
    public boolean isEnableForComponent(Component invoker) {        
        if (invoker.getName() != null && invoker.getName().equals("websocket.table")) {
            try {
                messagesLog = (JTable) invoker;
                int[] rows = messagesLog.getSelectedRows();
                if (rows.length == 1 && !isAlreadyExcluded()) {
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
        
    private boolean isAlreadyExcluded() {
    	String regex = buildRegexForSelectedChannel();
    	if (regex != null) {
    		// it may or may not be excluded => test if this entry is in list
    		return Model.getSingleton().getSession().getExcludeFromWebSocketRegexs().contains(regex);
    	}
		return false;
	}
}

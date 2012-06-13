/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.TabbedPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.tab.Tab;
import org.zaproxy.zap.extension.websocket.ExtensionWebSocket;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.view.SiteMapListener;

/**
 * Represents the WebSockets tab and manages several {@link WebSocketUiChannel}
 * instances and displays them when the appropriate WebSockets HTTP handshake
 * was selected from the sites tab.
 */
public class WebSocketUiPanel extends AbstractPanel implements Tab, SiteMapListener, ChangeListener {

	private static final long serialVersionUID = -2853099315338427006L;

	private static final Logger log = Logger.getLogger(WebSocketUiPanel.class);
    
    /**
     * Used to attach/detach tab.
     */
	private JTabbedPane tabbedPanel;
    
	/**
	 * Contains the UI representation of each {@link WebSocketProxy}.
	 */
	private Map<String, WebSocketUiChannel> channels;
	
	/**
	 * Contains hash of currently selected HTTP message node.
	 */
	private String selectedChannelHash;

	public WebSocketUiPanel() {
		initializePanel();
		
		channels = new HashMap<String, WebSocketUiChannel>();
	}
	
	/**
	 * Sets up this tab and installs a change listener.
	 */
	private void initializePanel() {
		View view = View.getSingleton();
        view.getSiteTreePanel().addSiteMapListenner(this);
		tabbedPanel = view.getWorkbench().getTabbedWork();
		tabbedPanel.addChangeListener(this);
		
		setIcon(new ImageIcon(View.class.getResource("/resource/icon/16/029.png")));
    	setName(Constant.messages.getString("http.panel.websocket.title"));
    	
		revalidate();
	}

	/**
	 * Create new UI panel for displaying WebSockets communication from one
	 * channel.
	 * 
	 * @return
	 */
	public WebSocketObserver createNewUiChannel(String handshakeHash) {
		WebSocketUiChannel channel = new WebSocketUiChannel(new WebSocketUiModel());
		
		channels.put(handshakeHash, channel);
		
		return channel;
	}

	/**
	 * Called when a node in the Sites tree is selected.
	 * If the selected HTTP-communication is a WebSocket upgrade,
	 * then show WebSockets tab.
	 */
	@Override
	public void nodeSelected(SiteNode node) {
		HttpMessage msg = null;
		
        try {
            msg = node.getHistoryReference().getHttpMessage();
        } catch (Exception e) {
        	log.warn(e.getMessage(), e);
        }

		if (msg != null && msg.isWebSocketUpgrade()) {
			// show tab
			attachTab();
			
			// retrieve WebSocketsProxyId
			selectedChannelHash = ExtensionWebSocket.createHandshakeHash(msg);
		} else {
			// hide tab (also on exception)
			detachTab();
		}
	}
    
	/**
	 * Show WebSockets tab when HTTP WebSockets handshake is chosen.
	 */
    private void attachTab() {
        tabbedPanel.addTab(getName(), getIcon(), this);
	}
    
    /**
     * Hide WebSockets tab
     */
    private void detachTab() {
        tabbedPanel.remove(this);
    }
    
    /**
     * Add WebSockets communication to the tab.
     * @param channel
     */
    private void attachCommuncation(WebSocketUiChannel channel) {
    	detachCommunication();
		Dimension targetSize = tabbedPanel.getSize();
		channel.setPreferredSize(new Dimension(targetSize.width - 50, targetSize.height - 50));
		add(channel);
		revalidate();
    }
    
    /**
     * Remove WebSockets communication from the tab.
     * @param webSocketProxyId
     */
    private void detachCommunication() {
    	removeAll();
    }

    /**
     * Tab has changed.
     */
	@Override
	public void stateChanged(ChangeEvent e) {
		Component component = ((TabbedPanel) e.getSource()).getSelectedComponent();
		if (component instanceof WebSocketUiPanel) {
			// WebSockets tab selected - show WebSockets communication of selected node
			if (selectedChannelHash != null && channels.containsKey(selectedChannelHash)) {
				attachCommuncation(channels.get(selectedChannelHash));
			}
		}
	}
}

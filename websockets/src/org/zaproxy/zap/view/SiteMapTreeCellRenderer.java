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
package org.zaproxy.zap.view;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.SiteMapPanel;
import org.zaproxy.zap.extension.websocket.ExtensionWebSocket;
import org.zaproxy.zap.extension.websocket.ui.WebSocketPanel;

/**
 * Custom {@link TreeCellRenderer} for {@link SiteMapPanel} to set custom icons
 * and tooltips. If you want tooltips you have to enable them via:
 * <code>ToolTipManager.sharedInstance().registerComponent(tree);</code>
 */
public class SiteMapTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -4278691012245035225L;

	private static Logger log = Logger.getLogger(SiteMapPanel.class);

	private ExtensionWebSocket extWebSocket;

	/**
	 * Sets custom tree node logos.
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		SiteNode node = null;
		if (value instanceof SiteNode) {
			node = (SiteNode) value;
		}

		if (leaf && isWebSocketNode(value)) {
			// WebSocket icon
			ExtensionWebSocket extWebSocket = getExtWebSocket();
			if (extWebSocket != null && extWebSocket.isConnected(getHttpMessageFromNode(value))) {
				if (node.isIncludedInScope()) {
					setIcon(WebSocketPanel.connectTargetIcon);
				} else {
					setIcon(WebSocketPanel.connectIcon);
				}
			} else {
				if (node.isIncludedInScope()) {
					setIcon(WebSocketPanel.disconnectTargetIcon);
				} else {
					setIcon(WebSocketPanel.disconnectIcon);
				}
			}
		} else if (node != null) {
			// folder / file icons with scope 'target' if relevant
			if (node.isRoot()) {
				setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/094.png")));	// 'World' icon
			} else if (leaf) {
				if (node.isIncludedInScope()) {
					setIcon(new ImageIcon(getClass().getResource("/resource/icon/fugue/document-target.png")));
				} else {
					setIcon(new ImageIcon(getClass().getResource("/resource/icon/fugue/document.png")));
				}
			} else {
				if  (expanded) {
					if (node.isIncludedInScope()) {
						setIcon(new ImageIcon(getClass().getResource("/resource/icon/fugue/folder-horizontal-open-target.png")));
					} else {
						setIcon(new ImageIcon(getClass().getResource("/resource/icon/fugue/folder-horizontal-open.png")));
					}
				} else {
					if (node.isIncludedInScope()) {
						setIcon(new ImageIcon(getClass().getResource("/resource/icon/fugue/folder-horizontal-target.png")));
					} else {
						setIcon(new ImageIcon(getClass().getResource("/resource/icon/fugue/folder-horizontal.png")));
					}
				}
			}
		}

		return this;
	}

	private ExtensionWebSocket getExtWebSocket() {
		if (extWebSocket == null) {
			extWebSocket = (ExtensionWebSocket) Control.getSingleton().getExtensionLoader().getExtension(ExtensionWebSocket.NAME);
		}
		return extWebSocket;
	}

	/**
	 * Returns true if the given node is a WebSockets HTTP handshake.
	 * 
	 * @param value
	 * @return
	 */
	private boolean isWebSocketNode(Object value) {
		HttpMessage msg = null;
		
		msg = getHttpMessageFromNode(value);
		
		if (msg != null && msg.isWebSocketUpgrade()) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Extract HttpMessage out of {@link SiteMap} node.
	 * 
	 * @param value
	 * @return
	 */
	private HttpMessage getHttpMessageFromNode(Object value) {
		SiteNode node = null;
		if (value instanceof SiteNode) {
			node = (SiteNode) value;
	
			if (node.getHistoryReference() != null) {
				try {
					// TODO: When a new session is created, a HttpMalformedHeaderException is received here, but why?
					HttpMessage msg = node.getHistoryReference().getHttpMessage();
	
					return msg;
				} catch (Exception e) {
					log.warn(e.getMessage(), e);
				}
			}
		}
		return null;
	}
}

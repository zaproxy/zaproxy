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

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.zaproxy.zap.extension.websocket.WebSocketChannelDTO;

/**
 * Displays a connected or disconnected plug beside channel name.
 */
public class ComboBoxChannelRenderer extends JLabel implements ListCellRenderer<WebSocketChannelDTO> {
	private static final long serialVersionUID = 1749702362670451484L;

	public ComboBoxChannelRenderer() {
		setOpaque(true);
	}

	/*
	 * This method finds the image and text corresponding to the selected value
	 * and returns the label, set up to display the text and image.
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends WebSocketChannelDTO> list, WebSocketChannelDTO channel,
			int index, boolean isSelected, boolean cellHasFocus) {
				
		if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setForeground(list.getForeground());
            
            // set alternating background colors
            if (index % 2 == 0) {
            	setBackground(Color.WHITE);
            } else {
                setBackground(list.getBackground());
            }
        }
		
		// avoid usage of index param, as it is not always valid
		// a channel id of -1 indicates a non-WebSocketChannel item
		String text = "";
		if (channel != null) {
			text = channel.toString();
			
			if (channel.id != null) {
				setWebSocketIcon(channel);
			} else {
				// unset icon
				setIcon(null);
			}
		}
		
		setText("<html><p style=\"padding:3px;white-space:nowrap;\">" + text + "</p></html>");
		setFont(list.getFont());

		return this;
	}

	private void setWebSocketIcon(WebSocketChannelDTO channel) {
		Boolean isConnected = channel.isConnected();
		if (isConnected != null && isConnected) {
			if (channel.isInScope()) {
				setIcon(WebSocketPanel.connectTargetIcon);
			} else {
				setIcon(WebSocketPanel.connectIcon);
			}
		} else {
			if (channel.isInScope()) {
				setIcon(WebSocketPanel.disconnectTargetIcon);
			} else {
				setIcon(WebSocketPanel.disconnectIcon);
			}
		}
	}
}

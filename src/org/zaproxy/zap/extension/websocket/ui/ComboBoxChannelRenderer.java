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
public class ComboBoxChannelRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = 1749702362670451484L;

	public ComboBoxChannelRenderer() {
		setOpaque(true);
	}

	/*
	 * This method finds the image and text corresponding to the selected value
	 * and returns the label, set up to display the text and image.
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		WebSocketChannelDTO channel = (WebSocketChannelDTO) value;
				
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
			} else {
				// unset icon
				setIcon(null);
			}
		}
		
		setText("<html><p style=\"padding:3px;white-space:nowrap;\">" + text + "</p></html>");
		setFont(list.getFont());

		return this;
	}
}

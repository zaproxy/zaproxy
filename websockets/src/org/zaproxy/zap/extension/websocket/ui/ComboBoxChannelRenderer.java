package org.zaproxy.zap.extension.websocket.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Displays a connected or disconnected plug beside channel name.
 */
public class ComboBoxChannelRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = 1L;

	public ComboBoxChannelRenderer() {
		setOpaque(true);
	}

	/*
	 * This method finds the image and text corresponding to the selected value
	 * and returns the label, set up to display the text and image.
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		ComboBoxChannelItem item = (ComboBoxChannelItem) value;
				
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
		if (item != null && item.getChannelId() != -1) {
			if (item.isConnected()) {
				setIcon(WebSocketPanel.connectIcon);
			} else {
				setIcon(WebSocketPanel.disconnectIcon);
			}
		} else {
			// unset icon
			setIcon(null);
		}
		
		setText("<html><p style=\"padding: 3px;\">" + value.toString() + "</p></html>");
		setFont(list.getFont());

		return this;
	}
}

package org.zaproxy.zap.extension.websocket.ui;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketChannelDTO;
import org.zaproxy.zap.utils.SortedComboBoxModel;

public class ComboBoxChannelModel extends SortedComboBoxModel {
	private static final long serialVersionUID = 1L;
	
	public ComboBoxChannelModel() {
		super();
		init();
	}

	/**
	 * Adds first element that is used as a wildcard entry.
	 */
	private void init() {
		String text = Constant.messages.getString("websocket.dialog.channel.select_all");
		WebSocketChannelDTO allChannelsItem = new WebSocketChannelDTO(text);
		addElement(allChannelsItem);
	}

	/**
	 * Removes all elements but the first.
	 */
	public void reset() {
		synchronized (this) {
			removeAllElements();
			init();
		}
	}

	/**
	 * Removes and re-adds element. Does not loose selected item.
	 * 
	 * @param channel
	 */
	public void updateElement(WebSocketChannelDTO channel) {
		synchronized (this) {
			boolean isSelected = false;
			Object selectedItem = getSelectedItem();
			if (selectedItem != null && selectedItem.equals(channel)) {
				isSelected = true;
			}
			
			removeElement(channel);
			addElement(channel);
			
			if (isSelected) {
				setSelectedItem(channel);
			}
		}
	}
	
	public void setSelectedChannelId(Integer channelId) {
		if (channelId == null) {
			setSelectedItem(getElementAt(0));
			return;
		}
		
		for (int i = 0; i < getSize(); i++) {
			WebSocketChannelDTO channel = (WebSocketChannelDTO) getElementAt(i);
			if (channel.id != null && channel.id.equals(channelId)) {
				setSelectedItem(channel);
				return;
			}
		}
	}
}

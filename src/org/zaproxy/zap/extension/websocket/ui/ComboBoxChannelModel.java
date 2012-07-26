package org.zaproxy.zap.extension.websocket.ui;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketChannelDAO;
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
		String text = Constant.messages.getString("websocket.toolbar.channel.select");
		WebSocketChannelDAO allChannelsItem = new WebSocketChannelDAO(text);
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
	 * @param dao
	 */
	public void updateElement(WebSocketChannelDAO dao) {
		synchronized (this) {
			boolean isSelected = false;
			if (getSelectedItem().equals(dao)) {
				isSelected = true;
			}
			
			removeElement(dao);
			addElement(dao);
			
			if (isSelected) {
				setSelectedItem(dao);
			}
		}
	}
}

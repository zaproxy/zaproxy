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
		String text = Constant.messages.getString("websocket.dialog.channel.select_all");
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
			Object selectedItem = getSelectedItem();
			if (selectedItem != null && selectedItem.equals(dao)) {
				isSelected = true;
			}
			
			removeElement(dao);
			addElement(dao);
			
			if (isSelected) {
				setSelectedItem(dao);
			}
		}
	}
	
	public void setSelectedChannelId(Integer channelId) {
		if (channelId == null) {
			setSelectedItem(getElementAt(0));
			return;
		}
		
		for (int i = 0; i < getSize(); i++) {
			WebSocketChannelDAO dao = (WebSocketChannelDAO) getElementAt(i);
			if (dao.channelId.equals(channelId)) {
				setSelectedItem(dao);
				return;
			}
		}
	}
}

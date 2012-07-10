package org.zaproxy.zap.extension.websocket.ui;

import java.util.HashMap;
import java.util.Map;

import org.zaproxy.zap.utils.SortedComboBoxModel;

public class ComboBoxChannelModel extends SortedComboBoxModel {
	private static final long serialVersionUID = 1L;
	
	private Map<Integer, ComboBoxChannelItem> items;
	
	public ComboBoxChannelModel() {
		this.items = new HashMap<Integer, ComboBoxChannelItem>();
	}

	public ComboBoxChannelItem getByChannelId(int channelId) {
		return items.get(channelId);
	}
	
	public void addElement(ComboBoxChannelItem item) {
		super.addElement(item);
		
		items.put(item.getChannelId(), item);
	}
}

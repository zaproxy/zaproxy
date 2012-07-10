package org.zaproxy.zap.extension.websocket.ui;

import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;

public class ComboBoxChannelItem implements Comparable<Object> {
	private final Integer channelId;
	private final String label;
	private WebSocketProxy wsProxy;

	public ComboBoxChannelItem(String name, WebSocketProxy wsProxy) {
		this.label = name;
		this.channelId = wsProxy.getChannelId();
		this.wsProxy = wsProxy;
	}

	public ComboBoxChannelItem(String name) {
		this.label = name;
		this.channelId = -1;
	}

	public Integer getChannelId() {
		return channelId;
	}
	
	public boolean isConnected() {
		return wsProxy.isConnected();
	}

	public String toString() {
		return label;
	}

	/**
	 * Used for sorting items. If two items have identical names, the channel
	 * number is used to determine order.
	 */
	@Override
	public int compareTo(Object o) {
		String otherString = o.toString().replaceAll("#[0-9]+", "");
		String thisString = toString().replaceAll("#[0-9]+", "");
		int result = thisString.compareTo(otherString);

		if (result == 0) {
			ComboBoxChannelItem otherItem = (ComboBoxChannelItem) o;
			return getChannelId().compareTo(otherItem.getChannelId());
		}

		return result;
	}

	public HistoryReference getHandshakeReference() {
		if (wsProxy != null) {
			return wsProxy.getHandshakeReference();
		}
		return null;
	}
}
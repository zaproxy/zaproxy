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

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketChannelDTO;
import org.zaproxy.zap.utils.SortedListModel;

public class ChannelSortedListModel extends SortedListModel<WebSocketChannelDTO> {

	private static final long serialVersionUID = 83057590716441165L;
	
	private boolean isShowJustInScope = false;
	private List<WebSocketChannelDTO> hiddenChannels = new ArrayList<>();

	public ChannelSortedListModel() {
		super();
		init();
	}

	/**
	 * Adds first element that is used as a wildcard entry.
	 */
	private void init() {
		trimToSize();
		ensureCapacity(3);

		String text = Constant.messages.getString("websocket.dialog.channel.select_all");
		WebSocketChannelDTO allChannelsItem = new WebSocketChannelDTO(text);
		super.addElement(allChannelsItem);
	}

	/**
	 * Removes all elements but the first.
	 */
	public void reset() {
		synchronized (this) {
			clear();
			init();
		}
	}

	/**
	 * Notifies all {@code EventListenerList} that the state of the connection
	 * of the given {@code WebSocketChannelDTO channel} has been changed. If the
	 * given {@code channel} is not present in this {@code ComboBoxChannelModel}
	 * no action is taken.
	 * <p>
	 * Should be called when the state of the connection of the given
	 * {@code channel} changes to {@code WebSocketProxy.State.CLOSED}.
	 * </p>
	 * <p>
	 * <strong>Implementation Note:</strong> Only the field
	 * {@code WebSocketChannelDTO.endTimestamp} is updated because it's the only
	 * field that is used to tell how the connection state is displayed.
	 * </p>
	 * 
	 * @param channel the channel that has changed.
	 */
	public void updateElement(WebSocketChannelDTO channel) {
		synchronized (this) {
			final int index = indexOf(channel);
			if (index != -1) {
				WebSocketChannelDTO old = getElementAt(index);
				old.endTimestamp = channel.endTimestamp;

				fireContentsChanged(this, index, index);
			}
		}
	}
	
	@Override
	public void addElement(WebSocketChannelDTO channel) {
		if (!isShowJustInScope || (isShowJustInScope && channel.isInScope())) {
			super.addElement(channel);
		} else {
			hiddenChannels.add(channel);
		}
	}

	/**
	 * Notifies all {@code EventListenerList} that the element at the given
	 * {@code index} has changed.
	 * 
	 * @param index the index of the element that has changed.
	 */
	public void elementChanged(int index) {
		fireContentsChanged(this, index, index);
	}

	/**
	 * Entries from this model are hidden, when given parameter is
	 * <code>true</code>. When <code>false</code> they are shown again.
	 * 
	 * @param isShow
	 */
	public void setShowJustInScope(boolean isShow) {
		isShowJustInScope = isShow;
		
		// first, show all channels
		for (WebSocketChannelDTO hiddenChannel : hiddenChannels) {
			super.addElement(hiddenChannel);
		}
		hiddenChannels.clear();
		
		// second, hide those not in scope if enabled
		if (isShowJustInScope) {
			for (int i = getSize() - 1; i > 0; i--) {
				WebSocketChannelDTO channel = getElementAt(i);
				if (!channel.isInScope()) {
					hiddenChannels.add(channel);
					removeElement(channel);
				}
			}
		}
		
		fireContentsChanged(this, 1, getSize());
	}

}
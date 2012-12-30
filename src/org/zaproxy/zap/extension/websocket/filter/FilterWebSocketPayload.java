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
package org.zaproxy.zap.extension.websocket.filter;

import java.util.List;
import java.util.regex.Matcher;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.filter.FilterAbstractReplace;
import org.parosproxy.paros.extension.filter.FilterReplaceDialog;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.websocket.ExtensionWebSocket;
import org.zaproxy.zap.extension.websocket.WebSocketException;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;
import org.zaproxy.zap.extension.websocket.ui.ChannelSortedListModel;

/**
 * Base class for filtering WebSockets traffic.
 */
public class FilterWebSocketPayload extends FilterAbstractReplace implements WebSocketFilter {

	public static final int FILTER_ID = 85;
	private FilterWebSocketReplaceDialog wsFilterReplaceDialog;
	private boolean shouldApplyOnIncoming = false;
	private boolean shouldApplyOnOutgoing = false;
	private List<String> applicableOpcodes;
	private List<Integer> applicableChannelIds;
	private ChannelSortedListModel channelsModel;
	private ExtensionWebSocket extension;

	public FilterWebSocketPayload(ExtensionWebSocket extension, ChannelSortedListModel model) {
		super();
		this.extension = extension;
		this.channelsModel = model;
	}

	@Override
	public int getId() {
		return FILTER_ID;
	}

	@Override
	public String getName() {
		return Constant.messages.getString("websocket.filter.payload.name");
	}

	@Override
	public void onHttpRequestSend(HttpMessage httpMessage) {
		// do not want to change HTTP request
	}

	@Override
	public void onHttpResponseReceive(HttpMessage httpMessage) {
		// do not want to change HTTP response
	}

	/**
	 * Change message payload if pattern applies. Ignore binary messages so far
	 * and apply only to other opcodes.
	 * 
	 * @param wsMessage
	 * @throws WebSocketException 
	 */
	@Override
	public void onWebSocketPayload(WebSocketMessage wsMessage) throws WebSocketException {
		WebSocketMessageDTO message = wsMessage.getDTO();
		if (!extension.isSafe(message)) {
			return;
		}
		
		boolean isApplicableMessage = (wsMessage.isFinished() && wsMessage.getPayloadLength() > 0);
		boolean hasPattern = (getPattern() != null);
		
		if (isApplicableMessage && hasPattern) {
			if (isApplicableDirection(wsMessage.getDirection()) &&
					isApplicableOpcode(message.readableOpcode) &&
					isApplicableChannelId(message.channel.id)) {

				String from = wsMessage.getReadablePayload();

				Matcher matcher = getPattern().matcher(from);

				try {
					String to = matcher.replaceAll(getReplaceText());
	
					if (!from.equals(to)) {
						wsMessage.setReadablePayload(to);
					}
				} catch (IllegalArgumentException e) {
					// e.g.: Illegal group reference when '$'-sign is used
					throw new WebSocketException("Replacement text of WebSocket payload filter contains non-escaped characters (\\,$).", e);
				} catch (StringIndexOutOfBoundsException e) {
					// e.g.: String index out of range: 1 when '\' is used
					throw new WebSocketException("Replacement text of WebSocket payload filter contains non-escaped characters (\\,$).", e);
				}
			}
		}
	}

	private boolean isApplicableDirection(Direction direction) {
		return ((direction.equals(Direction.INCOMING) && shouldApplyOnIncoming) ||
				(direction.equals(Direction.OUTGOING) && shouldApplyOnOutgoing));
	}

	private boolean isApplicableOpcode(String opcode) {
		if (applicableOpcodes != null) {
			return applicableOpcodes.contains(opcode);
		}
		return true;
	}

	private boolean isApplicableChannelId(Integer channelId) {
		if (applicableChannelIds != null) {
			return applicableChannelIds.contains(channelId);
		}
		return true;
	}

	@Override
	protected FilterReplaceDialog getFilterReplaceDialog() {
		if (wsFilterReplaceDialog == null) {
			wsFilterReplaceDialog = new FilterWebSocketReplaceDialog(getView().getMainFrame(), true, channelsModel);
		}
		return wsFilterReplaceDialog;
	}

	@Override
	protected void processFilterReplaceDialog(FilterReplaceDialog dialog) {
		// retrieve pattern and replace text
		super.processFilterReplaceDialog(dialog);

		FilterWebSocketReplaceDialog wsDialog = (FilterWebSocketReplaceDialog) dialog;

		// find out if pattern should be applied for IN- & OUTGOING messages
		shouldApplyOnIncoming = wsDialog.isIncomingChecked();
		shouldApplyOnOutgoing = wsDialog.isOutgoingChecked();
		
		applicableOpcodes = wsDialog.getOpcodes();
		applicableChannelIds = wsDialog.getChannelIds();
	}
	
	/**
	 * After resetting this filter, it is disabled
	 * and its values are set to default values.
	 */
	@Override
	public void reset() {
		this.setEnabled(false);
		shouldApplyOnIncoming = false;
		shouldApplyOnOutgoing = false;
		applicableOpcodes = null;
		applicableChannelIds = null;
	}
}

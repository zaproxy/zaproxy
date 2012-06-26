/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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

import java.util.regex.Matcher;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.filter.ExtensionFilter;
import org.parosproxy.paros.extension.filter.FilterAbstractReplace;
import org.parosproxy.paros.extension.filter.FilterReplaceDialog;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;

/**
 * Base class for filtering WebSockets traffic.
 */
public class FilterWebSocketPayload extends FilterAbstractReplace {

	public static final int FILTER_ID = 85;
	private FilterWebSocketReplaceDialog wsFilterReplaceDialog;
	private boolean shouldApplyOnIncoming = false;
	private boolean shouldApplyOnOutgoing = false;

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
	 * and apply only to other opcodes. Called by
	 * {@link ExtensionFilter#onMessageFrame(int, WebSocketMessage)}.
	 */
	@Override
	public void onWebSocketPayload(WebSocketMessage message) {
		Direction direction = message.getDirection();

		boolean isApplicable = (!message.isFinished() || message.getPayloadLength() == 0 || message.isBinary());
		boolean hasPattern = (getPattern() == null);
		boolean shouldBeApplied = ((direction.equals(Direction.INCOMING) && shouldApplyOnIncoming) ||
				(direction.equals(Direction.OUTGOING) && shouldApplyOnOutgoing));

		if (isApplicable && hasPattern && shouldBeApplied) {
			String from = message.getReadablePayload();

			Matcher matcher = getPattern().matcher(from);

			String to = matcher.replaceAll(getReplaceText());

			if (!from.equals(to)) {
				message.setReadablePayload(to);
			}
		}
	}

	@Override
	protected FilterReplaceDialog getFilterReplaceDialog() {
		if (wsFilterReplaceDialog == null) {
			wsFilterReplaceDialog = new FilterWebSocketReplaceDialog(getView().getMainFrame(), true);
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
	}
}

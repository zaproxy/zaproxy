package org.zaproxy.zap.extension.websocket.ui.httppanel.views.large;

import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.largeresponse.LargeResponseUtil;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;

public class WebSocketLargePayloadUtil extends LargeResponseUtil {

	public static boolean isLargePayload(Message aMessage) {
		if (aMessage instanceof WebSocketMessageDTO) {
			WebSocketMessageDTO message = (WebSocketMessageDTO) aMessage;
			if (message.payloadLength == null) {
				return false;
			}
			return message.payloadLength > minContentLength;
		}

		return false;
	}
}
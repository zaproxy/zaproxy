package org.zaproxy.zap.extension.websocket.ui.httppanel.views.large;

import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.largeresponse.ExtensionHttpPanelLargeResponseView;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDAO;

public class WebSocketLargePayloadUtil {
    
    private static int minContentLength;
    
    static {
    	restoreDefaultMinContentLength();
    }

	public static int getMinContentLength() {
		return minContentLength;
	}

	public static void setMinContentLength(int aMinContentLength) {
		minContentLength = aMinContentLength;
	}

	public static void restoreDefaultMinContentLength() {
		minContentLength = ExtensionHttpPanelLargeResponseView.MIN_CONTENT_LENGTH;
	}

	public static boolean isLargePayload(Message aMessage) {
		if (aMessage instanceof WebSocketMessageDAO) {
			WebSocketMessageDAO dao = (WebSocketMessageDAO) aMessage;
			if (dao.payloadLength == null) {
				return false;
			}
			return dao.payloadLength > minContentLength;
		}

		return false;
	}
}
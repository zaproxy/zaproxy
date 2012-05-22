package org.zaproxy.zap.extension.websocket;

import java.io.IOException;

/**
 * Reports a WebSocket-specific error.
 */
@SuppressWarnings("serial")
public class WebSocketException extends IOException {

	public WebSocketException() {
	}

	public WebSocketException(String msg) {
		super(msg);
	}

	public WebSocketException(Exception e) {
		super(e);
	}
}

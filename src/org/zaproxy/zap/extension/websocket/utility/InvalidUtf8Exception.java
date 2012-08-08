package org.zaproxy.zap.extension.websocket.utility;

public class InvalidUtf8Exception extends Exception {
	private static final long serialVersionUID = -9098080058643816518L;

	public InvalidUtf8Exception(String message) {
		super(message);
	}
}

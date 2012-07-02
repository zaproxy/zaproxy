package org.zaproxy.zap.extension.websocket.ui;

public class CommunicationChannel {
	private String domain;
	private int port;
	
	public CommunicationChannel(String domain, int port) {
		this.domain = domain;
		this.port = port;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public int getPort() {
		return port;
	}
}
package org.zaproxy.zap.extension.websocket.db;

/**
 * Wraps id of message & channel together into one class.
 */
public class WebSocketMessagePrimaryKey implements Comparable<WebSocketMessagePrimaryKey>{
	private Integer channelId;
	private Integer messageId;
	
	public WebSocketMessagePrimaryKey(Integer channelId, Integer messageId) {
		this.channelId = channelId;
		this.messageId = messageId;
	}

	@Override
	public int compareTo(WebSocketMessagePrimaryKey other) {
		int result = channelId.compareTo(other.getChannelId());
		
		if (result == 0) {
			result = messageId.compareTo(other.getMessageId());
		}
		
		return result;
	}
	
	public Integer getMessageId() {
		return messageId;
	}
	
	public Integer getChannelId() {
		return channelId;
	}
	
	public String toString() {
		return "#" + channelId + "." + messageId;
	}
}
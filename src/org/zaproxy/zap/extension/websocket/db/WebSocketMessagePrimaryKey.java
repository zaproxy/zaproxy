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
package org.zaproxy.zap.extension.websocket.db;

/**
 * Wraps id of message & channel together into one class.
 */
public class WebSocketMessagePrimaryKey implements Comparable<WebSocketMessagePrimaryKey>{
	private final Integer channelId;
	private final Integer messageId;
	
	public WebSocketMessagePrimaryKey(Integer channelId, Integer messageId) {
		this.channelId = channelId;
		this.messageId = messageId;
	}
	
	public Integer getMessageId() {
		return messageId;
	}
	
	public Integer getChannelId() {
		return channelId;
	}
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		
		strBuilder.append('#').append(channelId).append('.');
		
		if (messageId != null) {
			strBuilder.append(messageId);
		} else {
			strBuilder.append('-');
		}
		
		return strBuilder.toString();
	}

	@Override
	public int compareTo(WebSocketMessagePrimaryKey other) {
		int result = channelId.compareTo(other.getChannelId());
		
		if (result == 0) {
			result = messageId.compareTo(other.getMessageId());
		}
		
		return result;
	}
}
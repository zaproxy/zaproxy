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
package org.zaproxy.zap.extension.websocket.ui.httppanel.views;

import java.util.concurrent.atomic.AtomicInteger;

import org.zaproxy.zap.extension.httppanel.view.FuzzableMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.fuzz.WebSocketFuzzMessageDTO;

/**
 * Is called for example from the Request/Response tab, when fuzzing is chosen.
 * It takes a {@link WebSocketMessageDTO} and is able to fuzz it with given
 * strings. Finally a {@link WebSocketFuzzMessageDTO} is returned.
 */
public class WebSocketFuzzableTextMessage implements FuzzableMessage {

	public enum Location {HEADER, BODY};
	
	private final WebSocketMessageDTO message;
	private final int start;
	private final int end;
	
	private static AtomicInteger fuzzIdGenerator;
	private int fuzzId;
	
	static {
		fuzzIdGenerator = new AtomicInteger(0);
	}
	
	public WebSocketFuzzableTextMessage(WebSocketMessageDTO message, int start, int end) {
		this.message = message;
		
		this.start = start;
		this.end = end;
		
		this.fuzzId = fuzzIdGenerator.incrementAndGet();
	}
	
	@Override
	public WebSocketMessageDTO getMessage() {
		return message;
	}

	@Override
	public WebSocketFuzzMessageDTO fuzz(String fuzzString) throws IllegalArgumentException {
		WebSocketFuzzMessageDTO fuzzedMessage = copyMessage(message);
	    
		if (!(fuzzedMessage.payload instanceof String)) {
			// TODO: Exclude popup menu or support fuzzing binary payloads - why not?
			throw new IllegalArgumentException("You cannot fuzz binary messages!");
		}
		String orig = (String) fuzzedMessage.payload;
		
		final int length = orig.length();
		StringBuilder sb = new StringBuilder(start + fuzzString.length() + length - end);
		
		sb.append(orig.substring(0, start));
		sb.append(fuzzString);
		sb.append(orig.substring(end));
		
		fuzzedMessage.payload = sb.toString();
		fuzzedMessage.payloadLength = Integer.valueOf(length);
		fuzzedMessage.fuzzId = fuzzId;
		
		return fuzzedMessage;
	}
	
	/**
	 * Helper to duplicate the message.
	 * 
	 * @param msg
	 * @return
	 */
	private WebSocketFuzzMessageDTO copyMessage(WebSocketMessageDTO msg) {
		WebSocketFuzzMessageDTO fuzzMessage = new WebSocketFuzzMessageDTO();
		
		msg.copyInto(fuzzMessage);
        
        return fuzzMessage;
	}
}

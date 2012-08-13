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
package org.zaproxy.zap.extension.websocket.fuzz;

import org.zaproxy.zap.extension.fuzz.FuzzResult;

public class WebSocketFuzzResult extends FuzzResult {

    private String fuzz;
	private boolean isAbort = false;

    public WebSocketFuzzResult() {
        super();
    }
    
    public void setFuzz(String fuzz) {
        this.fuzz = fuzz;
    }
    
    public String getFuzz() {
        return fuzz;
    }

	public void setAbort(boolean isAbort) {
		this.isAbort  = isAbort;
	}
	
	public boolean isAbort() {
		return isAbort;
	}
    
    @Override
    public WebSocketFuzzMessageDTO getMessage() {
    	WebSocketFuzzMessageDTO fuzzMessage = (WebSocketFuzzMessageDTO) super.getMessage();
    	if (fuzzMessage != null) {
    		fuzzMessage.state = convertState(getState());
    		fuzzMessage.fuzz = getFuzz();
    	}
        return fuzzMessage;
    }
    
    /**
     * Decouple Data Access Object from this FuzzResult.
     * 
     * @param fuzzState
     * @return
     */
	private WebSocketFuzzMessageDTO.State convertState(State fuzzState) {
		WebSocketFuzzMessageDTO.State state;
		switch (fuzzState) {
		case ERROR:
			state = WebSocketFuzzMessageDTO.State.ERROR;
			break;

		case SUCCESSFUL:
		default:
			state = WebSocketFuzzMessageDTO.State.SUCCESSFUL;
			break;
		}
		return state;
	}
}

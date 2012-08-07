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

import java.util.Map;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.fuzz.AbstractFuzzProcess;
import org.zaproxy.zap.extension.fuzz.FuzzResult;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.extension.websocket.ui.httppanel.views.WebSocketFuzzableTextMessage;

/**
 * On process is created per fuzz string.
 */
public class WebSocketFuzzProcess extends AbstractFuzzProcess {

    private static final Logger logger = Logger.getLogger(WebSocketFuzzProcess.class);

    private WebSocketFuzzableTextMessage fuzzableMessage;
    private Map<Integer, WebSocketProxy> wsProxies;

    public WebSocketFuzzProcess(WebSocketFuzzableTextMessage fuzzableMessage, Map<Integer, WebSocketProxy> wsProxies) {
        this.fuzzableMessage = fuzzableMessage;
        this.wsProxies = wsProxies;
    }

    @Override
    public FuzzResult fuzz(String fuzz) {
        WebSocketFuzzResult fuzzResult = new WebSocketFuzzResult();
        fuzzResult.setFuzz(fuzz);
        
        WebSocketFuzzMessageDTO msg;
        
        try {
            // inject the payload
            msg = fuzzableMessage.fuzz(fuzz);
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
            
            fuzzResult.setMessage(fuzzableMessage.getMessage());

            fuzzResult.setState(FuzzResult.State.ERROR);
            
            return fuzzResult;
        }
        
        fuzzResult.setMessage(msg);
        
        try {
        	// send the payload
        	if (!wsProxies.containsKey(msg.channel.id)) {
        		msg.id = null;
        		fuzzResult.setAbort(true);
        		throw new WebSocketChannelClosedException("Channel #" + msg.channel.id + " does not exist!");
        	}
        	
        	WebSocketProxy wsProxy = wsProxies.get(msg.channel.id);
        	
        	// send message and notify all observers
        	// retrieve message from result to get state & fuzz set
    		wsProxy.sendAndNotify(fuzzResult.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fuzzResult.setState(FuzzResult.State.ERROR);
        }
        
        return fuzzResult;
    }
}

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
package org.zaproxy.zap.extension.websocket.ui;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.common.AbstractParam;

public class OptionsWebSocketParam extends AbstractParam {

    private static final String WEBSOCKET = "websocket";
    private static final String WEBSOCKET_DOMAIN = "domain";
    private static final String WEBSOCKET_PORT = "port";

	private List<CommunicationChannel> blacklistedChannels = new ArrayList<CommunicationChannel>();
	
	public OptionsWebSocketParam() {
	}
	
	@Override
	protected void parse() {
        blacklistedChannels.clear();

        String host = "";
        for (int i=0; host != null; i++) {

            host = getConfig().getString(getBlacklistedChannel(i, WEBSOCKET_DOMAIN));
            if (host == null) {
                   break;
            }
            
            if (host.equals("")) {
                break;
            }
            
            CommunicationChannel auth = new CommunicationChannel(
                    getConfig().getString(getBlacklistedChannel(i, WEBSOCKET_DOMAIN)),
                    getConfig().getInt(getBlacklistedChannel(i, WEBSOCKET_PORT)));
            blacklistedChannels.add(auth);
        }
	}


    public List<CommunicationChannel> getBlacklistedChannels() {
        return blacklistedChannels;
    }

    public void setBlacklistedChannels(List<CommunicationChannel> channels) {
        this.blacklistedChannels = channels;
        CommunicationChannel app = null;
        
        for (int i=0; i<((channels.size() > 100)? channels.size(): 100); i++) {
            // clearProperty doesn't work.  So set all host name to blank as a workaround.
            getConfig().clearProperty(getBlacklistedChannel(i, WEBSOCKET_DOMAIN));          
            getConfig().clearProperty(getBlacklistedChannel(i, WEBSOCKET_PORT));
            getConfig().clearProperty(WEBSOCKET + ".A"+i);
        }
        
        for (int i=0; i<channels.size(); i++) {
            app = channels.get(i);            
            getConfig().setProperty(getBlacklistedChannel(i, WEBSOCKET_DOMAIN), app.getDomain());
            getConfig().setProperty(getBlacklistedChannel(i, WEBSOCKET_PORT), app.getPort());
        }
    }

    private String getBlacklistedChannel(int i, String name) {
        return WEBSOCKET + ".A" + i + "." + name;
    }
}

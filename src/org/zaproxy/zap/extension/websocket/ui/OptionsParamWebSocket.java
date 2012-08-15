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

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.common.AbstractParam;

public class OptionsParamWebSocket extends AbstractParam {
//    private static Logger logger = Logger.getLogger(OptionsParamWebSocket.class);

	public static final String FORWARD_ALL = "websocket.forwardAll";
	public static final String BREAK_ON_PING_PONG = "websocket.breakOnPingPong";
	public static final String BREAK_ON_ALL = "websocket.breakOnAll";

	private boolean isForwardAll;
	private boolean isBreakOnPingPong;
	private boolean isBreakOnAll;

    /* (non-Javadoc)
     * @see org.parosproxy.paros.common.FileXML#parse()
     */
    @Override
    protected void parse() {
    	FileConfiguration cfg = getConfig();
    	isForwardAll = cfg.getBoolean(FORWARD_ALL, false);
    	isBreakOnPingPong = cfg.getBoolean(BREAK_ON_PING_PONG, false);
    	isBreakOnAll = cfg.getBoolean(BREAK_ON_ALL, false);
    }

    /**
	 * If true, then all WebSocket communication is forwarded, but not stored in
	 * database, nor shown in user interface.
	 * 
	 * @return True if all traffic should only be forwarded.
	 */
	public boolean isForwardAll() {
		return isForwardAll;
	}

	/**
	 * @see OptionsParamWebSocket#isForwardAll()
	 * 
	 * @param isForwardAll
	 */
	public void setForwardAll(boolean isForwardAll) {
		this.isForwardAll = isForwardAll;
		getConfig().setProperty(FORWARD_ALL, isForwardAll);
	}

	/**
	 * If false, then no PING/PONG messages are caught when:
	 * <ul>
	 * <li>enabled <i>break on all requests/responses</i> buttons are enabled</li>
	 * <li>stepping through to next request/response</li>
	 * </ul>
	 * 
	 * @return True if it should break also on ping & pong messages.
	 */
	public boolean isBreakOnPingPong() {
		return isBreakOnPingPong;
	}

	/**
	 * @see OptionsParamWebSocket#isBreakOnPingPong()
	 * 
	 * @param isCatchPingPong
	 */
	public void setBreakOnPingPong(boolean isCatchPingPong) {
		this.isBreakOnPingPong = isCatchPingPong;
		getConfig().setProperty(BREAK_ON_PING_PONG, isCatchPingPong);
	}

	/**
	 * If true, then WebSocket messages are caught when <i>break on all
	 * requests/responses</i> is active. Otherwise WebSocket communication is
	 * skipped.
	 * 
	 * @return True if it should break on all HTTP requests/responses.
	 */
	public boolean isBreakOnAll() {
		return isBreakOnAll;
	}

	/**
	 * @see OptionsParamWebSocket#isBreakOnAll()
	 * 
	 * @param isBreakOnAll
	 */
	public void setBreakOnAll(boolean isBreakOnAll) {
		this.isBreakOnAll = isBreakOnAll;
		getConfig().setProperty(BREAK_ON_ALL, isBreakOnAll);
	}
}
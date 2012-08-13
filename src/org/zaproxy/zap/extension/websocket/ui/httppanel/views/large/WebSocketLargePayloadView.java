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
package org.zaproxy.zap.extension.websocket.ui.httppanel.views.large;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.websocket.ui.httppanel.models.StringWebSocketPanelViewModel;
import org.zaproxy.zap.extension.websocket.ui.httppanel.views.WebSocketPanelTextView;

public class WebSocketLargePayloadView extends WebSocketPanelTextView {

	public static final String CONFIG_NAME = "largePayload";
	
	public static final String CAPTION_NAME = Constant.messages.getString("websocket.panel.view.largepayload.name");
	
	public WebSocketLargePayloadView(StringWebSocketPanelViewModel model) {
		super(model);
	}
	
	@Override
	public String getName() {
		return CAPTION_NAME;
	}
	
	@Override
	public String getConfigName() {
		return CONFIG_NAME;
	}

	@Override
	public int getPosition() {
		return 1;
	}

	@Override
	public boolean isEnabled(Message message) {
		return WebSocketLargePayloadUtil.isLargePayload(message);
	}

	@Override
	public boolean hasChanged() {
		return false;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public void setEditable(boolean editable) {
	}
}

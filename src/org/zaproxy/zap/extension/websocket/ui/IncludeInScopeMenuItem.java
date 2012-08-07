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

import java.util.regex.Pattern;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;

public class IncludeInScopeMenuItem extends WebSocketMessagesPopupMenuItem {

	private static final long serialVersionUID = -2345060529128495874L;

	public IncludeInScopeMenuItem() {
	}

	@Override
	protected String getMenuText() {
		return Constant.messages.getString("session.scope.include.title");
	}
	
	@Override
	protected boolean isEnabledExtended() {
		WebSocketMessageDTO message = getSelectedMessageDTO();
		if (message != null) {
			if (message.isInScope()) {
				// already in scope
				return false;
			}
		}
		return super.isEnabledExtended();
	}

	@Override
	protected void performAction() throws Exception {
		WebSocketMessageDTO message = getSelectedMessageDTO();
		if (message != null) {
			
			String url = message.channel.url;
        	url = Pattern.quote(url.substring(0, url.indexOf("?")));
//			List<String> excluded = session.getExcludeFromScopeRegexs();
//			excluded.remove(url);
//			session.setExcludeFromScopeRegexs(excluded);
        	Session session = Model.getSingleton().getSession();
	        session.addIncludeInScopeRegex(url);
		}
	}

	@Override
	protected String getInvokerName() {
		return "websocket.table";
	}

	@Override
	public boolean isSafe() {
		return true;
	}
}

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

import java.sql.SQLException;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;

public class ExcludeFromScopeMenuItem extends WebSocketMessagesPopupMenuItem {

	private static final long serialVersionUID = -2345060529128495874L;
	
	private static final Logger logger = Logger.getLogger(ExcludeFromScopeMenuItem.class);

	public ExcludeFromScopeMenuItem() {
	}

	@Override
	protected String getMenuText() {
		return Constant.messages.getString("session.scope.exclude.title");
	}
	
	@Override
	protected boolean isEnabledExtended() {
		WebSocketMessageDTO message = getSelectedMessageDTO();
		if (message != null) {
			Session session = Model.getSingleton().getSession();
			if (session.isInScope(message.channel.url)) {
				return true;
			}
			return false;
		}
		return super.isEnabledExtended();
	}

	@Override
	protected void performAction() {
		WebSocketMessageDTO message = getSelectedMessageDTO();
		if (message != null) {
			String url = message.channel.url;
        	url = Pattern.quote(url.substring(0, url.indexOf("?")));
        	// TODO support contexts
        	/*
	        try {
				Session session = Model.getSingleton().getSession();
				session.addExcludeFromScopeRegex(url);
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
			*/
		}
	}

	@Override
	protected String getInvokerName() {
		return WebSocketMessagesView.TABLE_NAME;
	}

	@Override
	public boolean isSafe() {
		return true;
	}
}

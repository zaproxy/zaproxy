/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.auth;

import java.util.List;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.PopupMenuSiteNode;

public class PopupFlagLoginMenu extends PopupMenuSiteNode {

	private static final long serialVersionUID = 2282358266003940700L;
	private ExtensionAuth extension = null;

	/**
	 * This method initializes 
	 * 
	 */
	public PopupFlagLoginMenu(ExtensionAuth ext) {
		super(Constant.messages.getString("auth.popup.login.req"), true);
		extension = ext;
	}
	    
   @Override
   public boolean isSubMenu() {
	   return true;
   }
   
   @Override
   public String getParentMenuName() {
		return Constant.messages.getString("flag.site.popup");
   }

   @Override
   public int getParentMenuIndex() {
	   return FLAG_MENU_INDEX;
   }
   
	@Override
	public void performAction(SiteNode sn) throws Exception {
		this.extension.setLoginRequest(sn);
		// Show the relevant session dialog
        View.getSingleton().showSessionDialog(Model.getSingleton().getSession(), SessionAuthenticationPanel.PANEL_NAME);
	}

	@Override
    public void performActions (List<HistoryReference> hrefs) throws Exception {
		super.performActions(hrefs);
	}

	@Override
	public boolean isEnableForInvoker(Invoker invoker) {
		return true;
	}
	
	@Override
    public boolean isEnabledForSiteNode (SiteNode sn) {
    	return true;
    }

    @Override
    public boolean isSafe() {
    	return true;
    }
}

/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.stdmenus;

import java.util.List;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.StructuralSiteNode;
import org.zaproxy.zap.view.SessionExcludeFromProxyPanel;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContainer;

public class PopupExcludeFromProxyMenu extends PopupMenuItemSiteNodeContainer {

	private static final long serialVersionUID = 2282358266003940700L;

	/**
	 * This method initializes 
	 * 
	 */
	public PopupExcludeFromProxyMenu() {
		super(Constant.messages.getString("sites.exclude.proxy.popup"), true);
	}
	    
   @Override
   public boolean isSubMenu() {
   	return true;
   }
   
   @Override
   public String getParentMenuName() {
   	return Constant.messages.getString("sites.exclude.popup");
   }

   @Override
   public int getParentMenuIndex() {
   	return EXCLUDE_MENU_INDEX;
   }
   
	@Override
	public void performAction(SiteNode sn) {
        try {
			Session session = Model.getSingleton().getSession();
			session.getExcludeFromProxyRegexs().add(new StructuralSiteNode(sn).getRegexPattern());
			SiteMap map = (SiteMap) View.getSingleton().getSiteTreePanel().getTreeSite().getModel();

            ExtensionHistory extHistory =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.class);

            if (extHistory != null) {
                extHistory.purge(map, sn);
            }

			
		} catch (DatabaseException e) {
			// Ignore
		}
	}

	@Override
    public void performHistoryReferenceActions (List<HistoryReference> hrefs) {
		super.performHistoryReferenceActions(hrefs);
        View.getSingleton().showSessionDialog(Model.getSingleton().getSession(), SessionExcludeFromProxyPanel.PANEL_NAME);
	}

	@Override
	public boolean isSafe() {
	   	return true;
	}
}

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
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.SessionExcludeFromSpiderPanel;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContainer;

public class PopupExcludeFromSpiderMenu extends PopupMenuItemSiteNodeContainer {

	private static final long serialVersionUID = 2282358266003940700L;

    private static final Logger logger = Logger.getLogger(PopupExcludeFromSpiderMenu.class);

	/**
	 * This method initializes 
	 * 
	 */
	public PopupExcludeFromSpiderMenu() {
		super(Constant.messages.getString("sites.exclude.spider.popup"), true);
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
        Session session = Model.getSingleton().getSession();
        String url;
        try {
            url = new URI(sn.getHierarchicNodeName(), false).toString();
        } catch (URIException e) {
            logger.error("Failed to create the URI to exclude from spider: " + e.getMessage(), e);
            return;
        }
        if (sn.isLeaf()) {
            url = Pattern.quote(url);
        } else {
        	url = Pattern.quote(url+"/") + ".*";
        }
        session.getExcludeFromSpiderRegexs().add(url);
	}

	@Override
    public void performHistoryReferenceActions (List<HistoryReference> hrefs) {
		super.performHistoryReferenceActions(hrefs);
        View.getSingleton().showSessionDialog(Model.getSingleton().getSession(), SessionExcludeFromSpiderPanel.PANEL_NAME);
	}

	@Override
    public boolean isSafe() {
    	return true;
    }
}

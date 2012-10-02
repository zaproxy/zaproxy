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
package org.zaproxy.zap.extension.stdmenus;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.PopupMenuSiteNode;

public class PopupContextIncludeMenu extends PopupMenuSiteNode {

	private static final long serialVersionUID = 2282358266003940700L;

    private List<PopupIncludeInContextMenu> subMenus = new ArrayList<PopupIncludeInContextMenu>();

	/**
	 * This method initializes 
	 * 
	 */
	public PopupContextIncludeMenu() {
		super("IncludeInContextX", true);
	}
	/**/
    @Override
    public String getParentMenuName() {
    	return Constant.messages.getString("context.include.popup");
    }
    
    @Override
    public boolean isSubMenu() {
    	return true;
    }
    @Override
    public boolean isDummyItem () {
    	return true;
    }
	    
	@Override
	public void performAction(SiteNode sn) throws Exception {
		// Do nothing
	}

	@Override
    public void performActions (List<HistoryReference> hrefs) throws Exception {
	}

	@Override
	public boolean isEnableForInvoker(Invoker invoker) {
		return true;
	}

	@Override
    public boolean isEnabledForSiteNode (SiteNode sn) {
		for (PopupIncludeInContextMenu menu : subMenus) {
			View.getSingleton().getPopupMenu().removeMenu(menu);
			
		}
		subMenus.clear();
		
        Session session = Model.getSingleton().getSession();
        List<Context> contexts = session.getContexts();
        for (Context context : contexts) {
        	PopupIncludeInContextMenu piicm = new PopupIncludeInContextMenu(context);
        	piicm.setMenuIndex(this.getMenuIndex());
			View.getSingleton().getPopupMenu().addMenu(piicm);
			this.subMenus.add(piicm);
        }
        // Add the 'new context' menu
    	PopupIncludeInContextMenu piicm = new PopupIncludeInContextMenu();
		View.getSingleton().getPopupMenu().addMenu(piicm);
		this.subMenus.add(piicm);
		
    	return false;
    }

    @Override
    public boolean isSafe() {
    	return true;
    }
}

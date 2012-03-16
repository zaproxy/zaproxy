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
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.PopupMenuSiteNode;
import org.zaproxy.zap.view.SessionExcludeFromProxyPanel;

public class PopupExcludeFromProxyMenu extends PopupMenuSiteNode {

	private static final long serialVersionUID = 2282358266003940700L;

	/**
	 * This method initializes 
	 * 
	 */
	public PopupExcludeFromProxyMenu() {
		super(Constant.messages.getString("sites.exclude.proxy.popup"), true);
	}
	    
    private static void purge(SiteMap map, SiteNode node) {
       SiteNode child = null;
       synchronized(map) {
           while (node.getChildCount() > 0) {
               try {
                   child = (SiteNode) node.getChildAt(0);
                   purge(map, child);
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
           
           if (node.isRoot()) {
               return;
           }

           // delete reference in node
           ExtensionHistory ext = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension("ExtensionHistory");
           if (ext != null) {
        	   // TODO shouldnt be allowed direct access to this!
        	   ext.getHistoryList().removeElement(node.getHistoryReference());
           }

           if (node.getHistoryReference()!= null) {
               node.getHistoryReference().delete();
           }

           // delete past reference in node
           while (node.getPastHistoryReference().size() > 0) {
               HistoryReference ref = node.getPastHistoryReference().get(0);
               if (ext != null) {
            	   ext.getHistoryList().removeElement(ref);
               }
               ref.delete();
               node.getPastHistoryReference().remove(0);
           }
           
           map.removeNodeFromParent(node);
       }
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
	public void performAction(SiteNode sn) throws Exception {
        Session session = Model.getSingleton().getSession();
        String url = sn.getHierarchicNodeName();
        if (! sn.isLeaf()) {
        	url += "/*";
        }
        session.getExcludeFromProxyRegexs().add(url);
        SiteMap map = (SiteMap) View.getSingleton().getSiteTreePanel().getTreeSite().getModel();

        purge(map, sn);
	}

	@Override
    public void performActions (List<HistoryReference> hrefs) throws Exception {
		super.performActions(hrefs);
        View.getSingleton().showSessionDialog(Model.getSingleton().getSession(), SessionExcludeFromProxyPanel.PANEL_NAME);
	}

	@Override
	public boolean isEnableForInvoker(Invoker invoker) {
		return true;
	}
}

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
package org.zaproxy.zap.extension.ascan;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.SessionExcludeFromProxyPanel;

/**
 * @deprecated Replaced by
 *             {@link org.zaproxy.zap.extension.stdmenus.PopupExcludeFromProxyMenu}
 */
@Deprecated
public class PopupExcludeFromProxyMenu extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 2282358266003940700L;

	private Component invoker = null;
    
	/**
	 * This method initializes 
	 * 
	 */
	public PopupExcludeFromProxyMenu() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("sites.exclude.proxy.popup"));
        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {    
        	    if (invoker.getName().equals("treeSite")) {
                    Session session = Model.getSingleton().getSession();
        	        JTree tree = (JTree) invoker;
                    TreePath[] paths = tree.getSelectionPaths();
                    SiteMap map = (SiteMap) tree.getModel();
                    for (int i=0; i<paths.length;i++) {
                        SiteNode node = (SiteNode) paths[i].getLastPathComponent(); 
                        String url = node.getHierarchicNodeName();
                        if (! node.isLeaf()) {
                        	url += "/*";
                        }
                        session.getExcludeFromProxyRegexs().add(url);
                        purge(map, node);
                    }

                    View.getSingleton().showSessionDialog(session, SessionExcludeFromProxyPanel.PANEL_NAME);
                }
        	    
        	}
        });

			
	}
	
	
    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals("treeSite")) {
            this.invoker = invoker;
	        JTree tree = (JTree) invoker;

		    SiteNode node = (SiteNode) tree.getLastSelectedPathComponent();
		    if (node != null) {
		        this.setEnabled(true);
		    } else {
		        this.setEnabled(false);
		    }
            return true;
        } else {
            this.invoker = null;
            return false;
        }

    }
    
    @SuppressWarnings("unused")
   private static void ignore(SiteMap map, SiteNode node) {
        SiteNode child = null;
        synchronized(map) {
            while (node.getChildCount() > 0) {
                try {
                    child = (SiteNode) node.getChildAt(0);
                    ignore(map, child);
                } catch (Exception e) {}
            }
            
            if (node.isRoot()) {
                return;
            }

            // delete reference in node
            
            ExtensionHistory ext = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
            ext.removeFromHistoryList(node.getHistoryReference());

            // delete past reference in node
            while (node.getPastHistoryReference().size() > 0) {
                HistoryReference ref = node.getPastHistoryReference().get(0);
                ext.removeFromHistoryList(ref);
                node.getPastHistoryReference().remove(0);
            }
            
            map.removeNodeFromParent(node);
        }
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
           ExtensionHistory ext = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
           ext.removeFromHistoryList(node.getHistoryReference());

           if (node.getHistoryReference()!= null) {
               node.getHistoryReference().delete();
           }
           

           // delete past reference in node
           while (node.getPastHistoryReference().size() > 0) {
               HistoryReference ref = node.getPastHistoryReference().get(0);
               ext.removeFromHistoryList(ref);
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
}

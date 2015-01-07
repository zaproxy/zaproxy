/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP Development team
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

import java.awt.Component;

import javax.swing.JTree;

import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.SiteMapPanel;
import org.zaproxy.zap.model.Target;

public class PopupContextTreeMenu extends ExtensionPopupMenuItem {
	private static final long serialVersionUID = 1L;
	
	private int contextId = -1;
    
    /**
	 * This method initializes 
	 * 
	 */
	public PopupContextTreeMenu() {
		super();
	}
	
	@Override
    public boolean isEnableForComponent(Component invoker) {
		contextId = -1;
		if (invoker instanceof JTree && SiteMapPanel.CONTEXT_TREE_COMPONENT_NAME.equals(invoker.getName())) {
			JTree contextTree = (JTree)invoker;
			SiteNode node = (SiteNode)contextTree.getLastSelectedPathComponent();
			if (node == null || node.isRoot()) {
				return false;
			}
			contextId = ((Target)node.getUserObject()).getContext().getIndex();
			return isEnabledForContext(contextId);
		}
		return false;
    }
	
	/**
	 * Override this method if the menu is not relevant all of the time
	 * @param context
	 * @return
	 */
	public boolean isEnabledForContext(int contextId) {
		return true;
	}
	
	protected int getContextId() {
		return contextId;
	}
	
    @Override
    public boolean isSafe() {
        return true;
    }
}

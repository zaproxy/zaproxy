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
package org.zaproxy.zap.extension.portscan;

import javax.swing.ImageIcon;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.view.PopupMenuSiteNode;

public class PopupMenuPortScan extends PopupMenuSiteNode {

	private static final long serialVersionUID = 1L;
	private ExtensionPortScan extension = null;
    
    /**
     * @param label
     */
    public PopupMenuPortScan(String label) {
        super(label);
        this.setIcon(new ImageIcon(PopupMenuPortScan.class.getResource("/resource/icon/16/187.png")));
    }

    @Override
    public boolean isSubMenu() {
    	return true;
    }
    
    @Override
    public String getParentMenuName() {
    	return Constant.messages.getString("attack.site.popup");
    }

    @Override
    public int getParentMenuIndex() {
    	return ATTACK_MENU_INDEX;
    }
    
    void setExtension(ExtensionPortScan extension) {
        this.extension = extension;
    }
    
	@Override
	public void performAction(SiteNode node) throws Exception {
	    if (node != null) {
	    	// Loop up to get the top parent
			while (node.getParent() != null && node.getParent().getParent() != null) {
				node = (SiteNode) node.getParent();
			}
	    	extension.portScanSite(node);
	    }
	}

	@Override
    public boolean isEnabledForSiteNode (SiteNode node) {
	    if (node != null && ! node.isRoot() && ! extension.isScanning(node)) {
	        this.setEnabled(true);
	    } else {
	        this.setEnabled(false);
	    }
        return true;
    }

	@Override
	public boolean isEnableForInvoker(Invoker invoker) {
		switch (invoker) {
		case alerts:
		case ascan:
		case bruteforce:
		case fuzz:
			return false;
		case history:
		case sites:
		case search:
		default:
			return true;
		}
	}

}

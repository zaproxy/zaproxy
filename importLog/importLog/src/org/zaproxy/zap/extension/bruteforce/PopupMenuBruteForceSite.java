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
package org.zaproxy.zap.extension.bruteforce;

import javax.swing.ImageIcon;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.view.PopupMenuSiteNode;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupMenuBruteForceSite extends PopupMenuSiteNode {

	private static final long serialVersionUID = 1L;
	private ExtensionBruteForce extension = null;
    
    /**
     * @param label
     */
    public PopupMenuBruteForceSite(String label) {
        super(label);
        this.setIcon(new ImageIcon(PopupMenuBruteForceSite.class.getResource(ExtensionBruteForce.HAMMER_ICON_RESOURCE)));
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
	
	@Override
	public void performAction(SiteNode node) throws Exception {
	    if (node != null) {
	    	// Loop up to get the top parent
			while (node.getParent() != null && node.getParent().getParent() != null) {
				node = (SiteNode) node.getParent();
			}
	    	extension.bruteForceSite(node);
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
    
    void setExtension(ExtensionBruteForce extension) {
        this.extension = extension;
    }
    
    protected ExtensionBruteForce getExtensionBruteForce() {
        return this.extension;
    }
    	
}

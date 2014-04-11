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
package org.zaproxy.zap.extension.stdmenus;

import javax.swing.ImageIcon;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.extension.spider.ExtensionSpider;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContainer;


public class PopupMenuSpiderScope extends PopupMenuItemSiteNodeContainer {

	private static final long serialVersionUID = 1L;
    private ExtensionSpider extension = null;

    /**
     * @param label
     */
    public PopupMenuSpiderScope(String label) {
        super(label);
        this.setIcon(new ImageIcon(PopupMenuSpiderScope.class.getResource("/resource/icon/16/spider.png")));
    }
    
    private ExtensionSpider getExtensionSpider() {
    	if (extension == null) {
    		extension = (ExtensionSpider) Control.getSingleton().getExtensionLoader().getExtension(ExtensionSpider.NAME);
    	}
    	return extension;
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
	public void performAction(SiteNode node) {
		extension.startScanAllInScope();
	}

	@Override
	public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
		if (getExtensionSpider() == null) {
			return false;
		}
		switch (invoker) {
		case ALERTS_PANEL:
		case ACTIVE_SCANNER_PANEL:
		case FORCED_BROWSE_PANEL:
		case FUZZER_PANEL:
			return false;
		case HISTORY_PANEL:
		case SITES_PANEL:
		case SEARCH_PANEL:
		default:
			return true;
		}
	}
	
}

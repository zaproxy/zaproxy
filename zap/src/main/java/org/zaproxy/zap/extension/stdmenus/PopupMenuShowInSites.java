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

import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContainer;

public class PopupMenuShowInSites extends PopupMenuItemSiteNodeContainer {

	private static final long serialVersionUID = 1L;

    /**
     * @param label
     */
    public PopupMenuShowInSites(String label) {
        super(label);
    }

	@Override
	public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
		switch (invoker) {
		case FORCED_BROWSE_PANEL:
		case FUZZER_PANEL:
		case SITES_PANEL:
			return false;
		case ACTIVE_SCANNER_PANEL:
		case ALERTS_PANEL:
		case HISTORY_PANEL:
		case SEARCH_PANEL:
		default:
			return true;
		}
	}

	@Override
	public void performAction(SiteNode sn) {
		View.getSingleton().getSiteTreePanel().showInSites(sn);
	}
	
    @Override
    public boolean isSafe() {
    	return true;
    }
}

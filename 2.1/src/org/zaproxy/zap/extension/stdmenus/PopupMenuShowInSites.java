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
import org.zaproxy.zap.view.PopupMenuSiteNode;

public class PopupMenuShowInSites extends PopupMenuSiteNode {

	private static final long serialVersionUID = 1L;

    /**
     * @param label
     */
    public PopupMenuShowInSites(String label) {
        super(label);
    }

	@Override
	public boolean isEnableForInvoker(Invoker invoker) {
		switch (invoker) {
		case bruteforce:
		case fuzz:
		case sites:
			return false;
		case ascan:
		case alerts:
		case history:
		case search:
		default:
			return true;
		}
	}

	@Override
	public void performAction(SiteNode sn) throws Exception {
		View.getSingleton().getSiteTreePanel().showInSites(sn);
	}
	
    @Override
    public boolean isSafe() {
    	return true;
    }
}

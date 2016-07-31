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
package org.zaproxy.zap.extension.siterefresh;

import java.net.MalformedURLException;
import java.net.URL;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

public class ExtensionSitesRefresh extends ExtensionAdaptor {

	private PopupMenuSitesRefresh popupMenuSitesRefresh = null;

    public ExtensionSitesRefresh() {
        super("ExtensionSitesRefresh");
        this.setOrder(1000);	// Want this to be as low as possible :)
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuSitesRefresh());
	    }
	}
	
	private PopupMenuSitesRefresh getPopupMenuSitesRefresh() {
		if (popupMenuSitesRefresh == null) {
			popupMenuSitesRefresh = new PopupMenuSitesRefresh();
		}
		return popupMenuSitesRefresh;
		
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("siterefresh.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * No database tables used, so all supported
	 */
	@Override
	public boolean supportsDb(String type) {
		return true;
	}
}
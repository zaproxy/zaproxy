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

import java.net.MalformedURLException;
import java.net.URL;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

public class ExtensionStdMenus extends ExtensionAdaptor {

	public static final String NAME = "ExtensionStandardMenus"; 

	private PopupMenuActiveScanNode popupMenuActiveScanNode = null;
	private PopupMenuActiveScanSite popupMenuActiveScanSite = null;
	private PopupIncludeInScopeMenu popupIncludeInScopeMenu = null;
	private PopupExcludeFromScopeMenu popupExcludeFromScopeMenu = null;
	private PopupExcludeFromProxyMenu popupExcludeFromProxyMenu = null;
	private PopupExcludeFromScanMenu popupExcludeFromScanMenu = null;
	private PopupExcludeFromSpiderMenu popupExcludeFromSpiderMenu = null;
	private PopupMenuResendMessage popupMenuResendMessage = null;
	private PopupMenuShowInHistory popupMenuShowInHistory = null;
	private PopupMenuShowInSites popupMenuShowInSites = null;
	private PopupMenuOpenUrlInBrowser popupMenuOpenUrlInBrowser = null;
	// Still being developed
	//private PopupMenuShowResponseInBrowser popupMenuShowResponseInBrowser = null;
    private PopupMenuAlert popupMenuAlert = null;

	public ExtensionStdMenus() {
		super();
		initialize();
	}

	private void initialize() {
        this.setName(NAME);
        this.setOrder(37);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    if (getView() != null) {
	    	// Be careful when changing the menu indexes (and order above) - its easy to get unexpected results!
            extensionHook.getHookMenu().addPopupMenuItem(getPopupIncludeInScopeMenu(0));
	    	extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuActiveScanSite(1));
	    	extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuActiveScanNode(1));
            extensionHook.getHookMenu().addPopupMenuItem(getPopupExcludeFromScopeMenu(2));
            extensionHook.getHookMenu().addPopupMenuItem(getPopupExcludeFromProxyMenu(2));
            extensionHook.getHookMenu().addPopupMenuItem(getPopupExcludeFromScanMenu(2));
            extensionHook.getHookMenu().addPopupMenuItem(getPopupExcludeFromSpiderMenu(2));
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuResendMessage(3));
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlert(4));
   			extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuShowInHistory(5));		// Both are index 5
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuShowInSites(5));		// on purpose ;)
	    	extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuOpenUrlInBrowser(6));
	    	//extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuShowResponseInBrowser(7));

	    }
	}

	private PopupMenuActiveScanSite getPopupMenuActiveScanSite(int menuIndex) {
		if (popupMenuActiveScanSite == null) {
			popupMenuActiveScanSite = new PopupMenuActiveScanSite(Constant.messages.getString("ascan.site.popup"));
		}
		return popupMenuActiveScanSite;
	}

	private PopupMenuActiveScanNode getPopupMenuActiveScanNode(int menuIndex) {
		if (popupMenuActiveScanNode == null) {
			popupMenuActiveScanNode = new PopupMenuActiveScanNode(Constant.messages.getString("ascan.node.popup"));
		}
		return popupMenuActiveScanNode;
	}

	private PopupMenuOpenUrlInBrowser getPopupMenuOpenUrlInBrowser(int menuIndex) {
		if (popupMenuOpenUrlInBrowser == null) {
			popupMenuOpenUrlInBrowser = new PopupMenuOpenUrlInBrowser(Constant.messages.getString("history.browser.popup"));
			popupMenuOpenUrlInBrowser.setMenuIndex(menuIndex);
		}
		return popupMenuOpenUrlInBrowser;
	}

	/*
	private PopupMenuShowResponseInBrowser getPopupMenuShowResponseInBrowser(int menuIndex) {
		if (popupMenuShowResponseInBrowser == null) {
			// TODO!
			popupMenuShowResponseInBrowser = new PopupMenuShowResponseInBrowser(Constant.messages.getString("history.showresponse.popup"));
			popupMenuShowResponseInBrowser.setMenuIndex(menuIndex);
		}
		return popupMenuShowResponseInBrowser;
	}
	*/
	
	
	private PopupIncludeInScopeMenu getPopupIncludeInScopeMenu(int menuIndex) {
		if (popupIncludeInScopeMenu == null) {
			popupIncludeInScopeMenu = new PopupIncludeInScopeMenu();
			popupIncludeInScopeMenu.setMenuIndex(menuIndex);
		}
		return popupIncludeInScopeMenu;
	}

	private PopupExcludeFromScopeMenu getPopupExcludeFromScopeMenu(int menuIndex) {
		if (popupExcludeFromScopeMenu == null) {
			popupExcludeFromScopeMenu = new PopupExcludeFromScopeMenu();
			popupExcludeFromScopeMenu.setMenuIndex(menuIndex);
		}
		return popupExcludeFromScopeMenu;
	}

	private PopupExcludeFromProxyMenu getPopupExcludeFromProxyMenu(int menuIndex) {
		if (popupExcludeFromProxyMenu == null) {
			popupExcludeFromProxyMenu = new PopupExcludeFromProxyMenu();
			//popupExcludeFromProxyMenu.setMenuIndex(menuIndex);
		}
		return popupExcludeFromProxyMenu;
	}

	private PopupExcludeFromScanMenu getPopupExcludeFromScanMenu(int menuIndex) {
		if (popupExcludeFromScanMenu == null) {
			popupExcludeFromScanMenu = new PopupExcludeFromScanMenu();
			//popupExcludeFromScanMenu.setMenuIndex(menuIndex);
		}
		return popupExcludeFromScanMenu;
	}

	private PopupExcludeFromSpiderMenu getPopupExcludeFromSpiderMenu(int menuIndex) {
		if (popupExcludeFromSpiderMenu == null) {
			popupExcludeFromSpiderMenu = new PopupExcludeFromSpiderMenu();
			//popupExcludeFromSpiderMenu.setMenuIndex(menuIndex);
		}
		return popupExcludeFromSpiderMenu;
	}

	private PopupMenuResendMessage getPopupMenuResendMessage(int menuIndex) {
		if (popupMenuResendMessage == null) {
			popupMenuResendMessage = new PopupMenuResendMessage(Constant.messages.getString("history.resend.popup"));
			popupMenuResendMessage.setMenuIndex(menuIndex);
		}
		return popupMenuResendMessage;
	}

	private PopupMenuShowInSites getPopupMenuShowInSites(int menuIndex) {
		if (popupMenuShowInSites == null) {
			popupMenuShowInSites = new PopupMenuShowInSites(Constant.messages.getString("sites.showinsites.popup"));
			popupMenuShowInSites.setMenuIndex(menuIndex);
		}
		return popupMenuShowInSites;
	}

	private PopupMenuShowInHistory getPopupMenuShowInHistory(int menuIndex) {
		if (popupMenuShowInHistory == null) {
			popupMenuShowInHistory = new PopupMenuShowInHistory(Constant.messages.getString("history.showinhistory.popup"));
			popupMenuShowInHistory.setMenuIndex(menuIndex);
		}
		return popupMenuShowInHistory;
	}

    private PopupMenuAlert getPopupMenuAlert(int menuIndex) {
        if (popupMenuAlert == null) {
            popupMenuAlert = new PopupMenuAlert(Constant.messages.getString("history.alert.popup"));
            popupMenuAlert.setMenuIndex(menuIndex);
        }
        return popupMenuAlert;
    }

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("stdexts.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}
}

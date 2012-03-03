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

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

public class ExtensionStdMenus extends ExtensionAdaptor {

	public static final String NAME = "ExtensionStandardMenus"; 

	private PopupMenuActiveScanNode popupMenuActiveScanNode = null;
	private PopupMenuActiveScanSite popupMenuActiveScanSite = null;
	private PopupExcludeFromProxyMenu popupExcludeFromProxyMenu = null;
	private PopupExcludeFromScanMenu popupExcludeFromScanMenu = null;
	private PopupExcludeFromSpiderMenu popupExcludeFromSpiderMenu = null;
	private PopupMenuResendMessage popupMenuResendMessage = null;
	private PopupMenuShowInHistory popupMenuShowInHistory = null;
	private PopupMenuShowInSites popupMenuShowInSites = null;
	private PopupMenuOpenUrlInBrowser popupMenuOpenUrlInBrowser = null;
    private PopupMenuAlert popupMenuAlert = null;

	public ExtensionStdMenus() {
		super();
		initialize();
	}

	private void initialize() {
        this.setName(NAME);
        this.setOrder(37);
	}

	public void hook(ExtensionHook extensionHook) {
	    if (getView() != null) {
	    	// Be careful when changing the menu indexes (and order above) - its easy to get unexpected results!
	    	extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuActiveScanSite(0));
	    	extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuActiveScanNode(0));
            extensionHook.getHookMenu().addPopupMenuItem(getPopupExcludeFromProxyMenu(1));
            extensionHook.getHookMenu().addPopupMenuItem(getPopupExcludeFromScanMenu(1));
            extensionHook.getHookMenu().addPopupMenuItem(getPopupExcludeFromSpiderMenu(1));
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuResendMessage(2));
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlert(3));
   			extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuShowInHistory(4));		// Both are index 4 
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuShowInSites(4));		// on purpose ;)
	    	extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuOpenUrlInBrowser(5));

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
}

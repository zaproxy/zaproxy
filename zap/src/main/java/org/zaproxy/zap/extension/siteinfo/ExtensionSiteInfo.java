/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.siteinfo;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

/** Adds the right-click "Get Info" menu item to the Sites tree (issue #3738). */
public class ExtensionSiteInfo extends ExtensionAdaptor {

    private static final String NAME = "ExtensionSiteInfo";

    private PopupMenuSiteGetInfo popupMenuSiteGetInfo;

    public ExtensionSiteInfo() {
        super(NAME);
        this.setOrder(1001);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        if (getView() != null) {
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuSiteGetInfo());
        }
    }

    private PopupMenuSiteGetInfo getPopupMenuSiteGetInfo() {
        if (popupMenuSiteGetInfo == null) {
            popupMenuSiteGetInfo = new PopupMenuSiteGetInfo();
        }
        return popupMenuSiteGetInfo;
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("siteinfo.name");
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("siteinfo.desc");
    }

    /** No database tables used, so all supported. */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }
}

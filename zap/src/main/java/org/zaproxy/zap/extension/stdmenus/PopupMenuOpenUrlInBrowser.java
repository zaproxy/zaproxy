/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.stdmenus;

import java.util.List;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.DesktopUtils;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemHttpMessageContainer;

public class PopupMenuOpenUrlInBrowser extends PopupMenuItemHttpMessageContainer {

    private static final long serialVersionUID = 1L;
    private boolean disabledToolTipSet = false;

    /** @param label */
    public PopupMenuOpenUrlInBrowser(String label) {
        super(label);
    }

    @Override
    public void performAction(HttpMessage msg) {
        if (!DesktopUtils.openUrlInBrowser(msg.getRequestHeader().getURI())) {
            View.getSingleton()
                    .showWarningDialog(Constant.messages.getString("history.browser.warning"));
        }
    }

    @Override
    protected boolean isButtonEnabledForSelectedMessages(List<HttpMessage> httpMessages) {
        if (Constant.isInContainer()
                && !Model.getSingleton()
                        .getOptionsParam()
                        .getViewParam()
                        .isAllowAppIntegrationInContainers()) {
            if (!disabledToolTipSet) {
                this.setToolTipText(Constant.messages.getString("history.browser.disabled"));
                disabledToolTipSet = true;
            }
            return false;
        }
        if (disabledToolTipSet) {
            this.setToolTipText("");
            disabledToolTipSet = false;
        }
        return super.isButtonEnabledForSelectedMessages(httpMessages);
    }

    @Override
    public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
        return DesktopUtils.canOpenUrlInBrowser();
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}

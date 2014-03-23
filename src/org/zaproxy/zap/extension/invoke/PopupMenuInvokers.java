/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.invoke;

import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuHttpMessageContainer;

public class PopupMenuInvokers extends PopupMenuHttpMessageContainer {

    private static final long serialVersionUID = 1L;

    private PopupMenuInvokeConfigure confPopup = null;

    public PopupMenuInvokers() {
        super(Constant.messages.getString("invoke.site.popup"));

        confPopup = new PopupMenuInvokeConfigure();
        setButtonStateOverriddenByChildren(false);
    }

    @Override
    public int getMenuIndex() {
        return 3;
    }

    @Override
    protected boolean isButtonEnabledForNumberOfSelectedMessages(int numberOfSelectedMessages) {
        return true;
    }

    @Override
    protected boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
        switch (invoker) {
        case SITES_PANEL:
        case HISTORY_PANEL:
            return true;
        default:
            return false;
        }
    }

    public void setApps(List<InvokableApp> apps) {
        removeAll();
        for (InvokableApp app : apps) {
            PopupMenuInvoke pmi = new PopupMenuInvoke(app.getDisplayName());
            pmi.setCommand(app.getFullCommand());
            pmi.setWorkingDir(app.getWorkingDirectory());
            pmi.setParameters(app.getParameters());
            pmi.setCaptureOutput(app.isCaptureOutput());
            pmi.setOutputNote(app.isOutputNote());
            add(pmi);
        }
        if (!apps.isEmpty()) {
            addSeparator();
        }
        add(confPopup);
    }

}

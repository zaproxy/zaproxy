/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.extension.history;

import java.util.List;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemHistoryReferenceContainer;

@SuppressWarnings("serial")
public class PopupMenuPurgeHistory extends PopupMenuItemHistoryReferenceContainer {

    private static final long serialVersionUID = -155358408946131183L;

    private final ExtensionHistory extension;

    public PopupMenuPurgeHistory(ExtensionHistory extension) {
        super(Constant.messages.getString("history.purge.popup"), true);

        this.extension = extension;
        setAccelerator(extension.getView().getDefaultDeleteKeyStroke());
    }

    @Override
    public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
        return (invoker == Invoker.HISTORY_PANEL);
    }

    @Override
    public void performHistoryReferenceActions(List<HistoryReference> hrefs) {
        extension.purgeHistory(hrefs);
    }

    @Override
    public void performAction(HistoryReference href) {
        // Nothing to do, the action is performed on all the references.
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}

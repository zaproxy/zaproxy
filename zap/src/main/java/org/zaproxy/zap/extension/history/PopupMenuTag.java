/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.view.popup.PopupMenuItemHistoryReferenceContainer;

@SuppressWarnings("serial")
public class PopupMenuTag extends PopupMenuItemHistoryReferenceContainer {

    private static final long serialVersionUID = 1L;

    private final ExtensionHistory extension;

    public PopupMenuTag(ExtensionHistory extension) {
        super(Constant.messages.getString("history.tags.popup"));

        this.extension = extension;
    }

    @Override
    protected boolean isButtonEnabledForHistoryReference(HistoryReference historyReference) {
        return !HistoryReference.getTemporaryTypes().contains(historyReference.getHistoryType());
    }

    @Override
    public void performAction(HistoryReference href) {
        extension.showManageTagsDialog(href, href.getTags());
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}

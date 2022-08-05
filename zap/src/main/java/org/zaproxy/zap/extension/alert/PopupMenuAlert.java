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
package org.zaproxy.zap.extension.alert;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemHistoryReferenceContainer;

@SuppressWarnings("serial")
public class PopupMenuAlert extends PopupMenuItemHistoryReferenceContainer {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LogManager.getLogger(PopupMenuAlert.class);

    private final ExtensionAlert extension;

    /**
     * Constructs a {@code PopupMenuAlert} with the given label and extension.
     *
     * @param label the label of the menu item.
     * @param extension the {@code ExtensionAlert} to show the Add Alert dialogue.
     */
    public PopupMenuAlert(String label, ExtensionAlert extension) {
        super(label);

        this.extension = extension;
    }

    @Override
    public void performAction(HistoryReference href) {
        Invoker invoker = getInvoker();
        if (invoker == Invoker.ACTIVE_SCANNER_PANEL) {
            try {
                extension.showAlertAddDialog(href.getHttpMessage(), HistoryReference.TYPE_SCANNER);
            } catch (HttpMalformedHeaderException | DatabaseException e) {
                logger.error(e.getMessage(), e);
            }
        } else if (invoker == Invoker.FUZZER_PANEL) {
            try {
                extension.showAlertAddDialog(href.getHttpMessage(), HistoryReference.TYPE_FUZZER);
            } catch (HttpMalformedHeaderException | DatabaseException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            extension.showAlertAddDialog(href);
        }
    }

    @Override
    public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
        switch (invoker) {
            case ALERTS_PANEL:
                return false;
            case SITES_PANEL:
            case HISTORY_PANEL:
            case ACTIVE_SCANNER_PANEL:
            case SEARCH_PANEL:
            case FUZZER_PANEL:
            case FORCED_BROWSE_PANEL:
            default:
                return true;
        }
    }

    @Override
    public boolean isButtonEnabledForHistoryReference(HistoryReference href) {
        if (href != null) {
            switch (getInvoker()) {
                case ACTIVE_SCANNER_PANEL:
                case FUZZER_PANEL:
                    return true;
                default:
                    return (href.getHistoryType() != HistoryReference.TYPE_TEMPORARY);
            }
        }
        return false;
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}

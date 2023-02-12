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
package org.zaproxy.zap.extension.alert;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;

/**
 * A {@link PopupMenuItemAlert} that allows to edit an {@link Alert alert}.
 *
 * @since 1.4.0
 */
@SuppressWarnings("serial")
public class PopupMenuAlertEdit extends PopupMenuItemAlert {

    private static final long serialVersionUID = 1L;

    private final ExtensionAlert extension;

    public PopupMenuAlertEdit(ExtensionAlert extension) {
        super(Constant.messages.getString("scanner.edit.popup"));

        this.extension = extension;
    }

    @Override
    protected void performAction(Alert alert) {
        extension.showAlertEditDialog(alert);
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}

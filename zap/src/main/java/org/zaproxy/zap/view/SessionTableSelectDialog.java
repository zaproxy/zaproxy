/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.view;

import java.awt.Frame;
import java.util.List;
import org.zaproxy.zap.utils.DisplayUtils;

public class SessionTableSelectDialog extends StandardFieldsDialog {

    private static final long serialVersionUID = 1L;

    private static final String NAME_FIELD = "session.select.session";

    private String selectedSession = null;

    public SessionTableSelectDialog(Frame owner, List<String> sessions) {
        super(owner, "session.select.title", DisplayUtils.getScaledDimension(400, 200));
        // this.setModal(true);
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.addComboField(NAME_FIELD, sessions, null);
    }

    @Override
    public void save() {
        this.selectedSession = this.getStringValue(NAME_FIELD);
    }

    public String getSelectedSession() {
        return this.selectedSession;
    }

    @Override
    public String validateFields() {
        return null;
    }
}

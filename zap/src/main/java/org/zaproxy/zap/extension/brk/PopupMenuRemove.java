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
package org.zaproxy.zap.extension.brk;

import java.awt.Component;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;

@SuppressWarnings("serial")
public class PopupMenuRemove extends ExtensionPopupMenuItem {

    private static final long serialVersionUID = 1L;
    private ExtensionBreak extension = null;

    public PopupMenuRemove() {
        super(Constant.messages.getString("brk.remove.popup"));

        this.addActionListener(
                new java.awt.event.ActionListener() {

                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        extension.removeUiSelectedBreakpoint();
                    }
                });
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals(BreakpointsPanel.PANEL_NAME)) {
            this.setEnabled(true);
            return true;
        }
        return false;
    }

    void setExtension(ExtensionBreak extension) {
        this.extension = extension;
    }
}

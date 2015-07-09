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
package org.zaproxy.zap.extension.httpsessions;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;

/**
 * A {@code ExtensionPopupMenuItem} that allows to copy the value of a session token, show in "Http Sessions" tab, to the
 * clipboard.
 */
public class PopupMenuItemCopySessionToken extends ExtensionPopupMenuItem implements ClipboardOwner {

    private static final long serialVersionUID = -2462677795340933336L;

    private final HttpSessionsPanel httpSessionsPanel;

    public PopupMenuItemCopySessionToken(HttpSessionsPanel panel) {
        super(Constant.messages.getString("httpsessions.popup.session.copyToken"));

        this.httpSessionsPanel = panel;
        this.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                HttpSession item = httpSessionsPanel.getSelectedSession();
                if (item == null) {
                    return;
                }

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(item.getTokenValuesString()), PopupMenuItemCopySessionToken.this);
            }
        });
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (HttpSessionsPanel.PANEL_NAME.equals(invoker.getName())) {
            setEnabled(httpSessionsPanel.getSelectedSession() != null);
            return true;
        }
        return false;
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}

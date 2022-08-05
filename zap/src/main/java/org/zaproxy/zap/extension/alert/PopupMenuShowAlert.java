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

import javax.swing.JMenuItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;

@SuppressWarnings("serial")
public class PopupMenuShowAlert extends JMenuItem implements Comparable<PopupMenuShowAlert> {

    private static final long serialVersionUID = 1L;

    private final ExtensionAlert extension;
    private final Alert alert;

    private static final Logger log = LogManager.getLogger(ExtensionPopupMenuItem.class);

    public PopupMenuShowAlert(String name, ExtensionAlert extension, Alert alert) {
        super(name);
        this.alert = alert;
        this.extension = extension;

        this.addActionListener(
                new java.awt.event.ActionListener() {

                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        try {
                            PopupMenuShowAlert.this.extension.showAlertEditDialog(
                                    PopupMenuShowAlert.this.alert);
                        } catch (Exception e2) {
                            log.error(e2.getMessage(), e2);
                        }
                    }
                });
    }

    @Override
    public int compareTo(PopupMenuShowAlert o) {
        if (o == null) {
            return -1;
        }
        // Negate the alert comparison so higher risks shown first
        return -alert.compareTo(o.alert);
    }
}

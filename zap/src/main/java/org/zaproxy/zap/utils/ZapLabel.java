/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.utils;

import java.awt.Color;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

/**
 * An alternative to JLabel which disables HTML and supports wrapping. Use this class instead of
 * JLabel where the text could include HTML that you do not want to render.
 *
 * @author psiinon
 * @since 2.6.0
 */
public class ZapLabel extends JTextArea {

    private static final long serialVersionUID = 1L;

    public ZapLabel() {
        setDefaults();
    }

    public ZapLabel(String text) {
        super(text);
        setDefaults();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setDefaults();
    }

    private void setDefaults() {
        this.setEditable(false);
        this.setCursor(null);
        this.setBorder(null);
        this.setBackground(
                new Color(
                        UIManager.getLookAndFeel()
                                .getDefaults()
                                .getColor("Label.background")
                                .getRGB()));
        this.setForeground(
                new Color(
                        UIManager.getLookAndFeel()
                                .getDefaults()
                                .getColor("Label.foreground")
                                .getRGB()));

        ((DefaultCaret) getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    }
}

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
package org.zaproxy.zap.view;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;

public class AboutDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code AboutDialog} with no owner and not modal.
     *
     * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
     */
    public AboutDialog() {
        this(null, false);
    }

    /**
     * Constructs an {@code AboutDialog} with the given owner and whether or not it's modal.
     *
     * @param owner the {@code Frame} from which the dialog is displayed
     * @param modal {@code true} if the dialogue should be modal, {@code false} otherwise
     * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
     * @see org.zaproxy.zap.view.AboutPanel
     */
    public AboutDialog(Frame owner, boolean modal) {
        super(owner, modal);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbcPanel = new GridBagConstraints();
        GridBagConstraints gbcButtons = new GridBagConstraints();

        gbcPanel.gridx = 0;
        gbcPanel.gridy = 0;
        gbcPanel.insets = new Insets(0, 0, 0, 0);
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.anchor = GridBagConstraints.NORTHWEST;
        gbcPanel.weightx = 1.0D;
        gbcPanel.weighty = 1.0D;
        gbcPanel.ipady = 2;
        gbcPanel.gridwidth = 2;

        gbcButtons.gridx = 1;
        gbcButtons.gridy = 1;
        gbcButtons.insets = new Insets(2, 2, 2, 2);
        gbcButtons.anchor = GridBagConstraints.SOUTHEAST;

        mainPanel.add(new AboutPanel(), gbcPanel);

        JButton btnOk = new JButton();
        btnOk.setText(Constant.messages.getString("all.button.ok"));
        btnOk.addActionListener(e -> dispose());

        mainPanel.add(btnOk, gbcButtons);

        this.setContentPane(mainPanel);
        this.pack();
    }
}

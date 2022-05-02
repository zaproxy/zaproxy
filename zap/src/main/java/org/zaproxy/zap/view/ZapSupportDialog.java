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
package org.zaproxy.zap.view;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;

/**
 * An {@code AbstractDialog} class which facilitates GUI display of support information.
 *
 * @since 2.7.0
 */
public class ZapSupportDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(ZapSupportDialog.class);

    private JPanel mainPanel = null;
    private ZapSupportPanel supportPanel = null;
    private JButton btnOK = null;
    private JButton btnOpen = null;
    private JButton btnCopy = null;

    /**
     * Constructs an {@code ZapSupportDialog} with no owner and not modal.
     *
     * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
     */
    public ZapSupportDialog() {
        this(null, true);
    }

    /**
     * Constructs an {@code ZapSupportDialog} with the given owner and whether or not it's modal.
     *
     * @param owner the {@code Frame} from which the dialog is displayed
     * @param modal {@code true} if the dialogue should be modal, {@code false} otherwise
     * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
     */
    public ZapSupportDialog(Frame owner, boolean modal) {
        super(owner, modal);

        this.setContentPane(getMainPanel());
        this.pack();
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel(new GridBagLayout());

            GridBagConstraints gbcPanel = new GridBagConstraints();
            GridBagConstraints gbcOpenButton = new GridBagConstraints();
            GridBagConstraints gbcCopyButton = new GridBagConstraints();
            GridBagConstraints gbcOkButton = new GridBagConstraints();

            gbcPanel.gridx = 0;
            gbcPanel.gridy = 0;
            gbcPanel.insets = new Insets(0, 0, 0, 0);
            gbcPanel.fill = GridBagConstraints.BOTH;
            gbcPanel.anchor = GridBagConstraints.NORTHWEST;
            gbcPanel.weightx = 1.0D;
            gbcPanel.weighty = 1.0D;
            gbcPanel.ipady = 2;
            gbcPanel.gridwidth = 3;

            int gridx = 0;
            Insets insets = new Insets(2, 2, 2, 2);
            gbcOpenButton.gridx = gridx;
            gbcOpenButton.gridy = 1;
            gbcOpenButton.insets = insets;
            gbcOpenButton.anchor = GridBagConstraints.SOUTHEAST;

            gbcCopyButton.gridx = ++gridx;
            gbcCopyButton.gridy = 1;
            gbcCopyButton.insets = insets;
            gbcCopyButton.anchor = GridBagConstraints.SOUTHEAST;

            gbcOkButton.gridx = ++gridx;
            gbcOkButton.gridy = 1;
            gbcOkButton.insets = insets;
            gbcOkButton.anchor = GridBagConstraints.SOUTHEAST;

            mainPanel.add(getSupportPanel(), gbcPanel);
            mainPanel.add(getBtnOpen(), gbcOpenButton);
            mainPanel.add(getBtnCopy(), gbcCopyButton);
            this.getRootPane().setDefaultButton(getBtnCopy());
            mainPanel.add(getBtnOK(), gbcOkButton);
        }
        return mainPanel;
    }

    private ZapSupportPanel getSupportPanel() {
        if (supportPanel == null) {
            supportPanel = new ZapSupportPanel();
        }
        return supportPanel;
    }

    private JButton getBtnOK() {
        if (btnOK == null) {
            btnOK = new JButton();
            btnOK.setText(Constant.messages.getString("all.button.ok"));
            btnOK.addActionListener(e -> ZapSupportDialog.this.dispose());
        }
        return btnOK;
    }

    private JButton getBtnOpen() {
        if (btnOpen == null) {
            btnOpen = new JButton();
            if (Desktop.isDesktopSupported()) {
                btnOpen.setEnabled(Desktop.getDesktop().isSupported(Desktop.Action.OPEN));
            } else {
                btnOpen.setEnabled(false);
            }
            btnOpen.setText(Constant.messages.getString("support.open.button"));
            btnOpen.setToolTipText(Constant.messages.getString("support.open.button.tooltip"));
            btnOpen.addActionListener(
                    e -> {
                        try {
                            Desktop.getDesktop().open(new File(Constant.getZapHome()));
                        } catch (IOException e1) {
                            LOGGER.error(
                                    "An exception occurred while trying to have the OS open ZAP's Home Directory.",
                                    e1);
                        }
                    });
        }
        return btnOpen;
    }

    private JButton getBtnCopy() {
        if (btnCopy == null) {
            btnCopy = new JButton();
            btnCopy.setText(Constant.messages.getString("support.copy.button"));
            btnCopy.setToolTipText(Constant.messages.getString("support.copy.button.tooltip"));
            btnCopy.addActionListener(
                    e -> {
                        StringSelection stringSelection =
                                new StringSelection(supportPanel.getSupportInfo());
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(stringSelection, null);
                    });
        }
        return btnCopy;
    }
}

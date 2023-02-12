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
package org.zaproxy.zap.view;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.help.ExtensionHelp;

public abstract class AbstractFormDialog extends JDialog {

    private static final long serialVersionUID = -3345423228612477780L;

    private static final String CANCEL_BUTTON_LABEL =
            Constant.messages.getString("form.dialog.button.cancel");

    private JButton helpButton;
    private JButton confirmButton;
    private JButton cancelButton;

    private boolean firstTime;
    private String helpTarget;

    public AbstractFormDialog(Dialog owner, String title) {
        this(owner, title, true);
    }

    public AbstractFormDialog(Dialog owner, String title, boolean initView) {
        super(owner, title, ModalityType.DOCUMENT_MODAL);
        initialise(initView);
    }

    public AbstractFormDialog(Frame owner, String title) {
        this(owner, title, true);
    }

    public AbstractFormDialog(Frame owner, String title, boolean initView) {
        super(owner, title, ModalityType.DOCUMENT_MODAL);
        initialise(initView);
    }

    public AbstractFormDialog(Window owner, String title) {
        this(owner, title, true);
    }

    public AbstractFormDialog(Window owner, String title, boolean initView) {
        super(owner, title, ModalityType.DOCUMENT_MODAL);
        initialise(initView);
    }

    private void initialise(boolean initView) {
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(
                new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        clearAndHide();
                    }
                });

        firstTime = true;
        if (initView) {
            initView();
        }

        // Handle escape key to close the dialog
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        AbstractAction escapeAction =
                new AbstractAction() {

                    private static final long serialVersionUID = -8088438488574461587L;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        clearAndHide();
                    }
                };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    protected void initView() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonsPanel.add(getHelpButton());
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(getCancelButton());
        buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonsPanel.add(getConfirmButton());

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(getFieldsPanel(), BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.PAGE_END);

        this.setContentPane(panel);
    }

    private JButton getHelpButton() {
        if (helpButton == null) {
            helpButton = new JButton();
            helpButton.setIcon(ExtensionHelp.getHelpIcon());
            helpButton.setToolTipText(Constant.messages.getString("help.dialog.button.tooltip"));
            helpButton.setVisible(false);

            helpButton.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            if (helpTarget != null) {
                                ExtensionHelp.showHelp(helpTarget);
                            }
                        }
                    });
        }
        return helpButton;
    }

    protected void setHelpTarget(String helpTarget) {
        this.helpTarget = helpTarget;
        getHelpButton().setVisible(helpTarget != null);
    }

    private JButton getConfirmButton() {
        if (confirmButton == null) {
            confirmButton = new JButton(getConfirmButtonLabel());
            confirmButton.setEnabled(false);

            confirmButton.addActionListener(
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (validateFields()) {
                                performAction();
                                clearAndHide();
                            }
                        }
                    });
        }
        return confirmButton;
    }

    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton(getCancelButtonLabel());
            cancelButton.addActionListener(
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            clearAndHide();
                        }
                    });
        }
        return cancelButton;
    }

    protected abstract JPanel getFieldsPanel();

    @Override
    public void setVisible(boolean b) {
        init();

        if (firstTime) {
            centreOnOwner();
            firstTime = false;
        }

        super.setVisible(b);
    }

    private void clearAndHide() {
        clearFields();
        dispose();
    }

    protected abstract String getConfirmButtonLabel();

    protected String getCancelButtonLabel() {
        return CANCEL_BUTTON_LABEL;
    }

    protected void setConfirmButtonEnabled(boolean enabled) {
        getConfirmButton().setEnabled(enabled);
    }

    protected void init() {}

    protected boolean validateFields() {
        return true;
    }

    protected void performAction() {}

    protected void clearFields() {}

    private void centreOnOwner() {
        Dimension frameSize = this.getSize();
        Rectangle mainrect = getMainRectangle();
        int x = mainrect.x + (mainrect.width - frameSize.width) / 2;
        int y = mainrect.y + (mainrect.height - frameSize.height) / 2;
        this.setLocation(x, y);
    }

    private Rectangle getMainRectangle() {
        Window owner = getOwner();
        if (owner != null) {
            return owner.getBounds();
        }
        return new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    }
}

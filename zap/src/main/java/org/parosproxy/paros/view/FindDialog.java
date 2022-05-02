/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/04/16 i18n
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/05/03 Changed the method find to check if txtComp is null.
// ZAP: 2014/01/30 Issue 996: Ensure all dialogs close when the escape key is pressed (copy tidy up)
// ZAP: 2017/07/12 Issue 765: Add constructor with window parent, to facilitate ctrl-F in various
// HttpPanels
// ZAP: 2017/07/17: Prevent opening multiple dialogs per parent.
// ZAP: 2017/10/18 Do not allow to obtain the FindDialog with a null parent.
// ZAP: 2018/08/17 Show a message if the string was not found (Issue 4935).
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/04/08 Remove boilerplate javadoc.
// ZAP: 2022/04/07 Put any selected text into the Find dialog text area.
package org.parosproxy.paros.view;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.ZapTextField;

public class FindDialog extends AbstractDialog {

    private static final long serialVersionUID = -3223449799557586758L;

    private static final Logger LOGGER = LogManager.getLogger(FindDialog.class);

    @SuppressWarnings("unchecked")
    private static Map<Object, FindDialog> parentsMap =
            new ReferenceMap(ReferenceMap.WEAK, ReferenceMap.HARD);

    private JPanel jPanel = null;
    private JButton btnFind = null;
    private JButton btnCancel = null;
    private ZapTextField txtFind = null;
    private JPanel jPanel1 = null;
    private JTextComponent lastInvoker = null;

    /** A label to inform if the string was not found. */
    private JLabel infoLabel;

    /** @param lastInvoker The lastInvoker to set. */
    public void setLastInvoker(JTextComponent lastInvoker) {
        this.lastInvoker = lastInvoker;
        if (lastInvoker != null) {
            String selectedText = lastInvoker.getSelectedText();
            if (selectedText != null) {
                this.getTxtFind().setText(selectedText);
            }
        }
    }

    /**
     * @throws HeadlessException
     * @deprecated 2.7.0, use #FindDialog(Window, boolean) instead
     * @see #getDialog(Window, boolean)
     */
    @Deprecated
    public FindDialog() throws HeadlessException {
        super();
        initialize();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     * @deprecated 2.7.0, use #FindDialog(Window, boolean) instead
     * @see #getDialog(Window, boolean)
     */
    @Deprecated
    public FindDialog(Frame arg0, boolean arg1) throws HeadlessException {
        super(arg0, arg1);
        initialize();
    }

    /**
     * Constructs a FindDialog.
     *
     * @param parent the parent window of the FindDialog.
     * @param modal whether or not this FindDialog should be modal
     * @throws HeadlessException
     * @see #getDialog(Window, boolean)
     * @since 2.7.0
     */
    public FindDialog(Window parent, boolean modal) throws HeadlessException {
        super(parent, modal);
        initialize();
    }

    private void initialize() {
        this.setTitle(Constant.messages.getString("edit.find.title"));
        this.infoLabel = new JLabel(Constant.messages.getString("edit.find.label.notfound"));
        this.infoLabel.setVisible(false);
        this.setContentPane(getJPanel());
        centreDialog();
        txtFind.requestFocus();
        this.getRootPane().setDefaultButton(btnFind);
        pack();
        this.setVisible(true);
    }

    private static Map<Object, FindDialog> getParentsMap() {
        return parentsMap;
    }

    /**
     * Get the FindDialog for the parent if there is one or creates and returns a new one.
     *
     * @param parent the parent Window (or Frame) for this FindDialog
     * @param modal a boolean indicating whether the FindDialog should ({@code true}), or shouldn't
     *     ({@code false}) be modal.
     * @return The existing FindDialog for the parent (if there is one), or a new FindDialog.
     * @throws IllegalArgumentException if the {@code parent} is {@code null}.
     * @since 2.7.0
     */
    public static FindDialog getDialog(Window parent, boolean modal) {
        if (parent == null) {
            throw new IllegalArgumentException("The parent must not be null.");
        }

        FindDialog activeDialog = getParentsMap().get(parent);
        if (activeDialog != null) {
            activeDialog.getTxtFind().requestFocus();
            return activeDialog;
        }
        FindDialog newDialog = new FindDialog(parent, modal);
        getParentsMap().put(parent, newDialog);
        newDialog.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        getParentsMap().remove(parent);
                    }
                });
        return newDialog;
    }

    private void discard() {
        this.setVisible(false);
        this.dispose();
    }

    private void find() {
        JTextComponent txtComp = lastInvoker;
        if (txtComp == null) {
            JFrame parent = (JFrame) (this.getParent());
            Component c = parent.getMostRecentFocusOwner();
            if (c instanceof JTextComponent) {
                txtComp = (JTextComponent) c;
            }
        }

        infoLabel.setVisible(false);

        // ZAP: Check if a JTextComponent was really found.
        if (txtComp == null) {
            return;
        }

        try {
            String findText = txtFind.getText().toLowerCase();
            String txt = txtComp.getText().toLowerCase();
            int startPos = txt.indexOf(findText, txtComp.getCaretPosition());

            // Enable Wrap Search
            if (startPos <= 0) {
                txtComp.setCaretPosition(0);
                startPos = txt.indexOf(findText, txtComp.getCaretPosition());
            }

            int length = findText.length();
            if (startPos > -1) {
                txtComp.setSelectionColor(DisplayUtils.getHighlightColor());
                txtComp.select(startPos, startPos + length);
                txtComp.requestFocusInWindow();
                txtFind.requestFocusInWindow();
            } else {
                infoLabel.setVisible(true);
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred while finding:", e);
        }
    }

    private JPanel getJPanel() {
        if (jPanel == null) {
            javax.swing.JLabel jLabel =
                    new JLabel(Constant.messages.getString("edit.find.label.what"));

            jPanel = new JPanel();
            GroupLayout layout = new GroupLayout(jPanel);
            jPanel.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);
            layout.setHonorsVisibility(infoLabel, Boolean.FALSE);

            layout.setHorizontalGroup(
                    layout.createSequentialGroup()
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(getTxtFind())
                                            .addComponent(infoLabel)
                                            .addComponent(getJPanel1())));

            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel)
                                            .addComponent(getTxtFind()))
                            .addComponent(infoLabel)
                            .addComponent(getJPanel1()));
        }
        return jPanel;
    }

    private JButton getBtnFind() {
        if (btnFind == null) {
            btnFind = new JButton();
            btnFind.setText(Constant.messages.getString("edit.find.button.find"));
            btnFind.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            find();
                        }
                    });
        }
        return btnFind;
    }

    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setText(Constant.messages.getString("edit.find.button.cancel"));
            btnCancel.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            FindDialog.this.discard();
                            FindDialog.this.dispatchEvent(
                                    new WindowEvent(FindDialog.this, WindowEvent.WINDOW_CLOSING));
                        }
                    });
        }
        return btnCancel;
    }

    private ZapTextField getTxtFind() {
        if (txtFind == null) {
            txtFind = new ZapTextField(15);
        }
        return txtFind;
    }

    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            jPanel1 = new JPanel(new FlowLayout(FlowLayout.TRAILING));
            jPanel1.add(getBtnFind());
            jPanel1.add(getBtnCancel());
        }
        return jPanel1;
    }
} //  @jve:decl-index=0:visual-constraint="10,10"

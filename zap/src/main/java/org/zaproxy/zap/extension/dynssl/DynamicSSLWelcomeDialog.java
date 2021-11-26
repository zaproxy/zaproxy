/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 mawoki@ymail.com
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
package org.zaproxy.zap.extension.dynssl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.utils.FontUtils;

@Deprecated
public class DynamicSSLWelcomeDialog extends AbstractDialog {

    private static final long serialVersionUID = -7686931099484922846L;

    private final JButton btnCreate =
            new JButton(Constant.messages.getString("dynssl.button.generate"));
    private final JButton btnLater =
            new JButton(Constant.messages.getString("dynssl.button.later"));

    /**
     * @param owner
     * @param modal
     * @throws HeadlessException
     */
    public DynamicSSLWelcomeDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);
        initLayout();
        initActionListener();
    }

    private void initLayout() {
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
            this.setSize(480, 300);
        }
        this.setPreferredSize(new Dimension(480, 300));
        JTextArea txtSslWontWork = new JTextArea();
        txtSslWontWork.setEditable(false);
        txtSslWontWork.setBackground(
                SystemColor.control); // XXX: this doesn't work like expected, it should be the same
        // color as the control's background :-/
        txtSslWontWork.setTabSize(4);
        txtSslWontWork.setWrapStyleWord(true);
        txtSslWontWork.setLineWrap(true);
        txtSslWontWork.setForeground(Color.BLACK);
        txtSslWontWork.setText(Constant.messages.getString("dynssl.text.sslwontwork"));
        getContentPane().setLayout(new BorderLayout());
        final JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        final JPanel contentIconPane = new JPanel();
        contentPanel.add(contentIconPane, BorderLayout.WEST);
        final JLabel lblCertificateIcon = new JLabel("");
        lblCertificateIcon.setIcon(
                new ImageIcon(
                        DynamicSSLWelcomeDialog.class.getResource(
                                "/resource/certificate48x54.png")));

        contentIconPane.add(lblCertificateIcon);
        final JPanel contentTextPane = new JPanel();
        contentTextPane.setBorder(new EmptyBorder(0, 10, 5, 5));

        contentPanel.add(contentTextPane, BorderLayout.CENTER);
        contentTextPane.setLayout(new BorderLayout(5, 5));

        JPanel panelTitle = new JPanel();
        panelTitle.setBorder(
                new MatteBorder(0, 0, 1, 0, UIManager.getColor("InternalFrame.borderShadow")));
        contentTextPane.add(panelTitle, BorderLayout.NORTH);
        {
            JLabel lblTitle =
                    new JLabel("SSL " + Constant.messages.getString("dynssl.label.rootca"));
            panelTitle.add(lblTitle);
            lblTitle.setFont(FontUtils.getFont(Font.BOLD));
        }

        contentTextPane.add(txtSslWontWork);
        final JPanel panelButtons = new JPanel();
        contentTextPane.add(panelButtons, BorderLayout.SOUTH);
        GridBagLayout gbl_panelButtons = new GridBagLayout();
        gbl_panelButtons.columnWidths = new int[] {25, 0, 0};
        gbl_panelButtons.rowHeights = new int[] {23, 23, 0};
        gbl_panelButtons.columnWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        gbl_panelButtons.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        panelButtons.setLayout(gbl_panelButtons);

        GridBagConstraints gbc_btnCreate = new GridBagConstraints();
        gbc_btnCreate.anchor = GridBagConstraints.WEST;
        gbc_btnCreate.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnCreate.insets = new Insets(0, 0, 5, 5);
        gbc_btnCreate.gridx = 0;
        gbc_btnCreate.gridy = 0;
        panelButtons.add(btnCreate, gbc_btnCreate);

        GridBagConstraints gbc_lblCreateCertificateNow = new GridBagConstraints();
        gbc_lblCreateCertificateNow.anchor = GridBagConstraints.WEST;
        gbc_lblCreateCertificateNow.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblCreateCertificateNow.insets = new Insets(0, 0, 5, 0);
        gbc_lblCreateCertificateNow.gridx = 1;
        gbc_lblCreateCertificateNow.gridy = 0;
        final JLabel lblCreateCertificateNow =
                new JLabel(Constant.messages.getString("dynssl.text.createnow"));
        panelButtons.add(lblCreateCertificateNow, gbc_lblCreateCertificateNow);

        GridBagConstraints gbc_btnLater = new GridBagConstraints();
        gbc_btnLater.anchor = GridBagConstraints.WEST;
        gbc_btnLater.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnLater.insets = new Insets(0, 0, 0, 5);
        gbc_btnLater.gridx = 0;
        gbc_btnLater.gridy = 1;
        panelButtons.add(btnLater, gbc_btnLater);

        GridBagConstraints gbc_lblNotNowBut = new GridBagConstraints();
        gbc_lblNotNowBut.anchor = GridBagConstraints.WEST;
        gbc_lblNotNowBut.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblNotNowBut.gridx = 1;
        gbc_lblNotNowBut.gridy = 1;
        final JLabel lblNotNowBut = new JLabel(Constant.messages.getString("dynssl.text.notnow"));
        panelButtons.add(lblNotNowBut, gbc_lblNotNowBut);
    }

    private void initActionListener() {
        btnCreate.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        DynamicSSLWelcomeDialog.this.setVisible(false);
                        SwingUtilities.invokeLater(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Control.getSingleton()
                                                .getMenuToolsControl()
                                                .options(
                                                        Constant.messages.getString(
                                                                "dynssl.options.name"));
                                    }
                                });
                    }
                });

        btnLater.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        DynamicSSLWelcomeDialog.this.setVisible(false);
                    }
                });
    }
}

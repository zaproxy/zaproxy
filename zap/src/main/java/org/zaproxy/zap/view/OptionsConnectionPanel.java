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

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapTextField;

/** @deprecated (2.12.0) No longer in use. */
@Deprecated
public class OptionsConnectionPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private JPanel panelProxyAuth = null;
    private JPanel panelProxyChain = null;
    private ZapTextField txtProxyChainRealm = null;
    private ZapTextField txtProxyChainUserName = null;
    private JPasswordField txtProxyChainPassword = null;
    private JCheckBox chkShowPassword = null;
    private ProxyDialog proxyDialog = null;
    private boolean prompting = false;

    public void setProxyDialog(ProxyDialog proxyDialog) {
        this.proxyDialog = proxyDialog;
    }

    public OptionsConnectionPanel(boolean prompting) {
        super();
        this.prompting = prompting;
        initialize();
    }

    public OptionsConnectionPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes panelProxyAuth
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPanelProxyAuth() {
        if (panelProxyAuth == null) {
            java.awt.GridBagConstraints gridBagConstraints82 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints72 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints62 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints52 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints42 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints31 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints21 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints16 = new GridBagConstraints();

            javax.swing.JLabel jLabel11 = new JLabel();

            javax.swing.JLabel jLabel10 = new JLabel();

            javax.swing.JLabel jLabel9 = new JLabel();

            panelProxyAuth = new JPanel();
            panelProxyAuth.setLayout(new GridBagLayout());
            jLabel9.setText(Constant.messages.getString("conn.options.proxy.auth.realm"));
            jLabel10.setText(Constant.messages.getString("conn.options.proxy.auth.username"));
            if (prompting) {
                jLabel11.setText(Constant.messages.getString("conn.options.proxy.auth.passprompt"));
            } else {
                jLabel11.setText(Constant.messages.getString("conn.options.proxy.auth.password"));
            }
            panelProxyAuth.setBorder(
                    javax.swing.BorderFactory.createTitledBorder(
                            null,
                            Constant.messages.getString("conn.options.proxy.auth.auth"),
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            FontUtils.getFont(FontUtils.Size.standard)));
            gridBagConstraints16.gridx = 0;
            gridBagConstraints16.gridy = 0;
            gridBagConstraints16.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints16.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints16.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints16.gridwidth = 2;
            gridBagConstraints16.weightx = 1.0D;
            gridBagConstraints21.gridx = 0;
            gridBagConstraints21.gridy = 1;
            gridBagConstraints21.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints21.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints21.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints21.weightx = 0.5D;
            gridBagConstraints31.gridx = 1;
            gridBagConstraints31.gridy = 1;
            gridBagConstraints31.weightx = 0.5D;
            gridBagConstraints31.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints31.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints31.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints31.ipadx = 50;
            gridBagConstraints42.gridx = 0;
            gridBagConstraints42.gridy = 2;
            gridBagConstraints42.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints42.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints42.weightx = 0.5D;
            gridBagConstraints42.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints52.gridx = 1;
            gridBagConstraints52.gridy = 2;
            gridBagConstraints52.weightx = 0.5D;
            gridBagConstraints52.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints52.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints52.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints52.ipadx = 50;
            gridBagConstraints62.gridx = 0;
            gridBagConstraints62.gridy = 3;
            gridBagConstraints62.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints62.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints62.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints62.weightx = 0.5D;
            gridBagConstraints72.gridx = 1;
            gridBagConstraints72.gridy = 3;
            gridBagConstraints72.weightx = 0.5D;
            gridBagConstraints72.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints72.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints72.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints72.ipadx = 50;
            gridBagConstraints82.gridx = 1;
            gridBagConstraints82.gridy = 4;
            gridBagConstraints82.weightx = 0.5D;
            gridBagConstraints82.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints82.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints82.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints82.ipadx = 50;
            panelProxyAuth.add(jLabel9, gridBagConstraints21);
            panelProxyAuth.add(getTxtProxyChainRealm(), gridBagConstraints31);
            panelProxyAuth.add(jLabel10, gridBagConstraints42);
            panelProxyAuth.add(getTxtProxyChainUserName(), gridBagConstraints52);
            panelProxyAuth.add(jLabel11, gridBagConstraints62);
            panelProxyAuth.add(getTxtProxyChainPassword(), gridBagConstraints72);
            panelProxyAuth.add(getChkShowPassword(), gridBagConstraints82);
        }
        return panelProxyAuth;
    }
    /**
     * This method initializes panelProxyChain
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPanelProxyChain() {
        if (panelProxyChain == null) {
            panelProxyChain = new JPanel();
            java.awt.GridBagConstraints gridBagConstraints92 = new GridBagConstraints();

            javax.swing.JLabel jLabel8 = new JLabel();

            java.awt.GridBagConstraints gridBagConstraints102 = new GridBagConstraints();

            panelProxyChain.setLayout(new GridBagLayout());
            gridBagConstraints92.gridx = 0;
            gridBagConstraints92.gridy = 0;
            gridBagConstraints92.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints92.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints92.fill = java.awt.GridBagConstraints.HORIZONTAL;
            panelProxyChain.setName("Proxy Chain");
            jLabel8.setText("");
            gridBagConstraints102.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints102.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints102.gridx = 0;
            gridBagConstraints102.gridy = 1;
            gridBagConstraints102.weightx = 1.0D;
            gridBagConstraints102.weighty = 1.0D;
            panelProxyChain.add(getPanelProxyAuth(), gridBagConstraints92);
            panelProxyChain.add(jLabel8, gridBagConstraints102);
        }
        return panelProxyChain;
    }
    /** This method initializes this */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("conn.options.title"));
        this.add(getPanelProxyChain(), getPanelProxyChain().getName());
    }

    @Override
    public void initParam(Object obj) {

        OptionsParam optionsParam = (OptionsParam) obj;
        org.parosproxy.paros.network.ConnectionParam connectionParam =
                optionsParam.getConnectionParam();

        // set Proxy Chain parameters
        txtProxyChainRealm.setText(connectionParam.getProxyChainRealm());
        txtProxyChainRealm.discardAllEdits();
        txtProxyChainUserName.setText(connectionParam.getProxyChainUserName());
        txtProxyChainUserName.discardAllEdits();
        chkShowPassword.setSelected(false); // Default don't show (everytime)
        txtProxyChainPassword.setEchoChar('*'); // Default mask (everytime)
        this.proxyDialog.pack();
    }

    @Override
    public void saveParam(Object obj) throws Exception {

        OptionsParam optionsParam = (OptionsParam) obj;
        org.parosproxy.paros.network.ConnectionParam connectionParam =
                optionsParam.getConnectionParam();

        connectionParam.setProxyChainRealm(txtProxyChainRealm.getText());
        connectionParam.setProxyChainUserName(txtProxyChainUserName.getText());
        // Make sure this isn't saved in the config file
        connectionParam.setProxyChainPassword(
                new String(txtProxyChainPassword.getPassword()), false);
    }

    private ZapTextField getTxtProxyChainRealm() {
        if (txtProxyChainRealm == null) {
            txtProxyChainRealm = new ZapTextField();
        }
        return txtProxyChainRealm;
    }

    private ZapTextField getTxtProxyChainUserName() {
        if (txtProxyChainUserName == null) {
            txtProxyChainUserName = new ZapTextField();
        }
        return txtProxyChainUserName;
    }

    private JPasswordField getTxtProxyChainPassword() {
        if (txtProxyChainPassword == null) {
            txtProxyChainPassword = new JPasswordField();
            txtProxyChainPassword.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            proxyDialog.saveAndClose();
                        }
                    });
        }
        return txtProxyChainPassword;
    }

    /**
     * This method initializes chkShowPassword
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getChkShowPassword() {
        if (chkShowPassword == null) {
            chkShowPassword = new JCheckBox();
            chkShowPassword.setText(
                    Constant.messages.getString("conn.options.proxy.auth.showpass"));
            chkShowPassword.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            if (chkShowPassword.isSelected()) {
                                txtProxyChainPassword.setEchoChar((char) 0);
                            } else {
                                txtProxyChainPassword.setEchoChar('*');
                            }
                        }
                    });
        }
        return chkShowPassword;
    }

    public void passwordFocus() {
        this.getTxtProxyChainPassword().requestFocus();
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.connection";
    }
}

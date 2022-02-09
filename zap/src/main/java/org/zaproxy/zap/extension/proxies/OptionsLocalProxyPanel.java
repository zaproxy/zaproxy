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
package org.zaproxy.zap.extension.proxies;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jdesktop.swingx.JXLabel;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.NetworkUtils;
import org.zaproxy.zap.utils.ZapPortNumberSpinner;

/** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
@Deprecated
public class OptionsLocalProxyPanel extends JPanel {

    private static final long serialVersionUID = -1350537974139536669L;

    private JComboBox<String> localAddress = null;
    private JCheckBox chkBehindNat;
    private JCheckBox chkRemoveUnsupportedEncodings = null;
    private JCheckBox chkAlwaysDecodeGzip = null;

    private ZapPortNumberSpinner spinnerProxyPort = null;

    public OptionsLocalProxyPanel() {
        super(new BorderLayout());
        initialize();
    }

    private JComboBox<String> getLocalAddress() {
        if (localAddress == null) {
            localAddress = new JComboBox<>();
            localAddress.setEditable(true);
            List<String> allAddrs = NetworkUtils.getAvailableAddresses(false);
            localAddress.addItem("localhost");
            localAddress.addItem("127.0.0.1");
            localAddress.addItem("::1");
            localAddress.addItem("0.0.0.0");
            for (String addr : allAddrs) {
                localAddress.addItem(addr);
            }
        }
        return localAddress;
    }

    private JCheckBox getChkBehindNat() {
        if (chkBehindNat == null) {
            chkBehindNat =
                    new JCheckBox(
                            Constant.messages.getString("options.proxy.local.label.behindnat"));
            chkBehindNat.setToolTipText(
                    Constant.messages.getString("options.proxy.local.tooltip.behindnat"));
        }
        return chkBehindNat;
    }

    public JCheckBox getChkRemoveUnsupportedEncodings() {
        if (chkRemoveUnsupportedEncodings == null) {
            chkRemoveUnsupportedEncodings =
                    new JCheckBox(
                            Constant.messages.getString(
                                    "options.proxy.local.label.removeUnsupportedEncodings"));
            chkRemoveUnsupportedEncodings.setToolTipText(
                    Constant.messages.getString(
                            "options.proxy.local.tooltip.removeUnsupportedEncodings"));
        }
        return chkRemoveUnsupportedEncodings;
    }

    private JCheckBox getChkAlwaysDecodeGzip() {
        if (chkAlwaysDecodeGzip == null) {
            chkAlwaysDecodeGzip =
                    new JCheckBox(
                            Constant.messages.getString(
                                    "options.proxy.local.label.alwaysDecodeGzip"));
            chkAlwaysDecodeGzip.setToolTipText(
                    Constant.messages.getString("options.proxy.local.tooltip.alwaysDecodeGzip"));
        }
        return chkAlwaysDecodeGzip;
    }

    /**
     * This method initializes spinnerProxyPort
     *
     * @return ZapPortNumberSpinner
     */
    private ZapPortNumberSpinner getSpinnerProxyPort() {
        if (spinnerProxyPort == null) {
            // Do not allow invalid port numbers
            spinnerProxyPort = new ZapPortNumberSpinner(8080);
        }
        return spinnerProxyPort;
    }

    private void initialize() {
        JXLabel jLabel6 = new JXLabel();
        GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        JLabel jLabel = new JLabel();
        JLabel jLabel1 = new JLabel();

        JPanel panelLocalProxy = new JPanel(new GridBagLayout());
        panelLocalProxy.setFont(FontUtils.getFont(FontUtils.Size.standard));
        panelLocalProxy.setBorder(
                javax.swing.BorderFactory.createTitledBorder(
                        null,
                        Constant.messages.getString("options.proxy.local.title"),
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        FontUtils.getFont(FontUtils.Size.standard)));

        jLabel.setText(Constant.messages.getString("options.proxy.local.label.address"));

        gridBagConstraints4.gridx = 0;
        gridBagConstraints4.gridy = 0;
        gridBagConstraints4.ipadx = 0;
        gridBagConstraints4.ipady = 0;
        gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints4.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints4.weightx = 0.5D;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;

        gridBagConstraints5.gridx = 1;
        gridBagConstraints5.gridy = 0;
        gridBagConstraints5.weightx = 0.5D;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints5.ipadx = 50;
        gridBagConstraints5.ipady = 0;
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints5.insets = new java.awt.Insets(2, 2, 2, 2);

        gridBagConstraints6.gridx = 0;
        gridBagConstraints6.gridy = 1;
        gridBagConstraints6.ipadx = 0;
        gridBagConstraints6.ipady = 0;
        gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints6.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints6.weightx = 0.5D;

        gridBagConstraints7.gridx = 1;
        gridBagConstraints7.gridy = 1;
        gridBagConstraints7.weightx = 0.5D;
        gridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints7.ipadx = 50;
        gridBagConstraints7.ipady = 0;
        gridBagConstraints7.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints7.insets = new java.awt.Insets(2, 2, 2, 2);

        jLabel1.setText(Constant.messages.getString("options.proxy.local.label.port"));
        jLabel6.setLineWrap(true);
        jLabel6.setText(Constant.messages.getString("options.proxy.local.label.browser"));

        gridBagConstraints15.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints15.gridx = 0;
        gridBagConstraints15.gridy = 4;
        gridBagConstraints15.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints15.weightx = 1.0D;
        gridBagConstraints15.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints15.gridwidth = 2;

        panelLocalProxy.add(jLabel, gridBagConstraints4);
        panelLocalProxy.add(getLocalAddress(), gridBagConstraints5);
        panelLocalProxy.add(jLabel1, gridBagConstraints6);
        panelLocalProxy.add(getSpinnerProxyPort(), gridBagConstraints7);
        panelLocalProxy.add(jLabel6, gridBagConstraints15);

        java.awt.GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new java.awt.Insets(2, 2, 2, 2);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0D;
        gbc.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gbc.anchor = java.awt.GridBagConstraints.PAGE_START;
        panelLocalProxy.add(getChkBehindNat(), gbc);
        panelLocalProxy.add(getChkRemoveUnsupportedEncodings(), gbc);
        panelLocalProxy.add(this.getChkAlwaysDecodeGzip(), gbc);

        this.add(panelLocalProxy, BorderLayout.CENTER);
    }

    public void setProxy(ProxiesParamProxy proxy) {
        getLocalAddress().setSelectedItem(proxy.getAddress());
        this.getSpinnerProxyPort().setValue(proxy.getPort());
        this.getChkBehindNat().setSelected(proxy.isBehindNat());
        this.getChkAlwaysDecodeGzip().setSelected(proxy.isAlwaysDecodeGzip());
        this.getChkRemoveUnsupportedEncodings().setSelected(proxy.isRemoveUnsupportedEncodings());
    }

    public ProxiesParamProxy getProxy() {
        ProxiesParamProxy proxy =
                new ProxiesParamProxy(
                        (String) getLocalAddress().getSelectedItem(),
                        getSpinnerProxyPort().getValue(),
                        true);
        proxy.setBehindNat(getChkBehindNat().isSelected());
        proxy.setAlwaysDecodeGzip(getChkAlwaysDecodeGzip().isSelected());
        proxy.setRemoveUnsupportedEncodings(getChkRemoveUnsupportedEncodings().isSelected());
        return proxy;
    }
}

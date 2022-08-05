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
package org.zaproxy.zap.extension.callback;

import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.NetworkUtils;
import org.zaproxy.zap.utils.ZapPortNumberSpinner;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

/** @deprecated (2.11.0) Superseded by the OAST add-on. */
@Deprecated
@SuppressWarnings("serial")
public class OptionsCallbackPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private JPanel panel = null;

    private ExtensionCallback ext;
    private JComboBox<String> localAddress = null;
    private JComboBox<String> remoteAddress = null;
    private ZapTextField testURL = null;
    private JCheckBox randomPort = null;
    private ZapPortNumberSpinner spinnerPort = null;
    private JCheckBox secure;

    public OptionsCallbackPanel(ExtensionCallback ext) {
        super();
        this.ext = ext;
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("callback.options.title"));
        this.add(getCallbackPanel(), getCallbackPanel().getName());
    }

    private JPanel getCallbackPanel() {
        if (panel == null) {

            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            int currentRowIndex = -1;

            JLabel localAddrLabel =
                    new JLabel(Constant.messages.getString("callback.options.label.localaddress"));
            localAddrLabel.setLabelFor(getLocalAddress());
            panel.add(
                    localAddrLabel,
                    LayoutHelper.getGBC(0, ++currentRowIndex, 1, 0.5D, new Insets(2, 2, 2, 2)));
            panel.add(
                    getLocalAddress(),
                    LayoutHelper.getGBC(1, currentRowIndex, 1, 0.5D, new Insets(2, 2, 2, 2)));

            JLabel remoteAddrLabel =
                    new JLabel(Constant.messages.getString("callback.options.label.remoteaddress"));
            remoteAddrLabel.setLabelFor(getRemoteAddress());
            panel.add(
                    remoteAddrLabel,
                    LayoutHelper.getGBC(0, ++currentRowIndex, 1, 0.5D, new Insets(2, 2, 2, 2)));
            panel.add(
                    getRemoteAddress(),
                    LayoutHelper.getGBC(1, currentRowIndex, 1, 0.5D, new Insets(2, 2, 2, 2)));

            JLabel secureLabel =
                    new JLabel(Constant.messages.getString("callback.options.label.secure"));
            secureLabel.setLabelFor(getSecure());
            panel.add(
                    secureLabel,
                    LayoutHelper.getGBC(0, ++currentRowIndex, 1, 0.5D, new Insets(2, 2, 2, 2)));
            panel.add(
                    getSecure(),
                    LayoutHelper.getGBC(1, currentRowIndex, 1, 0.5D, new Insets(2, 2, 2, 2)));

            JLabel rndPortLabel =
                    new JLabel(Constant.messages.getString("callback.options.label.rndport"));
            rndPortLabel.setLabelFor(getSpinnerPort());
            panel.add(
                    rndPortLabel,
                    LayoutHelper.getGBC(0, ++currentRowIndex, 1, 0.5D, new Insets(2, 2, 2, 2)));
            panel.add(
                    this.getRandomPort(),
                    LayoutHelper.getGBC(1, currentRowIndex, 1, 0.5D, new Insets(2, 2, 2, 2)));

            JLabel portLabel =
                    new JLabel(Constant.messages.getString("callback.options.label.port"));
            portLabel.setLabelFor(getSpinnerPort());
            panel.add(
                    portLabel,
                    LayoutHelper.getGBC(0, ++currentRowIndex, 1, 0.5D, new Insets(2, 2, 2, 2)));
            panel.add(
                    getSpinnerPort(),
                    LayoutHelper.getGBC(1, currentRowIndex, 1, 0.5D, new Insets(2, 2, 2, 2)));

            JLabel testUrlLabel =
                    new JLabel(Constant.messages.getString("callback.options.label.testurl"));
            testUrlLabel.setLabelFor(getTestURL());
            panel.add(
                    testUrlLabel,
                    LayoutHelper.getGBC(0, ++currentRowIndex, 1, 0.5D, new Insets(2, 2, 2, 2)));
            panel.add(
                    getTestURL(),
                    LayoutHelper.getGBC(1, currentRowIndex, 1, 0.5D, new Insets(2, 2, 2, 2)));

            panel.add(new JLabel(), LayoutHelper.getGBC(0, 20, 2, 0.5D, 1.0D));
        }

        return panel;
    }

    private JComboBox<String> getLocalAddress() {
        if (localAddress == null) {
            localAddress = new JComboBox<>();
        }
        return localAddress;
    }

    private JComboBox<String> getRemoteAddress() {
        if (remoteAddress == null) {
            remoteAddress = new JComboBox<>();
            remoteAddress.setEditable(true);
        }
        return remoteAddress;
    }

    private ZapTextField getTestURL() {
        if (testURL == null) {
            testURL = new ZapTextField();
            testURL.setEditable(false);
            testURL.setFocusable(true);
        }
        return testURL;
    }

    private JCheckBox getRandomPort() {
        if (randomPort == null) {
            randomPort = new JCheckBox();
            randomPort.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            getSpinnerPort().setEnabled(!randomPort.isSelected());
                        }
                    });
        }
        return randomPort;
    }

    private JCheckBox getSecure() {
        if (secure == null) {
            secure = new JCheckBox();
        }
        return secure;
    }

    private ZapPortNumberSpinner getSpinnerPort() {
        if (spinnerPort == null) {
            spinnerPort = new ZapPortNumberSpinner(0);
        }
        return spinnerPort;
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        CallbackParam proxyParam = optionsParam.getParamSet(CallbackParam.class);

        List<String> allAddrs = NetworkUtils.getAvailableAddresses(false);
        localAddress.removeAllItems();
        localAddress.addItem("0.0.0.0");
        for (String addr : allAddrs) {
            localAddress.addItem(addr);
        }
        localAddress.setSelectedItem(proxyParam.getLocalAddress());

        remoteAddress.removeAllItems();
        for (String addr : allAddrs) {
            remoteAddress.addItem(addr);
        }
        remoteAddress.setSelectedItem(proxyParam.getRemoteAddress());

        secure.setSelected(proxyParam.isSecure());

        if (proxyParam.getPort() == 0) {
            getRandomPort().setSelected(true);
            getSpinnerPort().setEnabled(false);
            getSpinnerPort().setValue(ext.getPort()); // As 0 isn't a valid port
        } else {
            getSpinnerPort().setEnabled(true);
            getSpinnerPort().setValue(proxyParam.getPort());
        }
        getTestURL().setText(ext.getTestUrl());
    }

    @Override
    public void validateParam(Object obj) throws Exception {}

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam optionsParam = (OptionsParam) obj;
        CallbackParam proxyParam = optionsParam.getParamSet(CallbackParam.class);

        proxyParam.setLocalAddress((String) localAddress.getSelectedItem());
        proxyParam.setRemoteAddress((String) remoteAddress.getSelectedItem());
        proxyParam.setSecure(secure.isSelected());
        if (getRandomPort().isSelected()) {
            proxyParam.setPort(0);
        } else {
            proxyParam.setPort(spinnerPort.getValue());
        }
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.callback";
    }
}

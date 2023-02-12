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

import java.awt.Dialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.AbstractFormDialog;

/** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
@Deprecated
@SuppressWarnings("serial")
class DialogAddProxy extends AbstractFormDialog {

    private static final long serialVersionUID = 4460797449668634319L;

    private static final String DIALOG_TITLE =
            Constant.messages.getString("options.proxy.dialog.proxy.add.title");

    private static final String CONFIRM_BUTTON_LABEL =
            Constant.messages.getString("options.proxy.dialog.proxy.add.button.confirm");

    protected OptionsLocalProxyPanel proxyPanel;

    protected ProxiesParamProxy proxy;

    private ExtensionProxies extension;

    public DialogAddProxy(Dialog owner, ExtensionProxies extension) {
        super(owner, DIALOG_TITLE);
        this.extension = extension;
        setConfirmButtonEnabled(true);
    }

    protected DialogAddProxy(Dialog owner, String title, ExtensionProxies extension) {
        super(owner, title);
        this.extension = extension;
    }

    @Override
    protected JPanel getFieldsPanel() {
        if (proxyPanel == null) {
            proxyPanel = new OptionsLocalProxyPanel();
        }
        return proxyPanel;
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }

    @Override
    protected void init() {
        proxy = null;
        this.getFieldsPanel(); // to initialise proxyPanel
        ProxiesParamProxy paramProxy = new ProxiesParamProxy(true);
        proxyPanel.setProxy(paramProxy);
    }

    @Override
    protected boolean validateFields() {
        ProxiesParamProxy testProxy = proxyPanel.getProxy();

        if (extension.getAdditionalProxy(testProxy.getAddress(), testProxy.getPort()) != null) {
            JOptionPane.showMessageDialog(
                    this,
                    Constant.messages.getString("options.proxy.dialog.proxy.warning.dup.message"),
                    Constant.messages.getString("options.proxy.dialog.proxy.warning.dup.title"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (this.proxy == null
                || !(this.proxy.getAddress().equals(testProxy.getAddress())
                        && this.proxy.getPort() == testProxy.getPort())) {
            // We're adding a proxy or changing the addr:port, so check that we can listen on this
            // combination
            if (!extension.canListenOn(testProxy.getAddress(), testProxy.getPort())) {
                JOptionPane.showMessageDialog(
                        this,
                        Constant.messages.getString(
                                "options.proxy.dialog.proxy.warning.fail.message",
                                testProxy.getAddress(),
                                Integer.toString(testProxy.getPort())),
                        Constant.messages.getString(
                                "options.proxy.dialog.proxy.warning.fail.title"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }

    @Override
    protected void performAction() {
        proxy = proxyPanel.getProxy();
    }

    @Override
    protected void clearFields() {}

    public ProxiesParamProxy getProxy() {
        return proxy;
    }

    public void clear() {
        this.proxy = null;
    }
}

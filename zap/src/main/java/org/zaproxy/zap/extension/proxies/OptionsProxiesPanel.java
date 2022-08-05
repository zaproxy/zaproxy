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

import java.awt.GridBagLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SortOrder;
import org.jdesktop.swingx.VerticalLayout;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;
import org.zaproxy.zap.view.LayoutHelper;

/** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
@Deprecated
@SuppressWarnings("serial")
public class OptionsProxiesPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;

    private ProxiesMultipleOptionsPanel proxiesOptionsPanel;

    private JScrollPane proxiesScrollPane;
    private JPanel scrollPanel;
    private OptionsLocalProxyPanel mainProxyPanel;
    private org.parosproxy.paros.extension.option.SecurityProtocolsPanel securityProtocolsPanel;
    private OptionsProxiesTableModel proxiesModel;
    private String currentAddress;
    private int currentPort;

    private ExtensionProxies extension;

    public OptionsProxiesPanel(ExtensionProxies extension) {
        super();
        this.extension = extension;
        initialize();
    }

    private void initialize() {
        this.setName(Constant.messages.getString("proxies.options.title"));
        this.setLayout(new GridBagLayout());
        this.add(getScrollPane(), LayoutHelper.getGBC(0, 0, 1, 1.0, 1.0));
    }

    private JScrollPane getScrollPane() {
        if (proxiesScrollPane == null) {
            proxiesScrollPane = new JScrollPane();
            proxiesScrollPane.setBorder(BorderFactory.createEmptyBorder());
            proxiesScrollPane.setViewportView(getScrollPanel());
        }
        return proxiesScrollPane;
    }

    private JPanel getScrollPanel() {
        if (scrollPanel == null) {
            scrollPanel = new JPanel(new VerticalLayout());
            scrollPanel.add(getMainProxyPanel());
            scrollPanel.add(getSecurityProtocolsPanel());
            scrollPanel.add(getProxiesOptionsPanel());
        }
        return scrollPanel;
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        ProxiesParam param = optionsParam.getParamSet(ProxiesParam.class);
        getMainProxyPanel().setProxy(param.getMainProxy());
        getSecurityProtocolsPanel()
                .setSecurityProtocolsEnabled(param.getSecurityProtocolsEnabled());
        getProxiesModel().setProxies(param.getProxies());
        getProxiesOptionsPanel().setRemoveWithoutConfirmation(!param.isConfirmRemoveProxy());
        this.currentAddress = param.getMainProxy().getAddress();
        this.currentPort = param.getMainProxy().getPort();
    }

    @Override
    public void validateParam(Object obj) throws Exception {
        getSecurityProtocolsPanel().validateSecurityProtocols();
        ProxiesParamProxy mainProxy = getMainProxyPanel().getProxy();
        String newAddress = mainProxy.getAddress();
        int newPort = mainProxy.getPort();

        if (!ExtensionProxies.isSameAddress(this.currentAddress, newAddress)
                || this.currentPort != newPort) {
            // Only check if they've changed, otherwise we'll still be listening on them
            if (!extension.canListenOn(newAddress, newPort)
                    || extension.getAdditionalProxy(newAddress, newPort) != null) {
                throw new Exception(
                        Constant.messages.getString(
                                "options.proxy.dialog.proxy.warning.fail.message",
                                newAddress,
                                Integer.toString(newPort)));
            }
        }
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam optionsParam = (OptionsParam) obj;
        ProxiesParam param = optionsParam.getParamSet(ProxiesParam.class);

        List<ProxiesParamProxy> proxies = getProxiesModel().getElements();
        param.setMainProxy(getMainProxyPanel().getProxy());
        param.setSecurityProtocolsEnabled(getSecurityProtocolsPanel().getSelectedProtocols());
        param.setProxies(proxies);
        param.setConfirmRemoveProxy(!getProxiesOptionsPanel().isRemoveWithoutConfirmation());
    }

    private ProxiesMultipleOptionsPanel getProxiesOptionsPanel() {
        if (proxiesOptionsPanel == null) {
            proxiesOptionsPanel = new ProxiesMultipleOptionsPanel(getProxiesModel(), extension);
            proxiesOptionsPanel.setBorder(
                    javax.swing.BorderFactory.createTitledBorder(
                            null,
                            Constant.messages.getString("options.proxy.additional.title"),
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            FontUtils.getFont(FontUtils.Size.standard)));
        }
        return proxiesOptionsPanel;
    }

    private OptionsLocalProxyPanel getMainProxyPanel() {
        if (mainProxyPanel == null) {
            mainProxyPanel = new OptionsLocalProxyPanel();
        }
        return mainProxyPanel;
    }

    private org.parosproxy.paros.extension.option.SecurityProtocolsPanel
            getSecurityProtocolsPanel() {
        if (securityProtocolsPanel == null) {
            securityProtocolsPanel =
                    new org.parosproxy.paros.extension.option.SecurityProtocolsPanel();
        }
        return securityProtocolsPanel;
    }

    private OptionsProxiesTableModel getProxiesModel() {
        if (proxiesModel == null) {
            proxiesModel = new OptionsProxiesTableModel();
        }
        return proxiesModel;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.localproxy";
    }

    private static class ProxiesMultipleOptionsPanel
            extends AbstractMultipleOptionsTablePanel<ProxiesParamProxy> {

        private static final long serialVersionUID = -115340627058929308L;

        private static final String REMOVE_DIALOG_TITLE =
                Constant.messages.getString("options.proxy.dialog.proxy.remove.title");
        private static final String REMOVE_DIALOG_TEXT =
                Constant.messages.getString("options.proxy.dialog.proxy.remove.text");

        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL =
                Constant.messages.getString("options.proxy.dialog.proxy.remove.button.confirm");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL =
                Constant.messages.getString("options.proxy.dialog.proxy.remove.button.cancel");

        private static final String REMOVE_DIALOG_CHECKBOX_LABEL =
                Constant.messages.getString("options.proxy.dialog.proxy.remove.checkbox.label");

        private DialogAddProxy addDialog = null;
        private DialogModifyProxy modifyDialog = null;

        private ExtensionProxies extension;

        public ProxiesMultipleOptionsPanel(
                OptionsProxiesTableModel model, ExtensionProxies extension) {
            super(model);

            this.extension = extension;

            getTable().getColumnExt(0).setPreferredWidth(20);
            getTable().setVisibleRowCount(10);
            getTable().setSortOrder(1, SortOrder.ASCENDING);
        }

        @Override
        public ProxiesParamProxy showAddDialogue() {
            if (addDialog == null) {
                addDialog =
                        new DialogAddProxy(View.getSingleton().getOptionsDialog(null), extension);
                addDialog.pack();
            }
            addDialog.setVisible(true);
            ProxiesParamProxy proxy = addDialog.getProxy();
            addDialog.clear();

            return proxy;
        }

        @Override
        public ProxiesParamProxy showModifyDialogue(ProxiesParamProxy e) {
            if (modifyDialog == null) {
                modifyDialog =
                        new DialogModifyProxy(
                                View.getSingleton().getOptionsDialog(null), extension);
                modifyDialog.pack();
            }
            modifyDialog.setProxy(e);
            modifyDialog.setVisible(true);

            ProxiesParamProxy proxy = modifyDialog.getProxy();
            modifyDialog.clear();
            return proxy;
        }

        @Override
        public boolean showRemoveDialogue(ProxiesParamProxy e) {
            JCheckBox removeWithoutConfirmationCheckBox =
                    new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
            Object[] messages = {REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox};
            int option =
                    JOptionPane.showOptionDialog(
                            View.getSingleton().getMainFrame(),
                            messages,
                            REMOVE_DIALOG_TITLE,
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[] {
                                REMOVE_DIALOG_CONFIRM_BUTTON_LABEL,
                                REMOVE_DIALOG_CANCEL_BUTTON_LABEL
                            },
                            null);

            if (option == JOptionPane.OK_OPTION) {
                setRemoveWithoutConfirmation(removeWithoutConfirmationCheckBox.isSelected());

                return true;
            }

            return false;
        }
    }
}

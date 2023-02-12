/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.api;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SortOrder;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.network.DomainMatcher;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;
import org.zaproxy.zap.view.LayoutHelper;

public class OptionsApiPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private JPanel panelMisc = null;
    private JCheckBox chkEnabled = null;
    private JCheckBox chkUiEnabled = null;
    private JCheckBox chkSecureOnly = null;
    private JCheckBox reportPermErrors = null;
    private JCheckBox disableKey = null;
    private JCheckBox incErrorDetails = null;
    private JCheckBox autofillKey = null;
    private JCheckBox enableJSONP = null;
    private JCheckBox noKeyForSafeOps = null;
    private ZapTextField keyField = null;
    private JButton generateKeyButton = null;

    private PermittedAddressesPanel permittedAddressesPanel;
    private PermittedAddressesTableModel permittedAddressesTableModel;

    // private JCheckBox chkPostActions = null;

    public OptionsApiPanel() {
        super();
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("api.options.title"));
        this.add(getPanelMisc(), getPanelMisc().getName());
    }
    /**
     * This method initializes panelMisc
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPanelMisc() {
        if (panelMisc == null) {
            panelMisc = new JPanel();
            panelMisc.setLayout(new GridBagLayout());
            int y = 0;
            panelMisc.add(getChkEnabled(), LayoutHelper.getGBC(0, y++, 1, 0.5));
            panelMisc.add(getChkUiEnabled(), LayoutHelper.getGBC(0, y++, 1, 0.5));
            panelMisc.add(getChkSecureOnly(), LayoutHelper.getGBC(0, y++, 1, 0.5));

            panelMisc.add(
                    new JLabel(Constant.messages.getString("api.options.label.apiKey")),
                    LayoutHelper.getGBC(0, y, 1, 0.5));
            panelMisc.add(getKeyField(), LayoutHelper.getGBC(1, y++, 1, 0.5));
            panelMisc.add(getGenerateKeyButton(), LayoutHelper.getGBC(1, y++, 1, 0.5));

            JPanel jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.setBorder(
                    javax.swing.BorderFactory.createTitledBorder(
                            null,
                            Constant.messages.getString("api.options.addr.title"),
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            FontUtils.getFont(FontUtils.Size.standard)));

            jPanel.add(getProxyPermittedAddressesPanel(), LayoutHelper.getGBC(0, 0, 1, 1.0, 1.0));
            panelMisc.add(jPanel, LayoutHelper.getGBC(0, y++, 2, 1.0, 1.0));

            JLabel warning =
                    new ZapHtmlLabel(
                            Constant.messages.getString("api.options.label.testingWarning"));
            warning.setForeground(Color.RED);
            panelMisc.add(warning, LayoutHelper.getGBC(0, y++, 2, 0.5D));
            panelMisc.add(getDisableKey(), LayoutHelper.getGBC(0, y++, 1, 0.5));
            panelMisc.add(getNoKeyForSafeOps(), LayoutHelper.getGBC(0, y++, 1, 0.5));
            panelMisc.add(getReportPermErrors(), LayoutHelper.getGBC(0, y++, 1, 0.5));
            panelMisc.add(getIncErrorDetails(), LayoutHelper.getGBC(0, y++, 1, 0.5));
            panelMisc.add(getAutofillKey(), LayoutHelper.getGBC(0, y++, 1, 0.5));
            panelMisc.add(getEnableJSONP(), LayoutHelper.getGBC(0, y++, 1, 0.5));

            panelMisc.add(new JLabel(), LayoutHelper.getGBC(0, y, 1, 0.5D, 1.0D)); // Spacer
        }
        return panelMisc;
    }
    /**
     * This method initializes chkProcessImages
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getChkEnabled() {
        if (chkEnabled == null) {
            chkEnabled = new JCheckBox();
            chkEnabled.setText(Constant.messages.getString("api.options.enabled"));
            chkEnabled.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            chkEnabled.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return chkEnabled;
    }

    private JCheckBox getChkUiEnabled() {
        if (chkUiEnabled == null) {
            chkUiEnabled = new JCheckBox();
            chkUiEnabled.setText(Constant.messages.getString("api.options.uiEnabled"));
            chkUiEnabled.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            chkUiEnabled.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return chkUiEnabled;
    }

    private JCheckBox getChkSecureOnly() {
        if (chkSecureOnly == null) {
            chkSecureOnly = new JCheckBox();
            chkSecureOnly.setText(Constant.messages.getString("api.options.secure"));
            chkSecureOnly.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            chkSecureOnly.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return chkSecureOnly;
    }

    private JCheckBox getDisableKey() {
        if (disableKey == null) {
            disableKey = new JCheckBox();
            disableKey.setText(Constant.messages.getString("api.options.disableKey"));
            disableKey.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            disableKey.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
            disableKey.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            getKeyField().setEnabled(!disableKey.isSelected());
                            getGenerateKeyButton().setEnabled(!disableKey.isSelected());
                            if (!disableKey.isSelected()) {
                                // Repopulate the previously used value
                                getKeyField()
                                        .setText(
                                                Model.getSingleton()
                                                        .getOptionsParam()
                                                        .getApiParam()
                                                        .getRealKey());
                            }
                        }
                    });
        }
        return disableKey;
    }

    private JCheckBox getEnableJSONP() {
        if (enableJSONP == null) {
            enableJSONP = new JCheckBox();
            enableJSONP.setText(Constant.messages.getString("api.options.enableJSONP"));
            enableJSONP.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            enableJSONP.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return enableJSONP;
    }

    private JCheckBox getReportPermErrors() {
        if (reportPermErrors == null) {
            reportPermErrors = new JCheckBox();
            reportPermErrors.setText(Constant.messages.getString("api.options.reportPermErrors"));
            reportPermErrors.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            reportPermErrors.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return reportPermErrors;
    }

    private JCheckBox getNoKeyForSafeOps() {
        if (noKeyForSafeOps == null) {
            noKeyForSafeOps = new JCheckBox();
            noKeyForSafeOps.setText(Constant.messages.getString("api.options.noKeyForSafeOps"));
            noKeyForSafeOps.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            noKeyForSafeOps.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return noKeyForSafeOps;
    }

    private JCheckBox getIncErrorDetails() {
        if (incErrorDetails == null) {
            incErrorDetails = new JCheckBox();
            incErrorDetails.setText(Constant.messages.getString("api.options.incErrors"));
            incErrorDetails.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            incErrorDetails.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return incErrorDetails;
    }

    private JCheckBox getAutofillKey() {
        if (autofillKey == null) {
            autofillKey = new JCheckBox();
            autofillKey.setText(Constant.messages.getString("api.options.autofillKey"));
            autofillKey.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            autofillKey.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return autofillKey;
    }

    private ZapTextField getKeyField() {
        if (keyField == null) {
            keyField = new ZapTextField();
        }
        return keyField;
    }

    private JButton getGenerateKeyButton() {
        if (generateKeyButton == null) {
            generateKeyButton =
                    new JButton(Constant.messages.getString("api.options.button.generateKey"));
            generateKeyButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            getKeyField().setText(ExtensionAPI.generateApiKey());
                        }
                    });
        }
        return generateKeyButton;
    }

    /*
    public JCheckBox getChkPostActions() {
    	if (chkPostActions == null) {
    		chkPostActions = new JCheckBox();
    		chkPostActions.setText(Constant.messages.getString("api.options.postactions"));
    		chkPostActions.setVerticalAlignment(javax.swing.SwingConstants.TOP);
    		chkPostActions.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
    	}
    	return chkPostActions;
    }
    */

    @Override
    public void initParam(Object obj) {
        OptionsParam options = (OptionsParam) obj;
        getChkEnabled().setSelected(options.getApiParam().isEnabled());
        getChkUiEnabled().setSelected(options.getApiParam().isUiEnabled());
        getChkSecureOnly().setSelected(options.getApiParam().isSecureOnly());
        getDisableKey().setSelected(options.getApiParam().isDisableKey());
        getIncErrorDetails().setSelected(options.getApiParam().isIncErrorDetails());
        getAutofillKey().setSelected(options.getApiParam().isAutofillKey());
        getEnableJSONP().setSelected(options.getApiParam().isEnableJSONP());
        getReportPermErrors().setSelected(options.getApiParam().isReportPermErrors());
        getNoKeyForSafeOps().setSelected(options.getApiParam().isNoKeyForSafeOps());
        getKeyField().setText(options.getApiParam().getKey());
        // getChkPostActions().setSelected(options.getApiParam().isPostActions());

        getKeyField().setEnabled(!disableKey.isSelected());
        getGenerateKeyButton().setEnabled(!disableKey.isSelected());
        getPermittedAddressesTableModel()
                .setAddresses(options.getApiParam().getPermittedAddresses());
        getProxyPermittedAddressesPanel()
                .setRemoveWithoutConfirmation(
                        !options.getApiParam().isConfirmRemovePermittedAddress());
    }

    @Override
    public void validateParam(Object obj) throws Exception {
        if (!getDisableKey().isSelected() && getKeyField().getText().length() == 0) {
            getKeyField().requestFocusInWindow();
            throw new Exception(Constant.messages.getString("api.options.nokey.error"));
        }
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam options = (OptionsParam) obj;
        options.getApiParam().setEnabled(getChkEnabled().isSelected());
        options.getApiParam().setUiEnabled(getChkUiEnabled().isSelected());
        options.getApiParam().setSecureOnly(getChkSecureOnly().isSelected());
        options.getApiParam().setDisableKey(getDisableKey().isSelected());
        options.getApiParam().setIncErrorDetails(getIncErrorDetails().isSelected());
        options.getApiParam().setAutofillKey(getAutofillKey().isSelected());
        options.getApiParam().setEnableJSONP(getEnableJSONP().isSelected());
        options.getApiParam().setReportPermErrors(getReportPermErrors().isSelected());
        options.getApiParam().setNoKeyForSafeOps(getNoKeyForSafeOps().isSelected());

        if (!getDisableKey().isSelected()) {
            // Dont loose the old value on disabling
            options.getApiParam().setKey(getKeyField().getText());
        }
        // options.getApiParam().setPostActions(getChkPostActions().isEnabled());

        options.getApiParam()
                .setPermittedAddresses(getPermittedAddressesTableModel().getElements());
        options.getApiParam()
                .setConfirmRemovePermittedAddress(
                        !getProxyPermittedAddressesPanel().isRemoveWithoutConfirmation());
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.api";
    }

    private PermittedAddressesPanel getProxyPermittedAddressesPanel() {
        if (permittedAddressesPanel == null) {
            permittedAddressesPanel =
                    new PermittedAddressesPanel(getPermittedAddressesTableModel());
        }
        return permittedAddressesPanel;
    }

    private PermittedAddressesTableModel getPermittedAddressesTableModel() {
        if (permittedAddressesTableModel == null) {
            permittedAddressesTableModel = new PermittedAddressesTableModel();
        }
        return permittedAddressesTableModel;
    }

    private static class PermittedAddressesPanel
            extends AbstractMultipleOptionsTablePanel<DomainMatcher> {

        private static final long serialVersionUID = 2332044353650231701L;

        private static final String REMOVE_DIALOG_TITLE =
                Constant.messages.getString("api.options.addr.dialog.remove.title");
        private static final String REMOVE_DIALOG_TEXT =
                Constant.messages.getString("api.options.addr.dialog.remove.text");

        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL =
                Constant.messages.getString("api.options.addr.dialog.remove.button.confirm");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL =
                Constant.messages.getString("api.options.addr.dialog.remove.button.cancel");

        private static final String REMOVE_DIALOG_CHECKBOX_LABEL =
                Constant.messages.getString("api.options.addr.dialog.remove.checkbox.label");

        private DialogAddPermittedAddress addDialog = null;
        private DialogModifyPermittedAddress modifyDialog = null;

        public PermittedAddressesPanel(PermittedAddressesTableModel model) {
            super(model);

            getTable().setVisibleRowCount(5);
            getTable().setSortOrder(2, SortOrder.ASCENDING);
        }

        @Override
        public DomainMatcher showAddDialogue() {
            if (addDialog == null) {
                addDialog =
                        new DialogAddPermittedAddress(View.getSingleton().getOptionsDialog(null));
                addDialog.pack();
            }
            addDialog.setVisible(true);

            DomainMatcher hostAuthentication = addDialog.getAddress();
            addDialog.clear();

            return hostAuthentication;
        }

        @Override
        public DomainMatcher showModifyDialogue(DomainMatcher e) {
            if (modifyDialog == null) {
                modifyDialog =
                        new DialogModifyPermittedAddress(
                                View.getSingleton().getOptionsDialog(null));
                modifyDialog.pack();
            }
            modifyDialog.setAddress(e);
            modifyDialog.setVisible(true);

            DomainMatcher addr = modifyDialog.getAddress();
            modifyDialog.clear();

            if (!addr.equals(e)) {
                return addr;
            }

            return null;
        }

        @Override
        public boolean showRemoveDialogue(DomainMatcher e) {
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

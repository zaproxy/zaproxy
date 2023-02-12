/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Objects;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SortOrder;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.core.scanner.ScannerParamFilter;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.view.AbstractMultipleOptionsBaseTablePanel;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * Panel for Variant configuration
 *
 * @author yhawke (2014)
 */
public class OptionsVariantPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Flag that indicates whether or not the {@code ExtensionScript} is enabled.
     *
     * <p>Lazily initialised.
     *
     * @see #isExtensionScriptEnabled()
     */
    private static Boolean extensionScriptEnabled;

    private JPanel panelVariant = null;

    // Checkbox for Target definitions
    private JCheckBox chkInjectableQueryString = null;
    private JCheckBox chkAddQueryParam = null;
    private JCheckBox chkInjectableUrlPath = null;
    private JCheckBox chkInjectablePostData = null;
    private JCheckBox chkInjectableHeaders = null;
    private JCheckBox chkInjectableHeadersAllRequests;
    private JCheckBox chkInjectableCookie = null;

    // Checkbox for RPC to be enabled definitions
    private JCheckBox chkRPCMultipart = null;
    private JCheckBox chkRPCXML = null;
    private JCheckBox chkRPCJSON = null;
    private JCheckBox chkScanNullJsonValues;
    private JCheckBox chkRPCGWT = null;
    private JCheckBox chkRPCoData = null;
    private JCheckBox chkRPCDWR = null;
    private JCheckBox chkRPCCustom = null;

    // Table for Parameter exclusions
    private ExcludedParameterPanel excludedParamPanel;
    private ExcludedParameterTableModel excludedParamModel = null;

    /**
     * The reason why the variants are disabled.
     *
     * <p>Never {@code null}.
     *
     * @see #labelReasonVariantsDisabled
     */
    private String reasonVariantsDisabled;

    /**
     * The label that shows the {@link #reasonVariantsDisabled reason} why the variants are
     * disabled.
     *
     * <p>The label is only shown if the reason is non-empty.
     *
     * @see #setAllInjectableAndRPC(boolean)
     */
    private JLabel labelReasonVariantsDisabled;

    /** General Constructor */
    public OptionsVariantPanel() {
        super();

        reasonVariantsDisabled = "";
        labelReasonVariantsDisabled = new JLabel();
        labelReasonVariantsDisabled.setVisible(false);

        initialize();
    }

    /** This method initializes the Panel */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("variant.options.title"));
        this.add(getPanelScanner(), getPanelScanner().getName());
    }

    private JPanel getPanelScanner() {
        if (panelVariant == null) {
            panelVariant = new JPanel();
            panelVariant.setLayout(new GridBagLayout());
            panelVariant.setName("");

            // Add Injectable Params Section
            // ---------------------------------------------
            JPanel panelInjectable = new JPanel();
            panelInjectable.setLayout(new GridBagLayout());

            int row = 1;
            panelInjectable.add(
                    this.getChkInjectableQueryString(),
                    LayoutHelper.getGBC(
                            0,
                            row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 8, 2, 2)));
            panelInjectable.add(
                    this.getChkAddQueryParam(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 32, 2, 2)));
            panelInjectable.add(
                    this.getChkInjectablePostData(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 8, 2, 2)));
            panelInjectable.add(
                    this.getChkInjectableUrlPath(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 8, 2, 2)));
            panelInjectable.add(
                    this.getChkInjectableHeaders(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 8, 2, 2)));
            panelInjectable.add(
                    this.getChkInjectableHeadersAllRequests(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 32, 2, 2)));
            panelInjectable.add(
                    this.getChkInjectableCookie(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 8, 2, 2)));

            panelVariant.add(
                    new JLabel(Constant.messages.getString("variant.options.injectable.label")),
                    LayoutHelper.getGBC(
                            0,
                            0,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));

            panelVariant.add(
                    panelInjectable,
                    LayoutHelper.getGBC(
                            0,
                            1,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));

            // Add RPC Enabling Section
            // ---------------------------------------------
            JPanel panelRPC = new JPanel();
            panelRPC.setLayout(new GridBagLayout());

            row = 1;
            panelRPC.add(
                    this.getChkRPCMultipart(),
                    LayoutHelper.getGBC(
                            0,
                            row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 8, 2, 2)));
            panelRPC.add(
                    this.getChkRPCXML(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 8, 2, 2)));
            panelRPC.add(
                    this.getChkRPCJSON(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 8, 2, 2)));
            panelRPC.add(
                    this.getChkScanNullJsonValues(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 32, 2, 2)));

            panelRPC.add(
                    this.getChkRPCGWT(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 8, 2, 2)));
            panelRPC.add(
                    this.getChkRPCoData(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 8, 2, 2)));
            panelRPC.add(
                    this.getChkRPCDWR(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 8, 2, 2)));

            row = 0;
            panelVariant.add(
                    new JLabel(Constant.messages.getString("variant.options.rpc.label")),
                    LayoutHelper.getGBC(
                            1,
                            row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));

            panelVariant.add(
                    panelRPC,
                    LayoutHelper.getGBC(
                            1,
                            ++row,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));

            // Space Before
            panelVariant.add(
                    this.getChkRPCCustom(),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            2,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(16, 2, 2, 2)));

            panelVariant.add(
                    labelReasonVariantsDisabled,
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            2,
                            1.0D,
                            1.0D,
                            GridBagConstraints.BOTH,
                            new Insets(2, 2, 2, 2)));

            // Excluded Parameters
            panelVariant.add(
                    new ZapHtmlLabel(
                            Constant.messages.getString(
                                    "variant.options.excludedparam.label.tokens")),
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            2,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(16, 2, 2, 2)));

            // Set an header on it
            excludedParamPanel = new ExcludedParameterPanel(getExcludedParameterModel());
            panelVariant.add(
                    excludedParamPanel,
                    LayoutHelper.getGBC(
                            0,
                            ++row,
                            2,
                            1.0D,
                            1.0D,
                            GridBagConstraints.BOTH,
                            new Insets(2, 2, 2, 2)));
        }

        return panelVariant;
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam options = (OptionsParam) obj;
        ScannerParam param = options.getParamSet(ScannerParam.class);
        initParam(param);
    }

    /**
     * Initialises the panel (that is, the options shown in it) with the given options.
     *
     * @param param the options to initialise the panel
     */
    public void initParam(ScannerParam param) {
        // Set targets and RPC selections
        int targets = param.getTargetParamsInjectable();
        this.getChkInjectableQueryString()
                .setSelected((targets & ScannerParam.TARGET_QUERYSTRING) != 0);
        this.getChkAddQueryParam().setSelected(param.isAddQueryParam());
        this.getChkAddQueryParam()
                .setEnabled(
                        getChkInjectableQueryString().isEnabled()
                                && getChkInjectableQueryString().isSelected());
        this.getChkInjectableUrlPath().setSelected((targets & ScannerParam.TARGET_URLPATH) != 0);
        this.getChkInjectablePostData().setSelected((targets & ScannerParam.TARGET_POSTDATA) != 0);
        this.getChkInjectableHeaders()
                .setSelected((targets & ScannerParam.TARGET_HTTPHEADERS) != 0);
        this.getChkInjectableHeadersAllRequests().setSelected(param.isScanHeadersAllRequests());
        this.getChkInjectableHeadersAllRequests()
                .setEnabled(
                        getChkInjectableHeaders().isEnabled()
                                && getChkInjectableHeaders().isSelected());
        this.getChkInjectableCookie().setSelected((targets & ScannerParam.TARGET_COOKIE) != 0);

        int rpcEnabled = param.getTargetParamsEnabledRPC();
        this.getChkRPCMultipart().setSelected((rpcEnabled & ScannerParam.RPC_MULTIPART) != 0);
        this.getChkRPCXML().setSelected((rpcEnabled & ScannerParam.RPC_XML) != 0);
        this.getChkRPCJSON().setSelected((rpcEnabled & ScannerParam.RPC_JSON) != 0);
        this.getChkScanNullJsonValues().setSelected(param.isScanNullJsonValues());
        this.getChkRPCGWT().setSelected((rpcEnabled & ScannerParam.RPC_GWT) != 0);
        this.getChkRPCoData().setSelected((rpcEnabled & ScannerParam.RPC_ODATA) != 0);
        this.getChkRPCDWR().setSelected((rpcEnabled & ScannerParam.RPC_DWR) != 0);
        this.getChkRPCCustom().setSelected((rpcEnabled & ScannerParam.RPC_CUSTOM) != 0);

        this.getExcludedParameterModel().setTokens(param.getExcludedParamList());
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam options = (OptionsParam) obj;
        ScannerParam param = options.getParamSet(ScannerParam.class);
        saveParam(param);
    }

    /**
     * Saves the options shown in the panel to the given options object.
     *
     * @param param the options object where to save the options shown
     */
    public void saveParam(ScannerParam param) {
        // Set Injectable Targets
        int targets = 0;
        if (this.getChkInjectableQueryString().isSelected()) {
            targets |= ScannerParam.TARGET_QUERYSTRING;
        }

        param.setAddQueryParam(getChkAddQueryParam().isSelected());

        if (this.getChkInjectableUrlPath().isSelected()) {
            targets |= ScannerParam.TARGET_URLPATH;
        }

        if (this.getChkInjectablePostData().isSelected()) {
            targets |= ScannerParam.TARGET_POSTDATA;
        }

        if (this.getChkInjectableHeaders().isSelected()) {
            targets |= ScannerParam.TARGET_HTTPHEADERS;
        }

        param.setScanHeadersAllRequests(getChkInjectableHeadersAllRequests().isSelected());

        if (this.getChkInjectableCookie().isSelected()) {
            targets |= ScannerParam.TARGET_COOKIE;
        }

        param.setTargetParamsInjectable(targets);

        // Set Enabled RPC schemas
        int enabledRpc = 0;
        if (this.getChkRPCMultipart().isSelected()) {
            enabledRpc |= ScannerParam.RPC_MULTIPART;
        }

        if (this.getChkRPCXML().isSelected()) {
            enabledRpc |= ScannerParam.RPC_XML;
        }

        if (this.getChkRPCJSON().isSelected()) {
            enabledRpc |= ScannerParam.RPC_JSON;
        }

        param.setScanNullJsonValues(getChkScanNullJsonValues().isSelected());

        if (this.getChkRPCGWT().isSelected()) {
            enabledRpc |= ScannerParam.RPC_GWT;
        }

        if (this.getChkRPCoData().isSelected()) {
            enabledRpc |= ScannerParam.RPC_ODATA;
        }

        if (this.getChkRPCDWR().isSelected()) {
            enabledRpc |= ScannerParam.RPC_DWR;
        }

        if (this.getChkRPCCustom().isSelected()) {
            enabledRpc |= ScannerParam.RPC_CUSTOM;
        }

        param.setTargetParamsEnabledRPC(enabledRpc);

        param.setExcludedParamList(getExcludedParameterModel().getElements());
    }

    /**
     * Set all checkbox to a specific value
     *
     * @param enabled true if all the checkbox should be enabled, false otherwise
     * @see #setReasonVariantsDisabled(String)
     */
    public void setAllInjectableAndRPC(boolean enabled) {

        this.getChkInjectableQueryString().setEnabled(enabled);
        this.getChkAddQueryParam()
                .setEnabled(enabled && getChkInjectableQueryString().isSelected());
        this.getChkInjectableUrlPath().setEnabled(enabled);
        this.getChkInjectablePostData().setEnabled(enabled);
        this.getChkInjectableHeaders().setEnabled(enabled);
        this.getChkInjectableHeadersAllRequests()
                .setEnabled(enabled && getChkInjectableHeaders().isSelected());
        this.getChkInjectableCookie().setEnabled(enabled);

        this.getChkRPCMultipart().setEnabled(enabled);
        this.getChkRPCXML().setEnabled(enabled);
        this.getChkRPCJSON().setEnabled(enabled);
        this.getChkScanNullJsonValues().setEnabled(enabled && getChkRPCJSON().isSelected());
        this.getChkRPCGWT().setEnabled(enabled);
        this.getChkRPCoData().setEnabled(enabled);
        this.getChkRPCDWR().setEnabled(enabled);
        this.getChkRPCCustom().setEnabled(enabled && isExtensionScriptEnabled());

        if (!reasonVariantsDisabled.isEmpty()) {
            labelReasonVariantsDisabled.setVisible(!enabled);
        }
    }

    /**
     * This method initializes the table Model
     *
     * @return the table model
     */
    private ExcludedParameterTableModel getExcludedParameterModel() {
        if (excludedParamModel == null) {
            excludedParamModel = new ExcludedParameterTableModel();
        }
        return excludedParamModel;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.ascaninput";
    }

    /**
     * Sets the reason to show when the the variants are disabled.
     *
     * @param reason the reason that indicates why the variants are disabled.
     * @since 2.8.0
     * @throws NullPointerException if the given {@code reason} is {@code null}.
     * @see #setAllInjectableAndRPC(boolean)
     */
    public void setReasonVariantsDisabled(String reason) {
        reasonVariantsDisabled = Objects.requireNonNull(reason);

        labelReasonVariantsDisabled.setText(reasonVariantsDisabled);
        if (reasonVariantsDisabled.isEmpty() && labelReasonVariantsDisabled.isVisible()) {
            labelReasonVariantsDisabled.setVisible(false);
        }
    }

    private JCheckBox getChkInjectableQueryString() {
        if (chkInjectableQueryString == null) {
            chkInjectableQueryString = new JCheckBox();
            chkInjectableQueryString.setText(
                    Constant.messages.getString("variant.options.injectable.querystring.label"));
            chkInjectableQueryString.addItemListener(
                    e ->
                            getChkAddQueryParam()
                                    .setEnabled(e.getStateChange() == ItemEvent.SELECTED));
        }
        return chkInjectableQueryString;
    }

    private JCheckBox getChkAddQueryParam() {
        if (chkAddQueryParam == null) {
            chkAddQueryParam = new JCheckBox();
            chkAddQueryParam.setText(
                    Constant.messages.getString("variant.options.injectable.addqueryparam.label"));
            chkAddQueryParam.setToolTipText(
                    Constant.messages.getString(
                            "variant.options.injectable.addqueryparam.toolTip"));
        }
        return chkAddQueryParam;
    }

    private JCheckBox getChkInjectableUrlPath() {
        if (chkInjectableUrlPath == null) {
            chkInjectableUrlPath = new JCheckBox();
            chkInjectableUrlPath.setText(
                    Constant.messages.getString("variant.options.injectable.urlpath.label"));
        }
        return chkInjectableUrlPath;
    }

    private JCheckBox getChkInjectablePostData() {
        if (chkInjectablePostData == null) {
            chkInjectablePostData = new JCheckBox();
            chkInjectablePostData.setText(
                    Constant.messages.getString("variant.options.injectable.postdata.label"));
        }
        return chkInjectablePostData;
    }

    private JCheckBox getChkInjectableHeaders() {
        if (chkInjectableHeaders == null) {
            chkInjectableHeaders = new JCheckBox();
            chkInjectableHeaders.setText(
                    Constant.messages.getString("variant.options.injectable.headers.label"));
            chkInjectableHeaders.addItemListener(
                    new ItemListener() {

                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            getChkInjectableHeadersAllRequests()
                                    .setEnabled(e.getStateChange() == ItemEvent.SELECTED);
                        }
                    });
        }
        return chkInjectableHeaders;
    }

    private JCheckBox getChkInjectableHeadersAllRequests() {
        if (chkInjectableHeadersAllRequests == null) {
            chkInjectableHeadersAllRequests = new JCheckBox();
            chkInjectableHeadersAllRequests.setText(
                    Constant.messages.getString(
                            "variant.options.injectable.headersAllRequests.label"));
            chkInjectableHeadersAllRequests.setToolTipText(
                    Constant.messages.getString(
                            "variant.options.injectable.headersAllRequests.toolTip"));
        }
        return chkInjectableHeadersAllRequests;
    }

    private JCheckBox getChkInjectableCookie() {
        if (chkInjectableCookie == null) {
            chkInjectableCookie = new JCheckBox();
            chkInjectableCookie.setText(
                    Constant.messages.getString("variant.options.injectable.cookie.label"));
        }
        return chkInjectableCookie;
    }

    private JCheckBox getChkRPCMultipart() {
        if (chkRPCMultipart == null) {
            chkRPCMultipart = new JCheckBox();
            chkRPCMultipart.setText(
                    Constant.messages.getString("variant.options.rpc.multipart.label"));
        }
        return chkRPCMultipart;
    }

    private JCheckBox getChkRPCXML() {
        if (chkRPCXML == null) {
            chkRPCXML = new JCheckBox();
            chkRPCXML.setText(Constant.messages.getString("variant.options.rpc.xml.label"));
        }
        return chkRPCXML;
    }

    private JCheckBox getChkRPCJSON() {
        if (chkRPCJSON == null) {
            chkRPCJSON = new JCheckBox();
            chkRPCJSON.setText(Constant.messages.getString("variant.options.rpc.json.label"));
            chkRPCJSON.addItemListener(
                    e ->
                            getChkScanNullJsonValues()
                                    .setEnabled(e.getStateChange() == ItemEvent.SELECTED));
        }
        return chkRPCJSON;
    }

    private JCheckBox getChkScanNullJsonValues() {
        if (chkScanNullJsonValues == null) {
            chkScanNullJsonValues = new JCheckBox();
            chkScanNullJsonValues.setText(
                    Constant.messages.getString("variant.options.rpc.json.nulls.label"));
        }
        return chkScanNullJsonValues;
    }

    private JCheckBox getChkRPCGWT() {
        if (chkRPCGWT == null) {
            chkRPCGWT = new JCheckBox();
            chkRPCGWT.setText(Constant.messages.getString("variant.options.rpc.gwt.label"));
        }
        return chkRPCGWT;
    }

    private JCheckBox getChkRPCoData() {
        if (chkRPCoData == null) {
            chkRPCoData = new JCheckBox();
            chkRPCoData.setText(Constant.messages.getString("variant.options.rpc.odata.label"));
        }
        return chkRPCoData;
    }

    private JCheckBox getChkRPCDWR() {
        if (chkRPCDWR == null) {
            chkRPCDWR = new JCheckBox();
            chkRPCDWR.setText(Constant.messages.getString("variant.options.rpc.dwr.label"));
        }
        return chkRPCDWR;
    }

    private JCheckBox getChkRPCCustom() {
        if (chkRPCCustom == null) {
            chkRPCCustom = new JCheckBox();
            chkRPCCustom.setText(Constant.messages.getString("variant.options.rpc.custom.label"));
            chkRPCCustom.setEnabled(isExtensionScriptEnabled());
        }
        return chkRPCCustom;
    }

    private static boolean isExtensionScriptEnabled() {
        if (extensionScriptEnabled == null) {
            extensionScriptEnabled =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.class)
                            != null;
        }
        return extensionScriptEnabled;
    }

    private static class ExcludedParameterPanel
            extends AbstractMultipleOptionsBaseTablePanel<ScannerParamFilter> {

        private static final long serialVersionUID = 1L;
        private static final String REMOVE_DIALOG_TITLE =
                Constant.messages.getString(
                        "variant.options.excludedparam.dialog.token.remove.title");
        private static final String REMOVE_DIALOG_TEXT =
                Constant.messages.getString(
                        "variant.options.excludedparam.dialog.token.remove.text");

        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL =
                Constant.messages.getString(
                        "variant.options.excludedparam.dialog.token.remove.button.confirm");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL =
                Constant.messages.getString(
                        "variant.options.excludedparam.dialog.token.remove.button.cancel");

        private static final String REMOVE_DIALOG_CHECKBOX_LABEL =
                Constant.messages.getString(
                        "variant.options.excludedparam.dialog.token.remove.checkbox.label");

        private ExcludedParameterAddDialog addDialog = null;
        private ExcludedParameterModifyDialog modifyDialog = null;

        public ExcludedParameterPanel(ExcludedParameterTableModel model) {
            super(model);
            getTable().setSortOrder(0, SortOrder.ASCENDING);
            getTable().getColumnModel().getColumn(0).setPreferredWidth(100);
            getTable().getColumnModel().getColumn(1).setPreferredWidth(50);
            getTable().getColumnModel().getColumn(2).setPreferredWidth(200);
        }

        @Override
        public ScannerParamFilter showAddDialogue() {
            if (addDialog == null) {
                addDialog =
                        new ExcludedParameterAddDialog(View.getSingleton().getOptionsDialog(null));
                addDialog.pack();
            }

            addDialog.setTokens(model.getElements());
            addDialog.setVisible(true);

            ScannerParamFilter filter = addDialog.getToken();
            addDialog.clear();
            return filter;
        }

        @Override
        public ScannerParamFilter showModifyDialogue(ScannerParamFilter e) {
            if (modifyDialog == null) {
                modifyDialog =
                        new ExcludedParameterModifyDialog(
                                View.getSingleton().getOptionsDialog(null));
                modifyDialog.pack();
            }

            modifyDialog.setTokens(model.getElements());
            modifyDialog.setToken(e);
            modifyDialog.setVisible(true);

            ScannerParamFilter token = modifyDialog.getToken();
            modifyDialog.clear();

            if (!token.equals(e)) {
                return token;
            }

            return null;
        }

        @Override
        public boolean showRemoveDialogue(ScannerParamFilter e) {

            JCheckBox removeWithoutConfirmationCB = new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
            Object[] messages = {REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCB};

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

            return (option == JOptionPane.OK_OPTION);
        }
    }
}

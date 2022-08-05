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
package org.zaproxy.zap.extension.ascan;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.PositiveValuesSlider;

@SuppressWarnings("serial")
public class OptionsScannerPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private JPanel panelScanner = null;
    private JSlider sliderHostPerScan = null;
    private JSlider sliderThreadsPerHost = null;
    private ZapNumberSpinner spinnerDelayInMs = null;
    private JLabel labelThreadsPerHostValue = null;
    private ZapNumberSpinner spinnerMaxRuleDuration = null;
    private ZapNumberSpinner spinnerMaxScanDuration = null;
    private ZapNumberSpinner spinnerMaxResultsList = null;
    private JCheckBox chkInjectPluginIdInHeader = null;
    private JCheckBox chkHandleAntiCsrfTokens = null;
    private JCheckBox chkPromptInAttackMode = null;
    private JCheckBox chkRescanInAttackMode = null;
    private JComboBox<String> defaultAscanPolicy = null;
    private JComboBox<String> defaultAttackPolicy = null;
    private JCheckBox allowAttackModeOnStart = null;
    private ZapNumberSpinner spinnerMaxChartTime = null;

    private ExtensionActiveScan extension;

    /**
     * Constructs an {@code OptionsScannerPanel} with the given active scan extension.
     *
     * @param extension the active scan extension, to obtain scan policy names
     */
    public OptionsScannerPanel(ExtensionActiveScan extension) {
        super();
        this.extension = extension;
        initialize();
    }

    /** This method initializes the Panel */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("ascan.options.title"));
        this.add(new JScrollPane(getPanelScanner()));
    }

    private JPanel getPanelScanner() {
        if (panelScanner == null) {
            panelScanner = new JPanel();
            panelScanner.setLayout(new GridBagLayout());
            panelScanner.setName("");

            int row = 0;
            panelScanner.add(
                    new JLabel(Constant.messages.getString("ascan.options.numHosts.label")),
                    LayoutHelper.getGBC(0, row++, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL));
            panelScanner.add(
                    getSliderHostPerScan(),
                    LayoutHelper.getGBC(
                            0,
                            row++,
                            3,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));

            panelScanner.add(
                    new JLabel(Constant.messages.getString("ascan.options.numThreads.label")),
                    LayoutHelper.getGBC(
                            0,
                            row,
                            2,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));
            panelScanner.add(
                    getLabelThreadsPerHostValue(),
                    LayoutHelper.getGBC(
                            2,
                            row++,
                            1,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));
            panelScanner.add(
                    getSliderThreadsPerHost(),
                    LayoutHelper.getGBC(
                            0,
                            row++,
                            3,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));

            panelScanner.add(
                    new JLabel(Constant.messages.getString("ascan.options.maxRes.label")),
                    LayoutHelper.getGBC(
                            0,
                            row,
                            1,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));
            panelScanner.add(
                    this.getSpinnerMaxResultsList(),
                    LayoutHelper.getGBC(
                            1,
                            row++,
                            2,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));

            panelScanner.add(
                    new JLabel(Constant.messages.getString("ascan.options.maxRule.label")),
                    LayoutHelper.getGBC(
                            0,
                            row,
                            1,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));
            panelScanner.add(
                    this.getSpinnerMaxRuleDuration(),
                    LayoutHelper.getGBC(
                            1,
                            row++,
                            2,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));

            panelScanner.add(
                    new JLabel(Constant.messages.getString("ascan.options.maxScan.label")),
                    LayoutHelper.getGBC(
                            0,
                            row,
                            1,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));
            panelScanner.add(
                    this.getSpinnerMaxScanDuration(),
                    LayoutHelper.getGBC(
                            1,
                            row++,
                            2,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));

            panelScanner.add(
                    new JLabel(Constant.messages.getString("ascan.options.delayInMs.label")),
                    LayoutHelper.getGBC(
                            0,
                            row,
                            1,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));
            panelScanner.add(
                    getSpinnerDelayInMs(),
                    LayoutHelper.getGBC(
                            1,
                            row++,
                            2,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));

            // Add checkboxes for Active scan configuration
            // ---------------------------------------------
            panelScanner.add(
                    getChkInjectPluginIdInHeader(),
                    LayoutHelper.getGBC(
                            0,
                            row++,
                            3,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(16, 2, 2, 2)));
            panelScanner.add(
                    getChkHandleAntiCSRFTokens(),
                    LayoutHelper.getGBC(
                            0,
                            row++,
                            3,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(16, 2, 2, 2)));

            panelScanner.add(
                    this.getChkPromptInAttackMode(),
                    LayoutHelper.getGBC(
                            0,
                            row++,
                            3,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(16, 2, 2, 2)));
            panelScanner.add(
                    this.getChkRescanInAttackMode(),
                    LayoutHelper.getGBC(
                            0,
                            row++,
                            3,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(16, 2, 2, 2)));

            // Add Attack settings section
            // ---------------------------------------------
            panelScanner.add(
                    new JLabel(Constant.messages.getString("ascan.options.policy.ascan.label")),
                    LayoutHelper.getGBC(
                            0,
                            row,
                            1,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(16, 2, 2, 2)));
            panelScanner.add(
                    getDefaultAscanPolicyPulldown(),
                    LayoutHelper.getGBC(
                            1,
                            row++,
                            2,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(16, 2, 2, 2)));

            panelScanner.add(
                    new JLabel(Constant.messages.getString("ascan.options.policy.attack.label")),
                    LayoutHelper.getGBC(
                            0,
                            row,
                            1,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));
            panelScanner.add(
                    getDefaultAttackPolicyPulldown(),
                    LayoutHelper.getGBC(
                            1,
                            row++,
                            2,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));
            panelScanner.add(
                    this.getAllowAttackModeOnStart(),
                    LayoutHelper.getGBC(
                            0,
                            row++,
                            3,
                            1.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(16, 2, 2, 2)));

            // Chart
            panelScanner.add(
                    new JLabel(Constant.messages.getString("ascan.options.maxChart.label")),
                    LayoutHelper.getGBC(
                            0,
                            row,
                            1,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));
            panelScanner.add(
                    this.getSpinnerMaxChartTime(),
                    LayoutHelper.getGBC(
                            1,
                            row++,
                            2,
                            0.0D,
                            0,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 2, 2, 2)));

            // Close Panel
            panelScanner.add(
                    new JLabel(),
                    LayoutHelper.getGBC(0, row, 3, 1.0D, 1.0D, GridBagConstraints.BOTH));
        }

        return panelScanner;
    }

    private JComboBox<String> getDefaultAscanPolicyPulldown() {
        if (defaultAscanPolicy == null) {
            defaultAscanPolicy = new JComboBox<>();
        }
        return defaultAscanPolicy;
    }

    private JComboBox<String> getDefaultAttackPolicyPulldown() {
        if (defaultAttackPolicy == null) {
            defaultAttackPolicy = new JComboBox<>();
        }
        return defaultAttackPolicy;
    }

    private void initPolicyPulldowns() {

        this.getDefaultAscanPolicyPulldown().removeAllItems();
        this.getDefaultAttackPolicyPulldown().removeAllItems();

        for (String policy : extension.getPolicyManager().getAllPolicyNames()) {
            this.getDefaultAscanPolicyPulldown().addItem(policy);
            this.getDefaultAttackPolicyPulldown().addItem(policy);
        }
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam options = (OptionsParam) obj;
        ScannerParam param = options.getParamSet(ScannerParam.class);
        getSliderHostPerScan().setValue(param.getHostPerScan());
        getSliderThreadsPerHost().setValue(param.getThreadPerHost());
        getSpinnerDelayInMs().setValue(param.getDelayInMs());
        getSpinnerMaxResultsList().setValue(param.getMaxResultsToList());
        getSpinnerMaxRuleDuration().setValue(param.getMaxRuleDurationInMins());
        getSpinnerMaxScanDuration().setValue(param.getMaxScanDurationInMins());
        getChkInjectPluginIdInHeader().setSelected(param.isInjectPluginIdInHeader());
        getChkHandleAntiCSRFTokens().setSelected(param.getHandleAntiCSRFTokens());
        getChkPromptInAttackMode().setSelected(param.isPromptInAttackMode());
        getChkRescanInAttackMode().setSelected(param.isRescanInAttackMode());
        getChkRescanInAttackMode().setEnabled(!getChkPromptInAttackMode().isSelected());

        initPolicyPulldowns();
        getDefaultAscanPolicyPulldown().setSelectedItem(param.getDefaultPolicy());
        getDefaultAttackPolicyPulldown().setSelectedItem(param.getAttackPolicy());
        getAllowAttackModeOnStart().setSelected(param.isAllowAttackOnStart());
        getSpinnerMaxChartTime().setValue(param.getMaxChartTimeInMins());
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam options = (OptionsParam) obj;
        ScannerParam param = options.getParamSet(ScannerParam.class);
        param.setHostPerScan(getSliderHostPerScan().getValue());
        param.setThreadPerHost(getSliderThreadsPerHost().getValue());
        param.setDelayInMs(getDelayInMs());
        param.setMaxResultsToList(this.getSpinnerMaxResultsList().getValue());
        param.setMaxRuleDurationInMins(this.getSpinnerMaxRuleDuration().getValue());
        param.setMaxScanDurationInMins(this.getSpinnerMaxScanDuration().getValue());
        param.setInjectPluginIdInHeader(getChkInjectPluginIdInHeader().isSelected());
        param.setHandleAntiCSRFTokens(getChkHandleAntiCSRFTokens().isSelected());
        param.setPromptInAttackMode(getChkPromptInAttackMode().isSelected());
        param.setRescanInAttackMode(getChkRescanInAttackMode().isSelected());
        param.setDefaultPolicy((String) this.getDefaultAscanPolicyPulldown().getSelectedItem());
        param.setAttackPolicy((String) this.getDefaultAttackPolicyPulldown().getSelectedItem());
        param.setAllowAttackOnStart(this.getAllowAttackModeOnStart().isSelected());
        param.setMaxChartTimeInMins(this.getSpinnerMaxChartTime().getValue());
    }

    /**
     * This method initializes sliderHostPerScan
     *
     * @return javax.swing.JSlider
     */
    private JSlider getSliderHostPerScan() {
        if (sliderHostPerScan == null) {
            sliderHostPerScan = new JSlider();
            sliderHostPerScan.setMaximum(5);
            sliderHostPerScan.setMinimum(1);
            sliderHostPerScan.setMinorTickSpacing(1);
            sliderHostPerScan.setPaintTicks(true);
            sliderHostPerScan.setPaintLabels(true);
            sliderHostPerScan.setName("");
            sliderHostPerScan.setMajorTickSpacing(1);
            sliderHostPerScan.setSnapToTicks(true);
            sliderHostPerScan.setPaintTrack(true);
        }
        return sliderHostPerScan;
    }

    /**
     * This method initializes sliderThreadsPerHost
     *
     * @return javax.swing.JSlider
     */
    private JSlider getSliderThreadsPerHost() {
        if (sliderThreadsPerHost == null) {
            sliderThreadsPerHost = new PositiveValuesSlider(Constant.MAX_THREADS_PER_SCAN);

            sliderThreadsPerHost.addChangeListener(
                    new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            setLabelThreadsPerHostValue(getSliderThreadsPerHost().getValue());
                        }
                    });
        }
        return sliderThreadsPerHost;
    }

    private ZapNumberSpinner getSpinnerDelayInMs() {
        if (spinnerDelayInMs == null) {
            spinnerDelayInMs = new ZapNumberSpinner(0, 0, Integer.MAX_VALUE);
        }
        return spinnerDelayInMs;
    }

    private int getDelayInMs() {
        return this.spinnerDelayInMs.getValue();
    }

    private void setLabelThreadsPerHostValue(int value) {
        if (labelThreadsPerHostValue == null) {
            labelThreadsPerHostValue = new JLabel();
        }
        labelThreadsPerHostValue.setText(String.valueOf(value));
    }

    private JLabel getLabelThreadsPerHostValue() {
        if (labelThreadsPerHostValue == null) {
            setLabelThreadsPerHostValue(getSliderThreadsPerHost().getValue());
        }
        return labelThreadsPerHostValue;
    }

    private ZapNumberSpinner getSpinnerMaxResultsList() {
        if (spinnerMaxResultsList == null) {
            spinnerMaxResultsList = new ZapNumberSpinner();
            spinnerMaxResultsList.setToolTipText(
                    Constant.messages.getString("ascan.options.maxRes.tooltip"));
        }
        return spinnerMaxResultsList;
    }

    private ZapNumberSpinner getSpinnerMaxRuleDuration() {
        if (spinnerMaxRuleDuration == null) {
            spinnerMaxRuleDuration = new ZapNumberSpinner();
        }
        return spinnerMaxRuleDuration;
    }

    private ZapNumberSpinner getSpinnerMaxScanDuration() {
        if (spinnerMaxScanDuration == null) {
            spinnerMaxScanDuration = new ZapNumberSpinner();
        }
        return spinnerMaxScanDuration;
    }

    private ZapNumberSpinner getSpinnerMaxChartTime() {
        if (spinnerMaxChartTime == null) {
            spinnerMaxChartTime = new ZapNumberSpinner();
            spinnerMaxChartTime.setToolTipText(
                    Constant.messages.getString("ascan.options.maxChart.tooltip"));
        }
        return spinnerMaxChartTime;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.ascan";
    }

    private JCheckBox getChkInjectPluginIdInHeader() {
        if (chkInjectPluginIdInHeader == null) {
            chkInjectPluginIdInHeader = new JCheckBox();
            chkInjectPluginIdInHeader.setText(
                    Constant.messages.getString("ascan.options.pluginHeader.label"));
        }
        return chkInjectPluginIdInHeader;
    }

    private JCheckBox getChkHandleAntiCSRFTokens() {
        if (chkHandleAntiCsrfTokens == null) {
            chkHandleAntiCsrfTokens = new JCheckBox();
            chkHandleAntiCsrfTokens.setText(
                    Constant.messages.getString("ascan.options.anticsrf.label"));
        }
        return chkHandleAntiCsrfTokens;
    }

    private JCheckBox getChkPromptInAttackMode() {
        if (chkPromptInAttackMode == null) {
            chkPromptInAttackMode =
                    new JCheckBox(Constant.messages.getString("ascan.options.attackPrompt.label"));
            chkPromptInAttackMode.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            getChkRescanInAttackMode()
                                    .setEnabled(!chkPromptInAttackMode.isSelected());
                        }
                    });
        }
        return chkPromptInAttackMode;
    }

    private JCheckBox getChkRescanInAttackMode() {
        if (chkRescanInAttackMode == null) {
            chkRescanInAttackMode =
                    new JCheckBox(Constant.messages.getString("ascan.options.attackRescan.label"));
        }
        return chkRescanInAttackMode;
    }

    private JCheckBox getAllowAttackModeOnStart() {
        if (allowAttackModeOnStart == null) {
            allowAttackModeOnStart =
                    new JCheckBox(Constant.messages.getString("ascan.options.attackOnStart.label"));
        }
        return allowAttackModeOnStart;
    }
}

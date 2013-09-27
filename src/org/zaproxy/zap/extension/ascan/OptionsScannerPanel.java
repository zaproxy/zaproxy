/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 The ZAP development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
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
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.view.LayoutHelper;

public class OptionsScannerPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private JPanel panelScanner = null;
    private JSlider sliderHostPerScan = null;
    private JSlider sliderThreadsPerHost = null;
    private JSlider sliderDelayInMs = null;
    private JLabel labelThreadsPerHostValue = null;
    private JLabel labelDelayInMsValue = null;
    private ZapNumberSpinner spinnerMaxResultsList = null;
    private JCheckBox chkHandleAntiCrsfTokens = null;
    private JCheckBox chkDeleteRequestsOnShutdown = null;
    private JComboBox<String> comboThreshold = null;
    private JLabel labelThresholdNotes = null;
    private JComboBox<String> comboStrength = null;
    private JLabel labelStrengthNotes = null;
    
    // Checkbox for Target definitions
    private JCheckBox chkInjectableQueryString = null;
    private JCheckBox chkInjectablePostData = null;
    private JCheckBox chkInjectableHeaders = null;
    private JCheckBox chkInjectableCookie = null;
    
    // Checkbox for RPC to be enabled definitions
    private JCheckBox chkRPCMultipart = null;
    private JCheckBox chkRPCXML = null;
    private JCheckBox chkRPCJSON = null;
    private JCheckBox chkRPCGWT = null;
    private JCheckBox chkRPCoData = null;

    /**
     * General Constructor
     */
    public OptionsScannerPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes the Panel
     */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("ascan.options.title"));
        this.setSize(314, 245);
        this.add(getPanelScanner(), getPanelScanner().getName());
    }

    /**
     * 
     * @return 
     */
    private JPanel getPanelScanner() {
        if (panelScanner == null) {
            panelScanner = new JPanel();
            panelScanner.setLayout(new GridBagLayout());
            panelScanner.setSize(114, 132);
            panelScanner.setName("");

            panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.numHosts.label")),
                    LayoutHelper.getGBC(0, 0, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL));
            panelScanner.add(getSliderHostPerScan(),
                    LayoutHelper.getGBC(0, 1, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            
            panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.numThreads.label")),
                    LayoutHelper.getGBC(0, 2, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getLabelThreadsPerHostValue(),
                    LayoutHelper.getGBC(2, 2, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getSliderThreadsPerHost(),
                    LayoutHelper.getGBC(0, 3, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            
            panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.maxRes.label")),
                    LayoutHelper.getGBC(0, 4, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(this.getSpinnerMaxResultsList(),
                    LayoutHelper.getGBC(1, 4, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

            panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.delayInMs.label")),
                    LayoutHelper.getGBC(0, 5, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getLabelDelayInMsValue(),
                    LayoutHelper.getGBC(2, 5, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getSliderDelayInMs(),
                    LayoutHelper.getGBC(0, 6, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getChkHandleAntiCSRFTokens(),
                    LayoutHelper.getGBC(0, 7, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getChkDeleteRequestsOnShutdown(),
                    LayoutHelper.getGBC(0, 8, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));


            // Add Attack settings section
            // ---------------------------------------------
            panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.level.label")),
                    LayoutHelper.getGBC(0, 9, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getComboThreshold(),
                    LayoutHelper.getGBC(1, 9, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getThresholdNotes(),
                    LayoutHelper.getGBC(2, 9, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

            panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.strength.label")),
                    LayoutHelper.getGBC(0, 10, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getComboStrength(),
                    LayoutHelper.getGBC(1, 10, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getStrengthNotes(),
                    LayoutHelper.getGBC(2, 10, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

            // Add Injectable Params Section
            // ---------------------------------------------
            JPanel panelInjectable = new JPanel();
            panelInjectable.setLayout(new GridBagLayout());
            
            panelInjectable.add(
                    new JLabel(Constant.messages.getString("ascan.options.injectable.label")),
                    LayoutHelper.getGBC(0, 0, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

            panelInjectable.add(
                    this.getChkInjectableQueryString(), 
                    LayoutHelper.getGBC(0, 1, 1, 0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));            
            panelInjectable.add(
                    this.getChkInjectablePostData(),
                    LayoutHelper.getGBC(0, 2, 1, 0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelInjectable.add(
                    this.getChkInjectableHeaders(),
                    LayoutHelper.getGBC(0, 3, 1, 0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelInjectable.add(
                    this.getChkInjectableCookie(),
                    LayoutHelper.getGBC(0, 4, 1, 0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            
            panelScanner.add(
                    panelInjectable,
                    LayoutHelper.getGBC(0, 11, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            
            // Add RPC Enabling Section
            // ---------------------------------------------
            JPanel panelRPC = new JPanel();
            panelRPC.setLayout(new GridBagLayout());
            panelRPC.add(
                    new JLabel(Constant.messages.getString("ascan.options.rpc.label")),
                    LayoutHelper.getGBC(0, 0, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

            panelRPC.add(
                    this.getChkRPCMultipart(), 
                    LayoutHelper.getGBC(0, 1, 1, 0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));            
            panelRPC.add(
                    this.getChkRPCXML(), 
                    LayoutHelper.getGBC(0, 2, 1, 0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));            
            panelRPC.add(
                    this.getChkRPCJSON(), 
                    LayoutHelper.getGBC(0, 3, 1, 0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));            
            panelRPC.add(
                    this.getChkRPCGWT(), 
                    LayoutHelper.getGBC(1, 1, 1, 0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));            
            panelRPC.add(
                    this.getChkRPCoData(), 
                    LayoutHelper.getGBC(1, 2, 1, 0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));            
            
            panelScanner.add(
                    panelRPC,
                    LayoutHelper.getGBC(2, 11, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

            // Close Panel
            panelScanner.add(
                    new JLabel(), 
                    LayoutHelper.getGBC(0, 30, 3, 1.0D, 1.0D, GridBagConstraints.BOTH));
        }
        
        return panelScanner;
    }

    private JLabel getThresholdNotes() {
        if (labelThresholdNotes == null) {
            labelThresholdNotes = new JLabel();
        }
        return labelThresholdNotes;
    }

    private JComboBox<String> getComboThreshold() {
        if (comboThreshold == null) {
            comboThreshold = new JComboBox<>();
            comboThreshold.addItem(Constant.messages.getString("ascan.options.level.low"));
            comboThreshold.addItem(Constant.messages.getString("ascan.options.level.medium"));
            comboThreshold.addItem(Constant.messages.getString("ascan.options.level.high"));
            comboThreshold.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Set the explanation
                    if (comboThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.low"))) {
                        getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.low.label"));
                    
                    } else if (comboThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.medium"))) {
                        getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.medium.label"));
                    
                    } else {
                        getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.high.label"));
                    }
                }
            });
        }
        
        return comboThreshold;
    }

    private JLabel getStrengthNotes() {
        if (labelStrengthNotes == null) {
            labelStrengthNotes = new JLabel();
        }
        
        return labelStrengthNotes;
    }

    private JComboBox<String> getComboStrength() {
        if (comboStrength == null) {
            comboStrength = new JComboBox<>();
            comboStrength.addItem(Constant.messages.getString("ascan.options.strength.low"));
            comboStrength.addItem(Constant.messages.getString("ascan.options.strength.medium"));
            comboStrength.addItem(Constant.messages.getString("ascan.options.strength.high"));
            comboStrength.addItem(Constant.messages.getString("ascan.options.strength.insane"));
            comboStrength.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Set the explanation
                    if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.low"))) {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.low.label"));
        
                    } else if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.medium"))) {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.medium.label"));
                    
                    } else if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.high"))) {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.high.label"));
                    
                    } else {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.insane.label"));
                    }
                }
            });
        }
        return comboStrength;
    }

    /**
     * 
     * @param obj 
     */
    @Override
    public void initParam(Object obj) {
        OptionsParam options = (OptionsParam) obj;
        ScannerParam param = (ScannerParam) options.getParamSet(ScannerParam.class);
        getSliderHostPerScan().setValue(param.getHostPerScan());
        getSliderThreadsPerHost().setValue(param.getThreadPerHost());
        getSliderDelayInMs().setValue(param.getDelayInMs());
        setLabelDelayInMsValue(param.getDelayInMs());
        getSpinnerMaxResultsList().setValue(param.getMaxResultsToList());
        getChkHandleAntiCSRFTokens().setSelected(param.getHandleAntiCSRFTokens());
        getChkDeleteRequestsOnShutdown().setSelected(param.isDeleteRequestsOnShutdown());
        
        switch (param.getAlertThreshold()) {
            case LOW:
                getComboThreshold().setSelectedItem(Constant.messages.getString("ascan.options.level.low"));
                getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.low.label"));
                break;
                
            case HIGH:
                getComboThreshold().setSelectedItem(Constant.messages.getString("ascan.options.level.high"));
                getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.high.label"));
                break;
                
            case MEDIUM:
            default:
                getComboThreshold().setSelectedItem(Constant.messages.getString("ascan.options.level.medium"));
                getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.medium.label"));
                break;
        }
        
        switch (param.getAttackStrength()) {
            case LOW:
                getComboStrength().setSelectedItem(Constant.messages.getString("ascan.options.strength.low"));
                getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.low.label"));
                break;
                
            case HIGH:
                getComboStrength().setSelectedItem(Constant.messages.getString("ascan.options.strength.high"));
                getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.high.label"));
                break;
                
            case INSANE:
                getComboStrength().setSelectedItem(Constant.messages.getString("ascan.options.strength.insane"));
                getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.insane.label"));
                break;
                
            case MEDIUM:
            default:
                getComboStrength().setSelectedItem(Constant.messages.getString("ascan.options.strength.medium"));
                getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.medium.label"));
                break;
        }
        
        // Set targets and RPC selections
        int targets = param.getTargetParamsInjectable();
        this.getChkInjectableQueryString().setSelected((targets & ScannerParam.TARGET_QUERYSTRING) != 0);
        this.getChkInjectablePostData().setSelected((targets & ScannerParam.TARGET_POSTDATA) != 0);
        this.getChkInjectableHeaders().setSelected((targets & ScannerParam.TARGET_HTTPHEADERS) != 0);
        this.getChkInjectableCookie().setSelected((targets & ScannerParam.TARGET_COOKIE) != 0);

        int rpcEnabled = param.getTargetParamsEnabledRPC();
        this.getChkRPCMultipart().setSelected((rpcEnabled & ScannerParam.RPC_MULTIPART) != 0);
        this.getChkRPCXML().setSelected((rpcEnabled & ScannerParam.RPC_XML) != 0);
        this.getChkRPCJSON().setSelected((rpcEnabled & ScannerParam.RPC_JSON) != 0);
        this.getChkRPCGWT().setSelected((rpcEnabled & ScannerParam.RPC_GWT) != 0);
        this.getChkRPCoData().setSelected((rpcEnabled & ScannerParam.RPC_ODATA) != 0);
    }

    /**
     * 
     * @param obj 
     */
    @Override
    public void validateParam(Object obj) {
        // no validation needed
    }

    /**
     * 
     * @param obj
     * @throws Exception 
     */
    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam options = (OptionsParam) obj;
        ScannerParam param = (ScannerParam) options.getParamSet(ScannerParam.class);
        param.setHostPerScan(getSliderHostPerScan().getValue());
        param.setThreadPerHost(getSliderThreadsPerHost().getValue());
        param.setDelayInMs(getDelayInMs());
        param.setMaxResultsToList(this.getSpinnerMaxResultsList().getValue());
        param.setHandleAntiCSRFTokens(getChkHandleAntiCSRFTokens().isSelected());
        param.setDeleteRequestsOnShutdown(this.getChkDeleteRequestsOnShutdown().isSelected());

        // Set the Attack Threshold Configuration Section
        Plugin.AlertThreshold threshold;    
        if (comboThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.low"))) {
            threshold = AlertThreshold.LOW;
        
        } else if (comboThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.medium"))) {
            threshold = AlertThreshold.MEDIUM;
        
        } else {
            threshold = AlertThreshold.HIGH;
        }
        
        param.setAlertThreshold(threshold);
        
        // Set the Attack Strenght Configuration Section
        Plugin.AttackStrength strength;        
        if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.low"))) {
            strength = AttackStrength.LOW;
        
        } else if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.medium"))) {
            strength = AttackStrength.MEDIUM;
        
        } else if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.high"))) {
            strength = AttackStrength.HIGH;
        
        } else {
            strength = AttackStrength.INSANE;
        }
        
        param.setAttackStrength(strength);
        
        // Set Injectable Targets
        int targets = 0;        
        if (this.getChkInjectableQueryString().isSelected()) {
            targets |= ScannerParam.TARGET_QUERYSTRING;
        }
        
        if (this.getChkInjectablePostData().isSelected()) {
            targets |= ScannerParam.TARGET_POSTDATA;
        }
        
        if (this.getChkInjectableHeaders().isSelected()) {
            targets |= ScannerParam.TARGET_HTTPHEADERS;
        }
        
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
        
        if (this.getChkRPCGWT().isSelected()) {
            enabledRpc |= ScannerParam.RPC_GWT;
        }

        if (this.getChkRPCoData().isSelected()) {
            enabledRpc |= ScannerParam.RPC_ODATA;
        }
        
        param.setTargetParamsEnabledRPC(enabledRpc);
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
            sliderThreadsPerHost = new JSlider();
            sliderThreadsPerHost.setMaximum(Constant.MAX_THREADS_PER_SCAN);
            sliderThreadsPerHost.setMinimum(0);
            sliderThreadsPerHost.setValue(1);
            sliderThreadsPerHost.setPaintTicks(true);
            sliderThreadsPerHost.setPaintLabels(true);
            sliderThreadsPerHost.setMinorTickSpacing(1);
            sliderThreadsPerHost.setMajorTickSpacing(5);
            sliderThreadsPerHost.setSnapToTicks(true);
            sliderThreadsPerHost.setPaintTrack(true);

            sliderThreadsPerHost.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    // If the minimum is set to 1 then the ticks are at 6, 11 etc
                    // But we dont want to support 0 threads, hence this hack
                    if (getSliderThreadsPerHost().getValue() == 0) {
                        getSliderThreadsPerHost().setValue(1);
                    }
                    setLabelThreadsPerHostValue(getSliderThreadsPerHost().getValue());
                }
            });
        }
        return sliderThreadsPerHost;
    }

    private JSlider getSliderDelayInMs() {
        if (sliderDelayInMs == null) {
            sliderDelayInMs = new JSlider();
            sliderDelayInMs.setMaximum(1000);
            sliderDelayInMs.setMinimum(0);
            sliderDelayInMs.setValue(0);
            sliderDelayInMs.setPaintTicks(true);
            sliderDelayInMs.setPaintLabels(true);
            sliderDelayInMs.setMinorTickSpacing(25);
            sliderDelayInMs.setMajorTickSpacing(100);
            sliderDelayInMs.setSnapToTicks(true);
            sliderDelayInMs.setPaintTrack(true);

            sliderDelayInMs.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    setLabelDelayInMsValue(getSliderDelayInMs().getValue());
                }
            });

        }
        return sliderDelayInMs;
    }

    /**
     * 
     * @return 
     */
    public int getDelayInMs() {
        return this.sliderDelayInMs.getValue();
    }

    /**
     * 
     * @param value 
     */
    public void setLabelDelayInMsValue(int value) {
        if (labelDelayInMsValue == null) {
            labelDelayInMsValue = new JLabel();
        }

        // Snap to ticks
        value = ((value + 13) / 25) * 25;
        
        String val;
        if (value < 10) {
            val = "   " + value;
            
        } else if (value < 100) {
            val = "  " + value;
            
        } else if (value < 1000) {
            val = " " + value;
            
        } else {
            val = "" + value;
        }
        
        labelDelayInMsValue.setText(val);
    }

    /**
     * 
     * @return 
     */
    public JLabel getLabelDelayInMsValue() {
        if (labelDelayInMsValue == null) {
            setLabelDelayInMsValue(getSliderDelayInMs().getValue());
        }
        return labelDelayInMsValue;
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
            spinnerMaxResultsList.setToolTipText(Constant.messages.getString("ascan.options.maxRes.tooltip"));
        }
        return spinnerMaxResultsList;
    }

    /**
     * 
     * @return 
     */
    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.ascan";
    }

    private JCheckBox getChkHandleAntiCSRFTokens() {
        if (chkHandleAntiCrsfTokens == null) {
            chkHandleAntiCrsfTokens = new JCheckBox();
            chkHandleAntiCrsfTokens.setText(Constant.messages.getString("ascan.options.anticsrf.label"));
        }
        return chkHandleAntiCrsfTokens;
    }

    private JCheckBox getChkDeleteRequestsOnShutdown() {
        if (chkDeleteRequestsOnShutdown == null) {
            chkDeleteRequestsOnShutdown = new JCheckBox();
            chkDeleteRequestsOnShutdown.setText(Constant.messages.getString("ascan.options.deleterecs.label"));
        }
        return chkDeleteRequestsOnShutdown;
    }

    private JCheckBox getChkInjectableQueryString() {
        if (chkInjectableQueryString == null) {
            chkInjectableQueryString = new JCheckBox();
            chkInjectableQueryString.setText(Constant.messages.getString("ascan.options.injectable.querystring.label"));
        }
        return chkInjectableQueryString;
    }

    private JCheckBox getChkInjectablePostData() {
        if (chkInjectablePostData == null) {
            chkInjectablePostData = new JCheckBox();
            chkInjectablePostData.setText(Constant.messages.getString("ascan.options.injectable.postdata.label"));
        }
        return chkInjectablePostData;
    }

    private JCheckBox getChkInjectableHeaders() {
        if (chkInjectableHeaders == null) {
            chkInjectableHeaders = new JCheckBox();
            chkInjectableHeaders.setText(Constant.messages.getString("ascan.options.injectable.headers.label"));
        }
        return chkInjectableHeaders;
    }

    private JCheckBox getChkInjectableCookie() {
        if (chkInjectableCookie== null) {
            chkInjectableCookie = new JCheckBox();
            chkInjectableCookie.setText(Constant.messages.getString("ascan.options.injectable.cookie.label"));
        }
        return chkInjectableCookie;
    }

    private JCheckBox getChkRPCMultipart() {
        if (chkRPCMultipart == null) {
            chkRPCMultipart = new JCheckBox();
            chkRPCMultipart.setText(Constant.messages.getString("ascan.options.rpc.multipart.label"));
        }
        return chkRPCMultipart;
    }

    private JCheckBox getChkRPCXML() {
        if (chkRPCXML == null) {
            chkRPCXML = new JCheckBox();
            chkRPCXML.setText(Constant.messages.getString("ascan.options.rpc.xml.label"));
        }
        return chkRPCXML;
    }

    private JCheckBox getChkRPCJSON() {
        if (chkRPCJSON == null) {
            chkRPCJSON = new JCheckBox();
            chkRPCJSON.setText(Constant.messages.getString("ascan.options.rpc.json.label"));
        }
        return chkRPCJSON;
    }

    private JCheckBox getChkRPCGWT() {
        if (chkRPCGWT == null) {
            chkRPCGWT = new JCheckBox();
            chkRPCGWT.setText(Constant.messages.getString("ascan.options.rpc.gwt.label"));
        }
        return chkRPCGWT;
    }

    private JCheckBox getChkRPCoData() {
        if (chkRPCoData == null) {
            chkRPCoData = new JCheckBox();
            chkRPCoData.setText(Constant.messages.getString("ascan.options.rpc.odata.label"));
        }
        return chkRPCoData;
    }
}

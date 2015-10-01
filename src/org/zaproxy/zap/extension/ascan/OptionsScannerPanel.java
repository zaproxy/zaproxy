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

public class OptionsScannerPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private JPanel panelScanner = null;
    private JSlider sliderHostPerScan = null;
    private JSlider sliderThreadsPerHost = null;
    private JSlider sliderDelayInMs = null;
    private JLabel labelThreadsPerHostValue = null;
    private JLabel labelDelayInMsValue = null;
    private ZapNumberSpinner spinnerMaxResultsList = null;
    private JCheckBox chkInjectPluginIdInHeader = null;
    private JCheckBox chkHandleAntiCrsfTokens = null;
    private JCheckBox chkPromptInAttackMode = null;
    private JCheckBox chkRescanInAttackMode = null;
    private JComboBox<String> defaultAscanPolicy = null;
    private JComboBox<String> defaultAttackPolicy = null;
    private JCheckBox allowAttackModeOnStart = null;
    private ZapNumberSpinner spinnerMaxChartTime = null;
    
    private ExtensionActiveScan extension;
    
    /**
     * General Constructor
     * @param extensionn 
     */
    public OptionsScannerPanel(ExtensionActiveScan extension) {
        super();
        this.extension = extension;
        initialize();
    }

    /**
     * This method initializes the Panel
     */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("ascan.options.title"));
        this.add(new JScrollPane(getPanelScanner()));
    }

    /**
     * 
     * @return 
     */
    private JPanel getPanelScanner() {
        if (panelScanner == null) {
            panelScanner = new JPanel();
            panelScanner.setLayout(new GridBagLayout());
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
                    LayoutHelper.getGBC(1, 4, 2, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

            panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.delayInMs.label")),
                    LayoutHelper.getGBC(0, 5, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getLabelDelayInMsValue(),
                    LayoutHelper.getGBC(2, 5, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getSliderDelayInMs(),
                    LayoutHelper.getGBC(0, 6, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            
            // Add checkboxes for Active scan configuration
            // ---------------------------------------------            
            panelScanner.add(getChkInjectPluginIdInHeader(),
            		LayoutHelper.getGBC(0, 7, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(16, 2, 2, 2))); 
            panelScanner.add(getChkHandleAntiCSRFTokens(),
                    LayoutHelper.getGBC(0, 8, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(16, 2, 2, 2)));

            panelScanner.add(this.getChkPromptInAttackMode(),
                    LayoutHelper.getGBC(0, 9, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(16, 2, 2, 2)));
            panelScanner.add(this.getChkRescanInAttackMode(),
                    LayoutHelper.getGBC(0, 10, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(16, 2, 2, 2)));


            // Add Attack settings section
            // ---------------------------------------------
            panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.policy.ascan.label")),
                    LayoutHelper.getGBC(0, 11, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(16, 2, 2, 2)));
            panelScanner.add(getDefaultAscanPolicyPulldown(),
                    LayoutHelper.getGBC(1, 11, 2, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(16, 2, 2, 2)));

            panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.policy.attack.label")),
                    LayoutHelper.getGBC(0, 12, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(getDefaultAttackPolicyPulldown(),
                    LayoutHelper.getGBC(1, 12, 2, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(this.getAllowAttackModeOnStart(),
                    LayoutHelper.getGBC(0, 13, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(16, 2, 2, 2)));

            // Chart
            panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.maxChart.label")),
                    LayoutHelper.getGBC(0, 14, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            panelScanner.add(this.getSpinnerMaxChartTime(),
                    LayoutHelper.getGBC(1, 14, 2, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

            // Close Panel
            panelScanner.add(
                    new JLabel(), 
                    LayoutHelper.getGBC(0, 30, 3, 1.0D, 1.0D, GridBagConstraints.BOTH));
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

    /**
     * 
     * @param obj 
     */
    @Override
    public void initParam(Object obj) {
        OptionsParam options = (OptionsParam) obj;
        ScannerParam param = options.getParamSet(ScannerParam.class);
        getSliderHostPerScan().setValue(param.getHostPerScan());
        getSliderThreadsPerHost().setValue(param.getThreadPerHost());
        getSliderDelayInMs().setValue(param.getDelayInMs());
        setLabelDelayInMsValue(param.getDelayInMs());
        getSpinnerMaxResultsList().setValue(param.getMaxResultsToList());
        getChkInjectPluginIdInHeader().setSelected(param.isInjectPluginIdInHeader());
        getChkHandleAntiCSRFTokens().setSelected(param.getHandleAntiCSRFTokens());
        getChkPromptInAttackMode().setSelected(param.isPromptInAttackMode());
        getChkRescanInAttackMode().setSelected(param.isRescanInAttackMode());
		getChkRescanInAttackMode().setEnabled(! getChkPromptInAttackMode().isSelected());

		initPolicyPulldowns();
        getDefaultAscanPolicyPulldown().setSelectedItem(param.getDefaultPolicy());
        getDefaultAttackPolicyPulldown().setSelectedItem(param.getAttackPolicy());
        getAllowAttackModeOnStart().setSelected(param.isAllowAttackOnStart());
        getSpinnerMaxChartTime().setValue(param.getMaxChartTimeInMins());

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
        ScannerParam param = options.getParamSet(ScannerParam.class);
        param.setHostPerScan(getSliderHostPerScan().getValue());
        param.setThreadPerHost(getSliderThreadsPerHost().getValue());
        param.setDelayInMs(getDelayInMs());
        param.setMaxResultsToList(this.getSpinnerMaxResultsList().getValue());
        param.setInjectPluginIdInHeader(getChkInjectPluginIdInHeader().isSelected());
        param.setHandleAntiCSRFTokens(getChkHandleAntiCSRFTokens().isSelected());
        param.setPromptInAttackMode(getChkPromptInAttackMode().isSelected());
        param.setRescanInAttackMode(getChkRescanInAttackMode().isSelected());
        param.setDefaultPolicy((String)this.getDefaultAscanPolicyPulldown().getSelectedItem());
        param.setAttackPolicy((String)this.getDefaultAttackPolicyPulldown().getSelectedItem());
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

            sliderThreadsPerHost.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
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
            val = Integer.toString(value);
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

    private ZapNumberSpinner getSpinnerMaxChartTime() {
        if (spinnerMaxChartTime == null) {
            spinnerMaxChartTime = new ZapNumberSpinner();
            spinnerMaxChartTime.setToolTipText(Constant.messages.getString("ascan.options.maxChart.tooltip"));
        }
        return spinnerMaxChartTime;
    }

    /**
     * 
     * @return 
     */
    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.ascan";
    }
    private JCheckBox getChkInjectPluginIdInHeader() {
    	if (chkInjectPluginIdInHeader == null) {
    		chkInjectPluginIdInHeader = new JCheckBox();
    		chkInjectPluginIdInHeader.setText(Constant.messages.getString("ascan.options.pluginHeader.label"));
    	}
    	return chkInjectPluginIdInHeader;
    }
    
    private JCheckBox getChkHandleAntiCSRFTokens() {
        if (chkHandleAntiCrsfTokens == null) {
            chkHandleAntiCrsfTokens = new JCheckBox();
            chkHandleAntiCrsfTokens.setText(Constant.messages.getString("ascan.options.anticsrf.label"));
        }
        return chkHandleAntiCrsfTokens;
    }
    
    private JCheckBox getChkPromptInAttackMode() {
    	if (chkPromptInAttackMode == null) {
    		chkPromptInAttackMode = new JCheckBox(Constant.messages.getString("ascan.options.attackPrompt.label"));
    		chkPromptInAttackMode.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					getChkRescanInAttackMode().setEnabled(! chkPromptInAttackMode.isSelected());
				}});
    	}
    	return chkPromptInAttackMode;
    }

    private JCheckBox getChkRescanInAttackMode() {
    	if (chkRescanInAttackMode == null) {
    		chkRescanInAttackMode = new JCheckBox(Constant.messages.getString("ascan.options.attackRescan.label"));
    	}
    	return chkRescanInAttackMode;
    }

    private JCheckBox getAllowAttackModeOnStart() {
    	if (allowAttackModeOnStart == null) {
    		allowAttackModeOnStart = new JCheckBox(Constant.messages.getString("ascan.options.attackOnStart.label"));
    	}
    	return allowAttackModeOnStart;
    }

}

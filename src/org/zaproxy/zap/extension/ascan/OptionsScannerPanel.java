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
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.view.LayoutHelper;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsScannerPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JPanel panelScanner = null; 
	private JSlider sliderHostPerScan = null;
	private JSlider sliderThreadsPerHost = null;
	private JSlider sliderDelayInMs = null;
	private JLabel labelThreadsPerHostValue = null;
	private JLabel labelDelayInMsValue = null;
	private JCheckBox chkHandleAntiCrsfTokens = null;
	private JComboBox comboLevel = null;
	private JLabel labelLevelNotes = null;
	// Not enabled yet - not sure how helpful they are, and can yet shoe that the cookie scanning finds any issues!
	//private JCheckBox chkTargetParamsUrl = null;
	//private JCheckBox chkTargetParamsForm = null;
	//private JCheckBox chkTargetParamsCookie = null;

    public OptionsScannerPanel() {
        super();
 		initialize();
   }
    
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("ascan.options.title"));
        this.setSize(314, 245);
        this.add(getPanelScanner(), getPanelScanner().getName());
	}

	private JPanel getPanelScanner() {
		if (panelScanner == null) {
			panelScanner = new JPanel();
			panelScanner.setLayout(new GridBagLayout());
			panelScanner.setSize(114, 132);
			panelScanner.setName("");
			
			panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.numHosts.label")), 
					LayoutHelper.getGBC(0, 0, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL));
			panelScanner.add(getSliderHostPerScan(), 
					LayoutHelper.getGBC(0, 1, 3,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.numThreads.label")), 
					LayoutHelper.getGBC(0, 2, 2,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelScanner.add(getLabelThreadsPerHostValue(), 
					LayoutHelper.getGBC(2, 2, 1,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelScanner.add(getSliderThreadsPerHost(), 
					LayoutHelper.getGBC(0, 3, 3,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			
			panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.delayInMs.label")), 
					LayoutHelper.getGBC(0, 4, 2,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelScanner.add(getLabelDelayInMsValue(), 
					LayoutHelper.getGBC(2, 4, 1,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelScanner.add(getSliderDelayInMs(), 
					LayoutHelper.getGBC(0, 5, 3,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelScanner.add(getChkHandleAntiCSRFTokens(), 
					LayoutHelper.getGBC(0, 6, 3,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));

			panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.level.label")), 
					LayoutHelper.getGBC(0, 7, 1,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelScanner.add(getComboLevel(), 
					LayoutHelper.getGBC(1, 7, 1,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelScanner.add(getLabelLevelNotes(), 
					LayoutHelper.getGBC(2, 7, 1,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			
			/*
			panelScanner.add(new JLabel(Constant.messages.getString("ascan.options.params.label")), 
					LayoutHelper.getGBC(0, 9, 1,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelScanner.add(getChkTargetParamsUrl(), LayoutHelper.getGBC(1, 8, 2,  1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelScanner.add(getChkTargetParamsForm(), LayoutHelper.getGBC(1, 9, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelScanner.add(getChkTargetParamsCookie(), LayoutHelper.getGBC(1, 10, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			*/
			
			panelScanner.add(new JLabel(), LayoutHelper.getGBC(0, 20, 2, 1.0D, 1.0D, GridBagConstraints.BOTH));

		}
		return panelScanner;
	}
	
	private JLabel getLabelLevelNotes() {
		if (labelLevelNotes == null) {
			labelLevelNotes = new JLabel();
		}
		return labelLevelNotes;
	}


	private JComboBox getComboLevel() {
		if (comboLevel == null) {
			comboLevel = new JComboBox();
			comboLevel.addItem(Constant.messages.getString("ascan.options.level.low"));
			comboLevel.addItem(Constant.messages.getString("ascan.options.level.medium"));
			comboLevel.addItem(Constant.messages.getString("ascan.options.level.high"));
			comboLevel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Set the explanation
				    if (comboLevel.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.low"))) {
				    	getLabelLevelNotes().setText(Constant.messages.getString("ascan.options.level.low.label"));
				    } else if (comboLevel.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.medium"))) {
				    	getLabelLevelNotes().setText(Constant.messages.getString("ascan.options.level.medium.label"));
				    } else {
				    	getLabelLevelNotes().setText(Constant.messages.getString("ascan.options.level.high.label"));
				    }
				}});
		}
		return comboLevel;
	}


	@Override
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    ScannerParam param = (ScannerParam) options.getParamSet(ScannerParam.class);
	    getSliderHostPerScan().setValue(param.getHostPerScan());
	    getSliderThreadsPerHost().setValue(param.getThreadPerHost());
	    getSliderDelayInMs().setValue(param.getDelayInMs());
	    setLabelDelayInMsValue(param.getDelayInMs());
	    getChkHandleAntiCSRFTokens().setSelected(param.getHandleAntiCSRFTokens());
	    switch (param.getLevel()) {
	    case LOW: 
	    	getComboLevel().setSelectedItem(Constant.messages.getString("ascan.options.level.low"));
	    	getLabelLevelNotes().setText(Constant.messages.getString("ascan.options.level.low.label"));
	    	break;
	    case MEDIUM: 
	    	getComboLevel().setSelectedItem(Constant.messages.getString("ascan.options.level.medium")); 
	    	getLabelLevelNotes().setText(Constant.messages.getString("ascan.options.level.medium.label"));
	    	break;
	    case HIGH: 
	    	getComboLevel().setSelectedItem(Constant.messages.getString("ascan.options.level.high")); 
	    	getLabelLevelNotes().setText(Constant.messages.getString("ascan.options.level.high.label"));
	    	break;
	    }
	    /*
	    this.getChkTargetParamsUrl().setSelected(param.isTargetParamsUrl());
	    this.getChkTargetParamsForm().setSelected(param.isTargetParamsForm());
	    this.getChkTargetParamsCookie().setSelected(param.isTargetParamsCookie());
	    */
	}
	
	@Override
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	@Override
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    ScannerParam param = (ScannerParam) options.getParamSet(ScannerParam.class);
	    param.setHostPerScan(getSliderHostPerScan().getValue());
	    param.setThreadPerHost(getSliderThreadsPerHost().getValue());
	    param.setDelayInMs(getDelayInMs());
	    param.setHandleAntiCSRFTokens(getChkHandleAntiCSRFTokens().isSelected());
	    
	    Plugin.AlertThreshold level = null;
	    if (comboLevel.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.low"))) {
	    	level = AlertThreshold.LOW;
	    } else if (comboLevel.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.medium"))) {
	    	level = AlertThreshold.MEDIUM;
	    } else {
	    	level = AlertThreshold.HIGH;
	    }
	    param.setAlertThreshold(level);
	    // Not enabled yet
	    //param.setTargetParamsUrl(getChkTargetParamsUrl().isSelected());
	    //param.setTargetParamsForm(getChkTargetParamsForm().isSelected());
	    //param.setTargetParamsCookie(getChkTargetParamsCookie().isSelected());
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

			sliderThreadsPerHost.addChangeListener(new ChangeListener () {
				@Override
				public void stateChanged(ChangeEvent e) {
					// If the minimum is set to 1 then the ticks are at 6, 11 etc
					// But we dont want to support 0 threads, hence this hack
					if (getSliderThreadsPerHost().getValue() == 0) {
						getSliderThreadsPerHost().setValue(1);
					}
					setLabelThreadsPerHostValue(getSliderThreadsPerHost().getValue());
				}});
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
			
			sliderDelayInMs.addChangeListener(new ChangeListener () {

				@Override
				public void stateChanged(ChangeEvent e) {
					setLabelDelayInMsValue(getSliderDelayInMs().getValue());
				}});

		}
		return sliderDelayInMs;
	}

    public int getDelayInMs() {
    	return this.sliderDelayInMs.getValue();
    }

	public void setLabelDelayInMsValue(int value) {
		if (labelDelayInMsValue == null) {
			labelDelayInMsValue = new JLabel();
		}
		
		// Snap to ticks
		value = ((value + 13) / 25) * 25;
		String val = null;
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
		labelThreadsPerHostValue.setText(""+value);
	}

	private JLabel getLabelThreadsPerHostValue() {
		if (labelThreadsPerHostValue == null) {
			setLabelThreadsPerHostValue(getSliderThreadsPerHost().getValue());
		}
		return labelThreadsPerHostValue;
	}

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

	// Not enabled yet
	/*
	private JCheckBox getChkTargetParamsUrl() {
		if (chkTargetParamsUrl == null) {
			chkTargetParamsUrl = new JCheckBox();
			chkTargetParamsUrl.setText(Constant.messages.getString("ascan.options.params.url.label"));
		}
		return chkTargetParamsUrl;
	}

	private JCheckBox getChkTargetParamsForm() {
		if (chkTargetParamsForm == null) {
			chkTargetParamsForm = new JCheckBox();
			chkTargetParamsForm.setText(Constant.messages.getString("ascan.options.params.form.label"));
		}
		return chkTargetParamsForm;
	}

	private JCheckBox getChkTargetParamsCookie() {
		if (chkTargetParamsCookie == null) {
			chkTargetParamsCookie = new JCheckBox();
			chkTargetParamsCookie.setText(Constant.messages.getString("ascan.options.params.cookie.label"));
		}
		return chkTargetParamsCookie;
	}
	*/

}

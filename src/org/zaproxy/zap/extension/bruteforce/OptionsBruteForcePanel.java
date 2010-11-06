/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.bruteforce;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;

public class OptionsBruteForcePanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JPanel panelPortScan = null;
    public OptionsBruteForcePanel() {
        super();
 		initialize();
   }
    
	private JSlider sliderThreadsPerScan = null;
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("bruteforce.options.title"));
        this.setSize(314, 245);
        this.add(getPanelPortScan(), getPanelPortScan().getName());
	}
	/**
	 * This method initializes panelSpider	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelPortScan() {
		if (panelPortScan == null) {
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();

			JLabel jLabel2 = new JLabel();

			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();

			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

			panelPortScan = new JPanel();
			JLabel jLabel1 = new JLabel();

			panelPortScan.setLayout(new GridBagLayout());
			panelPortScan.setSize(114, 132);
			panelPortScan.setName("");
			jLabel1.setText(Constant.messages.getString("bruteforce.options.label.threads"));
		
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 2;
			gridBagConstraints3.ipadx = 0;
			gridBagConstraints3.ipady = 0;
			gridBagConstraints3.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.insets = new Insets(2,2,2,2);
			gridBagConstraints3.weightx = 1.0D;
			gridBagConstraints3.gridwidth = 2;
			
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 3;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.ipadx = 0;
			gridBagConstraints4.ipady = 0;
			gridBagConstraints4.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints4.insets = new Insets(2,2,2,2);
			gridBagConstraints4.gridwidth = 2;
			
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 10;
			gridBagConstraints6.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.insets = new Insets(2,2,2,2);
			gridBagConstraints6.weightx = 1.0D;
			gridBagConstraints6.weighty = 1.0D;
			gridBagConstraints6.gridwidth = 2;
			
			jLabel2.setText("");
			panelPortScan.add(jLabel1, gridBagConstraints3);
			panelPortScan.add(getSliderThreadsPerScan(), gridBagConstraints4);
			panelPortScan.add(jLabel2, gridBagConstraints6);
		}
		return panelPortScan;
	}
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    BruteForceParam param = (BruteForceParam) options.getParamSet(BruteForceParam.class);
	    if (param == null) {
		    getSliderThreadsPerScan().setValue(BruteForceParam.DEFAULT_THREAD_PER_SCAN);
	    } else {
		    getSliderThreadsPerScan().setValue(param.getThreadPerScan());
	    }
	}
	
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    BruteForceParam param = (BruteForceParam) options.getParamSet(BruteForceParam.class);
	    if (param == null) {
	    	param = new BruteForceParam();
	    	options.addParamSet(param);
	    }
	   	param.setThreadPerScan(getSliderThreadsPerScan().getValue());
	}
	
	/**
	 * This method initializes sliderThreadsPerHost	
	 * 	
	 * @return JSlider	
	 */    
	private JSlider getSliderThreadsPerScan() {
		if (sliderThreadsPerScan == null) {
			sliderThreadsPerScan = new JSlider();
			sliderThreadsPerScan.setMaximum(20);	// TODO put in Constants?
			sliderThreadsPerScan.setMinimum(1);
			sliderThreadsPerScan.setValue(BruteForceParam.DEFAULT_THREAD_PER_SCAN);
			sliderThreadsPerScan.setPaintTicks(true);
			sliderThreadsPerScan.setPaintLabels(true);
			sliderThreadsPerScan.setMinorTickSpacing(1);
			sliderThreadsPerScan.setMajorTickSpacing(1);
			sliderThreadsPerScan.setSnapToTicks(true);
			sliderThreadsPerScan.setPaintTrack(true);
		}
		return sliderThreadsPerScan;
	}

    public int getThreadPerScan() {
    	return this.sliderThreadsPerScan.getValue();
    }

} 

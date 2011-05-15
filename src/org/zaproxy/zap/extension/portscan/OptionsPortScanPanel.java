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
package org.zaproxy.zap.extension.portscan;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;

public class OptionsPortScanPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JPanel panelPortScan = null;
	private JSlider sliderMaxPort = null;
	private JSlider sliderThreadsPerScan = null;
	private JSlider sliderTimeoutInMs = null;
	private JLabel labelMaxPortValue = null;
	private JCheckBox checkUseProxy = null;
	
    public OptionsPortScanPanel() {
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
        this.setName(Constant.messages.getString("ports.options.title"));
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
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints7b = new GridBagConstraints();
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1b = new GridBagConstraints();

			panelPortScan = new JPanel();

			JLabel jLabel = new JLabel();
			JLabel jLabel1 = new JLabel();
			JLabel jLabel2 = new JLabel();
			JLabel jLabel3 = new JLabel();
			JLabel jLabelx = new JLabel();
			
			labelMaxPortValue = new JLabel();
			labelMaxPortValue.setText(""+getSliderMaxPort().getValue());

			panelPortScan.setLayout(new GridBagLayout());
			panelPortScan.setSize(114, 132);
			panelPortScan.setName("");
			jLabel.setText(Constant.messages.getString("ports.options.label.maxPort"));
			jLabel1.setText(Constant.messages.getString("ports.options.label.threads"));
			jLabel2.setText(Constant.messages.getString("ports.options.label.timeoutInMs"));
			jLabel3.setText(Constant.messages.getString("ports.options.label.useProxy"));
			jLabelx.setText("");
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.ipadx = 0;
			gridBagConstraints1.ipady = 0;
			gridBagConstraints1.insets = new Insets(2,2,2,2);
			gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.weightx = 1.0D;
			
			gridBagConstraints1b.gridx = 1;
			gridBagConstraints1b.gridy = 0;
			gridBagConstraints1b.ipadx = 0;
			gridBagConstraints1b.ipady = 0;
			gridBagConstraints1b.insets = new Insets(2,2,2,2);
			gridBagConstraints1b.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints1b.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1b.weightx = 1.0D;
			
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.ipady = 0;
			gridBagConstraints2.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints2.insets = new Insets(2,2,2,2);
			gridBagConstraints2.gridwidth = 2;
			
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
			
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 4;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.ipadx = 0;
			gridBagConstraints5.ipady = 0;
			gridBagConstraints5.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints5.insets = new Insets(2,2,2,2);
			gridBagConstraints5.gridwidth = 2;
			
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 5;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.ipadx = 0;
			gridBagConstraints6.ipady = 0;
			gridBagConstraints6.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints6.insets = new Insets(2,2,2,2);
			gridBagConstraints6.gridwidth = 2;
			
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.gridy = 6;
			gridBagConstraints7.ipadx = 0;
			gridBagConstraints7.ipady = 0;
			gridBagConstraints7.insets = new Insets(2,2,2,2);
			gridBagConstraints7.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.weightx = 1.0D;
			
			gridBagConstraints7b.gridx = 1;
			gridBagConstraints7b.gridy = 6;
			gridBagConstraints7b.ipadx = 0;
			gridBagConstraints7b.ipady = 0;
			gridBagConstraints7b.insets = new Insets(2,2,2,2);
			gridBagConstraints7b.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints7b.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints7b.weightx = 1.0D;
			
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.gridy = 10;
			gridBagConstraints8.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints8.fill = GridBagConstraints.BOTH;
			gridBagConstraints8.insets = new Insets(2,2,2,2);
			gridBagConstraints8.weightx = 1.0D;
			gridBagConstraints8.weighty = 1.0D;
			gridBagConstraints8.gridwidth = 2;
			
			panelPortScan.add(jLabel, gridBagConstraints1);
			panelPortScan.add(labelMaxPortValue, gridBagConstraints1b);
			panelPortScan.add(getSliderMaxPort(), gridBagConstraints2);
			panelPortScan.add(jLabel1, gridBagConstraints3);
			panelPortScan.add(getSliderThreadsPerScan(), gridBagConstraints4);
			panelPortScan.add(jLabel2, gridBagConstraints5);
			panelPortScan.add(getSliderTimeoutInMs(), gridBagConstraints6);
			panelPortScan.add(jLabel3, gridBagConstraints7);
			panelPortScan.add(getCheckUseProxy(), gridBagConstraints7b);
			panelPortScan.add(jLabelx, gridBagConstraints8);
		}
		return panelPortScan;
	}
	
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    PortScanParam param = (PortScanParam) options.getParamSet(PortScanParam.class);
	    if (param == null) {
		    getSliderMaxPort().setValue(PortScanParam.DEFAULT_MAX_PORT);
		    getSliderThreadsPerScan().setValue(PortScanParam.DEFAULT_THREAD_PER_SCAN);
		    getSliderTimeoutInMs().setValue(PortScanParam.DEFAULT_TIMEOUT_IN_MS);
		    getCheckUseProxy().setSelected(PortScanParam.DEFAULT_USE_PROXY);
	    } else {
		    getSliderMaxPort().setValue(param.getMaxPort());
		    getSliderThreadsPerScan().setValue(param.getThreadPerScan());
		    getSliderTimeoutInMs().setValue(param.getTimeoutInMs());
		    getCheckUseProxy().setSelected(param.isUseProxy());
	    }
	}
	
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    PortScanParam param = (PortScanParam) options.getParamSet(PortScanParam.class);
	    if (param == null) {
	    	param = new PortScanParam();
	    	options.addParamSet(param);
	    }
	    param.setMaxPort(getSliderMaxPort().getValue());
	   	param.setThreadPerScan(getSliderThreadsPerScan().getValue());
	   	param.setTimeoutInMs(getSliderTimeoutInMs().getValue());
	   	param.setUseProxy(getCheckUseProxy().isSelected());
	}
	
	/**
	 * This method initializes sliderHostPerScan	
	 * 	
	 * @return JSlider	
	 */    
	private JSlider getSliderMaxPort() {
		if (sliderMaxPort == null) {
			sliderMaxPort = new JSlider();
			sliderMaxPort.setMaximum(65536);
			sliderMaxPort.setMinimum(1024);
			sliderMaxPort.setMinorTickSpacing(1024);
			sliderMaxPort.setPaintTicks(true);
			sliderMaxPort.setPaintLabels(true);
			sliderMaxPort.setName("");
			sliderMaxPort.setMajorTickSpacing(8192);
			sliderMaxPort.setSnapToTicks(true);
			sliderMaxPort.setPaintTrack(true);
			
			sliderMaxPort.addChangeListener(new ChangeListener () {

				@Override
				public void stateChanged(ChangeEvent e) {
					labelMaxPortValue.setText("" + sliderMaxPort.getValue());
					
				}});
		}
		return sliderMaxPort;
	}
	/**
	 * This method initializes sliderThreadsPerHost	
	 * 	
	 * @return JSlider	
	 */    
	private JSlider getSliderThreadsPerScan() {
		if (sliderThreadsPerScan == null) {
			sliderThreadsPerScan = new JSlider();
			sliderThreadsPerScan.setMaximum(Constant.MAX_HOST_CONNECTION);
			sliderThreadsPerScan.setMinimum(1);
			sliderThreadsPerScan.setValue(1);
			sliderThreadsPerScan.setPaintTicks(true);
			sliderThreadsPerScan.setPaintLabels(true);
			sliderThreadsPerScan.setMinorTickSpacing(1);
			sliderThreadsPerScan.setMajorTickSpacing(1);
			sliderThreadsPerScan.setSnapToTicks(true);
			sliderThreadsPerScan.setPaintTrack(true);
		}
		return sliderThreadsPerScan;
	}

	private JSlider getSliderTimeoutInMs() {
		if (sliderTimeoutInMs == null) {
			sliderTimeoutInMs = new JSlider();
			sliderTimeoutInMs.setMaximum(1000);
			sliderTimeoutInMs.setMinimum(0);
			sliderTimeoutInMs.setValue(200);
			sliderTimeoutInMs.setPaintTicks(true);
			sliderTimeoutInMs.setPaintLabels(true);
			sliderTimeoutInMs.setMinorTickSpacing(20);
			sliderTimeoutInMs.setMajorTickSpacing(100);
			sliderTimeoutInMs.setSnapToTicks(true);
			sliderTimeoutInMs.setPaintTrack(true);
		}
		return sliderTimeoutInMs;
	}

    public int getThreadPerScan() {
    	return this.sliderThreadsPerScan.getValue();
    }

    public int getMaxPort() {
    	return this.sliderMaxPort.getValue();
    }
    
	public JCheckBox getCheckUseProxy() {
		if (checkUseProxy == null) {
			checkUseProxy = new JCheckBox();
		}
		return checkUseProxy;
	}
} 

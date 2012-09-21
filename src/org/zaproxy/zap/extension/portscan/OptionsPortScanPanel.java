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
import org.zaproxy.zap.view.LayoutHelper;

public class OptionsPortScanPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JPanel panelPortScan = null;
	private JSlider sliderMaxPort = null;
	private JSlider sliderThreadsPerScan = null;
	private JSlider sliderTimeoutInMs = null;
	private JLabel labelMaxPortValue = null;
	private JLabel labelThreadsPerScanValue = null;
	private JCheckBox checkUseProxy = null;
	
    public OptionsPortScanPanel() {
        super();
 		initialize();
   }
    
	/**
	 * This method initializes this
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
			panelPortScan = new JPanel();
			panelPortScan.setLayout(new GridBagLayout());
			panelPortScan.setSize(114, 132);
			panelPortScan.setName("");

			labelMaxPortValue = new JLabel();
			labelMaxPortValue.setText(""+getSliderMaxPort().getValue());

			panelPortScan.add(new JLabel(Constant.messages.getString("ports.options.label.maxPort")), 
					LayoutHelper.getGBC(0, 0, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelPortScan.add(labelMaxPortValue, 
					LayoutHelper.getGBC(1, 0, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelPortScan.add(getSliderMaxPort(), 
					LayoutHelper.getGBC(0, 1, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelPortScan.add(new JLabel(Constant.messages.getString("ports.options.label.threads")), 
					LayoutHelper.getGBC(0, 2, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelPortScan.add(getLabelThreadsPerScanValue(), 
					LayoutHelper.getGBC(1, 2, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelPortScan.add(getSliderThreadsPerScan(), 
					LayoutHelper.getGBC(0, 3, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelPortScan.add(new JLabel(Constant.messages.getString("ports.options.label.timeoutInMs")), 
					LayoutHelper.getGBC(0, 4, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelPortScan.add(getSliderTimeoutInMs(), 
					LayoutHelper.getGBC(0, 5, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelPortScan.add(new JLabel(Constant.messages.getString("ports.options.label.useProxy")), 
					LayoutHelper.getGBC(0, 6, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelPortScan.add(getCheckUseProxy(), 
					LayoutHelper.getGBC(1, 6, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2)));
			panelPortScan.add(new JLabel(""), 
					LayoutHelper.getGBC(0, 10, 2, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(2,2,2,2)));
		}
		return panelPortScan;
	}
	
	@Override
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
	
	@Override
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	@Override
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
			sliderThreadsPerScan.setMaximum(Constant.MAX_THREADS_PER_SCAN);
			sliderThreadsPerScan.setMinimum(0);
			sliderThreadsPerScan.setValue(1);
			sliderThreadsPerScan.setPaintTicks(true);
			sliderThreadsPerScan.setPaintLabels(true);
			sliderThreadsPerScan.setMinorTickSpacing(1);
			sliderThreadsPerScan.setMajorTickSpacing(5);
			sliderThreadsPerScan.setSnapToTicks(true);
			sliderThreadsPerScan.setPaintTrack(true);
			sliderThreadsPerScan.addChangeListener(new ChangeListener () {
				@Override
				public void stateChanged(ChangeEvent e) {
					// If the minimum is set to 1 then the ticks are at 6, 11 etc
					// But we dont want to support 0 threads, hence this hack
					if (getSliderThreadsPerScan().getValue() == 0) {
						getSliderThreadsPerScan().setValue(1);
					}
					setLabelThreadsPerScanValue(getSliderThreadsPerScan().getValue());
				}});
		}
		return sliderThreadsPerScan;
	}

	private void setLabelThreadsPerScanValue(int value) {
		if (labelThreadsPerScanValue == null) {
			labelThreadsPerScanValue = new JLabel();
		}
		labelThreadsPerScanValue.setText(""+value);
	}

	private JLabel getLabelThreadsPerScanValue() {
		if (labelThreadsPerScanValue == null) {
			setLabelThreadsPerScanValue(getSliderThreadsPerScan().getValue());
		}
		return labelThreadsPerScanValue;
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

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.portscan";
	}
	
} 

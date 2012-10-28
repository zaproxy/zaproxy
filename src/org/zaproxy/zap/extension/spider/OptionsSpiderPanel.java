/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 cosminstefanxp [at] gmail [.] com
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
 * 
 * Note that this extension and the other classes in this package are heavily 
 * based on the original Paros OptionsSpiderPanel! 
 */

package org.zaproxy.zap.extension.spider;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.spider.SpiderParam.HandleParametersOption;
import org.zaproxy.zap.utils.ZapTextArea;

/**
 * The Class OptionsSpiderPanel defines the Options Panel showed when configuring settings related to the
 * spider.
 */
public class OptionsSpiderPanel extends AbstractParamPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5623691753271231473L;

	/** The full panel for the spider options. */
	private JPanel panelSpider = null;

	// The controls for the options:
	private JSlider sliderMaxDepth = null;
	private JSlider sliderThreads = null;
	private ZapTextArea txtDomainScope = null;
	private JScrollPane scrollPaneDomainScope = null;
	private JCheckBox chkPostForm = null;
	private JCheckBox chkProcessForm = null;
	private JCheckBox useCookies = null;
	private JCheckBox parseComments = null;
	private JCheckBox parseRobotsTxt = null;
	private JComboBox<HandleParametersOption> handleParameters = null;

	/**
	 * Instantiates a new options spider panel.
	 */
	public OptionsSpiderPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this options Panel.
	 */
	private void initialize() {
		this.setLayout(new CardLayout());
		this.setName(Constant.messages.getString("spider.options.title"));
		if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
			this.setSize(314, 245);
		}
		this.add(getPanelSpider(), getPanelSpider().getName());
	}

	/**
	 * This method initializes the main panel containing all option controls.
	 * 
	 * @return the panel for the spider options.
	 */
	private JPanel getPanelSpider() {
		if (panelSpider == null) {

			// Initialize the panel
			panelSpider = new JPanel();
			panelSpider.setLayout(new GridBagLayout());
			if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
				panelSpider.setSize(114, 132);
			}
			panelSpider.setName("");

			// Prepare the GridBagConstraints, defining the position/layout of elements
			GridBagConstraints maxDepthSliderGridBag = new GridBagConstraints();
			GridBagConstraints maxDepthLabelGridBag = new GridBagConstraints();
			GridBagConstraints noThreadsSliderGridBag = new GridBagConstraints();
			GridBagConstraints noThreadsLabelGridBag = new GridBagConstraints();
			GridBagConstraints domainsScrollPaneGridBag = new GridBagConstraints();
			GridBagConstraints domainsLabelGridBag = new GridBagConstraints();
			GridBagConstraints processFormGridBag = new GridBagConstraints();
			GridBagConstraints postFormGridBag = new GridBagConstraints();
			GridBagConstraints useCookiesGridBag = new GridBagConstraints();
			GridBagConstraints parseCommentsGridBag = new GridBagConstraints();
			GridBagConstraints parseRobotsTxtGridBag = new GridBagConstraints();
			GridBagConstraints handleParametersGridBag = new GridBagConstraints();
			GridBagConstraints handleParametersLabelGridBag = new GridBagConstraints();

			maxDepthLabelGridBag.gridx = 0;
			maxDepthLabelGridBag.gridy = 0;
			maxDepthLabelGridBag.weightx = 1.0D;
			maxDepthLabelGridBag.fill = GridBagConstraints.HORIZONTAL;
			maxDepthLabelGridBag.anchor = GridBagConstraints.NORTHWEST;
			maxDepthLabelGridBag.insets = new Insets(2, 2, 2, 2);

			maxDepthSliderGridBag.gridx = 0;
			maxDepthSliderGridBag.gridy = 1;
			maxDepthSliderGridBag.weightx = 1.0;
			maxDepthSliderGridBag.fill = GridBagConstraints.HORIZONTAL;
			maxDepthSliderGridBag.anchor = GridBagConstraints.NORTHWEST;
			maxDepthSliderGridBag.insets = new Insets(2, 2, 2, 2);

			noThreadsLabelGridBag.gridx = 0;
			noThreadsLabelGridBag.gridy = 2;
			noThreadsLabelGridBag.weightx = 1.0D;
			noThreadsLabelGridBag.fill = GridBagConstraints.HORIZONTAL;
			noThreadsLabelGridBag.anchor = GridBagConstraints.NORTHWEST;
			noThreadsLabelGridBag.insets = new Insets(2, 2, 2, 2);

			noThreadsSliderGridBag.gridx = 0;
			noThreadsSliderGridBag.gridy = 3;
			noThreadsSliderGridBag.weightx = 1.0;
			noThreadsSliderGridBag.fill = GridBagConstraints.HORIZONTAL;
			noThreadsSliderGridBag.anchor = GridBagConstraints.NORTHWEST;
			noThreadsSliderGridBag.insets = new Insets(2, 2, 2, 2);

			domainsLabelGridBag.gridx = 0;
			domainsLabelGridBag.gridy = 4;
			domainsLabelGridBag.weightx = 1.0D;
			domainsLabelGridBag.fill = GridBagConstraints.HORIZONTAL;
			domainsLabelGridBag.anchor = GridBagConstraints.NORTHWEST;
			domainsLabelGridBag.insets = new Insets(2, 2, 2, 2);

			domainsScrollPaneGridBag.gridx = 0;
			domainsScrollPaneGridBag.gridy = 5;
			domainsScrollPaneGridBag.weightx = 1.0;
			domainsScrollPaneGridBag.weighty = 0.3;
			domainsScrollPaneGridBag.fill = GridBagConstraints.BOTH;
			domainsScrollPaneGridBag.anchor = GridBagConstraints.NORTHWEST;
			domainsScrollPaneGridBag.insets = new Insets(2, 2, 2, 2);

			handleParametersLabelGridBag.gridx = 0;
			handleParametersLabelGridBag.gridy = 6;
			handleParametersLabelGridBag.weightx = 1.0D;
			handleParametersLabelGridBag.fill = GridBagConstraints.HORIZONTAL;
			handleParametersLabelGridBag.anchor = GridBagConstraints.NORTHWEST;
			handleParametersLabelGridBag.insets = new Insets(2, 2, 2, 2);


			handleParametersGridBag.gridx = 0;
			handleParametersGridBag.gridy = 7;
			handleParametersGridBag.weightx = 1.0;
			handleParametersGridBag.fill = GridBagConstraints.HORIZONTAL;
			handleParametersGridBag.anchor = GridBagConstraints.NORTHWEST;
			handleParametersGridBag.insets = new Insets(2, 2, 2, 2);

			
			processFormGridBag.gridx = 0;
			processFormGridBag.gridy = 8;
			processFormGridBag.weightx = 1.0;
			processFormGridBag.fill = GridBagConstraints.HORIZONTAL;
			processFormGridBag.anchor = GridBagConstraints.WEST;
			processFormGridBag.insets = new Insets(2, 2, 2, 2);

			postFormGridBag.gridx = 0;
			postFormGridBag.gridy = 9;
			postFormGridBag.weightx = 1.0;
			postFormGridBag.fill = GridBagConstraints.HORIZONTAL;
			postFormGridBag.anchor = GridBagConstraints.WEST;
			postFormGridBag.insets = new Insets(2, 15, 2, 2);

			useCookiesGridBag.gridx = 0;
			useCookiesGridBag.gridy = 10;
			useCookiesGridBag.weightx = 1.0;
			useCookiesGridBag.fill = GridBagConstraints.HORIZONTAL;
			useCookiesGridBag.anchor = GridBagConstraints.NORTHWEST;
			useCookiesGridBag.insets = new Insets(2, 2, 2, 2);

			parseCommentsGridBag.gridx = 0;
			parseCommentsGridBag.gridy =11;
			parseCommentsGridBag.weightx = 1.0;
			parseCommentsGridBag.fill = GridBagConstraints.HORIZONTAL;
			parseCommentsGridBag.anchor = GridBagConstraints.NORTHWEST;
			parseCommentsGridBag.insets = new Insets(2, 2, 2, 2);

			parseRobotsTxtGridBag.gridx = 0;
			parseRobotsTxtGridBag.gridy = 12;
			parseRobotsTxtGridBag.weightx = 1.0;
			parseRobotsTxtGridBag.fill = GridBagConstraints.HORIZONTAL;
			parseRobotsTxtGridBag.anchor = GridBagConstraints.NORTHWEST;
			parseRobotsTxtGridBag.insets = new Insets(2, 2, 2, 2);

			// Prepare the necessary labels
			JLabel domainsLabel = new JLabel();
			JLabel noThreadsLabel = new JLabel();
			JLabel maxDepthLabel = new JLabel();
			JLabel handleParametersLabel = new JLabel();

			maxDepthLabel.setText(Constant.messages.getString("spider.options.label.depth"));
			noThreadsLabel.setText(Constant.messages.getString("spider.options.label.threads"));
			domainsLabel.setText(Constant.messages.getString("spider.options.label.domains"));
			handleParametersLabel.setText(Constant.messages.getString("spider.options.label.handleparameters"));

			// Add the components on the panel
			panelSpider.add(maxDepthLabel, maxDepthLabelGridBag);
			panelSpider.add(getSliderMaxDepth(), maxDepthSliderGridBag);
			panelSpider.add(noThreadsLabel, noThreadsLabelGridBag);
			panelSpider.add(getSliderThreads(), noThreadsSliderGridBag);
			panelSpider.add(domainsLabel, domainsLabelGridBag);
			panelSpider.add(getDomainsScrollPane(), domainsScrollPaneGridBag);
			panelSpider.add(getChkProcessForm(), processFormGridBag);
			panelSpider.add(getChkPostForm(), postFormGridBag);
			panelSpider.add(getChkUseCookies(), useCookiesGridBag);
			panelSpider.add(getChkParseComments(), parseCommentsGridBag);
			panelSpider.add(getChkParseRobotsTxt(), parseRobotsTxtGridBag);
			panelSpider.add(handleParametersLabel, handleParametersLabelGridBag);
			panelSpider.add(getComboHandleParameters(), handleParametersGridBag);
		}
		return panelSpider;
	}

	@Override
	public void initParam(Object obj) {
		OptionsParam options = (OptionsParam) obj;

		SpiderParam param = (SpiderParam) options.getParamSet(SpiderParam.class);
		getSliderMaxDepth().setValue(param.getMaxDepth());
		getSliderThreads().setValue(param.getThreadCount());
		getDomainScopeTextArea().setText(param.getScopeText());
		getDomainScopeTextArea().discardAllEdits();
		getChkProcessForm().setSelected(param.isProcessForm());
		getChkPostForm().setSelected(param.isPostForm());
		getChkUseCookies().setSelected(param.isSendCookies());
		getChkParseComments().setSelected(param.isParseComments());
		getChkParseRobotsTxt().setSelected(param.isParseRobotsTxt());
		getComboHandleParameters().setSelectedItem(param.getHandleParameters());

	}

	@Override
	public void validateParam(Object obj) {
		// no validation needed
	}

	@Override
	public void saveParam(Object obj) throws Exception {
		OptionsParam options = (OptionsParam) obj;
		SpiderParam param = (SpiderParam) options.getParamSet(SpiderParam.class);
		param.setMaxDepth(getSliderMaxDepth().getValue());
		param.setThreadCount(getSliderThreads().getValue());
		param.setScopeString(getDomainScopeTextArea().getText());
		param.setProcessForm(getChkProcessForm().isSelected());
		param.setPostForm(getChkPostForm().isSelected());
		param.setSendCookies(getChkUseCookies().isSelected());
		param.setParseComments(getChkParseComments().isSelected());
		param.setParseRobotsTxt(getChkParseRobotsTxt().isSelected());
		param.setHandleParameters((HandleParametersOption) getComboHandleParameters().getSelectedItem());
	}

	/**
	 * This method initializes the slider for MaxDepth.
	 * 
	 * @return the slider for max depth
	 */
	private JSlider getSliderMaxDepth() {
		if (sliderMaxDepth == null) {
			sliderMaxDepth = new JSlider();
			sliderMaxDepth.setMaximum(19);
			sliderMaxDepth.setMinimum(1);
			sliderMaxDepth.setMinorTickSpacing(1);
			sliderMaxDepth.setPaintTicks(true);
			sliderMaxDepth.setPaintLabels(true);
			sliderMaxDepth.setName("");
			sliderMaxDepth.setMajorTickSpacing(1);
			sliderMaxDepth.setSnapToTicks(true);
			sliderMaxDepth.setPaintTrack(true);
		}
		return sliderMaxDepth;
	}

	/**
	 * This method initializes the slider for maximum number of threads used.
	 * 
	 * @return javax.swing.JSlider
	 */
	private JSlider getSliderThreads() {
		if (sliderThreads == null) {
			sliderThreads = new JSlider();
			sliderThreads.setMaximum(Constant.MAX_THREADS_PER_SCAN);
			sliderThreads.setMinimum(0);
			sliderThreads.setValue(1);
			sliderThreads.setPaintTicks(true);
			sliderThreads.setPaintLabels(true);
			sliderThreads.setMinorTickSpacing(1);
			sliderThreads.setMajorTickSpacing(5);
			sliderThreads.setSnapToTicks(true);
			sliderThreads.setPaintTrack(true);
			sliderThreads.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					// If the minimum is set to 1 then the ticks are at 6, 11 etc
					// But we dont want to support 0 threads, hence this hack
					if (getSliderThreads().getValue() == 0) {
						getSliderThreads().setValue(1);
					}
					// setLabelThreadsPerHostValue(getSliderThreads().getValue());
				}
			});
		}
		return sliderThreads;
	}

	/**
	 * This method initializes the text area for the domain scope.
	 * 
	 * @return org.zaproxy.zap.utils.ZapTextArea
	 */
	private ZapTextArea getDomainScopeTextArea() {
		if (txtDomainScope == null) {
			txtDomainScope = new ZapTextArea();
			txtDomainScope.setLineWrap(true);
			txtDomainScope.setRows(3);
			txtDomainScope.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
		}
		return txtDomainScope;
	}

	/**
	 * This method initializes the ScrollPane for the domain scope.
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getDomainsScrollPane() {
		if (scrollPaneDomainScope == null) {
			scrollPaneDomainScope = new JScrollPane();
			scrollPaneDomainScope.setPreferredSize(new java.awt.Dimension(294, 30));
			scrollPaneDomainScope.setViewportView(getDomainScopeTextArea());
		}
		return scrollPaneDomainScope;
	}

	/**
	 * This method initializes the checkbox for POST form option. This option should not be enabled if the
	 * forms are not processed at all.
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getChkPostForm() {
		if (chkPostForm == null) {
			chkPostForm = new JCheckBox();
			chkPostForm.setText(Constant.messages.getString("spider.options.label.post"));

			if (!getChkProcessForm().isSelected())
				chkPostForm.setEnabled(false);
		}
		return chkPostForm;
	}

	/**
	 * This method initializes the checkbox for process form option.
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getChkProcessForm() {
		if (chkProcessForm == null) {
			chkProcessForm = new JCheckBox();
			chkProcessForm.setText(Constant.messages.getString("spider.options.label.processform"));

			// Code for controlling the status of the chkPostForm
			chkProcessForm.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ev) {
					if (chkProcessForm.isSelected())
						chkPostForm.setEnabled(true);
					else
						chkPostForm.setEnabled(false);
				}
			});
		}
		return chkProcessForm;
	}

	/**
	 * This method initializes the Use Cookies checkbox.
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getChkUseCookies() {
		if (useCookies == null) {
			useCookies = new JCheckBox();
			useCookies.setText(Constant.messages.getString("spider.options.label.cookies"));
		}
		return useCookies;
	}

	/**
	 * This method initializes the Parse Comments checkbox.
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getChkParseComments() {
		if (parseComments == null) {
			parseComments = new JCheckBox();
			parseComments.setText(Constant.messages.getString("spider.options.label.comments"));
		}
		return parseComments;
	}

	/**
	 * This method initializes the Parse robots.txt checkbox.
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getChkParseRobotsTxt() {
		if (parseRobotsTxt == null) {
			parseRobotsTxt = new JCheckBox();
			parseRobotsTxt.setText(Constant.messages.getString("spider.options.label.robotstxt"));
		}
		return parseRobotsTxt;
	}

	/**
	 * This method initializes the combobox for HandleParameters option.
	 * 
	 * @return the combo handle parameters
	 */
	private JComboBox<HandleParametersOption> getComboHandleParameters() {
		if (handleParameters == null) {
			handleParameters = new JComboBox<HandleParametersOption>(new HandleParametersOption[] {
					HandleParametersOption.USE_ALL, HandleParametersOption.IGNORE_VALUE,
					HandleParametersOption.IGNORE_COMPLETELY });

		}
		return handleParameters;
	}

	/**
	 * This method initializes the help index.
	 * 
	 * @return the help index
	 */
	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.spider";
	}

}

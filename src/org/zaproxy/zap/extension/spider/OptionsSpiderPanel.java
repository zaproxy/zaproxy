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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SortOrder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.spider.SpiderParam.HandleParametersOption;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;
import org.zaproxy.zap.view.PositiveValuesSlider;

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
	private JCheckBox chkPostForm = null;
	private JCheckBox chkProcessForm = null;
	private JCheckBox parseComments = null;
	private JCheckBox parseRobotsTxt = null;
	private JCheckBox parseSVNEntries = null;
	private JCheckBox parseGit = null;
	private JCheckBox handleODataSpecificParameters = null;
	private DomainsAlwaysInScopeMultipleOptionsPanel domainsAlwaysInScopePanel;
	private DomainsAlwaysInScopeTableModel domainsAlwaysInScopeTableModel;

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
			panelSpider = new JPanel(new BorderLayout());
			if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
				panelSpider.setSize(114, 150);
			}
			panelSpider.setName("");

			// Prepare the GridBagConstraints, defining the position/layout of elements
			GridBagConstraints maxDepthSliderGridBag = new GridBagConstraints();
			GridBagConstraints maxDepthLabelGridBag = new GridBagConstraints();
			GridBagConstraints noThreadsSliderGridBag = new GridBagConstraints();
			GridBagConstraints noThreadsLabelGridBag = new GridBagConstraints();
			GridBagConstraints domainsLabelGridBag = new GridBagConstraints();
            GridBagConstraints domainsPanelGridBag = new GridBagConstraints();
			GridBagConstraints processFormGridBag = new GridBagConstraints();
			GridBagConstraints postFormGridBag = new GridBagConstraints();
			GridBagConstraints useCookiesGridBag = new GridBagConstraints();
			GridBagConstraints parseCommentsGridBag = new GridBagConstraints();
			GridBagConstraints parseRobotsTxtGridBag = new GridBagConstraints();
			GridBagConstraints parseSVNEntriesGridBag = new GridBagConstraints();
			GridBagConstraints parseGitGridBag = new GridBagConstraints();
			GridBagConstraints handleODataSpecificParametersGridBag = new GridBagConstraints();
			
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

			domainsPanelGridBag.gridx = 0;
			domainsPanelGridBag.gridy = 5;
			domainsPanelGridBag.weightx = 1.0;
			domainsPanelGridBag.weighty = 1.0;
			domainsPanelGridBag.fill = GridBagConstraints.BOTH;
			domainsPanelGridBag.anchor = GridBagConstraints.NORTHWEST;
			domainsPanelGridBag.insets = new Insets(2, 2, 2, 2);

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

			parseSVNEntriesGridBag.gridx = 0;
			parseSVNEntriesGridBag.gridy = 13;
			parseSVNEntriesGridBag.weightx = 1.0;
			parseSVNEntriesGridBag.fill = GridBagConstraints.HORIZONTAL;
			parseSVNEntriesGridBag.anchor = GridBagConstraints.NORTHWEST;
			parseSVNEntriesGridBag.insets = new Insets(2, 2, 2, 2);

			parseGitGridBag.gridx = 0;
			parseGitGridBag.gridy = 14;
			parseGitGridBag.weightx = 1.0;
			parseGitGridBag.fill = GridBagConstraints.HORIZONTAL;
			parseGitGridBag.anchor = GridBagConstraints.NORTHWEST;
			parseGitGridBag.insets = new Insets(2, 2, 2, 2);

			handleODataSpecificParametersGridBag.gridx = 0;
			handleODataSpecificParametersGridBag.gridy = 15;
			handleODataSpecificParametersGridBag.weightx = 1.0;
			handleODataSpecificParametersGridBag.fill = GridBagConstraints.HORIZONTAL;
			handleODataSpecificParametersGridBag.anchor = GridBagConstraints.NORTHWEST;
			handleODataSpecificParametersGridBag.insets = new Insets(2, 2, 2, 2);

			

			// Prepare the necessary labels
			JLabel domainsLabel = new JLabel();
			JLabel noThreadsLabel = new JLabel();
			JLabel maxDepthLabel = new JLabel();
			JLabel handleParametersLabel = new JLabel();

			maxDepthLabel.setText(Constant.messages.getString("spider.options.label.depth"));
			noThreadsLabel.setText(Constant.messages.getString("spider.options.label.threads"));
			domainsLabel.setText(Constant.messages.getString("spider.options.label.domains"));
			handleParametersLabel.setText(Constant.messages.getString("spider.options.label.handleparameters"));

			JPanel innerPanel = new JPanel(new GridBagLayout());

			// Add the components on the panel
			innerPanel.add(maxDepthLabel, maxDepthLabelGridBag);
			innerPanel.add(getSliderMaxDepth(), maxDepthSliderGridBag);
			innerPanel.add(noThreadsLabel, noThreadsLabelGridBag);
			innerPanel.add(getSliderThreads(), noThreadsSliderGridBag);
			innerPanel.add(domainsLabel, domainsLabelGridBag);
			innerPanel.add(getDomainsAlwaysInScopePanel(), domainsPanelGridBag);
			innerPanel.add(getChkProcessForm(), processFormGridBag);
			innerPanel.add(getChkPostForm(), postFormGridBag);
			innerPanel.add(getChkParseComments(), parseCommentsGridBag);
			innerPanel.add(getChkParseRobotsTxt(), parseRobotsTxtGridBag);
			innerPanel.add(getChkParseSVNEntries(), parseSVNEntriesGridBag);
			innerPanel.add(getChkParseGit(), parseGitGridBag);
			innerPanel.add(handleParametersLabel, handleParametersLabelGridBag);
			innerPanel.add(getComboHandleParameters(), handleParametersGridBag);
			innerPanel.add(getHandleODataSpecificParameters(), handleODataSpecificParametersGridBag);

			JScrollPane scrollPane = new JScrollPane(innerPanel);
			scrollPane.setBorder(BorderFactory.createEmptyBorder());

			panelSpider.add(scrollPane, BorderLayout.CENTER);
		}
		return panelSpider;
	}

	@Override
	public void initParam(Object obj) {
		OptionsParam options = (OptionsParam) obj;

		SpiderParam param = (SpiderParam) options.getParamSet(SpiderParam.class);
		getSliderMaxDepth().setValue(param.getMaxDepth());
		getSliderThreads().setValue(param.getThreadCount());
		getDomainsAlwaysInScopeTableModel().setDomainsAlwaysInScope(param.getDomainsAlwaysInScope());
		getDomainsAlwaysInScopePanel().setRemoveWithoutConfirmation(param.isConfirmRemoveDomainAlwaysInScope());
		getChkProcessForm().setSelected(param.isProcessForm());
		getChkPostForm().setSelected(param.isPostForm());
		getChkParseComments().setSelected(param.isParseComments());
		getChkParseRobotsTxt().setSelected(param.isParseRobotsTxt());
		getChkParseSVNEntries().setSelected(param.isParseSVNEntries());
		getChkParseGit().setSelected(param.isParseGit());
		getComboHandleParameters().setSelectedItem(param.getHandleParameters());
		getHandleODataSpecificParameters().setSelected(param.isHandleODataParametersVisited());

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
		param.setDomainsAlwaysInScope(getDomainsAlwaysInScopeTableModel().getDomainsAlwaysInScope());
		param.setConfirmRemoveDomainAlwaysInScope(getDomainsAlwaysInScopePanel().isRemoveWithoutConfirmation());
		param.setProcessForm(getChkProcessForm().isSelected());
		param.setPostForm(getChkPostForm().isSelected());
		param.setParseComments(getChkParseComments().isSelected());
		param.setParseRobotsTxt(getChkParseRobotsTxt().isSelected());
		param.setParseSVNEntries(getChkParseSVNEntries().isSelected());
		param.setParseGit(getChkParseGit().isSelected());
		param.setHandleParameters((HandleParametersOption) getComboHandleParameters().getSelectedItem());
		param.setHandleODataParametersVisited(getHandleODataSpecificParameters().isSelected());
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
			sliderThreads = new PositiveValuesSlider(Constant.MAX_THREADS_PER_SCAN);
		}
		return sliderThreads;
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

			if (!getChkProcessForm().isSelected()){
				chkPostForm.setEnabled(false);
			}
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
					if (chkProcessForm.isSelected()) {
						chkPostForm.setEnabled(true);
					} else {
						chkPostForm.setEnabled(false);
					}
				}
			});
		}
		return chkProcessForm;
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
	 * This method initializes the Parse "SVN Entries" checkbox.
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getChkParseSVNEntries() {
		if (parseSVNEntries == null) {
			parseSVNEntries = new JCheckBox();
			parseSVNEntries.setText(Constant.messages.getString("spider.options.label.svnentries"));
		}
		return parseSVNEntries;
	}

	/**
	 * This method initializes the Parse "Git" checkbox.
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getChkParseGit() {
		if (parseGit == null) {
			parseGit = new JCheckBox();
			parseGit.setText(Constant.messages.getString("spider.options.label.git"));
		}
		return parseGit;
	}

	/**
	 * This method initializes the Handle OData-specific parameters checkbox.
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getHandleODataSpecificParameters() {
		if (handleODataSpecificParameters == null) {
			handleODataSpecificParameters = new JCheckBox();
			handleODataSpecificParameters.setText(Constant.messages.getString("spider.options.label.handlehodataparameters"));
		}
		return handleODataSpecificParameters;
	}
	
	
	/**
	 * This method initializes the combobox for HandleParameters option.
	 * 
	 * @return the combo handle parameters
	 */
	@SuppressWarnings("unchecked")
	private JComboBox<HandleParametersOption> getComboHandleParameters() {
		if (handleParameters == null) {
			handleParameters = new JComboBox<>(new HandleParametersOption[] {
					HandleParametersOption.USE_ALL, HandleParametersOption.IGNORE_VALUE,
					HandleParametersOption.IGNORE_COMPLETELY });
			handleParameters.setRenderer(new HandleParametersOptionRenderer());
		}
		return handleParameters;
	}

	private DomainsAlwaysInScopeMultipleOptionsPanel getDomainsAlwaysInScopePanel() {
		if (domainsAlwaysInScopePanel == null) {
			domainsAlwaysInScopePanel = new DomainsAlwaysInScopeMultipleOptionsPanel(getDomainsAlwaysInScopeTableModel());
		}
		return domainsAlwaysInScopePanel;
	}

	private DomainsAlwaysInScopeTableModel getDomainsAlwaysInScopeTableModel() {
		if (domainsAlwaysInScopeTableModel == null) {
			domainsAlwaysInScopeTableModel = new DomainsAlwaysInScopeTableModel();
		}
		return domainsAlwaysInScopeTableModel;
	}

	/**
	 * A renderer for properly displaying the name of the HandleParametersOptions in a ComboBox.
	 */
	private static class HandleParametersOptionRenderer extends BasicComboBoxRenderer {
		private static final long serialVersionUID = 3654541772447187317L;
		private static final Border BORDER = new EmptyBorder(2, 3, 3, 3);

		@Override
		@SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null) {
				setBorder(BORDER);
				HandleParametersOption item = (HandleParametersOption) value;
				setText(item.getName());
			}
			return this;
		}
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

	private static class DomainsAlwaysInScopeMultipleOptionsPanel extends
			AbstractMultipleOptionsTablePanel<DomainAlwaysInScopeMatcher> {

		private static final long serialVersionUID = 2332044353650231701L;

		private static final String REMOVE_DIALOG_TITLE = Constant.messages.getString("spider.options.domains.in.scope.dialog.remove.title");
		private static final String REMOVE_DIALOG_TEXT = Constant.messages.getString("spider.options.domains.in.scope.dialog.remove.text");

		private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = Constant.messages.getString("spider.options.domains.in.scope.dialog.remove.button.confirm");
		private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = Constant.messages.getString("spider.options.domains.in.scope.dialog.remove.button.cancel");

		private static final String REMOVE_DIALOG_CHECKBOX_LABEL = Constant.messages.getString("spider.options.domains.in.scope.dialog.remove.checkbox.label");

		private DialogAddDomainAlwaysInScope addDialog = null;
		private DialogModifyDomainAlwaysInScope modifyDialog = null;

		public DomainsAlwaysInScopeMultipleOptionsPanel(DomainsAlwaysInScopeTableModel model) {
			super(model);

			getTable().setVisibleRowCount(5);
			getTable().setSortOrder(2, SortOrder.ASCENDING);
		}

		@Override
		public DomainAlwaysInScopeMatcher showAddDialogue() {
			if (addDialog == null) {
				addDialog = new DialogAddDomainAlwaysInScope(View.getSingleton().getOptionsDialog(null));
				addDialog.pack();
			}
			addDialog.setVisible(true);

			DomainAlwaysInScopeMatcher hostAuthentication = addDialog.getDomainAlwaysInScope();
			addDialog.clear();

			return hostAuthentication;
		}

		@Override
		public DomainAlwaysInScopeMatcher showModifyDialogue(DomainAlwaysInScopeMatcher e) {
			if (modifyDialog == null) {
				modifyDialog = new DialogModifyDomainAlwaysInScope(View.getSingleton().getOptionsDialog(null));
				modifyDialog.pack();
			}
			modifyDialog.setDomainAlwaysInScope(e);
			modifyDialog.setVisible(true);

			DomainAlwaysInScopeMatcher excludedDomain = modifyDialog.getDomainAlwaysInScope();
			modifyDialog.clear();

			if (!excludedDomain.equals(e)) {
				return excludedDomain;
			}

			return null;
		}

		@Override
		public boolean showRemoveDialogue(DomainAlwaysInScopeMatcher e) {
			JCheckBox removeWithoutConfirmationCheckBox = new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
			Object[] messages = { REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox };
			int option = JOptionPane.showOptionDialog(
					View.getSingleton().getMainFrame(),
					messages,
					REMOVE_DIALOG_TITLE,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					new String[] { REMOVE_DIALOG_CONFIRM_BUTTON_LABEL, REMOVE_DIALOG_CANCEL_BUTTON_LABEL },
					null);

			if (option == JOptionPane.OK_OPTION) {
				setRemoveWithoutConfirmation(removeWithoutConfirmationCheckBox.isSelected());

				return true;
			}

			return false;
		}
	}

}

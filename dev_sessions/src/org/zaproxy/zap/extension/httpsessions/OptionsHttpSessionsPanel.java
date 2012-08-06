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
package org.zaproxy.zap.extension.httpsessions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;

/**
 * The OptionsHttpSessionsPanel is used to display and allow the users to modify the settings
 * regarding the behaviour of {@link ExtensionHttpSessions}.
 */
public class OptionsHttpSessionsPanel extends AbstractParamPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The table default tokens. */
	private JTable tableDefaultTokens = null;

	/** The scroll pane for efault tokens. */
	private JScrollPane scrollPaneDefaultTokens = null;

	/** The default session tokens model. */
	private OptionsHttpSessionsTableModel defaultTokensModel = null;

	/**
	 * Instantiates a new options panel for http sessions.
	 */
	public OptionsHttpSessionsPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this panel.
	 */
	private void initialize() {
		this.setLayout(new GridBagLayout());
		this.setSize(409, 268);
		this.setName(Constant.messages.getString("options.session.title"));

		JLabel tokenNamesLabel = new JLabel();
		tokenNamesLabel.setText(Constant.messages.getString("options.session.label.tokens"));
		tokenNamesLabel.setPreferredSize(new java.awt.Dimension(494, 25));
		tokenNamesLabel.setMinimumSize(new java.awt.Dimension(494, 25));

		GridBagConstraints tokenNamesScrollGridBag = new GridBagConstraints();
		GridBagConstraints tokenNamesLableGridBag = new GridBagConstraints();

		tokenNamesLableGridBag.gridx = 0;
		tokenNamesLableGridBag.gridy = 0;
		tokenNamesLableGridBag.gridheight = 1;
		tokenNamesLableGridBag.ipady = 25;
		tokenNamesLableGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
		tokenNamesLableGridBag.anchor = java.awt.GridBagConstraints.NORTHWEST;
		tokenNamesLableGridBag.fill = java.awt.GridBagConstraints.HORIZONTAL;

		tokenNamesScrollGridBag.gridx = 0;
		tokenNamesScrollGridBag.gridy = 1;
		tokenNamesScrollGridBag.weightx = 1.0;
		tokenNamesScrollGridBag.weighty = 1.0;
		tokenNamesScrollGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
		tokenNamesScrollGridBag.anchor = java.awt.GridBagConstraints.NORTHWEST;
		tokenNamesScrollGridBag.fill = java.awt.GridBagConstraints.BOTH;

		this.add(tokenNamesLabel, tokenNamesLableGridBag);
		this.add(getDefaultTokensScrollPane(), tokenNamesScrollGridBag);
	}

	/* (non-Javadoc)
	 * 
	 * @see org.parosproxy.paros.view.AbstractParamPanel#initParam(java.lang.Object) */
	@Override
	public void initParam(Object obj) {
		// Initialize the default token names
		OptionsParam optionsParam = (OptionsParam) obj;
		HttpSessionsParam param = optionsParam.getHttpSessionsParam();
		getDefaultTokensModel().setTokens(param.getDefaultTokens());
	}

	/* (non-Javadoc)
	 * 
	 * @see org.parosproxy.paros.view.AbstractParamPanel#validateParam(java.lang.Object) */
	@Override
	public void validateParam(Object obj) throws Exception {
	}

	/* (non-Javadoc)
	 * 
	 * @see org.parosproxy.paros.view.AbstractParamPanel#saveParam(java.lang.Object) */
	@Override
	public void saveParam(Object obj) throws Exception {
		OptionsParam optionsParam = (OptionsParam) obj;
		HttpSessionsParam sessionParam = optionsParam.getHttpSessionsParam();
		sessionParam.setDefaultTokens(getDefaultTokensModel().getTokens());
	}

	/**
	 * Gets the table for default tokens.
	 * 
	 * @return the table for default tokens
	 */
	private JTable getDefaultTokensTable() {
		if (tableDefaultTokens == null) {
			tableDefaultTokens = new JTable();
			tableDefaultTokens.setModel(getDefaultTokensModel());
			tableDefaultTokens.setRowHeight(18);
		}
		return tableDefaultTokens;
	}

	/**
	 * Gets the token names' scroll pane.
	 * 
	 * @return the token names' scroll pane
	 */
	private JScrollPane getDefaultTokensScrollPane() {
		if (scrollPaneDefaultTokens == null) {
			scrollPaneDefaultTokens = new JScrollPane();
			scrollPaneDefaultTokens.setViewportView(getDefaultTokensTable());
			scrollPaneDefaultTokens.setBorder(javax.swing.BorderFactory
					.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		}
		return scrollPaneDefaultTokens;
	}

	/**
	 * Gets the default tokens model.
	 * 
	 * @return the default tokens model
	 */
	private OptionsHttpSessionsTableModel getDefaultTokensModel() {
		if (defaultTokensModel == null) {
			defaultTokensModel = new OptionsHttpSessionsTableModel();
		}
		return defaultTokensModel;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.parosproxy.paros.view.AbstractParamPanel#getHelpIndex() */
	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.session";
	}
}

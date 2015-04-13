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
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SortOrder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;

/**
 * The OptionsHttpSessionsPanel is used to display and allow the users to modify the settings
 * regarding the behaviour of {@link ExtensionHttpSessions}.
 */
public class OptionsHttpSessionsPanel extends AbstractParamPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The proxy only checkbox. */
	private JCheckBox proxyOnlyCheckbox = null;

	private HttpSessionTokensMultipleOptionsPanel tokensOptionsPanel;
	
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
		this.setName(Constant.messages.getString("httpsessions.options.title"));
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		
		JLabel tokenNamesLabel = new JLabel();
		tokenNamesLabel.setText(Constant.messages.getString("httpsessions.options.label.tokens"));

		this.add(tokenNamesLabel, gbc);

		tokensOptionsPanel = new HttpSessionTokensMultipleOptionsPanel(getDefaultTokensModel());
		
		gbc.weighty = 1.0;
		this.add(tokensOptionsPanel, gbc);
		
		gbc.weighty = 0.0;
		gbc.insets = new Insets(10, 2, 2, 2);
		this.add(getChkProxyOnly(), gbc);
	}

	@Override
	public void initParam(Object obj) {
		// Initialize the default token names
		OptionsParam optionsParam = (OptionsParam) obj;
		HttpSessionsParam param = optionsParam.getParamSet(HttpSessionsParam.class);
		getDefaultTokensModel().setTokens(param.getDefaultTokens());
		getChkProxyOnly().setSelected(param.isEnabledProxyOnly());
		tokensOptionsPanel.setRemoveWithoutConfirmation(!param.isConfirmRemoveDefaultToken());
	}

	@Override
	public void validateParam(Object obj) throws Exception {
	}

	@Override
	public void saveParam(Object obj) throws Exception {
		OptionsParam optionsParam = (OptionsParam) obj;
		HttpSessionsParam sessionParam = optionsParam.getParamSet(HttpSessionsParam.class);
		sessionParam.setDefaultTokens(getDefaultTokensModel().getTokens());
		sessionParam.setEnabledProxyOnly(getChkProxyOnly().isSelected());
		sessionParam.setConfirmRemoveDefaultToken(!tokensOptionsPanel.isRemoveWithoutConfirmation());
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

	/**
	 * Gets the chk proxy only.
	 * 
	 * @return the chk proxy only
	 */
	private JCheckBox getChkProxyOnly() {
		if (proxyOnlyCheckbox == null) {
			proxyOnlyCheckbox = new JCheckBox();
			proxyOnlyCheckbox.setText(Constant.messages.getString("httpsessions.options.label.proxyOnly"));
		}
		return proxyOnlyCheckbox;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.httpsessions";
	}
	
	private static class HttpSessionTokensMultipleOptionsPanel extends AbstractMultipleOptionsTablePanel<HttpSessionToken> {

		private static final long serialVersionUID = -512878859657091461L;
		
		private static final String REMOVE_DIALOG_TITLE = Constant.messages.getString("httpsessions.options.dialog.token.remove.title");
		private static final String REMOVE_DIALOG_TEXT = Constant.messages.getString("httpsessions.options.dialog.token.remove.text");
		
		private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = Constant.messages.getString("httpsessions.options.dialog.token.remove.button.confirm");
		private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = Constant.messages.getString("httpsessions.options.dialog.token.remove.button.cancel");
		
		private static final String REMOVE_DIALOG_CHECKBOX_LABEL = Constant.messages.getString("httpsessions.options.dialog.token.remove.checkbox.label");
		
		private DialogAddToken addDialog = null;
		private DialogModifyToken modifyDialog = null;
		
		private OptionsHttpSessionsTableModel model;
		
		public HttpSessionTokensMultipleOptionsPanel(OptionsHttpSessionsTableModel model) {
			super(model);
			
			this.model = model;
			
			getTable().getColumnExt(0).setPreferredWidth(20);
			getTable().setSortOrder(1, SortOrder.ASCENDING);
		}

		@Override
		public HttpSessionToken showAddDialogue() {
			if (addDialog == null) {
				addDialog = new DialogAddToken(View.getSingleton().getOptionsDialog(null));
				addDialog.pack();
			}
			addDialog.setTokens(model.getElements());
			addDialog.setVisible(true);
			
			HttpSessionToken token = addDialog.getToken();
			addDialog.clear();
			
			return token;
		}
		
		@Override
		public HttpSessionToken showModifyDialogue(HttpSessionToken e) {
			if (modifyDialog == null) {
				modifyDialog = new DialogModifyToken(View.getSingleton().getOptionsDialog(null));
				modifyDialog.pack();
			}
			modifyDialog.setTokens(model.getElements());
			modifyDialog.setToken(e);
			modifyDialog.setVisible(true);
			
			HttpSessionToken token = modifyDialog.getToken();
			modifyDialog.clear();
			
			if (!token.equals(e)) {
				return token;
			}
			
			return null;
		}
		
		@Override
		public boolean showRemoveDialogue(HttpSessionToken e) {
			JCheckBox removeWithoutConfirmationCheckBox = new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
			Object[] messages = {REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox};
			int option = JOptionPane.showOptionDialog(View.getSingleton().getMainFrame(), messages, REMOVE_DIALOG_TITLE,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, new String[] { REMOVE_DIALOG_CONFIRM_BUTTON_LABEL, REMOVE_DIALOG_CANCEL_BUTTON_LABEL }, null);

			if (option == JOptionPane.OK_OPTION) {
				setRemoveWithoutConfirmation(removeWithoutConfirmationCheckBox.isSelected());
				
				return true;
			}
			
			return false;
		}
	}
}

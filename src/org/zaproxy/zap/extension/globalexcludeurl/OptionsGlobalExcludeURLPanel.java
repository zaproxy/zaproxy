/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * Copyright 2014 Jay Ball - Aspect Security
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
package org.zaproxy.zap.extension.globalexcludeurl;

import java.util.List;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SortOrder;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;

/** TODO The GlobalExcludeURL functionality is currently alpha and subject to change.  */
public class OptionsGlobalExcludeURLPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;

	private AntiCsrfMultipleOptionsPanel tokensOptionsPanel;

	private OptionsGlobalExcludeURLTableModel antiCsrfModel = null;
	
    /**
     * 
     */
    public OptionsGlobalExcludeURLPanel() {
        super();
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setName(Constant.messages.getString("options.globalexcludeurl.title"));
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;

        this.add(new JLabel(Constant.messages.getString("options.globalexcludeurl.label.tokens")), gbc);

        tokensOptionsPanel = new AntiCsrfMultipleOptionsPanel(getAntiCsrfModel());
        
        gbc.weighty = 1.0;
        this.add(tokensOptionsPanel, gbc);
        
        //gbc.weighty = 0.0;
	}

	@Override
    public void initParam(Object obj) {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    GlobalExcludeURLParam param = optionsParam.getGlobalExcludeURLParam();
	    getAntiCsrfModel().setTokens(param.getTokens());
	    tokensOptionsPanel.setRemoveWithoutConfirmation(!param.isConfirmRemoveToken());
    }


    @Override
    public void validateParam(Object obj) throws Exception {

    }

    private static Logger log = Logger.getLogger(OptionsGlobalExcludeURLPanel.class);


    @Override
    public void saveParam(Object obj) throws Exception {

        OptionsParam optionsParam = (OptionsParam) obj;
	    GlobalExcludeURLParam globalExcludeURLParam = optionsParam.getGlobalExcludeURLParam();
	    globalExcludeURLParam.setTokens(getAntiCsrfModel().getElements());
	    globalExcludeURLParam.setConfirmRemoveToken(!tokensOptionsPanel.isRemoveWithoutConfirmation());
	    
	    globalExcludeURLParam.parse();
	    List<String> ignoredRegexs = globalExcludeURLParam.getTokensNames();

	    log.warn(ignoredRegexs.toString());
	    Model.getSingleton().getSession().setGlobalExcludeURLRegexs(ignoredRegexs);
	    // after saving, force the proxy/spider/scanner to refresh the URL lists.
	    Model.getSingleton().getSession().forceGlobalExcludeURLRefresh();
	    log.debug("Done saving Global Exclude URL");
    }

	/**
	 * This method initializes authModel	
	 * 	
	 * @return org.parosproxy.paros.view.OptionsAuthenticationTableModel	
	 */    
	private OptionsGlobalExcludeURLTableModel getAntiCsrfModel() {
		if (antiCsrfModel == null) {
			antiCsrfModel = new OptionsGlobalExcludeURLTableModel();
		}
		return antiCsrfModel;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.anticsrf";  // FIXME language link
	}

	private static class AntiCsrfMultipleOptionsPanel extends AbstractMultipleOptionsTablePanel<GlobalExcludeURLParamToken> {
        
        private static final long serialVersionUID = -115340627058929308L;
        
        private static final String REMOVE_DIALOG_TITLE = Constant.messages.getString("options.globalexcludeurl.dialog.token.remove.title");
	    private static final String REMOVE_DIALOG_TEXT = Constant.messages.getString("options.globalexcludeurl.dialog.token.remove.text");
	    
	    private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = Constant.messages.getString("options.globalexcludeurl.dialog.token.remove.button.confirm");
	    private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = Constant.messages.getString("options.globalexcludeurl.dialog.token.remove.button.cancel");
	    
	    private static final String REMOVE_DIALOG_CHECKBOX_LABEL = Constant.messages.getString("options.globalexcludeurl.dialog.token.remove.checkbox.label");
	    
	    private DialogAddToken addDialog = null;
        private DialogModifyToken modifyDialog = null;
        
        private OptionsGlobalExcludeURLTableModel model;
        
        public AntiCsrfMultipleOptionsPanel(OptionsGlobalExcludeURLTableModel model) {
            super(model);
            
            this.model = model;
            
            getTable().getColumnExt(0).setPreferredWidth(20);
            getTable().setSortOrder(1, SortOrder.ASCENDING);
        }

        @Override
        public GlobalExcludeURLParamToken showAddDialogue() {
            if (addDialog == null) {
                addDialog = new DialogAddToken(View.getSingleton().getOptionsDialog(null));
                addDialog.pack();
            }
            addDialog.setTokens(model.getElements());
            addDialog.setVisible(true);
            
            GlobalExcludeURLParamToken token = addDialog.getToken();
            addDialog.clear();
            
            return token;
        }
        
        @Override
        public GlobalExcludeURLParamToken showModifyDialogue(GlobalExcludeURLParamToken e) {
            if (modifyDialog == null) {
                modifyDialog = new DialogModifyToken(View.getSingleton().getOptionsDialog(null));
                modifyDialog.pack();
            }
            modifyDialog.setTokens(model.getElements());
            modifyDialog.setToken(e);
            modifyDialog.setVisible(true);
            
            GlobalExcludeURLParamToken token = modifyDialog.getToken();
            modifyDialog.clear();
            
            if (!token.equals(e)) {
                return token;
            }
            
            return null;
        }
        
        @Override
        public boolean showRemoveDialogue(GlobalExcludeURLParamToken e) {
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

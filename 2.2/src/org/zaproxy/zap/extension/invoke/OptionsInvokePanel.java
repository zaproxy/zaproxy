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
package org.zaproxy.zap.extension.invoke;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SortOrder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;

public class OptionsInvokePanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;

    private InvokableAppMultipleOptionsPanel appsOptionsPanel;
    
	private OptionsInvokeTableModel tableModel = null;
	private ExtensionInvoke extension = null;
	
    public OptionsInvokePanel(ExtensionInvoke extension) {
        super();
        this.extension  = extension;
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setName(Constant.messages.getString("invoke.options.title"));
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        
        this.add(new JLabel(Constant.messages.getString("invoke.options.desc1")), gbc);
        this.add(new JLabel(Constant.messages.getString("invoke.options.desc2")), gbc);

        appsOptionsPanel = new InvokableAppMultipleOptionsPanel(getTableModel());
        
        gbc.weighty = 1.0;
        this.add(appsOptionsPanel, gbc);
        
        //gbc.weighty = 0.0;
	}
	
    @Override
    public void initParam(Object obj) {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    InvokeParam invokeParam = optionsParam.getInvokeParam();
	    getTableModel().setListInvokableApps(invokeParam.getListInvoke());
	    appsOptionsPanel.setRemoveWithoutConfirmation(!invokeParam.isConfirmRemoveApp());
    }

    @Override
    public void validateParam(Object obj) throws Exception {

    }
    
    @Override
    public void saveParam(Object obj) throws Exception {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    InvokeParam invokeParam = optionsParam.getInvokeParam();
	    invokeParam.setListInvoke(getTableModel().getListInvokableApps());
	    invokeParam.setConfirmRemoveApp(!appsOptionsPanel.isRemoveWithoutConfirmation());
	    
	    extension.replaceInvokeMenus(invokeParam.getListInvokeEnabled());
    }

	/**
	 * This method initializes authModel	
	 * 	
	 * @return org.parosproxy.paros.view.OptionsAuthenticationTableModel	
	 */    
	private OptionsInvokeTableModel getTableModel() {
		if (tableModel == null) {
			tableModel = new OptionsInvokeTableModel();
		}
		return tableModel;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.invokeapp";
	}

    private static class InvokableAppMultipleOptionsPanel extends AbstractMultipleOptionsTablePanel<InvokableApp> {

        private static final long serialVersionUID = -6794316746694248277L;
        
        private static final String REMOVE_DIALOG_TITLE = Constant.messages.getString("invoke.options.dialog.app.remove.title");
        private static final String REMOVE_DIALOG_TEXT = Constant.messages.getString("invoke.options.dialog.app.remove.text");
        
        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = Constant.messages.getString("invoke.options.dialog.app.remove.button.confirm");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = Constant.messages.getString("invoke.options.dialog.app.remove.button.cancel");
        
        private static final String REMOVE_DIALOG_CHECKBOX_LABEL = Constant.messages.getString("invoke.options.dialog.app.remove.checkbox.label");
        
        private DialogAddApp addDialog = null;
        private DialogModifyApp modifyDialog = null;
        
        private OptionsInvokeTableModel model;
        
        public InvokableAppMultipleOptionsPanel(OptionsInvokeTableModel model) {
            super(model);
            
            this.model = model;
            
            getTable().getColumnExt(0).setPreferredWidth(20);
            getTable().setSortOrder(1, SortOrder.ASCENDING);
        }

        @Override
        public InvokableApp showAddDialogue() {
            if (addDialog == null) {
                addDialog = new DialogAddApp(View.getSingleton().getOptionsDialog(null));
                addDialog.pack();
            }
            addDialog.setApps(model.getElements());
            addDialog.setVisible(true);
            
            InvokableApp app = addDialog.getApp();
            addDialog.clear();
            
            return app;
        }
        
        @Override
        public InvokableApp showModifyDialogue(InvokableApp e) {
            if (modifyDialog == null) {
                modifyDialog = new DialogModifyApp(View.getSingleton().getOptionsDialog(null));
                modifyDialog.pack();
            }
            modifyDialog.setApps(model.getElements());
            modifyDialog.setApp(e);
            modifyDialog.setVisible(true);
            
            InvokableApp app = modifyDialog.getApp();
            modifyDialog.clear();
            
            if (!app.equals(e)) {
                return app;
            }
            
            return null;
        }
        
        @Override
        public boolean showRemoveDialogue(InvokableApp e) {
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

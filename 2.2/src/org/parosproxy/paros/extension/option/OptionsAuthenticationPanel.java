/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/04/16 i18n
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/11/15 Issue 416: Normalise how multiple related options are managed
// throughout ZAP and enhance the usability of some options.
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments

package org.parosproxy.paros.extension.option;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SortOrder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HostAuthentication;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.OptionsAuthenticationTableModel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;

public class OptionsAuthenticationPanel extends AbstractParamPanel {

	private static final long serialVersionUID = -2971474654304050620L;

	private HostAuthenticationMultipleOptionsPanel authsOptionsPanel;

	private OptionsAuthenticationTableModel authModel = null;
	
    /**
     * 
     */
    public OptionsAuthenticationPanel() {
        super();
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
	    this.setName(Constant.messages.getString("options.auth.title"));
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        
        this.add(new JLabel(Constant.messages.getString("options.auth.label.hosts")), gbc);

        authsOptionsPanel = new HostAuthenticationMultipleOptionsPanel(getAuthModel());
        
        gbc.weighty = 1.0;
        this.add(authsOptionsPanel, gbc);
	}
	
    @Override
    public void initParam(Object obj) {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    ConnectionParam connectionParam = optionsParam.getConnectionParam();
	    getAuthModel().setListAuth(connectionParam.getListAuth());
	    authsOptionsPanel.setRemoveWithoutConfirmation(!connectionParam.isConfirmRemoveAuth());
    }

    @Override
    public void validateParam(Object obj) throws Exception {

    }

    @Override
    public void saveParam(Object obj) throws Exception {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    ConnectionParam connectionParam = optionsParam.getConnectionParam();
	    connectionParam.setListAuth(getAuthModel().getListAuth());
	    connectionParam.setConfirmRemoveAuth(!authsOptionsPanel.isRemoveWithoutConfirmation());
    }

	/**
	 * This method initializes authModel	
	 * 	
	 * @return org.parosproxy.paros.view.OptionsAuthenticationTableModel	
	 */    
	private OptionsAuthenticationTableModel getAuthModel() {
		if (authModel == null) {
			authModel = new OptionsAuthenticationTableModel();
		}
		return authModel;
	}

	@Override
	public String getHelpIndex() {
		// ZAP: added help index support
		return "ui.dialogs.options.authentication";
	}

    private static class HostAuthenticationMultipleOptionsPanel extends AbstractMultipleOptionsTablePanel<HostAuthentication> {

        private static final long serialVersionUID = 2332044353650231701L;
        
        private static final String REMOVE_DIALOG_TITLE = Constant.messages.getString("options.auth.dialog.hostAuth.remove.title");
        private static final String REMOVE_DIALOG_TEXT = Constant.messages.getString("options.auth.dialog.hostAuth.remove.text");
        
        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = Constant.messages.getString("options.auth.dialog.hostAuth.remove.button.confirm");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = Constant.messages.getString("options.auth.dialog.hostAuth.remove.button.cancel");
        
        private static final String REMOVE_DIALOG_CHECKBOX_LABEL = Constant.messages.getString("options.auth.dialog.hostAuth.remove.checkbox.label");
        
        private DialogAddHostAuthentication addDialog = null;
        private DialogModifyHostAuthentication modifyDialog = null;
        
        private OptionsAuthenticationTableModel model;
        
        public HostAuthenticationMultipleOptionsPanel(OptionsAuthenticationTableModel model) {
            super(model);
            
            this.model = model;
            
            getTable().getColumnExt(0).setPreferredWidth(20);
            getTable().setSortOrder(1, SortOrder.ASCENDING);
        }

        @Override
        public HostAuthentication showAddDialogue() {
            if (addDialog == null) {
                addDialog = new DialogAddHostAuthentication(View.getSingleton().getOptionsDialog(null));
                addDialog.pack();
            }
            addDialog.setAuthentications(model.getElements());
            addDialog.setVisible(true);
            
            HostAuthentication hostAuthentication = addDialog.getToken();
            addDialog.clear();
            
            return hostAuthentication;
        }
        
        @Override
        public HostAuthentication showModifyDialogue(HostAuthentication e) {
            if (modifyDialog == null) {
                modifyDialog = new DialogModifyHostAuthentication(View.getSingleton().getOptionsDialog(null));
                modifyDialog.pack();
            }
            modifyDialog.setAuthentications(model.getElements());
            modifyDialog.setHostAuthentication(e);
            modifyDialog.setVisible(true);
            
            HostAuthentication hostAuthentication = modifyDialog.getToken();
            modifyDialog.clear();
            
            if (!hostAuthentication.equals(e)) {
                return hostAuthentication;
            }
            
            return null;
        }
        
        @Override
        public boolean showRemoveDialogue(HostAuthentication e) {
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

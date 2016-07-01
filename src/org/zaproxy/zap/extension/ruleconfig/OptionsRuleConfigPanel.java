/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 The ZAP Development team
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
package org.zaproxy.zap.extension.ruleconfig;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.MultipleOptionsTablePanel;

public class OptionsRuleConfigPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(OptionsRuleConfigPanel.class);

    private ExtensionRuleConfig extension;
    private RuleConfigOptionsPanel ruleConfigOptionsPanel;
    private JButton resetButton;

    private RuleConfigTableModel ruleConfigModel;
    
    public OptionsRuleConfigPanel(ExtensionRuleConfig extension) {
        super();
        this.extension = extension;
         initialize();
    }

    private void initialize() {
        this.setName(Constant.messages.getString("ruleconfig.options.title"));
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;

        ruleConfigOptionsPanel = new RuleConfigOptionsPanel(getRuleConfigModel());
        
        gbc.weighty = 1.0;
        this.add(ruleConfigOptionsPanel, 
                LayoutHelper.getGBC(0, 0, 5, 1.0D, 1.0D, GridBagConstraints.BOTH, 
                        GridBagConstraints.LINE_START, null));
        
        this.add(new JLabel(), LayoutHelper.getGBC(0, 1, 1, 0.5D, 0));    // Spacer
        this.add(getResetButton(), LayoutHelper.getGBC(3, 1, 1, 0, 0));
        this.add(new JLabel(), LayoutHelper.getGBC(4, 1, 1, 0.5D, 0));    // Spacer
        
    }

    @Override
    public void initParam(Object obj) {
        this.getRuleConfigModel().setRuleConfigs(extension.getAllRuleConfigs());
        ruleConfigOptionsPanel.packAll();
    }

    @Override
    public void validateParam(Object obj) throws Exception {
        // Nothing to do
    }

    private JButton getResetButton() {
        if (resetButton == null) {
            resetButton = new JButton(Constant.messages.getString("ruleconfig.options.button.reset"));
            resetButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (RuleConfig rc : getRuleConfigModel().getElements()) {
                        rc.reset();
                    }
                    getRuleConfigModel().fireTableDataChanged();
                }});
        }
        return resetButton;
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        for (RuleConfig rc : getRuleConfigModel().getElements()) {
            if (rc.isChanged()) {
                logger.debug("Setting rule config " + rc.getKey() + " to " + rc.getValue());
                extension.setRuleConfigValue(rc.getKey(), rc.getValue());
            }
        }
    }

    private RuleConfigTableModel getRuleConfigModel() {
        if (ruleConfigModel == null) {
            ruleConfigModel = new RuleConfigTableModel();
        }
        return ruleConfigModel;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.ruleconfig";
    }

    private static class RuleConfigOptionsPanel extends MultipleOptionsTablePanel {
        
        private static final long serialVersionUID = -115340627058929308L;
        
        private DialogEditRuleConfig modifyDialog = null;
        
        private RuleConfigTableModel model;
        
        public RuleConfigOptionsPanel(final RuleConfigTableModel model) {
            super(model);
            
            this.model = model;

            // Sort on the key names
            getTable().setSortOrder(0, SortOrder.ASCENDING);
            
            getTable().addMouseListener(new java.awt.event.MouseAdapter() { 
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {

                    if (SwingUtilities.isLeftMouseButton(e)) {
                        int row = getTable().getSelectedRow();
                        if (row >= 0) {
                            // This is just a single click
                            showModifyDialogue(model.getElements().get(getTable().convertRowIndexToModel(row)));
                        }
                    }

                }
            });
        }
        
        protected void packAll() {
            getTable().packAll();
        }

        public void showModifyDialogue(RuleConfig rc) {
            if (modifyDialog == null) {
                modifyDialog = new DialogEditRuleConfig(
                        View.getSingleton().getOptionsDialog(null));
                modifyDialog.pack();
            }
            modifyDialog.init(rc, model);
            modifyDialog.setVisible(true);
        }
        
    }

}

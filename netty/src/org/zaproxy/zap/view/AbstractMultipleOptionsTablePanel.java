/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.Enableable;

public abstract class AbstractMultipleOptionsTablePanel<E extends Enableable> extends MultipleOptionsTablePanel {

    private static final long serialVersionUID = -7609757285865562636L;
    
    private static final String ADD_BUTTON_LABEL = Constant.messages.getString("multiple.options.panel.add.button.label");
    private static final String MODIFY_BUTTON_LABEL = Constant.messages.getString("multiple.options.panel.modify.button.label");
    private static final String REMOVE_BUTTON_LABEL = Constant.messages.getString("multiple.options.panel.remove.button.label");
    
    private static final String ENABLE_ALL_BUTTON_LABEL = Constant.messages.getString("multiple.options.panel.enableAll.button.label");
    private static final String DISABLE_ALL_BUTTON_LABEL = Constant.messages.getString("multiple.options.panel.disableAll.button.label");

    private static final String REMOVE_WO_CONFIRMATION_CHECKBOX_LABEL = Constant.messages.getString("multiple.options.panel.removeWithoutConfirmation.checkbox.label");

    private JButton addButton;

    private JButton modifyButton;

    private JButton removeButton;
    
    private JButton enableAllButton;
    
    private JButton disableAllButton;

    private AbstractMultipleOptionsTableModel<E> model;
    
    private GridBagConstraints gbcFooterPanel;
    
    private JCheckBox removeWithoutConfirmationCheckBox;
    
    public AbstractMultipleOptionsTablePanel(AbstractMultipleOptionsTableModel<E> model) {
        super(model);
        
        getFooterPanel().setLayout(new GridBagLayout());
        
        gbcFooterPanel = new GridBagConstraints();
        gbcFooterPanel.gridx = 0;
        gbcFooterPanel.weightx = 1.0D;
        gbcFooterPanel.weighty = 1.0D;
        gbcFooterPanel.anchor = GridBagConstraints.LINE_START;
        
        addFooterPanelComponent(getRemoveWithoutConfirmationCheckBox());
        
        this.model = model;
        
        addButton = new JButton(ADD_BUTTON_LABEL);
        addButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                E e = showAddDialogue();
                
                if (e != null) {
                    getMultipleOptionsModel().addElement(e);
                }
            }
        });
        
        modifyButton = new JButton(MODIFY_BUTTON_LABEL);
        modifyButton.setEnabled(false);
        modifyButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                int row = getSelectedRow();
                
                E e = showModifyDialogue(getMultipleOptionsModel().getElement(row));
                if (e != null) {
                    getMultipleOptionsModel().modifyElement(row, e);
                }
            }
        });
        
        removeButton = new JButton(REMOVE_BUTTON_LABEL);
        removeButton.setEnabled(false);
        removeButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                int row = getSelectedRow();
                
                if (!isRemoveWithoutConfirmation()) {
                    if (!showRemoveDialogue(getMultipleOptionsModel().getElement(row))) {
                        return;
                    }
                }
                
                getMultipleOptionsModel().removeElement(row);
            }
        });
        
        final boolean buttonsEnabled = getModel().getRowCount() > 0;
        
        enableAllButton = new JButton(ENABLE_ALL_BUTTON_LABEL);
        enableAllButton.setEnabled(buttonsEnabled);
        enableAllButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                getMultipleOptionsModel().setAllEnabled(true);
            }
        });
        
        disableAllButton = new JButton(DISABLE_ALL_BUTTON_LABEL);
        disableAllButton.setEnabled(buttonsEnabled);
        disableAllButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                getMultipleOptionsModel().setAllEnabled(false);
            }
        });
        
        addButton(addButton);
        addButton(modifyButton);
        addButton(removeButton);
        
        addButtonSpacer();
        
        addButton(enableAllButton);
        addButton(disableAllButton);
        
        getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    boolean enabled = getTable().getSelectionModel().getMinSelectionIndex() >= 0;
                    
                    modifyButton.setEnabled(enabled);
                    removeButton.setEnabled(enabled);
                }
            }
        });
        
        getModel().addTableModelListener(new TableModelListener() {
            
            @Override
            public void tableChanged(TableModelEvent e) {
                if (TableModelEvent.ALL_COLUMNS == e.getColumn() ||
                    TableModelEvent.INSERT == e.getType() ||
                    TableModelEvent.DELETE == e.getType()) {
                    boolean enabled = getModel().getRowCount() > 0;

                    enableAllButton.setEnabled(enabled);
                    disableAllButton.setEnabled(enabled);
                }
            }
        });
    }
    
    public void addFooterPanelComponent(JComponent component) {
        getFooterPanel().add(component, gbcFooterPanel);
    }
    
    public boolean isRemoveWithoutConfirmation() {
        return getRemoveWithoutConfirmationCheckBox().isSelected();
    }
    
    public void setRemoveWithoutConfirmation(boolean enabled) {
        getRemoveWithoutConfirmationCheckBox().setSelected(enabled);
    }
    
    protected String getRemoveWithoutConfirmationLabel() {
        return REMOVE_WO_CONFIRMATION_CHECKBOX_LABEL;
    }
    
    private JCheckBox getRemoveWithoutConfirmationCheckBox() {
        if (removeWithoutConfirmationCheckBox == null) {
            removeWithoutConfirmationCheckBox = new JCheckBox(getRemoveWithoutConfirmationLabel());
        }
        return removeWithoutConfirmationCheckBox;
    }
    
    private int getSelectedRow() {
        return getTable().convertRowIndexToModel(getTable().getSelectionModel().getMinSelectionIndex());
    }
    
    private AbstractMultipleOptionsTableModel<E> getMultipleOptionsModel() {
        return model;
    }
    
    public abstract E showAddDialogue();
    
    public abstract E showModifyDialogue(E e);
    
    public abstract boolean showRemoveDialogue(E e);

}

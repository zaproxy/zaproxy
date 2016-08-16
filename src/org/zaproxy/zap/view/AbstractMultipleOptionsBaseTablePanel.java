/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.parosproxy.paros.Constant;

/**
 *
 * @author yhawke (2014)
 * @param <E> the type of the options
 */
public abstract class AbstractMultipleOptionsBaseTablePanel<E> extends MultipleOptionsTablePanel {
    
    protected static final long serialVersionUID = -7609757285865562636L;
    
    protected static final String ADD_BUTTON_LABEL = Constant.messages.getString("multiple.options.panel.add.button.label");
    protected static final String MODIFY_BUTTON_LABEL = Constant.messages.getString("multiple.options.panel.modify.button.label");
    protected static final String REMOVE_BUTTON_LABEL = Constant.messages.getString("multiple.options.panel.remove.button.label");
    protected static final String REMOVE_WO_CONFIRMATION_CHECKBOX_LABEL = Constant.messages.getString("multiple.options.panel.removeWithoutConfirmation.checkbox.label");
    
    protected JButton addButton;
    protected JButton modifyButton;
    protected JButton removeButton;
    
    protected AbstractMultipleOptionsBaseTableModel<E> model;
    protected GridBagConstraints gbcFooterPanel;
    protected JCheckBox removeWithoutConfirmationCheckBox;

    public AbstractMultipleOptionsBaseTablePanel(AbstractMultipleOptionsBaseTableModel<E> model) {
        this(model, true);
    }

    protected AbstractMultipleOptionsBaseTablePanel(AbstractMultipleOptionsBaseTableModel<E> model, boolean allowModification) {
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
        
        if (allowModification) {
            modifyButton = new JButton(MODIFY_BUTTON_LABEL);
            modifyButton.setEnabled(false);
            modifyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                	modifyElement(getSelectedRow());
                }
            });
        }
        
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
        
        addButton(addButton);
        if (allowModification) {
            addButton(modifyButton);
        }
        addButton(removeButton);
                
        getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    selectionChanged(getTable().getSelectionModel().getMinSelectionIndex() >= 0);
                }
            }
        });
        
        if (allowModification) {
	        getTable().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent me) {
		        	// Bring up the modify dialog if the user double clicks on a row
			        if (me.getClickCount() == 2 && modifyButton != null && modifyButton.isEnabled()) {
						JXTable table =(JXTable) me.getSource();
				        Point p = me.getPoint();
				        int row = table.rowAtPoint(p);
				        if (row >= 0) {
				            modifyElement(getTable().convertRowIndexToModel(row));
				        }
			        }
				}});
        }
    }
    
    private void modifyElement(int row) {
        E e = showModifyDialogue(getMultipleOptionsModel().getElement(row));
        if (e != null) {
            getMultipleOptionsModel().modifyElement(row, e);
        }
    }
    
    protected void selectionChanged(boolean entrySelected) {
        if (modifyButton != null) {
            modifyButton.setEnabled(entrySelected);
        }
        removeButton.setEnabled(entrySelected);
    }

    public AbstractMultipleOptionsBaseTablePanel(TableModel model) {
        super(model);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to also enable/disable the added buttons ("add", "modify", "remove" and "remove without confirmation").
     * </p>
     */
    @Override
    public void setComponentEnabled(boolean enabled) {
        super.setComponentEnabled(enabled);

        addButton.setEnabled(enabled);
        removeWithoutConfirmationCheckBox.setEnabled(enabled);

        boolean enable = enabled && getTable().getSelectionModel().getMinSelectionIndex() >= 0;
        if (modifyButton != null) {
            modifyButton.setEnabled(enable);
        }
        removeButton.setEnabled(enable);
    }

    public final void addFooterPanelComponent(JComponent component) {
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

    protected final JCheckBox getRemoveWithoutConfirmationCheckBox() {
        if (removeWithoutConfirmationCheckBox == null) {
            removeWithoutConfirmationCheckBox = new JCheckBox(getRemoveWithoutConfirmationLabel());
        }
        return removeWithoutConfirmationCheckBox;
    }

    protected int getSelectedRow() {
        int selectedRow = getTable().getSelectionModel().getMinSelectionIndex();
        if (selectedRow == -1) {
            return -1;
        }
        return getTable().convertRowIndexToModel(selectedRow);
    }

    protected AbstractMultipleOptionsBaseTableModel<E> getMultipleOptionsModel() {
        return model;
    }

    public abstract E showAddDialogue();

    public abstract E showModifyDialogue(E e);

    public abstract boolean showRemoveDialogue(E e);    
}

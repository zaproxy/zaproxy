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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.Enableable;

public abstract class AbstractMultipleOptionsTablePanel<E extends Enableable> extends AbstractMultipleOptionsBaseTablePanel<E> {
    
    private static final long serialVersionUID = 1L;
    private static final String ENABLE_ALL_BUTTON_LABEL = Constant.messages.getString("multiple.options.panel.enableAll.button.label");
    private static final String DISABLE_ALL_BUTTON_LABEL = Constant.messages.getString("multiple.options.panel.disableAll.button.label");
    
    private JButton enableAllButton;    
    private JButton disableAllButton;
    
    public AbstractMultipleOptionsTablePanel(AbstractMultipleOptionsTableModel<E> model) {
        super(model);
        
        final boolean buttonsEnabled = getModel().getRowCount() > 0;
        
        enableAllButton = new JButton(ENABLE_ALL_BUTTON_LABEL);
        enableAllButton.setEnabled(buttonsEnabled);
        enableAllButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                ((AbstractMultipleOptionsTableModel<?>)getMultipleOptionsModel()).setAllEnabled(true);
            }
        });
        
        disableAllButton = new JButton(DISABLE_ALL_BUTTON_LABEL);
        disableAllButton.setEnabled(buttonsEnabled);
        disableAllButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                ((AbstractMultipleOptionsTableModel<?>)getMultipleOptionsModel()).setAllEnabled(false);
            }
        });
        
        addButtonSpacer();
        
        addButton(enableAllButton);
        addButton(disableAllButton);
        
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

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to also enable/disable the added buttons ("enable all" and "disable all").
     * </p>
     */
    @Override
    public void setComponentEnabled(boolean enabled) {
        super.setComponentEnabled(enabled);

        boolean enable = enabled && getModel().getRowCount() > 0;
        enableAllButton.setEnabled(enable);
        disableAllButton.setEnabled(enable);
    }
}

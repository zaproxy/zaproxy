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
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods and
// removed unnecessary cast.
// ZAP: 2012/07/09 Update row in UI after editing its properties.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
package org.parosproxy.paros.extension.filter;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

class AllFilterTableEditor extends AbstractCellEditor implements TableCellEditor {
	private static final long serialVersionUID = 1L;
    
    // This is the component that will handle the editing of the cell value
    private JButton button = null;
    private int row = 0;
    private AllFilterTableModel model = null;
    AllFilterTableEditor(AllFilterTableModel model) {
        this.model = model;
        button = new JButton();
		button.addActionListener(new java.awt.event.ActionListener() { 

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {    

				// ZAP: Removed unnecessary cast.
				Filter filter = AllFilterTableEditor.this.model.getAllFilters().get(row);
				filter.editProperty();
				
				// ZAP: filter might be enabled or disabled after editing properties
				// change has to be propagated to the checkbox in the corresponding row
				AllFilterTableEditor.this.model.fireTableRowsUpdated(row, row);
			}
		});
    }
    
    // This method is called when a cell value is edited by the user.
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int rowIndex, int vColIndex) {
        // 'value' is value contained in the cell located at (rowIndex, vColIndex)
        
        if (isSelected) {
            // cell (and perhaps other cells) are selected
        }
        
        // Configure the component with the specified value
        button.setText((String)value);

        row = rowIndex;
        
        // Return the configured component
        return button;
    }
    
    // This method is called when editing is completed.
    // It must return the new value to be stored in the cell.
    @Override
    public Object getCellEditorValue() {
        return button.getText();
    }
}

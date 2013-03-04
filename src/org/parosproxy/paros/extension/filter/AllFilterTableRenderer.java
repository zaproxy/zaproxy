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
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
package org.parosproxy.paros.extension.filter;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

class AllFilterTableRenderer extends JComponent implements TableCellRenderer {
	private static final long serialVersionUID = 1L;
	
    JComponent button = new JButton();
    JComponent label = new JLabel();
    
    AllFilterTableRenderer() {
        super();
    }
  
//  This renderer extends a component. It is used each time a
//  cell must be displayed.

    // This method is called each time a cell in a column
     // using this renderer needs to be rendered.
     @Override
     public Component getTableCellRendererComponent(JTable table, Object value,
             boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
         // 'value' is value contained in the cell located at
         // (rowIndex, vColIndex)

         if (isSelected) {
             // cell (and perhaps other cells) are selected
         }

         if (hasFocus) {
             // this cell is the anchor and the table has the focus
         }

         JComponent result = label;
         // Configure the component with the specified value
         if (!value.toString().equals("")) {
             result = button;
             ((JButton) button).setText(value.toString());
         }

         // Set tool tip if desired
         //setToolTipText((String)value);

         // Since the renderer is a component, return itself
         return result;
     }

     // The following methods override the defaults for performance reasons
     @Override
     public void validate() {}
     @Override
     public void revalidate() {}
     @Override
     protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
     @Override
     public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}

}

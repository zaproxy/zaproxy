/*
 * @(#)TreeTableModelAdapter.java	1.2 98/10/27
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package com.sittinglittleduck.DirBuster.gui.JTableTree;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 * This is a wrapper class takes a TreeTableModel and implements 
 * the table model interface. The implementation is trivial, with 
 * all of the event dispatching support provided by the superclass: 
 * the AbstractTableModel. 
 *
 * @version 1.2 10/27/98
 *
 * @author Philip Milne
 * @author Scott Violet
 */
public class TreeTableModelAdapter extends AbstractTableModel
{
    JTree tree;
    TreeTableModel treeTableModel;

    public TreeTableModelAdapter(TreeTableModel treeTableModel, JTree tree) {
        this.tree = tree;
        this.treeTableModel = treeTableModel;

	tree.addTreeExpansionListener(new TreeExpansionListener() {
	    // Don't use fireTableRowsInserted() here; the selection model
	    // would get updated twice. 
	    public void treeExpanded(TreeExpansionEvent event) {  
	      fireTableDataChanged(); 
	    }
            public void treeCollapsed(TreeExpansionEvent event) {  
	      fireTableDataChanged(); 
	    }
	});

	// Install a TreeModelListener that can update the table when
	// tree changes. We use delayedFireTableDataChanged as we can
	// not be guaranteed the tree will have finished processing
	// the event before us.
        //
        // FIXME we are ignoring the above warning, and trying to do the
        // relevant calculations directly. This may break something
        // but I guess we won't know if we don't try!
	treeTableModel.addTreeModelListener(new TreeModelListener() {
	    public void treeNodesChanged(TreeModelEvent e) {
                int row = TreeTableModelAdapter.this.tree.getRowForPath(e.getTreePath());
                if (row < 0) return; // parent is not visible
                
                // This is painful! Why does the relevant TreePath constructor have to be protected?!
                Object[] children = e.getChildren();
                Object[] path = e.getTreePath().getPath();
                Object[] childPath = new Object[path.length+1];
                System.arraycopy(path, 0, childPath, 0, path.length);
                
                childPath[childPath.length - 1] = children[0];
                TreePath firstChildChanged = new TreePath(childPath);
                int firstRow = TreeTableModelAdapter.this.tree.getRowForPath(firstChildChanged);
                
                childPath[childPath.length - 1] = children[children.length-1];
                TreePath lastChildChanged = new TreePath(childPath);
                int lastRow = TreeTableModelAdapter.this.tree.getRowForPath(lastChildChanged);
                
                if (firstRow * lastRow < 0) System.err.println("First row is " + firstRow + " and last row is " + lastRow);
                if (firstRow < 0 || lastRow < 0) return;
                
                if (e instanceof TreeTableModelEvent && firstRow == lastRow) {
                    int column = ((TreeTableModelEvent) e).getColumn();
                    delayedFireTableCellUpdated(firstRow, column);
                } else {
                    delayedFireTableRowsUpdated(firstRow, lastRow);
                }
	    }

	    public void treeNodesInserted(TreeModelEvent e) {
		delayedFireTableDataChanged();
	    }

	    public void treeNodesRemoved(TreeModelEvent e) {
		delayedFireTableDataChanged();
	    }

	    public void treeStructureChanged(TreeModelEvent e) {
                delayedFireTableStructureChanged();
	    }
	});
    }

    // Wrappers, implementing TableModel interface. 

    public int getColumnCount() {
	return treeTableModel.getColumnCount();
    }

    public String getColumnName(int column) {
	return treeTableModel.getColumnName(column);
    }

    public Class getColumnClass(int column) {
	return treeTableModel.getColumnClass(column);
    }

    public int getRowCount() {
	return tree.getRowCount();
    }

    protected Object nodeForRow(int row) {
	TreePath treePath = tree.getPathForRow(row);
	return treePath.getLastPathComponent();         
    }

    public Object getValueAt(int row, int column) {
	return treeTableModel.getValueAt(nodeForRow(row), column);
    }
    
    public Object getRowNode(int row)
    {
        return nodeForRow(row);
    }

    public boolean isCellEditable(int row, int column) {
         return treeTableModel.isCellEditable(nodeForRow(row), column); 
    }

    public void setValueAt(Object value, int row, int column) {
	treeTableModel.setValueAt(value, nodeForRow(row), column);
    }

    /**
     * Invokes fireTableDataChanged after all the pending events have been
     * processed. SwingUtilities.invokeLater is used to handle this.
     */
    protected void delayedFireTableDataChanged() {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		fireTableDataChanged();
	    }
	});
    }

    /**
     * Invokes fireTableDataChanged after all the pending events have been
     * processed. SwingUtilities.invokeLater is used to handle this.
     */
    protected void delayedFireTableCellUpdated(final int row, final int column) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		fireTableCellUpdated(row, column);
	    }
	});
    }

    /**
     * Invokes fireTableDataChanged after all the pending events have been
     * processed. SwingUtilities.invokeLater is used to handle this.
     */
    protected void delayedFireTableRowsUpdated(final int first, final int last) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		fireTableRowsUpdated(first, last);
	    }
	});
    }

    /**
     * Invokes fireTableDataChanged after all the pending events have been
     * processed. SwingUtilities.invokeLater is used to handle this.
     */
    protected void delayedFireTableStructureChanged() {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		fireTableStructureChanged();
	    }
	});
    }
}


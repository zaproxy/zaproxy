package org.zaproxy.zap.utils;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * It will give the user the ability to hide columns and then re-show them in
 * their last viewed position. This functionality is supported by a popup menu
 * added to the table header of the table. The TableColumnModel is still used to
 * control the view for the table. The manager will invoke the appropriate
 * methods of the TableColumnModel to hide/show columns as required. <br>
 * Code taken from <a href="http://tips4java.wordpress.com/2011/05/08/table-column-manager/">tips4java</a>. Written by Rob
 * Camick, which states free usage: You are free to use and/or modify any or all
 * code posted on the Java Tips Weblog without restriction. A credit in the code
 * comments would be nice, but not in any way mandatory.
 */
public class TableColumnManager implements MouseListener, ActionListener,
		TableColumnModelListener, PropertyChangeListener {
	private JTable table;
	private TableColumnModel columnModel;
	private boolean menuPopup;

	private List<TableColumn> allColumns;

	/**
	 * Convenience constructor for creating a TableColumnManager for a table.
	 * Support for a popup menu on the table header will be enabled.
	 * 
	 * @param table
	 *            the table whose TableColumns will managed.
	 */
	public TableColumnManager(JTable table) {
		this(table, true);
	}

	/**
	 * Create a TableColumnManager for a table.
	 * 
	 * @param table
	 *            the table whose TableColumns will managed.
	 * @param menuPopup
	 *            enable or disable a popup menu to allow the users to manager
	 *            the visibility of TableColumns.
	 */
	public TableColumnManager(JTable table, boolean menuPopup) {
		this.table = table;
		setMenuPopup(menuPopup);

		table.addPropertyChangeListener(this);
		reset();
	}

	/**
	 * Reset the TableColumnManager to only manage the TableColumns that are
	 * currently visible in the table.
	 * 
	 * Generally this method should only be invoked by the TableColumnManager
	 * when the TableModel of the table is changed.
	 */
	public void reset() {
		table.getColumnModel().removeColumnModelListener(this);
		columnModel = table.getColumnModel();
		columnModel.addColumnModelListener(this);

		// Keep a duplicate TableColumns for managing hidden TableColumns

		int count = columnModel.getColumnCount();
		allColumns = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			allColumns.add(columnModel.getColumn(i));
		}
	}

	/**
	 * Get the popup support.
	 * 
	 * @return the popup support
	 */
	public boolean isMenuPopup() {
		return menuPopup;
	}

	/**
	 * Add/remove support for a popup menu to the table header. The popup menu
	 * will give the user control over which columns are visible.
	 * 
	 * @param menuPopup
	 *            when true support for displaying a popup menu is added
	 *            otherwise the popup menu is removed.
	 */
	public void setMenuPopup(boolean menuPopup) {
		table.getTableHeader().removeMouseListener(this);

		if (menuPopup) {
			table.getTableHeader().addMouseListener(this);
		}
		
		this.menuPopup = menuPopup;
	}

	/**
	 * Hide a column from view in the table.
	 * 
	 * @param modelColumn
	 *            the column index from the TableModel of the column to be
	 *            removed
	 */
	public void hideColumn(int modelColumn) {
		int viewColumn = table.convertColumnIndexToView(modelColumn);

		if (viewColumn != -1) {
			TableColumn column = columnModel.getColumn(viewColumn);
			hideColumn(column);
		}
	}

	/**
	 * Hide a column from view in the table.
	 * 
	 * @param columnName
	 *            the column name of the column to be removed
	 */
	public void hideColumn(Object columnName) {
		if (columnName == null) {
			return;
		}

		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			TableColumn column = columnModel.getColumn(i);

			if (columnName.equals(column.getHeaderValue())) {
				hideColumn(column);
				break;
			}
		}
	}

	/**
	 * Hide a column from view in the table.
	 * 
	 * @param column
	 *            the TableColumn to be removed from the TableColumnModel of the
	 *            table
	 */
	public void hideColumn(TableColumn column) {
		if (columnModel.getColumnCount() == 1) {
			return;
		}

		// Ignore changes to the TableColumnModel made by the TableColumnManager

		columnModel.removeColumnModelListener(this);
		columnModel.removeColumn(column);
		columnModel.addColumnModelListener(this);
	}

	/**
	 * Show a hidden column in the table.
	 * 
	 * @param modelColumn
	 *            the column index from the TableModel of the column to be added
	 */
	public void showColumn(int modelColumn) {
		for (TableColumn column : allColumns) {
			if (column.getModelIndex() == modelColumn) {
				showColumn(column);
				break;
			}
		}
	}

	/**
	 * Show a hidden column in the table.
	 * 
	 * @param columnName
	 *            the column name from the TableModel of the column to be added
	 */
	public void showColumn(Object columnName) {
		for (TableColumn column : allColumns) {
			if (column.getHeaderValue().equals(columnName)) {
				showColumn(column);
				break;
			}
		}
	}

	/**
	 * Show a hidden column in the table. The column will be positioned at its
	 * proper place in the view of the table.
	 * 
	 * @param column
	 *            the TableColumn to be shown.
	 */
	private void showColumn(TableColumn column) {
		// Ignore changes to the TableColumnModel made by the TableColumnManager

		columnModel.removeColumnModelListener(this);

		// Add the column to the end of the table

		columnModel.addColumn(column);

		// Move the column to its position before it was hidden.
		// (Multiple columns may be hidden so we need to find the first
		// visible column before this column so the column can be moved
		// to the appropriate position)

		int position = allColumns.indexOf(column);
		int from = columnModel.getColumnCount() - 1;
		int to = 0;

		for (int i = position - 1; i > -1; i--) {
			try {
				TableColumn visibleColumn = allColumns.get(i);
				to = columnModel.getColumnIndex(visibleColumn.getHeaderValue()) + 1;
				break;
			} catch (IllegalArgumentException e) {
			}
		}

		columnModel.moveColumn(from, to);

		columnModel.addColumnModelListener(this);
	}

	//
	// Implement MouseListener
	//
	@Override
	public void mousePressed(MouseEvent e) {
		checkForPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		checkForPopup(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	private void checkForPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			JTableHeader header = (JTableHeader) e.getComponent();
			int column = header.columnAtPoint(e.getPoint());
			showPopup(column);
		}
	}

	/*
	 * Show a popup containing items for all the columns found in the table
	 * column manager. The popup will be displayed below the table header
	 * columns that was clicked.
	 * 
	 * @param index index of the table header column that was clicked
	 */
	private void showPopup(int index) {
		Object headerValue = columnModel.getColumn(index).getHeaderValue();
		int columnCount = columnModel.getColumnCount();
		JPopupMenu popup = new SelectPopupMenu();

		// Create a menu item for all columns managed by the table column
		// manager, checking to see if the column is shown or hidden.

		for (TableColumn tableColumn : allColumns) {
			Object value = tableColumn.getHeaderValue();
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(value.toString());
			item.addActionListener(this);

			try {
				columnModel.getColumnIndex(value);
				item.setSelected(true);

				if (columnCount == 1) {
					item.setEnabled(false);
				}
			} catch (IllegalArgumentException e) {
				item.setSelected(false);
			}

			popup.add(item);

			if (value == headerValue) {
				popup.setSelected(item);
			}
		}

		// Display the popup below the TableHeader

		JTableHeader header = table.getTableHeader();
		Rectangle r = header.getHeaderRect(index);
		popup.show(header, r.x, r.height);
	}

	//
	// Implement ActionListener
	//
	/*
	 * A table column will either be added to the table or removed from the
	 * table depending on the state of the menu item that was clicked.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		JMenuItem item = (JMenuItem) event.getSource();

		if (item.isSelected()) {
			showColumn(item.getText());
		} else {
			hideColumn(item.getText());
		}
	}

	//
	// Implement TableColumnModelListener
	//
	@Override
	public void columnAdded(TableColumnModelEvent e) {
		// A table column was added to the TableColumnModel so we need
		// to update the manager to track this column

		TableColumn column = columnModel.getColumn(e.getToIndex());

		if (!allColumns.contains(column)) {
			allColumns.add(column);
		}
	}

	@Override
	public void columnMoved(TableColumnModelEvent e) {
		if (e.getFromIndex() == e.getToIndex()) {
			return;
		}

		// A table column has been moved one position to the left or right
		// in the view of the table so we need to update the manager to
		// track the new location

		int index = e.getToIndex();
		TableColumn column = columnModel.getColumn(index);
		allColumns.remove(column);

		if (index == 0) {
			allColumns.add(0, column);
		} else {
			index--;
			TableColumn visibleColumn = columnModel.getColumn(index);
			int insertionColumn = allColumns.indexOf(visibleColumn);
			allColumns.add(insertionColumn + 1, column);
		}
	}

	@Override
	public void columnMarginChanged(ChangeEvent e) {
	}

	@Override
	public void columnRemoved(TableColumnModelEvent e) {
	}

	@Override
	public void columnSelectionChanged(ListSelectionEvent e) {
	}

	//
	// Implement PropertyChangeListener
	//
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if ("model".equals(e.getPropertyName())) {
			if (table.getAutoCreateColumnsFromModel()) {
				reset();
			}
		}
	}

	/*
	 * Allows you to select a specific menu item when the popup is displayed.
	 * (ie. this is a bug? fix)
	 */
	class SelectPopupMenu extends JPopupMenu {
		
		private static final long serialVersionUID = 918018121618942657L;

		@Override
		public void setSelected(Component sel) {
			int index = getComponentIndex(sel);
			getSelectionModel().setSelectedIndex(index);
			final MenuElement[] me = new MenuElement[2];
			me[0] = this;
			me[1] = getSubElements()[index];

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					MenuSelectionManager.defaultManager().setSelectedPath(me);
				}
			});
		}
	};
}
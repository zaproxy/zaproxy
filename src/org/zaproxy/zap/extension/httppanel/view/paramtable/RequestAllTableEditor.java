package org.zaproxy.zap.extension.httppanel.view.paramtable;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

class RequestAllTableEditor extends DefaultCellEditor implements TableCellEditor, ActionListener {

	private JComboBox comboBox;
	private int selectedRow = -1;
	private RequestAllTableView view;

	public RequestAllTableEditor(JComboBox comboBox, RequestAllTableView view) {
		super(comboBox);
		this.comboBox = comboBox;
		this.view = view;
		comboBox.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JComboBox cb = (JComboBox) e.getSource();
		view.comboBoxClicked(cb.getSelectedIndex(), selectedRow);
	}

	// Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		return comboBox;
	}

	// Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		selectedRow = row;
		return comboBox;
	}
}

/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.utils;

import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.widgets.WritableFileChooser;
/**
 * A {@code JButton} class to facilitate exporting tables (as shown) to a file (such as CSV).
 * Filters, sorting, column order, and column visibility may all impact the data exported.
 * 
 * @since TODO add version
 */
public class TableExportButton extends JButton {

	private static final long serialVersionUID = 3437613469695367668L;

	private static final Logger LOGGER = Logger.getLogger(TableExportButton.class);
	
	private JTable exportTable = null;
	
	/**
	 * Constructs a {@code TableExportButton} for the given table.
	 * 
	 * @param table the Table for which the data should be exported
	 */
	public TableExportButton(JTable table) {
		super(Constant.messages.getString("export.button.name"));
		setTable(table);
		super.setIcon(new ImageIcon(TableExportButton.class.getResource("/resource/icon/16/115.png")));
		super.addActionListener((new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				WritableFileChooser chooser = new WritableFileChooser(Model.getSingleton().getOptionsParam().getUserDirectory());
				chooser.setSelectedFile(new File(Constant.messages.getString("export.button.default.filename")));
				if (chooser.showSaveDialog(View.getSingleton().getMainFrame()) == WritableFileChooser.APPROVE_OPTION) {
					String file = chooser.getSelectedFile().toString();
					if (!file.endsWith(".csv")) {
						file += ".csv";
					}
					try (CSVPrinter pw = new CSVPrinter(
							Files.newBufferedWriter(chooser.getSelectedFile().toPath(), StandardCharsets.UTF_8),
							CSVFormat.DEFAULT)) {
						pw.printRecord(getColumnNames());
						int rowCount = getTable().getRowCount();
						int colCount = getTable().getColumnCount();
						for (int row = 0; row < rowCount; row++) {
							List<Object> valueOfRow = new ArrayList<Object>();
							for (int col = 0; col < colCount; col++) {
								Object value = getTable().getValueAt(row, col);
								value = value == null ? "" : value;
								valueOfRow.add(value.toString());
							}
							pw.printRecord(valueOfRow);
						}
						JOptionPane.showMessageDialog(View.getSingleton().getMainFrame(),
								Constant.messages.getString("export.button.success"));
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(View.getSingleton().getMainFrame(),
								Constant.messages.getString("export.button.error") + "\n"
										+ ex.getMessage());
						LOGGER.error("Export Failed: " + ex.getMessage(), ex);
					}
				}
			}
		}));
	}

	/**
	 * Gets a {@code List} of (visible) column names for the given table.
	 * 
	 * @return the {@code List} of column names
	 */
	private List<String> getColumnNames() {
		List<String> columnNamesList = new ArrayList<String>();
		for (int col = 0; col < getTable().getColumnCount(); col++) {
			columnNamesList.add(getTable().getColumnModel().getColumn(col).getHeaderValue().toString());
		}
		return columnNamesList;
	}
	
	/**
	 * Gets the Table which this button is associated with.
	 * 
	 * @return the Table this button is associated with
	 */
	private JTable getTable() {
		return exportTable;
	}
	
	/**
	 * Sets the Table this button is for.
	 * 
	 * @param table the Table this button applies to
	 */
	public void setTable(JTable table) {
		this.exportTable = table;
	}
	
}

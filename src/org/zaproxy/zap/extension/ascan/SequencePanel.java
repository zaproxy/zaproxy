/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP development team
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

package org.zaproxy.zap.extension.ascan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.AbstractParamContainerPanel;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

public class SequencePanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;

	private JTable tblSequence;

	//TODO: The following 8 strings, should be internationalized.
	private static final String BTNINCLUDESELECT = "Select all Sequence Scripts";
	private static final String BTNINCLUDEDESELECT = "Deselect all Sequence Scripts";
	
	private static final String TBLSEQHEADER0 = "Script name";
	private static final String TBLSEQHEADER1 = "Include in scan";
	private static final String TBLSEQHEADER2 = "Script";
	
	private static final String HELPSTRING = "ui.dialogs.sequence";

	private JButton btnInclude = null; 
	private JButton btnHelp = null;

	private boolean creating = false;

	
	/**
	 * Creates a new instance of the Sequence Panel,
	 * @param parent The Custom Scan Dialog, where the Sequence Panel will be added.
	 */
	public SequencePanel() {
		creating = true;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JLabel labelTop = new JLabel("Sequences are defined as scripts.");
		GridBagConstraints gbc_labelTop = new GridBagConstraints();
		gbc_labelTop.anchor = GridBagConstraints.NORTHWEST;
		gbc_labelTop.insets = new Insets(15, 15, 5, 0);
		gbc_labelTop.gridx = 0;
		gbc_labelTop.gridy = 0;
		add(labelTop, gbc_labelTop);

		btnInclude = new JButton(BTNINCLUDESELECT);
		btnInclude.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(btnInclude.getText().equals(BTNINCLUDESELECT)) {
					setBooleanColumn(1, true);
					btnInclude.setText(BTNINCLUDEDESELECT);
				}
				else {
					setBooleanColumn(1, false);
					btnInclude.setText(BTNINCLUDESELECT);
				}
			}
		});
		GridBagConstraints gbc_btnInclude = new GridBagConstraints();
		gbc_btnInclude.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnInclude.insets = new Insets(0, 15, 5, 0);
		gbc_btnInclude.gridx = 0;
		gbc_btnInclude.gridy = 1;
		add(btnInclude, gbc_btnInclude);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.anchor = GridBagConstraints.NORTHWEST;
		gbc_scrollPane.gridheight = 3;
		gbc_scrollPane.insets = new Insets(15, 15, 15, 15);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 3;
		add(scrollPane, gbc_scrollPane);

		tblSequence = new JTable();
		
		tblSequence.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
						TBLSEQHEADER0, TBLSEQHEADER1, TBLSEQHEADER2
				}
				) {
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("rawtypes")
			Class[] columnTypes = new Class[] {
				String.class, Boolean.class, ScriptWrapper.class
			};
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				if(column == 0)	{
					return false;
				}
				return super.isCellEditable(row, column);
			}
		});
		tblSequence.getColumnModel().getColumn(0).setPreferredWidth(525);
		tblSequence.getColumnModel().getColumn(0).setMinWidth(25);
		tblSequence.getColumnModel().getColumn(1).setPreferredWidth(100);
		tblSequence.getColumnModel().getColumn(1).setMinWidth(100);
		tblSequence.getColumnModel().getColumn(2).setPreferredWidth(0);
		tblSequence.getColumnModel().getColumn(2).setMinWidth(0);
		tblSequence.getColumnModel().getColumn(2).setMaxWidth(0);
		scrollPane.setViewportView(tblSequence);
		loadTableValues();

		add(getHelpButton());
		creating = false;
	}

	private JButton getHelpButton() {
		if (btnHelp == null) {
			btnHelp = new JButton();
			btnHelp.setBorder(null);
			btnHelp.setIcon(new ImageIcon(AbstractParamContainerPanel.class.getResource("/resource/icon/16/201.png"))); // help icon
			btnHelp.addActionListener(
					new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							ExtensionHelp.showHelp(HELPSTRING);

						}
					});
			btnHelp.setToolTipText(Constant.messages.getString("menu.help"));
		}
		return btnHelp;
	}

	private void loadTableValues() {
		List<ScriptWrapper> scripts = getSequenceScripts();
		DefaultTableModel dtm = (DefaultTableModel)tblSequence.getModel();

		for(int i = 0; i < scripts.size(); i++) {
			Object[] row = new Object[3];

			ScriptWrapper sw = scripts.get(i);
			row[0] = sw.getName();
			row[1] = false;
			row[2] = sw;
			dtm.addRow(row);

		}
		tblSequence.setModel(dtm);
	}

	private void setBooleanColumn(int columnIndex, boolean value) {
		for(int row = 0; row < tblSequence.getRowCount(); row++){
			tblSequence.setValueAt(value, row, columnIndex);
		}
	}

	/**
	 * Gets a list of Sequence Scripts, that were selected in the "Include" column.
	 * @return A list of the selected Sequence scripts in the "Include" column.
	 */
	public List<ScriptWrapper> getSelectedIncludeScripts() {
		ArrayList<ScriptWrapper> sw = new ArrayList<ScriptWrapper>();
		for(int i = 0; i < tblSequence.getModel().getRowCount(); i++) {
			boolean selected = (boolean)tblSequence.getValueAt(i, 1);
			if(selected) {
				ScriptWrapper script = (ScriptWrapper)tblSequence.getValueAt(i, 2);
				sw.add(script);
			}
		}
		return sw;
	}

	
	@Override
	public void initParam(Object obj) {
	}

	@Override
	public void validateParam(Object obj) throws Exception {
	}

	@Override
	public void saveParam(Object obj) throws Exception {
	}

	@Override
	public String getHelpIndex() {
		return HELPSTRING;
	}

	
	/**
	 * Fetches all the Sequence Scripts.
	 * @return A list of all Sequence Scripts.
	 */
	private List<ScriptWrapper> getSequenceScripts() {
		ExtensionScript extScript = (ExtensionScript) Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.class);
		return extScript.getScripts("sequence");
	}
}

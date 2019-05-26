/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.httppanel.view.paramtable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModelEvent;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModelListener;
import org.zaproxy.zap.extension.httppanel.view.paramtable.addins.ParamAddinInterface;
import org.zaproxy.zap.extension.httppanel.view.paramtable.addins.ParamAddinMagic;
import org.zaproxy.zap.extension.httppanel.view.paramtable.addins.ParamAddinUrlencode;
import org.zaproxy.zap.utils.DisplayUtils;

public abstract class HttpPanelParamTableView implements HttpPanelView, HttpPanelViewModelListener {

	public static final String NAME = "HttpPanelParamTableView";
	
	private static final String CAPTION_NAME = Constant.messages.getString("http.panel.view.tablev2.name");
	private static final String ADD_INS = Constant.messages.getString("http.panel.view.tableparam.addins");
	
	private JTable table;
	private JPanel mainPanel;
	private HttpPanelParamTableModel httpPanelTabularModel;
	private boolean isEditable = false;
	private List<ParamAddinInterface> addins;
	private JComboBox<Object> comboBoxAddIns;
	
	private HttpPanelViewModel model;
	
	public HttpPanelParamTableView(HttpPanelViewModel model, HttpPanelParamTableModel tableModel) {
		this.httpPanelTabularModel = tableModel;
		this.model = model;
		
		init();
		initAddins();
		
		this.model.addHttpPanelViewModelListener(this);
	}
	
	private void init() {
		// Table
        table = new JTable();
        table.setName("");
        table.setModel(httpPanelTabularModel);
        table.setGridColor(java.awt.Color.gray);
        table.setIntercellSpacing(new java.awt.Dimension(1, 1));
        table.setRowHeight(DisplayUtils.getScaledSize(18));
        
		// Issue 954: Force the JTable cell to auto-save when the focus changes.
		// Example, edit cell, click OK for a panel dialog box, the data will get saved.
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);


	    // Set standard row width's
	    TableColumn column = table.getColumnModel().getColumn(0);
	    column.setPreferredWidth(70);
	    column.setWidth(70);
	    column.setMaxWidth(70);
	    if (table.getColumnCount() == 4) {
	    	column = table.getColumnModel().getColumn(3);
	    	column.setPreferredWidth(150);
	    	column.setWidth(150);
	    	column.setMaxWidth(150);
	    }

  		// Main panel
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
	}
	
	private void initAddins() {
		// Get all addins
		addins = new LinkedList<>();
		addins.add( new ParamAddinMagic());
		addins.add( new ParamAddinUrlencode());
		
		comboBoxAddIns = new JComboBox<>();
		comboBoxAddIns.addItem(ADD_INS);
		for(ParamAddinInterface addin: addins) {
			comboBoxAddIns.addItem(addin);
		}
		comboBoxAddIns.addActionListener(new ComboBoxAddinsActionListener());
		
		table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(getComboBoxTypes()));
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		if (table.getColumnCount() != 4) {
			return;
		}
		
		table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(comboBoxAddIns));
		table.getColumnModel().getColumn(3).setCellRenderer(new ComboBoxCellRenderer(comboBoxAddIns));
	}
	
	public abstract JComboBox<HtmlParameter.Type> getComboBoxTypes();
	
	@Override
	public void dataChanged(HttpPanelViewModelEvent e) {
	    // FIXME(This view should ask for a specific model based on HttpMessage)
		httpPanelTabularModel.setHttpMessage((HttpMessage)model.getMessage());
	}
	
	@Override
	public void save() {
		httpPanelTabularModel.save();
	}
	
	@Override
	public void setSelected(boolean selected) {
		if (selected) {
			table.requestFocusInWindow();
		}
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String getCaptionName() {
		return CAPTION_NAME;
	}
	
	@Override
	public int getPosition() {
		return 10;
	}

	@Override
	public boolean isEnabled(Message msg) {
		return true;
	}

	@Override
	public boolean hasChanged() {
		return httpPanelTabularModel.hasChanged();
	}

	@Override
	public JComponent getPane() {
		return mainPanel;
	}

	@Override
	public boolean isEditable() {
		return isEditable;
	}

	@Override
	public void setEditable(boolean editable) {
		if (isEditable != editable) {
			if (isEditable) {
				table.getColumnModel().removeColumn(table.getColumnModel().getColumn(3));
			} else {
				TableColumn column = new TableColumn(3, 150, new ComboBoxCellRenderer(comboBoxAddIns), new DefaultCellEditor(comboBoxAddIns));
		    	column.setPreferredWidth(150);
		    	column.setMaxWidth(150);
				table.addColumn(column);
			}
			
			isEditable = editable;
			
			httpPanelTabularModel.setEditable(editable);
		}
	}
	
	@Override
	public HttpPanelViewModel getModel() {
		return model;
	}
	
	@Override
	public void setParentConfigurationKey(String configurationKey) {
	}
	
	@Override
	public void loadConfiguration(FileConfiguration configuration) {
	}
	
	@Override
	public void saveConfiguration(FileConfiguration configuration) {
	}
	
	private static final class ComboBoxAddinsActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			((JComboBox<?>)e.getSource()).setSelectedIndex(0);
		}
	}

	private static final class ComboBoxCellRenderer extends JComboBox<Object> implements TableCellRenderer {

		private static final long serialVersionUID = 7945388210094363435L;

		public ComboBoxCellRenderer(JComboBox<Object> comboBox) {
			this.addItem(comboBox.getModel().getElementAt(0));
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			return this;
		}
	}
}

package org.zaproxy.zap.extension.httppanel.view.paramtable;

import java.awt.BorderLayout;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.paramtable.addins.ParamAddinInterface;
import org.zaproxy.zap.extension.httppanel.view.paramtable.addins.ParamAddinMagic;
import org.zaproxy.zap.extension.httppanel.view.paramtable.addins.ParamAddinUrlencode;

public class RequestAllTableView implements HttpPanelView {

	private JTable table;
	private JPanel mainPanel;
	private RequestAllTableModel httpPanelTabularModel;
	private boolean isEditable = false;
	private JComboBox comboBox = null;
	private LinkedList<ParamAddinInterface> addins;
	private boolean isChanged = false;
	
	protected static final Logger log = Logger.getLogger(RequestAllTableView.class);

	
	public RequestAllTableView(RequestAllTableModel httpPanelTabularModel, boolean editable) {
		this.httpPanelTabularModel = httpPanelTabularModel;
		this.isEditable = editable;
		init();
		initAddins();
	}
	
	private void initAddins() {
		if (table.getColumnCount() != 4) {
			return;
		}
		// Get all addins
		addins = new LinkedList<ParamAddinInterface>();
		addins.add( new ParamAddinMagic());
		addins.add( new ParamAddinUrlencode());
		
		// Add combobox editor to table
		comboBox = new RequestAllTableRenderer();
		comboBox.addItem("AddIns");
		for(ParamAddinInterface addin: addins) {
			comboBox.addItem(addin.getName());
		}
		comboBox.setVisible(true);
		comboBox.setEditable(false);
		TableColumn col = table.getColumnModel().getColumn(3);
	    col.setCellEditor(new RequestAllTableEditor(comboBox, this));
	    
	    // Add combobox renderer to table
	    // Note: cant be the same as the renderer, or else UI is fucked up
	    comboBox = new RequestAllTableRenderer();
		comboBox.addItem("AddIns");
		comboBox.setVisible(true);
		comboBox.setEditable(false);
	    col.setCellRenderer((TableCellRenderer) comboBox);
	    table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
	}
	
	private void init() {
		// Table
        table = new JTable();
        table.setName("");
        table.setModel(httpPanelTabularModel);
        table.setGridColor(java.awt.Color.gray);
        table.setIntercellSpacing(new java.awt.Dimension(1, 1));
        table.setRowHeight(18);
        
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
	
	@Override
	public void load() {
		httpPanelTabularModel.load();
		isChanged = false;
	}

	@Override
	public void save() {
		httpPanelTabularModel.save();
		isChanged = false;
	}

	@Override
	public String getConfigName() {
		return "Table";
	}
	
	@Override
	public String getName() {
		return Constant.messages.getString("request.panel.view.table");
	}

	@Override
	public boolean isEnabled(HttpMessage msg) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean hasChanged() {
		return isChanged;
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
		this.isEditable = editable;
	}
	
	public void comboBoxClicked(int selectedIndex, int selectedRow) {
		if (selectedIndex == 0 || selectedRow < 0) {
			return;
		}

		ParamAddinInterface addin = addins.get(selectedIndex-1);
		String result;
		try {
			result = addin.convertData( 
					(String) httpPanelTabularModel.getValueAt(selectedRow, 2));
			httpPanelTabularModel.setValueAt(result, selectedRow, 2);
			
			isChanged = true;
		} catch (UnsupportedEncodingException e) {
			log.warn("Could not convert data", e);
		}
	}
}

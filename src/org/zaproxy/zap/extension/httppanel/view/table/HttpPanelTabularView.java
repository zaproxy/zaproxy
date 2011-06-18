package org.zaproxy.zap.extension.httppanel.view.table;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanelView;

public class HttpPanelTabularView implements HttpPanelView {

	private JTable tableBody;
	private JPanel mainPanel;
	private AbstractTableModel httpPanelTabularModel;
	
	public HttpPanelTabularView(AbstractTableModel model) {
		this.httpPanelTabularModel = model;
		init();
	}
	
	public HttpPanelTabularView() {
		httpPanelTabularModel = new HttpPanelTabularModel();
		init();
	}
	
	private void init() {
		// Table
        tableBody = new JTable();
        tableBody.setName("");
        tableBody.setModel(httpPanelTabularModel);

        tableBody.setGridColor(java.awt.Color.gray);
        tableBody.setIntercellSpacing(new java.awt.Dimension(1, 1));
        tableBody.setRowHeight(18);

		// Main panel
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(tableBody, BorderLayout.CENTER);
	}
	
	@Override
	public String getName() {
		return "Table";
	}

	@Override
	public boolean hasChanged() {
		//return httpPanelTabularModel.hasChanged();
		return true;
	}

	@Override
	public JComponent getPane() {
		return mainPanel;
	}

	@Override
	public boolean isEditable() {
		//return httpPanelTabularModel.isEditable();
		return false;
	}

	@Override
	public void setEditable(boolean editable) {
		//httpPanelTabularModel.setEditable(editable);		
	}
	
	@Override
	public boolean isEnabled(HttpMessage msg) {
		return tableBody.isEnabled();
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}

}

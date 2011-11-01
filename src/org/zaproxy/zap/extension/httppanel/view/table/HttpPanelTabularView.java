package org.zaproxy.zap.extension.httppanel.view.table;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextArea.MessageType;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;

public class HttpPanelTabularView implements HttpPanelView {

	private JTable tableBody;
	private JPanel mainPanel;
	private HttpPanelTabularModel httpPanelTabularModel;
	private MessageType messageType;
	private HttpPanelTextModelInterface model;
	private boolean isEditable = false;

	public HttpPanelTabularView(HttpPanelTextModelInterface modelTextBody, MessageType body, boolean editable) {
		httpPanelTabularModel = new HttpPanelTabularModel();
		this.messageType = body;
		this.model = modelTextBody;
		this.isEditable = editable;
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
		mainPanel.add(new JScrollPane(tableBody), BorderLayout.CENTER);
	}
	
	@Override
	public String getName() {
		return Constant.messages.getString("request.panel.view.table");
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
		return httpPanelTabularModel.isEditable();
	}

	@Override
	public void setEditable(boolean editable) {
		httpPanelTabularModel.setEditable(editable);		
	}
	
	@Override
	public boolean isEnabled(HttpMessage msg) {
		return true;
		//return tableBody.isEnabled();
	}

	@Override
	public void load() {
		httpPanelTabularModel.setText(model.getData());
	}

	@Override
	public void save() {
		String data = httpPanelTabularModel.getText();
		if (data != null) {
			model.setData(data);
		}
	}

}

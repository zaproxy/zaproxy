package org.zaproxy.zap.extension.httppanel.plugin.response.all;

import javax.swing.JComboBox;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.plugin.request.all.RequestAllView;
import org.zaproxy.zap.extension.httppanel.view.hex.HttpPanelHexView;

public class ResponseAllView extends RequestAllView {

	public ResponseAllView(HttpPanel httpPanel, HttpMessage httpMessage) {
		super(httpPanel, httpMessage);
	}
	
	@Override
	public boolean isRequest() {
		return false;
	}
	
	@Override
	protected void initModel() {
		modelText = new ResponseAllModelText(httpMessage);
		modelText = new ResponseAllModelText(httpMessage);
		modelHex = new ResponseAllModelText(httpMessage);
//		modelTableBody = new ResponseSplitModelBodyText(httpMessage);
	}

	@Override
	protected void initPlugins() {
		// Plugins - View
		textView = new HttpResponseAllPanelTextView(modelText, httpPanel.isEditable());
		//tableView = new RequestAllTableView(modelTable, MessageType.Full, httpPanel.isEditable());
		hexView = new HttpPanelHexView(modelText, httpPanel.isEditable());
		
		views.put(textView.getName(), textView);
		//views.put(tableView.getName(), tableView);
		views.put(hexView.getName(), hexView);
		
		panelMainSwitchable.add(textView.getPane(), textView.getName());
		//panelMainSwitchable.add(tableView.getPane(), tableView.getName());
		panelMainSwitchable.add(hexView.getPane(), hexView.getName());

		// Combobox
		comboxSelectView = new JComboBox();
		comboxSelectView.addItem(textView.getName());
		//comboxSelectView.addItem(tableView.getName());
		comboxSelectView.addItem(hexView.getName());
		comboxSelectView.addActionListener(this);
	}
	
	public void setHttpMessage(HttpMessage httpMessage) {
		this.httpMessage = httpMessage;
		
		modelText.setHttpMessage(httpMessage);
		//modelTable.setHttpMessage(httpMessage);
		
		// This is not nice, but needed for fuzzing
		// ExtensionAntiCSRF gets HttpMessage from HttpPanelTextView...
		textView.setHttpMessage(httpMessage);
		//tableView.set
	}
}

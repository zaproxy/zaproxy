package org.zaproxy.zap.extension.httppanel.plugin.response.split;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.plugin.request.split.RequestSplitView;
import org.zaproxy.zap.extension.httppanel.view.hex.HttpPanelHexView;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextView;

/*
 * ResponseSplitView is identical to RequestSplitView
 */

public class ResponseSplitView extends RequestSplitView {

	public ResponseSplitView(HttpPanel httpPanel, HttpMessage httpMessage) {
		super(httpPanel, httpMessage);
	}
	
	@Override
	public boolean isRequest() {
		return false;
	}
	
	@Override
	protected HttpPanelTextView createHttpPanelTextView() {
		return new HttpResponseHeaderPanelTextView(modelTextHeader, httpPanel.isEditable());
	}

	@Override
	protected void initModel() {
		modelTextHeader = new ResponseSplitModelHeaderText(httpMessage);
		modelTextBody = new ResponseSplitModelBodyText(httpMessage);
		modelHexBody = new ResponseSplitModelBodyText(httpMessage);
		modelTableBody = new ResponseSplitModelBodyText(httpMessage);
	}
	
	@Override
	protected void initPlugins() {
		viewBodyText = new HttpResponseBodyPanelTextView(modelTextBody, httpPanel.isEditable());
		viewBodyHex = new HttpPanelHexView(modelTextBody, httpPanel.isEditable());
		
		views.put(viewBodyText.getName(), viewBodyText);
		views.put(viewBodyHex.getName(), viewBodyHex);
		
		panelMainSwitchable.add(viewBodyText.getPane(), viewBodyText.getName());
		panelMainSwitchable.add(viewBodyHex.getPane(), viewBodyHex.getName());
		
		// Combobox
		comboxSelectView.addItem(viewBodyText.getName());
		comboxSelectView.addItem(viewBodyHex.getName());
		comboxSelectView.addActionListener(this);
		panelOptions.add(comboxSelectView);
	}
}

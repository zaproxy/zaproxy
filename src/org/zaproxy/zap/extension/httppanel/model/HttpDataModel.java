package org.zaproxy.zap.extension.httppanel.model;

import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelHexUi;
import org.zaproxy.zap.extension.httppanel.HttpPanelTableUi;
import org.zaproxy.zap.extension.httppanel.HttpPanelTextUi;

public abstract class HttpDataModel {
	
	protected HttpPanel httpPanel;
	protected HttpPanelHexUi httpPanelHexUi;
	protected HttpPanelTextUi httpPanelTextUi;
	protected HttpPanelTableUi httpPanelTableUi;
	
	public HttpDataModel(HttpPanel httpPanel, HttpPanelHexUi httpPanelHexUi, HttpPanelTableUi httpPanelTableUi, HttpPanelTextUi httpPanelTextUi) {
		this.httpPanel = httpPanel;
		this.httpPanelHexUi = httpPanelHexUi;
		this.httpPanelTextUi = httpPanelTextUi;
		this.httpPanelTableUi = httpPanelTableUi;
	}
	
	protected HttpMessage getHttpMessage() {
		return httpPanel.getHttpMessage();
	}

	abstract public void textDataToView();
	abstract public void tableDataToView();
	abstract public void hexDataToView();
	
	abstract public void textDataFromView();
	abstract public void tableDataFromView();
	abstract public void hexDataFromView();
	
	// Really necessary?
	protected String convertHeader(String request) {
		String result = request.replaceAll("\\n", "\r\n");
		result = result.replaceAll("(\\r\\n)*\\z", "") + "\r\n\r\n";
		return result;
	}
}
package org.zaproxy.zap.extension.httppanel.model;

import org.parosproxy.paros.view.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelSplitUi;

public class HttpDataModelResSplit {

	private HttpPanel httpPanel;
	private HttpPanelSplitUi splitUi;
		
	public HttpDataModelResSplit(HttpPanel httpPanel, HttpPanelSplitUi splitUi) {
		this.httpPanel = httpPanel;
		this.splitUi = splitUi;
	}
		
	public void loadData() {
		splitUi.setMessage(httpPanel.getHttpMessage(), true);
	}

	public void saveData() {
		splitUi.getMessage(httpPanel.getHttpMessage(), true);
	}
	
}
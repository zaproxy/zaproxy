package org.zaproxy.zap.extension.httppanel;

import org.parosproxy.paros.view.HttpPanel;

public class HttpDataModelReqSplit  {
	private HttpPanel httpPanel;
	private HttpPanelSplitUi splitUi;
	
	public HttpDataModelReqSplit(HttpPanel httpPanel, HttpPanelSplitUi splitUi) {
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

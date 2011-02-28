package org.zaproxy.zap.extension.httppanel;

import org.parosproxy.paros.view.HttpPanel;


public class HttpDataModelResAll extends HttpDataModel {

	public HttpDataModelResAll(HttpPanel httpPanel,
			HttpPanelHexUi httpPanelHexUi, HttpPanelTableUi httpPanelTableUi,
			HttpPanelTextUi httpPanelTextUi) {
		super(httpPanel, httpPanelHexUi, httpPanelTableUi, httpPanelTextUi);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void hexDataFromView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hexDataToView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tableDataFromView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tableDataToView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void textDataFromView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void textDataToView() {
		httpPanelTextUi.setData(getHttpMessage().getResponseHeader().toString() + getHttpMessage().getResponseBody().toString());
	}

}

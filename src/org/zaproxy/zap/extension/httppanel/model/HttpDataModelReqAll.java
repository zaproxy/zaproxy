package org.zaproxy.zap.extension.httppanel.model;

import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelHexUi;
import org.zaproxy.zap.extension.httppanel.HttpPanelTableUi;
import org.zaproxy.zap.extension.httppanel.HttpPanelTextUi;

public class HttpDataModelReqAll extends HttpDataModel {

	public HttpDataModelReqAll(HttpPanel httpPanel,
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
		String[] parts = httpPanelTextUi.getData().split("\r\n\r\n");
		
		try {
			//getHttpMessage().setRequestHeader(convertHeader(parts[0]));
			getHttpMessage().setRequestHeader(parts[0]);
		} catch (HttpMalformedHeaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (parts.length == 2) {
			getHttpMessage().setRequestBody(parts[1]);
		}
	}

	@Override
	public void textDataToView() {
		httpPanelTextUi.setData(getHttpMessage().getRequestHeader().toString() + getHttpMessage().getRequestBody().toString());
	}

}
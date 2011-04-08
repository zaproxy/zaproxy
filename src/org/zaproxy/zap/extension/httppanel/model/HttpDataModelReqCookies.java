package org.zaproxy.zap.extension.httppanel.model;

import java.util.TreeSet;

import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelHexUi;
import org.zaproxy.zap.extension.httppanel.HttpPanelTableUi;
import org.zaproxy.zap.extension.httppanel.HttpPanelTextUi;

public class HttpDataModelReqCookies extends HttpDataModel {

	public HttpDataModelReqCookies(HttpPanel httpPanel,
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
		getHttpMessage().setCookieParamsAsString(httpPanelTextUi.getData());
	}

	@Override
	public void textDataToView() {
		httpPanelTextUi.setData(getHttpMessage().getCookieParamsAsString()); 
	}

}
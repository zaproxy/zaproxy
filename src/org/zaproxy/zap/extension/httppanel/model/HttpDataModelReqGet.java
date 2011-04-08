package org.zaproxy.zap.extension.httppanel.model;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.view.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelHexUi;
import org.zaproxy.zap.extension.httppanel.HttpPanelTableUi;
import org.zaproxy.zap.extension.httppanel.HttpPanelTextUi;

public class HttpDataModelReqGet extends HttpDataModel {

	public HttpDataModelReqGet(HttpPanel httpPanel,
			HttpPanelHexUi httpPanelHexUi, 
			HttpPanelTableUi httpPanelTableUi,
			HttpPanelTextUi httpPanelTextUi) 
	{
		super(httpPanel, httpPanelHexUi, httpPanelTableUi, httpPanelTextUi);
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
		URI origUri = getHttpMessage().getRequestHeader().getURI();
		try {
			origUri.setQuery(httpPanelTextUi.getData());
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void textDataToView() {
		try {
			httpPanelTextUi.setData(getHttpMessage().getRequestHeader().getURI().getQuery());
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
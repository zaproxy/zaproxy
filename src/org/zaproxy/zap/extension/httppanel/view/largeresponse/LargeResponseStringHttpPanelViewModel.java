package org.zaproxy.zap.extension.httppanel.view.largeresponse;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpHeader;
import org.zaproxy.zap.extension.httppanel.view.models.response.ResponseStringHttpPanelViewModel;

public class LargeResponseStringHttpPanelViewModel extends ResponseStringHttpPanelViewModel {

	@Override
	public String getData() {
		if (httpMessage == null || httpMessage.getResponseHeader().isEmpty()) {
			return "";
		}
		
		return httpMessage.getResponseHeader().toString().replaceAll(HttpHeader.CRLF, HttpHeader.LF) +
				Constant.messages.getString("http.panel.view.largeresponse.all.warning");
	}

	@Override
	public void setData(String data) {
	}

}

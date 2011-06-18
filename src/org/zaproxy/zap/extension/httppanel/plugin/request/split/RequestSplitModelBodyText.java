package org.zaproxy.zap.extension.httppanel.plugin.request.split;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;

public class RequestSplitModelBodyText extends HttpPanelTextModelInterface {

	public RequestSplitModelBodyText(HttpMessage httpMessage) {
		setHttpMessage(httpMessage);
	}
	
	@Override
	public String getData() {
		return httpMessage.getRequestBody().toString();
	}

	@Override
	public void setData(String data) {
		httpMessage.getRequestBody().setBody(data);
	}

}

package org.zaproxy.zap.extension.httppanel.plugin.response.split;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;

public class ResponseSplitModelBodyText extends HttpPanelTextModelInterface {

	public ResponseSplitModelBodyText(HttpMessage httpMessage) {
		setHttpMessage(httpMessage);
	}
	
	@Override
	public String getData() {
		return httpMessage.getResponseBody().toString();
	}

	@Override
	public void setData(String data) {
		httpMessage.getResponseBody().setBody(data);
	}
}

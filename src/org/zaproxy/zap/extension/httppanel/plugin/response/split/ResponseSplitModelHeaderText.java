package org.zaproxy.zap.extension.httppanel.plugin.response.split;

import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;

public class ResponseSplitModelHeaderText extends HttpPanelTextModelInterface {
	
	public ResponseSplitModelHeaderText(HttpMessage httpMessage) {
		setHttpMessage(httpMessage);
	}
	
	@Override
	public String getData() {
		return httpMessage.getResponseHeader().toString();
	}

	@Override
	public void setData(String data) {
		try {
			httpMessage.setResponseHeader(data);
		} catch (HttpMalformedHeaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

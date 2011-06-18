package org.zaproxy.zap.extension.httppanel.plugin.request.split;

import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;

public class RequestSplitModelHeaderText extends HttpPanelTextModelInterface {
	
	public RequestSplitModelHeaderText(HttpMessage httpMessage) {
		setHttpMessage(httpMessage);
	}
	
	@Override
	public String getData() {
		return httpMessage.getRequestHeader().toString();
	}

	@Override
	public void setData(String data) {
		try {
			httpMessage.setRequestHeader(data);
		} catch (HttpMalformedHeaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

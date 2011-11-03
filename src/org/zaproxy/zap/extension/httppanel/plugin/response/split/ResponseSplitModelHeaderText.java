package org.zaproxy.zap.extension.httppanel.plugin.response.split;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;

public class ResponseSplitModelHeaderText extends HttpPanelTextModelInterface {
	
    private static Logger log = Logger.getLogger(HttpMessage.class);
	
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
			log.warn("Could not Save Header: " + data);
		}
	}
}

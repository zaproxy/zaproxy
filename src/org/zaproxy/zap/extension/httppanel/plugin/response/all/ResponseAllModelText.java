package org.zaproxy.zap.extension.httppanel.plugin.response.all;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;

public class ResponseAllModelText extends HttpPanelTextModelInterface {
	
	private static Logger log = Logger.getLogger(ResponseAllModelText.class);
	
	public ResponseAllModelText(HttpMessage httpMessage) {
		setHttpMessage(httpMessage);
	}

	@Override
	public String getData() {
		return httpMessage.getResponseHeader().toString().replaceAll(HttpHeader.CRLF, HttpHeader.LF) + httpMessage.getResponseBody().toString();
	}

	@Override
	public void setData(String data) {
		String[] parts = data.split(HttpHeader.LF + HttpHeader.LF);
		String header = parts[0].replaceAll("(?<!\r)\n", HttpHeader.CRLF);
		
		try {
			httpMessage.setResponseHeader(header);
		} catch (HttpMalformedHeaderException e) {
			log.warn("Could not Save Header: " + header, e);
		}
		
		if (parts.length > 1) {
			httpMessage.setResponseBody(data.substring(parts[0].length()+2));
		} else {
			httpMessage.setResponseBody("");
		}
	}
}

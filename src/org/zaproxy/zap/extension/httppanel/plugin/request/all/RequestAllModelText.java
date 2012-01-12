package org.zaproxy.zap.extension.httppanel.plugin.request.all;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;

public class RequestAllModelText extends HttpPanelTextModelInterface {
	
	private static Logger log = Logger.getLogger(RequestAllModelText.class);
	
	public RequestAllModelText(HttpMessage httpMessage) {
		setHttpMessage(httpMessage);
	}
	
	@Override
	public String getData() {
		return httpMessage.getRequestHeader().toString().replaceAll(HttpHeader.CRLF, HttpHeader.LF) + httpMessage.getRequestBody().toString();
	}

	@Override
	public void setData(String data) {
		String[] parts = data.split(HttpHeader.LF + HttpHeader.LF);
		String header = parts[0].replaceAll("(?<!\r)\n", HttpHeader.CRLF);
		
		try {
			httpMessage.setRequestHeader(header);
		} catch (HttpMalformedHeaderException e) {
			log.warn("Could not Save Header: " + header, e);
		}
		
		if (parts.length > 1) {
			httpMessage.setRequestBody(data.substring(parts[0].length()+2));
		} else {
			httpMessage.setRequestBody("");
		}
	}
}

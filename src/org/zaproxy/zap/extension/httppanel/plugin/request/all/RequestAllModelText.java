package org.zaproxy.zap.extension.httppanel.plugin.request.all;

import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;

public class RequestAllModelText extends HttpPanelTextModelInterface {
	
	public RequestAllModelText(HttpMessage httpMessage) {
		setHttpMessage(httpMessage);
	}
	
	@Override
	public String getData() {
		return httpMessage.getRequestHeader().toString() + httpMessage.getRequestBody().toString();
	}

	@Override
	public void setData(String data) {
		String[] parts = data.split("\r\n\r\n");
        
        try {
                httpMessage.setRequestHeader(parts[0]);
        } catch (HttpMalformedHeaderException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        
        if (parts.length == 2) {
                httpMessage.setRequestBody(parts[1]);
        }
	}
}

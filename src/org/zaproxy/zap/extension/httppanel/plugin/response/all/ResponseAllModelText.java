package org.zaproxy.zap.extension.httppanel.plugin.response.all;

import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;

public class ResponseAllModelText extends HttpPanelTextModelInterface {
	
	public ResponseAllModelText(HttpMessage httpMessage) {
		setHttpMessage(httpMessage);
	}

	@Override
	public String getData() {
		return httpMessage.getResponseHeader().toString() + httpMessage.getResponseBody().toString();
	}

	@Override
	public void setData(String data) {
		String[] parts = data.split("\r\n\r\n");
        
        try {
                httpMessage.setResponseHeader(parts[0]);
        } catch (HttpMalformedHeaderException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        
        if (parts.length == 2) {
                httpMessage.setResponseBody(parts[1]);
        }		
	}

}

package org.zaproxy.zap.extension.httppanel.view.text;

import org.parosproxy.paros.network.HttpMessage;

public abstract class HttpPanelTextModelInterface {
	protected HttpMessage httpMessage;
	
	public void setHttpMessage(HttpMessage httpMessage) {
		this.httpMessage = httpMessage;
	}
	
	public HttpMessage getHttpMessage() {
		return httpMessage;
	}
	
	public abstract String getData();
	public abstract void setData(String data);
}

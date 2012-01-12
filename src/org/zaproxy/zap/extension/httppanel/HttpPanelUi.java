package org.zaproxy.zap.extension.httppanel;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.search.SearchMatch;

public interface HttpPanelUi {
	public void setHttpMessage(HttpMessage httpMessage);
	public HttpMessage getHttpMessage();
	public SearchMatch getTextSelection();
}

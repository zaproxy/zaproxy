package org.zaproxy.zap.extension.httppanel;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextArea.MessageType;
import org.zaproxy.zap.extension.search.SearchMatch;

public interface HttpPanelUi {
	public MessageType getMessageType();
	public void setHttpMessage(HttpMessage httpMessage);
	public HttpMessage getHttpMessage();
	public SearchMatch getTextSelection();
}

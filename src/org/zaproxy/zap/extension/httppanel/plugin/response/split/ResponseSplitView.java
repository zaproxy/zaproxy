package org.zaproxy.zap.extension.httppanel.plugin.response.split;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.plugin.request.split.RequestSplitModelBodyText;
import org.zaproxy.zap.extension.httppanel.plugin.request.split.RequestSplitModelHeaderText;
import org.zaproxy.zap.extension.httppanel.plugin.request.split.RequestSplitView;

/*
 * ResponseSplitView is identical to RequestSplitView
 */

public class ResponseSplitView extends RequestSplitView {

	public ResponseSplitView(HttpPanel httpPanel, HttpMessage httpMessage) {
		super(httpPanel, httpMessage);
	}

	@Override
	protected void initModel() {
		modelTextHeader = new ResponseSplitModelHeaderText(httpMessage);
		modelTextBody = new ResponseSplitModelBodyText(httpMessage);
		modelHexBody = new ResponseSplitModelBodyText(httpMessage);
	}
}

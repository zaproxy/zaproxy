package org.zaproxy.zap.extension.httppanel;

import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.plugin.response.split.ResponseSplitView;

public class HttpPanelResponse extends HttpPanel  {
	private static final long serialVersionUID = 1L;
    
	
	public HttpPanelResponse(boolean isEditable, HttpMessage httpMessage) {
		super(isEditable, httpMessage);
	}
	
	public HttpPanelResponse(boolean isEditable, Extension extension, HttpMessage httpMessage) {
		super(isEditable, extension, httpMessage);
	}

	public HttpPanelResponse(boolean isEditable, Extension extension, HttpMessage httpMessage, OptionsParamView.ViewType viewType) {
		super(isEditable, extension, httpMessage, viewType);
	}
	
	protected void initPlugins() {
		// TODO: elsewhere
		new ResponseSplitView(this, httpMessage);
	}

	protected void initSpecial() {
		
	}
	
}
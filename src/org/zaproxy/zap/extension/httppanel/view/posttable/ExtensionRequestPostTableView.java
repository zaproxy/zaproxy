/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.httppanel.view.posttable;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.httppanel.component.split.request.RequestSplitComponent;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestBodyStringHttpPanelViewModel;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelViewFactory;

public class ExtensionRequestPostTableView extends ExtensionAdaptor {

	public static final String NAME = "ExtensionRequestPostTableView";
	
	public ExtensionRequestPostTableView() {
		super(NAME);

		setOrder(80);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
		if (getView() != null) {
			HttpPanelManager.getInstance().addRequestViewFactory(RequestSplitComponent.NAME, new RequestPostTableViewFactory());
		}
	}
	
	@Override
	public boolean canUnload() {
		// Do not allow the unload until moved to an add-on.
		return false;
	}
	
	@Override
	public void unload() {
		if (getView() != null) {
			HttpPanelManager panelManager = HttpPanelManager.getInstance();
			panelManager.removeRequestViewFactory(RequestSplitComponent.NAME, RequestPostTableViewFactory.NAME);
			panelManager.removeRequestViews(RequestSplitComponent.NAME, RequestPostTableView.NAME, RequestSplitComponent.ViewComponent.BODY);
		}
	}
	
	private static final class RequestPostTableViewFactory implements HttpPanelViewFactory {
		
		public static final String NAME = "RequestPostTableViewFactory";
		
		@Override
		public String getName() {
			return NAME;
		}
		
		@Override
		public HttpPanelView getNewView() {
			return new RequestPostTableView(new RequestBodyStringHttpPanelViewModel());
		}

		@Override
		public Object getOptions() {
			return RequestSplitComponent.ViewComponent.BODY;
		}
	}
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}


	/**
	 * No database tables used, so all supported
	 */
	@Override
	public boolean supportsDb(String type) {
		return true;
	}
}

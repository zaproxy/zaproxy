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
package org.zaproxy.zap.extension.httppanel.view.hex;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.httppanel.component.split.request.RequestSplitComponent;
import org.zaproxy.zap.extension.httppanel.component.split.response.ResponseSplitComponent;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestBodyByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestHeaderByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseBodyByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseHeaderByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.component.all.request.RequestAllComponent;
import org.zaproxy.zap.extension.httppanel.component.all.response.ResponseAllComponent;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelViewFactory;

public class ExtensionHttpPanelHexView extends ExtensionAdaptor {

	public static final String NAME = "ExtensionHttpPanelHexView";
	
	public ExtensionHttpPanelHexView() {
		super(NAME);
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
		
		HttpPanelManager.getInstance().addRequestView(RequestSplitComponent.NAME, new RequestSplitHeaderViewFactory());
		HttpPanelManager.getInstance().addRequestView(RequestSplitComponent.NAME, new RequestSplitBodyViewFactory());
		
		HttpPanelManager.getInstance().addResponseView(ResponseSplitComponent.NAME, new ResponseSplitHeaderViewFactory());
		HttpPanelManager.getInstance().addResponseView(ResponseSplitComponent.NAME, new ResponseSplitBodyViewFactory());
		
		
		HttpPanelManager.getInstance().addRequestView(RequestAllComponent.NAME, new RequestAllViewFactory());
		HttpPanelManager.getInstance().addResponseView(ResponseAllComponent.NAME, new ResponseAllViewFactory());
		
	}

	private static final class RequestSplitHeaderViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpPanelHexView(new RequestHeaderByteHttpPanelViewModel(), false);
		}

		@Override
		public Object getOptions() {
			return RequestSplitComponent.ViewComponent.HEADER;
		}
	}

	private static final class RequestSplitBodyViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpPanelHexView(new RequestBodyByteHttpPanelViewModel(), false);
		}

		@Override
		public Object getOptions() {
			return RequestSplitComponent.ViewComponent.BODY;
		}
	}

	private static final class ResponseSplitHeaderViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpPanelHexView(new ResponseHeaderByteHttpPanelViewModel(), false);
		}

		@Override
		public Object getOptions() {
			return ResponseSplitComponent.ViewComponent.HEADER;
		}
	}

	private static final class ResponseSplitBodyViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpPanelHexView(new ResponseBodyByteHttpPanelViewModel(), false);
		}

		@Override
		public Object getOptions() {
			return ResponseSplitComponent.ViewComponent.BODY;
		}
	}

	private static final class RequestAllViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpPanelHexView(new RequestByteHttpPanelViewModel(), false);
		}

		@Override
		public Object getOptions() {
			return null;
		}
	}
	
	private static final class ResponseAllViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpPanelHexView(new ResponseByteHttpPanelViewModel(), false);
		}

		@Override
		public Object getOptions() {
			return null;
		}
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}
}


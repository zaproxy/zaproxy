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
package org.zaproxy.zap.extension.httppanelviews.syntaxhighlight;

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.httppanel.component.split.request.RequestSplitComponent;
import org.zaproxy.zap.extension.httppanel.component.split.response.ResponseSplitComponent;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.models.request.RequestBodyStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.models.request.RequestHeaderStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.models.request.RequestStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.models.response.ResponseBodyStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.models.response.ResponseHeaderStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.models.response.ResponseStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanelcomp.all.request.RequestAllComponent;
import org.zaproxy.zap.extension.httppanelcomp.all.response.ResponseAllComponent;
import org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.components.all.request.HttpRequestAllPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.components.all.response.HttpResponseAllPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.components.split.request.HttpRequestBodyPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.components.split.request.HttpRequestHeaderPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.components.split.response.HttpResponseBodyPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanelviews.syntaxhighlight.components.split.response.HttpResponseHeaderPanelSyntaxHighlightTextView;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelViewFactory;

public class ExtensionHttpPanelSyntaxHighlightTextView extends ExtensionAdaptor {

	private static final String NAME = "ExtensionHttpPanelSyntaxHighlightTextView";
	
	public ExtensionHttpPanelSyntaxHighlightTextView() {
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
			return new HttpRequestHeaderPanelSyntaxHighlightTextView(new RequestHeaderStringHttpPanelViewModel());
		}
		
		@Override
		public Object getOptions() {
			return RequestSplitComponent.ViewComponent.HEADER;
		}
	}
	
	private static final class RequestSplitBodyViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpRequestBodyPanelSyntaxHighlightTextView(new RequestBodyStringHttpPanelViewModel());
		}
		
		@Override
		public Object getOptions() {
			return RequestSplitComponent.ViewComponent.BODY;
		}
	}
	
	private static final class ResponseSplitHeaderViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpResponseHeaderPanelSyntaxHighlightTextView(new ResponseHeaderStringHttpPanelViewModel());
		}
		
		@Override
		public Object getOptions() {
			return ResponseSplitComponent.ViewComponent.HEADER;
		}
	}
	
	private static final class ResponseSplitBodyViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpResponseBodyPanelSyntaxHighlightTextView(new ResponseBodyStringHttpPanelViewModel());
		}
		
		@Override
		public Object getOptions() {
			return ResponseSplitComponent.ViewComponent.BODY;
		}
	}
	
	private static final class RequestAllViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpRequestAllPanelSyntaxHighlightTextView(new RequestStringHttpPanelViewModel());
		}
		
		@Override
		public Object getOptions() {
			return null;
		}
	}
	
	private static final class ResponseAllViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpResponseAllPanelSyntaxHighlightTextView(new ResponseStringHttpPanelViewModel());
		}
		
		@Override
		public Object getOptions() {
			return null;
		}
	}
}

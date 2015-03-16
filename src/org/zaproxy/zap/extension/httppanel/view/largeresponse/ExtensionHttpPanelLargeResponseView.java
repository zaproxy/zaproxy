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
package org.zaproxy.zap.extension.httppanel.view.largeresponse;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.component.all.response.ResponseAllComponent;
import org.zaproxy.zap.extension.httppanel.component.split.response.ResponseSplitComponent;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelDefaultViewSelectorFactory;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelViewFactory;

public class ExtensionHttpPanelLargeResponseView extends ExtensionAdaptor {
	
	public static final String NAME = "ExtensionHttpPanelLargeResponseView";
	
	public ExtensionHttpPanelLargeResponseView() {
		super(NAME);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
		if (getView() != null) {
			HttpPanelManager panelManager = HttpPanelManager.getInstance();
			panelManager.addResponseViewFactory(ResponseSplitComponent.NAME, new ResponseLargeResponseSplitViewFactory());
			panelManager.addResponseDefaultViewSelectorFactory(ResponseSplitComponent.NAME, new LargeResponseDefaultSplitViewSelectorFactory());
			
			panelManager.addResponseViewFactory(ResponseAllComponent.NAME, new ResponseLargeResponseAllViewFactory());
			panelManager.addResponseDefaultViewSelectorFactory(ResponseAllComponent.NAME, new LargeResponseDefaultAllViewSelectorFactory());
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
			panelManager.removeResponseViewFactory(ResponseSplitComponent.NAME, ResponseLargeResponseSplitViewFactory.NAME);
			panelManager.removeResponseViews(
					ResponseSplitComponent.NAME,
					ResponseLargeResponseSplitView.NAME,
					ResponseSplitComponent.ViewComponent.BODY);
			panelManager.removeResponseDefaultViewSelectorFactory(
					ResponseSplitComponent.NAME,
					LargeResponseDefaultSplitViewSelectorFactory.NAME);
			panelManager.removeResponseDefaultViewSelectors(
					ResponseSplitComponent.NAME,
					LargeResponseDefaultSplitViewSelector.NAME,
					ResponseSplitComponent.ViewComponent.BODY);

			panelManager.removeResponseViewFactory(ResponseAllComponent.NAME, ResponseLargeResponseAllViewFactory.NAME);
			panelManager.removeResponseViews(ResponseAllComponent.NAME, ResponseLargeResponseAllView.NAME, null);
			panelManager.removeResponseDefaultViewSelectorFactory(
					ResponseAllComponent.NAME,
					LargeResponseDefaultAllViewSelectorFactory.NAME);
			panelManager.removeResponseDefaultViewSelectors(
					ResponseAllComponent.NAME,
					LargeResponseDefaultAllViewSelector.NAME,
					null);
		}
	}
	
	private static final class ResponseLargeResponseSplitViewFactory implements HttpPanelViewFactory {
		
		public static final String NAME = "ResponseLargeResponseSplitViewFactory";
		
		@Override
		public String getName() {
			return NAME;
		}
		
		@Override
		public HttpPanelView getNewView() {
			return new ResponseLargeResponseSplitView();
		}

		@Override
		public Object getOptions() {
			return ResponseSplitComponent.ViewComponent.BODY;
		}
	}

	private static final class LargeResponseDefaultSplitViewSelector implements HttpPanelDefaultViewSelector {

		public static final String NAME = "LargeResponseDefaultSplitViewSelector";
		
		@Override
		public String getName() {
			return NAME;
		}
		
		@Override
		public boolean matchToDefaultView(Message aMessage) {
			return LargeResponseUtil.isLargeResponse(aMessage);
		}

		@Override
		public String getViewName() {
			return ResponseLargeResponseSplitView.NAME;
		}
		
		@Override
		public int getOrder() {
			return 50;
		}
	}

	private static final class ResponseLargeResponseAllViewFactory implements HttpPanelViewFactory {

		public static final String NAME = "ResponseLargeResponseAllViewFactory";
		
		@Override
		public String getName() {
			return NAME;
		}
		
		@Override
		public HttpPanelView getNewView() {
			return new ResponseLargeResponseAllView(new LargeResponseStringHttpPanelViewModel());
		}

		@Override
		public Object getOptions() {
			return null;
		}
	}

	private static final class LargeResponseDefaultAllViewSelector implements HttpPanelDefaultViewSelector {

		public static final String NAME = "LargeResponseDefaultAllViewSelector";
		
		@Override
		public String getName() {
			return NAME;
		}
		
		@Override
		public boolean matchToDefaultView(Message aMessage) {
			return LargeResponseUtil.isLargeResponse(aMessage);
		}

		@Override
		public String getViewName() {
			return ResponseLargeResponseAllView.NAME;
		}
		
		@Override
		public int getOrder() {
			return 50;
		}
	}

	private static final class LargeResponseDefaultSplitViewSelectorFactory implements HttpPanelDefaultViewSelectorFactory {
		
		private static HttpPanelDefaultViewSelector defaultViewSelector = null;
		
		private HttpPanelDefaultViewSelector getDefaultViewSelector() {
			if (defaultViewSelector == null) {
				createViewSelector();
			}
			return defaultViewSelector;
		}
		
		private synchronized void createViewSelector() {
			if (defaultViewSelector == null) {
				defaultViewSelector = new LargeResponseDefaultSplitViewSelector();
			}
		}
		
		public static final String NAME = "LargeResponseDefaultSplitViewSelectorFactory";
		
		@Override
		public String getName() {
			return NAME;
		}
		
		@Override
		public HttpPanelDefaultViewSelector getNewDefaultViewSelector() {
			return getDefaultViewSelector();
		}
		
		@Override
		public Object getOptions() {
			return ResponseSplitComponent.ViewComponent.BODY;
		}
	}
	
	private static final class LargeResponseDefaultAllViewSelectorFactory implements HttpPanelDefaultViewSelectorFactory {
		
		private static HttpPanelDefaultViewSelector defaultViewSelector = null;
		
		private HttpPanelDefaultViewSelector getDefaultViewSelector() {
			if (defaultViewSelector == null) {
				createViewSelector();
			}
			return defaultViewSelector;
		}
		
		private synchronized void createViewSelector() {
			if (defaultViewSelector == null) {
				defaultViewSelector = new LargeResponseDefaultAllViewSelector();
			}
		}
		
		public static final String NAME = "LargeResponseDefaultAllViewSelectorFactory";
		
		@Override
		public String getName() {
			return NAME;
		}
		
		@Override
		public HttpPanelDefaultViewSelector getNewDefaultViewSelector() {
			return getDefaultViewSelector();
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

	/**
	 * No database tables used, so all supported
	 */
	@Override
	public boolean supportsDb(String type) {
		return true;
	}
}

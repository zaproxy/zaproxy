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
package org.zaproxy.zap.extension.httppanel.view.image;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.component.split.response.ResponseSplitComponent;
import org.zaproxy.zap.extension.httppanel.view.DefaultHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelDefaultViewSelectorFactory;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelViewFactory;

public class ExtensionHttpPanelImageView extends ExtensionAdaptor {
	
	public static final String NAME = "ExtensionHttpPanelImageView";
	
	public ExtensionHttpPanelImageView() {
		super(NAME);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    if (getView() != null) {
    		HttpPanelManager.getInstance().addResponseView(ResponseSplitComponent.NAME, new ResponseSplitBodyViewFactory());
    		
    		HttpPanelManager.getInstance().addResponseDefaultView(ResponseSplitComponent.NAME, new ImageDefaultViewSelectorFactory());
	    }
	}
	
	private static final class ResponseSplitBodyViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new ResponseImageView(new DefaultHttpPanelViewModel());
		}

		@Override
		public Object getOptions() {
			return ResponseSplitComponent.ViewComponent.BODY;
		}
	}

	private static final class ImageDefaultViewSelector implements HttpPanelDefaultViewSelector {

		@Override
		public String getName() {
			return "ImageDefaultViewSelector";
		}
		
		@Override
		public boolean matchToDefaultView(Message aMessage) {
			return ResponseImageView.isImage(aMessage);
		}

		@Override
		public String getViewName() {
			return ResponseImageView.CONFIG_NAME;
		}
		
		@Override
		public int getOrder() {
			return 20;
		}
	}

	private static final class ImageDefaultViewSelectorFactory implements HttpPanelDefaultViewSelectorFactory {
		
		private static HttpPanelDefaultViewSelector defaultViewSelector = null;
		
		private HttpPanelDefaultViewSelector getDefaultViewSelector() {
			if (defaultViewSelector == null) {
				createViewSelector();
			}
			return defaultViewSelector;
		}
		
		private synchronized void createViewSelector() {
			if (defaultViewSelector == null) {
				defaultViewSelector = new ImageDefaultViewSelector();
			}
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
	
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}
}


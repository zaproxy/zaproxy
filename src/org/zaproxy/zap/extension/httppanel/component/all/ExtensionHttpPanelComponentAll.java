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
package org.zaproxy.zap.extension.httppanel.component.all;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentInterface;
import org.zaproxy.zap.extension.httppanel.component.all.request.RequestAllComponent;
import org.zaproxy.zap.extension.httppanel.component.all.response.ResponseAllComponent;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelComponentFactory;

public class ExtensionHttpPanelComponentAll extends ExtensionAdaptor {

	public static final String NAME = "ExtensionHttpPanelComponentonentAll";
	
	public ExtensionHttpPanelComponentAll() {
		super(NAME);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
		if (getView() != null) {
			HttpPanelManager panelManager = HttpPanelManager.getInstance();
			panelManager.addRequestComponentFactory(new RequestAllComponentFactory());
			panelManager.addResponseComponentFactory(new ResponseAllComponentFactory());
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
			panelManager.removeRequestComponentFactory(RequestAllComponentFactory.NAME);
			panelManager.removeRequestComponents(RequestAllComponent.NAME);
			
			panelManager.removeResponseComponentFactory(ResponseAllComponentFactory.NAME);
			panelManager.removeResponseComponents(ResponseAllComponent.NAME);
		}
	}

	private static final class ResponseAllComponentFactory implements HttpPanelComponentFactory {
		
		public static final String NAME = "ResponseAllComponentFactory";
		
		@Override
		public String getName() {
			return NAME;
		}
		
		@Override
		public HttpPanelComponentInterface getNewComponent() {
			return new ResponseAllComponent();
		}

		@Override
		public String getComponentName() {
			return ResponseAllComponent.NAME;
		}
	}

	private static final class RequestAllComponentFactory implements HttpPanelComponentFactory {
		
		public static final String NAME = "RequestAllComponentFactory";
		
		@Override
		public String getName() {
			return NAME;
		}
		
		@Override
		public HttpPanelComponentInterface getNewComponent() {
			return new RequestAllComponent();
		}

		@Override
		public String getComponentName() {
			return RequestAllComponent.NAME;
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


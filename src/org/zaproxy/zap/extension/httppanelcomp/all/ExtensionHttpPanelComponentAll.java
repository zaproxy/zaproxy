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
package org.zaproxy.zap.extension.httppanelcomp.all;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentInterface;
import org.zaproxy.zap.extension.httppanelcomp.all.request.RequestAllComponent;
import org.zaproxy.zap.extension.httppanelcomp.all.response.ResponseAllComponent;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelComponentFactory;

public class ExtensionHttpPanelComponentAll extends ExtensionAdaptor {

	public static final String NAME = "ExtensionHttpPanelComponentAll";
	
	public ExtensionHttpPanelComponentAll() {
		super(NAME);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		
		HttpPanelManager.getInstance().addRequestComponent(new RequestAllComponentFactory());
		
		HttpPanelManager.getInstance().addResponseComponent(new ResponseAllComponentFactory());
	}

	private static final class ResponseAllComponentFactory implements HttpPanelComponentFactory {
		
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
}


/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.extension.userauth;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.extension.httpsessions.ExtensionHttpSessions;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.view.ContextPanelFactory;

public class ExtensionUserAuthentication extends ExtensionAdaptor implements ContextPanelFactory,
		ContextDataFactory {
	public static final String NAME = "ExtensionUserAuthentication";

	private Map<Integer, OptionsUserAuthUserPanel> userPanelsMap = new HashMap<>();

	public ExtensionUserAuthentication() {

		initialize();
	}

	private ExtensionHttpSessions extensionHttpSessions;
	
	/**
	 * Gets the ExtensionHttpSessions, if it's enabled
	 * 
	 * @return the Http Sessions extension or null, if it's not available
	 */
	protected ExtensionHttpSessions getExtensionHttpSessions() {
		if(extensionHttpSessions==null){
			extensionHttpSessions = (ExtensionHttpSessions) Control.getSingleton().getExtensionLoader()
					.getExtension(ExtensionHttpSessions.NAME);
		}
		return extensionHttpSessions;
		
	}
	
	private void initialize() {
		this.setName(NAME);
		this.setOrder(100);

//		 this.api = new AuthAPI(this);
//		 API.getInstance().registerApiImplementor(api);
//		 HttpSender.addListener(this);
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);
		// Register this as a context data factory
		 Model.getSingleton().addContextDataFactory(this);

		if (getView() != null) {
			// Factory for generating Session Context UserAuth panels
			 getView().addContextPanelFactory(this);

		}
	}

	@Override
	public AbstractParamPanel getContextPanel(Context ctx) {
		return getContextPanel(ctx.getIndex());
	}

	private OptionsUserAuthUserPanel getContextPanel(int contextId) {
		OptionsUserAuthUserPanel panel = this.userPanelsMap.get(contextId);
		if (panel == null) {
			panel = new OptionsUserAuthUserPanel(contextId);
			this.userPanelsMap.put(contextId, panel);
		} 
		return panel;
	}

	@Override
	public void discardContexts() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadContextData(Context ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveContextData(Context ctx) {
		// TODO Auto-generated method stub

	}

}

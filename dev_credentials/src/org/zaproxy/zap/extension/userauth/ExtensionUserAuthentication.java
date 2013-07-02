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

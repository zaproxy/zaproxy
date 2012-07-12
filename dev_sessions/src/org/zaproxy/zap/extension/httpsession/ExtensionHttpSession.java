package org.zaproxy.zap.extension.httpsession;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;

public class ExtensionHttpSession extends ExtensionAdaptor implements SessionChangedListener, ProxyListener {

	private static final String NAME = "ExtensionHttpSession";

	private static final Logger log = Logger.getLogger(ExtensionHttpSession.class);

	private HttpSessionPanel httpSessionPanel;

	public ExtensionHttpSession() {
		super();
		initialize();
	}

	private void initialize() {
		this.setOrder(68);
		this.setName(NAME);
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("httpsession.desc");
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

		extensionHook.addSessionListener(this);
		extensionHook.addProxyListener(this);

		if (getView() != null) {
			extensionHook.getHookView().addStatusPanel(getHttpSessionPanel());

			// extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuParamSearch());
			// extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAddAntiCSRF());
			// extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuRemoveAntiCSRF());
			// extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAddSession());
			// extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuRemoveSession());
		}
	}

	protected HttpSessionPanel getHttpSessionPanel() {
		if (httpSessionPanel == null) {
			httpSessionPanel = new HttpSessionPanel(this);
		}
		return httpSessionPanel;
	}

	@Override
	public int getProxyListenerOrder() {
		// TODO Auto-generated method stub
		return 20;
	}

	@Override
	public boolean onHttpRequestSend(HttpMessage msg) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onHttpResponseReceive(HttpMessage msg) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void sessionChanged(Session session) {
		log.info("Session changed."); // TODO Auto-generated method stub

	}

	@Override
	public void sessionAboutToChange(Session session) {
		log.info("Session about to change.");

	}

}

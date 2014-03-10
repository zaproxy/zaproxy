package org.zaproxy.zap.extension.auth;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.zaproxy.zap.extension.authentication.ExtensionAuthentication;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;

/**
 * Temporary, deprecated class introduced to keep old ZAP addons using the deprecated Authentication
 * extension still working.
 * 
 * TODO: Should be eliminated after ZAP 2.3 is released.
 * 
 * @deprecated The new {@link ExtensionAuthentication} and {@link ExtensionUserManagement} should be
 *             used instead.
 */
public class ExtensionAuth extends ExtensionAdaptor {

	public static final String NAME = "ExtensionAuth";
	private AuthApi api;
	protected ExtensionAuthentication extensionAuth;

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	protected ExtensionAuthentication getAuthenticationExtension() {
		Logger.getLogger(this.getClass()).warn("WARNING: Call to deprecated ExtensionAuth.");
		if (extensionAuth == null)
			extensionAuth = (ExtensionAuthentication) Control.getSingleton().getExtensionLoader()
					.getExtension(ExtensionAuthentication.NAME);
		return extensionAuth;
	}

	public ExtensionAuth() {
		super(NAME);
		this.setOrder(190);
	}

	public AuthApi getApi() {
		Logger.getLogger(this.getClass()).warn("WARNING: Call to deprecated ExtensionAuth.");
		if (api == null)
			api = new AuthApi();
		return api;
	}

}

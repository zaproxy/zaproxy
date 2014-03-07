package org.zaproxy.zap.extension.auth;

import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.authentication.FormBasedAuthenticationMethodType.FormBasedAuthenticationMethod;
import org.zaproxy.zap.extension.authentication.ExtensionAuthentication;
import org.zaproxy.zap.model.Context;

/**
 * Temporary, deprecated class introduced to keep old ZAP addons using the deprecated Authentication
 * extension still working.
 * 
 * Should be eliminated after ZAP 2.3 is released.
 * 
 * @deprecated Use the new {@link ExtensionAuthentication}
 */
public class AuthApi {

	public HttpMessage getLoginRequest(int contextID) {
		Context ctx = Model.getSingleton().getSession().getContext(contextID);
		if (!(ctx.getAuthenticationMethod() instanceof FormBasedAuthenticationMethod))
			return null;
		FormBasedAuthenticationMethod method = (FormBasedAuthenticationMethod) ctx.getAuthenticationMethod();
		try {
			return method.getLoginRequestMessage();
		} catch (Exception e) {
			return null;
		}
	}
}

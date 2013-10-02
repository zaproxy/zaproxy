package org.zaproxy.zap.extension.auth;

import java.sql.SQLException;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.authentication.FormBasedAuthenticationMethodType;
import org.zaproxy.zap.authentication.FormBasedAuthenticationMethodType.FormBasedAuthenticationMethod;
import org.zaproxy.zap.extension.authentication.ExtensionAuthentication;
import org.zaproxy.zap.model.Context;

public class ExtensionAuth extends ExtensionAdaptor {

	public static final String NAME = "ExtensionAuth";
	private AuthApi api;
	protected ExtensionAuthentication extensionAuth;

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	protected ExtensionAuthentication getAuthenticationExtension() {
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
		if (api == null)
			api = new AuthApi();
		return api;
	}

	public class AuthApi {

		public HttpMessage getLoginRequest(int contextID) {
			Context ctx = getModel().getSession().getContext(contextID);
			if (!(ctx.getAuthenticationMethod() instanceof FormBasedAuthenticationMethod))
				return null;
			FormBasedAuthenticationMethod method = (FormBasedAuthenticationMethod) ctx
					.getAuthenticationMethod();
			try {
				return method.getLoginRequestMessage();
			} catch (Exception e) {
				return null;
			}
		}
	}

}

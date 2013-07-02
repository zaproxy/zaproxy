package org.zaproxy.zap.userauth.authentication;

public abstract class AuthenticationMethodFactory<T extends AuthenticationMethod> {

	public abstract T buildAuthenticationMethod();

	public abstract String getName();

	public abstract AbstractAuthenticationMethodOptionsPanel<T> buildOptionsPanel(int contextId);

	@Override
	public String toString() {
		return getName();
	}

}

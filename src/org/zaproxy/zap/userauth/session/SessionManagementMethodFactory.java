package org.zaproxy.zap.userauth.session;

public abstract class SessionManagementMethodFactory<T extends SessionManagementMethod> {

	public abstract T buildAuthenticationMethod();

	public abstract String getName();

	public abstract AbstractSessionManagementMethodOptionsPanel<T> buildOptionsPanel();

	@Override
	public String toString() {
		return getName();
	}
}

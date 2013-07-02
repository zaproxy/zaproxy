package org.zaproxy.zap.userauth.authentication;

import javax.swing.JPanel;

/**
 * The Class AbstractSessionManagementMethodOptionsPanel.
 * 
 * @param <T> the generic type
 */
public abstract class AbstractAuthenticationMethodOptionsPanel<T extends AuthenticationMethod> extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9003182467823059637L;

	protected T method;

	public AbstractAuthenticationMethodOptionsPanel(AuthenticationMethodFactory<T> factory) {
		super();
		method = factory.buildAuthenticationMethod();
	}

	public AbstractAuthenticationMethodOptionsPanel(T existingMethod) {
		super();
		method = existingMethod;
	}

	public abstract boolean validateFields();

	public abstract void saveMethod();

	public T getMethod() {
		return method;
	}

}

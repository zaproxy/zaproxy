package org.zaproxy.zap.userauth.session;

import javax.swing.JPanel;

/**
 * The Class AbstractSessionManagementMethodOptionsPanel.
 * 
 * @param <T> the generic type
 */
public abstract class AbstractSessionManagementMethodOptionsPanel<T extends SessionManagementMethod> extends
		JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9003182467823059637L;

	public AbstractSessionManagementMethodOptionsPanel() {
		super();
		initialize();
	}

	protected void initialize() {

	}

	public abstract void validateFields();

	public abstract void saveMethod();

	public abstract T getMethod();

}

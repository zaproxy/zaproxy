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
package org.zaproxy.zap.userauth.session;

import javax.swing.JPanel;

/**
 * An Options Panel that is used to configure all the settings corresponding to an
 * {@link SessionManagementMethod}.<br/>
 * <br/>
 * This panel will be displayed to users in a separate dialog.
 * 
 * @param <T> the session management method type
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

	/**
	 * Validate the fields of the configuration panel.
	 */
	public abstract void validateFields();

	/**
	 * Save the changes from the panel in the session management method.
	 */
	public abstract void saveMethod();

	/**
	 * Gets the session management method configured by this panel.
	 * 
	 * @return the method
	 */
	public abstract T getMethod();

}

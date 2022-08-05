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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.authentication;

import javax.swing.JPanel;

/**
 * An Options Panel that is used to configure all the settings corresponding to an {@link
 * AuthenticationCredentials}.
 *
 * <p>This panel will be displayed to users in a separate dialog.
 *
 * @param <T> the authenticator type
 */
@SuppressWarnings("serial")
public abstract class AbstractCredentialsOptionsPanel<T extends AuthenticationCredentials>
        extends JPanel {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -199406099430582188L;

    /** The method. */
    protected T credentials;

    /**
     * Instantiates a new abstract options panel for configuring {@link AuthenticationCredentials}.
     *
     * @param credentials the authentication credentials to be configured.
     */
    public AbstractCredentialsOptionsPanel(T credentials) {
        super();
        this.credentials = credentials;
    }

    /**
     * Validate the fields.
     *
     * @return true, if successful
     */
    public abstract boolean validateFields();

    /**
     * Save the changes from the panel in the credentials. After this method call, calls to {@link
     * AbstractCredentialsOptionsPanel#getCredentials()} should return the credentials with the
     * saved changes.
     */
    public abstract void saveCredentials();

    /**
     * Gets the corresponding authenticator.
     *
     * @return the authenticator
     */
    public T getCredentials() {
        return credentials;
    }
}

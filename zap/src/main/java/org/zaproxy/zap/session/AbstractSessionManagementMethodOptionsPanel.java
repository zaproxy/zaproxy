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
package org.zaproxy.zap.session;

import javax.swing.JPanel;
import org.zaproxy.zap.session.SessionManagementMethodType.UnsupportedSessionManagementMethodException;

/**
 * An Options Panel that is used to configure all the settings corresponding to an {@link
 * SessionManagementMethod}.
 *
 * <p>This panel will be displayed to users in a separate dialog.
 */
public abstract class AbstractSessionManagementMethodOptionsPanel extends JPanel {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 9003182467823059637L;

    public AbstractSessionManagementMethodOptionsPanel() {
        super();
    }

    /**
     * Binds (loads) data from an existing Session Management method in the panel. After this
     * method, the {@link #getMethod()} should return the same object, eventually with some changes
     * (if {@link #saveMethod()} was called).
     *
     * @param method the method to be loaded/shown in the panel.
     * @throws UnsupportedSessionManagementMethodException if the given method is not supported.
     */
    public abstract void bindMethod(SessionManagementMethod method)
            throws UnsupportedSessionManagementMethodException;

    /**
     * Validate the fields of the configuration panel. If any of the fields are not in the proper
     * state, an {@link IllegalStateException} is thrown, containing a message describing the
     * problem.
     *
     * @throws IllegalStateException if any of the fields are not in the valid state
     */
    public abstract void validateFields() throws IllegalStateException;

    /** Save the changes from the panel in the session management method. */
    public abstract void saveMethod();

    /**
     * Gets the session management method configured by this panel.
     *
     * @return the method
     */
    public abstract SessionManagementMethod getMethod();
}

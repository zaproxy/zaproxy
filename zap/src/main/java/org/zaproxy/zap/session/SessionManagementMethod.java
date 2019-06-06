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

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiResponse;

/**
 * The {@link SessionManagementMethod} represents a session management method that can be used to
 * manage an existing Web Session corresponding to an entity (user) interacting with a particular
 * WebApplication.
 */
public interface SessionManagementMethod {

    /**
     * Checks if the session management method is fully configured.
     *
     * @return true, if is configured
     */
    public boolean isConfigured();

    /**
     * Gets the {@link SessionManagementMethodType} corresponding to this session management method.
     *
     * <p>Implementations may return new instantiations at every call, so performance considerations
     * should be taken by users.
     *
     * @return the type
     */
    public SessionManagementMethodType getType();

    /**
     * Clones the current session management method, creating a deep-copy of it.
     *
     * @return a deep copy of the session management method
     */
    public SessionManagementMethod clone();

    /**
     * Extracts the web session information from a Http Message, creating a {@link WebSession}
     * object corresponding to the Session Management Method.
     *
     * @param msg the msg
     * @return the web session
     */
    public WebSession extractWebSession(HttpMessage msg);

    /**
     * Creates an empty web session.
     *
     * @return the web session
     */
    public WebSession createEmptyWebSession();

    /**
     * Clears any tokens or elements that can link the HttpMessage provided as a parameter to a
     * WebSession.
     *
     * @param msg the http message
     */
    public void clearWebSessionIdentifiers(HttpMessage msg);

    /**
     * Gets an api response that represents the Session Management Method.
     *
     * @return the api response representation
     */
    public abstract ApiResponse getApiResponseRepresentation();

    /**
     * Modifies a message so its Request Header/Body matches the given web session, according to
     * this session management method.
     *
     * @param message the message
     * @param session the session
     * @throws UnsupportedWebSessionException if the web session type is unsupported
     */
    public void processMessageToMatchSession(HttpMessage message, WebSession session)
            throws UnsupportedWebSessionException;

    /**
     * Thrown when an unsupported type of web session is used with a {@link SessionManagementMethod}
     * .
     */
    public class UnsupportedWebSessionException extends RuntimeException {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 4802501809913124766L;

        public UnsupportedWebSessionException(String message) {
            super(message);
        }
    }
}

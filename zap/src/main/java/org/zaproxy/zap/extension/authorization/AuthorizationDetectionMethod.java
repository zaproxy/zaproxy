/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.authorization;

import org.apache.commons.configuration.Configuration;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.model.Context;

/** Defines the process of identifying how the server responds to unauthorized requests. */
public interface AuthorizationDetectionMethod {

    public static final String CONTEXT_CONFIG_AUTH = Context.CONTEXT_CONFIG + ".authorization";
    public static final String CONTEXT_CONFIG_AUTH_TYPE = CONTEXT_CONFIG_AUTH + ".type";

    /** Checks whether the responds received was for an unauthorized request. */
    public boolean isResponseForUnauthorizedRequest(HttpMessage message);

    /**
     * Clones this detection method, creating a deep-copy of it.
     *
     * @return a deep copy of the object
     */
    public AuthorizationDetectionMethod clone();

    /**
     * Gets a unique identifier for this authorization detection method.
     *
     * @return the method id
     */
    public int getMethodUniqueIdentifier();

    /**
     * Persists the method in the session database.
     *
     * @param session the session
     * @param contextId the context id
     * @throws DatabaseException if an error occurred while reading from the database
     */
    public void persistMethodToSession(Session session, int contextId) throws DatabaseException;

    /**
     * Export the method data to the configuration
     *
     * @param config
     */
    public void exportMethodData(Configuration config);

    /**
     * Gets the api response representation of the authorization method.
     *
     * @return the api response representation
     */
    public ApiResponse getApiResponseRepresentation();
}

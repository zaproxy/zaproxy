/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.model;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.parosproxy.paros.model.Session;

/**
 * A Context Data Factory implements the logic for persisting and loading data into {@link Context
 * Contexts} into/from a {@link Session}, for external (non-core) components.
 */
public interface ContextDataFactory {

    /**
     * Loads the Context data from the given session.
     *
     * @param session the session to load from, not {@code null}.
     * @param context the context to load into, not {@code null}.
     */
    void loadContextData(Session session, Context context);

    /**
     * Save the Context data into the given session.
     *
     * @param session the session to persist into, not {@code null}.
     * @param context the context to persist from, not {@code null}.
     */
    void persistContextData(Session session, Context context);

    /**
     * Export the Context data into the given configuration
     *
     * @param ctx the context to export, not {@code null}.
     * @param config the {@code Configuration} where to export the context data, not {@code null}.
     */
    void exportContextData(Context ctx, Configuration config);

    /**
     * Import the Context data from the given configuration
     *
     * @param ctx the context to import the context data to
     * @param config the {@code Configuration} containing the context data, not {@code null}.
     * @throws ConfigurationException if an error occurred while reading the context data from the
     *     {@code Configuration}, not {@code null}.
     */
    void importContextData(Context ctx, Configuration config) throws ConfigurationException;
}

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
 *   http://www.apache.org/licenses/LICENSE-2.0 
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
	 * @param session the session to load from
	 * @param context the context to load into
	 */
	void loadContextData(Session session, Context context);

	/**
	 * Save the Context data into the given session.
	 * 
	 * @param session the session to persist into
	 * @param context the context to persist from
	 */
	void persistContextData(Session session, Context context);

	/**
	 * Export the Context data into the given configuration
	 * @param ctx
	 * @param config
	 */
	void exportContextData(Context ctx, Configuration config);

	/**
	 * Import the Context data from the given configuration
	 * @param ctx
	 * @param config
	 * @throws Exception 
	 */
	void importContextData(Context ctx, Configuration config) throws ConfigurationException;
}

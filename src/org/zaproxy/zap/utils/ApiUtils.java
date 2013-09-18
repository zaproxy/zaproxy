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
package org.zaproxy.zap.utils;

import net.sf.json.JSONObject;

import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.model.Context;

/**
 * Utils for manipulating API calls and parameters.
 */
public class ApiUtils {

	/**
	 * Gets the int param with a given name and throws an exception accordingly if not found or
	 * valid.
	 * 
	 * @param params the params
	 * @param paramName the param name
	 * @return the int param
	 * @throws ApiException the api exception
	 */
	public static int getIntParam(JSONObject params, String paramName) throws ApiException {
		int value;
		try {
			value = params.getInt(paramName);
		} catch (Exception ex) {
			throw new ApiException(Type.MISSING_PARAMETER, paramName + ": " + ex.getLocalizedMessage());
		}
		return value;
	}

	/**
	 * Gets the non empty string param with a given name and throws an exception accordingly if not
	 * found or empty.
	 * 
	 * @param params the params
	 * @param paramName the param name
	 * @return the non empty string param
	 * @throws ApiException the api exception thown if param not found or string empty
	 */
	public static String getNonEmptyStringParam(JSONObject params, String paramName) throws ApiException {
		if (!params.containsKey(paramName))
			throw new ApiException(Type.MISSING_PARAMETER, paramName);
		String value = params.getString(paramName);
		if (value == null || value.isEmpty())
			throw new ApiException(Type.MISSING_PARAMETER, paramName);
		return value;
	}

	/**
	 * Gets the {@link Context} whose id is provided as a parameter with the given name. Throws an
	 * exception accordingly if not found or valid.
	 * 
	 * @param params the params
	 * @param contextIdParamName the context id param name
	 * @return the context
	 * @throws ApiException the api exception
	 */
	public static Context getContextByParamId(JSONObject params, String contextIdParamName)
			throws ApiException {
		int contextId = getIntParam(params, contextIdParamName);
		Context context = Model.getSingleton().getSession().getContext(contextId);
		if (context == null)
			throw new ApiException(Type.CONTEXT_NOT_FOUND, contextIdParamName);
		return context;
	}
}

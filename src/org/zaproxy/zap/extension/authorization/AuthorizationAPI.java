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
package org.zaproxy.zap.extension.authorization;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.extension.authorization.BasicAuthorizationDetectionMethod.LogicalOperator;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.ApiUtils;

/**
 * The API for managing the Authorization for a Context.
 */
public class AuthorizationAPI extends ApiImplementor {

	private static final Logger log = Logger.getLogger(AuthorizationAPI.class);

	private static final String PREFIX = "authorization";

	private static final String VIEW_GET_AUTHORIZATION_METHOD = "getAuthorizationDetectionMethod";

	private static final String ACTION_SET_AUTHORIZATION_METHOD = "setBasicAuthorizationDetectionMethod";

	public static final String PARAM_CONTEXT_ID = "contextId";
	public static final String PARAM_HEADER_REGEX = "headerRegex";
	public static final String PARAM_BODY_REGEX = "bodyRegex";
	public static final String PARAM_STATUS_CODE = "statusCode";
	public static final String PARAM_LOGICAL_OPERATOR = "logicalOperator";
	public static final String RESPONSE_TYPE = "methodType";
	public static final String RESPONSE_TAG = "authorizationDetectionMethod";

	public AuthorizationAPI() {
		super();

		this.addApiView(new ApiView(VIEW_GET_AUTHORIZATION_METHOD, new String[] { PARAM_CONTEXT_ID }));

		this.addApiAction(new ApiAction(ACTION_SET_AUTHORIZATION_METHOD, new String[] { PARAM_CONTEXT_ID },
				new String[] { PARAM_HEADER_REGEX, PARAM_BODY_REGEX, PARAM_STATUS_CODE,
						PARAM_LOGICAL_OPERATOR }));
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
		log.debug("handleApiView " + name + " " + params.toString());

		switch (name) {
		case VIEW_GET_AUTHORIZATION_METHOD:
			Context context = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);
			return new ApiResponseElement(context.getAuthorizationDetectionMethod().getApiResponseRepresentation());
		default:
			throw new ApiException(Type.BAD_VIEW);
		}
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("handleApiAction " + name + " " + params.toString());
		Context context;
		switch (name) {
		case ACTION_SET_AUTHORIZATION_METHOD:
			context = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);

			String headerRegex = params.optString(PARAM_HEADER_REGEX, null);
			String bodyRegex = params.optString(PARAM_BODY_REGEX, null);

			LogicalOperator logicalOperator = ApiUtils.getOptionalEnumParam(params, PARAM_LOGICAL_OPERATOR,
					LogicalOperator.class);
			if (logicalOperator == null) {
				logicalOperator = LogicalOperator.AND;
			}

			int statusCode = params.optInt(PARAM_STATUS_CODE,
					BasicAuthorizationDetectionMethod.NO_STATUS_CODE);

			if (log.isDebugEnabled()) {
				log.debug(String.format("Setting basic authorization detection to: %s / %s / %d / %s",
						headerRegex, bodyRegex, statusCode, logicalOperator));
			}

			BasicAuthorizationDetectionMethod method = new BasicAuthorizationDetectionMethod(statusCode,
					headerRegex, bodyRegex, logicalOperator);
			context.setAuthorizationDetectionMethod(method);

			return ApiResponseElement.OK;
		default:
			throw new ApiException(Type.BAD_ACTION);
		}
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
}

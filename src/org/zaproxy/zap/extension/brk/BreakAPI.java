/*
 /*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.brk;

import net.sf.json.JSONObject;

import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;

public class BreakAPI extends ApiImplementor {


	private static final String PREFIX = "break";

	private static final String ACTION_BREAK = "break";
	private static final String ACTION_BREAK_ON_ID = "breakOnId";
	private static final String ACTION_ADD_HTTP_BREAK_POINT = "addHttpBreakpoint";
	private static final String ACTION_REM_HTTP_BREAK_POINT = "removeHttpBreakpoint";

	private static final String PARAM_STRING = "string";
	private static final String PARAM_LOCATION = "location";
	private static final String PARAM_MATCH = "match";
	private static final String PARAM_INVERSE = "inverse";
	private static final String PARAM_IGNORECASE = "ignorecase";
	private static final String PARAM_KEY = "key";
	private static final String PARAM_SCOPE = "scope";
	private static final String PARAM_STATE = "state";
	private static final String PARAM_TYPE = "type";

	private static final String VALUE_TYPE_HTTP_ALL = "http-all";
	private static final String VALUE_TYPE_HTTP_REQUESTS = "http-requests";
	private static final String VALUE_TYPE_HTTP_RESPONSES = "http-responses";
	

	private ExtensionBreak extension = null;

	public BreakAPI(ExtensionBreak ext) {
		extension = ext;
		
		this.addApiAction(new ApiAction(ACTION_BREAK, new String[] {PARAM_TYPE, PARAM_SCOPE, PARAM_STATE}));
		// Disable pending other changes
		//this.addApiAction(new ApiAction(ACTION_BREAK_ON_ID, new String[] {PARAM_KEY, PARAM_STATE}));
		this.addApiAction(new ApiAction(ACTION_ADD_HTTP_BREAK_POINT, 
				new String[] {PARAM_STRING, PARAM_LOCATION, PARAM_MATCH, PARAM_INVERSE, PARAM_IGNORECASE}));
		this.addApiAction(new ApiAction(ACTION_REM_HTTP_BREAK_POINT, 
				new String[] {PARAM_STRING, PARAM_LOCATION, PARAM_MATCH, PARAM_INVERSE, PARAM_IGNORECASE}));
		
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		if (ACTION_BREAK.equals(name)) {
			String type = params.getString(PARAM_TYPE).toLowerCase();
			if (type.equals(VALUE_TYPE_HTTP_ALL)) {
				extension.setBreakAllRequests(params.getBoolean(PARAM_STATE));
				extension.setBreakAllResponses(params.getBoolean(PARAM_STATE));
			} else if (type.equals(VALUE_TYPE_HTTP_REQUESTS)) {
				extension.setBreakAllRequests(params.getBoolean(PARAM_STATE));
			} else if (type.equals(VALUE_TYPE_HTTP_RESPONSES)) {
				extension.setBreakAllResponses(params.getBoolean(PARAM_STATE));
			} else {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_TYPE +
						" not in [" + VALUE_TYPE_HTTP_ALL +"," + VALUE_TYPE_HTTP_REQUESTS +"," +
						VALUE_TYPE_HTTP_RESPONSES +"]");
			}
			
		} else if (ACTION_BREAK_ON_ID.equals(name)) {
			extension.setBreakOnId(params.getString(PARAM_KEY), params.getString(PARAM_STATE).toLowerCase().equals("on"));

		} else if (ACTION_ADD_HTTP_BREAK_POINT.equals(name)) {
			try {
				extension.addHttpBreakpoint(
						params.getString(PARAM_STRING), 
						params.getString(PARAM_LOCATION), 
						params.getString(PARAM_MATCH), 
						params.getBoolean(PARAM_INVERSE), 
						params.getBoolean(PARAM_IGNORECASE));
			} catch (Exception e) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, e.getMessage());
			}

		} else if (ACTION_REM_HTTP_BREAK_POINT.equals(name)) {
			try {
				extension.removeHttpBreakpoint(
						params.getString(PARAM_STRING), 
						params.getString(PARAM_LOCATION), 
						params.getString(PARAM_MATCH), 
						params.getBoolean(PARAM_INVERSE), 
						params.getBoolean(PARAM_IGNORECASE));
			} catch (Exception e) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, e.getMessage());
			}

		} else {
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
		return ApiResponseElement.OK;

	}
	
}

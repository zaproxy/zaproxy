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
package org.zaproxy.zap.extension.pscan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;

public class PassiveScanAPI extends ApiImplementor {

	private static final String PREFIX = "pscan";
	
	private static final String VIEW_RECORDS_TO_SCAN = "recordsToScan";
	private static final String VIEW_SCANNERS = "scanners";

	private static final String ACTION_SET_ENABLED = "setEnabled";

	private static final String PARAM_ENABLED = "enabled";

	private ExtensionPassiveScan extension;
	
	public PassiveScanAPI (ExtensionPassiveScan extension) {
		this.extension = extension;

		this.addApiAction(new ApiAction(ACTION_SET_ENABLED, new String[] {PARAM_ENABLED}));

		this.addApiView(new ApiView(VIEW_RECORDS_TO_SCAN));
		this.addApiView(new ApiView(VIEW_SCANNERS));

	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		switch (name) {
		case ACTION_SET_ENABLED:
			boolean enabled = getParam(params, PARAM_ENABLED, false);
			
			extension.setPassiveScanEnabled(enabled);
			break;
		default:
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}

		return ApiResponseElement.OK;
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params)
			throws ApiException {
		ApiResponse result;

		switch (name) {
		case VIEW_RECORDS_TO_SCAN:
			result = new ApiResponseList(name);
			result = new ApiResponseElement(name, String.valueOf(extension.getRecordsToScan()));
			break;
		case VIEW_SCANNERS:
			List<PluginPassiveScanner> scanners = extension.getPluginPassiveScanners();
			
			ApiResponseList resultList = new ApiResponseList(name);
			for (PluginPassiveScanner scanner : scanners) {
				Map<String, String> map = new HashMap<>();
				map.put("id", String.valueOf(scanner.getPluginId()));
				map.put("name", scanner.getName());
				resultList.addItem(new ApiResponseSet("scanner", map));
			}
			
			result = resultList;
			break;
		default:
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}
	
}

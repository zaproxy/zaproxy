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

import net.sf.json.JSONObject;

import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiView;

public class PassiveScanAPI extends ApiImplementor {

	private static final String PREFIX = "pscan";
	
	private static final String VIEW_RECORDS_TO_SCAN = "recordsToScan";

	private ExtensionPassiveScan extension;
	
	public PassiveScanAPI (ExtensionPassiveScan extension) {
		this.extension = extension;
		this.addApiView(new ApiView(VIEW_RECORDS_TO_SCAN));

	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params)
			throws ApiException {
		ApiResponse result;

		if (VIEW_RECORDS_TO_SCAN.equals(name)) {
			result = new ApiResponseList(name);
			result = new ApiResponseElement(name, "" + extension.getRecordsToScan());
		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}
	
}

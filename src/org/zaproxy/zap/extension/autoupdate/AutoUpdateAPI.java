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
package org.zaproxy.zap.extension.autoupdate;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiView;

public class AutoUpdateAPI extends ApiImplementor {

    private static Logger log = Logger.getLogger(AutoUpdateAPI.class);

	private static final String PREFIX = "autoupdate";
	private static final String DOWNLOAD_LATEST_RELEASE = "downloadLatestRelease";
	private static final String VIEW_LATEST_VERSION_NUMBER = "latestVersionNumber";
	private static final String VIEW_IS_LATEST_VERSION = "isLatestVersion";
	
	private ExtensionAutoUpdate extension;
	
	public AutoUpdateAPI (ExtensionAutoUpdate extension) {
		this.extension = extension;
		this.addApiAction(new ApiAction(DOWNLOAD_LATEST_RELEASE));
		this.addApiView(new ApiView(VIEW_LATEST_VERSION_NUMBER));
		this.addApiView(new ApiView(VIEW_IS_LATEST_VERSION));

	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("handleApiAction " + name + " " + params.toString());
		if (DOWNLOAD_LATEST_RELEASE.equals(name)) {
			if (this.downloadLatestRelease()) {
				return ApiResponseElement.OK;
			} else {
				return ApiResponseElement.FAIL;
			}

		} else {
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params)
			throws ApiException {
		ApiResponse result;
		if (VIEW_LATEST_VERSION_NUMBER.equals(name)) {
			result = new ApiResponseElement(name, this.getLatestVersionNumber());
		} else if (VIEW_IS_LATEST_VERSION.equals(name)) {
			result = new ApiResponseElement(name, Boolean.toString(this.isLatestVersion()));
		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}
	
    public String getLatestVersionNumber() {
    	return extension.getLatestVersionNumber();
    }

    public boolean isLatestVersion() {
    	return extension.isLatestVersion();
    }

    public boolean downloadLatestRelease() {
    	return extension.downloadLatestRelease();
    }
}

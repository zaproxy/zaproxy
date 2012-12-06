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

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiView;

public class AutoUpdateAPI extends ApiImplementor {

    private static Logger log = Logger.getLogger(AutoUpdateAPI.class);

	private static final String PREFIX = "autoupdate";
	private static final String DOWNLOAD_LATEST_RELEASE = "download_latest_release";
	private static final String VIEW_LATEST_VERSION_NUMBER = "latest_version_number";
	private static final String VIEW_IS_LATEST_VERSION = "is_latest_version";
	private static final String ACTION_SCANSITE_PARAM_URL = "url";
	
	private ExtensionAutoUpdate extension;
	
	public AutoUpdateAPI (ExtensionAutoUpdate extension) {
		this.extension = extension;
		List<String> scanParams = new ArrayList<>(1);
		scanParams.add(ACTION_SCANSITE_PARAM_URL);
		this.addApiAction(new ApiAction(DOWNLOAD_LATEST_RELEASE, scanParams));
		this.addApiView(new ApiView(VIEW_LATEST_VERSION_NUMBER));
		this.addApiView(new ApiView(VIEW_IS_LATEST_VERSION));

	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public JSON handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("handleApiAction " + name + " " + params.toString());
		JSONArray result = new JSONArray();
		if (DOWNLOAD_LATEST_RELEASE.equals(name)) {
			if (this.downloadLatestRelease()) {
				result.add("OK");
			} else {
				result.add("FAIL");
			}
			return result;

		} else {
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
	}


	@Override
	public JSON handleApiView(String name, JSONObject params)
			throws ApiException {
		JSONArray result = new JSONArray();
		if (VIEW_LATEST_VERSION_NUMBER.equals(name)) {
			result.add(this.getLatestVersionNumber());
		} else if (VIEW_IS_LATEST_VERSION.equals(name)) {
			result.add(this.isLatestVersion());
		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}
	/*
	@Override
	public String viewResultToXML (String name, JSON result) {
		XMLSerializer serializer = new XMLSerializer();
		if (VIEW_STATUS.equals(name)) {
			serializer.setArrayName("status");
			serializer.setElementName("percent");
		}
		return serializer.write(result);
	}
	*/

	@Override
	public String actionResultToXML (String name, JSON result) {
		XMLSerializer serializer = new XMLSerializer();
		serializer.setArrayName("result");
		return serializer.write(result);
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

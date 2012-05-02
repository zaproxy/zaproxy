/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 The ZAP development team
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

package org.zaproxy.zap.extension.params;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiView;

public class ParamsAPI extends ApiImplementor {

	private static final String PREFIX = "params";
	private static final String VIEW_PARAMS = "params";
	private static final String VIEW_PARAMS_PARAM_SITE = "site";

	private ExtensionParams extension;
	
	public ParamsAPI (ExtensionParams extension) {
		this.extension = extension;
		List<String> paramViewParams = new ArrayList<String>(1);
		paramViewParams.add(VIEW_PARAMS_PARAM_SITE);
		this.addApiView(new ApiView(VIEW_PARAMS, paramViewParams));

	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public JSON handleApiAction(String name, JSONObject params)
			throws ApiException {
		throw new ApiException(ApiException.Type.BAD_ACTION);
	}

	@Override
	public JSON handleApiView(String name, JSONObject params)
			throws ApiException {
		JSONArray result = new JSONArray();
		if (VIEW_PARAMS.equals(name)) {
			Collection<SiteParameters> siteParams = extension.getAllSiteParameters();
			for (SiteParameters siteParam : siteParams) {
				result.add(siteParam.toJSON());
			}
		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}

}

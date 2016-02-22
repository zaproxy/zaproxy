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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.utils.ApiUtils;

public class ParamsAPI extends ApiImplementor {

	private static final String PREFIX = "params";
	private static final String VIEW_PARAMS = "params";
	private static final String VIEW_PARAMS_PARAM_SITE = "site";

	private ExtensionParams extension;
	
	public ParamsAPI (ExtensionParams extension) {
		this.extension = extension;
		this.addApiView(new ApiView(VIEW_PARAMS, new String[]{}, new String[]{VIEW_PARAMS_PARAM_SITE}));

	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params)
			throws ApiException {
		if (VIEW_PARAMS.equals(name)) {
			ApiResponseList result = new ApiResponseList("Parameters");
			if (params.containsKey(VIEW_PARAMS_PARAM_SITE)) {
				String paramSite = params.getString(VIEW_PARAMS_PARAM_SITE);
				if (!paramSite.isEmpty()) {
					String site = ApiUtils.getAuthority(paramSite);
					if (!extension.hasSite(site)) {
						throw new ApiException(ApiException.Type.DOES_NOT_EXIST, paramSite);
					}

					if (extension.hasParameters(site)) {
						result.addItem(createSiteParamStatsResponse(extension.getSiteParameters(site)));
					}
					return result;
				}
			}

			Collection<SiteParameters> siteParams = extension.getAllSiteParameters();
			for (SiteParameters siteParam : siteParams) {
				result.addItem(createSiteParamStatsResponse(siteParam));
			}
			return result;
			
		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
	}

	private static ApiResponseList createSiteParamStatsResponse(SiteParameters siteParam) {
		ApiResponseList stats = new ApiResponseList("Parameter");
		for (HtmlParameterStats param : siteParam.getParams()) {
			Map<String, String> map = new HashMap<>();
			map.put("site", param.getSite());
			map.put("name", param.getName());
			map.put("type", param.getType().name());
			map.put("timesUsed", String.valueOf(param.getTimesUsed()));
			stats.addItem(new ApiResponseSet("Stats", map));

			ApiResponseList flags = new ApiResponseList("Flags");
			for (String flag : param.getFlags()) {
				flags.addItem(new ApiResponseElement("Flag", flag));
			}
			if (param.getFlags().size() > 0) {
				stats.addItem(flags);
			}

			ApiResponseList vals = new ApiResponseList("Values");
			for (String value : param.getValues()) {
				vals.addItem(new ApiResponseElement("Value", value));
			}
			if (param.getValues().size() > 0) {
				stats.addItem(vals);
			}
		}
		return stats;
	}

}

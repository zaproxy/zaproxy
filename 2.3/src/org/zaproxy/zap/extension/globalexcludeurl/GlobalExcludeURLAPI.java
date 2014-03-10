/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development team
 * Copyright 2014 Jay Ball - Aspect Security
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
package org.zaproxy.zap.extension.globalexcludeurl;

import net.sf.json.JSONObject;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiOther;

/** TODO The GlobalExcludeURL functionality is currently alpha and subject to change.  */
public class GlobalExcludeURLAPI extends ApiImplementor {

	private static final String PREFIX = "globalexcludeurl";

	private static final String OTHER_GENERATE_FORM = "genForm";
	private static final String OTHER_GENERATE_FORM_PARAM_HREFID = "hrefId";
	
	private ExtensionGlobalExcludeURL extension = null;
	
	public GlobalExcludeURLAPI(ExtensionGlobalExcludeURL ext) {
		this.extension = ext;
		this.addApiOthers(new ApiOther(OTHER_GENERATE_FORM, new String[] {OTHER_GENERATE_FORM_PARAM_HREFID}));
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
	

}

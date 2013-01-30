/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development team
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
package org.zaproxy.zap.extension.anticsrf;

import net.sf.json.JSONObject;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiOther;

public class AntiCsrfAPI extends ApiImplementor {

	private static final String PREFIX = "acsrf";

	private static final String OTHER_GENERATE_FORM = "genForm";
	private static final String OTHER_GENERATE_FORM_PARAM_HREFID = "hrefId";
	
	// Format: http://zap/OTHER/acsrf/other/genForm/?hrefId=458
	public static final String ANTI_CSRF_FORM_URL = 
			API.API_URL + API.Format.OTHER.name() + "/" + PREFIX + "/" + API.RequestType.other.name() + 
				"/" + OTHER_GENERATE_FORM + "/?" + OTHER_GENERATE_FORM_PARAM_HREFID + "=";

	private ExtensionAntiCSRF extension = null;
	
	public AntiCsrfAPI(ExtensionAntiCSRF ext) {
		this.extension = ext;
		this.addApiOthers(new ApiOther(OTHER_GENERATE_FORM, new String[] {OTHER_GENERATE_FORM_PARAM_HREFID}));
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public HttpMessage handleApiOther(HttpMessage msg, String name, JSONObject params) throws ApiException {
		if (OTHER_GENERATE_FORM.equals(name)) {
			String hrefIdStr = params.getString(OTHER_GENERATE_FORM_PARAM_HREFID);
			if (hrefIdStr == null || hrefIdStr.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, OTHER_GENERATE_FORM_PARAM_HREFID);
			}
			int hrefId;
			try {
				hrefId = Integer.parseInt(hrefIdStr);
				
		    	String response = extension.generateForm(hrefId);
		    	if (response == null) {
					throw new ApiException(ApiException.Type.HREF_NOT_FOUND, hrefIdStr);
		    	}
		    	msg.setResponseHeader(
		    			"HTTP/1.1 200 OK\r\n" +
		    			"Pragma: no-cache\r\n" +
		  				"Cache-Control: no-cache\r\n" + 
		    			"Content-Length: " + response.length() + 
		    			"\r\nContent-Type: text/html;");
		    	msg.setResponseBody(response);
				
			} catch (NumberFormatException e) {
				throw new ApiException(ApiException.Type.BAD_FORMAT, OTHER_GENERATE_FORM_PARAM_HREFID);
			} catch (Exception e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR);
			}
			
		} else {
			throw new ApiException(ApiException.Type.BAD_OTHER, name);
		}
		return msg;
	}

}

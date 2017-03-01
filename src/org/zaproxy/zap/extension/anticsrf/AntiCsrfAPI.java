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

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiOther;

public class AntiCsrfAPI extends ApiImplementor {

	private static final String PREFIX = "acsrf";

	private static final String OTHER_GENERATE_FORM = "genForm";
	private static final String OTHER_GENERATE_FORM_PARAM_HREFID = "hrefId";
	
	private ExtensionAntiCSRF extension = null;
	
	public AntiCsrfAPI(ExtensionAntiCSRF ext) {
		this.extension = ext;
		this.addApiOthers(new ApiOther(OTHER_GENERATE_FORM, new String[] {OTHER_GENERATE_FORM_PARAM_HREFID}));
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
	
	public static String getAntiCsrfFormUrl(int hrefid) {
		return API.getInstance().getBaseURL(API.Format.OTHER, PREFIX, API.RequestType.other, OTHER_GENERATE_FORM, false) +
				OTHER_GENERATE_FORM_PARAM_HREFID + "=" + hrefid;
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

				// Get the charset from the original message
				ExtensionHistory extHist = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
				String charset = extHist.getHistoryReference(hrefId).getHttpMessage().getResponseHeader().getCharset();
				if (charset == null || charset.length() == 0) {
				    charset = "";
				} else {
				    charset = " charset=" + charset;
				}

	            msg.setResponseHeader(API.getDefaultResponseHeader("text/html; " + charset, response.length()));
		    	msg.setResponseBody(response);
				
			} catch (NumberFormatException e) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, OTHER_GENERATE_FORM_PARAM_HREFID);
			} catch (ApiException e) {
				throw e;
			} catch (Exception e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR);
			}
			
		} else {
			throw new ApiException(ApiException.Type.BAD_OTHER, name);
		}
		return msg;
	}

}

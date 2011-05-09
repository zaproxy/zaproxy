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
package org.zaproxy.zap.extension.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpInputStream;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpOutputStream;
import org.parosproxy.paros.network.HttpRequestHeader;


public class API {
	public enum Format {XML, HTML, JSON};
	public enum RequestType {action, view};
	
	public static String API_URL = "http://zap/";

	private static Pattern patternParam = Pattern.compile("&", Pattern.CASE_INSENSITIVE);

	private Map<String, ApiImplementor> implementors = new HashMap<String, ApiImplementor>();
	private static API api = null;
	
    private Logger logger = Logger.getLogger(this.getClass());

	private static synchronized API newInstance() {
		if (api == null) {
			api = new API();
		}
		return api;
	}
	
	public static API getInstance() {
		if (api == null) {
			newInstance();
		}
		return api;
	}

	public void registerApiImplementor (ApiImplementor impl) {
		if (implementors.get(impl.getPrefix()) != null) {
			logger.error("Second attempt to register API implementor with prefix of " + impl.getPrefix());
			return;
		}
		implementors.put(impl.getPrefix(), impl);
	}
	
	public boolean handleApiRequest (HttpRequestHeader requestHeader, HttpInputStream httpIn, 
			HttpOutputStream httpOut) throws IOException {
		String url = requestHeader.getURI().toString();
		Format format = Format.HTML;
		if ( ! url.startsWith(API_URL)) {
			return false;
		}
		
		HttpMessage msg = new HttpMessage();
		msg.setRequestHeader(requestHeader);
		String contentType = "text/plain";
		String response = "";
		
		try {
			// Parse the query:
			// format of url is http://zaproxy/format/component/reqtype/name/?params
			//                    1       2      3        4        5      6
			String[] elements = url.split("/");
			if (elements.length < 6) {
				throw new ApiException(ApiException.Type.BAD_FORMAT);
			}
			RequestType reqType = null;
			try {
				format = Format.valueOf(elements[3].toUpperCase());
				switch (format) {
				case JSON: 	// Browsers will prompt for you to save application/json format, which is a pain
							//contentType = "application/json"; 
							contentType = "text/plain";
							break;
				case XML:	contentType = "text/xml";
							break;
				case HTML:	contentType = "text/html";
							break;
				}
			} catch (IllegalArgumentException e) {
				throw new ApiException(ApiException.Type.BAD_FORMAT);
			}
			String component = elements[4];
			ApiImplementor impl = implementors.get(component);
			if (impl == null) {
				throw new ApiException(ApiException.Type.NO_IMPLEMENTOR);
			}
			try {
				reqType = RequestType.valueOf(elements[5]);
			} catch (IllegalArgumentException e) {
				throw new ApiException(ApiException.Type.BAD_TYPE);
			}
			String name = elements[6];
			
			// Check API is enabled
			if (!Model.getSingleton().getOptionsParam().getApiParam().isEnabled()) {
				throw new ApiException(ApiException.Type.DISABLED);
			}
			
			JSON result;
			JSONObject params = getParams(requestHeader.getURI().getEscapedQuery());
			switch (reqType) {
			case action:	
				// TODO Handle POST requests - need to read these in and then parse params from POST body
				/*
				if (Model.getSingleton().getOptionsParam().getApiParam().isPostActions()) {
					throw new ApiException(ApiException.Type.DISABLED);
				}
				*/
				result = impl.handleApiAction(name, params);
				switch (format) {
				case JSON: 	response = result.toString();
							break;
				case XML:	response = impl.actionResultToXML(name, result);
							break;
				case HTML:	response = impl.actionResultToHTML(name, result);
							break;
				}
				break;
			case view:		
				result = impl.handleApiView(name, params);	
				switch (format) {
				case JSON: 	response = result.toString();
							break;
				case XML:	response = impl.viewResultToXML(name, result);
							break;
				case HTML:	response = impl.viewResultToHTML(name, result);
							break;
				}
				break;
			}
			
		} catch (ApiException e) {
			response =  e.toString(format);
		}

    	msg.setResponseHeader("HTTP/1.1 200 OK\r\nContent-Length: " + response.length() + 
    			"\r\nContent-Type: " + contentType + ";");
    	msg.setResponseBody(response);

    	httpOut.write(msg.getResponseHeader());
    	httpOut.write(msg.getResponseBody());
		httpOut.flush();
		httpOut.close();
		httpIn.close();
		
		return true;
	}
	
	private JSONObject getParams (String params) throws ApiException {
		JSONObject jp = new JSONObject();
		if (params == null || params.length() == 0) {
			return jp;
		}
	    String[] keyValue = patternParam.split(params);
		String key = null;
		String value = null;
		int pos = 0;
		for (int i=0; i<keyValue.length; i++) {
			key = null;
			pos = keyValue[i].indexOf('=');
			if (pos > 0) {
				// param found
				key = keyValue[i].substring(0,pos);
				value = keyValue[i].substring(pos+1);
				jp.put(key, value);
			} else {
				throw new ApiException(ApiException.Type.BAD_FORMAT);
			}
		}
		return jp;
	}
}

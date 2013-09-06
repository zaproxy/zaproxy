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
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpInputStream;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpOutputStream;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.view.View;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class API {
	public enum Format {XML, HTML, JSON, JSONP, UI, OTHER};
	public enum RequestType {action, view, other};
	
	public static String API_URL = "http://zap/";

	private static Pattern patternParam = Pattern.compile("&", Pattern.CASE_INSENSITIVE);
	private static final String CALL_BACK_URL = "/zapCallBackUrl/";

	private Map<String, ApiImplementor> implementors = new HashMap<>();
	private static API api = null;
	private WebUI webUI = new WebUI(this);
	private Map<String, ApiImplementor> callBacks = new HashMap<>();

	private Map<String, ApiImplementor> shortcuts = new HashMap<>();

	private Random random = new Random();
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
		for (String shortcut : impl.getApiShortcuts()) {
			// TODO check for clashes
			logger.debug("Registering API shortcut: " + shortcut);
			this.shortcuts.put("/" + shortcut, impl);
		}
	}
	
	public void removeApiImplementor(ApiImplementor impl) {
		if (!implementors.containsKey(impl.getPrefix())) {
			logger.warn("Attempting to remove an API implementor not registered, with prefix: " + impl.getPrefix());
			return;
		}
		implementors.remove(impl.getPrefix());
	}
	
	public boolean isEnabled() {
		// Check API is enabled (its always enabled if run from the cmdline)
		if ( View.isInitialised() && ! Model.getSingleton().getOptionsParam().getApiParam().isEnabled()) {
			return false;
		}
		return true;
	}

	public boolean handleApiRequest (HttpRequestHeader requestHeader, HttpInputStream httpIn, 
			HttpOutputStream httpOut) throws IOException {
		return this.handleApiRequest(requestHeader, httpIn, httpOut, false);
	}

	public boolean handleApiRequest (HttpRequestHeader requestHeader, HttpInputStream httpIn, 
			HttpOutputStream httpOut, boolean force) throws IOException {
		
		String url = requestHeader.getURI().toString();
		Format format = Format.OTHER;
		ApiImplementor callbackImpl = null;
		ApiImplementor shortcutImpl = null;
		
		// Check for callbacks
		if (url.contains(CALL_BACK_URL)) {
			logger.debug("handleApiRequest Callback: " + url);
			for (Entry<String, ApiImplementor> callback : callBacks.entrySet()) {
				if (url.startsWith(callback.getKey())) {
					callbackImpl = callback.getValue();
					break;
				}
			}
		}
		String path = requestHeader.getURI().getPath();
		if (path != null) {
			for (Entry<String, ApiImplementor> shortcut : shortcuts.entrySet()) {
				if (path.startsWith(shortcut.getKey())) {
					shortcutImpl = shortcut.getValue();
					break;
				}
			}
		}
		
		if (shortcutImpl == null && callbackImpl == null && ! url.startsWith(API_URL) && ! force) {
			return false;
		}
		logger.debug("handleApiRequest " + url);

		HttpMessage msg = new HttpMessage();
		msg.setRequestHeader(requestHeader);
		String component = null;
		ApiImplementor impl = null;
		RequestType reqType = null;
		String contentType = "text/plain";
		String response = "";
		String name = null;
		
		try {
			if (shortcutImpl != null) {
				msg = shortcutImpl.handleShortcut(msg);
			} else if (callbackImpl != null) {
				response = callbackImpl.handleCallBack(msg);
			} else {
			
				// Parse the query:
				// format of url is http://zap/format/component/reqtype/name/?params
				//                    0  1  2    3        4        5      6
				String[] elements = url.split("/");
			
				if (elements.length > 3) {
					try {
						format = Format.valueOf(elements[3].toUpperCase());
						switch (format) {
						case JSON: 	// Browsers will prompt for you to save application/json format, which is a pain
									//contentType = "application/json"; 
									contentType = "text/plain";
									break;
						case JSONP: contentType = "application/javascript";
									break;
						case XML:	contentType = "text/xml";
									break;
						case HTML:	contentType = "text/html";
									break;
						case UI:	contentType = "text/html";
									break;
						default:
									break;
						}
					} catch (IllegalArgumentException e) {
						throw new ApiException(ApiException.Type.BAD_FORMAT);
					}
				}
				if (elements.length > 4) {
					component = elements[4];
					impl = implementors.get(component);
					if (impl == null) {
						throw new ApiException(ApiException.Type.NO_IMPLEMENTOR);
					}
				}
				if (elements.length > 5) {
					try {
						reqType = RequestType.valueOf(elements[5]);
					} catch (IllegalArgumentException e) {
						throw new ApiException(ApiException.Type.BAD_TYPE);
					}
				}
				if (elements.length > 6) {
					name = elements[6];
					if (name != null && name.indexOf("?") > 0) {
						name = name.substring(0, name.indexOf("?"));
					}
				}
				
				if (format.equals(Format.UI)) {
					if ( ! isEnabled()) {
						throw new ApiException(ApiException.Type.DISABLED);
					}

					response = webUI.handleRequest(component, impl, reqType, name);
					contentType = "text/html";
				} else if (name != null) {
					if ( ! isEnabled()) {
						throw new ApiException(ApiException.Type.DISABLED);
					}

					ApiResponse res;
					JSONObject params = getParams(requestHeader.getURI().getEscapedQuery());
					switch (reqType) {
					case action:	
						// TODO Handle POST requests - need to read these in and then parse params from POST body
						/*
						if (Model.getSingleton().getOptionsParam().getApiParam().isPostActions()) {
							throw new ApiException(ApiException.Type.DISABLED);
						}
						*/
						// Check for mandatory params
						ApiAction action = impl.getApiAction(name);
						if (action != null) {
							// Checking for null to handle option actions
							List<String> mandatoryParams = action.getMandatoryParamNames();
							if (mandatoryParams != null) {
								for (String param : mandatoryParams) {
									if (!params.has(param) || params.getString(param).length() == 0) {
										throw new ApiException(ApiException.Type.MISSING_PARAMETER, param);
									}
								}
							}
						}
						
						res = impl.handleApiOptionAction(name, params);	
						if (res == null) {
							res = impl.handleApiAction(name, params);
						}
						switch (format) {
						case JSON: 	response = res.toJSON().toString();
									break;
						case JSONP: response = this.getJsonpWrapper(res.toJSON().toString()); 
									break;
						case XML:	response = this.responseToXml(name, res);
									break;
						case HTML:	this.responseToHtml(name, res);
									break;
						default:
									break;
						}
							
						break;
					case view:		
						ApiView view = impl.getApiView(name);
						if (view != null) {
							// Checking for null to handle option actions
							List<String> mandatoryParams = view.getMandatoryParamNames();
							if (mandatoryParams != null) {
								for (String param : mandatoryParams) {
									if (params.getString(param) == null || params.getString(param).length() == 0) {
										throw new ApiException(ApiException.Type.MISSING_PARAMETER, param);
									}
								}
							}
						}
						res = impl.handleApiOptionView(name, params);	
						if (res == null) {
							res = impl.handleApiView(name, params);
						}
						switch (format) {
						case JSON: 	response = res.toJSON().toString();
									break;
						case JSONP: response = this.getJsonpWrapper(res.toJSON().toString()); 
									break;
						case XML:	response = this.responseToXml(name, res);
									break;
						case HTML:	response = this.responseToHtml(name, res);
									break;
						default:
									break;
						}

						break;
					case other:
						ApiOther other = impl.getApiOther(name);
						if (other != null) {
							// Checking for null to handle option actions
							List<String> mandatoryParams = other.getMandatoryParamNames();
							if (mandatoryParams != null) {
								for (String param : mandatoryParams) {
									if (params.getString(param) == null || params.getString(param).length() == 0) {
										throw new ApiException(ApiException.Type.MISSING_PARAMETER, param);
									}
								}
							}
						}
						msg = impl.handleApiOther(msg, name, params);
					}
				} else {
					// Handle default front page, even if the API is disabled
					response = webUI.handleRequest(requestHeader.getURI(), this.isEnabled());
					format = Format.UI;
					contentType = "text/html";
				}
			}
			logger.debug("handleApiRequest returning: " + response);
			
		} catch (ApiException e) {
			response =  e.toString(format);
 			logger.debug("handleApiRequest error: " + response, e);
		}
		
		if (format == null || ! format.equals(Format.OTHER) && shortcutImpl == null) {
	    	msg.setResponseHeader(
	    			"HTTP/1.1 200 OK\r\n" +
	    			"Pragma: no-cache\r\n" +
	  				"Cache-Control: no-cache\r\n" + 
	  				"Access-Control-Allow-Origin: *\r\n" + 
	  				"Access-Control-Allow-Methods: GET,POST,OPTIONS\r\n" + 
	  				"Access-Control-Allow-Headers: ZAP-Header\r\n" + 
	    			"Content-Length: " + response.length() + 
	    			"\r\nContent-Type: " + contentType + ";");
	    	msg.setResponseBody(response);
		}

    	httpOut.write(msg.getResponseHeader());
    	httpOut.write(msg.getResponseBody());
		httpOut.flush();
		httpOut.close();
		httpIn.close();
		
		return true;
	}
	
	private String responseToHtml(String name, ApiResponse res) {
		StringBuilder sb = new StringBuilder();
		sb.append("<head>\n");
		sb.append("</head>\n");
		sb.append("<body>\n");
		res.toHTML(sb);
		sb.append("</body>\n");
		return sb.toString();
	}

	private String responseToXml(String name, ApiResponse res) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement(name);
			doc.appendChild(rootElement);
			res.toXML(doc, rootElement);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			StringWriter sw = new StringWriter();
			StreamResult result =  new StreamResult(sw);
			transformer.transform(source, result);
			
			return sw.toString();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return "";
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
				try {
					key = URLDecoder.decode(keyValue[i].substring(0,pos), "UTF-8");
					value = URLDecoder.decode(keyValue[i].substring(pos+1), "UTF-8");
					jp.put(key, value);
				} catch (UnsupportedEncodingException | IllegalArgumentException e) {
					// Carry on anyway
					Exception apiException = new ApiException(ApiException.Type.BAD_FORMAT, params, e);
					logger.error(apiException.getMessage(), apiException);
				}
			} else {
				// Carry on anyway
				Exception e = new ApiException(ApiException.Type.BAD_FORMAT, params);
				logger.error(e.getMessage(), e);
			}
		}
		return jp;
	}
	
	private String getJsonpWrapper(String json) {
		return "zapJsonpResult (" + json + " )";
	}

	protected Map<String, ApiImplementor> getImplementors() {
		return implementors;
	}
	
	public String getCallBackUrl(ApiImplementor impl, String site) {
		String url = site + CALL_BACK_URL + random.nextLong();
		this.callBacks.put(url, impl);
		return url;
	}
	
}

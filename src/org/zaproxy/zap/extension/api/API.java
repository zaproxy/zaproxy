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
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Date;
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

import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.core.proxy.ProxyParam;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpInputStream;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpOutputStream;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.view.View;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sf.json.JSONObject;

public class API {
	public enum Format {XML, HTML, JSON, JSONP, UI, OTHER};
	public enum RequestType {action, view, other};
	
	/**
	 * The custom domain to access the ZAP API while proxying through ZAP.
	 * 
	 * @see #getBaseURL(boolean)
	 */
	public static final String API_DOMAIN = "zap";

	/**
	 * The HTTP URL to access the ZAP API while proxying through ZAP.
	 * 
	 * @see #getBaseURL(boolean)
	 */
	public static final String API_URL = "http://" + API_DOMAIN + "/";

	/**
	 * The HTTPS URL to access the ZAP API while proxying through ZAP.
	 * 
	 * @see #getBaseURL(boolean)
	 */
	public static final String API_URL_S = "https://" + API_DOMAIN + "/";
	public static final String API_KEY_PARAM = "apikey";
	public static final String API_NONCE_PARAM = "apinonce";

	private static Pattern patternParam = Pattern.compile("&", Pattern.CASE_INSENSITIVE);
	private static final String CALL_BACK_URL = "/zapCallBackUrl/";

	private static final String STATUS_OK = "200 OK";
	private static final String STATUS_BAD_REQUEST = "400 Bad Request";
	private static final String STATUS_INTERNAL_SERVER_ERROR = "500 Internal Server Error";

	private Map<String, ApiImplementor> implementors = new HashMap<>();
	private static API api = null;
	private WebUI webUI = new WebUI(this);
	private Map<String, ApiImplementor> callBacks = new HashMap<>();

	private Map<String, ApiImplementor> shortcuts = new HashMap<>();
	
	private Map<String, Nonce> nonces = Collections.synchronizedMap(new HashMap<String, Nonce>());
	
	/**
	 * The options for the API.
	 * 
	 * @see #getOptionsParamApi()
	 */
	private OptionsParamApi optionsParamApi;

	/**
	 * The options of the local proxy.
	 * 
	 * @see #getProxyParam()
	 */
	private ProxyParam proxyParam;

	private Random random = new SecureRandom();
    private static final Logger logger = Logger.getLogger(API.class);

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

	/**
	 * Registers the given {@code ApiImplementor} to the ZAP API.
	 * <p>
	 * The implementor is not registed if the {@link ApiImplementor#getPrefix() API implementor prefix} is already in use.
	 * <p>
	 * <strong>Note:</strong> The preferred method to add an {@code ApiImplementor} is through the method
	 * {@link org.parosproxy.paros.extension.ExtensionHook#addApiImplementor(ApiImplementor)
	 * ExtensionHook.addApiImplementor(ApiImplementor)} when the corresponding
	 * {@link org.parosproxy.paros.extension.Extension#hook(org.parosproxy.paros.extension.ExtensionHook) extension is hooked}.
	 * Only use this method if really necessary.
	 *
	 * @param impl the implementor that will be registered
	 * @see #removeApiImplementor(ApiImplementor)
	 */
	public void registerApiImplementor (ApiImplementor impl) {
		if (implementors.get(impl.getPrefix()) != null) {
			logger.error("Second attempt to register API implementor with prefix of " + impl.getPrefix());
			return;
		}
		implementors.put(impl.getPrefix(), impl);
		for (String shortcut : impl.getApiShortcuts()) {
			logger.debug("Registering API shortcut: " + shortcut);
			if (this.shortcuts.containsKey(shortcut)) {
				logger.error("Duplicate API shortcut: " + shortcut);
			}
			this.shortcuts.put("/" + shortcut, impl);
		}
	}
	
	/**
	 * Removes the given {@code ApiImplementor} from the ZAP API.
	 *
	 * @param impl the implementor that will be removed
	 * @since 2.1.0
	 * @see #registerApiImplementor(ApiImplementor)
	 */
	public void removeApiImplementor(ApiImplementor impl) {
		if (!implementors.containsKey(impl.getPrefix())) {
			logger.warn("Attempting to remove an API implementor not registered, with prefix: " + impl.getPrefix());
			return;
		}
		implementors.remove(impl.getPrefix());
		for (String shortcut : impl.getApiShortcuts()) {
			String key = "/" + shortcut;
			if (this.shortcuts.containsKey(key)) {
				logger.debug("Removing registered API shortcut: " + shortcut);
				this.shortcuts.remove(key);
			}
		}
	}
	
	public boolean isEnabled() {
		// Check API is enabled (its always enabled if run from the cmdline)
		if ( View.isInitialised() && ! getOptionsParamApi().isEnabled()) {
			return false;
		}
		return true;
	}
	
	private OptionsParamApi getOptionsParamApi() {
		if (optionsParamApi == null) {
			optionsParamApi = Model.getSingleton().getOptionsParam().getApiParam();
		}
		return optionsParamApi;
	}

	void setOptionsParamApi(OptionsParamApi optionsParamApi) {
		this.optionsParamApi = optionsParamApi;
	}

	private ProxyParam getProxyParam() {
		if (proxyParam == null) {
			proxyParam = Model.getSingleton().getOptionsParam().getProxyParam();
		}
		return proxyParam;
	}

	void setProxyParam(ProxyParam proxyParam) {
		this.proxyParam = proxyParam;
	}
	
	public boolean handleApiRequest (HttpRequestHeader requestHeader, HttpInputStream httpIn, 
			HttpOutputStream httpOut) throws IOException {
		return this.handleApiRequest(requestHeader, httpIn, httpOut, false);
	}

	private boolean isPermittedAddr(HttpRequestHeader requestHeader) {
		if (getOptionsParamApi().isPermittedAddress(requestHeader.getSenderAddress().getHostAddress())) {
			if (getOptionsParamApi().isPermittedAddress(requestHeader.getHostName())) {
				return true;
			}
			logger.warn("Request to API URL " + requestHeader.getURI().toString() + " with host header " +
					requestHeader.getHostName() + " not permitted");
			return false;
		}
		logger.warn("Request to API URL " + requestHeader.getURI().toString() + " from " +
				requestHeader.getSenderAddress().getHostAddress() + " not permitted");
		return false;
	}
	
	public boolean handleApiRequest (HttpRequestHeader requestHeader, HttpInputStream httpIn, 
			HttpOutputStream httpOut, boolean force) throws IOException {
		
		String url = requestHeader.getURI().toString();
		Format format = Format.OTHER;
		ApiImplementor callbackImpl = null;
		ApiImplementor shortcutImpl = null;
		
		// Check for callbacks
		if (url.contains(CALL_BACK_URL)) {
			if (! isPermittedAddr(requestHeader)) {
				return true;
			}
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
		
		if (shortcutImpl == null && callbackImpl == null && ! url.startsWith(API_URL) && ! url.startsWith(API_URL_S) && ! force) {
			return false;
		}
		if (! isPermittedAddr(requestHeader)) {
			return true;
		}
		if (getOptionsParamApi().isSecureOnly() && ! requestHeader.isSecure()) {
			// Insecure request with secure only set, always ignore
			logger.debug("handleApiRequest rejecting insecure request");
			return true;
		}
			
		logger.debug("handleApiRequest " + url);

		HttpMessage msg = new HttpMessage();
		msg.setRequestHeader(requestHeader);
		if (requestHeader.getContentLength() > 0) {
			msg.setRequestBody(httpIn.readRequestBody(requestHeader));
		}
		String component = null;
		ApiImplementor impl = null;
		RequestType reqType = null;
		String contentType = "text/plain; charset=UTF-8";
		String response = "";
		String name = null;
		boolean error = false;
		
		try {
			JSONObject params = getParams(requestHeader.getURI().getEscapedQuery());

			if (shortcutImpl != null) {
				if (!getOptionsParamApi().isDisableKey() && !getOptionsParamApi().isNoKeyForSafeOps()) {
					if ( ! this.hasValidKey(requestHeader, params)) {
						throw new ApiException(ApiException.Type.BAD_API_KEY);
					}
				}
				msg = shortcutImpl.handleShortcut(msg);
			} else if (callbackImpl != null) {
				// Callbacks have suitably random URLs and therefore don't require keys/nonces
				response = callbackImpl.handleCallBack(msg);
			} else {
			
				// Parse the query:
				// format of url is http://zap/format/component/reqtype/name/?params
				//                    0  1  2    3        4        5      6
				String[] elements = url.split("/");

				if (elements.length > 3 && elements[3].equalsIgnoreCase("favicon.ico")) {
					// Treat the favicon as a special case:)
					if (!getOptionsParamApi().isUiEnabled()) {
						throw new ApiException(ApiException.Type.DISABLED);
					}
					InputStream is = API.class.getResourceAsStream("/resource/zap.ico");
			    	byte[] icon = new byte[is.available()];
			    	is.read(icon);
			    	is.close();

			    	msg.setResponseHeader(getDefaultResponseHeader(contentType));
			    	msg.getResponseHeader().setContentLength(icon.length);
			    	httpOut.write(msg.getResponseHeader());
			    	httpOut.write(icon);
					httpOut.flush();
					httpOut.close();
					httpIn.close();
					return true;
					
				} else if (elements.length > 3) {
					try {
						format = Format.valueOf(elements[3].toUpperCase());
						switch (format) {
						case JSON: 	contentType = "application/json; charset=UTF-8";
									break;
						case JSONP: contentType = "application/javascript; charset=UTF-8";
									break;
						case XML:	contentType = "text/xml; charset=UTF-8";
									break;
						case HTML:	contentType = "text/html; charset=UTF-8";
									break;
						case UI:	contentType = "text/html; charset=UTF-8";
									break;
						default:
									break;
						}
					} catch (IllegalArgumentException e) {
						format = Format.HTML;
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
					if ( ! isEnabled() || !getOptionsParamApi().isUiEnabled()) {
						throw new ApiException(ApiException.Type.DISABLED);
					}

					response = webUI.handleRequest(component, impl, reqType, name);
					contentType = "text/html; charset=UTF-8";
				} else if (name != null) {
					if ( ! isEnabled()) {
						throw new ApiException(ApiException.Type.DISABLED);
					}
					// Do this now as it might contain the api key/nonce
					if (requestHeader.getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
						String contentTypeHeader = requestHeader.getHeader(HttpHeader.CONTENT_TYPE);
						if (contentTypeHeader != null
								&& contentTypeHeader.equals(HttpHeader.FORM_URLENCODED_CONTENT_TYPE)) {
							params = getParams(msg.getRequestBody().toString());
						} else {
							throw new ApiException(ApiException.Type.CONTENT_TYPE_NOT_SUPPORTED);
						}
					}
					
					if (format.equals(Format.JSONP)) {
						if (!getOptionsParamApi().isEnableJSONP()) {
							// Not enabled
							throw new ApiException(ApiException.Type.DISABLED);
						}
						if (! this.hasValidKey(requestHeader, params)) {
							// An api key is required for ALL JSONP requests
							throw new ApiException(ApiException.Type.BAD_API_KEY);
						}
					}

					ApiResponse res;
					switch (reqType) {
					case action:
						if (!getOptionsParamApi().isDisableKey()) {
							if ( ! this.hasValidKey(requestHeader, params)) {
								throw new ApiException(ApiException.Type.BAD_API_KEY);
							}
						}

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
						case HTML:	response = this.responseToHtml(name, res);
									break;
						default:
									break;
						}
							
						break;
					case view:		
						if (!getOptionsParamApi().isDisableKey() && !getOptionsParamApi().isNoKeyForSafeOps()) {
							if ( ! this.hasValidKey(requestHeader, params)) {
								throw new ApiException(ApiException.Type.BAD_API_KEY);
							}
						}
						ApiView view = impl.getApiView(name);
						if (view != null) {
							// Checking for null to handle option actions
							List<String> mandatoryParams = view.getMandatoryParamNames();
							if (mandatoryParams != null) {
								for (String param : mandatoryParams) {
									if (!params.has(param) || params.getString(param).length() == 0) {
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
							if (!getOptionsParamApi().isDisableKey() && 
									(!getOptionsParamApi().isNoKeyForSafeOps() ||
										other.isRequiresApiKey())) {
								// Check if a valid api key has been used
								if ( ! this.hasValidKey(requestHeader, params)) {
									throw new ApiException(ApiException.Type.BAD_API_KEY);
								}
							}
							List<String> mandatoryParams = other.getMandatoryParamNames();
							if (mandatoryParams != null) {
								for (String param : mandatoryParams) {
									if (!params.has(param) || params.getString(param).length() == 0) {
										throw new ApiException(ApiException.Type.MISSING_PARAMETER, param);
									}
								}
							}
						}
						msg = impl.handleApiOther(msg, name, params);
					}
				} else {
					// Handle default front page, unless if the API UI is disabled
					if ( ! isEnabled() || !getOptionsParamApi().isUiEnabled()) {
						throw new ApiException(ApiException.Type.DISABLED);
					}
					response = webUI.handleRequest(requestHeader.getURI(), this.isEnabled());
					format = Format.UI;
					contentType = "text/html; charset=UTF-8";
				}
			}
			logger.debug("handleApiRequest returning: " + response);
			
		} catch (Exception e) {
			if (! getOptionsParamApi().isReportPermErrors()) {
				if (e instanceof ApiException) {
					ApiException exception = (ApiException) e;
					if (exception.getType().equals(ApiException.Type.DISABLED) ||
							exception.getType().equals(ApiException.Type.BAD_API_KEY)) {
						// Fail silently
						return true;
					}
				}
			}
			handleException(msg, format, contentType, e);
			error = true;
		}
		
		if (!error && ! format.equals(Format.OTHER) && shortcutImpl == null) {
	    	msg.setResponseHeader(getDefaultResponseHeader(contentType));
	    	msg.setResponseBody(response);
	    	msg.getResponseHeader().setContentLength(msg.getResponseBody().length());
		}
		
		if (impl != null) {
			impl.addCustomHeaders(name, reqType, msg);
		}

    	httpOut.write(msg.getResponseHeader());
    	httpOut.write(msg.getResponseBody().getBytes());
		httpOut.flush();
		httpOut.close();
		httpIn.close();
		
		return true;
	}
	
	/**
	 * Returns a URI for the specified parameters.
	 * <p>
	 * An {@link #getOneTimeNonce(String) one time nonce query parameter} is added to the resulting URL, if required (that is,
	 * not a view). In this case the URL is ended with an ampersand (for example,
	 * {@code https://zap/format/prefix/action/name/?apinonce=xyz&}), otherwise it has a trailing slash (for example,
	 * {@code http://zap/format/prefix/view/name/}).
	 * 
	 * @param format the format of the API response
	 * @param prefix the prefix of the API implementor
	 * @param type the request type
	 * @param name the name of the endpoint
	 * @param proxy if true then the URI returned will only work if proxying via ZAP, ie it will start with http://zap/..
	 * @return the URL to access the defined endpoint
	 * @see #getBaseURL(boolean)
	 */
	public String getBaseURL(API.Format format, String prefix, API.RequestType type, String name, boolean proxy) {
		String apiPath = format.name() + "/" + prefix + "/" + type.name() + "/" + name + "/";
		if (!RequestType.view.equals(type)) {
			return getBaseURL(proxy) + apiPath + "?" + API_NONCE_PARAM + "=" + this.getOneTimeNonce("/" + apiPath) + "&";
		}
		return getBaseURL(proxy) + apiPath;
	}

	/**
	 * Gets the base URL to access the ZAP API, possibly proxying through ZAP.
	 * <p>
	 * If proxying through ZAP the base URL will use the custom domain, {@value #API_DOMAIN}.
	 * <p>
	 * The resulting base URL has a trailing slash, for example, {@code https://127.0.0.1/} or {@code https://zap/}.
	 * 
	 * @param proxy {@code true} if the URL will be accessed while proxying through ZAP, {@code false} otherwise.
	 * @return the base URL to access the ZAP API.
	 * @since TODO add version
	 */
	public String getBaseURL(boolean proxy) {
		if (proxy) {
			return getOptionsParamApi().isSecureOnly() ? API_URL_S : API_URL;
		}

		StringBuilder strBuilder = new StringBuilder(50);
		strBuilder.append("http");
		if (getOptionsParamApi().isSecureOnly()) {
			strBuilder.append('s');
		}
		strBuilder.append("://")
				.append(getProxyParam().getProxyIp())
				.append(':')
				.append(getProxyParam().getProxyPort())
				.append('/');
		return strBuilder.toString();
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

	public static JSONObject getParams (String params) throws ApiException {
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
					Exception apiException = new ApiException(ApiException.Type.ILLEGAL_PARAMETER, params, e);
					logger.error(apiException.getMessage(), apiException);
				}
			} else {
				// Carry on anyway
				Exception e = new ApiException(ApiException.Type.ILLEGAL_PARAMETER, params);
				logger.error(e.getMessage(), e);
			}
		}
		return jp;
	}
	
	private String getJsonpWrapper(String json) {
		return "zapJsonpResult (" + json + " )";
	}

	public Map<String, ApiImplementor> getImplementors() {
		return Collections.unmodifiableMap(implementors);
	}
	
	public String getCallBackUrl(ApiImplementor impl, String site) {
		String url = site + CALL_BACK_URL + random.nextLong();
		this.callBacks.put(url, impl);
		return url;
	}

	/**
	 * Returns a one time nonce to be used with the API call specified by the URL
	 * @param apiUrl the API URL
	 * @return a one time nonce
	 * @since 2.6.0
	 */
	public String getOneTimeNonce(String apiUrl) {
		String nonce = Long.toHexString(random.nextLong());
		this.nonces.put(nonce, new Nonce(nonce, apiUrl, true));
		return nonce;
	}

	/**
	 * Returns a nonce that will be valid for the lifetime of the ZAP process to used with the API call specified by the URL
	 * @param apiUrl the API URL
	 * @return a nonce that will be valid for the lifetime of the ZAP process
	 * @since 2.6.0
	 */
	public String getLongLivedNonce(String apiUrl) {
		String nonce = Long.toHexString(random.nextLong());
		this.nonces.put(nonce, new Nonce(nonce, apiUrl, false));
		return nonce;
	}

	/**
	 * Returns true if the API call has a valid key
	 * @param msg the message
	 * @return true if the API call has a valid key
	 * @since 2.6.0
	 */
	public boolean hasValidKey(HttpMessage msg) {
		try {
			return this.hasValidKey(msg.getRequestHeader(), getParams(msg.getRequestHeader().getURI().getEscapedQuery()));
		} catch (ApiException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Returns true if the API call has a valid key
	 * @param reqHeader the request header
	 * @param params the parameters
	 * @return true if the API call has a valid key
	 * @since 2.6.0
	 */
	public boolean hasValidKey(HttpRequestHeader reqHeader, JSONObject params) {
		try {
			String apiPath;
			try {
				apiPath = reqHeader.getURI().getPath();
			} catch (URIException e) {
				logger.error(e.getMessage(), e);
				return false;
			}
			String nonceParam = reqHeader.getHeader(HttpHeader.X_ZAP_API_NONCE);
			if (nonceParam == null && params.has(API_NONCE_PARAM)) {
				nonceParam = params.getString(API_NONCE_PARAM);
			}
			
			if (nonceParam != null) {
				Nonce nonce = nonces.get(nonceParam);
				if (nonce == null) {
					logger.warn("API nonce " + nonceParam + " not found in request from " + reqHeader.getSenderAddress().getHostAddress());
					return false;
				} else if (nonce.isOneTime()) {
					nonces.remove(nonceParam);
				}
				if (! nonce.isValid()) {
					logger.warn("API nonce " + nonce.getNonceKey() + " expired at " + nonce.getExpires().toString() +
							" in request from " + reqHeader.getSenderAddress().getHostAddress());
					return false;
				}
				
				if (! apiPath.equals(nonce.getApiPath())) {
					logger.warn("API nonce path was " + nonce.getApiPath() + " but call was for " + apiPath + 
						" in request from " + reqHeader.getSenderAddress().getHostAddress());
					return false;
				}
			} else {
				String keyParam = reqHeader.getHeader(HttpHeader.X_ZAP_API_KEY);
				if (keyParam == null && params.has(API_KEY_PARAM)) {
					keyParam = params.getString(API_KEY_PARAM);
				}
				if (!getOptionsParamApi().getKey().equals(keyParam)) {
					logger.warn("API key incorrect or not supplied: " + keyParam +
							" in request from " + reqHeader.getSenderAddress().getHostAddress());
					return false;
				}
			}
			
			return true;
		} finally {
			synchronized (nonces) {
				for (Entry<String, Nonce> entry : nonces.entrySet()) {
					if (! entry.getValue().isValid()) {
						nonces.remove(entry.getKey());
					}
				}
			}
		}
	}

    public static String getDefaultResponseHeader(String contentType) {
        return getDefaultResponseHeader(contentType, 0);
    }

    public static String getDefaultResponseHeader(String contentType, int contentLength) {
        return getDefaultResponseHeader(STATUS_OK, contentType, contentLength, false);
    }

    public static String getDefaultResponseHeader(String contentType, int contentLength, boolean canCache) {
        return getDefaultResponseHeader(STATUS_OK, contentType, contentLength, canCache);
    }

    public static String getDefaultResponseHeader(String responseStatus, String contentType, int contentLength) {
    	return getDefaultResponseHeader(responseStatus, contentType, contentLength, false);
    }

    public static String getDefaultResponseHeader(String responseStatus, String contentType, int contentLength, boolean canCache) {
        StringBuilder sb = new StringBuilder(250);

        sb.append("HTTP/1.1 ").append(responseStatus).append("\r\n");
        if (! canCache) {
        	sb.append("Pragma: no-cache\r\n");
        	sb.append("Cache-Control: no-cache\r\n");
        }
        sb.append("Content-Security-Policy: default-src 'none'; script-src 'self'; connect-src 'self'; child-src 'self'; img-src 'self' data:; font-src 'self' data:; style-src 'self'\r\n");
        sb.append("Referrer-Policy: no-referrer\r\n");
        sb.append("Access-Control-Allow-Methods: GET,POST,OPTIONS\r\n");
        sb.append("Access-Control-Allow-Headers: ZAP-Header\r\n");
        sb.append("X-Frame-Options: DENY\r\n");
        sb.append("X-XSS-Protection: 1; mode=block\r\n");
        sb.append("X-Content-Type-Options: nosniff\r\n");
        sb.append("X-Clacks-Overhead: GNU Terry Pratchett\r\n");
        sb.append("Content-Length: ").append(contentLength).append("\r\n");
        sb.append("Content-Type: ").append(contentType).append("\r\n");

        return sb.toString();
    }

    private void handleException(HttpMessage msg, Format format, String contentType, Exception cause) {
        String responseStatus = STATUS_INTERNAL_SERVER_ERROR;
        if (format == Format.OTHER) {
            boolean logError = true;
            if (cause instanceof ApiException) {
                switch (((ApiException) cause).getType()) {
                case DISABLED:
                    responseStatus = STATUS_BAD_REQUEST;
                    logger.warn("ApiException while handling API request:", cause);
                    logError = false;
                    break;
                case BAD_TYPE:
                case NO_IMPLEMENTOR:
                case BAD_API_KEY:
                case MISSING_PARAMETER:
                case BAD_ACTION:
                case BAD_VIEW:
                case BAD_OTHER:
                    responseStatus = STATUS_BAD_REQUEST;
                    logger.warn("API 'other' malformed request:", cause);
                    logError = false;
                    break;
                default:
                }
            }

            if (logError) {
                logger.error("API 'other' endpoint didn't handle exception:", cause);
            }
        } else {
            ApiException exception;
            if (cause instanceof ApiException) {
                exception = (ApiException) cause;
                if (!ApiException.Type.INTERNAL_ERROR.equals(exception.getType())) {
                    responseStatus = STATUS_BAD_REQUEST;
                    logger.warn("ApiException while handling API request:", cause);
                }
            } else {
                exception = new ApiException(ApiException.Type.INTERNAL_ERROR, cause);
                logger.error("Exception while handling API request:", cause);
            }
            String response = exception.toString(format, getOptionsParamApi().isIncErrorDetails());

            msg.getResponseBody().setCharset(getCharset(contentType));
            msg.getResponseBody().setBody(response);
        }

        try {
            msg.setResponseHeader(getDefaultResponseHeader(responseStatus, contentType, msg.getResponseBody().length()));
        } catch (HttpMalformedHeaderException e) {
            logger.warn("Failed to build API error response:", e);
        }
    }

    private static String getCharset(String contentType) {
        int idx = contentType.indexOf("charset=");
        if (idx == -1) {
            return "UTF-8";
        }
        return contentType.substring(idx + 8);
    }
    
    private class Nonce {
        private final String nonceKey;
        private final String apiPath;
        private final boolean oneTime;
        private final Date expires;
        
        public Nonce(String nonceKey, String apiStr, boolean oneTime) {
            this.nonceKey = nonceKey;
            this.apiPath = apiStr;
            this.oneTime = oneTime;
            this.expires = DateUtils.addSeconds(new Date(), getOptionsParamApi().getNonceTimeToLiveInSecs());
        }
        
        public String getNonceKey() {
            return nonceKey;
        }

        public String getApiPath() {
            return apiPath;
        }
        
        public boolean isOneTime() {
            return oneTime;
        }
        
        public boolean isValid() {
            return ! oneTime || expires.after(new Date());
        }

        public Date getExpires() {
            return expires;
        }

    }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.api.API.Format;
import org.zaproxy.zap.extension.api.API.RequestType;

public class WebUI {
	
	private API api;

	public WebUI(API api) {
		this.api = api;
	}
	
	private ApiElement getElement(ApiImplementor impl, String name, RequestType reqType) throws ApiException {
		if (RequestType.action.equals(reqType) && name != null) {
			// Action form
			List<ApiAction> actionList = impl.getApiActions();
			ApiAction action = null;
			for (ApiAction act : actionList) {
				if (name.equals(act.getName())) {
					action = act;
					break;
				}
			}
			if (action == null) {
				throw new ApiException(ApiException.Type.BAD_ACTION);
			}
			return action;
		} else if (RequestType.other.equals(reqType) && name != null) {
			// Other form
			List<ApiOther> otherList = impl.getApiOthers();
			ApiOther other = null;
			for (ApiOther oth : otherList) {
				if (name.equals(oth.getName())) {
					other = oth;
					break;
				}
			}
			if (other == null) {
				throw new ApiException(ApiException.Type.BAD_OTHER);
			}
			return other;
		} else if (RequestType.view.equals(reqType) && name != null) {
			List<ApiView> viewList = impl.getApiViews();
			ApiView view = null;
			for (ApiView v : viewList) {
				if (name.equals(v.getName())) {
					view = v;
					break;
				}
			}
			if (view == null) {
				throw new ApiException(ApiException.Type.BAD_VIEW);
			}
			return view;
		} else {
			throw new ApiException(ApiException.Type.BAD_TYPE);
		}
	}
	
	private void appendElements(StringBuilder sb, String component, String type, List<ApiElement> elementList) {
		Collections.sort(elementList, new Comparator<ApiElement>() {
			@Override
			public int compare(ApiElement ae1, ApiElement ae2) {
				return ae1.getName().compareTo(ae2.getName());
			}});
		
		sb.append("\n<table>\n");
		for (ApiElement element : elementList) {
			List<String> mandatoryParams = element.getMandatoryParamNames();
			List<String> optionalParams = element.getOptionalParamNames();
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<a href=\"/");
			sb.append(Format.UI.name());
			sb.append('/');
			sb.append(component);
			sb.append('/');
			sb.append(type);
			sb.append('/');
			sb.append(element.getName());
			sb.append("/\">");
			sb.append(element.getName());
			if (mandatoryParams != null || optionalParams != null) {
				sb.append(" (");
				if (mandatoryParams != null) {
					for (String param : mandatoryParams) {
						sb.append(param);
						sb.append("* ");
					}
				}
				if (optionalParams != null) {
					for (String param : optionalParams) {
						sb.append(param);
						sb.append(" ");
					}
				}
				sb.append(") ");
			}
			sb.append("</a>");
			sb.append("</td><td>");
			
			String descTag = element.getDescriptionTag();
			if (descTag == null) {
				// This is the default, but it can be overriden by the getDescriptionTag method if required
				descTag = component + ".api." + type + "." + element.getName();
			}
			try {
				sb.append(Constant.messages.getString(descTag));
			} catch (Exception e) {
				// Might not be set, so ignore failures
				// Uncomment to see what tags are missing via the UI
				// sb.append(descTag);
			}
			sb.append("</td>");
			
			sb.append("</tr>\n");
		}
		sb.append("</table>\n");

	}

	public String handleRequest(String component, ApiImplementor impl,
			RequestType reqType, String name) throws ApiException {
		// Generate HTML UI
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>\n");
		sb.append("<head>\n");
		sb.append("<title>");
		sb.append(Constant.messages.getString("api.html.title"));
		sb.append("</title>\n");
		/* The script version prevents the cache being used if ZAP has been updated in the same day */ 
		sb.append("<script src=\"/script.js?v=" + CoreAPI.API_SCRIPT_VERSION + "\" type=\"text/javascript\"></script>\n");
		sb.append("</head>\n");
		sb.append("<body>\n");
		sb.append("<h1>");
		sb.append("<a href=\"/");
		sb.append(Format.UI.name());
		sb.append("/\">");
		sb.append(Constant.messages.getString("api.html.title"));
		sb.append("</a>");
		sb.append("</h1>\n");
		
		if (impl != null) {
			sb.append("<h2>");
			sb.append("<a href=\"/");
			sb.append(Format.UI.name());
			sb.append("/");
			sb.append(component);
			sb.append("/\">");
			sb.append(Constant.messages.getString("api.html.component"));
			sb.append(component);
			sb.append("</a>");
			sb.append("</h2>\n");
			
			if (name != null) {
				ApiElement element = this.getElement(impl, name, reqType);
				
				List<String> mandatoryParams = element.getMandatoryParamNames();
				List<String> optionalParams = element.getOptionalParamNames();
				sb.append("<h3>");
				sb.append(Constant.messages.getString("api.html." + reqType.name()));
				sb.append(element.getName());
				sb.append("</h3>\n");
				// Handle the (optional) description
				String descTag = element.getDescriptionTag();
				if (descTag == null) {
					// This is the default, but it can be overriden by the getDescriptionTag method if required
					descTag = component + ".api." + reqType.name() + "." + name;
				}
				try {
					sb.append(Constant.messages.getString(descTag));
				} catch (Exception e) {
					// Might not be set, so ignore failures
				}
				
				sb.append("\n<form id=\"zapform\" name=\"zapform\" action=\"override\">");
				sb.append("<table>\n");
				if ( ! RequestType.other.equals(reqType)) {
					sb.append("<tr><td>");
					sb.append(Constant.messages.getString("api.html.format"));
					sb.append("</td><td>\n");
					sb.append("<select id=\"zapapiformat\" name=\"zapapiformat\">\n");
					sb.append("<option value=\"JSON\">JSON</option>\n");
					if (getOptionsParamApi().isEnableJSONP()) {
						sb.append("<option value=\"JSONP\">JSONP</option>\n");
					} else {
						sb.append("<option value=\"JSONP\" disabled>JSONP</option>\n");
					}
					
					sb.append("<option value=\"HTML\">HTML</option>\n");
					sb.append("<option value=\"XML\">XML</option>\n");
					sb.append("</select>\n");
					sb.append("</td></tr>\n");
				}
				
				if (RequestType.action.equals(reqType) || RequestType.other.equals(reqType)) {
					String key = getOptionsParamApi().getKey();
					if (key != null && key.length() > 0) {
						sb.append("<tr>");
						sb.append("<td>");
						sb.append(API.API_KEY_PARAM);
						sb.append("*</td>");
						sb.append("<td>");
						sb.append("<input id=\"");
						sb.append(API.API_KEY_PARAM);
						sb.append("\" name=\"");
						sb.append(API.API_KEY_PARAM);
						sb.append("\" value=\"");
						if (getOptionsParamApi().isAutofillKey()) {
							sb.append(key);
						}
						sb.append("\"/>");
						sb.append("</td>");
						sb.append("</tr>\n");
					}
				}
				
				if (mandatoryParams != null) {
					for (String param : mandatoryParams) {
						sb.append("<tr>");
						sb.append("<td>");
						sb.append(param);
						sb.append("*</td>");
						sb.append("<td>");
						sb.append("<input id=\"");
						sb.append(param);
						sb.append("\" name=\"");
						sb.append(param);
						sb.append("\"/>");
						sb.append("</td>");
						sb.append("</tr>\n");
					}
				}
				if (optionalParams != null) {
					for (String param : optionalParams) {
						sb.append("<tr>");
						sb.append("<td>");
						sb.append(param);
						sb.append("</td>");
						sb.append("<td>");
						sb.append("<input id=\"");
						sb.append(param);
						sb.append("\" name=\"");
						sb.append(param);
						sb.append("\"/>");
						sb.append("</td>");
						sb.append("</tr>\n");
					}
				}
				sb.append("<tr>");
				sb.append("<td>");
				sb.append("</td>");
				sb.append("<td>");
				sb.append("<input id=\"button\" value=\"");
				sb.append(element.getName());
				sb.append("\" type=\"button\" zap-component=\"" + component + 
						"\" zap-type=\"" + reqType + "\" zap-name=\"" + name + "\"/>\n");
				sb.append("</td>");
				sb.append("</tr>\n");
				sb.append("</table>\n");
				sb.append("</form>\n");

			} else {
				List<ApiElement> elementList = new ArrayList<>();
				List<ApiView> viewList = impl.getApiViews();
				if (viewList != null && viewList.size() > 0) {
					sb.append("<h3>");
					sb.append(Constant.messages.getString("api.html.views"));
					sb.append("</h3>\n");
					elementList.addAll(viewList);
					this.appendElements(sb, component, RequestType.view.name(), elementList);
				}

				List<ApiAction> actionList = impl.getApiActions();
				if (actionList != null && actionList.size() > 0) {
					sb.append("<h3>");
					sb.append(Constant.messages.getString("api.html.actions"));
					sb.append("</h3>\n");
					elementList = new ArrayList<>();
					elementList.addAll(actionList);
					this.appendElements(sb, component, RequestType.action.name(), elementList);
				}
				
				List<ApiOther> otherList = impl.getApiOthers();
				if (otherList != null && otherList.size() > 0) {
					sb.append("<h3>");
					sb.append(Constant.messages.getString("api.html.others"));
					sb.append("</h3>\n");
					elementList = new ArrayList<>();
					elementList.addAll(otherList);
					this.appendElements(sb, component, RequestType.other.name(), elementList);
				}
			}

		} else {
			sb.append("<h3>");
			sb.append(Constant.messages.getString("api.html.components"));
			sb.append("</h3>\n");
			ArrayList<String> components = new ArrayList<String>(api.getImplementors().keySet());
			Collections.sort(components);
			
			sb.append("<table>\n");
			for (String cmp : components) {
				sb.append("<tr>");
				sb.append("<td>");
				sb.append("<a href=\"/");
				sb.append(Format.UI.name());
				sb.append('/');
				sb.append(cmp);
				sb.append("/\">");
				sb.append(cmp);
				sb.append("</a>");
				sb.append("</td>");
				sb.append("</tr>\n");
			}
			sb.append("</table>\n");
		}
		sb.append("</body>\n");
		
		return sb.toString();
		
	}

	public String handleRequest(URI uri, boolean apiEnabled) {
		// Right now just generate a basic home page
		StringBuilder sb = new StringBuilder();
		sb.append("<head>\n");
		sb.append("<title>");
		sb.append(Constant.messages.getString("api.html.title"));
		sb.append("</title>\n");
		sb.append("</head>\n");
		sb.append("<body>\n");
		sb.append(Constant.messages.getString("api.home.topmsg"));
		sb.append(Constant.messages.getString("api.home.proxypac"));
		sb.append(Constant.messages.getString("api.home.links.header"));
		if (apiEnabled) {
			sb.append(Constant.messages.getString("api.home.links.api.enabled"));
		} else {
			sb.append(Constant.messages.getString("api.home.links.api.disabled"));
		}
		sb.append(Constant.messages.getString("api.home.links.online"));
		sb.append("</body>\n");
		
		return sb.toString();
	}
	
	private OptionsParamApi getOptionsParamApi() {
		return Model.getSingleton().getOptionsParam().getApiParam();
	}
}

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

import java.util.List;
import java.util.Set;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.api.API.Format;
import org.zaproxy.zap.extension.api.API.RequestType;

public class WebUI {
	
	private API api;

	public WebUI(API api) {
		this.api = api;
	}

	public String handleRequest(String component, ApiImplementor impl,
			RequestType reqType, String name) throws ApiException {
		// Generate HTML UI
		//contentType = "text/html";
		StringBuffer sb = new StringBuffer();
		sb.append("<head>\n");
		sb.append("<title>");
		sb.append(Constant.messages.getString("api.html.title"));
		sb.append("</title>\n");
		sb.append("</head>\n");
		sb.append("<body>\n");
		sb.append("<h1>");
		sb.append("<a href=\"http://zap/");
		sb.append(Format.UI.name());
		sb.append("/\">");
		sb.append(Constant.messages.getString("api.html.title"));
		sb.append("</a>");
		sb.append("</h1>\n");
		
		if (impl != null) {
			sb.append("<h2>");
			sb.append(Constant.messages.getString("api.html.component"));
			sb.append(component);
			sb.append("</h2>\n");
			// TODO handle
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
				List<String> params = action.getParamNames();
				sb.append("<h3>");
				sb.append(Constant.messages.getString("api.html.action"));
				sb.append(action.getName());
				sb.append("</h3>\n");
				sb.append("<form action=\"");
				
				sb.append("http://zap/");
				sb.append(Format.XML.name());
				sb.append("/");
				sb.append(component);
				sb.append("/");
				sb.append(RequestType.action.name());
				sb.append("/");
				sb.append(action.getName());
				sb.append("/\">\n");
				sb.append("<table>\n");
				
				if (params != null) {
					for (String param : params) {
						sb.append("<tr>");
						sb.append("<td>");
						sb.append(param);
						sb.append("</td>");
						sb.append("<td>");
						sb.append("<input id=\"");
						sb.append(param);
						sb.append("\" name=\"");
						sb.append(param);
						sb.append("\"></input>");
						sb.append("</td>");
						sb.append("</tr>\n");
					}
				}
				sb.append("<tr>");
				sb.append("<td>");
				sb.append("</td>");
				sb.append("<td>");
				sb.append("<input id=\"submit\" type=\"submit\" value=\"");
				sb.append(action.getName());
				sb.append("\"></input>");
				sb.append("</td>");
				sb.append("</tr>\n");
				sb.append("</table>\n");
				sb.append("</form>\n");

			} else {
				List<ApiView> viewList = impl.getApiViews();
				if (viewList != null) {
					sb.append("<h3>");
					sb.append(Constant.messages.getString("api.html.views"));
					sb.append("</h3>\n");
					sb.append("<table>\n");
					for (ApiView view : viewList) {
						List<String> params = view.getParamNames();
						sb.append("<tr>");
						sb.append("<td>");
						if (params != null) {
							sb.append(view.getName());
							sb.append("</td>");
							
							sb.append("<td>");
							sb.append("<a href=\"http://zap/");
							sb.append(Format.JSON.name());
							sb.append("/");
							sb.append(component);
							sb.append("/");
							sb.append(RequestType.view.name());
							sb.append("/");
							sb.append(view.getName());
							sb.append("/\">");
							sb.append(Format.JSON.name());
							sb.append("</a>");
							
							sb.append("<td>");
							sb.append("<a href=\"http://zap/");
							sb.append(Format.XML.name());
							sb.append("/");
							sb.append(component);
							sb.append("/");
							sb.append(RequestType.view.name());
							sb.append("/");
							sb.append(view.getName());
							sb.append("/\">");
							sb.append(Format.XML.name());
							sb.append("</a>");
							
							sb.append("<td>");
							sb.append("<a href=\"http://zap/");
							sb.append(Format.HTML.name());
							sb.append("/");
							sb.append(component);
							sb.append("/");
							sb.append(RequestType.view.name());
							sb.append("/");
							sb.append(view.getName());
							sb.append("/\">");
							sb.append(Format.HTML.name());
							sb.append("</a>");
							
						} else {
							sb.append("<a href=\"http://zap/");
							sb.append(Format.UI.name());
							sb.append("/");
							sb.append(component);
							sb.append("/");
							sb.append(RequestType.view.name());
							sb.append("/");
							sb.append(view.getName());
							sb.append("/\">");
							sb.append(view.getName());
							if (params != null) {
								sb.append("...");
							}
							sb.append("</a>");
						}
						sb.append("</td>");
						sb.append("</tr>\n");
					}
					sb.append("</table>\n");
				}
				List<ApiAction> actionList = impl.getApiActions();
				sb.append("<h3>");
				sb.append(Constant.messages.getString("api.html.actions"));
				sb.append("</h3>\n");
				sb.append("<table>\n");
				for (ApiAction action : actionList) {
					List<String> params = action.getParamNames();
					sb.append("<tr>");
					sb.append("<td>");
					sb.append("<a href=\"http://zap/");
					sb.append(Format.UI.name());
					sb.append("/");
					sb.append(component);
					sb.append("/");
					sb.append(RequestType.action.name());
					sb.append("/");
					sb.append(action.getName());
					sb.append("/\">");
					sb.append(action.getName());
					if (params != null) {
						sb.append(" (");
						for (String param : params) {
							sb.append(param);
							sb.append(" ");
						}
						sb.append(") ");
					}
					sb.append("</a>");
					sb.append("</td>");
					sb.append("</tr>\n");
				}
				sb.append("</table>\n");
			}

		} else {
			sb.append("<h3>");
			sb.append(Constant.messages.getString("api.html.components"));
			sb.append("</h3>\n");
			Set<String> components = api.getImplementors().keySet();
			sb.append("<table>\n");
			for (String cmp : components) {
				sb.append("<tr>");
				sb.append("<td>");
				sb.append("<a href=\"http://zap/");
				sb.append(Format.UI.name());
				sb.append("/");
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

}

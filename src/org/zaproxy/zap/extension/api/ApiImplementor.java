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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.lang.StringEscapeUtils;
import org.parosproxy.paros.network.HttpMessage;


public abstract class ApiImplementor {

	private List<ApiAction> apiActions = new ArrayList<>();
	private List<ApiView> apiViews = new ArrayList<>();
	private List<ApiOther> apiOthers = new ArrayList<>();
	
	public List<ApiView> getApiViews() {
		return this.apiViews;
	}
	
	public List<ApiAction> getApiActions() {
		return this.apiActions;
	}

	public List<ApiOther> getApiOthers() {
		return this.apiOthers;
	}

	public void addApiView (ApiView view) {
		this.apiViews.add(view);
	}
	
	public void addApiOthers (ApiOther other) {
		this.apiOthers.add(other);
	}
	
	public void addApiAction (ApiAction action) {
		this.apiActions.add(action);
	}
	
	public String viewResultToXML (String name, JSON result) {
		return new XMLSerializer().write(result);
	}
	
	public String viewResultToHTML (String name, JSON result) {
		//return viewResultToXML (name, result);
		return jsonToHTML (result);
	}
	
	public String actionResultToXML (String name, JSON result) {
		return new XMLSerializer().write(result);
	}
	
	public String actionResultToHTML (String name, JSON result) {
		//return actionResultToXML (name, result);
		return jsonToHTML (result);
	}
	
	private String jsonToHTML (JSON json) {
		StringBuilder sb = new StringBuilder();
		jsonToHTML(json, sb);
		return sb.toString();
		
	}

	private void jsonToHTML (JSON json, StringBuilder sb) {
		if (json == null || json.isEmpty()) {
			return;
		}

		if (json.isArray()) {
			Object[] oa = ((JSONArray)json).toArray();
			sb.append("<table border=\"1\">\n");
			for (Object o : oa) {
				sb.append("<tr><td>\n");
				if (o instanceof JSON) {
					jsonToHTML((JSON)o, sb);
				} else {
					sb.append(o);
				}
				sb.append("</td></tr>\n");
			}
			sb.append("</table>\n");
		} else if (json instanceof JSONObject){
			JSONObject jo = (JSONObject)json;
			Set<?> set = jo.entrySet();
			@SuppressWarnings("unchecked")
			Iterator<Map.Entry<?, ?>> itor = (Iterator<Entry<?, ?>>) set.iterator();
			sb.append("<table border=\"1\">\n");
			while (itor.hasNext()) {
				Map.Entry<?, ?> me = itor.next();
				sb.append("<tr><td>\n");
				sb.append(StringEscapeUtils.escapeHtml(me.getKey().toString()));
				sb.append("</td>\n");
				sb.append("<td>\n");
				if (me.getValue() instanceof JSON) {
					jsonToHTML((JSON)me.getValue(), sb);
				} else {
					sb.append(StringEscapeUtils.escapeHtml(me.getValue().toString()));
				}
				sb.append("</td></tr>\n");
			}
			sb.append("</table>\n");
			
		}
	}

	/**
	 * Override if implementing one or more views
	 * @param name
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public JSON handleApiView(String name, JSONObject params) throws ApiException {
		throw new ApiException(ApiException.Type.BAD_VIEW, name);
	}

	/**
	 * Override if implementing one or more actions
	 * @param name
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public JSON handleApiAction(String name, JSONObject params) throws ApiException {
		throw new ApiException(ApiException.Type.BAD_ACTION, name);
	}
	
	/**
	 * Override if implementing one or more 'other' operations - these are operations that _dont_ return structured data
	 * @param msg
	 * @param name
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public HttpMessage handleApiOther(HttpMessage msg, String name, JSONObject params) throws ApiException {
		throw new ApiException(ApiException.Type.BAD_OTHER, name);
	}
	
	/**
	 * Override if handling callbacks
	 * @param msg
	 * @return
	 * @throws ApiException
	 */
	public String handleCallBack(HttpMessage msg)  throws ApiException {
		throw new ApiException (ApiException.Type.URL_NOT_FOUND, msg.getRequestHeader().getURI().toString());
	}

	public abstract String getPrefix();
}

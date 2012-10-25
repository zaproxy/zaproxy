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

import java.lang.reflect.Method;
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
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.network.HttpMessage;


public abstract class ApiImplementor {
	
	private static final String GET_OPTION_PREFIX = "get_option_";
	private static final String SET_OPTION_PREFIX = "set_option_";

	private List<ApiAction> apiActions = new ArrayList<>();
	private List<ApiView> apiViews = new ArrayList<>();
	private List<ApiOther> apiOthers = new ArrayList<>();
	private AbstractParam param = null;
	
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
	
	public void addApiOptions(AbstractParam param) {
		// Add option parameter getters and setters via reflection
		this.param = param;
		Method[] methods = param.getClass().getDeclaredMethods();
		List<String> addedActions = new ArrayList<String>();
		// Check for string setters (which take precedence)
		for (Method method : methods) {
			if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
				this.addApiView(new ApiView(GET_OPTION_PREFIX + method.getName().substring(3)));
			}
			if (method.getName().startsWith("is") && method.getParameterTypes().length == 0) {
				this.addApiView(new ApiView(GET_OPTION_PREFIX + method.getName().substring(2)));
			}
			if (method.getName().startsWith("set") && method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(String.class)) {
				this.addApiAction(new ApiAction(SET_OPTION_PREFIX + method.getName().substring(3), new String[]{"String"}));
				addedActions.add(method.getName());
			}
		}
		// Now check for non string setters
		for (Method method : methods) {
			if (method.getName().startsWith("set") && method.getParameterTypes().length == 1 && ! addedActions.contains(method.getName())) {
				// Non String setter
				if (method.getParameterTypes()[0].equals(Integer.class) || method.getParameterTypes()[0].equals(int.class)) {
					this.addApiAction(new ApiAction(SET_OPTION_PREFIX + method.getName().substring(3), new String[]{"Integer"}));
					addedActions.add(method.getName());	// Just in case there are more overloads
				} else if (method.getParameterTypes()[0].equals(Boolean.class) || method.getParameterTypes()[0].equals(boolean.class)) {
					this.addApiAction(new ApiAction(SET_OPTION_PREFIX + method.getName().substring(3), new String[]{"Boolean"}));
					addedActions.add(method.getName());	// Just in case there are more overloads
				}
			}
		}
		
	}

	public JSON handleApiOptionView(String name, JSONObject params) throws ApiException {
		if (this.param == null) {
			return null;
		}
		if (name.startsWith(GET_OPTION_PREFIX)) {
			name = name.substring(GET_OPTION_PREFIX.length());
			Method[] methods = param.getClass().getDeclaredMethods();
			for (Method method : methods) {
				if ((method.getName().equals("get" + name) ||  method.getName().equals("is" + name)) && method.getParameterTypes().length == 0) {
					try {
						JSONArray result = new JSONArray();
						Object value = method.invoke(this.param);
						result.add(value);
						return result;
					} catch (Exception e) {
						throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
					}
				}
			}
		}
		return null;
	}


	public JSON handleApiOptionAction(String name, JSONObject params) throws ApiException {
		if (this.param == null) {
			return null;
		}
		if (name.startsWith(SET_OPTION_PREFIX)) {
			name = name.substring(SET_OPTION_PREFIX.length());
			Method[] methods = param.getClass().getDeclaredMethods();
			for (Method method : methods) {
				if (method.getName().equals("set" + name) && method.getParameterTypes().length == 1) {
					Object val = null;
					if (method.getParameterTypes()[0].equals(String.class)) {
						val = params.getString("String");
					} else if (method.getParameterTypes()[0].equals(Integer.class) || method.getParameterTypes()[0].equals(int.class)) {
						val = params.getInt("Integer");
					} else if (method.getParameterTypes()[0].equals(Boolean.class) || method.getParameterTypes()[0].equals(boolean.class)) {
						val = params.getBoolean("Boolean");
					}
					if (val == null) {
						// Value supplied doesnt match the type - try the next one
						continue;
					}

					try {
						JSONArray result = new JSONArray();
						method.invoke(this.param, val);
						result.add("OK");
						return result;
					} catch (Exception e) {
						throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
					}
				}
			}
		}
		return null;
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

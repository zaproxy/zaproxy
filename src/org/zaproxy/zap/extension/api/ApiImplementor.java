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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.API.RequestType;


public abstract class ApiImplementor {
	
	private static final String GET_OPTION_PREFIX = "option";
	private static final String SET_OPTION_PREFIX = "setOption";
	private static final String ADD_OPTION_PREFIX = "addOption";
	private static final String REMOVE_OPTION_PREFIX = "removeOption";

	private static final Comparator<Method> METHOD_NAME_COMPARATOR;

	static {
		METHOD_NAME_COMPARATOR = new Comparator<Method>() {

			@Override
			public int compare(Method method, Method otherMethod) {
				if (method == null) {
					if (otherMethod == null) {
						return 0;
					}
					return -1;
				} else if (otherMethod == null) {
					return 1;
				}

				return method.getName().compareTo(otherMethod.getName());
			}
		};
	}
	
	private List<ApiAction> apiActions = new ArrayList<>();
	private List<ApiView> apiViews = new ArrayList<>();
	private List<ApiOther> apiOthers = new ArrayList<>();
	private List<String> apiShortcuts = new ArrayList<>();
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
	
	public void addApiShortcut (String shortcut) {
		this.apiShortcuts.add(shortcut);
	}
	
	/**
	 * Adds the given options to the API implementor.
	 * 
	 * @param param the options for the API
	 * @see ZapApiIgnore
	 */
	public void addApiOptions(AbstractParam param) {
		// Add option parameter getters and setters via reflection
		this.param = param;
		Method[] methods = param.getClass().getDeclaredMethods();
		Arrays.sort(methods, METHOD_NAME_COMPARATOR);
		List<String> addedActions = new ArrayList<>();
		// Check for string setters (which take precedence)
		for (Method method : methods) {
			if (isIgnored(method)) {
				continue;
			}

			if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
				this.addApiView(new ApiView(GET_OPTION_PREFIX + method.getName().substring(3)));
			}
			if (method.getName().startsWith("is") && method.getParameterTypes().length == 0) {
				this.addApiView(new ApiView(GET_OPTION_PREFIX + method.getName().substring(2)));
			}
			if (method.getName().startsWith("set") && method.getParameterTypes().length == 1 && 
					method.getParameterTypes()[0].equals(String.class)) {
				this.addApiAction(new ApiAction(SET_OPTION_PREFIX + method.getName().substring(3), 
						new String[]{"String"}));
				addedActions.add(method.getName());
			}
			if (method.getName().startsWith("add") && method.getParameterTypes().length == 1 && 
					method.getParameterTypes()[0].equals(String.class)) {
				this.addApiAction(new ApiAction(ADD_OPTION_PREFIX + method.getName().substring(3), 
						new String[]{"String"}));
				addedActions.add(method.getName());
			}
			if (method.getName().startsWith("remove") && method.getParameterTypes().length == 1 && 
					method.getParameterTypes()[0].equals(String.class)) {
				this.addApiAction(new ApiAction(REMOVE_OPTION_PREFIX + method.getName().substring(6), 
						new String[]{"String"}));
				addedActions.add(method.getName());
			}
		}
		// Now check for non string setters
		for (Method method : methods) {
			if (isIgnored(method)) {
				continue;
			}

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

	/**
	 * Tells whether or not the given {@code method} should be ignored, thus not included in the ZAP API.
	 * <p>
	 * Checks if the given {@code method} has been annotated with {@code ZapApiIgnore} or if it's not public, if any of the
	 * conditions is {@code true} the {@code method} is ignored.
	 * 
	 * @param method the method that will be checked
	 * @return {@code true} if the method should be ignored, {@code false} otherwise.
	 * @see ZapApiIgnore
	 */
	private static boolean isIgnored(Method method) {
		return method.getAnnotation(ZapApiIgnore.class) != null || !Modifier.isPublic(method.getModifiers());
	}

	public ApiResponse handleApiOptionView(String name, JSONObject params) throws ApiException {
		if (this.param == null) {
			return null;
		}
		if (name.startsWith(GET_OPTION_PREFIX)) {
			name = name.substring(GET_OPTION_PREFIX.length());
			Method[] methods = param.getClass().getDeclaredMethods();
			for (Method method : methods) {
				if (isIgnored(method)) {
					continue;
				}

				if ((method.getName().equals("get" + name) ||  method.getName().equals("is" + name)) && 
						method.getParameterTypes().length == 0) {
					try {
						return new ApiResponseElement(name, method.invoke(this.param).toString());
					} catch (Exception e) {
						throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
					}
				}
			}
		}
		return null;
	}


	public ApiResponse handleApiOptionAction(String name, JSONObject params) throws ApiException {
		if (this.param == null) {
			return null;
		}
		boolean isApiOption = false;

		if (name.startsWith(SET_OPTION_PREFIX)) {
			name = "set" + name.substring(SET_OPTION_PREFIX.length());
			isApiOption = true;
		} else if (name.startsWith(ADD_OPTION_PREFIX)) {
			name = "add" + name.substring(ADD_OPTION_PREFIX.length());
			isApiOption = true;
		} else if (name.startsWith(REMOVE_OPTION_PREFIX)) {
			name = "remove" + name.substring(REMOVE_OPTION_PREFIX.length());
			isApiOption = true;
		}

		if (isApiOption) {
			try {
				Method[] methods = param.getClass().getDeclaredMethods();
				for (Method method : methods) {
					if (isIgnored(method)) {
						continue;
					}

					if (method.getName().equals(name) && method.getParameterTypes().length == 1) {
						Object val = null;
						if (method.getParameterTypes()[0].equals(String.class)) {
							val = params.getString("String");
						} else if (method.getParameterTypes()[0].equals(Integer.class) || method.getParameterTypes()[0].equals(int.class)) {
							try {
								val = params.getInt("Integer");
							} catch (JSONException e) {
								throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, "Integer");
							}
						} else if (method.getParameterTypes()[0].equals(Boolean.class) || method.getParameterTypes()[0].equals(boolean.class)) {
							try {
								val = params.getBoolean("Boolean");
							} catch (JSONException e) {
								throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, "Boolean");
							}
						}
						if (val == null) {
							// Value supplied doesnt match the type - try the next one
							continue;
						}
						method.invoke(this.param, val);
						return ApiResponseElement.OK;
					}
				}
			} catch (ApiException e) {
				throw e;
			} catch (Exception e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
			}
		}
		return null;
	}


	/**
	 * Override if implementing one or more views
	 * @param name the name of the requested view
	 * @param params the API request parameters
	 * @return the API response
	 * @throws ApiException if an error occurred while handling the API view endpoint
	 */
	public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
		throw new ApiException(ApiException.Type.BAD_VIEW, name);
	}

	/**
	 * Override if implementing one or more actions
	 * @param name the name of the requested action
	 * @param params the API request parameters
	 * @return the API response
	 * @throws ApiException if an error occurred while handling the API action endpoint
	 */
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		throw new ApiException(ApiException.Type.BAD_ACTION, name);
	}
	
	/**
	 * Override if implementing one or more 'other' operations - these are operations that _dont_ return structured data
	 * @param msg the HTTP message containing the API request
	 * @param name the name of the requested other endpoint
	 * @param params the API request parameters
	 * @return the HTTP message with the API response
	 * @throws ApiException if an error occurred while handling the API other endpoint
	 */
	public HttpMessage handleApiOther(HttpMessage msg, String name, JSONObject params) throws ApiException {
		throw new ApiException(ApiException.Type.BAD_OTHER, name);
	}
	
	/**
	 * Override if handling callbacks
	 * @param msg the HTTP message containing the API request and response
	 * @return the API response (set in the HTTP response body)
	 * @throws ApiException if an error occurred while handling the API callback
	 */
	public String handleCallBack(HttpMessage msg)  throws ApiException {
		throw new ApiException (ApiException.Type.URL_NOT_FOUND, msg.getRequestHeader().getURI().toString());
	}

	public HttpMessage handleShortcut(HttpMessage msg)  throws ApiException {
		throw new ApiException (ApiException.Type.URL_NOT_FOUND, msg.getRequestHeader().getURI().toString());
	}

	public abstract String getPrefix();

	public ApiAction getApiAction(String name) {
		for (ApiAction action :this.apiActions) {
			if (action.getName().equals(name)) {
				return action;
			}
		}
		return null;
	}

	public ApiView getApiView(String name) {
		for (ApiView view :this.apiViews) {
			if (view.getName().equals(name)) {
				return view;
			}
		}
		return null;
	}

	public ApiOther getApiOther(String name) {
		for (ApiOther other : this.apiOthers) {
			if (other.getName().equals(name)) {
				return other;
			}
		}
		return null;
	}
	
	protected List<String> getApiShortcuts() {
		return this.apiShortcuts;
	}

	protected int getParam(JSONObject params, String name, int defaultValue) {
		try {
			return params.getInt(name);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	protected String getParam(JSONObject params, String name, String defaultValue) {
		try {
			return params.getString(name);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	protected boolean getParam(JSONObject params, String name, boolean defaultValue) {
		try {
			return params.getBoolean(name);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Override to add custom headers for specific API operations
	 * @param name	the name of the operation
	 * @param type the type of the operation
	 * @param msg the HTTP response message to the API request 
	 */
	public void addCustomHeaders(String name, RequestType type, HttpMessage msg) {
		// Do nothing in the default implementation
	}
}

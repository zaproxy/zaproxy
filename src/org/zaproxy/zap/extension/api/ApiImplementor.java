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
import java.util.List;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;


public abstract class ApiImplementor {

	private List<ApiAction> apiActions = new ArrayList<ApiAction>();
	private List<ApiView> apiViews = new ArrayList<ApiView>();
	
	public List<ApiView> getApiViews() {
		return this.apiViews;
	}
	
	public List<ApiAction> getApiActions() {
		return this.apiActions;
	}

	public void addApiView (ApiView view) {
		this.apiViews.add(view);
	}
	
	public void addApiAction (ApiAction action) {
		this.apiActions.add(action);
	}
	
	public String viewResultToXML (String name, JSON result) {
		return new XMLSerializer().write(result);
	}
	
	public String viewResultToHTML (String name, JSON result) {
		return viewResultToXML (name, result);
	}
	
	public String actionResultToXML (String name, JSON result) {
		return new XMLSerializer().write(result);
	}
	
	public String actionResultToHTML (String name, JSON result) {
		return actionResultToXML (name, result);
	}
	
	public abstract String getPrefix();
	
	public abstract JSON handleApiView(String name, JSONObject params) throws ApiException ;
	
	public abstract JSON handleApiAction(String name, JSONObject params) throws ApiException;
	
}

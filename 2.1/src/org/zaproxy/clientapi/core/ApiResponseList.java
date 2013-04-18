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
package org.zaproxy.clientapi.core;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

public class ApiResponseList extends ApiResponse {
	
	private List<ApiResponse> list = null;

	public ApiResponseList(String name) {
		super(name);
		this.list = new ArrayList<ApiResponse>();
	}

	public ApiResponseList(Node node) 
			throws ClientApiException {
		this(node.getNodeName());
		Node child = node.getFirstChild();
		while (child != null) {
			this.addItem(ApiResponseFactory.getResponse(child));
			child = child.getNextSibling();
		}
	}

	public ApiResponseList(Node node, ApiResponseList template) 
			throws ClientApiException {
		super(node.getNodeName());
		try {
			this.list = new ArrayList<ApiResponse>();
			Class<? extends ApiResponse> clazz = template.getItemsClass();
			if (clazz != null) {

				Node child = node.getFirstChild();
				while (child != null) {
					Constructor<? extends ApiResponse> cons = clazz.getConstructor(Node.class, ApiResponse.class);
					this.addItem(cons.newInstance(child, template.list.get(0)));
					child = child.getNextSibling();
				}
			}
		} catch (Exception e) {
			throw new ClientApiException(e);
		}
	}

	public ApiResponseList(String name, ApiResponse[] array) {
		super(name);
		this.list = new ArrayList<ApiResponse>();
		for (ApiResponse resp: array) {
			list.add(resp);
		}
	}

	public ApiResponseList(String name, List<ApiResponse> list) {
		super(name);
		this.list = list;
	}
	
	public void addItem(ApiResponse item) {
		this.list.add(item);
	}
	
	public List<ApiResponse> getItems() {
		return this.list;
	}
	
	public Class<? extends ApiResponse> getItemsClass() {
		if (this.list == null || this.list.size() == 0) {
			return null;
		}
		return this.list.get(0).getClass();
	}

	@Override
	public String toString(int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i=0 ; i < indent; i++) {
			sb.append("\t");
		}
		sb.append("ApiResponseList ");
		sb.append(this.getName());
		sb.append(" : [\n");
		for (ApiResponse resp: this.list) {
			sb.append(resp.toString(indent+1));
		}
		for (int i=0 ; i < indent; i++) {
			sb.append("\t");
		}
		sb.append("]\n");
		return sb.toString();
	}

	
}

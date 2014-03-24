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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Node;

public class ApiResponseSet extends ApiResponse {
	
	private String[] attributes = null;
	private Map<String, String> values = null;

	public ApiResponseSet(String name, String[] attributes) {
		super(name);
		this.attributes = attributes;
	}

	public ApiResponseSet(String name, Map<String, String> values) {
		super(name);
		this.values = values;
	}

	public ApiResponseSet(Node node) throws ClientApiException {
		super(node.getNodeName());
		Node child = node.getFirstChild();
		this.values = new HashMap<String, String>();
		while (child != null) {
			ApiResponseElement elem = (ApiResponseElement) ApiResponseFactory.getResponse(child);
			values.put(elem.getName(), elem.getValue());
			child = child.getNextSibling();
		}
	}

	public String[] getAttributes() {
		return attributes;
	}
	
	public String getAttribute(String name) {
		return this.values.get(name);
	}

	@Override
	public String toString(int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i=0 ; i < indent; i++) {
			sb.append("\t");
		}
		sb.append("ApiResponseSet ");
		sb.append(this.getName());
		sb.append(" : [\n");
		for (Entry<String, String> val  : values.entrySet()) {
			for (int i=0 ; i < indent+1; i++) {
				sb.append("\t");
			}
			sb.append(val.getKey());
			sb.append(" = ");
			sb.append(val.getValue());
			sb.append("\n");
		}
		for (int i=0 ; i < indent; i++) {
			sb.append("\t");
		}
		sb.append("]\n");
		return sb.toString();
	}

	
}

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

import org.w3c.dom.Node;

public class ApiResponseElement extends ApiResponse {
	
	public static ApiResponseElement OK = new ApiResponseElement("Result", "OK"); 
	public static ApiResponseElement FAIL = new ApiResponseElement("Result", "FAIL"); 
	
	private String value = null;

	public ApiResponseElement(String name) {
		super(name);
	}

	public ApiResponseElement(String name, String value) {
		super(name);
		this.value = value;
	}

	public ApiResponseElement(Node node, ApiResponse template) {
		super(node.getNodeName());
		this.value = node.getTextContent();

	}

	public ApiResponseElement(Node node) {
		super(node.getNodeName());
		this.value = node.getTextContent();
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString(int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i=0 ; i < indent; i++) {
			sb.append("\t");
		}
		sb.append("ApiResponseElement ");
		sb.append(this.getName());
		sb.append(" = " );
		sb.append(this.getValue());
		sb.append("\n");
		return sb.toString();
	}

}

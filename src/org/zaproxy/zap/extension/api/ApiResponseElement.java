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

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.zaproxy.zap.utils.XMLStringUtil;

public class ApiResponseElement extends ApiResponse {
	
	public static final ApiResponseElement OK = new ApiResponseElement("Result", "OK"); 
	public static final ApiResponseElement FAIL = new ApiResponseElement("Result", "FAIL"); 
	
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
	public JSON toJSON() {
		if (value == null) {
			return null;
		}
		JSONObject jo = new JSONObject();
		jo.put(this.getName(), this.value);
		return jo;
	}

	@Override
	public void toXML(Document doc, Element parent) {
		parent.appendChild(doc.createTextNode(XMLStringUtil.escapeControlChrs(this.getValue())));
	}

	@Override
	public void toHTML(StringBuilder sb) {
		sb.append("<table border=\"1\">\n");
		sb.append("<tr><td>\n");
		sb.append(this.getName());
		sb.append("</td><td>\n");
		sb.append(StringEscapeUtils.escapeHtml(this.getValue()));
		sb.append("</td></tr>\n");
		sb.append("</table>\n");
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

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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ApiResponseList extends ApiResponse {
	
	private List<ApiResponse> list = null;

	public ApiResponseList(String name) {
		super(name);
		this.list = new ArrayList<ApiResponse>();
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

	@Override
	public JSON toJSON() {
		if (list == null) {
			return null;
		}
		JSONObject jo = new JSONObject();
		JSONArray array = new JSONArray();
		for (ApiResponse resp: this.list) {
			if (resp instanceof ApiResponseElement) {
				array.add(((ApiResponseElement)resp).getValue());
			} else {
				array.add(resp.toJSON());
			}
		}
		jo.put(getName(), array);
		return jo;
	}

	@Override
	public void toXML(Document doc, Element parent) {
		for (ApiResponse resp: this.list) {
			Element el = doc.createElement(resp.getName());
			resp.toXML(doc, el);
			parent.appendChild(el);
		}
	}

	@Override
	public void toHTML(StringBuilder sb) {
		sb.append("<h2>" + this.getName() + "</h2>\n");
		sb.append("<table border=\"1\">\n");
		for (ApiResponse resp: this.list) {
			sb.append("<tr><td>\n");
			resp.toHTML(sb);
			sb.append("</td></tr>\n");
		}
		sb.append("</table>\n");
	}

	
}

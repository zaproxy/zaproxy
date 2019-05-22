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

package org.zaproxy.zap.network;

import java.util.TreeSet;

import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpBody;

public class HttpRequestBody extends HttpBody {

	public HttpRequestBody() {
		super();
	}

	public HttpRequestBody(int capacity) {
		super(capacity);
	}

	public HttpRequestBody(String data) {
		super(data);
	}

	// Construct a HTTP POST Body from the variables in postParams
	public void setFormParams(TreeSet<HtmlParameter> postParams) {
		if (postParams.isEmpty()) {
			this.setBody("");
			return;
		}
		
		StringBuilder postData = new StringBuilder();
		
		for(HtmlParameter parameter: postParams) {
			if (parameter.getType() != HtmlParameter.Type.form) {
				continue;
			}
			
			postData.append(parameter.getName());
			postData.append('=');
			postData.append(parameter.getValue());
			postData.append('&');
		}
		
		String data = "";
		if (postData.length() != 0) {
			data = postData.substring(0, postData.length() - 1);
		}
		
		this.setBody(data);
	}
}

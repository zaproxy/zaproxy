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
package org.zaproxy.zap.model;

import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMessage;

public interface ParameterParser {
	
	void init (String config);

	Map<String, String> getParams(HttpMessage msg, HtmlParameter.Type type);

	Map<String, String> parse(String paramStr);
	
	List<String> getTreePath(URI uri) throws URIException;
	
	String getDefaultKeyValuePairSeparator();

	String getDefaultKeyValueSeparator();

	String getConfig();

	ParameterParser clone();
}

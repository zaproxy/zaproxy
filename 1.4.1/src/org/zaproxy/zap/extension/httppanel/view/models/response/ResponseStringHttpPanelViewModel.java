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
package org.zaproxy.zap.extension.httppanel.view.models.response;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.extension.httppanel.view.models.AbstractStringHttpPanelViewModel;

public class ResponseStringHttpPanelViewModel extends AbstractStringHttpPanelViewModel {
	
	private static Logger log = Logger.getLogger(ResponseStringHttpPanelViewModel.class);
	
	@Override
	public String getData() {
		if (httpMessage == null || httpMessage.getResponseHeader().isEmpty()) {
			return "";
		}
		
		return httpMessage.getResponseHeader().toString().replaceAll(HttpHeader.CRLF, HttpHeader.LF) + httpMessage.getResponseBody().toString();
	}

	@Override
	public void setData(String data) {
		String[] parts = data.split(HttpHeader.LF + HttpHeader.LF);
		String header = parts[0].replaceAll("(?<!\r)\n", HttpHeader.CRLF);
		//Note that if the body has LF, those characters will not be replaced by CRLF.
		
		try {
			httpMessage.setResponseHeader(header);
		} catch (HttpMalformedHeaderException e) {
			log.warn("Could not Save Header: " + header, e);
		}
		
		if (parts.length > 1) {
			httpMessage.setResponseBody(data.substring(parts[0].length()+2));
		} else {
			httpMessage.setResponseBody("");
		}
	}
}

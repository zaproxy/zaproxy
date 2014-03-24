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
package org.zaproxy.zap.extension.httppanel.view.impl.models.http.response;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.AbstractHttpByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.HttpPanelViewModelUtils;

public class ResponseByteHttpPanelViewModel extends AbstractHttpByteHttpPanelViewModel {
	
	private static final Logger logger = Logger.getLogger(ResponseByteHttpPanelViewModel.class);
	
	@Override
	public byte[] getData() {
		if (httpMessage == null || httpMessage.getResponseHeader().isEmpty()) {
			return new byte[0];
		}
		
		byte[] headerBytes = httpMessage.getResponseHeader().toString().getBytes();
		byte[] bodyBytes = httpMessage.getResponseBody().getBytes();

		byte[] bytes = new byte[headerBytes.length + bodyBytes.length];
		
		System.arraycopy(headerBytes, 0, bytes, 0, headerBytes.length);
		System.arraycopy(bodyBytes, 0, bytes, headerBytes.length, bodyBytes.length);
		
		return bytes;
	}

	@Override
	public void setData(byte[] data) {
		int pos = findHeaderLimit(data);
		
		if (pos == -1) {
			logger.warn("Could not Save Header, limit not found. Header: " + new String(data));
			return;
		}
		
		try {
			httpMessage.setResponseHeader(new String(data, 0, pos));
		} catch (HttpMalformedHeaderException e) {
			logger.warn("Could not Save Header: " + Arrays.toString(data), e);
		}
		
		httpMessage.getResponseBody().setBody(ArrayUtils.subarray(data, pos, data.length));
		HttpPanelViewModelUtils.updateResponseContentLength(httpMessage);
	}
	
	private int findHeaderLimit(byte[] data) {
		boolean lastIsCRLF = false;
		boolean lastIsCR = false;
		boolean lastIsLF = false;
		int pos = -1;
		
		for(int i = 0; i < data.length; ++i) {
			if (!lastIsCR && data[i] == '\r') {
				lastIsCR = true;
			} else if (!lastIsLF && data[i] == '\n') {
				if (lastIsCRLF) {
					pos = i;
					break;
				}
				
				lastIsCRLF = true;
				lastIsCR = false;
				lastIsLF = false;
			} else {
				lastIsCR = false;
				lastIsLF = false;
				lastIsCRLF = false;
			}
		}
		
		return pos;
	}
}

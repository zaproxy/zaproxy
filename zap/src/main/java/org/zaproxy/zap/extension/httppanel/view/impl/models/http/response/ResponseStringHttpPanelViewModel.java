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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.AbstractHttpStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.HttpPanelViewModelUtils;

public class ResponseStringHttpPanelViewModel extends AbstractHttpStringHttpPanelViewModel {
	
	private static final Logger logger = Logger.getLogger(ResponseStringHttpPanelViewModel.class);
	
	@Override
	public String getData() {
		if (httpMessage == null || httpMessage.getResponseHeader().isEmpty()) {
			return "";
		}

		return httpMessage.getResponseHeader().toString().replaceAll(HttpHeader.CRLF, HttpHeader.LF) + getBody();
	}
	
	private String getBody() {
		if (HttpHeader.GZIP.equals(httpMessage.getResponseHeader().getHeader(HttpHeader.CONTENT_ENCODING))) {
			// Uncompress gziped content
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(httpMessage.getResponseBody().getBytes());
				GZIPInputStream gis = new GZIPInputStream(bais);
				InputStreamReader isr = new InputStreamReader(gis);
				BufferedReader br = new BufferedReader(isr);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				isr.close();
				gis.close();
				bais.close();
				return sb.toString();
			} catch (IOException e) {
				//this.log.error(e.getMessage(), e);
				System.out.println(e);
			}
		}
		return httpMessage.getResponseBody().toString();
	}

	@Override
	public void setData(String data) {
		String[] parts = data.split(HttpHeader.LF + HttpHeader.LF);
		String header = parts[0].replaceAll("(?<!\r)\n", HttpHeader.CRLF);
		//Note that if the body has LF, those characters will not be replaced by CRLF.
		
		try {
			httpMessage.setResponseHeader(header);
		} catch (HttpMalformedHeaderException e) {
			logger.warn("Could not Save Header: " + header, e);
		}
		
		if (parts.length > 1) {
			String body = data.substring(parts[0].length()+2);
			if (HttpHeader.GZIP.equals(httpMessage.getResponseHeader().getHeader(HttpHeader.CONTENT_ENCODING))) {
				// Recompress gziped content
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					GZIPOutputStream gis = new GZIPOutputStream(baos);
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(gis, "UTF-8"));
					bw.write(body);
					bw.close();
					gis.close();
					baos.close();
					httpMessage.getResponseBody().setBody(baos.toByteArray());
					HttpPanelViewModelUtils.updateResponseContentLength(httpMessage);
				} catch (IOException e) {
					//this.log.error(e.getMessage(), e);
					System.out.println(e);
				}
			} else {
				httpMessage.setResponseBody(body);
			}
		} else {
			httpMessage.setResponseBody("");
		}
		HttpPanelViewModelUtils.updateResponseContentLength(httpMessage);
	}
}

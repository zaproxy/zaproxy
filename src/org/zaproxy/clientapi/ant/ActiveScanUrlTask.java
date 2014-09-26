/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The Zed Attack Proxy Team
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
package org.zaproxy.clientapi.ant;

import org.apache.tools.ant.BuildException;
import org.zaproxy.clientapi.core.ApiResponseElement;

public class ActiveScanUrlTask extends ZapTask {
	
	private String url;
	private String apikey;

	@Override
	public void execute() throws BuildException {
		try {
			int status;
			this.getClientApi().ascan.scan(apikey, url, "false", "false");
			
			while (true) {
				Thread.sleep(1000);
				status = getStatusValue((ApiResponseElement) this.getClientApi().ascan.status()); 
				if (status >= 100) {
					break;
				}
			}

		} catch (Exception e) {
			throw new BuildException(e);
		}
	}

	private int getStatusValue(ApiResponseElement element) {
		return Integer.parseInt(element.getValue());
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}
}

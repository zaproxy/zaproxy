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
 * 
 * Note that this extension and the other classes in this package are heavily 
 * based on the original Paros ExtensionSpider! 
 */
package org.zaproxy.zap.extension.spider;

/**
 * A resource (e.g. webpage) found while spidering.
 * <p>
 * Contains the HTTP method used to fetch the resource, status code and reason, URI and the ID of the corresponding
 * (persisted) HTTP message.
 */

public class SpiderResource {
	
	private final int historyId;
	private final String method;
	private final String uri;
	private final int statusCode;
	private final String statusReason;

	public SpiderResource(int historyId, String method, String uri, int statusCode, String statusReason) {
		this.historyId = historyId;
		this.method = method;
		this.uri = uri;
		this.statusCode = statusCode;
		this.statusReason = statusReason;
	}

	public int getHistoryId() {
		return historyId;
	}

	public String getMethod() {
		return method;
	}

	public String getUri() {
		return uri;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusReason() {
		return statusReason;
	}
}

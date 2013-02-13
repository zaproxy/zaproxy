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
package org.zaproxy.zap.spider.filters;

import org.parosproxy.paros.network.HttpMessage;

/**
 * The DefaultParseFilter is an implementation of a {@link ParseFilter} that is default for
 * spidering process. Its filter rules are the following:<br/>
 * <ul>
 * <li>the resource body should be under MAX_RESPONSE_BODY_SIZE bytes, otherwise it's probably a
 * binary resource.</li>
 * <li>the resource must be of parsable type (text, html, xml, javascript). Actually, the content
 * type should be text/...</li>
 * </ul>
 */
public class DefaultParseFilter extends ParseFilter {

	/**
	 * The Constant MAX_RESPONSE_BODY_SIZE defining the size of response body that is considered too
	 * big for a parsable file.
	 */
	public static final int MAX_RESPONSE_BODY_SIZE = 512000;

	@Override
	public boolean isFiltered(HttpMessage responseMessage) {

		// Check response body size
		if (responseMessage.getResponseBody().length() > MAX_RESPONSE_BODY_SIZE) {
			if (log.isDebugEnabled()) {
				log.debug("Resource too large: " + responseMessage.getRequestHeader().getURI());
			}
			return true;
		}

		// Check response type
		if (!responseMessage.getResponseHeader().isText()) {
			if (log.isDebugEnabled()) {
				log.debug("Resource is not text: " + responseMessage.getRequestHeader().getURI());
			}
			return true;
		}

		return false;
	}

}

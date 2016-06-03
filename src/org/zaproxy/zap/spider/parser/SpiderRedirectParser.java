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
package org.zaproxy.zap.spider.parser;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;

/**
 * The Class SpiderRedirectParser is used for parsing of HTTP Redirection messages.
 */
public class SpiderRedirectParser extends SpiderParser {

	@Override
	public boolean parseResource(HttpMessage message, Source source, int depth) {
		log.debug("Parsing an HTTP redirection resource...");

		if (message == null || message.getResponseHeader() == null) {
			return false;
		}

		String location = message.getResponseHeader().getHeader(HttpHeader.LOCATION);
		if (location != null && !location.isEmpty()) {
			// Include the base url as well as some applications send relative URLs instead of
			// absolute ones
			String baseURL = message.getRequestHeader().getURI().toString();
			processURL(message, depth, location, baseURL);
		}
		// We consider the message fully parsed
		return true;
	}

	@Override
	public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyParsed) {
		return HttpStatusCode.isRedirection(message.getResponseHeader().getStatusCode());
	}
}

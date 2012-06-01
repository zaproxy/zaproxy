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

import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.spider.URLCanonicalizer;

/**
 * The Class SpiderHtmlParser is used for parsing of HTML files, gathering resource urls from them.
 */
public class SpiderHtmlParser extends SpiderParser {

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.zaproxy.zap.spider.parser.SpiderParser#parseResource(org.parosproxy.paros.network.HttpMessage
	 * , net.htmlparser.jericho.Source) */
	@Override
	public void parseResource(HttpMessage message, Source source) {

		// Get the context (base url)
		String baseURL;
		if (message == null)
			baseURL = "";
		else
			baseURL = message.getRequestHeader().getURI().toString();
		log.info("Base URL: " + baseURL);

		// Process A elements
		List<Element> elements = source.getAllElements(HTMLElementName.A);
		for (Element el : elements) {
			processAttributeElement(message, baseURL, el, "href");
		}
	}

	/**
	 * Processes the attribute with the given name of a Jericho element, for an URL. If an URL is
	 * found, notifies the listeners.
	 * 
	 * @param message the message
	 * @param baseURL the base url
	 * @param element the element
	 * @param attributeName the attribute name
	 */
	private void processAttributeElement(HttpMessage message, String baseURL, Element element, String attributeName) {
		// The URL as written in the attribute (can be relative or absolute)
		String localURL = element.getAttributeValue("href");
		if (localURL == null)
			return;

		// Build the absolute canonical URL
		String fullURL = URLCanonicalizer.getCanonicalURL(localURL, baseURL);
		if (fullURL == null)
			return;

		log.debug("Canonical URL constructed using '" + localURL + "': " + fullURL);
		notifyListenersResourceFound(message, fullURL);
	}

}

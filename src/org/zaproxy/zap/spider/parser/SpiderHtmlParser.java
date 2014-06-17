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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.spider.SpiderParam;

/**
 * The Class SpiderHtmlParser is used for parsing of HTML files, gathering resource urls from them.<br/>
 * 
 * <p>
 * NOTE: Handling of HTML Forms is not done in this Parser. Instead see {@link SpiderHtmlFormParser}
 * </p>
 * .
 */
public class SpiderHtmlParser extends SpiderParser {

	/** The Constant urlPattern defining the pattern for a meta url. */
	private static final Pattern urlPattern = Pattern.compile("url\\s*=\\s*([^;]+)", Pattern.CASE_INSENSITIVE);

	/** The params. */
	private SpiderParam params;

	/**
	 * Instantiates a new spider html parser.
	 * 
	 * @param params the params
	 */
	public SpiderHtmlParser(SpiderParam params) {
		super();
		this.params = params;
	}

	@Override
	public boolean parseResource(HttpMessage message, Source source, int depth) {

		// Prepare the source, if not provided
		if (source == null) {
			source = new Source(message.getResponseBody().toString());
		}

		// Get the context (base url)
		String baseURL;
		if (message == null) {
			baseURL = "";
		} else {
			baseURL = message.getRequestHeader().getURI().toString();
		}

		// Try to see if there's any BASE tag that could change the base URL
		Element base = source.getFirstElement(HTMLElementName.BASE);
		if (base != null) {
			if (log.isDebugEnabled()) {
				log.debug("Base tag was found in HTML: " + base.getDebugInfo());
			}
			if (base.getAttributeValue("href") != null) {
				baseURL = base.getAttributeValue("href");
			}
		}

		// Parse the source
		parseSource(message, source, depth, baseURL);

		// Parse the comments
		if (params.isParseComments()) {
			List<StartTag> comments = source.getAllStartTags(StartTagType.COMMENT);
			for (StartTag comment : comments) {
				Source s = new Source(comment.getTagContent());
				parseSource(message, s, depth, baseURL);
			}
		}
		
		return false;
	}

	/**
	 * Parses the HTML Jericho source for the elements that contain references to other resources.
	 * 
	 * @param message the message
	 * @param source the source
	 * @param depth the depth
	 * @param baseURL the base url
	 */
	private void parseSource(HttpMessage message, Source source, int depth, String baseURL) {
		log.debug("Parsing an HTML message...");
		// Process A elements
		List<Element> elements = source.getAllElements(HTMLElementName.A);
		for (Element el : elements) {
			processAttributeElement(message, depth, baseURL, el, "href");
		}

		// Process AREA elements
		elements = source.getAllElements(HTMLElementName.AREA);
		for (Element el : elements) {
			processAttributeElement(message, depth, baseURL, el, "href");
		}

		// Process Frame Elements
		elements = source.getAllElements(HTMLElementName.FRAME);
		for (Element el : elements) {
			processAttributeElement(message, depth, baseURL, el, "src");
		}

		// Process IFrame Elements
		elements = source.getAllElements(HTMLElementName.IFRAME);
		for (Element el : elements) {
			processAttributeElement(message, depth, baseURL, el, "src");
		}

		// Process Link elements
		elements = source.getAllElements(HTMLElementName.LINK);
		for (Element el : elements) {
			processAttributeElement(message, depth, baseURL, el, "href");
		}

		// Process Script elements with src
		elements = source.getAllElements(HTMLElementName.SCRIPT);
		for (Element el : elements) {
			processAttributeElement(message, depth, baseURL, el, "src");
		}

		// Process Img elements
		elements = source.getAllElements(HTMLElementName.IMG);
		for (Element el : elements) {
			processAttributeElement(message, depth, baseURL, el, "src");
		}

		// Process META elements
		elements = source.getAllElements(HTMLElementName.META);
		for (Element el : elements) {
			// If we have http-equiv attribute, then urls can be found.
			String equiv = el.getAttributeValue("http-equiv");
			String content = el.getAttributeValue("content");
			if (equiv != null && content != null) {

				// For the following cases:
				// http-equiv="refresh" content="0;URL=http://foo.bar/..."
				// http-equiv="location" content="url=http://foo.bar/..."
				if (equiv.equalsIgnoreCase("refresh") || equiv.equalsIgnoreCase("location")) {
					Matcher matcher = urlPattern.matcher(content);
					if (matcher.find()) {
						String url = matcher.group(1);
						processURL(message, depth, baseURL, url);
					}
				}
			}
		}
	}

	/**
	 * Processes the attribute with the given name of a Jericho element, for an URL. If an URL is
	 * found, notifies the listeners.
	 * 
	 * @param message the message
	 * @param depth the depth
	 * @param baseURL the base url
	 * @param element the element
	 * @param attributeName the attribute name
	 */
	private void processAttributeElement(HttpMessage message, int depth, String baseURL, Element element,
			String attributeName) {
		// The URL as written in the attribute (can be relative or absolute)
		String localURL = element.getAttributeValue(attributeName);
		if (localURL == null) {
			return;
		}

		processURL(message, depth, localURL, baseURL);
	}

	@Override
	public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyConsumed) {
		// Fallback parser - if it's a HTML message which has not already been processed
		return !wasAlreadyConsumed && message.getResponseHeader() != null
				&& message.getResponseHeader().isHtml();
	}

}

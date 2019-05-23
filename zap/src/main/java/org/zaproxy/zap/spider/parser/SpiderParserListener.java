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

import org.parosproxy.paros.network.HttpMessage;

/**
 * The listener interface for receiving spiderParser events. The class that is interested in
 * processing a spiderParser event implements this interface, and the object created with that class
 * is registered with a component using the component's
 * {@code addSpiderParserListener} method. When the spiderParser event occurs, that object's appropriate
 * method is invoked.
 * 
 */
public interface SpiderParserListener {

	/**
	 * Event triggered when a new resource URI is found. The responseMessage contains all the
	 * required information regarding the page which contains the URI.
	 * 
	 * @param responseMessage the response message
	 * @param depth the depth of this resource in the crawling process
	 * @param uri the universal resource locator
	 */
	void resourceURIFound(HttpMessage responseMessage, int depth, String uri);

	/**
	 * Event triggered when a new resource URI is found. The responseMessage contains all the
	 * required information regarding the page which contains the URI.
	 * <p>
	 * Also provides a {@code shouldIgnore} boolean that states that this resourceURI should be
	 * ignored in the fetching process, as it's probably a dead end (e.g. binary data, image, etc).
	 * 
	 * @param responseMessage the response message
	 * @param depth the depth of this resource in the crawling process
	 * @param uri the universal resource locator
	 * @param shouldIgnore whether this resource has a high chance of being not useful if fetched
	 */
	void resourceURIFound(HttpMessage responseMessage, int depth, String uri, boolean shouldIgnore);

	/**
	 * Event triggered when a new resource URI is found. However, if the URI needs to be fetched, it
	 * should be accessed with the HTTP POST method and the content of the request body message
	 * should be the one specified in {@code requestBody}.
	 * <p>
	 * For example, this method can be triggered if a parser finds the {@code uri} inside a form
	 * with the method set as {@code POST}. In this case, the messageContent should contain the form
	 * data set necessary for a successful submission of the form.
	 * </p>
	 * <p>
	 * The responseMessage contains all the required information regarding the page which contains
	 * the URI.
	 * </p>
	 * 
	 * @param responseMessage the response message
	 * @param depth the depth of this resource in the crawling process
	 * @param uri the universal resource locator
	 * @param requestBody represents the content that a request message should have in its body, if
	 *            fetching the resource
	 */
	void resourcePostURIFound(HttpMessage responseMessage, int depth, String uri, String requestBody);
}

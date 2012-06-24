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
 * <code>addSpiderParserListener<code> method. When the spiderParser event occurs, that object's appropriate
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
	public void resourceURIFound(HttpMessage responseMessage, int depth, String uri);

	/**
	 * Event triggered when a new resource URI is found. The responseMessage contains all the
	 * required information regarding the page which contains the URI.<br/>
	 * <br/>
	 * Also provides a {@code shouldIgnore} boolean that states that this resourceURI should be
	 * ignored in the fetching process, as it's probably a dead end (e.g. binary data, image, etc).
	 * 
	 * @param responseMessage the response message
	 * @param depth the depth of this resource in the crawling process
	 * @param uri the universal resource locator
	 * @param shouldIgnore whether this resource has a high chance of being not useful if fetched
	 */
	public void resourceURIFound(HttpMessage responseMessage, int depth, String uri, boolean shouldIgnore);
}

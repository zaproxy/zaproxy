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

package org.zaproxy.zap.spider;

import org.parosproxy.paros.network.HttpMessage;

/**
 * The listener interface for receiving spider related events. The class that is interested in
 * processing a spider event implements this interface, and the object created with that class is
 * registered with a component using the component's
 * <code>addSpiderListener<code> method. When  the spider event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see SpiderEvent
 */
public interface SpiderListener {

	/**
	 * Event triggered when the Spider progress has changed.
	 * 
	 * @param percentageComplete the percentage complete
	 * @param numberCrawled the number of pages crawled
	 * @param numberToCrawl the number of pages left to crawl
	 */
	public void spiderProgress(int percentageComplete, int numberCrawled, int numberToCrawl);

	/**
	 * Event triggered when a new uri was found. The <code>isSkipped</code> parameter says if the
	 * URI was skipped according to any skip rule or it was processed.
	 * 
	 * @param msg the message
	 * @param isSkipped if the uri was skipped
	 */
	public void foundURI(HttpMessage msg, boolean isSkipped);

	/**
	 * Event triggered when a new uri was read.
	 * 
	 * @param msg the message
	 */
	public void readURI(HttpMessage msg);

	/**
	 * Event triggered when the spider is complete.
	 */
	public void spiderComplete();

}

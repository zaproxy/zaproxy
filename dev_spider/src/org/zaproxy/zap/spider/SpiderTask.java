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

import java.io.IOException;

import net.htmlparser.jericho.Source;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.spider.filters.ParseFilter;
import org.zaproxy.zap.spider.parser.SpiderParser;

/**
 * The SpiderTask representing a spidering task performed during the Spidering process.
 */
public class SpiderTask implements Runnable {

	/** The parent spider. */
	private Spider parent;

	/** The uri that this task processes. */
	private URI uri;

	/** The depth of crawling where the uri was found. */
	private int depth;

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(SpiderTask.class);

	/**
	 * Instantiates a new spider task.
	 * 
	 * @param parent the parent
	 * @param uri the uri this SpiderTask should process
	 * @param depth the depth where this uri is at in the Spidering Process
	 */
	public SpiderTask(Spider parent, URI uri, int depth) {
		super();
		this.parent = parent;
		this.uri = uri;
		this.depth = depth;
	}

	/* (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run() */
	@Override
	public void run() {

		log.info("Spider Task Started. Processing uri (depth " + depth + "): " + uri);

		// Check if the should stop
		if (parent.isStopped()) {
			log.debug("Spider process is stopped. Skipping crawling task...");
			return;
		}
		if (uri == null) {
			log.warn("Null URI. Skipping crawling task...");
			return;
		}

		// Check if the crawling process is paused and do any "before execution" processing
		parent.preTaskExecution();

		// Fetch the resource
		HttpMessage msg = null;
		try {
			msg = fetchResource();
		} catch (Exception e) {
			log.error("An error occured while fetching resoure " + uri + ": " + e.getMessage(), e);
			return;
		}

		// Notify the SpiderListeners that a resource was read
		parent.notifyListenersReadURI(msg);

		// Check the parse filters to see if the resource should be skipped from parsing
		boolean isFiltered = false;
		for (ParseFilter filter : parent.getController().getParseFilters())
			if (filter.isFiltered(msg)) {
				log.info("Resource fetched, but will not be parsed due to a ParseFilter rule: " + uri);
				isFiltered = true;
				break;
			}

		// Process resource, if this is not the maximum depth
		if (!isFiltered && depth < parent.getSpiderParam().getMaxDepth())
			processResource(msg);

		// Update the progress and check if the spidering process should stop
		parent.postTaskExecution();
		log.info("Spider Task finished.");
	}

	/**
	 * Process a resource, searching for links (uris) to other resources.
	 * 
	 * @param msg the HTTP Message
	 */
	private void processResource(HttpMessage msg) {
		SpiderParser parser = parent.getController().getParser();
		Source source = new Source(msg.getResponseBody().toString());


		parser.parseResource(msg, source, depth);
	}

	/**
	 * Fetches a resource.
	 * 
	 * @return the http message
	 * @throws HttpException the http exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private HttpMessage fetchResource() throws HttpException, IOException {

		// Build the request message
		HttpMessage msg = new HttpMessage(new HttpRequestHeader(HttpRequestHeader.GET, uri, HttpHeader.HTTP11));
		msg.getRequestHeader().setHeader(HttpHeader.IF_MODIFIED_SINCE, null);
		msg.getRequestHeader().setHeader(HttpHeader.IF_NONE_MATCH, null);
		// Check if there is a custom user agent
		if (parent.getSpiderParam().getUserAgent() != null)
			msg.getRequestHeader().setHeader(HttpHeader.USER_AGENT, parent.getSpiderParam().getUserAgent());

		// Fetch the page
		parent.getHttpSender().sendAndReceive(msg);

		return msg;

	}

	/* (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString() */
	@Override
	public String toString() {
		return "SpiderTask [uri=" + uri + "]";
	}

}

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
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.spider.parser.SpiderParser;

/**
 * The SpiderTask representing a spidering task performed during the Spidering process.
 */
public class SpiderTask implements Runnable {

	/** The parent spider. */
	private Spider parent;

	/** The controller. */
	private SpiderController controller;

	/** The uri that this task processes. */
	private URI uri;

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(SpiderTask.class);

	/**
	 * Instantiates a new spider task.
	 * 
	 * @param parent the parent
	 * @param controller the controller
	 * @param uri the uri
	 */
	public SpiderTask(Spider parent, SpiderController controller, URI uri) {
		super();
		this.parent = parent;
		this.controller = controller;
		this.uri = uri;
	}

	/* (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run() */
	@Override
	public void run() {

		log.info("Spider Task Started. Processing uri: " + uri);

		// Check if the should stop
		if (parent.isStopped()) {
			log.debug("Spider process is stopped. Skipping task...");
			return;
		}
		if (uri == null) {
			log.warn("Null URI. Skipping crawling...");
			return;
		}

		// Check if the crawling process is paused and do any "before execution" processing
		parent.beforeTaskExecution();

		// Fetch the resource
		HttpMessage msg = null;
		try {
			msg = fetchResource();
		} catch (Exception e) {
			log.error("An error occured while fetching resoure " + uri + ": " + e.getMessage(), e);
			return;
		}

		// Process resource
		processResource(msg);

		log.info("Spider Task finished.");
	}

	/**
	 * Process a resource, searching for links (uris) to other resources.
	 * 
	 * @param msg the HTTP Message
	 */
	private void processResource(HttpMessage msg) {
		//log.debug("Processing message with content: " + msg.getResponseBody().toString());
		SpiderParser parser = controller.getParser();
		Source source = new Source(msg.getResponseBody().toString());
		parser.parseResource(msg, source);
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
		//log.debug("Fetching resource: " + uri + " using HttpMessage: " + msg.getRequestHeader().toString());
		parent.getHttpSender().sendAndReceive(msg);
		//log.debug("Resource received from server - response header: " + msg.getResponseHeader().toString());

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

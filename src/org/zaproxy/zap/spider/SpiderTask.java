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
import java.sql.SQLException;
import java.util.TreeSet;

import net.htmlparser.jericho.Source;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
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

	/**
	 * The uri that this task processes. Can be null, in which case the {@code reference} field
	 * should be used.
	 */
	private URI uri;

	/**
	 * The history reference to the database record where the request message has been partially
	 * filled in. Can be null, in which case the {@code uri} should be used.
	 */
	private HistoryReference reference;

	/** The depth of crawling where the uri was found. */
	private int depth;

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(SpiderTask.class);

	/**
	 * Instantiates a new spider task using only the target URI. The purpose of this task is to
	 * crawl the given uri, find any other uris in the fetched resource and create other tasks.
	 * 
	 * @param parent the spider
	 * @param uri the uri this SpiderTask should process
	 * @param depth the depth where this uri is at in the Spidering Process
	 */
	public SpiderTask(Spider parent, URI uri, int depth) {
		super();
		this.parent = parent;
		this.uri = uri;
		this.depth = depth;
		this.reference = null;
	}

	/**
	 * Instantiates a new spider task using both the target URI and the response message where the
	 * uri was found. The purpose of this task is to crawl the given uri, find any other uris in the
	 * fetched resource and create other tasks. <br/>
	 * <p>
	 * Other information from the response message (e.g. cookies) can be used to partially build the
	 * request message for this task and store it in the database using a {@link HistoryReference}.
	 * If no additional information is used from the response message, then no
	 * {@link HistoryReference} is used and only the task's uri is stored.
	 * </p>
	 * 
	 * @param parent the parent
	 * @param message the message
	 * @param uri the uri
	 * @param depth the depth
	 */
	public SpiderTask(Spider parent, HttpMessage message, URI uri, int depth) {
		super();
		this.parent = parent;
		this.depth = depth;

		// Check if cookies should be added
		TreeSet<HtmlParameter> cookies = buildCookiesList(message);
		if (cookies != null) {
			log.info("New task submitted for uri: " + uri + " with cookies: " + cookies);
			// Create a new HttpMessage that will be used for the request, add the cookies and
			// persist it in the databse using HistoryReference
			try {
				HttpMessage msg = new HttpMessage(new HttpRequestHeader(HttpRequestHeader.GET, uri, HttpHeader.HTTP11));
				msg.setCookieParams(cookies);
				this.reference = new HistoryReference(parent.getModel().getSession(), HistoryReference.TYPE_SPIDER, msg);
				return;
			} catch (HttpMalformedHeaderException e) {
				log.error("Error while building HttpMessage for uri: " + uri, e);
			} catch (SQLException e) {
				log.error("Error while persisting HttpMessage for uri: " + uri, e);
			}
		} else {
			log.info("New task submitted for uri: " + uri + " without cookies.");
			this.uri = uri;
			this.reference = null;
		}
	}

	/**
	 * Builds the list of the cookies that should be sent in the request message for the processed.
	 * 
	 * @param msg the response message where the uri was found
	 * @return the list of cookies to send with the request, or null if no cookies should be sent
	 *         {@code URI}.
	 */
	private TreeSet<HtmlParameter> buildCookiesList(HttpMessage msg) {

		if (parent.getSpiderParam().isSendCookies()) {
			TreeSet<HtmlParameter> cookies = msg.getResponseHeader().getCookieParams();

			if (!cookies.isEmpty())
				return cookies;
		}
		return null;
	}

	/* (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run() */
	@Override
	public void run() {

		// Log the task start
		// TODO: Take care because the second log is using database so it should be changed
		// eventually
		if (log.isInfoEnabled()) {
			if (uri != null)
				log.info("Spider Task Started. Processing uri (depth " + depth + "): " + uri);
			else
				try {
					log.info("Spider Task Started. Processing uri at depth " + depth
							+ " using already constructed message:  "
							+ reference.getHttpMessage().getRequestHeader().getURI());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		}

		// Check if the should stop
		if (parent.isStopped()) {
			log.debug("Spider process is stopped. Skipping crawling task...");
			return;
		}
		if (uri == null && reference == null) {
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
	 * @throws SQLException
	 */
	private HttpMessage fetchResource() throws HttpException, IOException, SQLException {

		// Build the request message or fetch it from the database, if it was already built
		HttpMessage msg;
		if (reference == null)
			msg = new HttpMessage(new HttpRequestHeader(HttpRequestHeader.GET, uri, HttpHeader.HTTP11));
		else
			msg = reference.getHttpMessage();
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

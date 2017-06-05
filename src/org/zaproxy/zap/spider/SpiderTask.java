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
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.net.ssl.SSLException;

import net.htmlparser.jericho.Source;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.spider.filters.ParseFilter;
import org.zaproxy.zap.spider.filters.ParseFilter.FilterResult;
import org.zaproxy.zap.spider.parser.SpiderParser;

/**
 * The SpiderTask representing a spidering task performed during the Spidering process.
 */
public class SpiderTask implements Runnable {

	/** The parent spider. */
	private Spider parent;

	/**
	 * The history reference to the database record where the request message has been partially filled in.
	 * <p>
	 * Might be {@code null} if failed to create or persist the message, if the task was already executed or if a clean up was
	 * performed.
	 * 
	 * @see #cleanup()
	 * @see #deleteHistoryReference()
	 * @see #prepareHttpMessage()
	 */
	private HistoryReference reference;

	/** The depth of crawling where the uri was found. */
	private int depth;
	
	private ExtensionHistory extHistory = null;

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(SpiderTask.class);

	/**
	 * Instantiates a new spider task using the target URI. The purpose of this task is to crawl the given
	 * uri, using the provided method, find any other uris in the fetched resource and create other tasks.
	 * 
	 * 
	 * @param parent the spider controlling the crawling process
	 * @param uri the uri that this task should process
	 * @param depth the depth where this uri is located in the spidering process
	 * @param method the HTTP method that should be used to fetch the resource
	 * 
	 */
	public SpiderTask(Spider parent, URI uri, int depth, String method) {
		this(parent, null, uri, depth, method, null);
	}

	/**
	 * Instantiates a new spider task using the target URI. The purpose of this task is to crawl the given
	 * uri, using the provided method, find any other uris in the fetched resource and create other tasks.
	 * 
	 * @param parent the spider controlling the crawling process
	 * @param sourceUri the URI where the given {@code uri} was found
	 * @param uri the uri that this task should process
	 * @param depth the depth where this uri is located in the spidering process
	 * @param method the HTTP method that should be used to fetch the resource
	 * @since 2.4.0
	 */
	public SpiderTask(Spider parent, URI sourceUri, URI uri, int depth, String method) {
		this(parent, sourceUri, uri, depth, method, null);
	}

	/**
	 * Instantiates a new spider task using the target URI. The purpose of this task is to crawl the given
	 * uri, using the provided method, find any other uris in the fetched resource and create other tasks.
	 * 
	 * <p>
	 * The body of the request message is also provided in the {@literal requestBody} parameter and will be
	 * used when fetching the resource from the specified uri.
	 * </p>
	 * 
	 * @param parent the spider controlling the crawling process
	 * @param uri the uri that this task should process
	 * @param depth the depth where this uri is located in the spidering process
	 * @param method the HTTP method that should be used to fetch the resource
	 * @param requestBody the body of the request
	 */
	public SpiderTask(Spider parent, URI uri, int depth, String method, String requestBody) {
		this(parent, null, uri, depth, method, requestBody);
	}
	
	/**
	 * Instantiates a new spider task using the target URI. The purpose of this task is to crawl the given
	 * uri, using the provided method, find any other uris in the fetched resource and create other tasks.
	 * <p>
	 * The body of the request message is also provided in the {@literal requestBody} parameter and will be
	 * used when fetching the resource from the specified uri.
	 * 
	 * @param parent the spider controlling the crawling process
	 * @param sourceUri the URI where the given {@code uri} was found
	 * @param uri the uri that this task should process
	 * @param depth the depth where this uri is located in the spidering process
	 * @param method the HTTP method that should be used to fetch the resource
	 * @param requestBody the body of the request
	 * @since 2.4.0
	 */
	public SpiderTask(Spider parent, URI sourceUri, URI uri, int depth, String method, String requestBody) {
		super();
		this.parent = parent;
		this.depth = depth;

		// Log the new task
		if (log.isDebugEnabled()) {
			log.debug("New task submitted for uri: " + uri);
		}

		// Create a new HttpMessage that will be used for the request and persist it in the database using
		// HistoryReference
		try {
			HttpRequestHeader requestHeader = 
					new HttpRequestHeader(method, uri, HttpHeader.HTTP11, parent.getConnectionParam());
			if (sourceUri != null && parent.getSpiderParam().isSendRefererHeader()) {
				requestHeader.setHeader(HttpRequestHeader.REFERER, sourceUri.toString());
			}
			HttpMessage msg = new HttpMessage(requestHeader);
			if (requestBody != null) {
				msg.getRequestHeader().setContentLength(requestBody.length());
				msg.setRequestBody(requestBody);
			}
			this.reference = new HistoryReference(parent.getModel().getSession(), HistoryReference.TYPE_SPIDER_TASK,
					msg);
		} catch (HttpMalformedHeaderException e) {
			log.error("Error while building HttpMessage for uri: " + uri, e);
		} catch (DatabaseException e) {
			log.error("Error while persisting HttpMessage for uri: " + uri, e);
		}
	}

	@Override
	public void run() {
		if (reference == null) {
			log.warn("Null URI. Skipping crawling task: " + this);
			parent.postTaskExecution();
			return;
		}

		// Log the task start
		if (log.isDebugEnabled()) {
			log.debug("Spider Task Started. Processing uri at depth " + depth
					+ " using already constructed message:  " + reference.getURI());
		}

		// Check if the should stop
		if (parent.isStopped()) {
			log.debug("Spider process is stopped. Skipping crawling task...");
			deleteHistoryReference();
			parent.postTaskExecution();
			return;
		}

		// Check if the crawling process is paused and do any "before execution" processing
		parent.preTaskExecution();

		// Fetch the resource
		HttpMessage msg;
		try {
			msg = prepareHttpMessage();
		} catch (Exception e) {
			log.error("Failed to prepare HTTP message: ", e);
			parent.postTaskExecution();
			return;
		}

		try {
			fetchResource(msg);
		} catch (Exception e) {
			setErrorResponse(msg, e);
			parent.notifyListenersSpiderTaskResult(new SpiderTaskResult(msg, getSkippedMessage("ioerror")));

			// The exception was already logged, in fetchResource, with the URL (which we dont have here)
			parent.postTaskExecution();
			return;
		}

		// Check if the should stop
		if (parent.isStopped()) {
		    parent.notifyListenersSpiderTaskResult(new SpiderTaskResult(msg, getSkippedMessage("stopped")));
			log.debug("Spider process is stopped. Skipping crawling task...");
			parent.postTaskExecution();
			return;
		}
		// Check if the crawling process is paused
		parent.checkPauseAndWait();

		// Check the parse filters to see if the resource should be skipped from parsing
		for (ParseFilter filter : parent.getController().getParseFilters()) {
			FilterResult filterResult = filter.filtered(msg);
			if (filterResult.isFiltered()) {
				if (log.isDebugEnabled()) {
					log.debug(
							"Resource [" + msg.getRequestHeader().getURI()
									+ "] fetched, but will not be parsed due to a ParseFilter rule: "
									+ filterResult.getReason());
				}

				parent.notifyListenersSpiderTaskResult(new SpiderTaskResult(msg, filterResult.getReason()));
				parent.postTaskExecution();
				return;
			}
		}

		// Check if the should stop
		if (parent.isStopped()) {
			parent.notifyListenersSpiderTaskResult(new SpiderTaskResult(msg, getSkippedMessage("stopped")));
			log.debug("Spider process is stopped. Skipping crawling task...");
			parent.postTaskExecution();
			return;
		}
		// Check if the crawling process is paused
		parent.checkPauseAndWait();
		
		if (depth < parent.getSpiderParam().getMaxDepth()) {
			parent.notifyListenersSpiderTaskResult(new SpiderTaskResult(msg));
			processResource(msg);
		} else {
			parent.notifyListenersSpiderTaskResult(new SpiderTaskResult(msg, getSkippedMessage("maxdepth")));
		}

		// Update the progress and check if the spidering process should stop
		parent.postTaskExecution();
		log.debug("Spider Task finished.");
	}

	private String getSkippedMessage(String key) {
		return parent.getExtensionSpider().getMessages().getString("spider.task.message.skipped." + key);
	}

	/**
	 * Prepares the HTTP message to be sent to the target server.
	 * <p>
	 * The HTTP message is read from the database and set up with common headers (e.g. User-Agent) and properties (e.g. user).
	 *
	 * @return the HTTP message
	 * @throws HttpMalformedHeaderException if an error occurred while parsing the HTTP message read from the database
	 * @throws DatabaseException if an error occurred while reading the HTTP message from the database
	 */
	private HttpMessage prepareHttpMessage() throws HttpMalformedHeaderException, DatabaseException {
		// Build fetch the request message from the database
		HttpMessage msg;
		try {
			msg = reference.getHttpMessage();
			// HistoryReference is about to be deleted, so no point keeping referencing it.
			msg.setHistoryRef(null);
		} finally {
			deleteHistoryReference();
		}

		msg.getRequestHeader().setHeader(HttpHeader.IF_MODIFIED_SINCE, null);
		msg.getRequestHeader().setHeader(HttpHeader.IF_NONE_MATCH, null);

		// Check if there is a custom user agent
		if (parent.getSpiderParam().getUserAgent() != null) {
			msg.getRequestHeader().setHeader(HttpHeader.USER_AGENT, parent.getSpiderParam().getUserAgent());
		}

		// Check if there's a need to send the message from the point of view of a User
		if (parent.getScanUser() != null) {
			msg.setRequestingUser(parent.getScanUser());
		}
		return msg;
	}
	
	/**
	 * Deletes the history reference, should be called when no longer needed.
	 * <p>
	 * The call to this method has no effect if the history reference no longer exists (i.e. {@code null}).
	 *
	 * @see #reference
	 */
	private void deleteHistoryReference() {
		if (reference == null) {
			return;
		}

		if (getExtensionHistory() != null) {
			getExtensionHistory().delete(reference);
			reference = null;
		}
	}
	
	private void setErrorResponse(HttpMessage msg, Exception cause) {
		StringBuilder strBuilder = new StringBuilder(250);
		if (cause instanceof SSLException) {
			strBuilder.append(Constant.messages.getString("network.ssl.error.connect"));
			strBuilder.append(msg.getRequestHeader().getURI().toString()).append('\n');
			strBuilder.append(Constant.messages.getString("network.ssl.error.exception"))
					.append(cause.getMessage())
					.append('\n');
			strBuilder.append(Constant.messages.getString("network.ssl.error.exception.rootcause"))
					.append(ExceptionUtils.getRootCauseMessage(cause))
					.append('\n');
			strBuilder.append(
					Constant.messages
							.getString("network.ssl.error.help", Constant.messages.getString("network.ssl.error.help.url")));

			strBuilder.append("\n\nStack Trace:\n");
			for (String stackTraceFrame : ExceptionUtils.getRootCauseStackTrace(cause)) {
				strBuilder.append(stackTraceFrame).append('\n');
			}
		} else {
			strBuilder.append(cause.getClass().getName())
					.append(": ")
					.append(cause.getLocalizedMessage())
					.append("\n\nStack Trace:\n");
			for (String stackTraceFrame : ExceptionUtils.getRootCauseStackTrace(cause)) {
				strBuilder.append(stackTraceFrame).append('\n');
			}
		}

		String message = strBuilder.toString();

		HttpResponseHeader responseHeader;
		try {
			responseHeader = new HttpResponseHeader("HTTP/1.1 400 ZAP IO Error");
			responseHeader.setHeader(HttpHeader.CONTENT_TYPE, "text/plain; charset=UTF-8");
			responseHeader
					.setHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(message.getBytes(StandardCharsets.UTF_8).length));
			msg.setResponseHeader(responseHeader);
			msg.setResponseBody(message);
		} catch (HttpMalformedHeaderException e) {
			log.error("Failed to create error response:", e);
		}
	}

	/**
	 * Process a resource, searching for links (uris) to other resources.
	 * 
	 * @param message the HTTP Message
	 */
	private void processResource(HttpMessage message) {
		List<SpiderParser> parsers = parent.getController().getParsers();

		// Prepare the Jericho source
		Source source = new Source(message.getResponseBody().toString());
		
		// Get the full path of the file
		String path = null;
		try {
			path = message.getRequestHeader().getURI().getPath();
		} catch (URIException e) {
		} finally {
			// Handle null paths.
			if (path == null)
				path = "";
		}
		
		// Parse the resource
		boolean alreadyConsumed = false;
		for (SpiderParser parser : parsers) {			
			if (parser.canParseResource(message, path, alreadyConsumed)) {
				if (log.isDebugEnabled()) log.debug("Parser "+ parser +" can parse resource '"+ path + "'");
				if (parser.parseResource(message, source, depth))
					alreadyConsumed = true;
			} else {
				if (log.isDebugEnabled()) log.debug("Parser "+ parser +" cannot parse resource '"+ path + "'");
			}
		}
	}

	private ExtensionHistory getExtensionHistory() {
		if (this.extHistory == null) {
			this.extHistory = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
		}
		return this.extHistory;
	}

	/**
	 * Fetches a resource.
	 * 
	 * @param msg the HTTP message that will be sent to the server
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void fetchResource(HttpMessage msg) throws IOException {
		if (parent.getHttpSender() == null) {
			return;
		}
		
		try {
			parent.getHttpSender().sendAndReceive(msg);
		} catch (ConnectException e) {
			log.debug("Failed to connect to: " + msg.getRequestHeader().getURI(), e);
			throw e;
		} catch (SocketTimeoutException e) {
			log.debug("Socket timeout: " + msg.getRequestHeader().getURI(), e);
			throw e;
		} catch (SocketException e) {
			log.debug("Socket exception: " + msg.getRequestHeader().getURI(), e);
			throw e;
		} catch (UnknownHostException e) {
			log.debug("Unknown host: " + msg.getRequestHeader().getURI(), e);
			throw e;
		} catch (Exception e) {
			log.error("An error occurred while fetching the resource [" + msg.getRequestHeader().getURI() + "]: "
						+ e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Cleans up the resources used by the task.
	 * <p>
	 * Should be called if the task was not executed.
	 * 
	 * @since 2.5.0
	 */
	void cleanup() {
		deleteHistoryReference();
	}

}

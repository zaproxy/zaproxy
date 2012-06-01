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

import java.util.HashSet;
import java.util.LinkedList;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.spider.FetchFilter.FetchStatus;
import org.zaproxy.zap.spider.parser.SpiderHtmlParser;
import org.zaproxy.zap.spider.parser.SpiderParser;
import org.zaproxy.zap.spider.parser.SpiderParserListener;

/**
 * The SpiderController is used to manage the crawling process and interacts directly with the
 * Spider Task threads.
 */
public class SpiderController implements SpiderParserListener {

	/** The fetch filters used by the spider to filter the resources which are fetched. */
	private LinkedList<FetchFilter> fetchFilters;

	/** The parser. */
	private SpiderParser parser;

	/** The spider. */
	private Spider spider;

	/** The connection parameters. */
	private ConnectionParam connectionParam;

	/** The resources visited using GET method. */
	private HashSet<String> visitedGet;

	/** The resources visited using POST method. */
	private HashSet<String> visitedPost;

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(SpiderController.class);

	/**
	 * Instantiates a new spider controller.
	 * 
	 * @param spider the spider
	 * @param connectionParam the connection param
	 */
	protected SpiderController(Spider spider, ConnectionParam connectionParam) {
		super();
		this.spider = spider;
		this.connectionParam = connectionParam;
		this.fetchFilters = new LinkedList<FetchFilter>();
		this.visitedGet = new HashSet<String>();
		this.visitedPost = new HashSet<String>();
		this.parser = new SpiderHtmlParser();
		this.parser.addSpiderParserListener(this);
	}

	/**
	 * Gets the fetch filters used by the spider.
	 * 
	 * @return the fetch filters
	 */
	protected LinkedList<FetchFilter> getFetchFilters() {
		return fetchFilters;
	}

	/**
	 * Adds a new fetch filter to the spider.
	 * 
	 * @param filter the filter
	 */
	public void addFetchFilter(FetchFilter filter) {
		fetchFilters.add(filter);
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.zaproxy.zap.spider.parser.SpiderParserListener#resourceFound(org.parosproxy.paros.network
	 * .HttpMessage, java.lang.String) */
	@Override
	public void resourceFound(HttpMessage responseMessage, String uri) {
		log.warn("New resource found: " + uri);

		// Build the uri
		URI uriV = null;
		try {
			uriV = new URI(uri, true);
		} catch (Exception e) {
			log.error("Error while converting to uri: " + uri, e);
		}

		// Check if any of the filters disallows this uri
		for (FetchFilter f : fetchFilters) {
			FetchStatus s = f.checkFilter(uriV);
			if (s != FetchStatus.VALID) {
				log.warn("URI: " + uriV + " was filtered by a filter with status: " + s);
				return;
			}
		}

		// Submit the task
		SpiderTask task = new SpiderTask(spider, this, uriV);
		spider.submitTask(task);
	}

	public SpiderParser getParser() {
		return parser;
	}
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zaproxy.zap.spider.parser;

import java.util.Locale;
import java.util.StringTokenizer;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.spider.SpiderParam;

/**
 * The Class SpiderRobotstxtParser used for parsing Robots.txt files.
 */
public class SpiderRobotstxtParser extends SpiderParser {

	private static final String PATTERNS_USERAGENT = "(?i)^User-agent:.*";
	private static final String PATTERNS_DISALLOW = "(?i)Disallow:.*";
	private static final String PATTERNS_ALLOW = "(?i)Allow:.*";

	private static final int PATTERNS_USERAGENT_LENGTH = 11;
	private static final int PATTERNS_DISALLOW_LENGTH = 9;
	private static final int PATTERNS_ALLOW_LENGTH = 6;

	/** The params. */
	private SpiderParam params;

	/**
	 * Instantiates a new spider robotstxt parser.
	 * 
	 * @param params the params
	 */
	public SpiderRobotstxtParser(SpiderParam params) {
		super();
		this.params = params;
	}

	@Override
	public boolean parseResource(HttpMessage message, Source source, int depth) {
		if (message == null || !params.isParseRobotsTxt()) {
			return false;
		}
		log.debug("Parsing a robots.txt resource...");

		// Get the response content
		String content = message.getResponseBody().toString();

		// Get the context (base url)
		String baseURL;
		baseURL = message.getRequestHeader().getURI().toString();

		@SuppressWarnings("unused")
		// for now...
		boolean inMatchingUserAgent = false;

		// Parse each line in the Spider.txt file
		StringTokenizer st = new StringTokenizer(content, "\n");
		while (st.hasMoreTokens()) {
			String line = st.nextToken();

			// Remove comments
			int commentStart = line.indexOf("#");
			if (commentStart != -1) {
				line = line.substring(0, commentStart);
			}

			// remove HTML markup and clean
			line = line.replaceAll("<[^>]+>", "");
			line = line.trim();

			// If nothing's left, skip
			if (line.length() == 0) {
				continue;
			}
			log.debug("Processing robots.txt line: " + line);

			// If the line is for defining the user agent
			if (line.matches(PATTERNS_USERAGENT)) {
				String ua = line.substring(PATTERNS_USERAGENT_LENGTH).trim().toLowerCase(Locale.ENGLISH);
				if (ua.equals("*") || ua.contains(Constant.USER_AGENT)) {
					log.debug("Parsing robots.txt file. Starting section applying to spider.");
					inMatchingUserAgent = true;
				} else {
					log.debug("Parsing robots.txt file. Start section not applying to spider.");
					inMatchingUserAgent = false;
				}
				// If the line is for defining a DISALLOW pattern
			} else if (line.matches(PATTERNS_DISALLOW)) {
				// The spider should explore URIs no matter who the pattern applies to
				// if (!inMatchingUserAgent) {
				// continue;
				// }
				String path = line.substring(PATTERNS_DISALLOW_LENGTH).trim();

				// Clean the path
				if (path.endsWith("*")) {
					path = path.substring(0, path.length() - 1);
				}
				path = path.trim();

				// Submit the found url
				if (path.length() > 0) {
					processURL(message, depth, path, baseURL);
				}
				// If the line is for defining an ALLOW pattern
			} else if (line.matches(PATTERNS_ALLOW)) {
				// The spider should explore URIs no matter who the pattern applies to
				// if (!inMatchingUserAgent) {
				// continue;
				// }

				// Get the cleaned path
				String path = line.substring(PATTERNS_ALLOW_LENGTH).trim();
				if (path.endsWith("*")) {
					path = path.substring(0, path.length() - 1);
				}
				path = path.trim();

				// Submit the found url
				if (path.length() > 0) {
					processURL(message, depth, path, baseURL);
				}
			}
		}

		// We consider the message fully parsed, so it doesn't get parsed by 'fallback' parsers
		return true;
	}

	@Override
	public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyParsed) {
		// If it's a robots.txt file
		return path != null && path.equalsIgnoreCase("/robots.txt");
	}
}

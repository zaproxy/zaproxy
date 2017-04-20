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
package org.zaproxy.zap.spider.filters;

import java.util.regex.Pattern;

import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;
import org.zaproxy.zap.spider.SpiderParam;

/**
 * The DefaultParseFilter is an implementation of a {@link ParseFilter} that is default for
 * spidering process. Its filter rules are the following:
 * <ul>
 * <li>the resource body should be under a {@link SpiderParam#getMaxParseSizeBytes() number of bytes}, otherwise it's considered
 * a binary resource.</li>
 * <li>the resource must be of parsable type (text, html, xml, javascript). Actually, the content
 * type should be text/...</li>
 * </ul>
 */
public class DefaultParseFilter extends ParseFilter {

	/**
	 * The Constant MAX_RESPONSE_BODY_SIZE defining the size of response body that is considered too
	 * big for a parsable file.
	 * 
	 * @deprecated (TODO add version) No longer in use, replaced by {@link SpiderParam#getMaxParseSizeBytes()}.
	 */
	@Deprecated
	public static final int MAX_RESPONSE_BODY_SIZE = 512000;

	/**
	 * a pattern to match the SQLite based ".svn/wc.db" file name.
	 */
	private static final Pattern SVN_SQLITE_FILENAME_PATTERN = Pattern.compile (".*/\\.svn/wc.db$");

	/**
	 * a pattern to match the XML based ".svn/entries" file name.
	 */
	private static final Pattern SVN_XML_FILENAME_PATTERN = Pattern.compile (".*/\\.svn/entries$");

	/**
	 * a pattern to match the Git index file.
	 */
	private static final Pattern GIT_FILENAME_PATTERN = Pattern.compile (".*/\\.git/index$");

	/**
	 * The configurations of the spider, never {@code null}.
	 */
	private final SpiderParam params;
	
	/**
	 * Constructs a {@code DefaultParseFilter} with default configurations.
	 *
	 * @deprecated (TODO add version) Replaced by {@link #DefaultParseFilter(SpiderParam)}.
	 */
	@Deprecated
	public DefaultParseFilter() {
		this(new SpiderParam());
	}

	/**
	 * Constructs a {@code DefaultParseFilter} with the given configurations.
	 *
	 * @param params the spider configurations
	 * @throws IllegalArgumentException if the given parameter is {@code null}.
	 * @since TODO add version
	 * @see SpiderParam#getMaxParseSizeBytes()
	 */
	public DefaultParseFilter(SpiderParam params) {
		if (params == null) {
			throw new IllegalArgumentException("Parameter params must not be null.");
		}
		this.params = params;
	}
	
	@Override
	public boolean isFiltered(HttpMessage responseMessage) {
		if (responseMessage == null || responseMessage.getRequestHeader().isEmpty()
				|| responseMessage.getResponseHeader().isEmpty()) {
			return true;
		}

		//if it's a file ending in "/.svn/entries", or "/.svn/wc.db", the SVN Entries or Git parsers will process it 
		//regardless of type, and regardless of whether it exceeds the file size restriction below.
		String fullfilename = responseMessage.getRequestHeader().getURI().getEscapedPath();
		if (fullfilename != null && (SVN_SQLITE_FILENAME_PATTERN.matcher(fullfilename).find()
				|| SVN_XML_FILENAME_PATTERN.matcher(fullfilename).find()
				|| GIT_FILENAME_PATTERN.matcher(fullfilename).find())) {
			return false;
		}

		// Check response body size
		if (responseMessage.getResponseBody().length() > params.getMaxParseSizeBytes()) {
			if (log.isDebugEnabled()) {
				log.debug("Resource too large: " + responseMessage.getRequestHeader().getURI());
			}
			return true;
		}

		// If it's a redirection, accept it, as the SpiderRedirectParser will process it
		if (HttpStatusCode.isRedirection(responseMessage.getResponseHeader().getStatusCode())) {
			return false;
		}
		
		// Check response type.
		if (!responseMessage.getResponseHeader().isText()) {
			if (log.isDebugEnabled()) {
				log.debug("Resource is not text: " + responseMessage.getRequestHeader().getURI());
			}
			return true;
		}

		return false;
	}

}

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;

/**
 * The DefaultParseFilter is an implementation of a {@link ParseFilter} that is default for
 * spidering process. Its filter rules are the following:
 * <ul>
 * <li>the resource body should be under MAX_RESPONSE_BODY_SIZE bytes, otherwise it's probably a
 * binary resource.</li>
 * <li>the resource must be of parsable type (text, html, xml, javascript). Actually, the content
 * type should be text/...</li>
 * </ul>
 */
public class DefaultParseFilter extends ParseFilter {

	/**
	 * The Constant MAX_RESPONSE_BODY_SIZE defining the size of response body that is considered too
	 * big for a parsable file.
	 */
	public static final int MAX_RESPONSE_BODY_SIZE = 512000;

	/**
	 * a pattern to match the SQLite based ".svn/wc.db" file name.
	 */
	private static final Pattern svnSQLiteFilenamePattern = Pattern.compile (".*/\\.svn/wc.db$");

	/**
	 * a pattern to match the XML based ".svn/entries" file name.
	 */
	private static final Pattern svnXMLFilenamePattern = Pattern.compile (".*/\\.svn/entries$");

	/**
	 * a pattern to match the Git index file.
	 */
	private static final Pattern gitFilenamePattern = Pattern.compile (".*/\\.git/index$");

	@Override
	public boolean isFiltered(HttpMessage responseMessage) {

		//if it's a file ending in "/.svn/entries", or "/.svn/wc.db", the SVN Entries or Git parsers will process it 
		//regardless of type, and regardless of whether it exceeds the file size restriction below.
		
		Matcher svnXMLFilenameMatcher, svnSQLiteFilenameMatcher, gitFilenameMatcher;
		try {
			String fullfilename = responseMessage.getRequestHeader().getURI().getPath();
			//handle null paths
			if (fullfilename == null) fullfilename = "";
			svnSQLiteFilenameMatcher = svnSQLiteFilenamePattern.matcher(fullfilename);
			svnXMLFilenameMatcher = svnXMLFilenamePattern.matcher(fullfilename);
			gitFilenameMatcher = gitFilenamePattern.matcher(fullfilename);
			
			if ( svnSQLiteFilenameMatcher.find() || svnXMLFilenameMatcher.find() || gitFilenameMatcher.find())
				return false;
		} catch (URIException e) {
			//give other parsers a chance to parse it.
			log.error(e);			
		}

		// Check response body size
		if (responseMessage.getResponseBody().length() > MAX_RESPONSE_BODY_SIZE) {
			if (log.isDebugEnabled()) {
				log.debug("Resource too large: " + responseMessage.getRequestHeader().getURI());
			}
			return true;
		}

		// If it's a redirection, accept it, as the SpiderRedirectParser will process it
		if (HttpStatusCode.isRedirection(responseMessage.getResponseHeader().getStatusCode()))
			return false;
		
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

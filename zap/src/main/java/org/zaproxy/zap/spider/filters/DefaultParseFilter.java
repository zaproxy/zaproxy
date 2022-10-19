/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.spider.filters;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;

/**
 * The DefaultParseFilter is an implementation of a {@link ParseFilter} that is default for
 * spidering process. Its filter rules are the following:
 *
 * <ul>
 *   <li>the resource body should be under a {@code SpiderParam#getMaxParseSizeBytes() number of
 *       bytes}, otherwise it's considered a binary resource.
 *   <li>the resource must be of parsable type (text, html, xml, javascript). Actually, the content
 *       type should be text/...
 * </ul>
 *
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class DefaultParseFilter extends ParseFilter {

    /**
     * The Constant MAX_RESPONSE_BODY_SIZE defining the size of response body that is considered too
     * big for a parsable file.
     *
     * @deprecated (2.7.0) No longer in use, replaced by {@code SpiderParam#getMaxParseSizeBytes()}.
     */
    @Deprecated public static final int MAX_RESPONSE_BODY_SIZE = 512000;

    /** a pattern to match the SQLite based ".svn/wc.db" file name. */
    private static final Pattern SVN_SQLITE_FILENAME_PATTERN = Pattern.compile(".*/\\.svn/wc.db$");

    /** a pattern to match the XML based ".svn/entries" file name. */
    private static final Pattern SVN_XML_FILENAME_PATTERN = Pattern.compile(".*/\\.svn/entries$");

    /** a pattern to match the Git index file. */
    private static final Pattern GIT_FILENAME_PATTERN = Pattern.compile(".*/\\.git/index$");

    /** a pattern to match the robots.txt file. */
    private static final Pattern ROBOTS_FILENAME_PATTERN =
            Pattern.compile(".*/robots.txt$", Pattern.CASE_INSENSITIVE);

    /** a pattern to match the sitemap.xml file. */
    private static final Pattern SITEMAP_FILENAME_PATTERN =
            Pattern.compile(".*/sitemap.xml$", Pattern.CASE_INSENSITIVE);

    /** The configurations of the spider, never {@code null}. */
    private final org.zaproxy.zap.spider.SpiderParam params;

    private final FilterResult filterResultEmpty;
    private final FilterResult filterResultMaxSize;
    private final FilterResult filterResultNotText;

    /**
     * Constructs a {@code DefaultParseFilter} with default configurations.
     *
     * @deprecated (2.7.0) Replaced by {@code #DefaultParseFilter(SpiderParam, ResourceBundle)}.
     */
    @Deprecated
    public DefaultParseFilter() {
        this(
                new org.zaproxy.zap.spider.SpiderParam(),
                new ResourceBundle() {

                    @Override
                    public Enumeration<String> getKeys() {
                        return Collections.emptyEnumeration();
                    }

                    @Override
                    protected Object handleGetObject(String key) {
                        return "";
                    }
                });
    }

    /**
     * Constructs a {@code DefaultParseFilter} with the given configurations and resource bundle.
     *
     * <p>The resource bundle is used to obtain the (internationalised) reasons of why the message
     * was filtered.
     *
     * @param params the spider configurations
     * @param resourceBundle the resource bundle to obtain the internationalised reasons.
     * @throws IllegalArgumentException if any of the given parameters is {@code null}.
     * @since 2.7.0
     */
    public DefaultParseFilter(
            org.zaproxy.zap.spider.SpiderParam params, ResourceBundle resourceBundle) {
        if (params == null) {
            throw new IllegalArgumentException("Parameter params must not be null.");
        }
        if (resourceBundle == null) {
            throw new IllegalArgumentException("Parameter resourceBundle must not be null.");
        }
        this.params = params;

        filterResultEmpty =
                new FilterResult(resourceBundle.getString("spider.parsefilter.reason.empty"));
        filterResultMaxSize =
                new FilterResult(resourceBundle.getString("spider.parsefilter.reason.maxsize"));
        filterResultNotText =
                new FilterResult(resourceBundle.getString("spider.parsefilter.reason.nottext"));
    }

    @Override
    public FilterResult filtered(HttpMessage responseMessage) {
        if (responseMessage == null
                || responseMessage.getRequestHeader().isEmpty()
                || responseMessage.getResponseHeader().isEmpty()) {
            return filterResultEmpty;
        }

        // if it's a file ending in "/.svn/entries", or "/.svn/wc.db", the SVN Entries or Git
        // parsers will process it
        // if it's a robots.txt or sitemap.xml the relevant parsers will process it
        // regardless of type, and regardless of whether it exceeds the file size restriction below.
        String fullfilename = responseMessage.getRequestHeader().getURI().getEscapedPath();
        if (fullfilename != null
                && (SVN_SQLITE_FILENAME_PATTERN.matcher(fullfilename).find()
                        || SVN_XML_FILENAME_PATTERN.matcher(fullfilename).find()
                        || GIT_FILENAME_PATTERN.matcher(fullfilename).find()
                        || ROBOTS_FILENAME_PATTERN.matcher(fullfilename).find()
                        || SITEMAP_FILENAME_PATTERN.matcher(fullfilename).find())) {
            return FilterResult.NOT_FILTERED;
        }

        // Check response body size
        if (responseMessage.getResponseBody().length() > params.getMaxParseSizeBytes()) {
            getLogger()
                    .debug("Resource too large: {}", responseMessage.getRequestHeader().getURI());
            return filterResultMaxSize;
        }

        // If it's a redirection, accept it, as the SpiderRedirectParser will process it
        if (HttpStatusCode.isRedirection(responseMessage.getResponseHeader().getStatusCode())) {
            return FilterResult.NOT_FILTERED;
        }

        // Check response type.
        if (!responseMessage.getResponseHeader().isText()) {
            getLogger()
                    .debug("Resource is not text: {}", responseMessage.getRequestHeader().getURI());
            return filterResultNotText;
        }

        return FilterResult.NOT_FILTERED;
    }
}

/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

/** Unit test for {@link DefaultParseFilter}. */
@SuppressWarnings("deprecation")
class DefaultParseFilterUnitTest {

    private static final String FILTERED_REASON_EMPTY = "empty";
    private static final String FILTERED_REASON_MAX_SIZE = "maxSize";
    private static final String FILTERED_REASON_NOT_TEXT = "notText";

    private ResourceBundle resourceBundle;

    @BeforeEach
    void setUp() throws Exception {
        resourceBundle =
                new ResourceBundle() {

                    @Override
                    protected Object handleGetObject(String key) {
                        switch (key) {
                            case "spider.parsefilter.reason.empty":
                                return FILTERED_REASON_EMPTY;
                            case "spider.parsefilter.reason.maxsize":
                                return FILTERED_REASON_MAX_SIZE;
                            case "spider.parsefilter.reason.nottext":
                                return FILTERED_REASON_NOT_TEXT;
                        }
                        return null;
                    }

                    @Override
                    public Enumeration<String> getKeys() {
                        return Collections.emptyEnumeration();
                    }
                };
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldCreateDefaultParseFilterWithDefaultConfigsAndResourceBundleIfNoneSet() {
        assertDoesNotThrow(() -> new DefaultParseFilter());
    }

    @Test
    void shouldCreateDefaultParseFilterWithConfigsAndResourceBundleSet() {
        // Given
        org.zaproxy.zap.spider.SpiderParam configs = new org.zaproxy.zap.spider.SpiderParam();
        // When / Then
        assertDoesNotThrow(() -> new DefaultParseFilter(configs, resourceBundle));
    }

    @Test
    void shouldFailToCreateDefaultParseFilterWithNullConfigs() {
        // Given
        org.zaproxy.zap.spider.SpiderParam configs = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new DefaultParseFilter(configs, resourceBundle));
    }

    @Test
    void shouldFailToCreateDefaultParseFilterWithNullResourceBundle() {
        // Given
        ResourceBundle resourceBundle = null;
        org.zaproxy.zap.spider.SpiderParam configs = new org.zaproxy.zap.spider.SpiderParam();
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new DefaultParseFilter(configs, resourceBundle));
    }

    @Test
    void shouldFilterNullHttpMessage() {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = null;
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(true)));
        assertThat(filterResult.getReason(), is(equalTo(FILTERED_REASON_EMPTY)));
    }

    @Test
    void shouldFilterHttpMessageWithEmptyRequestHeader() {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = new HttpMessage();
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(true)));
        assertThat(filterResult.getReason(), is(equalTo(FILTERED_REASON_EMPTY)));
    }

    @Test
    void shouldFilterHttpMessageWithEmptyResponseHeader() {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createDefaultRequest();
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(true)));
        assertThat(filterResult.getReason(), is(equalTo(FILTERED_REASON_EMPTY)));
    }

    @Test
    void shouldHandleHttpMessageWithNoPathInRequestUri() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createHttpMessageWithRequestUri("http://example.com");
        // When / Then
        assertDoesNotThrow(() -> filter.filtered(httpMessage));
    }

    @Test
    void shouldNotFilterHttpMessageWithSvnXmlRequest() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createHttpMessageWithRequestUri("/.svn/wc.db");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    void shouldNotFilterHttpMessageWithSvnDbRequest() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createHttpMessageWithRequestUri("/.svn/entries");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    void shouldNotFilterHttpMessageWithGitRequest() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createHttpMessageWithRequestUri("/.git/index");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    void shouldNotFilterHttpMessageWithRobotsTxtRequestEvenWithoutContentType() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createHttpMessageWithRequestUri("/robots.txt");
        httpMessage.getResponseHeader().setHeader(HttpHeader.CONTENT_TYPE, "");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    void shouldNotFilterHttpMessageWithRobotsTxtRequestEvenWithContentType() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createHttpMessageWithRequestUri("/robots.txt");
        httpMessage.getResponseHeader().setHeader(HttpHeader.CONTENT_TYPE, "text/plain");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    void shouldNotFilterHttpMessageWithSitemapXmlRequestEvenWithoutContentType() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createHttpMessageWithRequestUri("/sitemap.xml");
        httpMessage.getResponseHeader().setHeader(HttpHeader.CONTENT_TYPE, "");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    void shouldNotFilterHttpMessageWithSitemapXmlRequestEvenWithContentType() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createHttpMessageWithRequestUri("/sitemap.xml");
        httpMessage.getResponseHeader().setHeader(HttpHeader.CONTENT_TYPE, "application/xml");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    void shouldFilterHttpMessageWithNonTextResponse() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createDefaultRequest();
        httpMessage.setResponseHeader("HTTP/1.1 200 OK\r\nContent-Type: application/x-binary\r\n");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(true)));
        assertThat(filterResult.getReason(), is(equalTo(FILTERED_REASON_NOT_TEXT)));
    }

    @Test
    void shouldNotFilterHttpMessageWithTextResponse() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createDefaultRequest();
        httpMessage.setResponseHeader("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    void shouldNotFilterHttpMessageWithRedirectResponse() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createDefaultRequest();
        httpMessage.setResponseHeader("HTTP/1.1 303 See Other\r\n");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    void shouldFilterHttpMessageWithResponseAboveMaxParseSize() throws Exception {
        // Given
        int maxParseSizeBytes = 2;
        DefaultParseFilter filter =
                new DefaultParseFilter(createSpiderParam(maxParseSizeBytes), resourceBundle);
        HttpMessage httpMessage = createHttpMessageWithResponseBody("ABC");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(true)));
        assertThat(filterResult.getReason(), is(equalTo(FILTERED_REASON_MAX_SIZE)));
    }

    @Test
    void shouldNotFilterHttpMessageWithResponseEqualToMaxParseSize() throws Exception {
        // Given
        int maxParseSizeBytes = 2;
        DefaultParseFilter filter =
                new DefaultParseFilter(createSpiderParam(maxParseSizeBytes), resourceBundle);
        HttpMessage httpMessage = createHttpMessageWithResponseBody("AB");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    void shouldNotFilterHttpMessageWithResponseUnderMaxParseSize() throws Exception {
        // Given
        int maxParseSizeBytes = 2;
        DefaultParseFilter filter =
                new DefaultParseFilter(createSpiderParam(maxParseSizeBytes), resourceBundle);
        HttpMessage httpMessage = createHttpMessageWithResponseBody("A");
        // When
        org.zaproxy.zap.spider.filters.ParseFilter.FilterResult filterResult =
                filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    private DefaultParseFilter createDefaultParseFilter() {
        return new DefaultParseFilter(createSpiderParam(Integer.MAX_VALUE), resourceBundle);
    }

    private static org.zaproxy.zap.spider.SpiderParam createSpiderParam(
            final int maxParseSizeBytes) {
        return new org.zaproxy.zap.spider.SpiderParam() {

            @Override
            public int getMaxParseSizeBytes() {
                return maxParseSizeBytes;
            }
        };
    }

    private static HttpMessage createHttpMessageWithResponseBody(String responseBody) {
        try {
            HttpMessage message = createDefaultRequest();
            message.setResponseHeader("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n");
            message.setResponseBody(responseBody);
            return message;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpMessage createDefaultRequest() {
        try {
            return new HttpMessage(
                    new HttpRequestHeader("GET / HTTP/1.1\r\nHost: example.com\r\n"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpMessage createHttpMessageWithRequestUri(String requestUri) {
        try {
            HttpMessage message =
                    new HttpMessage(
                            new HttpRequestHeader(
                                    "GET " + requestUri + " HTTP/1.1\r\nHost: example.com\r\n"));
            message.setResponseHeader("HTTP/1.1 200 OK\r\n");
            return message;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

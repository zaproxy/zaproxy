/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2017 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zaproxy.zap.spider.filters;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.spider.filters.ParseFilter.FilterResult;

/**
 * Unit test for {@link DefaultParseFilter}.
 */
public class DefaultParseFilterUnitTest {

    private static final String FILTERED_REASON_EMPTY = "empty";
    private static final String FILTERED_REASON_MAX_SIZE = "maxSize";
    private static final String FILTERED_REASON_NOT_TEXT = "notText";
    
    private ResourceBundle resourceBundle;
    
    @BeforeClass
    public static void suppressLogging() {
        Logger.getRootLogger().addAppender(new NullAppender());
    }

    @Before
    public void setUp() throws Exception {
        resourceBundle = new ResourceBundle() {

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
    @SuppressWarnings({ "deprecation", "unused" })
    public void shouldCreateDefaultParseFilterWithDefaultConfigsAndResourceBundleIfNoneSet() {
        // Given / When
        new DefaultParseFilter();
        // Then = No exception.
    }

    @Test
    @SuppressWarnings("unused")
    public void shouldCreateDefaultParseFilterWithConfigsAndResourceBundleSet() {
        // Given
        SpiderParam configs = new SpiderParam();
        // When
        new DefaultParseFilter(configs, resourceBundle);
        // Then = No exception.
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void shouldFailToCreateDefaultParseFilterWithNullConfigs() {
        // Given
        SpiderParam configs = null;
        // When
        new DefaultParseFilter(configs, resourceBundle);
        // Then = IllegalArgumentException.
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void shouldFailToCreateDefaultParseFilterWithNullResourceBundle() {
        // Given
        ResourceBundle resourceBundle = null;
        SpiderParam configs = new SpiderParam();
        // When
        new DefaultParseFilter(configs, resourceBundle);
        // Then = IllegalArgumentException.
    }

    @Test
    public void shouldFilterNullHttpMessage() {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = null;
        // When
        FilterResult filterResult = filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(true)));
        assertThat(filterResult.getReason(), is(equalTo(FILTERED_REASON_EMPTY)));
    }

    @Test
    public void shouldFilterHttpMessageWithEmptyRequestHeader() {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = new HttpMessage();
        // When
        FilterResult filterResult = filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(true)));
        assertThat(filterResult.getReason(), is(equalTo(FILTERED_REASON_EMPTY)));
    }

    @Test
    public void shouldFilterHttpMessageWithEmptyResponseHeader() {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createDefaultRequest();
        // When
        FilterResult filterResult = filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(true)));
        assertThat(filterResult.getReason(), is(equalTo(FILTERED_REASON_EMPTY)));
    }

    @Test
    public void shouldHandleHttpMessageWithNoPathInRequestUri() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createHttpMessageWithRequestUri("http://example.com");
        // When
        filter.filtered(httpMessage);
        // Then = No exception.
    }

    @Test
    public void shouldNotFilterHttpMessageWithSvnXmlRequest() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createHttpMessageWithRequestUri("/.svn/wc.db");
        // When
        FilterResult filterResult = filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    public void shouldNotFilterHttpMessageWithSvnDbRequest() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createHttpMessageWithRequestUri("/.svn/entries");
        // When
        FilterResult filterResult = filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    public void shouldNotFilterHttpMessageWithGitRequest() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createHttpMessageWithRequestUri("/.git/index");
        // When
        FilterResult filterResult = filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    public void shouldFilterHttpMessageWithNonTextResponse() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createDefaultRequest();
        httpMessage.setResponseHeader("HTTP/1.1 200 OK\r\nContent-Type: application/x-binary\r\n");
        // When
        FilterResult filterResult = filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(true)));
        assertThat(filterResult.getReason(), is(equalTo(FILTERED_REASON_NOT_TEXT)));
    }

    @Test
    public void shouldNotFilterHttpMessageWithTextResponse() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createDefaultRequest();
        httpMessage.setResponseHeader("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n");
        // When
        FilterResult filterResult = filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    public void shouldNotFilterHttpMessageWithRedirectResponse() throws Exception {
        // Given
        DefaultParseFilter filter = createDefaultParseFilter();
        HttpMessage httpMessage = createDefaultRequest();
        httpMessage.setResponseHeader("HTTP/1.1 303 See Other\r\n");
        // When
        FilterResult filterResult = filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    public void shouldFilterHttpMessageWithResponseAboveMaxParseSize() throws Exception {
        // Given
        int maxParseSizeBytes = 2;
        DefaultParseFilter filter = new DefaultParseFilter(createSpiderParam(maxParseSizeBytes), resourceBundle);
        HttpMessage httpMessage = createHttpMessageWithResponseBody("ABC");
        // When
        FilterResult filterResult = filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(true)));
        assertThat(filterResult.getReason(), is(equalTo(FILTERED_REASON_MAX_SIZE)));
    }

    @Test
    public void shouldNotFilterHttpMessageWithResponseEqualToMaxParseSize() throws Exception {
        // Given
        int maxParseSizeBytes = 2;
        DefaultParseFilter filter = new DefaultParseFilter(createSpiderParam(maxParseSizeBytes), resourceBundle);
        HttpMessage httpMessage = createHttpMessageWithResponseBody("AB");
        // When
        FilterResult filterResult = filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    @Test
    public void shouldNotFilterHttpMessageWithResponseUnderMaxParseSize() throws Exception {
        // Given
        int maxParseSizeBytes = 2;
        DefaultParseFilter filter = new DefaultParseFilter(createSpiderParam(maxParseSizeBytes), resourceBundle);
        HttpMessage httpMessage = createHttpMessageWithResponseBody("A");
        // When
        FilterResult filterResult = filter.filtered(httpMessage);
        // Then
        assertThat(filterResult.isFiltered(), is(equalTo(false)));
    }

    private DefaultParseFilter createDefaultParseFilter() {
        return new DefaultParseFilter(createSpiderParam(Integer.MAX_VALUE), resourceBundle);
    }

    private static SpiderParam createSpiderParam(final int maxParseSizeBytes) {
        return new SpiderParam() {

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
            return new HttpMessage(new HttpRequestHeader("GET / HTTP/1.1\r\nHost: example.com\r\n"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpMessage createHttpMessageWithRequestUri(String requestUri) {
        try {
            HttpMessage message = new HttpMessage(
                    new HttpRequestHeader("GET " + requestUri + " HTTP/1.1\r\nHost: example.com\r\n"));
            message.setResponseHeader("HTTP/1.1 200 OK\r\n");
            return message;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

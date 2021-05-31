/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.spider.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import net.htmlparser.jericho.Source;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.spider.URLCanonicalizer;
import org.zaproxy.zap.spider.parser.SpiderParserTestUtils.TestSpiderParserListener;

/** Unit test for {@link SpiderParserGenericWithBuilderUnitTest}. */
class SpiderParserGenericWithBuilderUnitTest extends SpiderParserTestUtils {
    private static final int DEFAULT_NUMBER_RESOURCES_TO_SUBMIT = 5;

    @Test
    void shouldHaveNoAdditionalHeadersIfNotSet() {
        // Given
        TestSpiderParser testSpiderParser =
                new TestSpiderParser(DEFAULT_NUMBER_RESOURCES_TO_SUBMIT, false);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        testSpiderParser.addSpiderParserListener(listener);
        // When
        testSpiderParser.parseResource(createBasicMessage(), null, 0);
        // Then
        assertThat(
                listener.getNumberOfUrlsFound(), is(equalTo(DEFAULT_NUMBER_RESOURCES_TO_SUBMIT)));
        listener.getResourcesFound()
                .forEach(
                        r -> {
                            assertThat(r.getHeaders().isEmpty(), is(equalTo(true)));
                        });
    }

    @Test
    void shouldAddCustomHeadersToFoundUrls() {
        // Given
        List<HttpHeaderField> requestHeaders = new ArrayList<>();
        requestHeaders.add(new HttpHeaderField("Accept", "application/json, text/html, */*"));
        requestHeaders.add(new HttpHeaderField("X-Custom-Header", "xyz"));
        TestSpiderParser testSpiderParser =
                new TestSpiderParser(DEFAULT_NUMBER_RESOURCES_TO_SUBMIT, false, requestHeaders);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        testSpiderParser.addSpiderParserListener(listener);
        // When
        testSpiderParser.parseResource(createBasicMessage(), null, 0);
        // Then
        assertThat(
                listener.getNumberOfUrlsFound(), is(equalTo(DEFAULT_NUMBER_RESOURCES_TO_SUBMIT)));
        listener.getResourcesFound()
                .forEach(
                        r -> {
                            assertThat(r.getHeaders().size(), is(equalTo(requestHeaders.size())));
                            assertThat(r.getHeaders().get(0).getName(), is(equalTo("Accept")));
                            assertThat(
                                    r.getHeaders().get(0).getValue(),
                                    is(equalTo("application/json, text/html, */*")));
                            assertThat(
                                    r.getHeaders().get(1).getName(),
                                    is(equalTo("X-Custom-Header")));
                            assertThat(r.getHeaders().get(1).getValue(), is(equalTo("xyz")));
                        });
    }

    @Test
    void shouldAddHeadersToFoundPostBodyUrlsWithCorrectAttributes() {
        // Given
        List<HttpHeaderField> requestHeaders = new ArrayList<>();
        requestHeaders.add(new HttpHeaderField("X-Custom-Header", "xyz"));
        TestSpiderParser testSpiderParser =
                new TestSpiderParser(DEFAULT_NUMBER_RESOURCES_TO_SUBMIT, true, requestHeaders);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        testSpiderParser.addSpiderParserListener(listener);
        HttpMessage messageResponse = createBasicMessage();
        int testDepth = 7;
        // When
        testSpiderParser.parseResource(messageResponse, null, testDepth);
        // Then
        assertThat(
                listener.getNumberOfUrlsFound(), is(equalTo(DEFAULT_NUMBER_RESOURCES_TO_SUBMIT)));
        listener.getResourcesFound()
                .forEach(
                        r -> {
                            assertThat(r.getHeaders().size(), is(equalTo(requestHeaders.size())));
                            assertThat(
                                    r.getHeaders().get(0).getName(),
                                    is(equalTo("X-Custom-Header")));
                            assertThat(r.getHeaders().get(0).getValue(), is(equalTo("xyz")));
                            assertThat(r.getMethod(), is(equalTo(HttpRequestHeader.POST)));
                            assertThat(r.getRequestBody().isEmpty(), is(equalTo(false)));
                            assertThat(r.getDepth(), is(equalTo(testDepth)));
                            assertThat(r.getMessage(), is(messageResponse));
                            assertThat(
                                    r.getUri().startsWith(TestSpiderParser.SPIDER_RESOURCE_ORIGIN),
                                    is(equalTo(true)));
                        });
    }

    @Test
    void shouldThrowExceptionForNullHeaderField() {
        // Given
        List<HttpHeaderField> requestHeaders = new ArrayList<>();
        requestHeaders.add(new HttpHeaderField("X-Custom-Header", "xyz"));
        requestHeaders.add(null);
        requestHeaders.add(new HttpHeaderField("X-Custom-Header-2", "123"));
        TestSpiderParser testSpiderParser =
                new TestSpiderParser(DEFAULT_NUMBER_RESOURCES_TO_SUBMIT, false, requestHeaders);
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> testSpiderParser.parseResource(createBasicMessage(), null, 0));
    }

    @Test
    void shouldThrowExceptionForNegativeDepth() {
        // Given
        TestSpiderParser testSpiderParser =
                new TestSpiderParser(DEFAULT_NUMBER_RESOURCES_TO_SUBMIT, false);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        testSpiderParser.addSpiderParserListener(listener);
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> testSpiderParser.parseResource(createBasicMessage(), null, -99));
    }

    @Test
    void shouldSkipInvalidHeaders() {
        // Given
        List<HttpHeaderField> requestHeaders = new ArrayList<>();
        requestHeaders.add(new HttpHeaderField("X-Custom-Header-1", "xyz"));
        requestHeaders.add(new HttpHeaderField(null, "123"));
        requestHeaders.add(new HttpHeaderField("", "123"));
        requestHeaders.add(new HttpHeaderField(" ", "123"));
        requestHeaders.add(new HttpHeaderField("X-Custom-Header-3", null));
        TestSpiderParser testSpiderParser =
                new TestSpiderParser(DEFAULT_NUMBER_RESOURCES_TO_SUBMIT, false, requestHeaders);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        testSpiderParser.addSpiderParserListener(listener);
        HttpMessage messageResponse = createBasicMessage();
        // When
        testSpiderParser.parseResource(messageResponse, null, 0);
        // Then
        assertThat(
                listener.getNumberOfUrlsFound(), is(equalTo(DEFAULT_NUMBER_RESOURCES_TO_SUBMIT)));
        listener.getResourcesFound()
                .forEach(
                        r -> {
                            assertThat(r.getHeaders().size(), is(equalTo(1)));
                            assertThat(
                                    r.getHeaders().get(0).getName(),
                                    is(equalTo("X-Custom-Header-1")));
                            assertThat(r.getHeaders().get(0).getValue(), is(equalTo("xyz")));
                        });
    }

    @Test
    void shouldHaveOneDistinctSpiderResourceForRepeatedMessageParsing() {
        // Given
        TestSpiderParser testSpiderParser = new TestSpiderParser(1, false);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        testSpiderParser.addSpiderParserListener(listener);
        HttpMessage messageResponse = createBasicMessage();
        int repeatCount = 4;
        // When
        IntStream.range(0, repeatCount)
                .forEach(i -> testSpiderParser.parseResource(messageResponse, null, 0));
        // Then
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(repeatCount)));
        assertThat(new HashSet<>(listener.getResourcesFound()).size(), is(equalTo(1)));
    }

    private static HttpMessage createBasicMessage() {
        HttpMessage message = new HttpMessage();
        try {
            message.setRequestHeader(
                    "GET https://server.com/resource.txt HTTP/1.1\r\nHost: example.com\r\n");
            message.setResponseHeader(
                    "HTTP/1.1 200 OK\r\n"
                            + "Content-Type: text/html; charset=UTF-8\r\n"
                            + "Content-Length: 1");
            message.setResponseBody("a");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    /**
     * Test spider parser parses all resources and submits a fixed number of resources found with a
     * given path prefix and suffix either as GET or POST with a body.
     */
    private static class TestSpiderParser extends SpiderParser {
        private static String SPIDER_RESOURCE_ORIGIN = "https://server.com/";
        private int numResourcesToSubmit;
        private boolean postWithBody;
        private List<HttpHeaderField> requestHeaders = null;
        private String resourcePathPrefix = "";
        private String resourcePathSuffix = "";
        private String bodyPrefix = "";

        TestSpiderParser(int numResourcesToSubmit, boolean postWithBody) {
            this.numResourcesToSubmit = numResourcesToSubmit;
            if (this.numResourcesToSubmit < 1) {
                this.numResourcesToSubmit = 1;
            }
            this.postWithBody = postWithBody;
            this.requestHeaders = Collections.emptyList();
        }

        TestSpiderParser(
                int numResourcesToSubmit,
                boolean postWithBody,
                List<HttpHeaderField> requestHeaders) {
            this.numResourcesToSubmit = numResourcesToSubmit;
            if (this.numResourcesToSubmit < 1) {
                this.numResourcesToSubmit = 1;
            }
            this.postWithBody = postWithBody;
            this.requestHeaders = requestHeaders;
        }

        @Override
        public boolean parseResource(HttpMessage message, Source source, int depth) {
            for (int i = 0; i < numResourcesToSubmit; ++i) {
                if (postWithBody) {
                    notifyListenersResourceFound(
                            SpiderResourceFound.builder()
                                    .setMessage(message)
                                    .setDepth(depth)
                                    .setUri(buildResourceUrl(i))
                                    .setMethod(HttpRequestHeader.POST)
                                    .setBody(buildBody(i))
                                    .setRequestHeaders(requestHeaders)
                                    .build());
                } else {
                    // URL canonicalization as processURL(...) doesn't support req. headers
                    String url =
                            URLCanonicalizer.getCanonicalURL(
                                    buildResourceUrl(i),
                                    message.getRequestHeader().getURI().toString());
                    notifyListenersResourceFound(
                            SpiderResourceFound.builder()
                                    .setMessage(message)
                                    .setDepth(depth)
                                    .setUri(url)
                                    .setRequestHeaders(requestHeaders)
                                    .build());
                }
            }
            return true;
        }

        @Override
        public boolean canParseResource(
                HttpMessage message, String path, boolean wasAlreadyConsumed) {
            return true;
        }

        private String buildResourceUrl(int i) {
            StringBuilder urlBuilder = new StringBuilder(20);
            urlBuilder.append(SPIDER_RESOURCE_ORIGIN);
            if (resourcePathPrefix != null && !resourcePathPrefix.isEmpty()) {
                urlBuilder.append(resourcePathPrefix);
            }
            urlBuilder.append(i);
            if (resourcePathSuffix != null && !resourcePathSuffix.isEmpty()) {
                urlBuilder.append(resourcePathSuffix);
            }
            return urlBuilder.toString();
        }

        private String buildBody(int i) {
            StringBuilder bodyBuilder = new StringBuilder(20);
            if (bodyPrefix != null && !bodyPrefix.isEmpty()) {
                bodyBuilder.append(bodyPrefix);
            }
            bodyBuilder.append("\n>>" + i);
            return bodyBuilder.toString();
        }

        void setResourcePathPrefix(String resourcePathPrefix) {
            this.resourcePathPrefix = resourcePathPrefix;
        }

        void setResourcePathSuffix(String resourcePathSuffix) {
            this.resourcePathSuffix = resourcePathSuffix;
        }

        void setBodyPrefix(String bodyPrefix) {
            this.bodyPrefix = bodyPrefix;
        }

        void setRequestHeaders(List<HttpHeaderField> requestHeaders) {
            this.requestHeaders = requestHeaders;
        }
    }
}

/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import net.htmlparser.jericho.Source;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

/** Unit test for {@link SpiderParser}. */
@SuppressWarnings("deprecation")
class SpiderParserUnitTest extends SpiderParserTestUtils {

    @Test
    void shouldHaveNonNullLogger() {
        // Given
        TestSpiderParser testSpiderParser = new TestSpiderParser();
        // When / Then
        assertThat(testSpiderParser.getLogger(), is(not(nullValue())));
    }

    @Test
    void shouldNotifyListenersOfResourceFound() {
        // Given
        TestSpiderParser testSpiderParser = new TestSpiderParser();
        TestSpiderParserListener listener1 = createTestSpiderParserListener();
        TestSpiderParserListener listener2 = createTestSpiderParserListener();
        org.zaproxy.zap.spider.parser.SpiderResourceFound resourceFound1 =
                mock(org.zaproxy.zap.spider.parser.SpiderResourceFound.class);
        org.zaproxy.zap.spider.parser.SpiderResourceFound resourceFound2 =
                mock(org.zaproxy.zap.spider.parser.SpiderResourceFound.class);
        // When
        testSpiderParser.addSpiderParserListener(listener1);
        testSpiderParser.addSpiderParserListener(listener2);
        testSpiderParser.notifyListenersResourceFound(resourceFound1);
        testSpiderParser.notifyListenersResourceFound(resourceFound2);
        // Then
        assertThat(listener1.getResourcesFound(), contains(resourceFound1, resourceFound2));
        assertThat(listener2.getResourcesFound(), contains(resourceFound1, resourceFound2));
    }

    @Test
    void shouldNotNotifyRemovedListenerOfResourceFound() {
        // Given
        TestSpiderParser testSpiderParser = new TestSpiderParser();
        TestSpiderParserListener listener1 = createTestSpiderParserListener();
        testSpiderParser.addSpiderParserListener(listener1);
        TestSpiderParserListener listener2 = createTestSpiderParserListener();
        testSpiderParser.addSpiderParserListener(listener2);
        org.zaproxy.zap.spider.parser.SpiderResourceFound resourceFound1 =
                mock(org.zaproxy.zap.spider.parser.SpiderResourceFound.class);
        org.zaproxy.zap.spider.parser.SpiderResourceFound resourceFound2 =
                mock(org.zaproxy.zap.spider.parser.SpiderResourceFound.class);
        // When
        testSpiderParser.notifyListenersResourceFound(resourceFound1);
        testSpiderParser.removeSpiderParserListener(listener2);
        testSpiderParser.notifyListenersResourceFound(resourceFound2);
        // Then
        assertThat(listener1.getResourcesFound(), contains(resourceFound1, resourceFound2));
        assertThat(listener2.getResourcesFound(), contains(resourceFound1));
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldNotifyListenersOfUriResourceFound() {
        // Given
        TestSpiderParser testSpiderParser = new TestSpiderParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        testSpiderParser.addSpiderParserListener(listener);
        HttpMessage message = mock(HttpMessage.class);
        int depth = 42;
        String uri = "https://example.com";
        // When
        testSpiderParser.notifyListenersResourceFound(message, depth, uri);
        // Then
        assertThat(listener.getResourcesFound(), contains(uriResource(message, depth, uri)));
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldNotifyListenersOfPostResourceFound() {
        // Given
        TestSpiderParser testSpiderParser = new TestSpiderParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        testSpiderParser.addSpiderParserListener(listener);
        HttpMessage message = mock(HttpMessage.class);
        int depth = 42;
        String uri = "https://example.com";
        String body = "body";
        // When
        testSpiderParser.notifyListenersPostResourceFound(message, depth, uri, body);
        // Then
        assertThat(listener.getResourcesFound(), contains(postResource(message, depth, uri, body)));
    }

    @Test
    void shouldNotifyListenersOfProcessedUrl() {
        // Given
        TestSpiderParser testSpiderParser = new TestSpiderParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        testSpiderParser.addSpiderParserListener(listener);
        HttpMessage message = mock(HttpMessage.class);
        int depth = 42;
        String baseUrl = "https://example.com/";
        String localUrl = "/path/";
        String expectedUri = "https://example.com/path/";
        // When
        testSpiderParser.processURL(message, depth, localUrl, baseUrl);
        // Then
        assertThat(
                listener.getResourcesFound(),
                contains(uriResource(message, depth + 1, expectedUri)));
    }

    @Test
    void shouldNotNotifyListenersOfMalformedProcessedUrl() {
        // Given
        TestSpiderParser testSpiderParser = new TestSpiderParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        testSpiderParser.addSpiderParserListener(listener);
        HttpMessage message = mock(HttpMessage.class);
        int depth = 42;
        String baseUrl = "/";
        String localUrl = "/";
        // When
        testSpiderParser.processURL(message, depth, localUrl, baseUrl);
        // Then
        assertThat(listener.getResourcesFound(), is(empty()));
    }

    private static class TestSpiderParser extends SpiderParser {

        TestSpiderParser() {
            super(mock(org.zaproxy.zap.spider.SpiderParam.class));
        }

        @Override
        public boolean parseResource(HttpMessage message, Source source, int depth) {
            return true;
        }

        @Override
        public boolean canParseResource(
                HttpMessage message, String path, boolean wasAlreadyConsumed) {
            return true;
        }
    }
}

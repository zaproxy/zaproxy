/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import net.htmlparser.jericho.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

/** Unit test for {@link SpiderTextParser}. */
@SuppressWarnings("deprecation")
class SpiderTextParserUnitTest extends SpiderParserTestUtils {

    private static final String EMPTY_BODY = "";
    private static final String ROOT_PATH = "/";
    private static final int BASE_DEPTH = 0;

    private SpiderTextParser spiderParser;

    @BeforeEach
    void setup() {
        org.zaproxy.zap.spider.SpiderParam undefinedSpiderOptions =
                mock(org.zaproxy.zap.spider.SpiderParam.class);
        spiderParser = new org.zaproxy.zap.spider.parser.SpiderTextParser(undefinedSpiderOptions);
    }

    @Test
    void shouldFailToEvaluateAnUndefinedMessage() {
        // Given
        HttpMessage undefinedMessage = null;
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> spiderParser.canParseResource(undefinedMessage, ROOT_PATH, false));
    }

    @Test
    void shouldNotParseMessageIfAlreadyParsed() {
        // Given
        boolean parsed = true;
        // When
        boolean canParse = spiderParser.canParseResource(new HttpMessage(), ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(false)));
    }

    @Test
    void shouldNotParseNonTextResponse() {
        // Given
        HttpMessage message = createMessageWith("application/xyz", EMPTY_BODY);
        boolean parsed = false;
        // When
        boolean canParse = spiderParser.canParseResource(message, ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(false)));
    }

    @Test
    void shouldNotParseTextHtmlResponse() {
        // Given
        HttpMessage message = createMessageWith("text/html", EMPTY_BODY);
        boolean parsed = false;
        // When
        boolean canParse = spiderParser.canParseResource(message, ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(false)));
    }

    @Test
    void shouldParseTextResponse() {
        // Given
        HttpMessage messageHtmlResponse = createMessageWith(EMPTY_BODY);
        boolean parsed = false;
        // When
        boolean canParse = spiderParser.canParseResource(messageHtmlResponse, ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(true)));
    }

    @Test
    void shouldParseTextResponseEvenIfProvidedPathIsNull() {
        // Given
        HttpMessage messageHtmlResponse = createMessageWith(EMPTY_BODY);
        boolean parsed = false;
        // When
        boolean canParse = spiderParser.canParseResource(messageHtmlResponse, null, parsed);
        // Then
        assertThat(canParse, is(equalTo(true)));
    }

    @Test
    void shouldNotParseTextResponseIfAlreadyParsed() {
        // Given
        HttpMessage messageHtmlResponse = createMessageWith(EMPTY_BODY);
        boolean parsed = true;
        // When
        boolean canParse = spiderParser.canParseResource(messageHtmlResponse, ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(false)));
    }

    @Test
    void shouldFailToParseAnUndefinedMessage() {
        // Given
        HttpMessage undefinedMessage = null;
        Source source = createSource(createMessageWith(EMPTY_BODY));
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> spiderParser.parseResource(undefinedMessage, source, BASE_DEPTH));
    }

    @Test
    void shouldNeverConsiderCompletelyParsed() {
        // Given
        HttpMessage message = createMessageWith("Non Empty Body...");
        Source source = createSource(message);
        // When
        boolean completelyParsed = spiderParser.parseResource(message, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
    }

    @Test
    void shouldNotFindUrlsIfThereIsNone() {
        // Given
        TestSpiderParserListener listener = createTestSpiderParserListener();
        spiderParser.addSpiderParserListener(listener);
        HttpMessage message =
                createMessageWith(
                        body(
                                "Body with no HTTP/S URLs",
                                " ://example.com/ ",
                                "More text...  ftp://ftp.example.com/ ",
                                "Even more text... //noscheme.example.com "));
        Source source = createSource(message);
        // When
        boolean completelyParsed = spiderParser.parseResource(message, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(0)));
        assertThat(listener.getUrlsFound(), is(empty()));
    }

    @Test
    void shouldFindUrlsInCommentsWithoutElements() {
        // Given
        TestSpiderParserListener listener = createTestSpiderParserListener();
        spiderParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse =
                createMessageWith(
                        body(
                                "Body with HTTP/S URLs",
                                " - http://plaincomment.example.com some text not part of URL",
                                "- \"https://plaincomment.example.com/z.php?x=y\" more text not part of URL",
                                "- 'http://plaincomment.example.com/c.pl?x=y' even more text not part of URL",
                                "- <https://plaincomment.example.com/d.asp?x=y> ...",
                                "- http://plaincomment.example.com/e/e1/e2.html?x=y#stop fragment should be ignored",
                                "- (https://plaincomment.example.com/surrounded/with/parenthesis) parenthesis should not be included",
                                "- [https://plaincomment.example.com/surrounded/with/brackets] brackets should not be included",
                                "- {https://plaincomment.example.com/surrounded/with/curly/brackets} curly brackets should not be included",
                                "- mixed case URLs HtTpS://ExAmPlE.CoM/path/ should also be found"));
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                spiderParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(9)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://plaincomment.example.com/",
                        "https://plaincomment.example.com/z.php?x=y",
                        "http://plaincomment.example.com/c.pl?x=y",
                        "https://plaincomment.example.com/d.asp?x=y",
                        "http://plaincomment.example.com/e/e1/e2.html?x=y",
                        "https://plaincomment.example.com/surrounded/with/parenthesis",
                        "https://plaincomment.example.com/surrounded/with/brackets",
                        "https://plaincomment.example.com/surrounded/with/curly/brackets",
                        "https://example.com/path/"));
    }

    private static HttpMessage createMessageWith(String body) {
        return createMessageWith("text/xyz", body);
    }

    private static HttpMessage createMessageWith(String contentType, String body) {
        return createMessageWith("200 OK", contentType, body);
    }

    private static HttpMessage createMessageWith(
            String statusCodeMessage, String contentType, String body) {
        HttpMessage message = new HttpMessage();
        try {
            message.setRequestHeader("GET / HTTP/1.1\r\nHost: example.com\r\n");
            message.setResponseHeader(
                    "HTTP/1.1 "
                            + statusCodeMessage
                            + "\r\n"
                            + "Content-Type: "
                            + contentType
                            + "; charset=UTF-8\r\n"
                            + "Content-Length: "
                            + body.length());
            message.setResponseBody(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    private static String body(String... strings) {
        if (strings == null || strings.length == 0) {
            return "";
        }
        StringBuilder strBuilder = new StringBuilder(strings.length * 25);
        for (String string : strings) {
            if (strBuilder.length() > 0) {
                strBuilder.append("\n");
            }
            strBuilder.append(string);
        }
        return strBuilder.toString();
    }
}

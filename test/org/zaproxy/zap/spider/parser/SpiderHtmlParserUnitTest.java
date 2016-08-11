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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.junit.BeforeClass;
import org.junit.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.spider.SpiderParam;

import net.htmlparser.jericho.Source;

/**
 * Unit test for {@link SpiderHtmlParser}.
 */
public class SpiderHtmlParserUnitTest extends SpiderParserTestUtils {

    private static final String ROOT_PATH = "/";
    private static final int BASE_DEPTH = 0;

    private static final Path BASE_DIR_HTML_FILES = Paths.get("test/resources/org/zaproxy/zap/spider/parser");

    @BeforeClass
    public static void suppressLogging() {
        Logger.getRootLogger().addAppender(new NullAppender());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateParserWithUndefinedSpiderOptions() {
        // Given
        SpiderParam undefinedSpiderOptions = null;
        // When
        new SpiderHtmlParser(undefinedSpiderOptions);
        // Then = IllegalArgumentException
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailToEvaluateAnUndefinedMessage() {
        // Given
        HttpMessage undefinedMessage = null;
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        // When
        htmlParser.canParseResource(undefinedMessage, ROOT_PATH, false);
        // Then = NullPointerException
    }

    @Test
    public void shouldParseHtmlResponse() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        HttpMessage messageHtmlResponse = createMessageWith("NoURLsSpiderHtmlParser.html");
        boolean parsed = false;
        // When
        boolean canParse = htmlParser.canParseResource(messageHtmlResponse, ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(true)));
    }

    @Test
    public void shouldParseHtmlResponseEvenIfProvidedPathIsNull() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        HttpMessage messageHtmlResponse = createMessageWith("NoURLsSpiderHtmlParser.html");
        boolean parsed = false;
        // When
        boolean canParse = htmlParser.canParseResource(messageHtmlResponse, null, parsed);
        // Then
        assertThat(canParse, is(equalTo(true)));
    }

    @Test
    public void shouldNotParseHtmlResponseIfAlreadyParsed() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        HttpMessage messageHtmlResponse = createMessageWith("NoURLsSpiderHtmlParser.html");
        boolean parsed = true;
        // When
        boolean canParse = htmlParser.canParseResource(messageHtmlResponse, ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(false)));
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailToParseAnUndefinedMessage() {
        // Given
        HttpMessage undefinedMessage = null;
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        Source source = createSource(createMessageWith("NoURLsSpiderHtmlParser.html"));
        // When
        htmlParser.parseResource(undefinedMessage, source, BASE_DEPTH);
        // Then = NullPointerException
    }

    @Test
    public void shouldParseMessageEvenWithoutSource() {
        // Given
        Source source = null;
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        HttpMessage messageHtmlResponse = createMessageWith("NoURLsSpiderHtmlParser.html");
        // When
        htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then = No exception
    }

    @Test
    public void shouldNeverConsiderCompletelyParsed() {
        // Given
        Source source = null;
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        HttpMessage messageHtmlResponse = createMessageWith("NoURLsSpiderHtmlParser.html");
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
    }

    @Test
    public void shouldFindUrlsInAElements() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("AElementsSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(7)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://a.example.com/base/scheme",
                        "http://a.example.com:8000/b",
                        "https://a.example.com/c?a=b",
                        "http://example.com/sample/a/relative",
                        "http://example.com/sample/",
                        "http://example.com/a/absolute",
                        "ftp://a.example.com/"));
    }

    @Test
    public void shouldUseMessageUriIfNoBaseElement() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("NoBaseWithAElementSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.com/relative/no/base"));
    }

    @Test
    public void shouldIgnoreBaseAndUseMessageUriIfBaseElementDoesNotHaveHref() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("BaseWithoutHrefAElementSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.com/relative/no/base"));
    }

    @Test
    public void shouldIgnoreBaseAndUseMessageUriIfBaseElementHaveEmptyHref() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("BaseWithEmptyHrefAElementSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.com/relative/no/base"));
    }

    @Test
    public void shouldFindUrlsInAreaElements() throws Exception {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("AreaElementsSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(7)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://area.example.com/base/scheme",
                        "http://area.example.com:8000/b",
                        "https://area.example.com/c?a=b",
                        "http://example.com/sample/area/relative",
                        "http://example.com/sample/",
                        "http://example.com/area/absolute",
                        "ftp://area.example.com/"));
    }

    @Test
    public void shouldFindUrlsInFrameElements() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("FrameElementsSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(7)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://frame.example.com/base/scheme",
                        "http://frame.example.com:8000/b",
                        "https://frame.example.com/c?a=b",
                        "http://example.com/sample/frame/relative",
                        "http://example.com/sample/",
                        "http://example.com/frame/absolute",
                        "ftp://frame.example.com/"));
    }

    @Test
    public void shouldFindUrlsInIFrameElements() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("IFrameElementsSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(7)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://iframe.example.com/base/scheme",
                        "http://iframe.example.com:8000/b",
                        "https://iframe.example.com/c?a=b",
                        "http://example.com/sample/iframe/relative",
                        "http://example.com/sample/",
                        "http://example.com/iframe/absolute",
                        "ftp://iframe.example.com/"));
    }

    @Test
    public void shouldFindUrlsInLinkElements() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("LinkElementsSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(7)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://link.example.com/base/scheme",
                        "http://link.example.com:8000/b",
                        "https://link.example.com/c?a=b",
                        "http://example.com/sample/link/relative",
                        "http://example.com/sample/",
                        "http://example.com/link/absolute",
                        "ftp://link.example.com/"));
    }

    @Test
    public void shouldFindUrlsInScriptElements() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("ScriptElementsSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(7)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://script.example.com/base/scheme",
                        "http://script.example.com:8000/b",
                        "https://script.example.com/c?a=b",
                        "http://example.com/sample/script/relative",
                        "http://example.com/sample/",
                        "http://example.com/script/absolute",
                        "ftp://script.example.com/"));
    }

    @Test
    public void shouldFindUrlsInImgElements() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("ImgElementsSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(7)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://img.example.com/base/scheme",
                        "http://img.example.com:8000/b",
                        "https://img.example.com/c?a=b",
                        "http://example.com/sample/img/relative",
                        "http://example.com/sample/",
                        "http://example.com/img/absolute",
                        "ftp://img.example.com/"));
    }

    @Test
    public void shouldFindUrlsInMetaElements() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("MetaElementsSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(10)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://meta.example.com:8443/refresh/base/scheme",
                        "https://meta.example.com/refresh",
                        "http://example.com/sample/meta/refresh/relative",
                        "http://example.com/meta/refresh/absolute",
                        "ftp://meta.example.com/refresh",
                        "http://meta.example.com:8080/location/base/scheme",
                        "https://meta.example.com/location",
                        "http://example.com/sample/meta/location/relative",
                        "http://example.com/meta/location/absolute",
                        "ftp://meta.example.com/location"));
    }

    @Test
    public void shouldFindUrlsInCommentsWithElements() {
        // AKA shouldNotFindPlainUrlsInCommentsWithElements
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("CommentWithElementsSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(9)));
        System.err.println(listener.getUrlsFound());
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://a.example.com/",
                        "http://area.example.com/",
                        "http://frame.example.com/",
                        "http://iframe.example.com/",
                        "http://img.example.com/",
                        "http://link.example.com/",
                        "http://meta.example.com/refresh/",
                        "http://meta.example.com/location/",
                        "http://script.example.com/"));
    }

    @Test
    public void shouldNotFindUrlsInCommentsWithElementsIfNotEnabledToParseComments() {
        // Given
        SpiderParam spiderOptions = createSpiderParamWithConfig();
        spiderOptions.setParseComments(false);
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(spiderOptions);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("CommentWithElementsSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(0)));
        assertThat(listener.getUrlsFound(), is(empty()));
    }

    @Test
    public void shouldFindUrlsInCommentsWithoutElements() {
        // Given
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(new SpiderParam());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("CommentWithoutElementsSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(7)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://plaincomment.example.com/",
                        "http://plaincomment.example.com/z.php?x=y",
                        "http://plaincomment.example.com/c.pl?x=y",
                        "https://plaincomment.example.com/d.asp?x=y",
                        "https://plaincomment.example.com/e/e1/e2.html?x=y",
                        "http://plaincomment.example.com/variant1",
                        "http://plaincomment.example.com/variant2"));
    }

    @Test
    public void shouldNotFindUrlsInCommentsWithoutElementsIfNotEnabledToParseComments() {
        // Given
        SpiderParam spiderOptions = createSpiderParamWithConfig();
        spiderOptions.setParseComments(false);
        SpiderHtmlParser htmlParser = new SpiderHtmlParser(spiderOptions);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("CommentWithoutElementsSpiderHtmlParser.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(0)));
        assertThat(listener.getUrlsFound(), is(empty()));
    }

    private static HttpMessage createMessageWith(String filename) {
        HttpMessage message = new HttpMessage();
        try {
            String fileContents = readFile(BASE_DIR_HTML_FILES.resolve(filename));
            message.setRequestHeader("GET / HTTP/1.1\r\nHost: example.com\r\n");
            message.setResponseHeader(
                    "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html; charset=UTF-8\r\n" + "Content-Length: "
                            + fileContents.length());
            message.setResponseBody(fileContents);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return message;
    }
}

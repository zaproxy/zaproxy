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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.htmlparser.jericho.Source;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.DefaultValueGenerator;
import org.zaproxy.zap.model.ValueGenerator;
import org.zaproxy.zap.utils.Pair;

/** Unit test for {@link SpiderHtmlFormParser}. */
@SuppressWarnings("deprecation")
class SpiderHtmlFormParserUnitTest extends SpiderParserTestUtils {

    private static final String FORM_METHOD_TOKEN = "%%METHOD%%";
    private static final String FORM_ACTION_TOKEN = "%%ACTION%%";
    private static final String BASE_HTML_TOKEN = "%%BASE_HTML%%";

    private static final String ROOT_PATH = "/";
    private static final int BASE_DEPTH = 0;

    private static final Path BASE_DIR_HTML_FILES =
            getResourcePath("htmlform", SpiderHtmlFormParserUnitTest.class);

    @Test
    void shouldFailToCreateParserWithUndefinedSpiderOptions() {
        // Given
        org.zaproxy.zap.spider.SpiderParam undefinedSpiderOptions = null;
        // When / Then
        assertThrows(
                NullPointerException.class,
                () ->
                        new org.zaproxy.zap.spider.parser.SpiderHtmlFormParser(
                                undefinedSpiderOptions, new DefaultValueGenerator()));
    }

    @Test
    void shouldFailToEvaluateAnUndefinedMessage() {
        // Given
        HttpMessage undefinedMessage = null;
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> htmlParser.canParseResource(undefinedMessage, ROOT_PATH, false));
    }

    @Test
    void shouldNotParseMessageIfAlreadyParsed() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        boolean parsed = true;
        // When
        boolean canParse = htmlParser.canParseResource(new HttpMessage(), ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(false)));
    }

    @Test
    void shouldNotParseNonHtmlResponse() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        boolean parsed = false;
        // When
        boolean canParse = htmlParser.canParseResource(new HttpMessage(), ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(false)));
    }

    @Test
    void shouldParseHtmlResponse() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        HttpMessage messageHtmlResponse = createMessageWith("NoForms.html");
        boolean parsed = false;
        // When
        boolean canParse = htmlParser.canParseResource(messageHtmlResponse, ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(true)));
    }

    @Test
    void shouldParseHtmlResponseEvenIfProvidedPathIsNull() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        HttpMessage messageHtmlResponse = createMessageWith("NoForms.html");
        boolean parsed = false;
        // When
        boolean canParse = htmlParser.canParseResource(messageHtmlResponse, null, parsed);
        // Then
        assertThat(canParse, is(equalTo(true)));
    }

    @Test
    void shouldNotParseHtmlResponseIfAlreadyParsed() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        HttpMessage messageHtmlResponse = createMessageWith("NoForms.html");
        boolean parsed = true;
        // When
        boolean canParse = htmlParser.canParseResource(messageHtmlResponse, ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(false)));
    }

    @Test
    void shouldFailToParseAnUndefinedMessage() {
        // Given
        HttpMessage undefinedMessage = null;
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        Source source = createSource(createMessageWith("NoForms.html"));
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> htmlParser.parseResource(undefinedMessage, source, BASE_DEPTH));
    }

    @Test
    void shouldNotParseMessageIfFormProcessingIsDisabled() {
        // Given
        org.zaproxy.zap.spider.SpiderParam spiderOptions = createSpiderParamWithConfig();
        spiderOptions.setProcessForm(false);
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                new org.zaproxy.zap.spider.parser.SpiderHtmlFormParser(
                        spiderOptions, new DefaultValueGenerator());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("PostGetForms.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(0)));
    }

    @Test
    void shouldParseMessageEvenWithoutSource() {
        // Given
        Source source = null;
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        HttpMessage messageHtmlResponse = createMessageWith("NoForms.html");
        // When / Then
        assertDoesNotThrow(() -> htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH));
    }

    @Test
    void shouldNeverConsiderCompletelyParsed() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        HttpMessage messageHtmlResponse = createMessageWith("NoForms.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
    }

    @Test
    void shouldParseSingleGetForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GET", "Form.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    void shouldParseMultipleGetForms() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GET", "Forms.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(2)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://example.org/form1?field1=Text+1&field2=Text+2&submit=Submit",
                        "http://example.org/form2?a=x&b=y&c=z"));
    }

    @Test
    void shouldParseGetFormWithMultipleSubmitFields() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GET", "FormMultipleSubmitFields.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(5)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://example.org/?field1=Text+1&field2=Text+2&submit1=Submit+1",
                        "http://example.org/?field1=Text+1&field2=Text+2&submit2=Submit+2",
                        "http://example.org/?field1=Text+1&field2=Text+2&submit3=Submit+3",
                        "http://example.org/?field1=Text+1&field2=Text+2&submit=Submit+4",
                        "http://example.org/?field1=Text+1&field2=Text+2&submit=Submit+5"));
    }

    @Test
    void shouldParseSinglePostForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("POST", "Form.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg,
                                1,
                                "http://example.org/",
                                "field1=Text+1&field2=Text+2&submit=Submit")));
    }

    @Test
    void shouldParseMultiplePostForms() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("POST", "Forms.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(2)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg,
                                1,
                                "http://example.org/form1",
                                "field1=Text+1&field2=Text+2&submit=Submit"),
                        postResource(msg, 1, "http://example.org/form2", "a=x&b=y&c=z")));
    }

    @Test
    void shouldParsePostFormWithMultipleSubmitFields() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("POST", "FormMultipleSubmitFields.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(5)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg,
                                1,
                                "http://example.org/",
                                "field1=Text+1&field2=Text+2&submit1=Submit+1"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/",
                                "field1=Text+1&field2=Text+2&submit2=Submit+2"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/",
                                "field1=Text+1&field2=Text+2&submit3=Submit+3"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/",
                                "field1=Text+1&field2=Text+2&submit=Submit+4"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/",
                                "field1=Text+1&field2=Text+2&submit=Submit+5")));
    }

    @Test
    void shouldParsePostAndGetForms() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("PostGetForms.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(6)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg,
                                1,
                                "http://example.org/form1",
                                "field1=Text+1&field2=Text+2&submit=Submit"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/form1",
                                "field1=Text+1&field2=Text+2&submit=Submit+2"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/form1",
                                "field1=Text+1&field2=Text+2&submit3=Submit+3"),
                        uriResource(msg, 1, "http://example.org/form2?a=x&b=y&c=z"),
                        uriResource(msg, 1, "http://example.org/form2?a=x&b=y&submit=Submit+2"),
                        uriResource(msg, 1, "http://example.org/form2?a=x&b=y&submit3=Submit+3")));
    }

    @Test
    void shouldNotParsePostFormIfPostFormProcessingIsDisabled() {
        // Given
        org.zaproxy.zap.spider.SpiderParam spiderOptions = createSpiderParamWithConfig();
        spiderOptions.setProcessForm(true);
        spiderOptions.setPostForm(false);
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                new org.zaproxy.zap.spider.parser.SpiderHtmlFormParser(
                        spiderOptions, new DefaultValueGenerator());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("POST", "Form.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(0)));
    }

    @Test
    void shouldParseNonPostFormIfPostFormProcessingIsDisabled() {
        // Given
        org.zaproxy.zap.spider.SpiderParam spiderOptions = createSpiderParamWithConfig();
        spiderOptions.setProcessForm(true);
        spiderOptions.setPostForm(false);
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                new org.zaproxy.zap.spider.parser.SpiderHtmlFormParser(
                        spiderOptions, new DefaultValueGenerator());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GET", "Form.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    void shouldParseFormAsGetIfNeitherGetNorPostForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("NonGetPostForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    void shouldParseFormAsGetIfFormHasNoMethod() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("NoMethodForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    void shouldParseFormAsGetIfFormHasNoMethodEvenIfPostFormProcessingIsDisabled() {
        // Given
        org.zaproxy.zap.spider.SpiderParam spiderOptions = createSpiderParamWithConfig();
        spiderOptions.setProcessForm(true);
        spiderOptions.setPostForm(false);
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                new org.zaproxy.zap.spider.parser.SpiderHtmlFormParser(
                        spiderOptions, new DefaultValueGenerator());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("NoMethodForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    void shouldParseFormAsGetIfFormHasEmptyMethod() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("EmptyMethodForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    void shouldUseMessageUrlAsActionIfFormHasNoAction() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("NoActionForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.com/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    void shouldParseGetFormWithoutSubmitField() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GET", "FormNoSubmitField.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.org/?field1=Text+1&field2=Text+2"));
    }

    @Test
    void shouldParsePostFormWithoutSubmitField() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("POST", "FormNoSubmitField.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg, 1, "http://example.org/", "field1=Text+1&field2=Text+2")));
    }

    @Test
    void shouldRemoveFragmentFromActionWhenParsingGetForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GET", "FormActionWithFragment.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    void shouldRemoveFragmentFromActionWhenParsingPostForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("POST", "FormActionWithFragment.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                messageHtmlResponse,
                                1,
                                "http://example.org/",
                                "field1=Text+1&field2=Text+2&submit=Submit")));
    }

    @Test
    void shouldRemoveFragmentFromActionWhenParsingNeitherGetNorPostForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse =
                createMessageWith("NeitherGetNorPost", "FormActionWithFragment.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    void shouldAppendToEmptyQueryActionParametersWhenParsingGetForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GetFormActionWithEmptyQuery.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    void shouldAppendToQueryActionParametersWhenParsingGetForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GetFormActionWithQuery.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.org/?a=b&c=d&field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    void shouldAppendToQueryActionParametersTerminatedWithAmpersandWhenParsingGetForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GetFormActionWithQueryAmpersand.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.org/?a=b&field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    void shouldUseBaseHtmlUrlWhenParsingGetForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg =
                createMessageWith(
                        "GET", "FormWithHtmlBase.html", "search", "http://base.example.com/");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://base.example.com/search?q=Search&submit=Submit"));
    }

    @Test
    void shouldUseAbsolutePathBaseHtmlUrlWhenParsingGetFormWithRelativeAction() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg =
                createMessageWith(
                        "GET",
                        "FormWithHtmlBase.html",
                        "action/relative",
                        "/base/absolute/path/",
                        "/a/b.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://example.com/base/absolute/path/action/relative?q=Search&submit=Submit"));
    }

    @Test
    void shouldIgnoreAbsolutePathBaseHtmlUrlWhenParsingGetFormWithAbsoluteAction() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg =
                createMessageWith(
                        "GET",
                        "FormWithHtmlBase.html",
                        "/action/absolute",
                        "/base/absolute/path/",
                        "/a/b.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.com/action/absolute?q=Search&submit=Submit"));
    }

    @Test
    void shouldUseRelativePathBaseHtmlUrlWhenParsingGetFormWithRelativeAction() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg =
                createMessageWith(
                        "GET",
                        "FormWithHtmlBase.html",
                        "action/relative",
                        "base/relative/path/",
                        "/a/b.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://example.com/a/base/relative/path/action/relative?q=Search&submit=Submit"));
    }

    @Test
    void shouldUseButtonFormActionIfPresent() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("GET", "FormWithFormactionButtons.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(10)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://example.org/formaction1?field1=field1&field2=field2",
                        "http://example.org/form2?field1=field1&field2=field2",
                        "http://example.org/formaction2?field1=field1&field2=field2",
                        "http://example.org/emptyform",
                        "http://example.org/withchildbutton",
                        "http://example.org/withoutchildbutton",
                        "http://actionnoreset.com/",
                        "http://actionnobutton.com/",
                        "http://i.override.to.be.overridden.com/",
                        "http://not.overridden.by.nested.buttons.com/"));
    }

    @Test
    void shouldUseButtonFormMethodIfPresentGET() {
        // Given
        org.zaproxy.zap.spider.SpiderParam spiderOptions = createSpiderParamWithConfig();
        // Disable POST handling, we need just to retrieve the forms with GET methods
        spiderOptions.setPostForm(false);
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                new org.zaproxy.zap.spider.parser.SpiderHtmlFormParser(
                        spiderOptions, new DefaultValueGenerator());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("GET", "OverriddenMethodByButtonForms.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(4)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://example.org/form1?field1=Text+1&field2=Text+2",
                        "http://ignore.button.org/form3?a=x&b=y&c=z",
                        "http://ignore.reset.org/form4?a=x&b=y",
                        "http://example.org/form6?a=x&b=y"));
    }

    @Test
    void shouldUseButtonFormMethodIfPresentPOST() {
        // Given
        org.zaproxy.zap.spider.SpiderParam spiderOptions = createSpiderParamWithConfig();
        // Ensure POST handling is enabled, now both GET and POST methods should be identified by
        // our code
        spiderOptions.setPostForm(true);
        // Disable POST handling, we need just to retrieve the forms with GET methods
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                new org.zaproxy.zap.spider.parser.SpiderHtmlFormParser(
                        spiderOptions, new DefaultValueGenerator());
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("GET", "OverriddenMethodByButtonForms.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(7)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://example.org/form1?field1=Text+1&field2=Text+2",
                        "http://example.org/form2",
                        "http://ignore.button.org/form3?a=x&b=y&c=z",
                        "http://ignore.reset.org/form4?a=x&b=y",
                        "http://outside.org/form5",
                        "http://example.org/form6?a=x&b=y",
                        "http://example.org/form7"));
    }

    @Test
    void shouldIgnoreRelativePathBaseHtmlUrlWhenParsingGetFormWithAbsoluteAction() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg =
                createMessageWith(
                        "GET",
                        "FormWithHtmlBase.html",
                        "/action/absolute",
                        "base/relative/path/",
                        "/a/b.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.com/action/absolute?q=Search&submit=Submit"));
    }

    @Test
    void shouldIgnoreBaseHtmlIfEmptyHrefWhenParsingGetForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("GET", "FormWithHtmlBase.html", "search", "");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.com/search?q=Search&submit=Submit"));
    }

    @Test
    void shouldIgnoreBaseHtmlWithNoHrefWhenParsingGetForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("GET", "FormWithHtmlBaseWithoutHref.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("http://example.com/search?q=Search&submit=Submit"));
    }

    @Test
    void shouldIgnoreBaseHtmlIfActionIsAbsoluteWhenParsingGetForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg =
                createMessageWith(
                        "GET",
                        "FormWithHtmlBase.html",
                        "https://example.com/search",
                        "http://base.example.com/");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getUrlsFound(),
                contains("https://example.com/search?q=Search&submit=Submit"));
    }

    @Test
    void shouldUseBaseHtmlUrlWhenParsingPostForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg =
                createMessageWith(
                        "POST", "FormWithHtmlBase.html", "search", "http://base.example.com/");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg,
                                1,
                                "http://base.example.com/search",
                                "q=Search&submit=Submit")));
    }

    @Test
    void shouldUseAbsolutePathBaseHtmlUrlWhenParsingPostFormWithRelativeAction() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg =
                createMessageWith(
                        "POST",
                        "FormWithHtmlBase.html",
                        "action/relative",
                        "/base/absolute/path/",
                        "/a/b.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg,
                                1,
                                "http://example.com/base/absolute/path/action/relative",
                                "q=Search&submit=Submit")));
    }

    @Test
    void shouldIgnoreAbsolutePathBaseHtmlUrlWhenParsingPostFormWithAbsoluteAction() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg =
                createMessageWith(
                        "POST",
                        "FormWithHtmlBase.html",
                        "/action/absolute",
                        "/base/absolute/path/",
                        "/a/b.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg,
                                1,
                                "http://example.com/action/absolute",
                                "q=Search&submit=Submit")));
    }

    @Test
    void shouldUseRelativePathBaseHtmlUrlWhenParsingPostFormWithRelativeAction() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg =
                createMessageWith(
                        "POST",
                        "FormWithHtmlBase.html",
                        "action/relative",
                        "base/relative/path/",
                        "/a/b.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg,
                                1,
                                "http://example.com/a/base/relative/path/action/relative",
                                "q=Search&submit=Submit")));
    }

    @Test
    void shouldIgnoreRelativePathBaseHtmlUrlWhenParsingPostFormWithAbsoluteAction() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg =
                createMessageWith(
                        "POST",
                        "FormWithHtmlBase.html",
                        "/action/absolute",
                        "base/relative/path/",
                        "/a/b.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg,
                                1,
                                "http://example.com/action/absolute",
                                "q=Search&submit=Submit")));
    }

    @Test
    void shouldIgnoreBaseHtmlIfEmptyHrefWhenParsingPostForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("POST", "FormWithHtmlBase.html", "search", "");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg, 1, "http://example.com/search", "q=Search&submit=Submit")));
    }

    @Test
    void shouldIgnoreBaseHtmlWithNoHrefWhenParsingPostForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("POST", "FormWithHtmlBaseWithoutHref.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg, 1, "http://example.com/search", "q=Search&submit=Submit")));
    }

    @Test
    void shouldIgnoreBaseHtmlIfActionIsAbsoluteWhenParsingPostForm() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg =
                createMessageWith(
                        "POST",
                        "FormWithHtmlBase.html",
                        "https://example.com/search",
                        "http://base.example.com/");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg, 1, "https://example.com/search", "q=Search&submit=Submit")));
    }

    @Test
    void shouldSetValuesToFieldsWithNoValueWhenParsingGetForm() {
        // Given
        DefaultValueGenerator valueGenerator = new DefaultValueGenerator();
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser(valueGenerator);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        Date date = new Date(1474370354555L);
        valueGenerator.setDefaultDate(date);
        HttpMessage msg = createMessageWith("GET", "FormNoDefaultValues.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(8)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://example.org/?_file=test_file.txt&_hidden&_no-type=ZAP&_password=ZAP&_text=ZAP&submit=Submit",
                        "http://example.org/html5/number?_number=1&_number-max=2&_number-min=1&submit=Submit",
                        "http://example.org/html5/range?_range=1&_range-max=4&_range-min=3&submit=Submit",
                        "http://example.org/html5/misc?_color=%23ffffff&_email=foo-bar%40example.com&_tel=9999999999&_url=http%3A%2F%2Fwww.example.com&submit=Submit",
                        "http://example.org/unknown?_unknown&submit=Submit",
                        "http://example.org/selects?_select-one-option=first-option&_select-selected-option=selected-option&_select-two-options=last-option&submit=Submit",
                        "http://example.org/checkbox?_checkbox=first-checkbox&submit=Submit",
                        "http://example.org/html5/date-time?"
                                + params(
                                        param("_date", formattedDate("yyyy-MM-dd", date)),
                                        param(
                                                "_datetime",
                                                formattedDate("yyyy-MM-dd'T'HH:mm:ss'Z'", date)),
                                        param(
                                                "_datetime-local",
                                                formattedDate("yyyy-MM-dd'T'HH:mm:ss", date)),
                                        param("_month", formattedDate("yyyy-MM", date)),
                                        param("_time", formattedDate("HH:mm:ss", date)),
                                        param("_week", formattedDate("yyyy-'W'ww", date)),
                                        param("submit", "Submit"))));
    }

    @Test
    void shouldSetValuesToFieldsWithNoValueWhenParsingPostForm() {
        // Given
        DefaultValueGenerator valueGenerator = new DefaultValueGenerator();
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser(valueGenerator);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        Date date = new Date(1474370354555L);
        valueGenerator.setDefaultDate(date);
        HttpMessage msg = createMessageWith("POST", "FormNoDefaultValues.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(8)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg,
                                1,
                                "http://example.org/",
                                "_hidden=&_no-type=ZAP&_text=ZAP&_password=ZAP&_file=test_file.txt&submit=Submit"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/html5/number",
                                "_number=1&_number-min=1&_number-max=2&submit=Submit"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/html5/range",
                                "_range=1&_range-min=3&_range-max=4&submit=Submit"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/html5/misc",
                                "_url=http%3A%2F%2Fwww.example.com&_email=foo-bar%40example.com&_color=%23ffffff&_tel=9999999999&submit=Submit"),
                        postResource(
                                msg, 1, "http://example.org/unknown", "_unknown=&submit=Submit"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/selects",
                                "_select-one-option=first-option&_select-two-options=last-option&_select-selected-option=selected-option&submit=Submit"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/checkbox",
                                "_checkbox=first-checkbox&submit=Submit"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/html5/date-time",
                                params(
                                        param(
                                                "_datetime",
                                                formattedDate("yyyy-MM-dd'T'HH:mm:ss'Z'", date)),
                                        param(
                                                "_datetime-local",
                                                formattedDate("yyyy-MM-dd'T'HH:mm:ss", date)),
                                        param("_date", formattedDate("yyyy-MM-dd", date)),
                                        param("_time", formattedDate("HH:mm:ss", date)),
                                        param("_month", formattedDate("yyyy-MM", date)),
                                        param("_week", formattedDate("yyyy-'W'ww", date)),
                                        param("submit", "Submit")))));
    }

    @Test
    void shouldProvidedCorrectFormDataToValueGenerator() {
        // Given
        TestValueGenerator valueGenerator = new TestValueGenerator();
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser(valueGenerator);
        HttpMessage msg = createMessageWith("FormsForValueGenerator.html");
        Source source = createSource(msg);
        int fieldIndex = 0;
        // When
        htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(valueGenerator.getFields(), hasSize(9));
        assertThat(
                valueGenerator.getFields().get(fieldIndex),
                is(
                        equalTo(
                                formField(
                                        "http://example.com/",
                                        "http://example.org/post",
                                        "field1",
                                        "preDefValue1",
                                        list(""),
                                        attributes(
                                                attribute("name", "field1"),
                                                attribute("value", "preDefValue1"),
                                                attribute("type", "hidden"),
                                                attribute("id", "id1"),
                                                attribute("Control Type", "HIDDEN")),
                                        attributes(
                                                attribute("action", "http://example.org/post"),
                                                attribute("method", "POST"),
                                                attribute("atta", "valueA"))))));
        fieldIndex++;
        assertThat(
                valueGenerator.getFields().get(fieldIndex),
                is(
                        equalTo(
                                formField(
                                        "http://example.com/",
                                        "http://example.org/post",
                                        "field2",
                                        "preDefValue2",
                                        list(""),
                                        attributes(
                                                attribute("name", "field2"),
                                                attribute("value", "preDefValue2"),
                                                attribute("id", "id2"),
                                                attribute("att1", "value1"),
                                                attribute("Control Type", "TEXT")),
                                        attributes(
                                                attribute("action", "http://example.org/post"),
                                                attribute("method", "POST"),
                                                attribute("atta", "valueA"))))));
        fieldIndex++;
        assertThat(
                valueGenerator.getFields().get(fieldIndex),
                is(
                        equalTo(
                                formField(
                                        "http://example.com/",
                                        "http://example.org/post",
                                        "field3",
                                        "preDefValue3",
                                        list(""),
                                        attributes(
                                                attribute("name", "field3"),
                                                attribute("value", "preDefValue3"),
                                                attribute("type", "text"),
                                                attribute("Control Type", "TEXT")),
                                        attributes(
                                                attribute("action", "http://example.org/post"),
                                                attribute("method", "POST"),
                                                attribute("atta", "valueA"))))));
        fieldIndex++;
        assertThat(
                valueGenerator.getFields().get(fieldIndex),
                is(
                        equalTo(
                                formField(
                                        "http://example.com/",
                                        "http://example.org/post",
                                        "gender",
                                        "m",
                                        list(("m,f")),
                                        attributes(
                                                attribute("name", "gender"),
                                                attribute("type", "radio"),
                                                attribute("value", "m"),
                                                attribute("id", "male"),
                                                attribute("Control Type", "RADIO")),
                                        attributes(
                                                attribute("action", "http://example.org/post"),
                                                attribute("method", "POST"),
                                                attribute("atta", "valueA"))))));
        fieldIndex++;
        assertThat(
                valueGenerator.getFields().get(fieldIndex),
                is(
                        equalTo(
                                formField(
                                        "http://example.com/",
                                        "http://example.org/post",
                                        "submit",
                                        "Submit",
                                        list(""),
                                        attributes(
                                                attribute("name", "submit"),
                                                attribute("type", "submit"),
                                                attribute("value", "Submit"),
                                                attribute("Control Type", "SUBMIT")),
                                        attributes(
                                                attribute("action", "http://example.org/post"),
                                                attribute("method", "POST"),
                                                attribute("atta", "valueA"))))));
        fieldIndex++;
        assertThat(
                valueGenerator.getFields().get(fieldIndex),
                is(
                        equalTo(
                                formField(
                                        "http://example.com/",
                                        "http://example.org/get",
                                        "field1",
                                        "",
                                        list(""),
                                        attributes(
                                                attribute("name", "field1"),
                                                attribute("type", "hidden"),
                                                attribute("id", "id1"),
                                                attribute("Control Type", "HIDDEN")),
                                        attributes(
                                                attribute("action", "http://example.org/get"),
                                                attribute("method", "GET"),
                                                attribute("att1", "value1"),
                                                attribute("att2", "value2"))))));
        fieldIndex++;
        assertThat(
                valueGenerator.getFields().get(fieldIndex),
                is(
                        equalTo(
                                formField(
                                        "http://example.com/",
                                        "http://example.org/get",
                                        "field2",
                                        "",
                                        list(""),
                                        attributes(
                                                attribute("name", "field2"),
                                                attribute("id", "id2"),
                                                attribute("att1", "value1"),
                                                attribute("Control Type", "TEXT")),
                                        attributes(
                                                attribute("action", "http://example.org/get"),
                                                attribute("method", "GET"),
                                                attribute("att1", "value1"),
                                                attribute("att2", "value2"))))));
        fieldIndex++;
        assertThat(
                valueGenerator.getFields().get(fieldIndex),
                is(
                        equalTo(
                                formField(
                                        "http://example.com/",
                                        "http://example.org/get",
                                        "field3",
                                        "",
                                        list(""),
                                        attributes(
                                                attribute("name", "field3"),
                                                attribute("type", "text"),
                                                attribute("Control Type", "TEXT")),
                                        attributes(
                                                attribute("action", "http://example.org/get"),
                                                attribute("method", "GET"),
                                                attribute("att1", "value1"),
                                                attribute("att2", "value2"))))));
        fieldIndex++;
        assertThat(
                valueGenerator.getFields().get(fieldIndex),
                is(
                        equalTo(
                                formField(
                                        "http://example.com/",
                                        "http://example.org/get",
                                        "submit",
                                        "Submit",
                                        list(""),
                                        attributes(
                                                attribute("name", "submit"),
                                                attribute("type", "submit"),
                                                attribute("value", "Submit"),
                                                attribute("Control Type", "SUBMIT")),
                                        attributes(
                                                attribute("action", "http://example.org/get"),
                                                attribute("method", "GET"),
                                                attribute("att1", "value1"),
                                                attribute("att2", "value2"))))));
    }

    @Test
    void shouldParseGetFormAndIncludeRelatedInputsWithFormAttribute() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse =
                createMessageWith("GET", "FormAndInputsWithFormAttributes.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed =
                htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(3)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://example.org/?field1=Field1&field2=Field2&field3=Field3&field4=Field4&submit=Submit1",
                        "http://example.org/?field1=Field1&field2=Field2&field3=Field3&field4=Field4&submit=Submit2",
                        "http://example.org/?field1=Field1&field2=Field2&field3=Field3&field4=Field4&submit=Submit3"));
    }

    @Test
    void shouldParsePostFormAndIncludeRelatedInputsWithFormAttribute() {
        // Given
        org.zaproxy.zap.spider.parser.SpiderHtmlFormParser htmlParser =
                createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("POST", "FormAndInputsWithFormAttributes.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(3)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(
                                msg,
                                1,
                                "http://example.org/",
                                "field1=Field1&submit=Submit1&field2=Field2&field3=Field3&field4=Field4"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/",
                                "field1=Field1&submit=Submit2&field2=Field2&field3=Field3&field4=Field4"),
                        postResource(
                                msg,
                                1,
                                "http://example.org/",
                                "field1=Field1&submit=Submit3&field2=Field2&field3=Field3&field4=Field4")));
    }

    private static String formattedDate(String format, Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    private static org.zaproxy.zap.spider.parser.SpiderHtmlFormParser createSpiderHtmlFormParser() {
        return createSpiderHtmlFormParser(new DefaultValueGenerator());
    }

    private static org.zaproxy.zap.spider.parser.SpiderHtmlFormParser createSpiderHtmlFormParser(
            ValueGenerator valueGenerator) {
        org.zaproxy.zap.spider.SpiderParam spiderOptions = createSpiderParamWithConfig();
        spiderOptions.setProcessForm(true);
        spiderOptions.setPostForm(true);
        return new org.zaproxy.zap.spider.parser.SpiderHtmlFormParser(
                spiderOptions, valueGenerator);
    }

    private static HttpMessage createMessageWith(String filename) {
        return createMessageWith(null, filename);
    }

    private static HttpMessage createMessageWith(String formMethod, String filename) {
        return createMessageWith(formMethod, filename, null, null, "/");
    }

    private static HttpMessage createMessageWith(
            String formMethod, String filename, String formAction, String baseHtml) {
        return createMessageWith(formMethod, filename, formAction, baseHtml, "/");
    }

    private static HttpMessage createMessageWith(
            String formMethod,
            String filename,
            String formAction,
            String baseHtml,
            String requestUri) {
        HttpMessage message = new HttpMessage();
        try {
            String fileContents = readFile(BASE_DIR_HTML_FILES.resolve(filename));
            if (formMethod != null) {
                fileContents = fileContents.replace(FORM_METHOD_TOKEN, formMethod);
            }
            if (formAction != null) {
                fileContents = fileContents.replace(FORM_ACTION_TOKEN, formAction);
            }
            if (baseHtml != null) {
                fileContents = fileContents.replace(BASE_HTML_TOKEN, baseHtml);
            }
            message.setRequestHeader("GET " + requestUri + " HTTP/1.1\r\nHost: example.com\r\n");
            message.setResponseHeader(
                    "HTTP/1.1 200 OK\r\n"
                            + "Content-Type: text/html; charset=UTF-8\r\n"
                            + "Content-Length: "
                            + fileContents.length());
            message.setResponseBody(fileContents);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    private static class TestValueGenerator implements ValueGenerator {

        private final List<FormField> fields;

        TestValueGenerator() {
            fields = new ArrayList<>();
        }

        List<FormField> getFields() {
            return fields;
        }

        @Override
        public String getValue(
                URI uri,
                String targetUri,
                String fieldName,
                String defaultValue,
                List<String> values,
                Map<String, String> formAttributes,
                Map<String, String> fieldAttributes) {
            fields.add(
                    new FormField(
                            uri.toString(),
                            targetUri,
                            fieldName,
                            defaultValue,
                            values,
                            fieldAttributes,
                            formAttributes));
            return "";
        }
    }

    private static class FormField {

        private final String uri;
        private final String targetUri;
        private final String fieldName;
        private final String defaultValue;
        private final List<String> values;
        private final Map<String, String> fieldAttributes;
        private final Map<String, String> formAttributes;

        FormField(
                String uri,
                String targetUri,
                String fieldName,
                String defaultValue,
                List<String> values,
                Map<String, String> fieldAttributes,
                Map<String, String> formAttributes) {
            this.uri = uri;
            this.targetUri = targetUri;
            this.fieldName = fieldName;
            this.defaultValue = defaultValue;
            this.values = values;
            this.fieldAttributes = new HashMap<>(fieldAttributes);
            this.formAttributes = new HashMap<>(formAttributes);
        }

        String getUri() {
            return uri;
        }

        String getTargetUri() {
            return targetUri;
        }

        String getDefaultValue() {
            return defaultValue;
        }

        String getFieldName() {
            return fieldName;
        }

        List<String> getValues() {
            return values;
        }

        Map<String, String> getFieldAttributes() {
            return fieldAttributes;
        }

        Map<String, String> getFormAttributes() {
            return formAttributes;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fieldAttributes == null) ? 0 : fieldAttributes.hashCode());
            result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
            result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
            result = prime * result + ((values == null) ? 0 : values.hashCode());
            result = prime * result + ((formAttributes == null) ? 0 : formAttributes.hashCode());
            result = prime * result + ((targetUri == null) ? 0 : targetUri.hashCode());
            result = prime * result + ((uri == null) ? 0 : uri.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            FormField other = (FormField) obj;
            if (fieldAttributes == null) {
                if (other.fieldAttributes != null) {
                    return false;
                }
            } else if (!fieldAttributes.equals(other.fieldAttributes)) {
                return false;
            }
            if (fieldName == null) {
                if (other.fieldName != null) {
                    return false;
                }
            } else if (!fieldName.equals(other.fieldName)) {
                return false;
            }
            if (defaultValue == null) {
                if (other.defaultValue != null) {
                    return false;
                }
            } else if (!defaultValue.equals(other.defaultValue)) {
                return false;
            }
            if (values == null) {
                if (other.values != null) {
                    return false;
                }
            } else if (!values.equals(other.values)) {
                return false;
            }
            if (formAttributes == null) {
                if (other.formAttributes != null) {
                    return false;
                }
            } else if (!formAttributes.equals(other.formAttributes)) {
                return false;
            }
            if (targetUri == null) {
                if (other.targetUri != null) {
                    return false;
                }
            } else if (!targetUri.equals(other.targetUri)) {
                return false;
            }
            if (uri == null) {
                if (other.uri != null) {
                    return false;
                }
            } else if (!uri.equals(other.uri)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder strBuilder = new StringBuilder(250);
            strBuilder.append("uri=").append(uri);
            strBuilder.append(", targetUri=").append(targetUri);
            strBuilder.append(", fieldName=").append(fieldName);
            strBuilder.append(", defaultValue=").append(defaultValue);
            strBuilder.append(", values=").append(values);
            strBuilder.append(", fieldAttributes=").append(fieldAttributes);
            strBuilder.append(", formAttributes=").append(formAttributes);
            return strBuilder.toString();
        }
    }

    private static FormField formField(
            String uri,
            String targetUri,
            String fieldName,
            String defaultValue,
            List<String> values,
            Map<String, String> fieldAttributes,
            Map<String, String> formAttributes) {
        return new FormField(
                uri, targetUri, fieldName, defaultValue, values, fieldAttributes, formAttributes);
    }

    @SafeVarargs
    private static Map<String, String> attributes(Pair<String, String>... attributes) {
        if (attributes == null || attributes.length == 0) {
            return Collections.emptyMap();
        }

        Map<String, String> mapAttributes = new HashMap<>();
        for (Pair<String, String> attribute : attributes) {
            mapAttributes.put(attribute.first, attribute.second);
        }
        return mapAttributes;
    }

    private static Pair<String, String> attribute(String name, String value) {
        return new Pair<>(name, value);
    }

    private static List<String> list(String preDefValue) {
        if (preDefValue == null || preDefValue.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> values = new ArrayList<>();
        String[] value = preDefValue.split(",");
        for (String val : value) {
            values.add(val);
        }
        return values;
    }
}

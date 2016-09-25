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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.junit.BeforeClass;
import org.junit.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.spider.SpiderParam;

import net.htmlparser.jericho.Source;

/**
 * Unit test for {@link SpiderHtmlFormParser}.
 */
public class SpiderHtmlFormParserUnitTest extends SpiderParserTestUtils {

    private static final String FORM_METHOD_TOKEN = "%%METHOD%%";

    private static final String ROOT_PATH = "/";
    private static final int BASE_DEPTH = 0;

    private static final Path BASE_DIR_HTML_FILES = Paths.get("test/resources/org/zaproxy/zap/spider/parser/htmlform");

    @BeforeClass
    public static void suppressLogging() {
        Logger.getRootLogger().addAppender(new NullAppender());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateParserWithUndefinedSpiderOptions() {
        // Given
        SpiderParam undefinedSpiderOptions = null;
        // When
        new SpiderHtmlFormParser(undefinedSpiderOptions);
        // Then = IllegalArgumentException
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailToEvaluateAnUndefinedMessage() {
        // Given
        HttpMessage undefinedMessage = null;
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        // When
        htmlParser.canParseResource(undefinedMessage, ROOT_PATH, false);
        // Then = NullPointerException
    }

    @Test
    public void shouldNotParseMessageIfAlreadyParsed() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        boolean parsed = true;
        // When
        boolean canParse = htmlParser.canParseResource(new HttpMessage(), ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(false)));
    }

    @Test
    public void shouldNotParseNonHtmlResponse() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        boolean parsed = false;
        // When
        boolean canParse = htmlParser.canParseResource(new HttpMessage(), ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(false)));
    }

    @Test
    public void shouldParseHtmlResponse() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        HttpMessage messageHtmlResponse = createMessageWith("NoForms.html");
        boolean parsed = false;
        // When
        boolean canParse = htmlParser.canParseResource(messageHtmlResponse, ROOT_PATH, parsed);
        // Then
        assertThat(canParse, is(equalTo(true)));
    }

    @Test
    public void shouldParseHtmlResponseEvenIfProvidedPathIsNull() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        HttpMessage messageHtmlResponse = createMessageWith("NoForms.html");
        boolean parsed = false;
        // When
        boolean canParse = htmlParser.canParseResource(messageHtmlResponse, null, parsed);
        // Then
        assertThat(canParse, is(equalTo(true)));
    }

    @Test
    public void shouldNotParseHtmlResponseIfAlreadyParsed() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        HttpMessage messageHtmlResponse = createMessageWith("NoForms.html");
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
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        Source source = createSource(createMessageWith("NoForms.html"));
        // When
        htmlParser.parseResource(undefinedMessage, source, BASE_DEPTH);
        // Then = NullPointerException
    }

    @Test
    public void shouldNotParseMessageIfFormProcessingIsDisabled() {
        // Given
        SpiderParam spiderOptions = createSpiderParamWithConfig();
        spiderOptions.setProcessForm(false);
        SpiderHtmlFormParser htmlParser = new SpiderHtmlFormParser(spiderOptions);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("PostGetForms.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(0)));
    }

    @Test
    public void shouldParseMessageEvenWithoutSource() {
        // Given
        Source source = null;
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        HttpMessage messageHtmlResponse = createMessageWith("NoForms.html");
        // When
        htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then = No exception
    }

    @Test
    public void shouldNeverConsiderCompletelyParsed() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        HttpMessage messageHtmlResponse = createMessageWith("NoForms.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
    }

    @Test
    public void shouldParseSingleGetForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GetForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    public void shouldParseMultipleGetForms() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GetForms.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(2)));
        assertThat(listener.getUrlsFound(), contains(
                "http://example.org/form1?field1=Text+1&field2=Text+2&submit=Submit",
                "http://example.org/form2?a=x&b=y&c=z"));
    }

    @Test
    public void shouldParseGetFormWithMultipleSubmitFields() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GetFormMultipleSubmitFields.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
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
    public void shouldParseSinglePostForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("PostForm.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(postResource(msg, 1, "http://example.org/", "field1=Text+1&field2=Text+2&submit=Submit")));
    }

    @Test
    public void shouldParseMultiplePostForms() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("PostForms.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(2)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(msg, 1, "http://example.org/form1", "field1=Text+1&field2=Text+2&submit=Submit"),
                        postResource(msg, 1, "http://example.org/form2", "a=x&b=y&c=z")));
    }

    @Test
    public void shouldParsePostFormWithMultipleSubmitFields() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("PostFormMultipleSubmitFields.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(5)));
        assertThat(
                listener.getResourcesFound(),
                contains(
                        postResource(msg, 1, "http://example.org/", "field1=Text+1&field2=Text+2&submit1=Submit+1"),
                        postResource(msg, 1, "http://example.org/", "field1=Text+1&field2=Text+2&submit2=Submit+2"),
                        postResource(msg, 1, "http://example.org/", "field1=Text+1&field2=Text+2&submit3=Submit+3"),
                        postResource(msg, 1, "http://example.org/", "field1=Text+1&field2=Text+2&submit=Submit+4"),
                        postResource(msg, 1, "http://example.org/", "field1=Text+1&field2=Text+2&submit=Submit+5")));
    }

    @Test
    public void shouldParsePostAndGetForms() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
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
                        postResource(msg, 1, "http://example.org/form1", "field1=Text+1&field2=Text+2&submit=Submit"),
                        postResource(msg, 1, "http://example.org/form1", "field1=Text+1&field2=Text+2&submit=Submit+2"),
                        postResource(msg, 1, "http://example.org/form1", "field1=Text+1&field2=Text+2&submit3=Submit+3"),
                        uriResource(msg, 1, "http://example.org/form2?a=x&b=y&c=z"),
                        uriResource(msg, 1, "http://example.org/form2?a=x&b=y&submit=Submit+2"),
                        uriResource(msg, 1, "http://example.org/form2?a=x&b=y&submit3=Submit+3")));
    }

    @Test
    public void shouldNotParsePostFormIfPostFormProcessingIsDisabled() {
        // Given
        SpiderParam spiderOptions = createSpiderParamWithConfig();
        spiderOptions.setProcessForm(true);
        spiderOptions.setPostForm(false);
        SpiderHtmlFormParser htmlParser = new SpiderHtmlFormParser(spiderOptions);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("PostForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(0)));
    }

    @Test
    public void shouldParseNonPostFormIfPostFormProcessingIsDisabled() {
        // Given
        SpiderParam spiderOptions = createSpiderParamWithConfig();
        spiderOptions.setProcessForm(true);
        spiderOptions.setPostForm(false);
        SpiderHtmlFormParser htmlParser = new SpiderHtmlFormParser(spiderOptions);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GetForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    public void shouldParseFormAsGetIfNeitherGetNorPostForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("NonGetPostForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    public void shouldParseFormAsGetIfFormHasNoMethod() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("NoMethodForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    public void shouldParseFormAsGetIfFormHasNoMethodEvenIfPostFormProcessingIsDisabled() {
        // Given
        SpiderParam spiderOptions = createSpiderParamWithConfig();
        spiderOptions.setProcessForm(true);
        spiderOptions.setPostForm(false);
        SpiderHtmlFormParser htmlParser = new SpiderHtmlFormParser(spiderOptions);
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("NoMethodForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    public void shouldParseFormAsGetIfFormHasEmptyMethod() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("EmptyMethodForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    public void shouldUseMessageUrlAsActionIfFormHasNoAction() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("NoActionForm.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.com/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    public void shouldParseGetFormWithoutSubmitField() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GetFormNoSubmitField.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.org/?field1=Text+1&field2=Text+2"));
    }

    @Test
    public void shouldParsePostFormWithoutSubmitField() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("PostFormNoSubmitField.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(postResource(msg, 1, "http://example.org/", "field1=Text+1&field2=Text+2")));
    }

    @Test
    public void shouldRemoveFragmentFromActionWhenParsingGetForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GET", "FormActionWithFragment.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    public void shouldRemoveFragmentFromActionWhenParsingPostForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("POST", "FormActionWithFragment.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getResourcesFound(), contains(
                postResource(messageHtmlResponse, 1, "http://example.org/", "field1=Text+1&field2=Text+2&submit=Submit")));
    }

    @Test
    public void shouldRemoveFragmentFromActionWhenParsingNeitherGetNorPostForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("NeitherGetNorPost", "FormActionWithFragment.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    public void shouldAppendToEmptyQueryActionParametersWhenParsingGetForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GetFormActionWithEmptyQuery.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.org/?field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    public void shouldAppendToQueryActionParametersWhenParsingGetForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GetFormActionWithQuery.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.org/?a=b&c=d&field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    public void shouldAppendToQueryActionParametersTerminatedWithAmpersandWhenParsingGetForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage messageHtmlResponse = createMessageWith("GetFormActionWithQueryAmpersand.html");
        Source source = createSource(messageHtmlResponse);
        // When
        boolean completelyParsed = htmlParser.parseResource(messageHtmlResponse, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.org/?a=b&field1=Text+1&field2=Text+2&submit=Submit"));
    }

    @Test
    public void shouldUseBaseHtmlUrlWhenParsingGetForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("GetFormWithHtmlBase.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://base.example.com/search?q=Search&submit=Submit"));
    }

    @Test
    public void shouldIgnoreBaseHtmlIfEmptyHrefWhenParsingGetForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("GetFormWithHtmlBaseWithEmptyHref.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.com/search?q=Search&submit=Submit"));
    }

    @Test
    public void shouldIgnoreBaseHtmlWithNoHrefWhenParsingGetForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("GetFormWithHtmlBaseWithoutHref.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("http://example.com/search?q=Search&submit=Submit"));
    }

    @Test
    public void shouldIgnoreBaseHtmlIfActionIsAbsoluteWhenParsingGetForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("GetFormWithHtmlBaseAndAbsoluteAction.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(1)));
        assertThat(listener.getUrlsFound(), contains("https://example.com/search?q=Search&submit=Submit"));
    }

    @Test
    public void shouldUseBaseHtmlUrlWhenParsingPostForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("PostFormWithHtmlBase.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(postResource(msg, 1, "http://base.example.com/search", "q=Search&submit=Submit")));
    }

    @Test
    public void shouldIgnoreBaseHtmlIfEmptyHrefWhenParsingPostForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("PostFormWithHtmlBaseWithEmptyHref.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(postResource(msg, 1, "http://example.com/search", "q=Search&submit=Submit")));
    }

    @Test
    public void shouldIgnoreBaseHtmlWithNoHrefWhenParsingPostForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("PostFormWithHtmlBaseWithoutHref.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(postResource(msg, 1, "http://example.com/search", "q=Search&submit=Submit")));
    }

    @Test
    public void shouldIgnoreBaseHtmlIfActionIsAbsoluteWhenParsingPostForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        HttpMessage msg = createMessageWith("PostFormWithHtmlBaseAndAbsoluteAction.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(1)));
        assertThat(
                listener.getResourcesFound(),
                contains(postResource(msg, 1, "https://example.com/search", "q=Search&submit=Submit")));
    }

    @Test
    public void shouldSetValuesToFieldsWithNoValueWhenParsingGetForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        Date date = new Date(1474370354555L);
        htmlParser.setDefaultDate(date);
        HttpMessage msg = createMessageWith("GET", "FormNoDefaultValues.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(9)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "http://example.org/?_file=test_file.txt&_hidden&_no-type=ZAP&_password=ZAP&_text=ZAP&submit=Submit",
                        "http://example.org/html5/number?_number=1&_number-max=2&_number-min=1&submit=Submit",
                        "http://example.org/html5/range?_range=1&_range-max=4&_range-min=3&submit=Submit",
                        "http://example.org/html5/misc?_color=%23ffffff&_email=foo-bar%40example.com&_tel=9999999999&_url=http%3A%2F%2Fwww.example.com&submit=Submit",
                        "http://example.org/unknown?_unknown&submit=Submit",
                        "http://example.org/selects?_select-one-option=first-option&_select-selected-option=selected-option&_select-two-options=last-option&submit=Submit",
                        "http://example.org/radio?_radio=second-radio&submit=Submit",
                        "http://example.org/checkbox?_checkbox=second-checkbox&submit=Submit",
                        "http://example.org/html5/date-time?" + params(
                                param("_date", formattedDate("yyyy-MM-dd", date)),
                                param("_datetime", formattedDate("yyyy-MM-dd'T'HH:mm:ss'Z'", date)),
                                param("_datetime-local", formattedDate("yyyy-MM-dd'T'HH:mm:ss", date)),
                                param("_month", formattedDate("yyyy-MM", date)),
                                param("_time", formattedDate("HH:mm:ss", date)),
                                param("_week", formattedDate("yyyy-'W'ww", date)),
                                param("submit", "Submit"))));
    }

    @Test
    public void shouldSetValuesToFieldsWithNoValueWhenParsingPostForm() {
        // Given
        SpiderHtmlFormParser htmlParser = createSpiderHtmlFormParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        htmlParser.addSpiderParserListener(listener);
        Date date = new Date(1474370354555L);
        htmlParser.setDefaultDate(date);
        HttpMessage msg = createMessageWith("POST", "FormNoDefaultValues.html");
        Source source = createSource(msg);
        // When
        boolean completelyParsed = htmlParser.parseResource(msg, source, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
        assertThat(listener.getNumberOfResourcesFound(), is(equalTo(9)));
        assertThat(listener.getResourcesFound(), contains(
                postResource(msg, 1, "http://example.org/", "_hidden=&_no-type=ZAP&_text=ZAP&_password=ZAP&_file=test_file.txt&submit=Submit"),
                postResource(msg, 1, "http://example.org/html5/number", "_number=1&_number-min=1&_number-max=2&submit=Submit"),
                postResource(msg, 1, "http://example.org/html5/range", "_range=1&_range-min=3&_range-max=4&submit=Submit"),
                postResource(msg, 1, "http://example.org/html5/misc", "_url=http%3A%2F%2Fwww.example.com&_email=foo-bar%40example.com&_color=%23ffffff&_tel=9999999999&submit=Submit"),
                postResource(msg, 1, "http://example.org/unknown", "_unknown=&submit=Submit"),
                postResource(msg, 1, "http://example.org/selects", "_select-one-option=first-option&_select-two-options=last-option&_select-selected-option=selected-option&submit=Submit"),
                postResource(msg, 1, "http://example.org/radio", "_radio=second-radio&submit=Submit"),
                postResource(msg, 1, "http://example.org/checkbox", "_checkbox=second-checkbox&submit=Submit"),
                postResource(msg, 1, "http://example.org/html5/date-time", params(
                        param("_datetime", formattedDate("yyyy-MM-dd'T'HH:mm:ss'Z'", date)),
                        param("_datetime-local", formattedDate("yyyy-MM-dd'T'HH:mm:ss", date)),
                        param("_date", formattedDate("yyyy-MM-dd", date)),
                        param("_time", formattedDate("HH:mm:ss", date)),
                        param("_month", formattedDate("yyyy-MM", date)),
                        param("_week", formattedDate("yyyy-'W'ww", date)),
                        param("submit", "Submit")))));
    }

    private static String formattedDate(String format, Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    private SpiderHtmlFormParser createSpiderHtmlFormParser() {
        SpiderParam spiderOptions = createSpiderParamWithConfig();
        spiderOptions.setProcessForm(true);
        spiderOptions.setPostForm(true);
        return new SpiderHtmlFormParser(spiderOptions);
    }

    private static HttpMessage createMessageWith(String filename) {
        return createMessageWith(null, filename);
    }

    private static HttpMessage createMessageWith(String formMethod, String filename) {
        HttpMessage message = new HttpMessage();
        try {
            String fileContents = readFile(BASE_DIR_HTML_FILES.resolve(filename));
            if (formMethod != null) {
                fileContents = fileContents.replace(FORM_METHOD_TOKEN, formMethod);
            }
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

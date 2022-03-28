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
package org.parosproxy.paros.core.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.testutils.Log4jTestAppender;
import org.zaproxy.zap.testutils.Log4jTestAppender.AppendedLogEvent;

/** Unit test for {@link VariantJSONQuery}. */
class VariantJSONQueryUnitTest {

    private Log4jTestAppender testAppender;

    @BeforeEach
    void setup() {
        LoggerContext context = LoggerContext.getContext();
        context.getRootLogger()
                .getAppenders()
                .values()
                .forEach(context.getRootLogger()::removeAppender);
    }

    @AfterEach
    void cleanup() throws Exception {
        Configurator.reconfigure(getClass().getResource("/log4j2-test.properties").toURI());
    }

    @Test
    void shouldHaveParametersListEmptyByDefault() {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters, is(empty()));
    }

    @Test
    void shouldNotFindParameterFromMalformedJsonObject() throws HttpMalformedHeaderException {
        // Given
        withLoggerAppender();
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(getMessageWithBody("{\"foo\":\""));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters, is(empty()));
        assertLogEvent(Level.WARN, "EOF reached while reading JSON field name");
    }

    @Test
    void shouldNotThrowExceptionIfResponseIsEmpty() throws HttpMalformedHeaderException {
        // Given
        withLoggerAppender();
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(getMessageWithBody(""));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters, is(empty()));
        for (AppendedLogEvent logEvent : testAppender.getLogEvents()) {
            String logMessage = logEvent.getMessage();
            if (logMessage != null
                    && logMessage.contains("Input is invalid JSON; does not start with")) {
                fail("Wrote parsing warning when the body was empty: " + logMessage);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "True", "false", "False"})
    void shouldNotFindParametersForBooleanValues(String value) throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(
                getMessageWithBody("{\"a\":" + value + ", \"b\":[" + value + "]}"));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters, is(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "Null"})
    void shouldNotFindParametersForNullValuesByDefault(String value)
            throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(
                getMessageWithBody("{\"a\":" + value + ", \"b\":[" + value + "]}"));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters, is(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "Null"})
    void shouldFindParametersForNullValuesIfEnabled(String value)
            throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setScanNullValues(true);
        variantJSONQuery.setMessage(
                getMessageWithBody("{\"a\":" + value + ", \"b\":[" + value + ", " + value + "]}"));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters.get(0).getName(), is("a"));
        assertThat(parameters.get(0).getValue(), is(nullValue()));
        assertThat(parameters.get(1).getName(), is("b[0]"));
        assertThat(parameters.get(1).getValue(), is(nullValue()));
        assertThat(parameters.get(2).getName(), is("b[1]"));
        assertThat(parameters.get(2).getValue(), is(nullValue()));
    }

    @Test
    void shouldReplaceNullValueAsStringInObject() throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setScanNullValues(true);
        HttpMessage message = getMessageWithBody("{\"a\": null}");
        variantJSONQuery.setMessage(message);
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        variantJSONQuery.setParameter(message, parameters.get(0), "", "injection");
        // Then
        assertThat(message.getRequestBody().toString(), is("{\"a\": \"injection\"}"));
    }

    @Test
    void shouldReplaceNullValueAsStringInArray() throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setScanNullValues(true);
        HttpMessage message = getMessageWithBody("[null, null]");
        variantJSONQuery.setMessage(message);
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        variantJSONQuery.setParameter(message, parameters.get(1), "", "injection");
        // Then
        assertThat(message.getRequestBody().toString(), is("[null, \"injection\"]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"[]", "{\"a\":[]}"})
    void shouldNotFindParametersWithEmptyArrays(String json) throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(getMessageWithBody(json));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters, is(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"a\":{}}", "[{}]"})
    void shouldNotFindParametersWithEmptyJsonObjects(String json)
            throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(getMessageWithBody(json));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters, is(empty()));
    }

    @Test
    void shouldNotFindParameterFromUnknownValueType() throws HttpMalformedHeaderException {
        // Given
        withLoggerAppender();
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(getMessageWithBody("{\"foo\":something}"));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters, is(empty()));
        assertLogEvent(Level.WARN, "Unknown value type '115' for field 'foo' at position 8");
    }

    @Test
    void shouldFindStringValueInJsonObject() throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(getMessageWithBody("{\"foo\":\"bar\"}"));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters.get(0).getName(), is("foo"));
        assertThat(parameters.get(0).getValue(), is("bar"));
    }

    @Test
    void shouldFindStringWithEscapedCharacterInJsonObject() throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(getMessageWithBody("{\"foo\":\"bar\n\"}"));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters.get(0).getName(), is("foo"));
        assertThat(parameters.get(0).getValue(), is("bar\n"));
    }

    @Test
    void shouldFindStringWithEscapedQuotationMarkInJsonObject()
            throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(getMessageWithBody("{\"foo\":\"bar\\\"\"}"));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters.get(0).getName(), is("foo"));
        assertThat(parameters.get(0).getValue(), is("bar\""));
    }

    @Test
    void shouldFindPropertyWithEscapedCharacterInJsonObject() throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(getMessageWithBody("{\"foo\n\":\"bar\"}"));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters.get(0).getName(), is("foo\n"));
        assertThat(parameters.get(0).getValue(), is("bar"));
    }

    @Test
    void shouldFindPropertyWithEscapedQuotationMarkInJsonObject()
            throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(getMessageWithBody("{\"foo\\\"\":\"bar\"}"));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters.get(0).getName(), is("foo\\\""));
        assertThat(parameters.get(0).getValue(), is("bar"));
    }

    @Test
    void shouldFindValueWithEscapedQuotationMarkInJsonArray() throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(getMessageWithBody("[\"bar\\\"\"]"));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters.get(0).getName(), is("@items[0]"));
        assertThat(parameters.get(0).getValue(), is("bar\""));
    }

    @Test
    void shouldExtractNumbersInJsonObject() throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(
                getMessageWithBody(
                        "{ \"a\": 1, \"b\": -2, \"c\": 3.4e5, \"d\": -6E+7, \"e\": 8e-9 }"));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters.get(0).getName(), is("a"));
        assertThat(parameters.get(0).getValue(), is("1"));
        assertThat(parameters.get(1).getName(), is("b"));
        assertThat(parameters.get(1).getValue(), is("-2"));
        assertThat(parameters.get(2).getName(), is("c"));
        assertThat(parameters.get(2).getValue(), is("3.4e5"));
        assertThat(parameters.get(3).getName(), is("d"));
        assertThat(parameters.get(3).getValue(), is("-6E+7"));
        assertThat(parameters.get(4).getName(), is("e"));
        assertThat(parameters.get(4).getValue(), is("8e-9"));
    }

    @Test
    void shouldExtractNumbersInJsonArray() throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        variantJSONQuery.setMessage(getMessageWithBody("[ 1, -2, 3.4e5, -6E+7, 8e-9 ]"));
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters.get(0).getName(), is("@items[0]"));
        assertThat(parameters.get(0).getValue(), is("1"));
        assertThat(parameters.get(1).getName(), is("@items[1]"));
        assertThat(parameters.get(1).getValue(), is("-2"));
        assertThat(parameters.get(2).getName(), is("@items[2]"));
        assertThat(parameters.get(2).getValue(), is("3.4e5"));
        assertThat(parameters.get(3).getName(), is("@items[3]"));
        assertThat(parameters.get(3).getValue(), is("-6E+7"));
        assertThat(parameters.get(4).getName(), is("@items[4]"));
        assertThat(parameters.get(4).getValue(), is("8e-9"));
    }

    @Test
    void shouldReplaceNumberInJsonObject() throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        HttpMessage message =
                getMessageWithBody(
                        "{ \"a\": 1, \"b\": -2, \"c\": 3.4e5, \"d\": -6E+7, \"e\": 8e-9 }");
        variantJSONQuery.setMessage(message);
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        variantJSONQuery.setParameter(message, parameters.get(2), "c", "injection");
        // Then
        assertThat(
                message.getRequestBody().toString(),
                is("{ \"a\": 1, \"b\": -2, \"c\": \"injection\", \"d\": -6E+7, \"e\": 8e-9 }"));
    }

    @Test
    void shouldReplaceNumberEscapedInJsonObject() throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        HttpMessage message =
                getMessageWithBody(
                        "{ \"a\": 1, \"b\": -2, \"c\": 3.4e5, \"d\": -6E+7, \"e\": 8e-9 }");
        variantJSONQuery.setMessage(message);
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        variantJSONQuery.setEscapedParameter(message, parameters.get(2), "c", "injection");
        // Then
        assertThat(
                message.getRequestBody().toString(),
                is("{ \"a\": 1, \"b\": -2, \"c\": injection, \"d\": -6E+7, \"e\": 8e-9 }"));
    }

    @Test
    void shouldReplaceNumberInJsonArray() throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        HttpMessage message = getMessageWithBody("[ 1, -2, 3.4e5, -6E+7, 8e-9 ]");
        variantJSONQuery.setMessage(message);
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        variantJSONQuery.setParameter(message, parameters.get(2), "", "injection");
        // Then
        assertThat(
                message.getRequestBody().toString(), is("[ 1, -2, \"injection\", -6E+7, 8e-9 ]"));
    }

    @Test
    void shouldReplaceNumberEscapedInJsonArray() throws HttpMalformedHeaderException {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        HttpMessage message = getMessageWithBody("[ 1, -2, 3.4e5, -6E+7, 8e-9 ]");
        variantJSONQuery.setMessage(message);
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        variantJSONQuery.setEscapedParameter(message, parameters.get(2), "", "injection");
        // Then
        assertThat(message.getRequestBody().toString(), is("[ 1, -2, injection, -6E+7, 8e-9 ]"));
    }

    private static HttpMessage getMessageWithBody(String body) throws HttpMalformedHeaderException {
        return new HttpMessage(
                new HttpRequestHeader(
                        "POST / HTTP/1.1\nHost: www.example.com\nContent-Type: application/json"),
                new HttpRequestBody(body));
    }

    private void assertLogEvent(Level level, String message) {
        for (AppendedLogEvent logEvent : testAppender.getLogEvents()) {
            String logMessage = logEvent.getMessage();
            if (logMessage != null && logMessage.contains(message)) {
                assertThat(
                        "Log message with unexpected level.",
                        logEvent.getLevel(),
                        is(equalTo(level)));
                return;
            }
        }
        fail("Log message not found: " + message);
    }

    private void withLoggerAppender() {
        testAppender = new Log4jTestAppender();
        LoggerContext context = LoggerContext.getContext();
        Logger logger = context.getLogger(VariantJSONQuery.class.getCanonicalName());
        context.getConfiguration().addLoggerAppender(logger, testAppender);
        logger.setLevel(Level.ALL);
    }
}

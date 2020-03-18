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
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.network.HttpRequestBody;

/** Unit test for {@link VariantJSONQuery}. */
public class VariantJSONQueryUnitTest {
    @Test
    public void shouldHaveParametersListEmptyByDefault() {
        // Given
        VariantJSONQuery variantJSONQuery = new VariantJSONQuery();
        // When
        List<NameValuePair> parameters = variantJSONQuery.getParamList();
        // Then
        assertThat(parameters, is(empty()));
    }

    @Test
    public void shouldFindStringValueInJsonObject() throws HttpMalformedHeaderException {
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
    public void shouldFindStringWithEscapedCharacterInJsonObject()
            throws HttpMalformedHeaderException {
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
    public void shouldFindStringWithEscapedQuotationMarkInJsonObject()
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
    public void shouldFindPropertyWithEscapedCharacterInJsonObject()
            throws HttpMalformedHeaderException {
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
    public void shouldFindPropertyWithEscapedQuotationMarkInJsonObject()
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
    public void shouldFindValueWithEscapedQuotationMarkInJsonArray()
            throws HttpMalformedHeaderException {
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
    public void shouldExtractNumbersInJsonObject() throws HttpMalformedHeaderException {
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
    public void shouldExtractNumbersInJsonArray() throws HttpMalformedHeaderException {
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
    public void shouldReplaceNumberInJsonObject() throws HttpMalformedHeaderException {
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
    public void shouldReplaceNumberEscapedInJsonObject() throws HttpMalformedHeaderException {
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
    public void shouldReplaceNumberInJsonArray() throws HttpMalformedHeaderException {
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
    public void shouldReplaceNumberEscapedInJsonArray() throws HttpMalformedHeaderException {
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
}

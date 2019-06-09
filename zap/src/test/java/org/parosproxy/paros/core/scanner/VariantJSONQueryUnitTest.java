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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.junit.Test;
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

    private static HttpMessage getMessageWithBody(String body) throws HttpMalformedHeaderException {
        return new HttpMessage(
                new HttpRequestHeader(
                        "POST / HTTP/1.1\nHost: www.example.com\nContent-Type: application/json"),
                new HttpRequestBody(body));
    }
}

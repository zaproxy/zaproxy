/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

/** Unit test for {@link VariantPlainBody}. */
class VariantPlainBodyUnitTest {

    @Test
    void shouldHaveParametersListEmptyByDefault() {
        // Given
        VariantPlainBody variant = new VariantPlainBody();
        // When
        List<NameValuePair> parameters = variant.getParamList();
        // Then
        assertThat(parameters, is(empty()));
    }

    @Test
    void shouldNotAllowToModifyReturnedParametersList() {
        // Given
        VariantPlainBody variant = new VariantPlainBody();
        NameValuePair nvp = new NameValuePair(NameValuePair.TYPE_POST_DATA, "foo", "bar", 0);
        // When / Then
        assertThrows(UnsupportedOperationException.class, () -> variant.getParamList().add(nvp));
    }

    @Test
    void shouldFailToExtractParametersFromUndefinedMessage() {
        // Given
        VariantPlainBody variant = new VariantPlainBody();
        HttpMessage undefinedMessage = null;
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> variant.setMessage(undefinedMessage));
    }

    @Test
    void shouldNotExtractAnyParameterIfThereIsNoBodyContent() {
        // Given
        VariantPlainBody variant = new VariantPlainBody();
        HttpMessage msg = createBasicMessage();
        // When
        variant.setMessage(msg);
        // Then
        assertThat(variant.getParamList(), is(empty()));
    }

    @Test
    void shouldNotExtractAnyParameterIfThereIsANonTextPlainContentType() {
        // Given
        String body = "foobar";
        VariantPlainBody variant = new VariantPlainBody();
        HttpMessage msg = createBasicMessage();
        msg.setRequestBody(body);
        msg.getRequestHeader().setHeader(HttpHeader.CONTENT_TYPE, "application/foo");
        // When
        variant.setMessage(msg);
        // Then
        assertThat(variant.getParamList(), is(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"text/plain"})
    @NullSource
    void shouldExtractParameterIfThereIsBodyContentAndNoOrPlainTextContentType(String contentType) {
        // Given
        String body = "foobar";
        VariantPlainBody variant = new VariantPlainBody();
        HttpMessage msg = createBasicMessage();
        msg.getRequestHeader().setHeader(HttpHeader.CONTENT_TYPE, contentType);
        msg.setRequestBody(body);

        // When
        variant.setMessage(msg);
        // Then
        assertThat(variant.getParamList(), hasSize(1));
        NameValuePair nvp = variant.getParamList().get(0);
        assertThat(nvp.getName(), is(emptyString()));
        assertThat(nvp.getValue(), is(equalTo(body)));
    }

    private static HttpMessage createBasicMessage() {
        HttpMessage message = new HttpMessage();
        try {
            message.setRequestHeader("GET / HTTP/1.1\r\n");
        } catch (HttpMalformedHeaderException e) {
            throw new RuntimeException(e);
        }
        return message;
    }
}

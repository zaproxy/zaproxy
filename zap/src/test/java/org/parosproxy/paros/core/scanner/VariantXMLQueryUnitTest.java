/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.network.HttpRequestBody;

/** Unit test for {@link VariantXMLQuery}. */
class VariantXMLQueryUnitTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "text/xml",
                "Text/xml",
                "TEXT/XML",
                "application/xml",
                "Application/xml",
                "APPLICATION/XML",
                "application/soap+xml",
                "Application/soap+xml",
                "Application/XML; charset=utf-8"
            })
    void shouldExtractParametersForCaseInsensitiveXmlContentType(String contentType)
            throws HttpMalformedHeaderException {
        // Given
        VariantXMLQuery variantXMLQuery = new VariantXMLQuery();
        HttpMessage msg =
                new HttpMessage(
                        new HttpRequestHeader(
                                "POST / HTTP/1.1\r\nHost: www.example.com\r\nContent-Type: "
                                        + contentType),
                        new HttpRequestBody("<item><foo>bar</foo></item>"));
        // When
        variantXMLQuery.setMessage(msg);
        List<NameValuePair> parameters = variantXMLQuery.getParamList();
        // Then
        assertThat(variantXMLQuery.isValidContentType(msg.getRequestHeader()), is(true));
        assertThat(parameters.size(), is(equalTo(1)));
        assertThat(parameters.get(0).getName(), is(equalTo("foo")));
        assertThat(parameters.get(0).getValue(), is(equalTo("bar")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"text/html", "application/json", "application/octet-stream"})
    void shouldReturnFalseForInvalidContentType(String contentType) {
        // Given
        VariantXMLQuery variantXMLQuery = new VariantXMLQuery();
        // When / Then
        assertThat(variantXMLQuery.isValidContentType(createHeader(contentType)), is(false));
    }

    private static HttpRequestHeader createHeader(String contentType) {
        HttpRequestHeader header = new HttpRequestHeader();
        if (contentType != null) {
            header.setHeader(HttpHeader.CONTENT_TYPE, contentType);
        }
        return header;
    }
}

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
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.network.HttpRequestBody;

/** Unit test for {@link VariantFormQuery}. */
class VariantFormQueryUnitTest extends WithConfigsTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "application/x-www-form-urlencoded",
                "Application/x-www-form-urlencoded",
                "APPLICATION/X-WWW-FORM-URLENCODED",
                "application/x-www-form-urlencoded; charset=utf-8",
                "Application/x-www-form-urlencoded; charset=utf-8"
            })
    void shouldExtractParametersForCaseInsensitiveFormContentType(String contentType)
            throws HttpMalformedHeaderException {
        // Given
        VariantFormQuery variantFormQuery = new VariantFormQuery();
        HttpMessage msg =
                new HttpMessage(
                        new HttpRequestHeader(
                                "POST / HTTP/1.1\r\nHost: www.example.com\r\nContent-Type: "
                                        + contentType),
                        new HttpRequestBody("foo=val1&bar=val2"));
        // When
        variantFormQuery.setMessage(msg);
        List<NameValuePair> parameters = variantFormQuery.getParamList();
        // Then
        assertThat(parameters.size(), is(equalTo(2)));
        assertThat(parameters.get(0).getName(), is(equalTo("foo")));
        assertThat(parameters.get(0).getValue(), is(equalTo("val1")));
        assertThat(parameters.get(1).getName(), is(equalTo("bar")));
        assertThat(parameters.get(1).getValue(), is(equalTo("val2")));
    }
}

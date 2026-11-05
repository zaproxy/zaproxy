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
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpRequestHeader;

/** Unit test for {@link VariantGWTQuery}. */
class VariantGWTQueryUnitTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "text/x-gwt-rpc",
                "Text/x-gwt-rpc",
                "TEXT/X-GWT-RPC",
                "Text/x-gwt-rpc; charset=utf-8"
            })
    void shouldReturnTrueForCaseInsensitiveGwtContentType(String contentType) {
        // Given
        VariantGWTQuery variantGWTQuery = new VariantGWTQuery();
        // When / Then
        assertThat(variantGWTQuery.isValidContentType(createHeader(contentType)), is(true));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"text/html", "application/json", "application/xml"})
    void shouldReturnFalseForInvalidContentType(String contentType) {
        // Given
        VariantGWTQuery variantGWTQuery = new VariantGWTQuery();
        // When / Then
        assertThat(variantGWTQuery.isValidContentType(createHeader(contentType)), is(false));
    }

    private static HttpRequestHeader createHeader(String contentType) {
        HttpRequestHeader header = new HttpRequestHeader();
        if (contentType != null) {
            header.setHeader(HttpHeader.CONTENT_TYPE, contentType);
        }
        return header;
    }
}

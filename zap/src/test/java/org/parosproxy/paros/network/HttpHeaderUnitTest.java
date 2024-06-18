/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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
package org.parosproxy.paros.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HttpHeaderUnitTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "application/taxii+json",
                "application/vnd.api+json",
                "text/json",
                "application/yaml",
                "application/x-yaml",
                "text/yaml",
                "application/xml",
                "application/problem+xml",
                "text/xml"
            })
    void shouldIdentifyContentTypes(String type) {
        // Given
        HttpResponseHeader header = new HttpResponseHeader();
        header.setHeader(HttpHeader.CONTENT_TYPE, type);
        String[] acceptedTypes = {"json", "xml", "yaml"};
        // When
        boolean hasType = header.hasContentType(acceptedTypes);
        // Then
        assertThat(hasType, is(equalTo(true)));
    }
}

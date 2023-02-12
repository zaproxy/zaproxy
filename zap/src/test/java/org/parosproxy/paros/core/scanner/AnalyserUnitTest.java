/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.regex.Pattern;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit test for {@link Analyser}. */
class AnalyserUnitTest {

    @Test
    void shouldNotFailToCreatePathRegexWithReservedCharsInQuery() throws Exception {
        // Given
        URI uri = new URI("http://example.com/path?query=**", true);
        // When
        String regex = Analyser.getPathRegex(uri);
        // Then
        assertDoesNotThrow(() -> Pattern.compile(regex));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://example.com/",
                "https://example.com/path",
                "http://example.com/path?query=foo",
                "http://example.com/path?query=foo.bar.oof"
            })
    void shouldReturnCompileableRegexString(String url) throws Exception {
        // Given
        URI uri = new URI(url, true);
        // When
        String regex = Analyser.getPathRegex(uri);
        // Then
        assertDoesNotThrow(() -> Pattern.compile(regex));
    }
}

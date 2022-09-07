/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.extension.custompages;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;

class DefaultCustomPageUnitTest {

    private static final String TEST_MATCH_PATTERN = ".*Something went wrong.*";
    private static final String BASE64_TEST_PATTERN =
            new String(
                    Base64.getEncoder().encode(TEST_MATCH_PATTERN.getBytes(StandardCharsets.UTF_8)),
                    StandardCharsets.US_ASCII);

    @BeforeEach
    void setup() {
        Constant.messages = mock(I18N.class);
    }

    @Test
    void shouldEncodeCustomPage() {
        // Given
        DefaultCustomPage customPage =
                new DefaultCustomPage(
                        0,
                        TEST_MATCH_PATTERN,
                        CustomPageMatcherLocation.RESPONSE_CONTENT,
                        true,
                        CustomPage.Type.ERROR_500,
                        true);
        // When
        String encodedCustomPage = DefaultCustomPage.encode(customPage);
        // Then
        assertTrue(encodedCustomPage.equals(BASE64_TEST_PATTERN + ";1;true;1;true;"));
    }

    @Test
    void shouldDecodeCustomPage() {
        // Given
        String encodedCustomPage = BASE64_TEST_PATTERN + ";1;true;1;true;";
        DefaultCustomPage expectedCustomPage =
                new DefaultCustomPage(
                        0,
                        TEST_MATCH_PATTERN,
                        CustomPageMatcherLocation.RESPONSE_CONTENT,
                        true,
                        CustomPage.Type.ERROR_500,
                        true);
        // When
        DefaultCustomPage customPage = DefaultCustomPage.decode(0, encodedCustomPage);
        // Then
        assertCustomPagesMatch(expectedCustomPage, customPage);
    }

    @Test
    void shouldDecodeCustomPageWithDefaultTypeIfTypeIdInvalid() {
        // Given/When
        DefaultCustomPage actual =
                DefaultCustomPage.decode(0, BASE64_TEST_PATTERN + ";1;true;15;true;");
        DefaultCustomPage expected =
                new DefaultCustomPage(
                        0,
                        TEST_MATCH_PATTERN,
                        CustomPageMatcherLocation.getCustomPagePageMatcherLocationWithId(1),
                        true,
                        CustomPage.Type.getDefaultType(),
                        true);
        // Then
        assertCustomPagesMatch(expected, actual);
    }

    @Test
    void shouldDecodeCustomPageWithDefaultMatcherLocationIfMatcherLocationIdInvalid() {
        // Given/When
        DefaultCustomPage actual =
                DefaultCustomPage.decode(0, BASE64_TEST_PATTERN + ";5;true;1;true;");
        DefaultCustomPage expected =
                new DefaultCustomPage(
                        0,
                        TEST_MATCH_PATTERN,
                        CustomPageMatcherLocation.getDefaultLocation(),
                        true,
                        CustomPage.Type.getCustomPageTypeWithId(1),
                        true);
        // Then
        assertCustomPagesMatch(expected, actual);
    }

    @Test
    void shouldDecodeCustomPageWithIsRegexFalseWhenIsRegexComponentInvalid() {
        // Given/When
        DefaultCustomPage actual =
                DefaultCustomPage.decode(0, BASE64_TEST_PATTERN + ";1;foo;1;true;");
        DefaultCustomPage expected =
                new DefaultCustomPage(
                        0,
                        TEST_MATCH_PATTERN,
                        CustomPageMatcherLocation.getCustomPagePageMatcherLocationWithId(1),
                        false,
                        CustomPage.Type.getCustomPageTypeWithId(1),
                        true);
        // Then
        assertCustomPagesMatch(expected, actual);
    }

    @Test
    void shouldDecodeCustomPageWithEnabledFalseWhenEnabledComponentInvalid() {
        // Given/When
        DefaultCustomPage actual =
                DefaultCustomPage.decode(0, BASE64_TEST_PATTERN + ";1;true;1;foo;");
        DefaultCustomPage expected =
                new DefaultCustomPage(
                        0,
                        TEST_MATCH_PATTERN,
                        CustomPageMatcherLocation.getCustomPagePageMatcherLocationWithId(1),
                        true,
                        CustomPage.Type.getCustomPageTypeWithId(1),
                        false);
        // Then
        assertCustomPagesMatch(expected, actual);
    }

    private void assertCustomPagesMatch(CustomPage expected, CustomPage actual) {
        assertTrue(actual.getPageMatcher().equals(expected.getPageMatcher()));
        assertTrue(actual.getPageMatcherLocation().equals(expected.getPageMatcherLocation()));
        assertTrue(actual.isRegex() == expected.isRegex());
        assertTrue(actual.getType().equals(expected.getType()));
        assertTrue(actual.isEnabled() == expected.isEnabled());
    }
}

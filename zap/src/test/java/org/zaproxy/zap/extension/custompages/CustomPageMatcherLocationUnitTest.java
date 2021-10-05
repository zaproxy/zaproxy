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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;

class CustomPageMatcherLocationUnitTest {

    @BeforeEach
    void setup() {
        Constant.messages = mock(I18N.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void shouldFindLocationByValidId(int id) {
        // Given/When
        CustomPageMatcherLocation location =
                CustomPageMatcherLocation.getCustomPagePageMatcherLocationWithId(id);
        // Then
        assertTrue(Arrays.asList(CustomPageMatcherLocation.values()).contains(location));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 15, Integer.MAX_VALUE})
    void shouldReturnDefaultLocationForInvalidId(int id) {
        // Given/When
        CustomPageMatcherLocation location =
                CustomPageMatcherLocation.getCustomPagePageMatcherLocationWithId(id);
        // Then
        assertEquals(CustomPageMatcherLocation.getDefaultLocation(), location);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1, 2})
    void shouldNotReturnNullOrEmptyStringRepresentation(int id) {
        // Given/When
        CustomPageMatcherLocation location =
                CustomPageMatcherLocation.getCustomPagePageMatcherLocationWithId(id);
        // Then
        assertNotNull(location.toString());
        assertTrue(!location.toString().isEmpty());
    }

    @Test
    void shouldHaveExpectedI18nLocationNames() {
        // Given
        I18N i18n = new I18N(Locale.ENGLISH);
        // When/Then
        assertEquals("URL", i18n.getString(CustomPageMatcherLocation.URL.getNameKey()));
        assertEquals(
                "Response",
                i18n.getString(CustomPageMatcherLocation.RESPONSE_CONTENT.getNameKey()));
    }
}

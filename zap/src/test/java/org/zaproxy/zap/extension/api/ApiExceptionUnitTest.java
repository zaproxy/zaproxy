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
package org.zaproxy.zap.extension.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.util.Locale;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link ApiException}. */
class ApiExceptionUnitTest {

    @BeforeAll
    static void beforeAll() {
        Constant.messages = new I18N(Locale.ROOT);
    }

    @AfterAll
    static void afterAll() {
        Constant.messages = null;
    }

    @ParameterizedTest
    @EnumSource
    void shouldHaveResourceMessageForAllTypes(ApiException.Type type) {
        // Given
        ApiException ex = new ApiException(type);
        // When
        String toString = ex.toString();
        // Then
        assertThat(toString, not(startsWith("!api.error.")));
    }
}

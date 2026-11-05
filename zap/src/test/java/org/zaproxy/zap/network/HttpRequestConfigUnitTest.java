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
package org.zaproxy.zap.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Unit test for {@link HttpRequestConfig}. */
class HttpRequestConfigUnitTest {

    @Test
    void shouldHaveExpectedDefaults() {
        // Given / When
        HttpRequestConfig config = HttpRequestConfig.builder().build();

        // Then
        assertThat(config.isFollowRedirects(), is(false));
        assertThat(
                config.getRedirectionValidator(),
                is(sameInstance(DefaultHttpRedirectionValidator.INSTANCE)));
        assertThat(config.isNotifyListeners(), is(true));
        assertThat(config.getSoTimeout(), is(equalTo(HttpRequestConfig.NO_VALUE_SET)));
    }

    @Test
    void shouldCopyAllPropertiesWhenBuildingFromExistingConfig() {
        // Given
        HttpRedirectionValidator validator = redirection -> true;
        HttpRequestConfig original =
                HttpRequestConfig.builder()
                        .setFollowRedirects(true)
                        .setRedirectionValidator(validator)
                        .setNotifyListeners(false)
                        .setSoTimeout(4321)
                        .build();

        // When
        HttpRequestConfig copy = HttpRequestConfig.builder(original).build();

        // Then
        assertThat(copy.isFollowRedirects(), is(true));
        assertThat(copy.getRedirectionValidator(), is(sameInstance(validator)));
        assertThat(copy.isNotifyListeners(), is(false));
        assertThat(copy.getSoTimeout(), is(equalTo(4321)));
    }

    @Test
    void shouldFailToCreateBuilderFromNullConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> HttpRequestConfig.builder(null));
    }
}

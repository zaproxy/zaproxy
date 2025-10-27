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
package org.zaproxy.zap.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import org.junit.jupiter.api.Test;

class EncodingUtilsUnitTest {

    @Test
    void shouldEncodeEmptyMapToEmptyString() {
        // Given
        Map<String, String> input = Map.of();
        // When
        String result = EncodingUtils.mapToString(input);
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldEncodeEmptyMapEntryToEmptyPairString() {
        // Given
        Map<String, String> input = Map.of("", "");
        // When
        String result = EncodingUtils.mapToString(input);
        // Then
        assertThat(result).isEqualTo(":");
    }

    @Test
    void shouldDecodeEmptyStringToEmptyMap() {
        // Given
        String input = "";
        // When
        Map<String, String> result = EncodingUtils.stringToMap(input);
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldDecodeEmptyPairStringToEmptyMapEntry() {
        // Given
        String input = ":";
        // When
        Map<String, String> result = EncodingUtils.stringToMap(input);
        // Then
        assertThat(result).contains(entry("", ""));
    }
}

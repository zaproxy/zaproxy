/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit test for {@link ApiResponseSet}. */
class ApiResponseSetUnitTest {

    @Test
    void shouldReturnCorrectJsonObjectWithStdStringValues() throws ApiException {
        // Given
        String name = "name";
        String value = "value";
        Map<String, String> map = new HashMap<>();
        map.put(name, value);
        ApiResponseSet<String> apiRespSet = new ApiResponseSet<>("test", map);
        // When
        String jsonResponse = apiRespSet.toJSON().toString();
        // Then
        assertEquals(jsonResponse, "{\"" + name + "\":\"" + value + "\"}");
    }

    @Test
    void shouldReturnCorrectJsonObjectWithSingleQuotedStringValues() throws ApiException {
        // Given
        String name = "name";
        String value = "'value'";
        Map<String, String> map = new HashMap<>();
        map.put(name, value);
        ApiResponseSet<String> apiRespSet = new ApiResponseSet<>("test", map);
        // When
        String jsonResponse = apiRespSet.toJSON().toString();
        // Then
        assertEquals(jsonResponse, "{\"" + name + "\":\"" + value + "\"}");
    }

    @Test
    void shouldReturnCorrectJsonObjectWithDoubleQuotedStringValues() throws ApiException {
        // Given
        String name = "name";
        String value = "\"value\"";
        Map<String, String> map = new HashMap<>();
        map.put(name, value);
        ApiResponseSet<String> apiRespSet = new ApiResponseSet<>("test", map);
        // When
        String jsonResponse = apiRespSet.toJSON().toString();
        // Then
        assertEquals(jsonResponse, "{\"" + name + "\":\"" + value.replace("\"", "\\\"") + "\"}");
    }

    @Test
    void shouldReturnCorrectJsonObjectWithJsonStringValues() throws ApiException {
        // Given
        String name = "name";
        String value = "{\"key\":\"value\"}";
        Map<String, String> map = new HashMap<>();
        map.put(name, value);
        ApiResponseSet<String> apiRespSet = new ApiResponseSet<>("test", map);
        // When
        String jsonResponse = apiRespSet.toJSON().toString();
        // Then
        assertEquals(jsonResponse, "{\"" + name + "\":\"" + value.replace("\"", "\\\"") + "\"}");
    }
}

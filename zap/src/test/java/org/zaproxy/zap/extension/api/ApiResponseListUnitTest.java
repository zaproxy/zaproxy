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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit test for {@link ApiResponseList}. */
class ApiResponseListUnitTest {

    @Test
    void shouldReturnCorrectJsonObjectWithStdStringValues() throws ApiException {
        // Given
        List<ApiResponse> list = new ArrayList<>();
        list.add(new ApiResponseElement("key1", "val1"));
        list.add(new ApiResponseElement("key2", "val2"));
        ApiResponseList apiRespSet = new ApiResponseList("test", list);
        // When
        String jsonResponse = apiRespSet.toJSON().toString();
        // Then
        assertEquals(jsonResponse, "{\"test\":[\"val1\",\"val2\"]}");
    }

    @Test
    void shouldReturnCorrectJsonObjectWithJSONStringValues() throws ApiException {
        // Given
        List<ApiResponse> list = new ArrayList<>();
        list.add(new ApiResponseElement("key1", "{\"name1\":\"value1\"}"));
        list.add(new ApiResponseElement("key2", "{\"name2\":\"value2\"}"));
        ApiResponseList apiRespSet = new ApiResponseList("test", list);
        // When
        String jsonResponse = apiRespSet.toJSON().toString();
        // Then
        assertEquals(
                jsonResponse,
                "{\"test\":[\"{\\\"name1\\\":\\\"value1\\\"}\",\"{\\\"name2\\\":\\\"value2\\\"}\"]}");
    }
}

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
package org.zaproxy.zap.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;

/** Unit test for {@link JsonUtil}. */
public class JsonUtilUnitTest {

    @Test
    public void shouldQuoteValueThatLooksLikeAnArray() {
        // Given
        String value = "[ 1, 2, 3, 4]";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(quoted(value))));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    public void shouldQuoteValueThatLooksLikeAnObject() {
        // Given
        String value = "{ \"1\" : \"2\", \"3\" : \"4\" }";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(quoted(value))));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    public void shouldNotQuoteValueThatLooksLikeAnArrayButStartsWithSpace() {
        // Given
        String value = " [ 1, 2, 3, 4]";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(value)));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    public void shouldNotQuoteValueThatLooksLikeAnObjectButStartsWithSpace() {
        // Given
        String value = " { \"1\" : \"2\", \"3\" : \"4\" }";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(value)));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    public void shouldNotQuoteSingleQuotedValue() {
        // Given
        String value = "'value'";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(value)));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    public void shouldNotQuoteDoubleQuotedValue() {
        // Given
        String value = "\"value\"";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(value)));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    public void shouldNotQuoteNullLiteralValue() {
        // Given
        String value = "null";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(value)));
        assertValueAfterJsonObject(processedValue, value);
    }

    private static void assertValueAfterJsonObject(String processedValue, String value) {
        // Given processedValue
        String key = "key";
        JSONObject jsonObject = new JSONObject();
        // When
        jsonObject.put(key, processedValue);
        // Then
        assertThat(jsonObject.getString(key), is(equalTo(value)));
    }

    private static String quoted(String value) {
        return "'" + value + "'";
    }
}

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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.model.SessionStructure;

/** Unit test for {@link JsonUtil}. */
class JsonUtilUnitTest {

    @Test
    void shouldQuoteValueThatLooksLikeAnArray() {
        // Given
        String value = "[ 1, 2, 3, 4]";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(quoted(value))));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    void shouldQuoteValueThatLooksLikeAnObject() {
        // Given
        String value = "{ \"1\" : \"2\", \"3\" : \"4\" }";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(quoted(value))));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    void shouldQuoteValueThatLooksLikeAFunction() {
        // Given
        String value = "function() { }";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(quoted(value))));
        assertDoubleQuotedValueAfterJsonObject(processedValue, value);
    }

    @Test
    void shouldQuoteValueThatLooksLikeAFunctionWithWhitespaceEtc() {
        // Given
        String value = "function ( param ) { misc stuff in here with \\\"quotes\\\"}";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(quoted(value))));
        assertDoubleQuotedValueAfterJsonObject(processedValue, value);
    }

    @Test
    void shouldNotQuoteValueThatLooksLikeAnArrayButStartsWithSpace() {
        // Given
        String value = " [ 1, 2, 3, 4]";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(value)));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    void shouldNotQuoteValueThatLooksLikeAnObjectButStartsWithSpace() {
        // Given
        String value = " { \"1\" : \"2\", \"3\" : \"4\" }";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(value)));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    void shouldNotQuoteSingleQuotedValue() {
        // Given
        String value = "'value'";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(value)));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    void shouldNotQuoteDoubleQuotedValue() {
        // Given
        String value = "\"value\"";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(value)));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    void shouldNotQuoteNullLiteralValue() {
        // Given
        String value = "null";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(value)));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    void shouldNotQuoteFunctionHeader() {
        // Given
        String value = "function()";
        // When
        String processedValue = JsonUtil.getJsonFriendlyString(value);
        // Then
        assertThat(processedValue, is(equalTo(value)));
        assertValueAfterJsonObject(processedValue, value);
    }

    @Test
    void shouldReturnKeyStrForJsonObject() {
        // Given
        String json =
                """
                {"aaa":"bbb", "ccc":"ddd", "eee":"fff"}
                """;

        // When
        String res = JsonUtil.getJsonKeyString(json);

        // Then
        assertThat(res, is(equalTo("{aaa,ccc,eee}")));
    }

    @Test
    void shouldReturnKeyStrForJsonArray() {
        // Given
        String json = """
                ["aaa", "bbb", "ccc"]
                """;

        // When
        String res = JsonUtil.getJsonKeyString(json);

        // Then
        assertThat(res, is(equalTo("[]")));
    }

    @Test
    void shouldReturnKeyStrForDeepJsonObject() {
        // Given
        String json =
                """
                {"aaa":{"bbb": "ccc", "ddd": "eee"}, fff: ["kkk", {"ggg":"hhh"}, {"iii":"jjj"}]}
                """;

        // When
        String res = JsonUtil.getJsonKeyString(json);

        // Then
        assertThat(res, is(equalTo("{aaa:{bbb,ddd},fff:[{ggg},{iii}]}")));
    }

    @Test
    void shouldReturnStrForMultipleMatchingChildNodesInJson() {
        // given
        String substr = "{\"aaa\":{\"bbb\": \"ccc\", \"ddd\": \"eee\"}}";
        String json = "[" + String.join(",", Collections.nCopies(5, substr)) + "]";

        // When
        String res = JsonUtil.getJsonKeyString(json);

        // Then
        assertThat(res, is(equalTo("[{aaa:{bbb,ddd}}..]")));
    }

    @Test
    void shouldReturnTrimmedStrForVeryLongJson() {
        // given
        String aa = "{\"aaaaaaaaaa\": \"a\"}";
        String bb = "{\"bbbbbbbbbb\": \"b\"}";
        String aabbbbaa = "[" + aa + "," + bb + "],[" + bb + "," + aa + "]";
        String json = "{\"cccc\": [" + String.join(",", Collections.nCopies(10, aabbbbaa)) + "]}";
        String expectedStr = "[{aaaaaaaaaa},{bbbbbbbbbb}],[{bbbbbbbbbb},{aaaaaaaaaa}]";
        String expectedName = "{cccc:[" + String.join(",", Collections.nCopies(10, expectedStr));
        // when
        String res = JsonUtil.getJsonKeyString(json);
        // then
        assertThat(
                res,
                is(expectedName.substring(0, SessionStructure.MAX_NODE_NAME_SIZE - 3) + "..."));
    }

    @Test
    void shouldThrowExceptionForInvalidJson() throws Exception {
        // given / when / then
        assertThrows(JSONException.class, () -> JsonUtil.getJsonKeyString("{"));
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

    private static void assertDoubleQuotedValueAfterJsonObject(
            String processedValue, String value) {
        // Given processedValue
        String key = "key";
        JSONObject jsonObject = new JSONObject();
        // When
        jsonObject.put(key, processedValue);
        // Then
        assertThat(jsonObject.getString(key), is(equalTo(doubleQuoted(value))));
    }

    private static String quoted(String value) {
        return "'" + value + "'";
    }

    private static String doubleQuoted(String value) {
        return "\"" + value + "\"";
    }
}

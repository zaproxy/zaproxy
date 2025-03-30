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
package org.parosproxy.paros.core.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

/** Unit test for {@link VariantUserDefined}. */
class VariantUserDefinedUnitTest {

    @Test
    void shouldHaveParametersListEmptyByDefault() {
        // Given
        VariantUserDefined variant = new VariantUserDefined();
        // When
        List<NameValuePair> parameters = variant.getParamList();
        // Then
        assertThat(parameters, is(empty()));
    }

    @Test
    void shouldFailToExtractParametersFromUndefinedMessage() {
        // Given
        VariantUserDefined variant = new VariantUserDefined();
        HttpMessage undefinedMessage = null;
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> variant.setMessage(undefinedMessage));
    }

    @ParameterizedTest
    @CsvSource({
        "0,26,1", // Entire Visible Header - Silly example, but still
        "29,29,1", // At end of header
        "0,0,1", // At start of header
        "5,8,1", // Within header range
        "30,35,1", // Whole body
        "35,35,1", // End of body
        "30,30,1", // Start of body
        "31,34,1", // Within body range
        "25,33,0", // Within header to within body
        "40,40,0", // Beyond end of body
        "33,40,0" // Within body to beyond end
    })
    void shouldProcessBoundsAsExpected(int start, int end, int expected) {
        // Given
        HttpMessage msg = createBasicMessage();
        int[][] injPoints = new int[1][];
        injPoints[0] = new int[2];
        injPoints[0][0] = start;
        injPoints[0][1] = end;
        // Since the injection points are static, set them before instantiating the variant
        VariantUserDefined.setInjectionPoints(
                msg.getRequestHeader().getURI().toString(), injPoints);
        VariantUserDefined variant = new VariantUserDefined();
        // When
        variant.setMessage(msg);
        // Then
        assertThat(variant.getParamList().size(), is(equalTo(expected)));
    }

    private static HttpMessage createBasicMessage() {
        HttpMessage message = new HttpMessage();
        try {
            message.setRequestHeader("GET / HTTP/1.1\r\n");
            message.setRequestBody("foobar");
        } catch (HttpMalformedHeaderException e) {
            throw new RuntimeException(e);
        }
        return message;
    }
}

/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.withSettings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.extension.authorization.BasicAuthorizationDetectionMethod.LogicalOperator;
import org.zaproxy.zap.network.HttpResponseBody;

@ExtendWith(MockitoExtension.class)
class BasicAuthorizationDetectionMethodUnitTest {

    private static final String RESPONSE_TARGET_TEXT = "Unauthorized";
    private static final String LOCATION_URL = "http://www.example.com/login";
    private static final int STATUS_CODE = 302;
    private static final String RESPONSE_HEADER =
            "HTTP/1.1 "
                    + STATUS_CODE
                    + " Found\n"
                    + "Content-Type: text/html; charset=utf-8\n"
                    + "Location: "
                    + LOCATION_URL
                    + "\n"
                    + "Date: Sun, 18 May 2014 16:16:45 GMT\n"
                    + "Server: Google Frontend\n"
                    + "Content-Length: 0\n"
                    + "Alternate-Protocol: 80:quic,80:quic\n";
    private static final String RESPONSE_BODY =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                    + "Pellentesque auctor nulla id turpis placerat vulputate."
                    + RESPONSE_TARGET_TEXT
                    + " Proin tempor bibendum eros rutrum. ";

    private HttpMessage message;
    private BasicAuthorizationDetectionMethod authorizationMethod;

    @BeforeEach
    void setUp() throws Exception {
        message = Mockito.mock(HttpMessage.class, withSettings().strictness(Strictness.LENIENT));
        HttpResponseHeader mockedHeader =
                Mockito.mock(
                        HttpResponseHeader.class, withSettings().strictness(Strictness.LENIENT));
        HttpResponseBody mockedBody =
                Mockito.mock(HttpResponseBody.class, withSettings().strictness(Strictness.LENIENT));
        Mockito.when(message.getResponseHeader()).thenReturn(mockedHeader);
        Mockito.when(message.getResponseBody()).thenReturn(mockedBody);
        Mockito.when(mockedBody.toString()).thenReturn(RESPONSE_BODY);
        Mockito.when(mockedHeader.getStatusCode()).thenReturn(STATUS_CODE);
        Mockito.when(mockedHeader.toString()).thenReturn(RESPONSE_HEADER);
    }

    @Test
    void shouldNotReturnUnauthorizeWhenNothingIsSetWithAnd() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(null, null, null, LogicalOperator.AND);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
    }

    @Test
    void shouldNotReturnUnauthorizeWhenNothingIsSetWithOr() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(null, "", "", LogicalOperator.OR);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
    }

    @Test
    void shouldReturnUnauthorizeWhenJustStatusCodeIsSetWithOr() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(STATUS_CODE, "", "", LogicalOperator.OR);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
    }

    @Test
    void shouldReturnUnauthorizeWhenJustBodyIsSetWithOr() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        null, "", RESPONSE_TARGET_TEXT, LogicalOperator.OR);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
    }

    @Test
    void shouldReturnUnauthorizeWhenJustHeaderIsSetWithOr() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        null, "Location: " + LOCATION_URL, null, LogicalOperator.OR);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
    }

    @Test
    void shouldNotReturnUnauthorizeWhenJustStatusCodeIsSetWithOr() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(STATUS_CODE + 1, "", "", LogicalOperator.OR);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
    }

    @Test
    void shouldNotReturnUnauthorizeWhenJustBodyIsSetWithOr() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        null, "", RESPONSE_TARGET_TEXT + "RANDOM", LogicalOperator.OR);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
    }

    @Test
    void shouldReturnNotUnauthorizeWhenJustHeaderIsSetWithOr() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        null, "Location: " + LOCATION_URL + "/extra", null, LogicalOperator.OR);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
    }

    @Test
    void shouldReturnUnauthorizeWhenJustStatusCodeIsSetWithAnd() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(STATUS_CODE, "", "", LogicalOperator.AND);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
    }

    @Test
    void shouldReturnUnauthorizeWhenJustBodyIsSetWithAnd() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        null, "", RESPONSE_TARGET_TEXT, LogicalOperator.AND);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
    }

    @Test
    void shouldReturnUnauthorizeWhenJustHeaderIsSetWithAnd() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        null, "Location: " + LOCATION_URL, null, LogicalOperator.AND);

        // When/Then
        assertEquals(true, authorizationMethod.isResponseForUnauthorizedRequest(message));
    }

    @Test
    void shouldReturnUnauthorizedWithOr() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        STATUS_CODE + 1, "", RESPONSE_TARGET_TEXT, LogicalOperator.OR);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);

        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        STATUS_CODE, null, RESPONSE_TARGET_TEXT + "?TEST", LogicalOperator.OR);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);

        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        STATUS_CODE + 1,
                        LOCATION_URL,
                        RESPONSE_TARGET_TEXT + "??",
                        LogicalOperator.OR);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
    }

    @Test
    void shouldNotReturnUnauthorizedWithOr() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        STATUS_CODE + 1, "", RESPONSE_TARGET_TEXT + "EXTRA", LogicalOperator.OR);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);

        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        STATUS_CODE + 1,
                        "Location: wrongUrl",
                        RESPONSE_TARGET_TEXT + "EXTRA",
                        LogicalOperator.OR);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
    }

    @Test
    void shouldReturnUnauthorizedWithAnd() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        STATUS_CODE, "", RESPONSE_TARGET_TEXT, LogicalOperator.AND);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);

        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        STATUS_CODE, LOCATION_URL, RESPONSE_TARGET_TEXT, LogicalOperator.AND);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
    }

    @Test
    void shouldNotReturnUnauthorizedWithAnd() {
        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        STATUS_CODE + 2, null, RESPONSE_TARGET_TEXT, LogicalOperator.AND);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);

        // Given
        authorizationMethod =
                new BasicAuthorizationDetectionMethod(
                        STATUS_CODE, "No Location", RESPONSE_TARGET_TEXT, LogicalOperator.AND);

        // When/Then
        assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
    }
}

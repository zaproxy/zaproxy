/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import net.sf.json.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.extension.api.ApiResponse;

/**
 * @author Vahid Rafiei (@vahid_r)
 */
class UsernamePasswordAuthenticationCredentialsUnitTest {

    private UsernamePasswordAuthenticationCredentials usernamePasswordAuthenticationCredentials;
    private UsernamePasswordAuthenticationCredentials notConfiguredInstance;
    private String username = "myUser";
    private String password = "myPass";

    @BeforeEach
    void setUp() {
        this.usernamePasswordAuthenticationCredentials =
                new UsernamePasswordAuthenticationCredentials(username, password);
        this.notConfiguredInstance = new UsernamePasswordAuthenticationCredentials();
    }

    @Test
    void shouldBeConfiguredIfUsernameAndPasswordAreNotNull() {
        // Given/When
        boolean isConfigured = usernamePasswordAuthenticationCredentials.isConfigured();

        // Then
        assertThat(isConfigured).isTrue();
    }

    @Test
    void shouldNotBeConfiguredIfUsernameAndPasswordAreNull() {
        // Given/When
        boolean isConfigured = notConfiguredInstance.isConfigured();

        // Then
        assertThat(isConfigured).isFalse();
    }

    @Test
    void shouldNotBeConfiguredIfPasswordIsNull() {
        // Given
        UsernamePasswordAuthenticationCredentials credentials =
                new UsernamePasswordAuthenticationCredentials(username, null);
        // When
        boolean isConfigured = credentials.isConfigured();
        // Then
        assertThat(isConfigured).isFalse();
    }

    @Test
    void shouldThrowExceptionWhileEncodeWithFieldSeparator() {
        // Given
        String fieldSeparator = "~";
        usernamePasswordAuthenticationCredentials =
                new UsernamePasswordAuthenticationCredentials(username, password);

        // When
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> usernamePasswordAuthenticationCredentials.encode(fieldSeparator));

        // Then
        assertThat(e.getMessage()).contains("separator");
    }

    @Test
    void shouldEncodeMethodReturnNullPatternIfUsernameIsNull() {
        // Given
        String nullPattern = "AA==";
        username = null;
        password = "something";
        String stringSeparator = "|";
        usernamePasswordAuthenticationCredentials =
                new UsernamePasswordAuthenticationCredentials(username, password);

        // When
        String encodedResult = usernamePasswordAuthenticationCredentials.encode(stringSeparator);

        // Then
        assertThat(encodedResult).isEqualTo(nullPattern);
    }

    @Test
    void shouldEncodeUsernameAndPasswordWithTheCorrectFieldSeparator() {
        // Given
        List<String> someCorrectSeparators = Arrays.asList("-", "|", "/", "\\", "+");

        // When/Then
        for (String correctSeparator : someCorrectSeparators) {
            String encodedUsernamePassword =
                    usernamePasswordAuthenticationCredentials.encode(correctSeparator);

            assertThat(encodedUsernamePassword)
                    .as(String.format("Failed to encode with '%s'", correctSeparator))
                    .isNotNull();
            assertThat(encodedUsernamePassword)
                    .as(String.format("Failed to properly encode with '%s'", correctSeparator))
                    .isEqualTo("bXlVc2Vy~bXlQYXNz~");
        }
    }

    @Test
    void shouldDecodeEmptyUsernameAndPassword() {
        // Given
        String encodedCredentials = "~~";
        UsernamePasswordAuthenticationCredentials authCredentials =
                new UsernamePasswordAuthenticationCredentials();
        // When
        authCredentials.decode(encodedCredentials);
        // Then
        assertThat(authCredentials.getUsername()).isEmpty();
        assertThat(authCredentials.getPassword()).isEmpty();
    }

    @Test
    void shouldApiResponseRepresentationReturnApiResponseWithValidNameAndJsonFormat() {
        // Given/When
        ApiResponse apiResponse =
                usernamePasswordAuthenticationCredentials.getApiResponseRepresentation();
        JSON jsonRepresentation = apiResponse.toJSON();

        // Then
        assertThat(apiResponse).isNotNull();
        assertThat(apiResponse.getName()).isEqualToIgnoringCase("credentials");
        assertThat(jsonRepresentation.toString())
                .contains("username")
                .contains(username)
                .contains("password")
                .contains(password)
                .contains("type")
                .contains("UsernamePasswordAuthenticationCredentials");
    }
}

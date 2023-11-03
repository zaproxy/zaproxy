/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2023 The ZAP Development Team
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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit test for {@link ApiElement}. */
class ApiElementUnitTest {

    private static final String ELEMENT_NAME = "Element Name";

    private static final List<String> DEFAULT_MANDATORY_PARAMS =
            List.of("Mandatory A", "Mandatory B", "Mandatory C");
    private static final List<String> DEFAULT_OPTIONAL_PARAMS =
            List.of("Optional 1", "Optional 2", "Optional 3");

    static class Constructor {

        @Test
        void shouldAllowValidMandatoryAndOptionalParams() {
            ApiElement api =
                    assertDoesNotThrow(
                            () ->
                                    new ApiElement(
                                            ELEMENT_NAME,
                                            DEFAULT_MANDATORY_PARAMS,
                                            DEFAULT_OPTIONAL_PARAMS));
            assertThat(api.getMandatoryParamNames(), is(equalTo(DEFAULT_MANDATORY_PARAMS)));
            assertThat(api.getOptionalParamNames(), is(equalTo(DEFAULT_OPTIONAL_PARAMS)));
        }

        @Test
        void shouldNotAllowDuplicatedMandatoryParams() {
            // Given
            String duplicatedParam = "Param A";
            List<String> mandatoryParams = List.of(duplicatedParam, "Param B", duplicatedParam);
            // When / Then
            Exception ex =
                    assertThrows(
                            IllegalArgumentException.class,
                            () ->
                                    new ApiElement(
                                            ELEMENT_NAME,
                                            mandatoryParams,
                                            DEFAULT_OPTIONAL_PARAMS));
            assertThat(
                    ex.getMessage(),
                    allOf(containsString(ELEMENT_NAME), containsString(duplicatedParam)));
        }

        @Test
        void shouldNotAllowDuplicatedOptionalParams() {
            // Given
            String duplicatedParam = "Param 1";
            List<String> optionalParams = List.of(duplicatedParam, "Param 2", duplicatedParam);
            // When / Then
            Exception ex =
                    assertThrows(
                            IllegalArgumentException.class,
                            () ->
                                    new ApiElement(
                                            ELEMENT_NAME,
                                            DEFAULT_MANDATORY_PARAMS,
                                            optionalParams));
            assertThat(
                    ex.getMessage(),
                    allOf(containsString(ELEMENT_NAME), containsString(duplicatedParam)));
        }

        @Test
        void shouldNotAllowDuplicatedMandatoryAndOptionalParams() {
            // Given
            String duplicatedParam = "Param X";
            List<String> mandatoryParams = List.of("Param A", "Param B", duplicatedParam);
            List<String> optionalParams = List.of("Param 1", "Param 2", duplicatedParam);
            // When / Then
            Exception ex =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> new ApiElement(ELEMENT_NAME, mandatoryParams, optionalParams));
            assertThat(
                    ex.getMessage(),
                    allOf(containsString(ELEMENT_NAME), containsString(duplicatedParam)));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        void shouldNotAllowNullOrBlankDefaultMethod(String defaultMethod) {
            Exception ex =
                    assertThrows(
                            IllegalArgumentException.class,
                            () ->
                                    new ApiElement(
                                            ELEMENT_NAME,
                                            defaultMethod,
                                            DEFAULT_MANDATORY_PARAMS,
                                            DEFAULT_OPTIONAL_PARAMS));
            assertThat(
                    ex.getMessage(),
                    allOf(containsString(ELEMENT_NAME), containsString("default method")));
        }
    }

    static class Setters {

        private ApiElement api;

        @BeforeEach
        void setup() {
            api = new ApiElement(ELEMENT_NAME, DEFAULT_MANDATORY_PARAMS, DEFAULT_OPTIONAL_PARAMS);
        }

        @Test
        void shouldAllowValidMandatoryParams() {
            // Given
            List<String> mandatoryParams = List.of("Param A", "Param B", "Param C");
            // When
            api.setMandatoryParamNames(mandatoryParams);
            // Then
            assertThat(api.getMandatoryParamNames(), is(equalTo(mandatoryParams)));
            assertThat(api.getOptionalParamNames(), is(equalTo(DEFAULT_OPTIONAL_PARAMS)));
        }

        @Test
        void shouldAllowValidOptionalParams() {
            // Given
            List<String> optionalParams = List.of("Param 1", "Param 2", "Param 3");
            // When
            api.setOptionalParamNames(optionalParams);
            // Then
            assertThat(api.getMandatoryParamNames(), is(equalTo(DEFAULT_MANDATORY_PARAMS)));
            assertThat(api.getOptionalParamNames(), is(equalTo(optionalParams)));
        }

        @Test
        void shouldNotAllowDuplicatedMandatoryParams() {
            // Given
            String duplicatedParam = "Param A";
            List<String> mandatoryParams = List.of(duplicatedParam, "Param B", duplicatedParam);
            // When / Then
            Exception ex =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> api.setMandatoryParamNames(mandatoryParams));
            assertThat(
                    ex.getMessage(),
                    allOf(containsString(ELEMENT_NAME), containsString(duplicatedParam)));
            assertThat(api.getMandatoryParamNames(), is(equalTo(DEFAULT_MANDATORY_PARAMS)));
            assertThat(api.getOptionalParamNames(), is(equalTo(DEFAULT_OPTIONAL_PARAMS)));
        }

        @Test
        void shouldNotAllowMandatoryDuplicatingOptionalParams() {
            // Given
            String duplicatedParam = "Optional 1";
            List<String> mandatoryParams = List.of("Param A", "Param B", duplicatedParam);
            // When / Then
            Exception ex =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> api.setMandatoryParamNames(mandatoryParams));
            assertThat(
                    ex.getMessage(),
                    allOf(containsString(ELEMENT_NAME), containsString(duplicatedParam)));
            assertThat(api.getMandatoryParamNames(), is(equalTo(DEFAULT_MANDATORY_PARAMS)));
            assertThat(api.getOptionalParamNames(), is(equalTo(DEFAULT_OPTIONAL_PARAMS)));
        }

        @Test
        void shouldNotAllowDuplicatedOptionalParams() {
            // Given
            String duplicatedParam = "Param 1";
            List<String> optionalParams = List.of(duplicatedParam, "Param 2", duplicatedParam);
            // When / Then
            Exception ex =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> api.setOptionalParamNames(optionalParams));
            assertThat(
                    ex.getMessage(),
                    allOf(containsString(ELEMENT_NAME), containsString(duplicatedParam)));
            assertThat(api.getMandatoryParamNames(), is(equalTo(DEFAULT_MANDATORY_PARAMS)));
            assertThat(api.getOptionalParamNames(), is(equalTo(DEFAULT_OPTIONAL_PARAMS)));
        }

        @Test
        void shouldNotAllowOptionalDuplicatingMandatoryParams() {
            // Given
            String duplicatedParam = "Mandatory A";
            List<String> mandatoryParams = List.of("Param 1", "Param 2", duplicatedParam);
            // When / Then
            Exception ex =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> api.setOptionalParamNames(mandatoryParams));
            assertThat(
                    ex.getMessage(),
                    allOf(containsString(ELEMENT_NAME), containsString(duplicatedParam)));
            assertThat(api.getMandatoryParamNames(), is(equalTo(DEFAULT_MANDATORY_PARAMS)));
            assertThat(api.getOptionalParamNames(), is(equalTo(DEFAULT_OPTIONAL_PARAMS)));
        }
    }
}

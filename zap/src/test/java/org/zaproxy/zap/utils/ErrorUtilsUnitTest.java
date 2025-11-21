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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.Constant;

public class ErrorUtilsUnitTest {

    @BeforeAll
    static void beforeAll() {
        Constant.messages = mock(I18N.class);
    }

    @AfterAll
    static void afterAll() {
        Constant.messages = null;
    }

    @BeforeEach
    void setup() throws Exception {
        ErrorUtils.setOutOfDiskSpaceHandler(null);
        ErrorUtils.getOutOfDiskSpaceHandler().setExitOnOutOfSpace(false);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "Data File size limit is reached",
                "No space left on device, bad luck",
                "There is not enough space on the disk",
                "There was a file input/output error"
            })
    void shouldHandleDiskSpaceException(String message) {
        // Given
        Exception e = new Exception("Test", new Exception(message));
        // When / Then
        assertThat(ErrorUtils.handleDiskSpaceException(e), is(equalTo(true)));
        assertThat(ErrorUtils.getOutOfDiskSpaceHandler().isOutOfSpace(), is(equalTo(true)));
    }

    @Test
    void shouldIgnoreNonDiskSpaceException() {
        // Given
        Exception e = new Exception("Test", new Exception("Not a disk space exception"));
        // When / Then
        assertThat(ErrorUtils.handleDiskSpaceException(e), is(equalTo(false)));
        assertThat(ErrorUtils.getOutOfDiskSpaceHandler().isOutOfSpace(), is(equalTo(false)));
    }

    @Test
    void shouldSetOutOfDiskSpaceHandler() {
        // Given
        OutOfDiskSpaceHandler handler = mock(OutOfDiskSpaceHandler.class);
        ErrorUtils.setOutOfDiskSpaceHandler(handler);
        Exception e = new Exception("Test", new Exception("Data File size limit is reached"));
        // When / Then
        assertThat(ErrorUtils.handleDiskSpaceException(e), is(equalTo(false)));
        verify(handler, times(1)).handleDiskSpaceException(e);
    }

    @Test
    void shouldReturnTrueIfCause() {
        // Given
        Exception e = new Exception("Test", new Exception("This exception has a cause"));
        // When / Then
        assertThat(ErrorUtils.hasCause(e, "has a"), is(equalTo(true)));
    }

    @Test
    void shouldReturnFalseIfDifferentCause() {
        // Given
        Exception e = new Exception("Test", new Exception("This exception has a cause"));
        // When / Then
        assertThat(ErrorUtils.hasCause(e, "not in the cause"), is(equalTo(false)));
    }

    @Test
    void shouldReturnFalseIfCauseHasNoMessage() {
        // Given
        Exception e = new Exception("Test", new Exception());
        // When / Then
        assertThat(ErrorUtils.hasCause(e, "no message in the cause"), is(equalTo(false)));
    }

    @Test
    void shouldReturnFalseIfNoCause() {
        // Given
        Exception e = new Exception("Test");
        // When / Then
        assertThat(ErrorUtils.hasCause(e, "Test"), is(equalTo(false)));
    }
}

/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.extension.brk;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.zaproxy.zap.extension.httppanel.Message;

/** Unit test for {@link BreakpointMessageHandler2}. */
class BreakpointMessageHandler2UnitTest {

    private BreakpointManagementInterface breakpointManagementInterface;

    private BreakpointMessageHandler2 breakpointMessageHandler;

    @BeforeEach
    void setup() {
        breakpointManagementInterface = mock(BreakpointManagementInterface.class);
        breakpointMessageHandler = new BreakpointMessageHandler2(breakpointManagementInterface);
        breakpointMessageHandler.setEnabledBreakpoints(Collections.emptyList());
        breakpointMessageHandler.setEnabledIgnoreRules(Collections.emptyList());
    }

    @ParameterizedTest
    @MethodSource("requestAndOnlyIfInScope")
    void shouldBeBreakpointIfMessageForceIntercept(boolean request, boolean onlyIfInScope) {
        // Given
        Message message = mock(Message.class);
        given(message.isForceIntercept()).willReturn(true);
        // When
        boolean breakpoint = breakpointMessageHandler.isBreakpoint(message, request, onlyIfInScope);
        // Then
        assertThat(breakpoint, is(equalTo(true)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldNotBeBreakpointIfIgnoreRuleMatch(boolean request) {
        // Given
        Message message = mock(Message.class);
        given(message.isInScope()).willReturn(true);
        given(breakpointManagementInterface.isBreakRequest()).willReturn(true);
        given(breakpointManagementInterface.isBreakResponse()).willReturn(true);

        BreakpointMessageInterface skipBreakpoint = mock(BreakpointMessageInterface.class);
        given(skipBreakpoint.isEnabled()).willReturn(true);
        given(skipBreakpoint.match(message, request, false)).willReturn(true);
        List<BreakpointMessageInterface> ignoreRules = Arrays.asList(skipBreakpoint);
        breakpointMessageHandler.setEnabledIgnoreRules(ignoreRules);
        // When
        boolean breakpoint = breakpointMessageHandler.isBreakpoint(message, request, false);
        // Then
        assertThat(breakpoint, is(equalTo(false)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldBeBreakpointIfIgnoreRulesDoNotMatch(boolean request) {
        // Given
        Message message = mock(Message.class);
        given(message.isInScope()).willReturn(true);
        given(breakpointManagementInterface.isBreakRequest()).willReturn(true);
        given(breakpointManagementInterface.isBreakResponse()).willReturn(true);

        BreakpointMessageInterface skipBreakpoint = mock(BreakpointMessageInterface.class);
        given(skipBreakpoint.isEnabled()).willReturn(true);
        given(skipBreakpoint.match(message, request, false)).willReturn(false);
        List<BreakpointMessageInterface> ignoreRules = Arrays.asList(skipBreakpoint);
        breakpointMessageHandler.setEnabledIgnoreRules(ignoreRules);
        // When
        boolean breakpoint = breakpointMessageHandler.isBreakpoint(message, request, false);
        // Then
        assertThat(breakpoint, is(equalTo(true)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldBeBreakpointIfIgnoreRulesMatchButNotEnable(boolean request) {
        // Given
        Message message = mock(Message.class);
        given(message.isInScope()).willReturn(true);
        given(breakpointManagementInterface.isBreakRequest()).willReturn(true);
        given(breakpointManagementInterface.isBreakResponse()).willReturn(true);

        BreakpointMessageInterface skipBreakpoint = mock(BreakpointMessageInterface.class);
        given(skipBreakpoint.isEnabled()).willReturn(false);
        given(skipBreakpoint.match(message, request, false)).willReturn(true);
        List<BreakpointMessageInterface> ignoreRules = Arrays.asList(skipBreakpoint);
        breakpointMessageHandler.setEnabledIgnoreRules(ignoreRules);
        // When
        boolean breakpoint = breakpointMessageHandler.isBreakpoint(message, request, false);
        // Then
        assertThat(breakpoint, is(equalTo(true)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldNotFailWithoutIgnoreRules(boolean request) {
        // Given
        Message message = mock(Message.class);
        given(message.isInScope()).willReturn(false);
        boolean onlyIfInScope = true;
        breakpointMessageHandler.setEnabledIgnoreRules(null);
        // When
        boolean breakpoint =
                assertDoesNotThrow(
                        () ->
                                breakpointMessageHandler.isBreakpoint(
                                        message, request, onlyIfInScope));
        // Then
        assertThat(breakpoint, is(equalTo(false)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldNotBeBreakpointIfMessageNotInScopeWithOnlyIfInScope(boolean request) {
        // Given
        Message message = mock(Message.class);
        given(message.isInScope()).willReturn(false);
        boolean onlyIfInScope = true;
        // When
        boolean breakpoint = breakpointMessageHandler.isBreakpoint(message, request, onlyIfInScope);
        // Then
        assertThat(breakpoint, is(equalTo(false)));
    }

    @Test
    void shouldBeBreakpointIfRequestAndBreakingOnAllRequests() {
        // Given
        Message message = mock(Message.class);
        given(breakpointManagementInterface.isBreakRequest()).willReturn(true);
        boolean request = true;
        // When
        boolean breakpoint = breakpointMessageHandler.isBreakpoint(message, request, false);
        // Then
        assertThat(breakpoint, is(equalTo(true)));
    }

    @Test
    void shouldNotBeBreakpointIfNotRequestAndBreakingOnAllRequests() {
        // Given
        Message message = mock(Message.class);
        given(breakpointManagementInterface.isBreakRequest()).willReturn(true);
        boolean request = false;
        // When
        boolean breakpoint = breakpointMessageHandler.isBreakpoint(message, request, false);
        // Then
        assertThat(breakpoint, is(equalTo(false)));
    }

    @Test
    void shouldBeBreakpointIfResponseAndBreakingOnAllResponses() {
        // Given
        Message message = mock(Message.class);
        given(breakpointManagementInterface.isBreakResponse()).willReturn(true);
        boolean request = false;
        // When
        boolean breakpoint = breakpointMessageHandler.isBreakpoint(message, request, false);
        // Then
        assertThat(breakpoint, is(equalTo(true)));
    }

    @Test
    void shouldNotBeBreakpointIfNotResponseAndBreakingOnAllResponses() {
        // Given
        Message message = mock(Message.class);
        given(breakpointManagementInterface.isBreakResponse()).willReturn(true);
        boolean request = true;
        // When
        boolean breakpoint = breakpointMessageHandler.isBreakpoint(message, request, false);
        // Then
        assertThat(breakpoint, is(equalTo(false)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldBeBreakpointIfStepping(boolean request) {
        // Given
        Message message = mock(Message.class);
        given(breakpointManagementInterface.isStepping()).willReturn(true);
        // When
        boolean breakpoint = breakpointMessageHandler.isBreakpoint(message, request, false);
        // Then
        assertThat(breakpoint, is(equalTo(true)));
    }

    @ParameterizedTest
    @MethodSource("requestAndOnlyIfInScope")
    void shouldNotBeBreakpointIfBreakpointNotHit(boolean request, boolean onlyIfInScope) {
        // Given
        Message message = mock(Message.class);
        given(message.isInScope()).willReturn(onlyIfInScope);
        BreakpointMessageInterface breakpointMessage = mock(BreakpointMessageInterface.class);
        given(breakpointMessage.match(message, request, onlyIfInScope)).willReturn(false);
        breakpointMessageHandler.setEnabledBreakpoints(asList(breakpointMessage));
        // When
        boolean breakpoint = breakpointMessageHandler.isBreakpoint(message, request, onlyIfInScope);
        // Then
        assertThat(breakpoint, is(equalTo(false)));
    }

    @ParameterizedTest
    @MethodSource("requestAndOnlyIfInScope")
    void shouldBeBreakpointIfAtLeastOneBreakpointHit(boolean request, boolean onlyIfInScope) {
        // Given
        Message message = mock(Message.class);
        given(message.isInScope()).willReturn(onlyIfInScope);
        BreakpointMessageInterface breakpointMessage = mock(BreakpointMessageInterface.class);
        given(breakpointMessage.match(message, request, onlyIfInScope)).willReturn(true);
        breakpointMessageHandler.setEnabledBreakpoints(
                asList(mock(BreakpointMessageInterface.class), breakpointMessage));
        // When
        boolean breakpoint = breakpointMessageHandler.isBreakpoint(message, request, onlyIfInScope);
        // Then
        assertThat(breakpoint, is(equalTo(true)));
    }

    static Stream<Arguments> requestAndOnlyIfInScope() {
        return Stream.of(
                arguments(true, true),
                arguments(true, false),
                arguments(false, false),
                arguments(false, true));
    }
}

/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthPollFrequencyUnits;
import org.zaproxy.zap.users.AuthenticationState;
import org.zaproxy.zap.users.User;

/** Unit test for {@link VerificationMethod}. */
class VerificationMethodUnitTest {

    private VerificationMethod vm;

    @BeforeEach
    void setUp() {
        vm = new VerificationMethod();
    }

    @Test
    void shouldHaveExpectedDefaults() {
        assertThat(vm.getAuthCheckingStrategy(), is(AuthCheckingStrategy.POLL_URL));
        assertThat(vm.getPollFrequencyUnits(), is(AuthPollFrequencyUnits.SECONDS));
        assertThat(vm.getPollFrequency(), is(VerificationMethod.DEFAULT_POLL_FREQUENCY));
        assertThat(vm.getLoggedInIndicatorPattern(), is(nullValue()));
        assertThat(vm.getLoggedOutIndicatorPattern(), is(nullValue()));
    }

    @Nested
    class SetAuthCheckingStrategy {

        @Test
        void shouldThrowOnNull() {
            assertThrows(NullPointerException.class, () -> vm.setAuthCheckingStrategy(null));
        }
    }

    @Nested
    class IndicatorPatterns {

        @Test
        void shouldStoreNonEmptyLoggedInIndicator() {
            vm.setLoggedInIndicatorPattern("loggedin");
            assertThat(vm.getLoggedInIndicatorPattern().pattern(), is("loggedin"));
        }

        @Test
        void shouldStoreNonEmptyLoggedOutIndicator() {
            vm.setLoggedOutIndicatorPattern("loggedout");
            assertThat(vm.getLoggedOutIndicatorPattern().pattern(), is("loggedout"));
        }

        @Test
        void shouldClearLoggedInIndicatorWhenSetToNull() {
            vm.setLoggedInIndicatorPattern("loggedin");
            vm.setLoggedInIndicatorPattern(null);
            assertThat(vm.getLoggedInIndicatorPattern(), is(nullValue()));
        }

        @Test
        void shouldClearLoggedOutIndicatorWhenSetToNull() {
            vm.setLoggedOutIndicatorPattern("loggedout");
            vm.setLoggedOutIndicatorPattern(null);
            assertThat(vm.getLoggedOutIndicatorPattern(), is(nullValue()));
        }

        @Test
        void shouldClearLoggedInIndicatorWhenSetToBlank() {
            vm.setLoggedInIndicatorPattern("loggedin");
            vm.setLoggedInIndicatorPattern("   ");
            assertThat(vm.getLoggedInIndicatorPattern(), is(nullValue()));
        }

        @Test
        void shouldClearLoggedOutIndicatorWhenSetToBlank() {
            vm.setLoggedOutIndicatorPattern("loggedout");
            vm.setLoggedOutIndicatorPattern("   ");
            assertThat(vm.getLoggedOutIndicatorPattern(), is(nullValue()));
        }
    }

    @Nested
    class IsAuthenticated {

        @Test
        void shouldReturnFalseWhenMessageNull() {
            User user = mock(User.class);
            assertThat(vm.isAuthenticated(null, user), is(false));
        }

        @Test
        void shouldReturnFalseWhenUserNull() throws Exception {
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            assertThat(vm.isAuthenticated(msg, null), is(false));
        }

        @Test
        void shouldReturnFalseWhenStrategyIsAutoDetect() throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.AUTO_DETECT);
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            User user = mock(User.class);
            assertThat(vm.isAuthenticated(msg, user), is(false));
        }

        @Test
        void shouldReturnTrueWhenNoIndicatorsSet() throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            msg.setResponseBody("some body");
            User user = mock(User.class);
            given(user.getAuthenticationState()).willReturn(new AuthenticationState());
            assertThat(vm.isAuthenticated(msg, user), is(true));
        }

        @Test
        void shouldReturnTrueWhenResponseContainsLoggedInIndicator() throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
            vm.setLoggedInIndicatorPattern("loggedin");
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            msg.setResponseBody("The user is loggedin again.");
            User user = mock(User.class);
            given(user.getAuthenticationState()).willReturn(new AuthenticationState());
            assertThat(vm.isAuthenticated(msg, user), is(true));
        }

        @Test
        void shouldReturnFalseWhenResponseContainsLoggedOutIndicator() throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
            vm.setLoggedOutIndicatorPattern("loggedout");
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            msg.setResponseBody("The user is loggedout this time.");
            User user = mock(User.class);
            given(user.getAuthenticationState()).willReturn(new AuthenticationState());
            assertThat(vm.isAuthenticated(msg, user), is(false));
        }

        @Test
        void shouldReturnTrueWhenResponseLacksLoggedOutIndicator() throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
            vm.setLoggedOutIndicatorPattern("loggedout");
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            msg.setResponseBody("Welcome back, user.");
            User user = mock(User.class);
            given(user.getAuthenticationState()).willReturn(new AuthenticationState());
            assertThat(vm.isAuthenticated(msg, user), is(true));
        }

        @Test
        void shouldReturnFalseWhenResponseLacksLoggedInIndicatorAndNoLoggedOutIndicatorSet()
                throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
            vm.setLoggedInIndicatorPattern("loggedin");
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            msg.setResponseBody("Welcome, you are logged out.");
            User user = mock(User.class);
            given(user.getAuthenticationState()).willReturn(new AuthenticationState());
            assertThat(vm.isAuthenticated(msg, user), is(false));
        }

        @Test
        void shouldReturnTrueWhenResponseHeaderContainsLoggedInIndicator() throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
            vm.setLoggedInIndicatorPattern("loggedin");
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            msg.getResponseHeader().addHeader("X-Test", "loggedin");
            User user = mock(User.class);
            given(user.getAuthenticationState()).willReturn(new AuthenticationState());
            assertThat(vm.isAuthenticated(msg, user), is(true));
        }

        @Test
        void shouldReturnFalseWhenResponseHeaderContainsLoggedOutIndicator() throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
            vm.setLoggedOutIndicatorPattern("loggedout");
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            msg.getResponseHeader().addHeader("X-Test", "loggedout");
            User user = mock(User.class);
            given(user.getAuthenticationState()).willReturn(new AuthenticationState());
            assertThat(vm.isAuthenticated(msg, user), is(false));
        }

        @Test
        void shouldReturnFalseWhenResponseMatchesComplexLoggedOutRegex() throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
            vm.setLoggedOutIndicatorPattern("User [^\\s]* logged out");
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            msg.setResponseBody("Some text. User Test logged out. More text.");
            User user = mock(User.class);
            given(user.getAuthenticationState()).willReturn(new AuthenticationState());
            assertThat(vm.isAuthenticated(msg, user), is(false));
        }

        @Test
        void shouldReturnTrueWhenResponseDoesNotMatchComplexLoggedOutRegex() throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
            vm.setLoggedOutIndicatorPattern("User [^\\s]* logged out");
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            msg.setResponseBody("Welcome, you are logged out.");
            User user = mock(User.class);
            given(user.getAuthenticationState()).willReturn(new AuthenticationState());
            assertThat(vm.isAuthenticated(msg, user), is(true));
        }

        @Test
        void shouldReturnTrueWhenRequestBodyContainsLoggedInIndicatorAndStrategyIsEachReq()
                throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
            vm.setLoggedInIndicatorPattern("loggedin");
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            msg.setRequestBody("The user is loggedin again.");
            User user = mock(User.class);
            given(user.getAuthenticationState()).willReturn(new AuthenticationState());
            assertThat(vm.isAuthenticated(msg, user), is(true));
        }

        @Test
        void shouldReturnTrueWhenRequestHeaderContainsLoggedInIndicatorAndStrategyIsEachReq()
                throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
            vm.setLoggedInIndicatorPattern("loggedin");
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            msg.getRequestHeader().addHeader("X-Test", "loggedin");
            User user = mock(User.class);
            given(user.getAuthenticationState()).willReturn(new AuthenticationState());
            assertThat(vm.isAuthenticated(msg, user), is(true));
        }

        @Test
        void shouldReturnFalseWhenRequestBodyContainsLoggedOutIndicatorAndStrategyIsEachReq()
                throws Exception {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
            vm.setLoggedOutIndicatorPattern("loggedout");
            HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
            msg.setRequestBody("The user is loggedout this time.");
            User user = mock(User.class);
            given(user.getAuthenticationState()).willReturn(new AuthenticationState());
            assertThat(vm.isAuthenticated(msg, user), is(false));
        }
    }

    @Nested
    class PollAsUser {

        @Test
        void shouldThrowWhenStrategyIsNotPollUrl() {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
            User user = mock(User.class);
            assertThrows(IllegalArgumentException.class, () -> vm.pollAsUser(user));
        }

        @Test
        void shouldThrowWhenPollUrlIsNull() {
            User user = mock(User.class);
            IllegalArgumentException ex =
                    assertThrows(IllegalArgumentException.class, () -> vm.pollAsUser(user));
            assertThat(ex.getMessage(), is("Poll URL is not set"));
        }

        @Test
        void shouldThrowWhenPollUrlIsBlank() {
            vm.setPollUrl("   ");
            User user = mock(User.class);
            IllegalArgumentException ex =
                    assertThrows(IllegalArgumentException.class, () -> vm.pollAsUser(user));
            assertThat(ex.getMessage(), is("Poll URL is not set"));
        }

        @Test
        void shouldInvokeUserDataReplacerBeforeSending() {
            // Given
            List<HttpMessage> replacedMessages = new ArrayList<>();
            vm.setPollUrl("http://127.0.0.1:1/");
            vm.setUserDataReplacer((msg, user) -> replacedMessages.add(msg));
            User user = mock(User.class);
            // When
            assertThrows(IOException.class, () -> vm.pollAsUser(user));
            // Then
            assertThat(replacedMessages, hasSize(1));
        }
    }

    @Nested
    class Copy {

        @Test
        void shouldCopyAllFields() {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
            vm.setPollUrl("http://example.com/poll");
            vm.setPollData("data=value");
            vm.setPollHeaders("X-Header: value");
            vm.setPollFrequency(30);
            vm.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
            vm.setLoggedInIndicatorPattern("loggedin");
            vm.setLoggedOutIndicatorPattern("loggedout");

            VerificationMethod copy = vm.copy(null);

            assertThat(copy, is(not(sameInstance(vm))));
            assertThat(copy.getAuthCheckingStrategy(), is(AuthCheckingStrategy.EACH_REQ));
            assertThat(copy.getPollUrl(), is("http://example.com/poll"));
            assertThat(copy.getPollData(), is("data=value"));
            assertThat(copy.getPollHeaders(), is("X-Header: value"));
            assertThat(copy.getPollFrequency(), is(30));
            assertThat(copy.getPollFrequencyUnits(), is(AuthPollFrequencyUnits.REQUESTS));
            assertThat(copy.getLoggedInIndicatorPattern().pattern(), is("loggedin"));
            assertThat(copy.getLoggedOutIndicatorPattern().pattern(), is("loggedout"));
        }

        @Test
        void shouldBeIndependentOfOriginal() {
            vm.setLoggedInIndicatorPattern("original");
            VerificationMethod copy = vm.copy(null);

            vm.setLoggedInIndicatorPattern("modified");

            assertThat(copy.getLoggedInIndicatorPattern().pattern(), is("original"));
        }

        @Test
        void shouldSetProvidedUserDataReplacer() {
            java.util.function.BiConsumer<HttpMessage, User> replacer = (msg, user) -> {};

            VerificationMethod copy = vm.copy(replacer);

            assertThat(copy.getUserDataReplacer(), is(sameInstance(replacer)));
        }
    }

    @Nested
    class Equals {

        @Test
        void shouldBeEqualToItself() {
            assertThat(vm.equals(vm), is(equalTo(true)));
        }

        @Test
        void shouldBeEqualToMethodWithSameContents() {
            VerificationMethod other = createMethod("loggedin", "loggedout");
            vm = createMethod("loggedin", "loggedout");
            assertThat(vm.equals(other) & other.equals(vm), is(true));
        }

        @Test
        void shouldBeEqualWhenBothIndicatorsNull() {
            VerificationMethod other = new VerificationMethod();
            assertThat(vm.equals(other) & other.equals(vm), is(true));
        }

        @Test
        void shouldBeEqualWhenLoggedInIndicatorNull() {
            vm = createMethod(null, "loggedout");
            VerificationMethod other = createMethod(null, "loggedout");
            assertThat(vm.equals(other) & other.equals(vm), is(true));
        }

        @Test
        void shouldBeEqualWhenLoggedOutIndicatorNull() {
            vm = createMethod("loggedin", null);
            VerificationMethod other = createMethod("loggedin", null);
            assertThat(vm.equals(other) & other.equals(vm), is(true));
        }

        @Test
        void shouldNotBeEqualToNull() {
            assertThat(vm.equals(null), is(false));
        }

        @Test
        void shouldNotBeEqualWhenLoggedInIndicatorDiffers() {
            vm = createMethod("loggedinA", "loggedout");
            VerificationMethod other = createMethod("loggedinB", "loggedout");
            assertThat(vm.equals(other) | other.equals(vm), is(false));
        }

        @Test
        void shouldNotBeEqualWhenOneLoggedInIndicatorIsNull() {
            vm = createMethod("loggedin", "loggedout");
            VerificationMethod other = createMethod(null, "loggedout");
            assertThat(vm.equals(other) | other.equals(vm), is(false));
        }

        @Test
        void shouldNotBeEqualWhenLoggedOutIndicatorDiffers() {
            vm = createMethod("loggedin", "loggedoutA");
            VerificationMethod other = createMethod("loggedin", "loggedoutB");
            assertThat(vm.equals(other) | other.equals(vm), is(false));
        }

        @Test
        void shouldNotBeEqualWhenOneLoggedOutIndicatorIsNull() {
            vm = createMethod("loggedin", "loggedout");
            VerificationMethod other = createMethod("loggedin", null);
            assertThat(vm.equals(other) | other.equals(vm), is(false));
        }

        @Test
        void shouldNotBeEqualWhenStrategyDiffers() {
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
            VerificationMethod other = new VerificationMethod();
            other.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
            assertThat(vm.equals(other) | other.equals(vm), is(false));
        }

        @Test
        void shouldNotBeEqualWhenPollUrlDiffers() {
            vm.setPollUrl("http://example.com/a");
            VerificationMethod other = new VerificationMethod();
            other.setPollUrl("http://example.com/b");
            assertThat(vm.equals(other) | other.equals(vm), is(false));
        }

        @Test
        void shouldNotBeEqualWhenPollFrequencyDiffers() {
            vm.setPollFrequency(10);
            VerificationMethod other = new VerificationMethod();
            other.setPollFrequency(20);
            assertThat(vm.equals(other) | other.equals(vm), is(false));
        }

        @Test
        void shouldNotBeEqualWhenPollFrequencyUnitsDiffer() {
            vm.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
            VerificationMethod other = new VerificationMethod();
            other.setPollFrequencyUnits(AuthPollFrequencyUnits.SECONDS);
            assertThat(vm.equals(other) | other.equals(vm), is(false));
        }
    }

    @Nested
    class HashCode {

        @Test
        void shouldBeEqualForEqualMethods() {
            vm = createMethod("loggedin", "loggedout");
            VerificationMethod other = createMethod("loggedin", "loggedout");
            assertThat(vm.hashCode(), is(equalTo(other.hashCode())));
        }

        @Test
        void shouldBeEqualForEqualMethodsWithSamePollSettings() {
            vm = createMethod("loggedin", "loggedout");
            vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
            vm.setPollUrl("http://example.com/poll");
            vm.setPollData("a=b");
            vm.setPollHeaders("X-Test: 1");
            vm.setPollFrequency(10);
            vm.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);

            VerificationMethod other = createMethod("loggedin", "loggedout");
            other.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
            other.setPollUrl("http://example.com/poll");
            other.setPollData("a=b");
            other.setPollHeaders("X-Test: 1");
            other.setPollFrequency(10);
            other.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);

            assertThat(vm.equals(other), is(true));
            assertThat(vm.hashCode(), is(equalTo(other.hashCode())));
        }
    }

    private static VerificationMethod createMethod(String loggedIn, String loggedOut) {
        VerificationMethod m = new VerificationMethod();
        m.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
        m.setLoggedInIndicatorPattern(loggedIn);
        m.setLoggedOutIndicatorPattern(loggedOut);
        return m;
    }
}

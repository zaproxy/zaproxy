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
package org.zaproxy.zap.extension.authentication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.authentication.AuthenticationMethod;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthPollFrequencyUnits;
import org.zaproxy.zap.authentication.FormBasedAuthenticationMethodType;
import org.zaproxy.zap.authentication.FormBasedAuthenticationMethodType.FormBasedAuthenticationMethod;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link ExtensionAuthentication}. */
class ExtensionAuthenticationUnitTest extends WithConfigsTest {

    private ExtensionAuthentication extensionAuthentication;

    @BeforeEach
    void setup() {
        extensionAuthentication = new ExtensionAuthentication();
    }

    @Test
    void shouldImportContextWithNoAuthenticationMethod() throws ConfigurationException {
        // Given
        Context context = mock(Context.class);
        Configuration config = new ZapXmlConfiguration();
        // When
        extensionAuthentication.importContextData(context, config);
        // Then
        verify(context, times(0)).setAuthenticationMethod(any());
    }

    @Test
    void shouldImportContextWithUnknownAuthenticationMethod() throws ConfigurationException {
        // Given
        Context context = mock(Context.class);
        Configuration config = new ZapXmlConfiguration();
        config.setProperty("context.authentication.type", Integer.MIN_VALUE);
        // When
        extensionAuthentication.importContextData(context, config);
        // Then
        verify(context, times(0)).setAuthenticationMethod(any());
    }

    @Test
    void shouldExportAllAuthContextData() {
        // Given
        Context context = new Context(null, 0);
        String loggedInIndicator = "logged in";
        String loggedOutIndicator = "logged out";
        String pollUrl = "https://www.example.com/poll";
        String pollData = "example-poll-data";
        String pollHeaders = "aaa : bbb\\Nccc : ddd";
        int pollFreq = 55;

        FormBasedAuthenticationMethodType type = new FormBasedAuthenticationMethodType();
        FormBasedAuthenticationMethod method = type.createAuthenticationMethod(0);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        method.setPollUrl(pollUrl);
        method.setPollData(pollData);
        method.setPollHeaders(pollHeaders);
        method.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
        method.setPollFrequency(pollFreq);
        method.setLoggedInIndicatorPattern(loggedInIndicator);
        method.setLoggedOutIndicatorPattern(loggedOutIndicator);
        context.setAuthenticationMethod(method);
        Configuration config = new ZapXmlConfiguration();

        // When
        extensionAuthentication.exportContextData(context, config);

        // Then
        assertThat(config.getInt(AuthenticationMethod.CONTEXT_CONFIG_AUTH_TYPE), is(2));
        assertThat(
                config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_STRATEGY),
                is(AuthCheckingStrategy.POLL_URL.name()));
        assertThat(
                config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_URL), is(pollUrl));
        assertThat(
                config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_DATA), is(pollData));
        assertThat(
                config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_HEADERS),
                is(pollHeaders));
        assertThat(config.getInt(AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_FREQ), is(pollFreq));
        assertThat(
                config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_UNITS),
                is(AuthPollFrequencyUnits.REQUESTS.name()));
        assertThat(
                config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDIN),
                is(loggedInIndicator));
        assertThat(
                config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDOUT),
                is(loggedOutIndicator));
    }

    @Test
    void shouldImportAllAuthContextData() throws ConfigurationException {
        // Given
        Context context = new Context(null, 0);
        String loggedInIndicator = "logged in";
        String loggedOutIndicator = "logged out";
        String pollUrl = "https://www.example.com/poll";
        String pollData = "example-poll-data";
        String pollHeaders = "aaa : bbb\\Nccc : ddd";
        int pollFreq = 55;

        Configuration config = new ZapXmlConfiguration();
        config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_TYPE, 2);
        config.setProperty(
                AuthenticationMethod.CONTEXT_CONFIG_AUTH_STRATEGY,
                AuthCheckingStrategy.POLL_URL.name());
        config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_URL, pollUrl);
        config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_DATA, pollData);
        config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_HEADERS, pollHeaders);
        config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_FREQ, pollFreq);
        config.setProperty(
                AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_UNITS,
                AuthPollFrequencyUnits.REQUESTS.name());
        config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDIN, loggedInIndicator);
        config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDOUT, loggedOutIndicator);

        ExtensionHook hook = new ExtensionHook(Model.getSingleton(), null);
        extensionAuthentication.hook(hook);

        // When
        extensionAuthentication.importContextData(context, config);
        AuthenticationMethod method = context.getAuthenticationMethod();

        // Then
        assertThat(
                method.getClass().getCanonicalName(),
                is(FormBasedAuthenticationMethod.class.getCanonicalName()));
        assertThat(method.getAuthCheckingStrategy(), is(AuthCheckingStrategy.POLL_URL));
        assertThat(method.getPollUrl(), is(pollUrl));
        assertThat(method.getPollData(), is(pollData));
        assertThat(method.getPollHeaders(), is(pollHeaders));
        assertThat(method.getPollFrequencyUnits(), is(AuthPollFrequencyUnits.REQUESTS));
        assertThat(method.getPollFrequency(), is(pollFreq));
        assertThat(method.getLoggedInIndicatorPattern().toString(), is(loggedInIndicator));
        assertThat(method.getLoggedOutIndicatorPattern().toString(), is(loggedOutIndicator));
    }

    @Test
    void shouldImportContextWithNoPollData() throws ConfigurationException {
        // Given
        Context context = new Context(null, 0);
        String loggedInIndicator = "logged in";
        String loggedOutIndicator = "logged out";

        Configuration config = new ZapXmlConfiguration();
        config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_TYPE, 2);
        config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDIN, loggedInIndicator);
        config.setProperty(AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDOUT, loggedOutIndicator);

        ExtensionHook hook = new ExtensionHook(Model.getSingleton(), null);
        extensionAuthentication.hook(hook);

        // When
        extensionAuthentication.importContextData(context, config);
        AuthenticationMethod method = context.getAuthenticationMethod();

        // Then
        assertThat(
                method.getClass().getCanonicalName(),
                is(FormBasedAuthenticationMethod.class.getCanonicalName()));
        assertThat(method.getAuthCheckingStrategy(), is(AuthCheckingStrategy.EACH_RESP));
        assertThat(method.getLoggedInIndicatorPattern().toString(), is(loggedInIndicator));
        assertThat(method.getLoggedOutIndicatorPattern().toString(), is(loggedOutIndicator));
    }
}

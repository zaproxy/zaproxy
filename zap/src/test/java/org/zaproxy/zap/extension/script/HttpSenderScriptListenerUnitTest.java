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
package org.zaproxy.zap.extension.script;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.script.ScriptsCache.Configuration;
import org.zaproxy.zap.extension.script.ScriptsCache.InterfaceErrorMessageProvider;
import org.zaproxy.zap.extension.script.ScriptsCache.ScriptAction;

/** Unit test for {@link HttpSenderScriptListener}. */
class HttpSenderScriptListenerUnitTest extends WithConfigsTest {

    private static final String SCRIPT_TYPE = ExtensionScript.TYPE_HTTP_SENDER;
    private static final Class<HttpSenderScript> TARGET_INTERFACE = HttpSenderScript.class;

    private ExtensionScript extensionScript;
    private HttpMessage message;
    private int initiator;
    private HttpSender sender;

    @BeforeEach
    void setUp() {
        extensionScript = mock(ExtensionScript.class);
        message = mock(HttpMessage.class);
        initiator = HttpSender.MANUAL_REQUEST_INITIATOR;
        sender = mock(HttpSender.class);
    }

    @Test
    void shouldThrowExceptionIfExtensionScriptIsNull() {
        // Given
        ExtensionScript extensionScript = null;
        // When / Then
        assertThrows(
                NullPointerException.class, () -> new HttpSenderScriptListener(extensionScript));
    }

    @Test
    void shouldHaveHighestListenerOrder() {
        // Given
        HttpSenderScriptListener httpSenderScriptListener =
                new HttpSenderScriptListener(extensionScript);
        // When
        int listenerOrder = httpSenderScriptListener.getListenerOrder();
        // Then
        assertThat(listenerOrder, is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateScriptsCacheWithExpectedConfiguration() {
        // Given / When
        new HttpSenderScriptListener(extensionScript);
        // Then
        ArgumentCaptor<Configuration<HttpSenderScript>> argumentCaptor =
                ArgumentCaptor.forClass(Configuration.class);
        verify(extensionScript).createScriptsCache(argumentCaptor.capture());
        Configuration<HttpSenderScript> configuration = argumentCaptor.getValue();
        assertThat(configuration.getScriptType(), is(equalTo(SCRIPT_TYPE)));
        assertThat(configuration.getTargetInterface(), is(equalTo(TARGET_INTERFACE)));
        InterfaceErrorMessageProvider errorMessageProvider =
                configuration.getInterfaceErrorMessageProvider();
        assertThat(errorMessageProvider, is(not(nullValue())));
        assertThat(errorMessageProvider.getErrorMessage(null), is(not(nullValue())));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRefreshScriptsAndCallOnHttpRequestSend() throws Exception {
        // Given
        HttpSenderScript script = mock(TARGET_INTERFACE);
        ScriptsCache<HttpSenderScript> scriptsCache = createScriptsCache(script);
        given(extensionScript.<HttpSenderScript>createScriptsCache(any())).willReturn(scriptsCache);
        HttpSenderScriptListener httpSenderScriptListener =
                new HttpSenderScriptListener(extensionScript);
        // When
        httpSenderScriptListener.onHttpRequestSend(message, initiator, sender);
        // Then
        verify(scriptsCache, times(1)).refresh();
        verify(scriptsCache, times(1)).execute(any(ScriptAction.class));
        ArgumentCaptor<HttpSenderScriptHelper> argumentCaptor =
                ArgumentCaptor.forClass(HttpSenderScriptHelper.class);
        verify(script, times(1))
                .sendingRequest(eq(message), eq(initiator), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getHttpSender(), is(equalTo(sender)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCallOnHttpResponseReceive() throws Exception {
        // Given
        HttpSenderScript script = mock(TARGET_INTERFACE);
        ScriptsCache<HttpSenderScript> scriptsCache = createScriptsCache(script);
        given(extensionScript.<HttpSenderScript>createScriptsCache(any())).willReturn(scriptsCache);
        HttpSenderScriptListener httpSenderScriptListener =
                new HttpSenderScriptListener(extensionScript);
        // When
        httpSenderScriptListener.onHttpResponseReceive(message, initiator, sender);
        // Then
        verify(scriptsCache, times(0)).refresh();
        verify(scriptsCache, times(1)).execute(any(ScriptAction.class));
        ArgumentCaptor<HttpSenderScriptHelper> argumentCaptor =
                ArgumentCaptor.forClass(HttpSenderScriptHelper.class);
        verify(script, times(1))
                .responseReceived(eq(message), eq(initiator), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getHttpSender(), is(equalTo(sender)));
    }

    @SuppressWarnings("unchecked")
    private <T> ScriptsCache<T> createScriptsCache(T script) {
        ScriptsCache<T> scriptsCache = mock(ScriptsCache.class);
        Answer<Void> answer =
                invocation -> {
                    ScriptAction<T> action = (ScriptAction<T>) invocation.getArguments()[0];
                    action.apply(script);
                    return null;
                };
        doAnswer(answer).when(scriptsCache).execute(any(ScriptAction.class));
        return scriptsCache;
    }
}

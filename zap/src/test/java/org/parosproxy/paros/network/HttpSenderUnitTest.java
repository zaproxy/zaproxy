/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.parosproxy.paros.network;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.network.HttpSenderContext;
import org.zaproxy.zap.network.HttpSenderImpl;

/** Unit test for {@link HttpSender}. */
class HttpSenderUnitTest {

    private HttpMessage msg;
    private HttpSenderImpl<HttpSenderContext> impl;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        impl = mock(HttpSenderImpl.class);
        HttpSender.setImpl(impl);
    }

    @Test
    void shouldRestoreSavedStateAfterRemovingImplementation() {
        // Given
        Object state = mock(Object.class);
        given(impl.saveState()).willReturn(state);
        HttpSenderImpl<?> otherImpl = mock(HttpSenderImpl.class);
        // When
        HttpSender.setImpl(null);
        HttpSender.setImpl(otherImpl);
        // Then
        verify(otherImpl).restoreState(state);
    }

    @Test
    void shouldRestoreSavedStateReplacingImplementation() {
        // Given
        Object state = mock(Object.class);
        given(impl.saveState()).willReturn(state);
        HttpSenderImpl<?> otherImpl = mock(HttpSenderImpl.class);
        // When
        HttpSender.setImpl(otherImpl);
        // Then
        verify(otherImpl).restoreState(state);
    }

    @Test
    void shouldUseAndSendWithCreatedContext() throws IOException {
        // Given
        HttpSenderContext createdCtx = mock(HttpSenderContext.class);
        given(impl.createContext(any(), anyInt())).willReturn(createdCtx);
        // When
        HttpSender httpSender = new HttpSender(1);
        httpSender.sendAndReceive(msg);
        // Then
        verify(impl).createContext(httpSender, 1);
        verify(impl).getContext(httpSender);
        verify(impl).sendAndReceive(eq(createdCtx), any(), eq(msg), eq(null));
    }

    @Test
    void shouldUseAndSendWithGetContext() throws IOException {
        // Given
        HttpSenderContext createdCtx = mock(HttpSenderContext.class);
        given(impl.createContext(any(), anyInt())).willReturn(createdCtx);
        HttpSenderContext getCtx = mock(HttpSenderContext.class);
        given(impl.getContext(any())).willReturn(getCtx);
        // When
        HttpSender httpSender = new HttpSender(1);
        httpSender.sendAndReceive(msg);
        // Then
        verify(impl).createContext(httpSender, 1);
        verify(impl, times(3)).getContext(httpSender);
        verify(impl).sendAndReceive(eq(httpSender), any(), eq(msg), eq(null));
    }
}

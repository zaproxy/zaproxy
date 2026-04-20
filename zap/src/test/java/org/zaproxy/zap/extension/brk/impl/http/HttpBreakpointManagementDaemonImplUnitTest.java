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
package org.zaproxy.zap.extension.brk.impl.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.WithConfigsTest;

class HttpBreakpointManagementDaemonImplUnitTest extends WithConfigsTest {

    private static String OK_RESPONSE =
            "HTTP/1.1 200 OK"
                    + HttpHeader.CRLF
                    + "Connection: close"
                    + HttpHeader.CRLF
                    + HttpHeader.CRLF;

    private HttpBreakpointManagementDaemonImpl impl;

    @BeforeEach
    void setUp() throws Exception {
        impl = new HttpBreakpointManagementDaemonImpl();
    }

    @Test
    void shouldInitBreakPointsToFalseOnInit() {
        assertThat(impl.isBreakAll()).isFalse();
        assertThat(impl.isBreakRequest()).isFalse();
        assertThat(impl.isBreakResponse()).isFalse();
    }

    @Test
    void shouldBreakOnAllHttpRequestsAndResponses() throws HttpMalformedHeaderException {
        impl.setBreakAll(true);
        HttpMessage msg = new HttpMessage();
        assertThat(impl.isHoldMessage(msg)).isTrue();

        HttpResponseHeader resHeader = new HttpResponseHeader(OK_RESPONSE);
        msg.setResponseHeader(resHeader);
        assertThat(impl.isHoldMessage(msg)).isTrue();
    }

    @Test
    void shouldBreakOnJustHttpRequests() throws HttpMalformedHeaderException {
        impl.setBreakAllRequests(true);
        HttpMessage msg = new HttpMessage();
        assertThat(impl.isHoldMessage(msg)).isTrue();

        HttpResponseHeader resHeader = new HttpResponseHeader(OK_RESPONSE);
        msg.setResponseHeader(resHeader);
        assertThat(impl.isHoldMessage(msg)).isFalse();
    }

    @Test
    void shouldBreakOnJustHttpResponses() throws HttpMalformedHeaderException {
        impl.setBreakAllResponses(true);
        HttpMessage msg = new HttpMessage();
        assertThat(impl.isHoldMessage(msg)).isFalse();

        HttpResponseHeader resHeader = new HttpResponseHeader(OK_RESPONSE);
        msg.setResponseHeader(resHeader);
        assertThat(impl.isHoldMessage(msg)).isTrue();
    }

    @Test
    void shouldStep() throws HttpMalformedHeaderException {
        impl.setBreakAll(true);
        HttpMessage msg = new HttpMessage();
        assertThat(impl.isHoldMessage(msg)).isTrue();

        impl.step();
        assertThat(impl.isStepping()).isTrue();
        // False the first time
        assertThat(impl.isHoldMessage(msg)).isFalse();
        // Then true for subsequent times
        assertThat(impl.isHoldMessage(msg)).isTrue();
        assertThat(impl.isStepping()).isTrue();

        HttpResponseHeader resHeader = new HttpResponseHeader(OK_RESPONSE);
        msg.setResponseHeader(resHeader);

        impl.step();
        assertThat(impl.isStepping()).isTrue();
        // False the first time
        assertThat(impl.isHoldMessage(msg)).isFalse();
        // Then true for subsequent times
        assertThat(impl.isHoldMessage(msg)).isTrue();
        assertThat(impl.isStepping()).isTrue();
    }

    @Test
    void shouldClearBreaksOnContinue() throws HttpMalformedHeaderException {
        impl.setBreakAll(true);
        HttpMessage msg = new HttpMessage();
        assertThat(impl.isHoldMessage(msg)).isTrue();

        impl.cont();
        assertThat(impl.isHoldMessage(msg)).isFalse();
        assertThat(impl.isBreakAll()).isFalse();
        assertThat(impl.isBreakRequest()).isFalse();
        assertThat(impl.isBreakResponse()).isFalse();

        assertThat(impl.isHoldMessage(msg)).isFalse();
        // Deliberate duplicate check due to the side effects of stepping
        assertThat(impl.isHoldMessage(msg)).isFalse();
        assertThat(impl.isStepping()).isFalse();
    }

    @Test
    void shouldDrop() throws HttpMalformedHeaderException {
        impl.setBreakAll(true);
        HttpMessage msg = new HttpMessage();
        assertThat(impl.isHoldMessage(msg)).isTrue();

        impl.drop();
        assertThat(impl.isToBeDropped()).isTrue();
        assertThat(impl.isToBeDropped()).isFalse();
        assertThat(impl.isToBeDropped()).isFalse();

        impl.drop();
        assertThat(impl.isToBeDropped()).isTrue();
        assertThat(impl.isToBeDropped()).isFalse();
        assertThat(impl.isToBeDropped()).isFalse();
    }
}

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertFalse(impl.isBreakAll());
        assertFalse(impl.isBreakRequest());
        assertFalse(impl.isBreakResponse());
    }

    @Test
    void shouldBreakOnAllHttpRequestsAndResponses() throws HttpMalformedHeaderException {
        impl.setBreakAll(true);
        HttpMessage msg = new HttpMessage();
        assertTrue(impl.isHoldMessage(msg));

        HttpResponseHeader resHeader = new HttpResponseHeader(OK_RESPONSE);
        msg.setResponseHeader(resHeader);
        assertTrue(impl.isHoldMessage(msg));
    }

    @Test
    void shouldBreakOnJustHttpRequests() throws HttpMalformedHeaderException {
        impl.setBreakAllRequests(true);
        HttpMessage msg = new HttpMessage();
        assertTrue(impl.isHoldMessage(msg));

        HttpResponseHeader resHeader = new HttpResponseHeader(OK_RESPONSE);
        msg.setResponseHeader(resHeader);
        assertFalse(impl.isHoldMessage(msg));
    }

    @Test
    void shouldBreakOnJustHttpResponses() throws HttpMalformedHeaderException {
        impl.setBreakAllResponses(true);
        HttpMessage msg = new HttpMessage();
        assertFalse(impl.isHoldMessage(msg));

        HttpResponseHeader resHeader = new HttpResponseHeader(OK_RESPONSE);
        msg.setResponseHeader(resHeader);
        assertTrue(impl.isHoldMessage(msg));
    }

    @Test
    void shouldStep() throws HttpMalformedHeaderException {
        impl.setBreakAll(true);
        HttpMessage msg = new HttpMessage();
        assertTrue(impl.isHoldMessage(msg));

        impl.step();
        assertTrue(impl.isStepping());
        // False the first time
        assertFalse(impl.isHoldMessage(msg));
        // Then true for subsequent times
        assertTrue(impl.isHoldMessage(msg));
        assertTrue(impl.isStepping());

        HttpResponseHeader resHeader = new HttpResponseHeader(OK_RESPONSE);
        msg.setResponseHeader(resHeader);

        impl.step();
        assertTrue(impl.isStepping());
        // False the first time
        assertFalse(impl.isHoldMessage(msg));
        // Then true for subsequent times
        assertTrue(impl.isHoldMessage(msg));
        assertTrue(impl.isStepping());
    }

    @Test
    void shouldClearBreaksOnContinue() throws HttpMalformedHeaderException {
        impl.setBreakAll(true);
        HttpMessage msg = new HttpMessage();
        assertTrue(impl.isHoldMessage(msg));

        impl.cont();
        assertFalse(impl.isHoldMessage(msg));
        assertFalse(impl.isBreakAll());
        assertFalse(impl.isBreakRequest());
        assertFalse(impl.isBreakResponse());

        assertFalse(impl.isHoldMessage(msg));
        // Deliberate duplicate check due to the side effects of stepping
        assertFalse(impl.isHoldMessage(msg));
        assertFalse(impl.isStepping());
    }

    @Test
    void shouldDrop() throws HttpMalformedHeaderException {
        impl.setBreakAll(true);
        HttpMessage msg = new HttpMessage();
        assertTrue(impl.isHoldMessage(msg));

        impl.drop();
        assertTrue(impl.isToBeDropped());
        assertFalse(impl.isToBeDropped());
        assertFalse(impl.isToBeDropped());

        impl.drop();
        assertTrue(impl.isToBeDropped());
        assertFalse(impl.isToBeDropped());
        assertFalse(impl.isToBeDropped());
    }
}

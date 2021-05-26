/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.diff.Diff;
import java.util.List;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link HttpSender}. */
class HttpSenderUnitTest {

    private static final String PROXY_RESPONSE = "Proxy Response";
    private static final String SERVER_RESPONSE = "Server Response";

    private WireMockSequence proxy =
            new WireMockSequence(defaultOptions().enableBrowserProxying(true));

    private WireMockSequence server = new WireMockSequence(defaultOptions());

    @BeforeEach
    void setup() {
        proxy.start();
        server.start();
        server.stubFor(
                any(anyUrl())
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withStatusMessage("OK")
                                        .withBody(SERVER_RESPONSE)));
    }

    @AfterEach
    void teardown() {
        proxy.stop();
        server.stop();
    }

    @Test
    void shouldProxyIfEnabled() throws Exception {
        // Given
        ConnectionParam options = createOptionsWithProxy("localhost", proxy.port());
        options.setUseProxyChain(true);
        HttpMessage message = createMessage("GET", "/");
        HttpSender httpSender = new HttpSender(options, false, -1);
        // When
        httpSender.sendAndReceive(message);
        // Then
        proxy.verifyExactly(
                getRequestedFor(urlMatching("/"))
                        .withHeader("Host", matching("localhost:" + server.port())));
        server.verifyExactly(
                getRequestedFor(urlMatching("/"))
                        .withHeader("Host", matching("localhost:" + server.port())));
        assertThat(message.getResponseBody().toString(), is(equalTo(SERVER_RESPONSE)));
    }

    @Test
    void shouldNotProxyIfDisabled() throws Exception {
        // Given
        ConnectionParam options = createOptionsWithProxy("localhost", proxy.port());
        options.setUseProxyChain(false);
        HttpMessage message = createMessage("GET", "/");
        HttpSender httpSender = new HttpSender(options, false, -1);
        // When
        httpSender.sendAndReceive(message);
        // Then
        proxy.verifyNoRequests();
        server.verifyExactly(
                getRequestedFor(urlMatching("/"))
                        .withHeader("Host", matching("localhost:" + server.port())));
        assertThat(message.getResponseBody().toString(), is(equalTo(SERVER_RESPONSE)));
    }

    @Test
    void shouldNotAuthenticateToProxyIfAuthDisabled() throws Exception {
        // Given
        proxy.stubFor(
                get(urlMatching("/"))
                        .willReturn(
                                aResponse()
                                        .withStatus(407)
                                        .withHeader("Proxy-Authenticate", "Basic realm=\"\"")
                                        .withBody(PROXY_RESPONSE)));
        ConnectionParam options = createOptionsWithProxy("localhost", proxy.port());
        options.setUseProxyChain(true);
        options.setUseProxyChainAuth(false);
        HttpMessage message = createMessage("GET", "/");
        HttpSender httpSender = new HttpSender(options, false, -1);
        // When
        httpSender.sendAndReceive(message);
        // Then
        proxy.verifyExactly(
                getRequestedFor(urlMatching("/"))
                        .withoutHeader("Proxy-Authorization")
                        .withHeader("Host", matching("localhost:" + server.port())));
        server.verifyNoRequests();
        assertThat(message.getResponseBody().toString(), is(equalTo(PROXY_RESPONSE)));
    }

    @Test
    void shouldBasicAuthenticateToProxy() throws Exception {
        // Given
        String authRealm = "SomeRealm";
        proxy.stubFor(
                get(urlMatching("/"))
                        .inScenario("Basic Proxy Auth")
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willReturn(
                                aResponse()
                                        .withStatus(407)
                                        .withHeader(
                                                "Proxy-Authenticate",
                                                "Basic realm=\"" + authRealm + "\""))
                        .willSetStateTo("Challenged"));

        ConnectionParam options = createOptionsWithProxy("localhost", proxy.port());
        options.setUseProxyChain(true);
        options.setUseProxyChainAuth(true);
        options.setProxyChainRealm(authRealm);
        HttpMessage message = createMessage("GET", "/");
        HttpSender httpSender = new HttpSender(options, false, -1);
        // When
        httpSender.sendAndReceive(message);
        // Then
        proxy.verifyExactly(
                getRequestedFor(urlMatching("/"))
                        .withoutHeader("Proxy-Authorization")
                        .withHeader("Host", matching("localhost:" + server.port())),
                getRequestedFor(urlMatching("/"))
                        .withHeader(
                                "Proxy-Authorization", matching("Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
                        .withHeader("Host", matching("localhost:" + server.port())));
        server.verifyExactly(
                getRequestedFor(urlMatching("/"))
                        .withHeader("Host", matching("localhost:" + server.port())));
        assertThat(message.getResponseBody().toString(), is(equalTo(SERVER_RESPONSE)));
    }

    @Test
    void shouldNotBasicAuthenticateToProxyIfRealmMismatch() throws Exception {
        // Given
        proxy.stubFor(
                get(urlMatching("/"))
                        .willReturn(
                                aResponse()
                                        .withStatus(407)
                                        .withHeader(
                                                "Proxy-Authenticate", "Basic realm=\"SomeRealm\"")
                                        .withBody(PROXY_RESPONSE)));
        ConnectionParam options = createOptionsWithProxy("localhost", proxy.port());
        options.setUseProxyChain(true);
        options.setUseProxyChainAuth(true);
        options.setProxyChainRealm("NotSomeRealm");
        HttpMessage message = createMessage("GET", "/");
        HttpSender httpSender = new HttpSender(options, false, -1);
        // When
        httpSender.sendAndReceive(message);
        // Then
        proxy.verifyExactly(
                getRequestedFor(urlMatching("/"))
                        .withoutHeader("Proxy-Authorization")
                        .withHeader("Host", matching("localhost:" + server.port())));
        server.verifyNoRequests();
        assertThat(message.getResponseBody().toString(), is(equalTo(PROXY_RESPONSE)));
    }

    @Test
    void shouldSetContentEncodingsToResponse() throws Exception {
        // Given
        server.stubFor(
                get(urlMatching("/"))
                        .willReturn(
                                aResponse()
                                        .withHeader(HttpHeader.CONTENT_ENCODING, HttpHeader.GZIP)));
        HttpMessage message = createMessage("GET", "/");
        HttpSender httpSender = new HttpSender(createOptions(), false, -1);
        // When
        httpSender.sendAndReceive(message);
        // Then
        server.verifyExactly(
                getRequestedFor(urlMatching("/"))
                        .withHeader("Host", matching("localhost:" + server.port())));
        assertThat(message.getResponseBody().getContentEncodings(), is(not(empty())));
    }

    @Test
    void shouldNotSetContentEncodingsToResponseIfNoneInHeader() throws Exception {
        // Given
        HttpMessage message = createMessage("GET", "/");
        HttpSender httpSender = new HttpSender(createOptions(), false, -1);
        // When
        httpSender.sendAndReceive(message);
        // Then
        server.verifyExactly(
                getRequestedFor(urlMatching("/"))
                        .withHeader("Host", matching("localhost:" + server.port())));
        assertThat(message.getResponseBody().getContentEncodings(), is(empty()));
    }

    private HttpMessage createMessage(String method, String path) throws Exception {
        URI uri = new URI("http://localhost:" + server.port() + path, true);
        HttpRequestHeader requestHeader = new HttpRequestHeader(method, uri, "HTTP/1.1");
        return new HttpMessage(requestHeader);
    }

    private static ConnectionParam createOptionsWithProxy(String address, int port) {
        ConnectionParam options = createOptions();
        options.setProxyChainName(address);
        options.setProxyChainPort(port);
        options.setProxyChainUserName("username");
        options.setProxyChainPassword("password");
        return options;
    }

    private static ConnectionParam createOptions() {
        ConnectionParam options = new ConnectionParam();
        options.load(new ZapXmlConfiguration());
        return options;
    }

    private static WireMockConfiguration defaultOptions() {
        return options()
                .dynamicPort()
                .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.NEVER);
    }

    private static class WireMockSequence extends WireMockServer {

        WireMockSequence(WireMockConfiguration options) {
            super(options);
        }

        void verifyNoRequests() {
            List<LoggedRequest> requests =
                    findRequestsMatching(RequestPattern.everything()).getRequests();
            verifyRequestCount(requests, 0);
        }

        void verifyExactly(RequestPatternBuilder... requestPatternBuilders) {
            List<LoggedRequest> requests =
                    findRequestsMatching(RequestPattern.everything()).getRequests();
            verifyRequestCount(requests, requestPatternBuilders.length);
            int i = 0;
            for (LoggedRequest request : requests) {
                RequestPattern requestPattern = requestPatternBuilders[i].build();
                if (!requestPattern.match(request).isExactMatch()) {
                    Diff diff = new Diff(requestPattern, request);
                    throw VerificationException.forUnmatchedRequestPattern(diff);
                }
                i++;
            }
        }

        private void verifyRequestCount(List<LoggedRequest> requests, int expected) {
            if (requests.size() != expected) {
                throw new VerificationException(
                        String.format(
                                "Expected %s request(s) but received %d",
                                expected, requests.size()));
            }
        }
    }
}

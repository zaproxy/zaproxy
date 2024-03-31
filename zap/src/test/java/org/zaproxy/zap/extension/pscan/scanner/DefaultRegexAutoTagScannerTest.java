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
package org.zaproxy.zap.extension.pscan.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.InputStream;
import java.util.stream.Stream;
import net.htmlparser.jericho.Source;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanParam;
import org.zaproxy.zap.utils.Stats;
import org.zaproxy.zap.utils.StatsListener;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

@Timeout(6)
class DefaultRegexAutoTagScannerTest {

    private static final String BASE_STRING = "lorem ipsum < type= href= ";
    private static final String DEFAULT_EXPECTED_SITE = "http://example.com";

    private static Source source;
    private static HttpMessage message;
    private static PassiveScanParam passiveScanParam;
    private StatsListener listener;

    @BeforeAll
    static void beforeAll() throws Exception {
        passiveScanParam = new PassiveScanParam();
        try (InputStream is =
                DefaultRegexAutoTagScannerTest.class.getResourceAsStream(
                        "/org/zaproxy/zap/resources/config.xml")) {
            passiveScanParam.load(new ZapXmlConfiguration(is));
        }

        StringBuilder strBuilder = new StringBuilder(16_000_000);
        int count = strBuilder.capacity() / BASE_STRING.length();
        for (int i = 0; i < count; i++) {
            strBuilder.append(BASE_STRING);
        }
        String body = strBuilder.toString();
        source = new Source(body);
        message = new HttpMessage();
        message.setResponseBody(body);
    }

    @BeforeEach
    private void beforeEach() {
        listener = spy(StatsListener.class);
        Stats.addListener(listener);
    }

    static Stream<Arguments> defaultRegexes() {
        return passiveScanParam.getAutoTagScanners().stream()
                .map(e -> Arguments.of(e.getName(), e));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("defaultRegexes")
    void shouldNotBeSlowWhenScanningBigBody(String name, RegexAutoTagScanner scanner) {
        assertDoesNotThrow(() -> scanner.scanHttpResponseReceive(message, -1, source));
    }

    private RegexAutoTagScanner getRegexRuleByName(String taggerName) {
        for (RegexAutoTagScanner tagRule : passiveScanParam.getAutoTagScanners()) {
            if (tagRule.getName().equals(taggerName)) {
                return tagRule;
            }
        }
        return null;
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(
            strings = {
                "application/JSON",
                "application/jSon",
                "application/json",
                "aPPlication/json",
                "application/json; charset=utf-8"
            })
    void shouldCountWhenHeaderMatchesJsonTag(String contentType)
            throws URIException, HttpMalformedHeaderException, NullPointerException {
        shouldCountWhenHeaderMatchesExpectedTag(contentType, "response_json", "JSON");
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(
            strings = {
                // Response overlap
                "application/json",
                "application/json; charset=utf-8",
                "application/hal+json",
                "application/health+json",
                "application/problem+json",
                "application/vnd.api+json",
                "application/x-ndjson",
                "text/x-json",
                "text/json",
                "text/json; charset=utf-8"
            })
    void shouldCountWhenHeaderMatchesJsonExtendedTag(String contentType)
            throws URIException, HttpMalformedHeaderException, NullPointerException {
        shouldCountWhenHeaderMatchesExpectedTag(contentType, "json_extended", "JSON");
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(
            strings = {
                "application/yaml",
                "application/yaml; charset=utf-8",
                "text/yaml",
                "text/yaml; charset=utf-8",
                "application/x-yaml"
            })
    void shouldCountWhenHeaderMatchesYamlTag(String contentType)
            throws URIException, HttpMalformedHeaderException, NullPointerException {
        shouldCountWhenHeaderMatchesExpectedTag(contentType, "response_yaml", "YAML");
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(
            strings = {
                "application/xml; charset=utf-8",
                "text/xml",
                "application/problem+xml",
                "application/soap+xml"
            })
    void shouldCountWhenHeaderMatchesXmlTag(String contentType)
            throws URIException, HttpMalformedHeaderException, NullPointerException {
        shouldCountWhenHeaderMatchesExpectedTag(contentType, "response_xml", "XML");
    }

    private void shouldCountWhenHeaderMatchesExpectedTag(
            String contentType, String regexRuleName, String expectedConf)
            throws URIException, HttpMalformedHeaderException, NullPointerException {
        // Given
        HttpMessage tagMessage = new HttpMessage(new URI("http://example.com/", true));
        RegexAutoTagScanner rule = getRegexRuleByName(regexRuleName);
        rule.setEnabled(true);
        tagMessage.setHistoryRef(mock(HistoryReference.class));
        tagMessage.getResponseHeader().setHeader(HttpHeader.CONTENT_TYPE, contentType);
        // When
        rule.scanHttpResponseReceive(
                tagMessage, -1, new Source(tagMessage.getResponseBody().toString()));
        // Then
        ArgumentCaptor<String> siteCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        verify(listener).counterInc(siteCaptor.capture(), keyCaptor.capture());
        assertThat(siteCaptor.getValue(), is(equalTo(DEFAULT_EXPECTED_SITE)));
        assertThat(rule.getConf(), is(equalTo(expectedConf)));
        assertThat(
                keyCaptor.getValue(),
                is(equalTo(RegexAutoTagScanner.TAG_STATS_PREFIX + rule.getConf())));
    }
}

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
package org.zaproxy.zap.extension.pscan.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import net.htmlparser.jericho.Source;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanParam;
import org.zaproxy.zap.utils.Stats;
import org.zaproxy.zap.utils.StatsListener;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

class RegexAutoTagScannerUnitTest {

    private static final String BODY =
            "<html><head>@@head@@</head><body>@@body_one@@ @@body_two@@</body><html>";
    private static final String TEST_PATTERN = ".*foo\\sbar";
    private static final String TEST_CONFIG = "Test";

    private static PassiveScanParam passiveScanParam;

    private RegexAutoTagScanner rule;
    private StatsListener listener;

    @BeforeAll
    static void beforeAll() throws ConfigurationException, IOException {
        passiveScanParam = new PassiveScanParam();
        try (InputStream is =
                RegexAutoTagScannerUnitTest.class.getResourceAsStream(
                        "/org/zaproxy/zap/resources/config.xml")) {
            passiveScanParam.load(new ZapXmlConfiguration(is));
        }
    }

    @BeforeEach
    void setUp() {
        rule =
                new RegexAutoTagScanner(
                        TEST_CONFIG,
                        RegexAutoTagScanner.TYPE.TAG,
                        TEST_CONFIG,
                        null,
                        null,
                        null,
                        TEST_PATTERN,
                        true);
        listener = spy(StatsListener.class);
        Stats.addListener(listener);
    }

    @AfterEach
    void cleanup() {
        Stats.removeListener(listener);
    }

    @Test
    void shouldNotCountTagWhenBodyDoesNotMatch()
            throws URIException, HttpMalformedHeaderException, DatabaseException {
        // Given
        HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
        msg.setResponseBody(BODY);
        msg.setHistoryRef(mock(HistoryReference.class));
        // When
        rule.scanHttpResponseReceive(msg, -1, new Source(BODY));
        // Then
        Mockito.verifyNoInteractions(listener);
    }

    @Test
    void shouldCountTagWhenBodyHasMatch()
            throws URIException, HttpMalformedHeaderException, DatabaseException {
        // Given
        HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
        msg.setResponseBody(BODY.replace("@@body_two@@", "Lorem ipsum dolor sit amet, foo bar"));
        msg.setHistoryRef(mock(HistoryReference.class));
        // When
        rule.scanHttpResponseReceive(msg, -1, new Source(msg.getResponseBody().toString()));
        // Then
        verify(listener)
                .counterInc(
                        "http://example.com", RegexAutoTagScanner.TAG_STATS_PREFIX + TEST_CONFIG);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void shouldNotCountWhenDisabledThoughBodyContainsMatch()
            throws URIException, HttpMalformedHeaderException, DatabaseException {
        // Given
        rule.setEnabled(false);
        HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
        msg.setResponseBody(BODY.replace("@@body_two@@", "Lorem ipsum dolor sit amet, foo bar"));
        msg.setHistoryRef(mock(HistoryReference.class));
        // When
        rule.scanHttpResponseReceive(msg, -1, new Source(msg.getResponseBody().toString()));
        // Then
        Mockito.verifyNoInteractions(listener);
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
    void shouldCountWhenHeaderContainsJsonMatch(String contentType)
            throws URIException, HttpMalformedHeaderException, DatabaseException {
        // Given
        RegexAutoTagScanner rule = getRegexRuleByName("response_json");
        HttpMessage msg = new HttpMessage(new URI("http://example.com/", true));
        msg.setHistoryRef(mock(HistoryReference.class));
        msg.getResponseHeader().setHeader(HttpHeader.CONTENT_TYPE, contentType);
        // When
        rule.scanHttpResponseReceive(msg, -1, new Source(msg.getResponseBody().toString()));
        // Then
        ArgumentCaptor<String> siteCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(listener).counterInc(siteCaptor.capture(), keyCaptor.capture());
        assertThat(siteCaptor.getValue(), is(equalTo("http://example.com")));
        assertThat(rule.getConf(), is(equalTo("JSON")));
        assertThat(
                keyCaptor.getValue(),
                is(equalTo(RegexAutoTagScanner.TAG_STATS_PREFIX + rule.getConf())));
    }
}

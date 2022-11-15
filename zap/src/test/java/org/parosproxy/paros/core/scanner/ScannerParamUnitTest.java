/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link ScannerParam}. */
class ScannerParamUnitTest {

    private ScannerParam param;
    private ZapXmlConfiguration configuration;

    @BeforeAll
    static void beforeAll() {
        Constant.messages = mock(I18N.class);
    }

    @AfterAll
    static void afterAll() {
        Constant.messages = null;
    }

    @BeforeEach
    void setUp() {
        param = new ScannerParam();
        configuration = new ZapXmlConfiguration();
        param.load(configuration);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 10})
    void shouldLoadThreadPerHostFromConfig(int threadPerHost) {
        // Given
        configuration.setProperty("scanner.threadPerHost", threadPerHost);
        // When
        param.load(configuration);
        // Then
        assertThat(param.getThreadPerHost(), is(equalTo(threadPerHost)));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void shouldUseOneIfLoadingInvalidThreadPerHostFromConfig(int threadPerHost) {
        // Given
        configuration.setProperty("scanner.threadPerHost", threadPerHost);
        // When
        param.load(configuration);
        // Then
        assertThat(param.getThreadPerHost(), is(equalTo(1)));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 10})
    void shouldSetThreadPerHost(int threadPerHost) {
        // Given / When
        param.setThreadPerHost(threadPerHost);
        // Then
        assertThat(param.getThreadPerHost(), is(equalTo(threadPerHost)));
        assertThat(
                configuration.getProperty("scanner.threadPerHost"),
                is(equalTo(String.valueOf(threadPerHost))));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void shouldUseOneIfSettingInvalidNumberOfThreadPerHost(int threadPerHost) {
        // Given / When
        param.setThreadPerHost(threadPerHost);
        // Then
        assertThat(param.getThreadPerHost(), is(equalTo(1)));
        assertThat(configuration.getProperty("scanner.threadPerHost"), is(equalTo("1")));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 10})
    void shouldLoadHostPerScanFromConfig(int hostPerScan) {
        // Given
        configuration.setProperty("scanner.hostPerScan", hostPerScan);
        // When
        param.load(configuration);
        // Then
        assertThat(param.getHostPerScan(), is(equalTo(hostPerScan)));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void shouldUseOneIfLoadingInvalidHostPerScanFromConfig(int hostPerScan) {
        // Given
        configuration.setProperty("scanner.hostPerScan", hostPerScan);
        // When
        param.load(configuration);
        // Then
        assertThat(param.getHostPerScan(), is(equalTo(1)));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 10})
    void shouldSetHostPerScan(int hostPerScan) {
        // Given / When
        param.setHostPerScan(hostPerScan);
        // Then
        assertThat(param.getHostPerScan(), is(equalTo(hostPerScan)));
        assertThat(
                configuration.getProperty("scanner.hostPerScan"),
                is(equalTo(String.valueOf(hostPerScan))));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void shouldUseOneIfSettingInvalidNumberOfHostPerScan(int hostPerScan) {
        // Given / When
        param.setHostPerScan(hostPerScan);
        // Then
        assertThat(param.getHostPerScan(), is(equalTo(1)));
        assertThat(configuration.getProperty("scanner.hostPerScan"), is(equalTo("1")));
    }

    @Test
    void shouldHaveScanJsonNullValuesDisabledByDefault() {
        // Given / When
        boolean scanNullJsonValues = param.isScanNullJsonValues();
        // Then
        assertThat(scanNullJsonValues, is(equalTo(false)));
    }

    @Test
    void shouldHaveScanJsonNullValuesDisabledIfConfigurationNotBoolean() {
        // Given
        configuration.setProperty(ScannerParam.SCAN_NULL_JSON_VALUES, "NotBoolean");
        param.load(configuration);
        // When
        boolean scanNullJsonValues = param.isScanNullJsonValues();
        // Then
        assertThat(scanNullJsonValues, is(equalTo(false)));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldHaveScanJsonNullValuesAsSetInConfiguration(boolean value) {
        // Given
        configuration.setProperty(ScannerParam.SCAN_NULL_JSON_VALUES, value);
        param.load(configuration);
        // When
        boolean scanNullJsonValues = param.isScanNullJsonValues();
        // Then
        assertThat(scanNullJsonValues, is(equalTo(value)));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSetScanJsonNullValuesToConfiguration(boolean value) {
        // Given / When
        param.setScanNullJsonValues(value);
        // Then
        assertThat(
                configuration.getBoolean(ScannerParam.SCAN_NULL_JSON_VALUES), is(equalTo(value)));
    }

    @Test
    void shouldMigrateOldOptions() {
        // Given
        configuration.setProperty("scanner.deleteOnShutdown", true);
        configuration.setProperty("scanner.antiCSRF", true);
        // When
        param.load(configuration);
        // Then
        assertThat(configuration.getProperty("scanner.antiCSRF"), is(equalTo(true)));
        assertThat(param.getHandleAntiCSRFTokens(), is(equalTo(true)));
        assertNull(configuration.getProperty("scanner.antiCSFR"));
        assertNull(configuration.getProperty("scanner.deleteOnShutdown"));
    }
}

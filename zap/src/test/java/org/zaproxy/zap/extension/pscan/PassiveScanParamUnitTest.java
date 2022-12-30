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
package org.zaproxy.zap.extension.pscan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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

/** Unit test for {@link PassiveScanParam}. */
class PassiveScanParamUnitTest {

    private PassiveScanParam param;
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
        param = new PassiveScanParam();
        configuration = new ZapXmlConfiguration();
        param.load(configuration);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 10})
    void shouldLoadThreadsFromConfig(int threads) {
        // Given
        configuration.setProperty("pscans.threads", threads);
        // When
        param.load(configuration);
        // Then
        assertThat(param.getPassiveScanThreads(), is(equalTo(threads)));
    }

    @Test
    void shouldDefaultThreads() {
        // Given / When
        param.load(configuration);
        // Then
        assertThat(
                param.getPassiveScanThreads(), is(equalTo(Constant.getDefaultThreadCount() / 2)));
    }
}

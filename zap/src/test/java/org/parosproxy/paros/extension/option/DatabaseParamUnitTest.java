/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.parosproxy.paros.extension.option;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link DatabaseParam}. */
class DatabaseParamUnitTest {

    private ZapXmlConfiguration configuration;
    private DatabaseParam param;

    @BeforeEach
    void setUp() throws Exception {
        param = new DatabaseParam();
        configuration = new ZapXmlConfiguration();
        param.load(configuration);
    }

    @Test
    void shouldHaveSensibleDefragDefault() {
        // HSQLDB docs suggest 30-60 as useful values, 0 disables.
        assertThat(param.getDefragPercentage(), is(30));
    }

    @Test
    void shouldParseDefragPercentage() throws Exception {
        // Given
        configuration.setProperty("database.defrag", 45);
        // When
        param.load(configuration);
        // Then
        assertThat(param.getDefragPercentage(), is(45));
    }

    @Test
    void shouldPersistDefragPercentage() {
        // When
        param.setDefragPercentage(60);
        // Then
        assertThat(param.getDefragPercentage(), is(60));
        assertThat(configuration.getInt("database.defrag"), is(60));
    }

    @Test
    void shouldClampDefragPercentageAboveMax() {
        // When
        param.setDefragPercentage(150);
        // Then
        assertThat(param.getDefragPercentage(), is(100));
    }

    @Test
    void shouldClampDefragPercentageBelowMin() {
        // When
        param.setDefragPercentage(-5);
        // Then
        assertThat(param.getDefragPercentage(), is(0));
    }

    @Test
    void shouldClampParsedDefragPercentage() throws Exception {
        // Given
        configuration.setProperty("database.defrag", 150);
        // When
        param.load(configuration);
        // Then
        assertThat(param.getDefragPercentage(), is(100));
    }
}

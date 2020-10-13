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
package org.zaproxy.zap.resources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URI;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Test for home Log4j configuration. */
class Log4jHomeConfigurationTest {

    private static Configuration configuration;

    @BeforeAll
    static void setup() throws Exception {
        URI uri = Log4jHomeConfigurationTest.class.getResource("log4j2-home.properties").toURI();
        configuration =
                ConfigurationFactory.getInstance()
                        .getConfiguration(null, ConfigurationSource.fromUri(uri));
        configuration.start();
    }

    @AfterAll
    static void cleanup() {
        if (configuration != null) {
            configuration.stop();
        }
    }

    @Test
    void shouldHaveExpectedName() {
        // Given / When
        String name = configuration.getName();
        // Then
        assertThat(name, is(equalTo("ZAP Home Config")));
    }

    @Test
    void shouldHaveRootLevelSetToInfo() {
        // Given / When
        Level level = configuration.getRootLogger().getLevel();
        // Then
        assertThat(level, is(equalTo(Level.INFO)));
    }

    @Test
    void shouldHaveAppendersInRootLevel() {
        // Given / When
        Map<String, Appender> appenders = configuration.getRootLogger().getAppenders();
        // Then
        assertThat(appenders.values(), hasSize(2));
        assertThat(appenders.get("stdout"), is(notNullValue()));
        assertThat(appenders.get("RollingFile"), is(notNullValue()));
    }

    @Test
    void shouldHaveParosPackageSetToInfo() {
        // Given / When
        LoggerConfig loggerConfig = configuration.getLoggerConfig("org.parosproxy.paros");
        // Then
        assertThat(loggerConfig, is(notNullValue()));
        assertThat(loggerConfig.getLevel(), is(equalTo(Level.INFO)));
    }

    @Test
    void shouldHaveZapPackageSetToInfo() {
        // Given / When
        LoggerConfig loggerConfig = configuration.getLoggerConfig("org.zaproxy.zap");
        // Then
        assertThat(loggerConfig, is(notNullValue()));
        assertThat(loggerConfig.getLevel(), is(equalTo(Level.INFO)));
    }

    @Test
    void shouldHaveCommonsHttpClientPackageSetToError() {
        // Given / When
        LoggerConfig loggerConfig = configuration.getLoggerConfig("org.apache.commons.httpclient");
        // Then
        assertThat(loggerConfig, is(notNullValue()));
        assertThat(loggerConfig.getLevel(), is(equalTo(Level.ERROR)));
    }

    @Test
    void shouldHaveJerichoSetToOff() {
        // Given / When
        LoggerConfig loggerConfig = configuration.getLoggerConfig("net.htmlparser.jericho");
        // Then
        assertThat(loggerConfig, is(notNullValue()));
        assertThat(loggerConfig.getLevel(), is(equalTo(Level.OFF)));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "com.crawljax.core.Crawler",
                "com.crawljax.core.state.StateMachine",
                "com.crawljax.core.UnfiredCandidateActions"
            })
    void shouldHaveCrawljaxChattyClassesSetToWarn(String nname) {
        // Given / When
        LoggerConfig loggerConfig = configuration.getLoggerConfig(nname);
        // Then
        assertThat(loggerConfig, is(notNullValue()));
        assertThat(loggerConfig.getLevel(), is(equalTo(Level.WARN)));
    }
}

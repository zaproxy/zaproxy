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
package org.zaproxy.zap;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.util.stream.Stream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test for {@link ZapBootstrap}. */
class ZapBootstrapUnitTest {

    private static Configuration configuration;

    @BeforeEach
    void setup() throws Exception {
        System.setProperty("zap.user.log", Files.createTempDirectory("zap-log").toString());
        Configurator.reconfigure(
                getClass()
                        .getResource("/org/zaproxy/zap/resources/log4j2-home.properties")
                        .toURI());
        configuration = LoggerContext.getContext().getConfiguration();
    }

    @AfterEach
    void cleanupEach() {
        if (configuration != null) {
            configuration.stop();
        }
    }

    @AfterAll
    static void cleanupAll() throws Exception {
        Configurator.reconfigure(
                ZapBootstrapUnitTest.class.getResource("/log4j2-test.properties").toURI());
    }

    static Stream<Level> levelProvider() {
        return Stream.of(Level.DEBUG, Level.ERROR, Level.WARN, Level.INFO);
    }

    @ParameterizedTest
    @MethodSource("levelProvider")
    void shouldSetLogLevel(Level level) {
        // Given / When
        ZapBootstrap.setLogLevel(level);
        // Then
        assertLevel(getParosLoggerConfig(), level);
        assertLevel(getZapLoggerConfig(), level);
    }

    @Test
    void shouldNotSetNullLevel() {
        // Given
        configuration.getRootLogger().setLevel(Level.DEBUG);
        // When
        ZapBootstrap.setLogLevel(null);
        // Then
        assertLevel(getParosLoggerConfig(), Level.INFO);
        assertLevel(getZapLoggerConfig(), Level.INFO);
    }

    private static LoggerConfig getZapLoggerConfig() {
        return configuration.getLoggerConfig("org.zaproxy");
    }

    private static LoggerConfig getParosLoggerConfig() {
        return configuration.getLoggerConfig("org.parosproxy.paros");
    }

    private static void assertLevel(LoggerConfig loggerConfig, Level level) {
        assertThat(loggerConfig).isNotNull();
        assertThat(loggerConfig.getLevel()).isEqualTo(level);
    }
}

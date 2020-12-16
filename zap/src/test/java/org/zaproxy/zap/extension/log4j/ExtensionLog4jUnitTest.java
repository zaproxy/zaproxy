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
package org.zaproxy.zap.extension.log4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.zaproxy.zap.extension.log4j.ExtensionLog4j.ErrorAppender;

/** Unit test for {@link ExtensionLog4j}. */
class ExtensionLog4jUnitTest {

    /** Unit test for {@link ErrorAppender}. */
    static class ErrorAppenderUnitTest {

        private Logger logger;
        private List<String> logEvents;

        private ErrorAppender errorAppender;

        @BeforeEach
        void setup() {
            logEvents = new ArrayList<>();
            errorAppender = new ErrorAppender(logEvents::add);

            LoggerContext context = LoggerContext.getContext();
            LoggerConfig rootLoggerconfig = context.getConfiguration().getRootLogger();
            rootLoggerconfig
                    .getAppenders()
                    .values()
                    .forEach(context.getRootLogger()::removeAppender);
            rootLoggerconfig.addAppender(errorAppender, null, null);
            rootLoggerconfig.setLevel(Level.ALL);
            context.updateLoggers();

            logger = LogManager.getLogger(ErrorAppenderUnitTest.class);
        }

        @AfterEach
        void cleanup() throws Exception {
            Configurator.reconfigure(getClass().getResource("/log4j2-test.properties").toURI());
        }

        @Test
        void shouldLogError() {
            // Given
            Level level = Level.ERROR;
            String message = "Log Message";
            // When
            logger.log(level, message);
            // Then
            assertThat(logEvents, hasSize(1));
            assertThat(logEvents.get(0), startsWith(message));
        }

        @ParameterizedTest
        @ValueSource(strings = {"FATAL", "WARN", "INFO", "DEBUG", "TRACE"})
        void shouldIgnoreLogEventsOtherThanError(String levelName) {
            // Given
            Level level = Level.getLevel(levelName);
            String message = "Log Message";
            // When
            logger.log(level, message);
            // Then
            assertThat(logEvents, hasSize(0));
        }
    }
}

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
package org.zaproxy.zap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.PrintStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.ZAP.UncaughtExceptionLogger;
import org.zaproxy.zap.testutils.Log4jTestAppender;

/** Unit test for {@link ZAP}. */
class ZAPUnitTest {

    /** Unit test for {@link ZAP.UncaughtExceptionLogger}. */
    static class UncaughtExceptionLoggerUnitTest {

        private static final String THREAD_NAME = "ThreadName";
        private static final String OTHER_THREAD_NAME = "OtherThreadName";

        private PrintStream oldErrStream;

        private PrintStream errStream;
        private Log4jTestAppender testAppender;

        private UncaughtExceptionLogger uncaughtExceptionLogger;

        @BeforeEach
        void setup() {
            LoggerContext context = LoggerContext.getContext();
            context.getRootLogger()
                    .getAppenders()
                    .values()
                    .forEach(context.getRootLogger()::removeAppender);

            oldErrStream = System.err;
            errStream = mock(PrintStream.class);
            System.setErr(errStream);

            uncaughtExceptionLogger = new UncaughtExceptionLogger();
        }

        @AfterEach
        void cleanup() throws Exception {
            System.setErr(oldErrStream);
            Configurator.reconfigure(getClass().getResource("/log4j2-test.properties").toURI());
        }

        @Test
        void shouldFallbackToPrintExceptionsIfLoggerHasNoAppenders() {
            // Given
            Thread thread = mockThread();
            Throwable exception = mock(Exception.class);
            // When
            uncaughtExceptionLogger.uncaughtException(thread, exception);
            // Then
            verify(errStream).println(messageWith(THREAD_NAME));
            verify(exception).printStackTrace();
        }

        @Test
        void shouldNotPrintThreadDead() {
            // Given
            Thread thread = mockThread();
            Throwable exception = mock(ThreadDeath.class);
            // When
            uncaughtExceptionLogger.uncaughtException(thread, exception);
            // Then
            verifyNoInteractions(errStream);
            verifyNoInteractions(exception);
        }

        @Test
        void shouldLogExceptionIfRootLoggerHasAppenders() {
            // Given
            Thread thread = mockThread();
            Throwable exception = spy(RuntimeException.class);
            withRootLoggerAppender();
            // When
            uncaughtExceptionLogger.uncaughtException(thread, exception);
            // Then
            verifyNoInteractions(errStream);
            verify(exception, times(0)).printStackTrace();
            assertThat(testAppender.getLogEvents(), hasSize(1));
            assertLogEvent(testAppender.getLogEvents().get(0), THREAD_NAME, exception);
        }

        @Test
        void shouldLogExceptionsIfRootLoggerHasAppenders() {
            // Given
            Thread thread1 = mockThread(THREAD_NAME);
            Throwable exception1 = spy(RuntimeException.class);
            Thread thread2 = mockThread(OTHER_THREAD_NAME);
            Throwable exception2 = spy(RuntimeException.class);
            withRootLoggerAppender();
            // When
            uncaughtExceptionLogger.uncaughtException(thread1, exception1);
            uncaughtExceptionLogger.uncaughtException(thread2, exception2);
            // Then
            verifyNoInteractions(errStream);
            verify(exception1, times(0)).printStackTrace();
            verify(exception2, times(0)).printStackTrace();
            assertThat(testAppender.getLogEvents(), hasSize(2));
            assertLogEvent(testAppender.getLogEvents().get(0), THREAD_NAME, exception1);
            assertLogEvent(testAppender.getLogEvents().get(1), OTHER_THREAD_NAME, exception2);
        }

        @Test
        void shouldLogExceptionsAfterPrintingIfLoggerHasNowAppenders() {
            // Given
            Thread thread1 = mockThread(THREAD_NAME);
            Throwable exception1 = spy(RuntimeException.class);
            Thread thread2 = mockThread(OTHER_THREAD_NAME);
            Throwable exception2 = spy(RuntimeException.class);
            // When
            uncaughtExceptionLogger.uncaughtException(thread1, exception1);
            withRootLoggerAppender();
            uncaughtExceptionLogger.uncaughtException(thread2, exception2);
            // Then
            verify(errStream).println(messageWith(THREAD_NAME));
            verify(exception1).printStackTrace();
            assertThat(testAppender.getLogEvents(), hasSize(1));
            assertLogEvent(testAppender.getLogEvents().get(0), OTHER_THREAD_NAME, exception2);
        }

        @Test
        void shouldLogExceptionsIfLogConfigurationHasAppendersButNotRootLogger() {
            // Given
            Thread thread = mockThread();
            Throwable exception = mock(Exception.class);
            withLoggerAppender();
            // When
            uncaughtExceptionLogger.uncaughtException(thread, exception);
            // Then
            verifyNoInteractions(errStream);
            verify(exception, times(0)).printStackTrace();
            assertThat(testAppender.getLogEvents(), hasSize(1));
            assertLogEvent(testAppender.getLogEvents().get(0), THREAD_NAME, exception);
        }

        @Test
        void shouldNotLogThreadDead() {
            // Given
            Thread thread = mockThread();
            Throwable exception = mock(ThreadDeath.class);
            withRootLoggerAppender();
            // When
            uncaughtExceptionLogger.uncaughtException(thread, exception);
            // Then
            verifyNoInteractions(errStream);
            verify(exception, times(0)).printStackTrace();
            assertThat(testAppender.getLogEvents(), hasSize(0));
        }

        private void withRootLoggerAppender() {
            testAppender = new Log4jTestAppender();
            LoggerContext context = LoggerContext.getContext();
            LoggerConfig rootLoggerconfig = context.getConfiguration().getRootLogger();
            rootLoggerconfig.addAppender(testAppender, null, null);
            rootLoggerconfig.setLevel(Level.ERROR);
            context.updateLoggers();
        }

        private void withLoggerAppender() {
            testAppender = new Log4jTestAppender();
            LoggerContext context = LoggerContext.getContext();
            Logger logger = context.getLogger(UncaughtExceptionLogger.class.getCanonicalName());
            context.getConfiguration().addLoggerAppender(logger, testAppender);
            logger.setLevel(Level.ERROR);
        }

        private void assertLogEvent(
                Log4jTestAppender.AppendedLogEvent logEvent, String message, Throwable exception) {
            assertThat(logEvent.getMessage(), containsString(messageWith(message)));
            assertThat(logEvent.getException(), is(equalTo(exception)));
        }

        private static String messageWith(String threadName) {
            return String.format("Exception in thread \"%s\"", threadName);
        }

        private static Thread mockThread() {
            return mockThread(THREAD_NAME);
        }

        private static Thread mockThread(String threadName) {
            return new Thread(threadName);
        }
    }
}

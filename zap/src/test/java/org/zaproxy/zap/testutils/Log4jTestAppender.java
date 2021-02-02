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
package org.zaproxy.zap.testutils;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

public class Log4jTestAppender extends AbstractAppender {

    private final List<AppendedLogEvent> logEvents;

    public Log4jTestAppender() {
        super("ZAP-TestAppender", null, null, false, new Property[0]);

        logEvents = new ArrayList<>();
        start();
    }

    @Override
    public void append(LogEvent event) {
        logEvents.add(
                new AppendedLogEvent(
                        event.getLevel(),
                        event.getMessage().getFormattedMessage(),
                        event.getThrown()));
    }

    public List<AppendedLogEvent> getLogEvents() {
        return logEvents;
    }

    public static class AppendedLogEvent {
        private final Level level;
        private final String message;
        private final Throwable exception;

        private AppendedLogEvent(Level level, String message, Throwable exception) {
            this.level = level;
            this.message = message;
            this.exception = exception;
        }

        public Level getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getException() {
            return exception;
        }
    }
}

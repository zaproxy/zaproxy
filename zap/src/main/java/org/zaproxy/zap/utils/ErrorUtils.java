/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
package org.zaproxy.zap.utils;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.View;

public final class ErrorUtils {

    private static final Logger LOGGER = LogManager.getLogger(ErrorUtils.class);

    private static OutOfDiskSpaceHandler outOfDiskSpaceHandler = new DefaultOutOfDiskSpaceHandler();

    private ErrorUtils() {
        // Utility class
    }

    public static boolean handleDiskSpaceException(Exception e) {
        return outOfDiskSpaceHandler.handleDiskSpaceException(e);
    }

    public static void setOutOfDiskSpaceHandler(OutOfDiskSpaceHandler outOfDiskSpaceHandler) {
        ErrorUtils.outOfDiskSpaceHandler =
                outOfDiskSpaceHandler != null
                        ? outOfDiskSpaceHandler
                        : new DefaultOutOfDiskSpaceHandler();
    }

    public static OutOfDiskSpaceHandler getOutOfDiskSpaceHandler() {
        return outOfDiskSpaceHandler;
    }

    public static boolean hasCause(Exception e, List<String> wantedMessages) {
        String message = getCauseMessage(e);
        if (message == null) {
            return false;
        }
        return wantedMessages.stream().anyMatch(message::contains);
    }

    public static boolean hasCause(Exception e, String wantedMessage) {
        String message = getCauseMessage(e);
        return message != null && message.contains(wantedMessage);
    }

    private static String getCauseMessage(Exception e) {
        Throwable cause = e.getCause();
        if (cause == null) {
            return null;
        }
        String message = cause.getMessage();
        if (message == null) {
            return null;
        }
        return message;
    }

    private static class DefaultOutOfDiskSpaceHandler implements OutOfDiskSpaceHandler {

        private boolean outOfSpace;
        private boolean exitOnOutOfSpace = true;
        private static final List<String> DB_FULL_MSGS = List.of("Data File size limit is reached");
        private static final List<String> DISK_FULL_MSGS =
                List.of(
                        "No space left on device",
                        "There is not enough space on the disk",
                        "file input/output error");

        @Override
        public boolean handleDiskSpaceException(Exception e) {
            boolean dbFull = hasCause(e, DB_FULL_MSGS);
            boolean diskFull = hasCause(e, DISK_FULL_MSGS);

            if ((dbFull || diskFull) && !outOfSpace) {
                String errorMsg;
                if (dbFull) {
                    errorMsg = Constant.messages.getString("db.space.full");
                    Stats.incCounter("stats.error.database.full");
                } else {
                    errorMsg = Constant.messages.getString("disk.space.full");
                    Stats.incCounter("stats.error.diskspace.full");
                }

                logAndStderr(errorMsg);
                outOfSpace = true;
                if (View.isInitialised()) {
                    View.getSingleton().showWarningDialog(errorMsg);
                } else if (exitOnOutOfSpace) {
                    logAndStderr("Shutting down ZAP due to space issues...");
                    Control control = Control.getSingleton();
                    control.setExitStatus(2, errorMsg);
                    control.exit(false, null);
                }
            }

            return dbFull || diskFull;
        }

        /**
         * Write to the log and stderr, in case theres not enough space to write to the log
         *
         * @param msg
         */
        private void logAndStderr(String msg) {
            System.err.println(msg);
            LOGGER.error(msg);
        }

        @Override
        public boolean isOutOfSpace() {
            return outOfSpace;
        }

        @Override
        public boolean isExitOnOutOfSpace() {
            return exitOnOutOfSpace;
        }

        @Override
        public void setExitOnOutOfSpace(boolean exitOnOutOfSpace) {
            this.exitOnOutOfSpace = exitOnOutOfSpace;
        }
    }
}

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
package org.zaproxy.zap.extension.httppanel;

/**
 * Indicates that the data being set to the message is invalid.
 *
 * <p>Whenever possible the exception message should be internationalised, the message will be shown
 * to the user.
 *
 * @since 2.10.0
 */
public class InvalidMessageDataException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code InvalidMessageDataException} with the given message.
     *
     * @param message a message that indicates why the data is invalid.
     */
    public InvalidMessageDataException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code InvalidMessageDataException} with the given message and cause.
     *
     * @param message a message that indicates why the data is invalid.
     * @param cause the cause of the exception (e.g. if an error occurred while parsing the data).
     */
    public InvalidMessageDataException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@code InvalidMessageDataException} with the given cause.
     *
     * <p>The localised message of the cause is used as message of this exception.
     *
     * @param cause the cause of the exception (e.g. if an error occurred while parsing the data).
     */
    public InvalidMessageDataException(Throwable cause) {
        super(cause != null ? cause.getLocalizedMessage() : null, cause);
    }

    /**
     * Constructs an {@code InvalidMessageDataException} with the given details.
     *
     * @param message a message that indicates why the data is invalid.
     * @param cause the cause of the exception (e.g. if an error occurred while parsing the data).
     * @param enableSuppression {@code true} if the suppression should be enabled, {@code false}
     *     otherwise.
     * @param writableStackTrace {@code true} if the stack trace should be written, {@code false}
     *     otherwise.
     */
    protected InvalidMessageDataException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

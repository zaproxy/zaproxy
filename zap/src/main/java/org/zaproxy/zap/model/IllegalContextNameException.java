/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.model;

/**
 * An {@code IllegalArgumentException} that indicates why a context name is illegal.
 *
 * @since 2.6.0
 * @see Reason
 */
public class IllegalContextNameException extends IllegalArgumentException {

    private static final long serialVersionUID = 3303985594197846509L;

    /**
     * The reason why a context's name is illegal.
     *
     * @see IllegalContextNameException#getReason()
     */
    public enum Reason {
        /** The name is {@code null} or empty. */
        EMPTY_NAME,

        /** It's a duplicated name. */
        DUPLICATED_NAME
    }

    private final Reason reason;

    /**
     * Constructs an {@code IllegalContextNameException} with the given reason.
     *
     * @param reason the reason why the name is illegal, must not be {@code null}.
     */
    public IllegalContextNameException(Reason reason) {
        this(reason, null);
    }

    /**
     * Constructs an {@code IllegalContextNameException} with the given reason and message.
     *
     * @param reason the reason why the name is illegal, must not be {@code null}.
     * @param message the detail message
     */
    public IllegalContextNameException(Reason reason, String message) {
        super(message);

        this.reason = reason;
    }

    /**
     * Gets the reason why the context's name is illegal.
     *
     * @return the reason why the name is illegal.
     */
    public Reason getReason() {
        return reason;
    }
}

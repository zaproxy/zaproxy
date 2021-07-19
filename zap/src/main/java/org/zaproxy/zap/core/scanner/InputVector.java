/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
package org.zaproxy.zap.core.scanner;

/**
 * {@code InputVector} class wraps the parameters which are used to modify the {@code HttpMessage}.
 * It is specifically used for updating multiple parameters of {@code HttpMessage}
 *
 * @author preetkaran20@gmail.com KSASAN
 * @since 2.11.0
 */
public class InputVector {

    /**
     * {@code PayloadFormat} represents the format of the payload. This is useful for the use-cases
     * like say there is a parameter which is a URL then caller can itself encode/escape it so that
     * it can be directly used to modify {@code HttpMessage} or {@code Variant} has to escape it.
     */
    public enum PayloadFormat {
        REQUIRES_ESCAPING,
        ALREADY_ESCAPED
    }

    private final int position;
    private final String name;
    private final PayloadFormat namePayloadFormat;
    private final String value;
    private final PayloadFormat valuePayloadFormat;

    InputVector(
            int position,
            String name,
            PayloadFormat namePayloadFormat,
            String value,
            PayloadFormat valuePayloadFormat) {
        this.position = position;
        this.name = name;
        this.value = value;
        this.namePayloadFormat = namePayloadFormat;
        this.valuePayloadFormat = valuePayloadFormat;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public PayloadFormat getNamePayloadFormat() {
        return namePayloadFormat;
    }

    public String getValue() {
        return value;
    }

    public PayloadFormat getValuePayloadFormat() {
        return valuePayloadFormat;
    }
}

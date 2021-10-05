/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

public class MultipartFormParameter {

    public static enum Type {
        GENERAL,
        FILE_NAME,
        FILE_CONTENT_TYPE
    }

    private String name;
    private String value;
    private int start;
    private int end;
    private int position;
    private Type type;

    public MultipartFormParameter(
            String name, String value, int start, int end, int position, Type type) {
        this.name = name;
        this.value = value;
        this.start = start;
        this.end = end;
        this.position = position;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getPosition() {
        return position;
    }

    public Type getType() {
        return type;
    }
}

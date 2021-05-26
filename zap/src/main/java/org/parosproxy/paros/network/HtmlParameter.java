/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.parosproxy.paros.network;

import java.util.HashSet;
import java.util.Set;

public class HtmlParameter implements Comparable<HtmlParameter> {
    public enum Type {
        cookie,
        form,
        url,
        header,
        multipart
    }

    public enum Flags {
        anticsrf,
        session,
        structural
    }

    private String name;
    private String value;
    private Type type;
    private Set<String> flags;

    /**
     * Constructs a {@code HtmlParameter} with the given type, name, and value.
     *
     * @param type the type.
     * @param name the name.
     * @param value the value.
     * @throws IllegalArgumentException if any of the parameters is {@code null}.
     */
    public HtmlParameter(Type type, String name, String value) {
        super();
        setName(name);
        setValue(value);
        setType(type);
    }

    /**
     * Constructs a {@code HtmlParameter}, with type {@link Type#cookie}, from the given cookie
     * line.
     *
     * <p>The cookie line can be from a {@code Cookie} or {@code Set-Cookie} header.
     *
     * @param cookieLine the cookie line
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     */
    public HtmlParameter(String cookieLine) {
        super();
        validateNotNull(cookieLine, "cookieLine");
        this.type = Type.cookie;
        String[] array = cookieLine.split(";");
        int eqOffset = array[0].indexOf('=');
        if (eqOffset == -1) {
            this.name = "";
            this.value = array[0].trim();
        } else {
            this.name = array[0].substring(0, eqOffset).trim();
            this.value = array[0].substring(eqOffset + 1).trim();
        }
        if (array.length > 1) {
            for (int i = 1; i < array.length; i++) {
                this.addFlag(array[i].trim());
            }
        }
    }

    private static void validateNotNull(Object parameter, String parameterName) {
        if (parameter == null) {
            throw new IllegalArgumentException("Parameter " + parameterName + " must not be null");
        }
    }

    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name.
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     */
    public void setName(String name) {
        validateNotNull(name, "name");
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type.
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     */
    public void setType(Type type) {
        validateNotNull(type, "type");
        this.type = type;
    }

    public Set<String> getFlags() {
        if (this.flags == null) this.flags = new HashSet<>();
        return this.flags;
    }

    public void addFlag(String flag) {
        this.getFlags().add(flag);
    }

    @Override
    public int compareTo(HtmlParameter o) {
        if (o == null) {
            return 1;
        }

        int result = this.type.ordinal() - o.getType().ordinal();
        if (result == 0) {
            // Same type
            result = this.name.compareTo(o.getName());
        }
        if (result == 0) {
            // Same type and name
            if (this.value != null && o.getValue() != null) {
                result = this.value.compareTo(o.getValue());
            } else if (this.value != null || o.getValue() != null) {
                // They can't both be null due to previous test
                return this.value != null ? 1 : -1;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "HtmlParameter type = " + type + " name= " + name + " value=" + value;
    }
}

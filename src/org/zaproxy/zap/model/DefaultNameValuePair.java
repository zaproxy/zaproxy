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
 * Default implementation of {@code NameValuePair}, in which the name and value are optional, that is, can be {@code null}.
 * <p>
 * The implementation is immutable thus thread safe.
 *
 * @since 2.5.0
 */
public final class DefaultNameValuePair implements NameValuePair, Comparable<DefaultNameValuePair> {

    /**
     * The name of the pair, might be {@code null}.
     */
    private final String name;

    /**
     * The value of the pair, might be {@code null}.
     */
    private final String value;

    /**
     * Constructs a {@code DefaultNameValuePair} with no name nor value.
     */
    public DefaultNameValuePair() {
        this(null, null);
    }

    /**
     * Constructs a {@code DefaultNameValuePair} with the given {@code name} and no value.
     *
     * @param name the name, might be {@code null}
     */
    public DefaultNameValuePair(String name) {
        this(name, null);
    }

    /**
     * Constructs a {@code DefaultNameValuePair} with the given {@code name} and {@code value}.
     *
     * @param name the name, might be {@code null}
     * @param value the value, might be {@code null}
     */
    public DefaultNameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return the name, might be {@code null}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return the value, might be {@code null}
     */
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder(75);
        strBuilder.append("[");
        if (name != null) {
            strBuilder.append("Name=").append(name);
        }
        if (value != null) {
            if (name != null) {
                strBuilder.append(", ");
            }
            strBuilder.append("Value=").append(value);
        }
        strBuilder.append(']');
        return strBuilder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }

        DefaultNameValuePair other = (DefaultNameValuePair) object;
        if (!equalStrings(name, other.name)) {
            return false;
        }

        if (!equalStrings(value, other.value)) {
            return false;
        }

        return true;
    }

    private static boolean equalStrings(String string, String otherString) {
        if (string == null) {
            if (otherString != null) {
                return false;
            }
        } else if (!string.equals(otherString)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(DefaultNameValuePair other) {
        if (other == null) {
            return 1;
        }

        int res = compareStrings(name, other.name);
        if (res != 0) {
            return res;
        }

        res = compareStrings(value, other.value);
        if (res != 0) {
            return res;
        }

        return 0;
    }

    private static int compareStrings(String string, String otherString) {
        if (string == null) {
            if (otherString != null) {
                return -1;
            }
            return 0;
        } else if (otherString == null) {
            return 1;
        }

        return string.compareTo(otherString);
    }
}

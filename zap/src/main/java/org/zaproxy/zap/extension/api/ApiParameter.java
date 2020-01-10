/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
package org.zaproxy.zap.extension.api;

import java.util.Objects;

/**
 * A parameter of an {@link ApiElement}.
 *
 * @since 2.9.0
 */
public class ApiParameter {

    private final String name;
    private String descriptionKey;
    private final boolean required;

    /**
     * Constructs an {@code ApiParameter} with the given details.
     *
     * @param name the name of the parameter.
     * @param descriptionKey the resource key of the description.
     * @param required {@code true} if the parameter is required, {@code false} otherwise.
     * @throws NullPointerException if {@code name} or {@code descriptionKey} is {@code null}.
     * @throws IllegalArgumentException if {@code name} is empty.
     */
    ApiParameter(String name, String descriptionKey, boolean required) {
        Objects.requireNonNull(name);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("The name must not be empty.");
        }
        this.name = name;
        this.descriptionKey = Objects.requireNonNull(descriptionKey);
        this.required = required;
    }

    /**
     * Gets the name.
     *
     * @return the name, never {@code null}.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the resource key of the description.
     *
     * @return the key of the description, never {@code null}.
     * @see org.zaproxy.zap.utils.I18N#getString(String)
     */
    public String getDescriptionKey() {
        return descriptionKey;
    }

    void setDescriptionKey(String key) {
        this.descriptionKey = Objects.requireNonNull(key);
    }

    /**
     * Tells whether or not the parameter is required.
     *
     * @return {@code true} if the parameter is required, {@code false} otherwise.
     */
    public boolean isRequired() {
        return required;
    }
}

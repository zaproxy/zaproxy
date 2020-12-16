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
package org.zaproxy.zap.extension.custompages;

import org.parosproxy.paros.Constant;

/** Defines the page matcher locations supported by the Custom Page package. */
public enum CustomPageMatcherLocation {
    RESPONSE_CONTENT(1, "custompages.content.location.response"),
    URL(2, "custompages.content.location.url");

    private final int id;
    private final String name;
    private final String nameKey;

    private CustomPageMatcherLocation(int id, String nameKey) {
        this.id = id;
        this.name = Constant.messages.getString(nameKey);
        this.nameKey = nameKey;
    }

    /**
     * Gets the ID of this custom page page matcher location.
     *
     * <p>The ID can be used for persistence and later creation, using the method {@code
     * getCustomPagePageMatcherLocationWithId(int)}.
     *
     * @return the ID of the custom page page matcher location
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the name of this custom page page matcher location.
     *
     * @return the name of the custom page page matcher location
     */
    public String getName() {
        return name;
    }

    String getNameKey() {
        return nameKey;
    }

    /**
     * Gets the custom page page matcher location that has the given {@code id}.
     *
     * <p>Default return is {@code getDefaultLocation()}.
     *
     * @param id the ID of the custom page page matcher location
     * @return the custom page page matcher location that matches the given {@code id}.
     * @throws IllegalArgumentException if the given {@code id} is {@code null}.
     * @see #getId()
     * @see #getDefaultLocation()
     */
    public static CustomPageMatcherLocation getCustomPagePageMatcherLocationWithId(int id) {
        for (CustomPageMatcherLocation cpct : values()) {
            if (cpct.getId() == id) {
                return cpct;
            }
        }
        return getDefaultLocation();
    }

    /**
     * Gets the default custom page page matcher location (RESPONSE_CONTENT).
     *
     * @return the default {@code CustomPageMatcherLocation}.
     */
    public static CustomPageMatcherLocation getDefaultLocation() {
        return RESPONSE_CONTENT;
    }

    public static String describeCustomPagePageMatcherLocationss() {
        StringBuilder descCustomPagePageMatcherLocations = new StringBuilder();
        descCustomPagePageMatcherLocations.append(
                "Available Custom Page Page Matcher Locations (ID : Name): \n");
        for (CustomPageMatcherLocation cpct : CustomPageMatcherLocation.values()) {
            descCustomPagePageMatcherLocations
                    .append(cpct.getId())
                    .append(" : ")
                    .append(cpct.getName());
            descCustomPagePageMatcherLocations.append("\n");
        }

        return descCustomPagePageMatcherLocations.toString();
    }

    @Override
    public String toString() {
        if (getName() == null) {
            return name();
        }
        return getName();
    }
}

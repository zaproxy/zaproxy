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
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.utils.EnableableInterface;

public interface CustomPage extends EnableableInterface {

    /**
     * Returns the context ID for a Custom Page.
     *
     * @return the context ID {@code int}
     */
    int getContextId();

    /**
     * Sets the context ID for a Custom Page.
     *
     * @param contextId the ID of the context
     */
    void setContextId(int contextId);

    /**
     * Returns the page matcher for a Custom Page.
     *
     * @return the page matcher {@code String}
     */
    String getPageMatcher();

    /**
     * Sets the page matcher for a Custom Page.
     *
     * @param pageMatcher the page matcher being set
     */
    void setPageMatcher(String pageMatcher);

    /**
     * Returns the {@link CustomPageMatcherLocation} {@code enum} literal for a Custom Page.
     *
     * @return the custom page matcher location {@code CustomPageMatcherLocation}
     */
    CustomPageMatcherLocation getPageMatcherLocation();

    /**
     * Sets the {@link CustomPageMatcherLocation} {@code enum} literal for a Custom Page.
     *
     * @param cppmt the {@code CustomPageMatcherLocation} being set
     */
    void setPageMatcherLocation(CustomPageMatcherLocation cppmt);

    /**
     * Returns {@code true} if the page matcher of a Custom Page is a Regex pattern, otherwise
     * {@code false}.
     *
     * @return a boolean representing whether or not the page matcher is a Regex pattern
     */
    boolean isRegex();

    /**
     * Sets a boolean designating whether or not the page matcher of a Custom Page is a Regex
     * pattern.
     *
     * @param regex the state being set
     */
    void setRegex(boolean regex);

    /**
     * Returns the {@code enum} literal {@link CustomPage.Type} of a Custom Page.
     *
     * @return the custom page type
     */
    Type getType();

    /**
     * Sets the {@link CustomPage.Type} {@code enum} literal for a Custom Page.
     *
     * @param cpt the custom page type to be set
     */
    void setType(Type cpt);

    /**
     * Determines if a {@code HttpMessage} is a Custom Page of a particular {@code CustomPage.Type}.
     *
     * @param msg the HTTP message to be evaluated
     * @param cpt the CustomPage.Type of the Custom Pages against which the HTTP message should be
     *     evaluated
     * @return {@code true} if the HTTP message is a Custom Page of the type in question, {@code
     *     false} otherwise
     */
    boolean isCustomPage(HttpMessage msg, Type cpt);

    /** Defines the types supported by the Custom Page package. */
    enum Type {
        ERROR_500(1, "custompages.type.error"),
        NOTFOUND_404(2, "custompages.type.notfound"),
        OK_200(3, "custompages.type.ok"),
        /**
         * OTHER is intended to provide an option for scan rule scripts for usages that may not yet
         * have been planned.
         */
        OTHER(4, "custompages.type.other"),
        /**
         * AUTH_4XX represents conditions that would normally be covered by HTTP 401 or 403 type
         * responses.
         *
         * @since 2.12.0
         */
        AUTH_4XX(5, "custompages.type.auth");

        private final int id;
        private final String name;
        private final String nameKey;

        Type(int id, String nameKey) {
            this.id = id;
            this.name = Constant.messages.getString(nameKey);
            this.nameKey = nameKey;
        }

        /**
         * Gets the ID of this custom page type.
         *
         * <p>The ID can be used for persistence and later creation, using the method {@code
         * getCustomPageTypeWithId(int)}.
         *
         * @return the ID of the custom page type
         */
        public int getId() {
            return id;
        }

        /**
         * Gets the name of this custom page type.
         *
         * @return the name of the custom page type
         */
        public String getName() {
            return name;
        }

        String getNameKey() {
            return nameKey;
        }

        /**
         * Gets the custom page type that has the given {@code id}.
         *
         * <p>Default return is {@code getDefaultType()}.
         *
         * @param id the ID of the custom page type
         * @return the custom page type that matches the given {@code id}.
         * @throws IllegalArgumentException if the given {@code id} is {@code null}.
         * @see #getId()
         * @see #getDefaultType()
         */
        public static Type getCustomPageTypeWithId(int id) {
            for (Type customPageType : values()) {
                if (customPageType.id == id) {
                    return customPageType;
                }
            }
            return getDefaultType();
        }

        /**
         * Gets the default custom page type (ERROR).
         *
         * @return the default {@code CustomPage.Type}.
         */
        protected static Type getDefaultType() {
            return ERROR_500;
        }

        public static String describeCustomPageTypes() {
            StringBuilder descCustomPageTypes = new StringBuilder();
            descCustomPageTypes.append("Available Custom Page Types (ID : Name): \n");
            for (Type cpt : Type.values()) {
                descCustomPageTypes.append(cpt.getId()).append(" : ").append(cpt.getName());
                descCustomPageTypes.append("\n");
            }

            return descCustomPageTypes.toString();
        }

        @Override
        public String toString() {
            if (getName() == null) {
                return name();
            }
            return getName();
        }
    }
}

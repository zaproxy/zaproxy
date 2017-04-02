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
package org.zaproxy.zap.network;

/**
 * A configuration of a HTTP request.
 * <p>
 * Allows to configure how the HTTP request is sent, for example, if and how redirects are followed.
 * 
 * @since 2.6.0
 */
public class HttpRequestConfig {

    private final boolean followRedirects;
    private final HttpRedirectionValidator redirectionValidator;

    HttpRequestConfig(boolean followRedirects, HttpRedirectionValidator redirectionValidator) {
        this.followRedirects = followRedirects;
        this.redirectionValidator = redirectionValidator;
    }

    /**
     * Tells whether or not the redirects should be followed.
     *
     * @return {@code true} if the redirects should be followed, {@code false} otherwise.
     * @see #getRedirectionValidator()
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
     * Gets the {@code HttpRedirectionValidator}, to validate the followed redirections.
     *
     * @return the validator responsible for validation of redirections, never {@code null}.
     * @see #isFollowRedirects()
     */
    public HttpRedirectionValidator getRedirectionValidator() {
        return redirectionValidator;
    }

    /**
     * Gets a new HTTP request configuration builder.
     *
     * @return a new HTTP request configuration builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets a new HTTP request configuration builder, using the following configuration as default.
     *
     * @param configuration the HTTP request configuration for defaults.
     * @return a new HTTP request configuration builder.
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     */
    public static Builder builder(HttpRequestConfig configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Parameter configuration must not be null.");
        }
        return new Builder(configuration);
    }

    /**
     * Builder of {@link HttpRequestConfig}.
     */
    public static class Builder {

        private boolean followRedirects;
        private HttpRedirectionValidator redirectionValidator;

        private Builder() {
            this.followRedirects = false;
            this.redirectionValidator = DefaultHttpRedirectionValidator.INSTANCE;
        }

        private Builder(HttpRequestConfig config) {
            this.followRedirects = config.isFollowRedirects();
            this.redirectionValidator = config.getRedirectionValidator();
        }

        /**
         * Sets whether or not the redirects should be followed.
         *
         * @param followRedirects {@code true} if the redirects should be followed, {@code false} otherwise.
         * @return the builder.
         * @see #setRedirectionValidator(HttpRedirectionValidator)
         */
        public Builder setFollowRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        /**
         * Sets the validator of redirections.
         * <p>
         * Automatically sets that the redirections should be followed.
         * 
         * @param redirectionValidator the validator of redirections.
         * @return the builder.
         * @throws IllegalArgumentException if the given parameter is {@code null}.
         * @see #setFollowRedirects(boolean)
         */
        public Builder setRedirectionValidator(HttpRedirectionValidator redirectionValidator) {
            if (redirectionValidator == null) {
                throw new IllegalArgumentException("Parameter redirectionValidator must not be null.");
            }
            this.redirectionValidator = redirectionValidator;
            this.followRedirects = true;
            return this;
        }

        /**
         * Builds a new {@code HttpRequestConfig}, with the configurations previously set.
         *
         * @return a new {@code HttpRequestConfig}.
         */
        public HttpRequestConfig build() {
            return new HttpRequestConfig(followRedirects, redirectionValidator);
        }
    }
}
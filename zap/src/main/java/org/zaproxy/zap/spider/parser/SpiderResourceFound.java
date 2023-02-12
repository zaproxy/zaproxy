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
package org.zaproxy.zap.spider.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

/**
 * Class SpiderResourceFound is used to store information about found resources by spider parsers.
 *
 * @since 2.11.0
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class SpiderResourceFound {
    /** The message where the resource was found. */
    private final HttpMessage message;
    /** Spider depth for resource. */
    private final int depth;
    /** HTTP method for resource. */
    private final String method;
    /** Uniform resource identifier of resource. */
    private final String uri;
    /** Body for the resource. */
    private final String body;
    /** Defines resource as useful or not useful in the fetching process. */
    private final boolean shouldIgnore;
    /** Additional request headers to be passed for the resource. */
    private final List<HttpHeaderField> headers;

    /**
     * Instantiates a spider resource found.
     *
     * @param message the message for the found resource
     * @param depth the depth of this resource in the crawling process
     * @param uri the universal resource locator
     * @param method HTTP method for the resource found
     * @param body request body for the resource
     * @param shouldIgnore flag to ignore the resource found
     * @param headers additional request headers for the resource
     */
    private SpiderResourceFound(
            HttpMessage message,
            int depth,
            String uri,
            String method,
            String body,
            boolean shouldIgnore,
            List<HttpHeaderField> headers) {
        this.message = message;
        this.depth = depth;
        this.uri = uri;
        this.method = method;
        this.body = body;
        this.shouldIgnore = shouldIgnore;
        this.headers = headers;
    }

    /**
     * Returns the message where the resource was found.
     *
     * @return HTTP message
     */
    public HttpMessage getMessage() {
        return message;
    }

    /**
     * Returns the spider depth of the resource.
     *
     * @return depth value
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Gives back the method to be applied for the found resource.
     *
     * @return HTTP method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Returns the URI of the found resource.
     *
     * @return uniform resource identifier
     */
    public String getUri() {
        return uri;
    }

    /**
     * Returns request body for the found resource.
     *
     * @return body string (empty if resource is GET-based)
     */
    public String getBody() {
        return body;
    }

    /**
     * States if the found resource should be ignored in the fetching process.
     *
     * @return boolean whether resource should be ignored
     */
    public boolean isShouldIgnore() {
        return shouldIgnore;
    }

    /**
     * Returns the additional request headers for the resource found.
     *
     * @return list of HTTP header fields
     */
    public List<HttpHeaderField> getHeaders() {
        return headers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, depth, headers, message, method, shouldIgnore, uri);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SpiderResourceFound)) {
            return false;
        }
        SpiderResourceFound other = (SpiderResourceFound) obj;
        return Objects.equals(body, other.body)
                && depth == other.depth
                && Objects.equals(headers, other.headers)
                && Objects.equals(message, other.message)
                && Objects.equals(method, other.method)
                && shouldIgnore == other.shouldIgnore
                && Objects.equals(uri, other.uri);
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder(75);
        strBuilder
                .append("[Method=")
                .append(method)
                .append(", URI=")
                .append(uri)
                .append(", Headers=")
                .append(headers)
                .append(", Body=")
                .append(body)
                .append(", ShouldIgnore=")
                .append(shouldIgnore)
                .append(", Depth=")
                .append(depth)
                .append(", Message=")
                .append(message);
        return strBuilder.toString();
    }

    /**
     * Gets a new builder for a spider resource found.
     *
     * @return a new spider resource found builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets a new spider resource found builder, using the values of the supplied resource found.
     *
     * @param spiderResourceFound spider resource found for reference.
     * @return a new spider resource found builder.
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     */
    public static Builder builder(SpiderResourceFound spiderResourceFound) {
        if (spiderResourceFound == null) {
            throw new IllegalArgumentException("Parameter spiderResourceFound must not be null.");
        }
        return new Builder(spiderResourceFound);
    }

    /** Builder of {@link SpiderResourceFound}. */
    public static class Builder {
        private HttpMessage message;
        private int depth;
        private String method;
        private String uri;
        private String body;
        private boolean shouldIgnore;
        private List<HttpHeaderField> headers;

        private Builder() {
            this.message = null;
            this.depth = 0;
            this.method = HttpRequestHeader.GET;
            this.uri = "";
            this.body = "";
            this.shouldIgnore = false;
            this.headers = Collections.emptyList();
        }

        private Builder(SpiderResourceFound resourceFound) {
            this.message = resourceFound.getMessage();
            this.depth = resourceFound.getDepth();
            this.method = resourceFound.getMethod();
            this.uri = resourceFound.getUri();
            this.body = resourceFound.getBody();
            this.shouldIgnore = resourceFound.isShouldIgnore();
            this.headers = resourceFound.getHeaders();
        }

        /**
         * Sets the message where the resource was found.
         *
         * @param message HTTP message where the resource as found (null allowed).
         * @return the builder.
         */
        public Builder setMessage(HttpMessage message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the spider depth for the resource found.
         *
         * @param depth Spider depth for resource.
         * @return the builder.
         * @throws IllegalArgumentException if the given parameter is negative.
         */
        public Builder setDepth(int depth) {
            if (depth < 0) {
                throw new IllegalArgumentException("Parameter depth must not be negative.");
            }
            this.depth = depth;
            return this;
        }

        /**
         * Sets the method for the resource found.
         *
         * @param method Method for resource found.
         * @return the builder.
         * @throws IllegalArgumentException if the given parameter is {@code null}.
         */
        public Builder setMethod(String method) {
            if (method == null) {
                throw new IllegalArgumentException("Parameter method must not be null.");
            }
            this.method = method;
            return this;
        }

        /**
         * Sets the URI for the resource found.
         *
         * @param uri URI for resource found.
         * @return the builder.
         * @throws IllegalArgumentException if the given parameter is {@code null}.
         */
        public Builder setUri(String uri) {
            if (uri == null) {
                throw new IllegalArgumentException("Parameter uri must not be null.");
            }
            this.uri = uri;
            return this;
        }

        /**
         * Sets the request body for the resource found.
         *
         * @param body Body for resource found.
         * @return the builder.
         * @throws IllegalArgumentException if the given parameter is {@code null}.
         */
        public Builder setBody(String body) {
            if (body == null) {
                throw new IllegalArgumentException("Parameter body must not be null.");
            }
            this.body = body;
            return this;
        }

        /**
         * Sets a flag whether the resource should be ignored.
         *
         * @param shouldIgnore Flag for resource found to be ignored.
         * @return the builder.
         */
        public Builder setShouldIgnore(boolean shouldIgnore) {
            this.shouldIgnore = shouldIgnore;
            return this;
        }

        /**
         * Sets additional request headers for the resource found. Only valid header fields (i.e.
         * name not {@code null} and not empty, value not {@code null}) will be added.
         *
         * @param headers Additional request headers for resource found.
         * @return the builder.
         * @throws IllegalArgumentException if the given parameter is {@code null} or an element is
         *     {@code null}.
         */
        public Builder setHeaders(List<HttpHeaderField> headers) {
            if (headers == null) {
                throw new IllegalArgumentException("Parameter headers must not be null.");
            }
            if (!headers.isEmpty()) {
                List<HttpHeaderField> validHeaders = new ArrayList<>();
                for (HttpHeaderField headerField : headers) {
                    if (headerField == null) {
                        throw new IllegalArgumentException("Element of headers must not be null.");
                    }
                    if (headerField.getName() != null
                            && !headerField.getName().trim().isEmpty()
                            && headerField.getValue() != null) {
                        validHeaders.add(headerField);
                    }
                }
                this.headers = Collections.unmodifiableList(validHeaders);
            } else {
                this.headers = Collections.emptyList();
            }
            return this;
        }

        /**
         * Builds a new {@code SpiderResourceFound}, with the configurations previously set.
         *
         * @return a new {@code SpiderResourceFound}.
         */
        public SpiderResourceFound build() {
            return new SpiderResourceFound(
                    message, depth, uri, method, body, shouldIgnore, headers);
        }
    }
}

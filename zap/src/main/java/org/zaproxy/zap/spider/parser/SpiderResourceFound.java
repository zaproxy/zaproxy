/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

/**
 * Class SpiderResourceFound is used to store information about found resources by spider parsers.
 *
 * @since 2.11.0
 */
public class SpiderResourceFound {
    /** The message where the resource was found. */
    private HttpMessage message;
    /** Spider depth for resource. */
    private int depth;
    /** HTTP method for resource. */
    private String method;
    /** Uniform resource identifier of resource. */
    private String uri;
    /** Body for the resource. */
    private String body;
    /** Defines resource as useful or not useful in the fetching process. */
    private boolean shouldIgnore;
    /** Additional request headers to be passed for the resource. */
    private List<HttpHeaderField> requestHeaders;

    /**
     * Instantiates a spider resource found.
     *
     * @param message the message for the found resource
     * @param depth the depth of this resource in the crawling process
     * @param uri the universal resource locator
     * @param method HTTP method for the resource found
     * @param body request body for the resource
     * @param shouldIgnore flag to ignore the resource found
     * @param requestHeaders additional request headers for the resource
     */
    private SpiderResourceFound(
            HttpMessage message,
            int depth,
            String uri,
            String method,
            String body,
            boolean shouldIgnore,
            List<HttpHeaderField> requestHeaders) {
        this.message = message;
        this.depth = depth;
        this.uri = uri;
        this.method = method;
        this.body = body;
        this.shouldIgnore = shouldIgnore;
        this.requestHeaders = requestHeaders;
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
     * Returns a deep copy of additional request headers for the resource found.
     *
     * @return list of HTTP header fields
     */
    public List<HttpHeaderField> getRequestHeaders() {
        List<HttpHeaderField> requestHeadersCopy = new ArrayList<>();
        for (HttpHeaderField headerField : requestHeaders) {
            requestHeadersCopy.add(
                    new HttpHeaderField(headerField.getName(), headerField.getValue()));
        }
        return requestHeadersCopy;
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
        private List<HttpHeaderField> requestHeaders = Collections.emptyList();

        private Builder() {
            this.message = null;
            this.depth = 0;
            this.method = HttpRequestHeader.GET;
            this.uri = "";
            this.body = "";
            this.shouldIgnore = false;
            this.requestHeaders = Collections.emptyList();
        }

        private Builder(SpiderResourceFound resourceFound) {
            this.message = new HttpMessage(resourceFound.getMessage());
            this.depth = resourceFound.getDepth();
            this.method = resourceFound.getMethod();
            this.uri = resourceFound.getUri();
            this.body = resourceFound.getBody();
            this.shouldIgnore = resourceFound.isShouldIgnore();
            this.requestHeaders = new ArrayList<>();
            for (HttpHeaderField headerField : resourceFound.getRequestHeaders()) {
                this.requestHeaders.add(
                        new HttpHeaderField(headerField.getName(), headerField.getValue()));
            }
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
         * @param requestHeaders Additional request headers for resource found.
         * @return the builder.
         * @throws IllegalArgumentException if the given parameter is {@code null} or an element is
         *     {@code null}.
         */
        public Builder setRequestHeaders(List<HttpHeaderField> requestHeaders) {
            if (requestHeaders == null) {
                throw new IllegalArgumentException("Parameter requestHeaders must not be null.");
            }
            if (!requestHeaders.isEmpty()) {
                this.requestHeaders = new ArrayList<>();
                for (HttpHeaderField headerField : requestHeaders) {
                    if (headerField == null) {
                        throw new IllegalArgumentException(
                                "Element of requestHeaders must not be null.");
                    }
                    if (headerField.getName() != null
                            && !headerField.getName().trim().isEmpty()
                            && headerField.getValue() != null) {
                        this.requestHeaders.add(
                                new HttpHeaderField(headerField.getName(), headerField.getValue()));
                    }
                }
            } else {
                this.requestHeaders = Collections.emptyList();
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
                    message, depth, uri, method, body, shouldIgnore, requestHeaders);
        }
    }
}

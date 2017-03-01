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
package org.zaproxy.zap.spider.parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

import net.htmlparser.jericho.Source;

/**
 * Class with helper/utility methods to help testing classes involving {@code SpiderParser} implementations.
 *
 * @see org.zaproxy.zap.spider.parser.SpiderParser
 */
public class SpiderParserTestUtils {

    protected static Source createSource(HttpMessage messageHtmlResponse) {
        return new Source(messageHtmlResponse.getResponseBody().toString());
    }

    protected static SpiderParam createSpiderParamWithConfig() {
        SpiderParam spiderParam = new SpiderParam();
        spiderParam.load(new ZapXmlConfiguration());
        return spiderParam;
    }

    protected static String readFile(Path file) throws IOException {
        StringBuilder strBuilder = new StringBuilder();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            strBuilder.append(line).append('\n');
        }
        return strBuilder.toString();
    }

    public static TestSpiderParserListener createTestSpiderParserListener() {
        return new TestSpiderParserListener();
    }

    public static class TestSpiderParserListener implements SpiderParserListener {

        private final List<SpiderResource> resources;
        private final List<String> urls;

        private TestSpiderParserListener() {
            resources = new ArrayList<>();
            urls = new ArrayList<>();
        }

        public int getNumberOfUrlsFound() {
            return resources.size();
        }

        public List<String> getUrlsFound() {
            return urls;
        }

        public int getNumberOfResourcesFound() {
            return resources.size();
        }

        public List<SpiderResource> getResourcesFound() {
            return resources;
        }

        @Override
        public void resourceURIFound(HttpMessage responseMessage, int depth, String uri) {
            urls.add(uri);
            resources.add(uriResource(responseMessage, depth, uri));
        }

        @Override
        public void resourceURIFound(HttpMessage responseMessage, int depth, String uri, boolean shouldIgnore) {
            urls.add(uri);
            resources.add(uriResource(responseMessage, depth, uri, shouldIgnore));
        }

        @Override
        public void resourcePostURIFound(HttpMessage responseMessage, int depth, String uri, String requestBody) {
            urls.add(uri);
            resources.add(postResource(responseMessage, depth, uri, requestBody));
        }

        public boolean isResourceFound() {
            return false;
        }
    }

    public static SpiderResource uriResource(HttpMessage message, int depth, String uri) {
        return new SpiderResource(message, depth, uri);
    }

    public static SpiderResource uriResource(HttpMessage message, int depth, String uri, boolean shouldIgnore) {
        return new SpiderResource(message, depth, uri, shouldIgnore);
    }

    public static SpiderResource postResource(HttpMessage message, int depth, String uri, String requestBody) {
        return new SpiderResource(message, depth, uri, requestBody);
    }

    public static String params(String... params) {
        if (params == null || params.length == 0) {
            return "";
        }

        StringBuilder strBuilder = new StringBuilder();
        for (String param : params) {
            if (strBuilder.length() > 0) {
                strBuilder.append('&');
            }
            strBuilder.append(param);
        }
        return strBuilder.toString();
    }

    public static String param(String name, String value) {
        try {
            return URLEncoder.encode(name, StandardCharsets.UTF_8.name()) + "="
                    + URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static class SpiderResource {

        private final HttpMessage message;
        private final int depth;
        private final String uri;

        private final boolean shouldIgnore;

        private final String requestBody;

        private SpiderResource(HttpMessage message, int depth, String uri) {
            this.message = message;
            this.depth = depth;
            this.uri = uri;
            this.requestBody = null;
            this.shouldIgnore = false;
        }

        private SpiderResource(HttpMessage message, int depth, String uri, boolean shouldIgnore) {
            this.message = message;
            this.depth = depth;
            this.uri = uri;
            this.requestBody = null;
            this.shouldIgnore = shouldIgnore;
        }

        private SpiderResource(HttpMessage message, int depth, String uri, String requestBody) {
            this.message = message;
            this.depth = depth;
            this.uri = uri;
            this.requestBody = requestBody;
            this.shouldIgnore = false;
        }

        public HttpMessage getMessage() {
            return message;
        }

        public int getDepth() {
            return depth;
        }

        public String getUri() {
            return uri;
        }

        public boolean isShouldIgnore() {
            return shouldIgnore;
        }

        public String getRequestBody() {
            return requestBody;
        }

        @Override
        public int hashCode() {
            int result = 31 + depth;
            result = 31 * result + ((message == null) ? 0 : message.hashCode());
            result = 31 * result + ((requestBody == null) ? 0 : requestBody.hashCode());
            result = 31 * result + (shouldIgnore ? 1231 : 1237);
            result = 31 * result + ((uri == null) ? 0 : uri.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SpiderResource other = (SpiderResource) obj;
            if (depth != other.depth) {
                return false;
            }
            if (message == null) {
                if (other.message != null) {
                    return false;
                }
            } else if (message != other.message) {
                return false;
            }
            if (requestBody == null) {
                if (other.requestBody != null) {
                    return false;
                }
            } else if (!requestBody.equals(other.requestBody)) {
                return false;
            }
            if (shouldIgnore != other.shouldIgnore) {
                return false;
            }
            if (uri == null) {
                if (other.uri != null) {
                    return false;
                }
            } else if (!uri.equals(other.uri)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder strBuilder = new StringBuilder(250);
            strBuilder.append("URI=").append(uri);
            strBuilder.append(", Depth=").append(depth);
            strBuilder.append(", RequestBody=").append(requestBody);
            strBuilder.append(", ShouldIgnore=").append(shouldIgnore);
            strBuilder.append(", Message=").append(message.hashCode());
            return strBuilder.toString();
        }
    }
}

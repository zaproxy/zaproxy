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
import net.htmlparser.jericho.Source;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.testutils.TestUtils;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/**
 * Class with helper/utility methods to help testing classes involving {@code SpiderParser}
 * implementations.
 *
 * @see org.zaproxy.zap.spider.parser.SpiderParser
 */
class SpiderParserTestUtils extends TestUtils {

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

    static TestSpiderParserListener createTestSpiderParserListener() {
        return new TestSpiderParserListener();
    }

    static TestSpiderParserListener createAndAddTestSpiderParserListener(SpiderParser parser) {
        TestSpiderParserListener listener = createTestSpiderParserListener();
        parser.addSpiderParserListener(listener);
        return listener;
    }

    protected static class TestSpiderParserListener implements SpiderParserListener {

        private final List<SpiderResource> resources;
        private final List<String> urls;

        private TestSpiderParserListener() {
            resources = new ArrayList<>();
            urls = new ArrayList<>();
        }

        int getNumberOfUrlsFound() {
            return urls.size();
        }

        List<String> getUrlsFound() {
            return urls;
        }

        int getNumberOfResourcesFound() {
            return resources.size();
        }

        List<SpiderResource> getResourcesFound() {
            return resources;
        }

        @Override
        public void resourceFound(SpiderResourceFound resourceFound) {
            urls.add(resourceFound.getUri());
            resources.add(new SpiderResource(resourceFound));
        }

        boolean isResourceFound() {
            return false;
        }
    }

    static SpiderResource uriResource(HttpMessage message, int depth, String uri) {
        return new SpiderResource(
                SpiderResourceFound.builder()
                        .setMessage(message)
                        .setDepth(depth)
                        .setUri(uri)
                        .build());
    }

    static SpiderResource uriResource(
            HttpMessage message, int depth, String uri, boolean shouldIgnore) {
        return uriResource(message, depth, uri, shouldIgnore, new ArrayList<>());
    }

    static SpiderResource uriResource(
            HttpMessage message,
            int depth,
            String uri,
            boolean shouldIgnore,
            List<HttpHeaderField> requestHeaders) {
        SpiderResourceFound resourceFound =
                SpiderResourceFound.builder()
                        .setMessage(message)
                        .setDepth(depth)
                        .setUri(uri)
                        .setShouldIgnore(shouldIgnore)
                        .setRequestHeaders(requestHeaders)
                        .build();
        return new SpiderResource(resourceFound);
    }

    static SpiderResource postResource(
            HttpMessage message, int depth, String uri, String requestBody) {
        return postResource(message, depth, uri, requestBody, new ArrayList<>());
    }

    static SpiderResource postResource(
            HttpMessage message,
            int depth,
            String uri,
            String requestBody,
            List<HttpHeaderField> requestHeaders) {
        SpiderResourceFound resourceFound =
                SpiderResourceFound.builder()
                        .setMessage(message)
                        .setDepth(depth)
                        .setUri(uri)
                        .setMethod(HttpRequestHeader.POST)
                        .setBody(requestBody)
                        .build();
        return new SpiderResource(resourceFound);
    }

    static String params(String... params) {
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

    static String param(String name, String value) {
        try {
            return URLEncoder.encode(name, StandardCharsets.UTF_8.name())
                    + "="
                    + URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    static class SpiderResource {
        private SpiderResourceFound resourceFound;

        SpiderResource(SpiderResourceFound resourceFound) {
            this.resourceFound = resourceFound;
        }

        HttpMessage getMessage() {
            return resourceFound.getMessage();
        }

        int getDepth() {
            return resourceFound.getDepth();
        }

        String getUri() {
            return resourceFound.getUri();
        }

        boolean isShouldIgnore() {
            return resourceFound.isShouldIgnore();
        }

        String getRequestBody() {
            return resourceFound.getBody();
        }

        List<HttpHeaderField> getHeaders() {
            return resourceFound.getRequestHeaders();
        }

        String getMethod() {
            return resourceFound.getMethod();
        }

        @Override
        public int hashCode() {
            int result = 31 + getDepth();
            result = 31 * result + ((getMessage() == null) ? 0 : getMessage().hashCode());
            result = 31 * result + ((getRequestBody() == null) ? 0 : getRequestBody().hashCode());
            result = 31 * result + (isShouldIgnore() ? 1231 : 1237);
            result = 31 * result + ((getUri() == null) ? 0 : getUri().hashCode());
            result = 31 * result + ((getMethod() == null) ? 0 : getMethod().hashCode());
            result = 31 * result + ((getHeaders() == null) ? 0 : getHeaders().hashCode());
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
            if (getDepth() != other.getDepth()) {
                return false;
            }
            if (getMessage() == null) {
                if (other.getMessage() != null) {
                    return false;
                }
            } else if (getMessage() != other.getMessage()) {
                return false;
            }
            if (getRequestBody() == null) {
                if (other.getRequestBody() != null) {
                    return false;
                }
            } else if (!getRequestBody().equals(other.getRequestBody())) {
                return false;
            }
            if (isShouldIgnore() != other.isShouldIgnore()) {
                return false;
            }
            if (getUri() == null) {
                if (other.getUri() != null) {
                    return false;
                }
            } else if (!getUri().equals(other.getUri())) {
                return false;
            }
            if (getMethod() == null) {
                if (other.getMethod() != null) {
                    return false;
                }
            } else if (!getMethod().equals(other.getMethod())) {
                return false;
            }
            if (getHeaders() == null) {
                if (other.getHeaders() != null) return false;
            } else if (!getHeaders().equals(other.getHeaders())) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder strBuilder = new StringBuilder(250);
            strBuilder.append("URI=").append(getUri());
            strBuilder.append(", Depth=").append(getDepth());
            strBuilder.append(", RequestBody=").append(getRequestBody());
            strBuilder.append(", ShouldIgnore=").append(isShouldIgnore());
            strBuilder.append(", Message=").append(getMessage().hashCode());
            strBuilder.append(", Method=").append(getMethod());
            strBuilder.append(", Headers=").append(getHeaders().hashCode());
            return strBuilder.toString();
        }
    }
}

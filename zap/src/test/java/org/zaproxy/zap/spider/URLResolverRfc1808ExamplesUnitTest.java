/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.spider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

/** Unit test for RFC 1808 compliance of {@link org.zaproxy.zap.spider.URLResolver}. */
class URLResolverRfc1808ExamplesUnitTest {

    /**
     * @see <a href="https://tools.ietf.org/html/rfc1808#section-5.1">RFC 1808 - 5.1. Normal
     *     Examples</a>
     */
    @Test
    void resolveRfc1808NormalExamples() {
        final String baseUrl = "http://a/b/c/d;p?q#f";

        assertThat(URLResolver.resolveUrl(baseUrl, "g:h"), is("g:h"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g"), is("http://a/b/c/g"));
        assertThat(URLResolver.resolveUrl(baseUrl, "./g"), is("http://a/b/c/g"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g/"), is("http://a/b/c/g/"));
        assertThat(URLResolver.resolveUrl(baseUrl, "/g"), is("http://a/g"));
        assertThat(URLResolver.resolveUrl(baseUrl, "//g"), is("http://g"));
        assertThat(URLResolver.resolveUrl(baseUrl, "?y"), is("http://a/b/c/d;p?y"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g?y"), is("http://a/b/c/g?y"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g?y/./x"), is("http://a/b/c/g?y/./x"));
        assertThat(URLResolver.resolveUrl(baseUrl, "#s"), is("http://a/b/c/d;p?q#s"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g#s"), is("http://a/b/c/g#s"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g#s/./x"), is("http://a/b/c/g#s/./x"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g?y#s"), is("http://a/b/c/g?y#s"));
        assertThat(URLResolver.resolveUrl(baseUrl, ";x"), is("http://a/b/c/d;x"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g;x"), is("http://a/b/c/g;x"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g;x?y#s"), is("http://a/b/c/g;x?y#s"));
        assertThat(URLResolver.resolveUrl(baseUrl, "."), is("http://a/b/c/"));
        assertThat(URLResolver.resolveUrl(baseUrl, "./"), is("http://a/b/c/"));
        assertThat(URLResolver.resolveUrl(baseUrl, ".."), is("http://a/b/"));
        assertThat(URLResolver.resolveUrl(baseUrl, "../"), is("http://a/b/"));
        assertThat(URLResolver.resolveUrl(baseUrl, "../g"), is("http://a/b/g"));
        assertThat(URLResolver.resolveUrl(baseUrl, "../.."), is("http://a/"));
        assertThat(URLResolver.resolveUrl(baseUrl, "../../"), is("http://a/"));
        assertThat(URLResolver.resolveUrl(baseUrl, "../../g"), is("http://a/g"));
    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc1808#section-5.2">RFC 1808 - 5.2. Abnormal
     *     Examples</a>
     */
    @Test
    void resolveRfc1808AbnormalExamples() {
        final String baseUrl = "http://a/b/c/d;p?q#f";

        assertThat(URLResolver.resolveUrl(baseUrl, ""), is("http://a/b/c/d;p?q#f"));

        // Deviations from RFC 1808 to match browsers' behaviour.
        // Expected by RFC 1808:
        // assertThat(URLResolver.resolveUrl(baseUrl, "../../../g"), is("http://a/../g"));
        // assertThat(URLResolver.resolveUrl(baseUrl, "../../../../g"), is("http://a/../../g"));
        // assertThat(URLResolver.resolveUrl(baseUrl, "/../g"), is("http://a/../g"));
        // Browsers' behaviour:
        assertThat(URLResolver.resolveUrl(baseUrl, "../../../g"), is("http://a/g"));
        assertThat(URLResolver.resolveUrl(baseUrl, "../../../../g"), is("http://a/g"));
        assertThat(URLResolver.resolveUrl(baseUrl, "/../g"), is("http://a/g"));

        assertThat(URLResolver.resolveUrl(baseUrl, "/./g"), is("http://a/./g"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g."), is("http://a/b/c/g."));
        assertThat(URLResolver.resolveUrl(baseUrl, ".g"), is("http://a/b/c/.g"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g.."), is("http://a/b/c/g.."));
        assertThat(URLResolver.resolveUrl(baseUrl, "..g"), is("http://a/b/c/..g"));
        assertThat(URLResolver.resolveUrl(baseUrl, "./../g"), is("http://a/b/g"));
        assertThat(URLResolver.resolveUrl(baseUrl, "./g/."), is("http://a/b/c/g/"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g/./h"), is("http://a/b/c/g/h"));
        assertThat(URLResolver.resolveUrl(baseUrl, "g/../h"), is("http://a/b/c/h"));
        assertThat(URLResolver.resolveUrl(baseUrl, "http:g"), is("http:g"));
        assertThat(URLResolver.resolveUrl(baseUrl, "http:"), is("http:"));
    }
}

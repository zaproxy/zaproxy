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
package org.parosproxy.paros.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit test for {@link HttpRequestHeader}. */
class HttpRequestHeaderUnitTest {

    @Test
    void shouldBeEmptyIfNoContents() {
        // Given
        HttpRequestHeader header = new HttpRequestHeader();
        // When
        boolean empty = header.isEmpty();
        // Then
        assertThat(empty, is(equalTo(true)));
    }

    @Test
    void shouldNotBeEmptyIfItHasRequestLine() throws Exception {
        // Given
        HttpRequestHeader header =
                new HttpRequestHeader("GET http://example.com/ HTTP/1.1\r\n\r\n");
        // When
        boolean empty = header.isEmpty();
        // Then
        assertThat(empty, is(equalTo(false)));
    }

    @Test
    void shouldNotBeEmptyIfItHasRequestLineAndHeaders() throws Exception {
        // Given
        HttpRequestHeader header =
                new HttpRequestHeader("GET / HTTP/1.1\r\nHost: example.com\r\n\r\n");
        // When
        boolean empty = header.isEmpty();
        // Then
        assertThat(empty, is(equalTo(false)));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "HTTP/0.9",
                "HTTP/1.0",
                "HTTP/1.1",
                "HTTP/1.2",
                "HTTP/2",
                "HTTP/3.0",
                "HTTP/4.5"
            })
    void shouldParseWithArbitraryHttpVersions(String version) throws Exception {
        // Given
        HttpRequestHeader header =
                new HttpRequestHeader("GET http://example.com/ " + version + "\r\n\r\n");
        // When
        String parsedVersion = header.getVersion();
        // Then
        assertThat(parsedVersion, is(equalTo(version)));
    }

    @Test
    void shouldCreateConnectRequest() throws Exception {
        // Given
        String data = "CONNECT example.com:443 HTTP/1.1\\r\\nHost: example.com:443\\r\\n\\r\\n";
        // When
        HttpRequestHeader header = new HttpRequestHeader(data);
        // Then
        assertThat(header.getMethod(), is(equalTo("CONNECT")));
        assertThat(header.getHostName(), is(equalTo("example.com")));
        assertThat(header.getHostPort(), is(equalTo(443)));
        assertThat(header.getURI().getAuthority(), is(equalTo("example.com:443")));
        assertThat(header.getURI().getHost(), is(equalTo("example.com")));
        assertThat(header.getURI().getPort(), is(equalTo(443)));
        assertThat(header.getURI().toString(), is(equalTo("example.com:443")));
    }

    @Test
    void shouldNotBeImageIfItHasNoRequestUri() {
        // Given
        HttpRequestHeader header = new HttpRequestHeader();
        // When
        boolean image = header.isImage();
        // Then
        assertThat(image, is(equalTo(false)));
    }

    @Test
    void shouldNotBeImageIfRequestUriHasNoPath() throws Exception {
        // Given
        HttpRequestHeader header = new HttpRequestHeader();
        header.setURI(new URI("http://example.com", true));
        // When
        boolean image = header.isImage();
        // Then
        assertThat(image, is(equalTo(false)));
    }

    @Test
    void shouldBeImageIfRequestUriHasPathWithImageExtension() throws Exception {
        // Given
        String[] extensions = {"bmp", "ico", "jpg", "jpeg", "gif", "tiff", "tif", "png"};
        HttpRequestHeader header = new HttpRequestHeader();
        for (String extension : extensions) {
            header.setURI(new URI("http://example.com/image." + extension, true));
            // When
            boolean image = header.isImage();
            // Then
            assertThat(image, is(equalTo(true)));
        }
    }

    @Test
    void shouldSetCookieParam() {
        // Given
        HttpRequestHeader header = new HttpRequestHeader();
        TreeSet<HtmlParameter> cookies = parameters(cookieParam("c1", "v1"));
        // When
        header.setCookieParams(cookies);
        // Then
        assertThat(header.getHeader(HttpHeader.COOKIE), is(equalTo("c1=v1")));
        assertThat(header.getHeaderValues(HttpHeader.COOKIE), hasSize(1));
    }

    @Test
    void shouldSetCookieParams() {
        // Given
        HttpRequestHeader header = new HttpRequestHeader();
        TreeSet<HtmlParameter> cookies =
                parameters(cookieParam("c1", "v1"), cookieParam("c2", "v2"), cookieParam("", "v3"));
        // When
        header.setCookieParams(cookies);
        // Then
        assertThat(header.getHeader(HttpHeader.COOKIE), is(equalTo("v3; c1=v1; c2=v2")));
        assertThat(header.getHeaderValues(HttpHeader.COOKIE), hasSize(1));
    }

    @Test
    void shouldSetCookieParamWithEmptyName() {
        // Given
        HttpRequestHeader header = new HttpRequestHeader();
        TreeSet<HtmlParameter> cookies = parameters(cookieParam("", "v1"));
        // When
        header.setCookieParams(cookies);
        // Then
        assertThat(header.getHeader(HttpHeader.COOKIE), is(equalTo("v1")));
        assertThat(header.getHeaderValues(HttpHeader.COOKIE), hasSize(1));
    }

    @Test
    void shouldRemoveCookieHeaderIfEmptyCookieParam() {
        // Given
        HttpRequestHeader header = new HttpRequestHeader();
        TreeSet<HtmlParameter> cookies = parameters(cookieParam("", ""));
        // When
        header.setCookieParams(cookies);
        // Then
        assertThat(header.getHeaderValues(HttpHeader.COOKIE), is(empty()));
    }

    @Test
    void shouldRemoveCookieHeadersWhenSettingNoCookieParams() {
        // Given
        HttpRequestHeader header = createRequestHeaderWithCookies();
        TreeSet<HtmlParameter> noCookies = new TreeSet<>();
        // When
        header.setCookieParams(noCookies);
        // Then
        assertThat(header.getHeaderValues(HttpHeader.COOKIE), is(empty()));
    }

    @Test
    void shouldRemoveCookieHeadersWhenSettingNoCookieTypeParams() {
        // Given
        HttpRequestHeader header = createRequestHeaderWithCookies();
        TreeSet<HtmlParameter> paramsWithoutCookies =
                parameters(urlParam("p1", "v1"), formParam("p2", "v2"));
        // When
        header.setCookieParams(paramsWithoutCookies);
        // Then
        assertThat(header.getHeaderValues(HttpHeader.COOKIE), is(empty()));
    }

    @Test
    void shouldReplaceAnyCookieHeaderWhenSettingCookieParams() {
        // Given
        HttpRequestHeader header = createRequestHeaderWithCookies();
        TreeSet<HtmlParameter> cookies =
                parameters(cookieParam("c1", "v1"), cookieParam("c2", "v2"), cookieParam("", "v3"));
        // When
        header.setCookieParams(cookies);
        // Then
        assertThat(header.getHeader(HttpHeader.COOKIE), is(equalTo("v3; c1=v1; c2=v2")));
        assertThat(header.getHeaderValues(HttpHeader.COOKIE), hasSize(1));
    }

    @Test
    void shouldNotHaveContentLengthHeaderByDefault() throws Exception {
        // Given / When
        URI uri = new URI("http://example.com", true);
        HttpRequestHeader header =
                new HttpRequestHeader(HttpRequestHeader.GET, uri, HttpHeader.HTTP11);
        // Then
        assertThat(header.getHeaderValues(HttpHeader.CONTENT_LENGTH), is(empty()));
    }

    private static Stream<Arguments> falseTestCssUrls() {
        return falseTestUrls("css");
    }

    @ParameterizedTest
    @MethodSource("falseTestCssUrls")
    void isCssShouldReturnFalseWhenUrlDoesNotIndicateCss(String url) {
        // Given
        HttpRequestHeader reqHeader = createRequestHeader(url);
        // When / Then
        assertFalse(reqHeader.isCss());
    }

    private static Stream<Arguments> trueTestCssUrls() {
        return trueTestUrls("css");
    }

    @ParameterizedTest
    @MethodSource("trueTestCssUrls")
    void isCssShouldReturnTrueWhenUrlIndicatesCss(String url) {
        // Given
        HttpRequestHeader reqHeader = createRequestHeader(url);
        // When / Then
        assertTrue(reqHeader.isCss());
    }

    private static Stream<Arguments> falseTestImageUrls() {
        return falseTestUrls("gif");
    }

    @ParameterizedTest
    @MethodSource("falseTestImageUrls")
    void isImageShouldReturnFalseWhenUrlDoesNotIndicateImage(String url) {
        // Given
        HttpRequestHeader reqHeader = createRequestHeader(url);
        // When / Then
        assertFalse(reqHeader.isImage());
    }

    private static Stream<Arguments> trueTestImageUrls() {
        // Per HttpRequestHeader.patternImage
        return trueTestUrls("bmp", "ico", "jpg", "jpeg", "gif", "tiff", "tif", "png", "svg");
    }

    @ParameterizedTest
    @MethodSource("trueTestImageUrls")
    void isImageShouldReturnTrueWhenUrlIndicatesImage(String url) {
        // Given
        HttpRequestHeader reqHeader = createRequestHeader(url);
        // When / Then
        assertTrue(reqHeader.isImage());
    }

    private static Stream<Arguments> falseTestUrls(String extension) {
        List<Arguments> urls = new ArrayList<>();
        // In directory path
        urls.add(arguments("http://example.org/" + extension + "/file.ext"));
        // In domain name
        urls.add(arguments("http://domain" + extension + ".com/"));
        // In domain extension (TLD)
        urls.add(arguments("https://example." + extension));
        // In domain extension (TLD)
        urls.add(arguments("https://example." + extension + "/dir/file.ext"));
        // In parameter value
        urls.add(arguments("https://example.org/dir/file?foo=bar&thing=" + extension));
        // In parameter value including period
        urls.add(
                arguments(
                        "http://example.org/"
                                + extension
                                + "/file.ext?foo=bar&type=."
                                + extension));
        // In parameter name
        urls.add(arguments("https://example.org/dir/file?foo=bar&" + extension + "=file.ext"));
        // In parameter name and value
        urls.add(
                arguments(
                        "https://example.org/dir/file?foo=bar&"
                                + extension
                                + "=file."
                                + extension));
        return urls.stream();
    }

    private static Stream<Arguments> trueTestUrls(String... exts) {
        List<Arguments> urls = new ArrayList<>();
        for (String ext : exts) {
            // In path
            urls.add(arguments("http://example.org/example." + ext));
            // In deeper path
            urls.add(arguments("http://example.org/assets/images/example." + ext));
            // In path, ignoring params
            urls.add(arguments("http://example.org/images/example." + ext + "?foo=bar"));
            // In path, ignoring params
            urls.add(
                    arguments(
                            "http://example.org/images/example." + ext + "?foo=bar&thing=." + ext));
        }
        return urls.stream();
    }

    private static HttpRequestHeader createRequestHeader(String url) {
        HttpRequestHeader hrh = new HttpRequestHeader();
        try {
            hrh.setURI(new URI(url, false));
        } catch (URIException | NullPointerException hmhe) {
            // Should not happen
        }
        return hrh;
    }

    private static HtmlParameter urlParam(String name, String value) {
        return param(HtmlParameter.Type.url, name, value);
    }

    private static HtmlParameter formParam(String name, String value) {
        return param(HtmlParameter.Type.form, name, value);
    }

    private static HtmlParameter cookieParam(String name, String value) {
        return param(HtmlParameter.Type.cookie, name, value);
    }

    private static HtmlParameter param(HtmlParameter.Type type, String name, String value) {
        return new HtmlParameter(type, name, value);
    }

    private static TreeSet<HtmlParameter> parameters(HtmlParameter... params) {
        TreeSet<HtmlParameter> parameters = new TreeSet<>();
        if (params == null || params.length == 0) {
            return parameters;
        }
        for (HtmlParameter param : params) {
            parameters.add(param);
        }
        return parameters;
    }

    private static HttpRequestHeader createRequestHeaderWithCookies() {
        try {
            HttpRequestHeader header =
                    new HttpRequestHeader(
                            "GET / HTTP/1.1\r\n"
                                    + "Cookie: cookie1=value1; cookie2=value2\r\n"
                                    + "Cookie: cookie3=value3; cookie4=value4\r\n");
            return header;
        } catch (HttpMalformedHeaderException e) {
            throw new RuntimeException(e);
        }
    }
}

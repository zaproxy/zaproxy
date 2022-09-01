/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.function.Predicate;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * This test ensure that nothing was broken in the handling of normal URLs during the implementation
 * of OData support as well as ensure that the OData support is correct.
 *
 * <p>It checks the canonicalization mechanism used to verify if a URL has already been visited
 * before during the spider phase.
 */
@SuppressWarnings("deprecation")
class URLCanonicalizerUnitTest {

    @Test
    void shouldRemoveDefaultPortOfHttpUriWhenCanonicalizing() {
        // Given
        String uri = "http://example.com:80/";
        // When
        String canonicalizedUri = org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/")));
    }

    @Test
    void shouldNotRemoveNonDefaultPortOfHttpUriWhenCanonicalizing() {
        // Given
        String uri = "http://example.com:443/";
        // When
        String canonicalizedUri = org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com:443/")));
    }

    @Test
    void shouldRemoveDefaultPortOfHttpsUriWhenCanonicalizing() {
        // Given
        String uri = "https://example.com:443/";
        // When
        String canonicalizedUri = org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("https://example.com/")));
    }

    @Test
    void shouldNotRemoveNonDefaultPortOfHttpsUriWhenCanonicalizing() {
        // Given
        String uri = "https://example.com:80/";
        // When
        String canonicalizedUri = org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("https://example.com:80/")));
    }

    @Test
    void shouldAddEmptyPathIfUriHasNoPathWhenCanonicalizing() {
        // Given
        String uri = "http://example.com";
        // When
        String canonicalizedUri = org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/")));
    }

    @Test
    void shouldCanonicalizeURIsWithAuthority() {
        // Given
        String[] uris = {"http://example.com/", "https://example.com/", "ftp://example.com/"};
        for (String uri : uris) {
            // When
            String canonicalizedUri = org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uri);
            // Then
            assertThat(canonicalizedUri, canonicalizedUri, is(equalTo(uri)));
        }
    }

    @Test
    void shouldUseBaseURIToResolveRelativeURIsWhenCanonicalizing() {
        // Given
        String baseURI = "http://example.com/path/";
        String[] relativeURIs = {"relative", "a/b/c", "../", "/absolute/path", ""};
        String[] expectedCanonicalURIs = {
            "http://example.com/path/relative",
            "http://example.com/path/a/b/c",
            "http://example.com/",
            "http://example.com/absolute/path",
            "http://example.com/path/",
        };
        for (int i = 0; i < relativeURIs.length; i++) {
            // When
            String canonicalizedUri =
                    org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(
                            relativeURIs[i], baseURI);
            // Then
            assertThat(canonicalizedUri, canonicalizedUri, is(equalTo(expectedCanonicalURIs[i])));
        }
    }

    @Test
    void shouldNormaliseEmptyAndDotPathSegmentsWhenCanonicalizing() {
        // Given
        String[] uris = {
            "http://example.com/../../x",
            "http://example.com/a//b/c//",
            "http://example.com/a/./b/./c",
            "http://example.com/.."
        };
        String[] expectedCanonicalURIs = {
            "http://example.com/x",
            "http://example.com/a/b/c/",
            "http://example.com/a/b/c",
            "http://example.com/.."
        };
        for (int i = 0; i < uris.length; i++) {
            // When
            String canonicalizedUri =
                    org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uris[i]);
            // Then
            assertThat(canonicalizedUri, canonicalizedUri, is(equalTo(expectedCanonicalURIs[i])));
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "javascript:",
                "javascript:ignore()",
                "mailto:ignore@example.com",
                "tel:+1-900-555-0191"
            })
    void shouldIgnoreURIsWithNoAuthority(String uri) {
        // Given / When
        String canonicalizedUri = org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, canonicalizedUri, is(equalTo(null)));
    }

    @Test
    void shouldReturnCanonicalUriWithPercentEncodedPath() throws URIException {
        // Given
        String uri = new URI("http://example.com/path/%C3%A1/", true).toString();
        // When
        String canonicalizedUri = org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/path/%C3%A1/")));
    }

    @Test
    void shouldReturnCanonicalUriWithPercentEncodedQuery() throws URIException {
        // Given
        String uri = new URI("http://example.com/path/?par%C3%A2m=v%C3%A3lue", true).toString();
        // When
        String canonicalizedUri = org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/path/?par%C3%A2m=v%C3%A3lue")));
    }

    @Test
    void shouldCorrectlyParseQueryParameterNamesAndValuesWithAmpersandsAndEqualsWhenCanonicalizing()
            throws URIException {
        // Given
        String uri = new URI("http://example.com/?par%26am%3D1=val%26u%3De1", true).toString();
        // When
        String canonicalizedUri = org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/?par%26am%3D1=val%26u%3De1")));
    }

    @Test
    void shouldPreserveQueryParametersWithSameNameWhenCanonicalizing() throws URIException {
        // Given
        String uri =
                new URI(
                                "http://example.com/?name1=value1.1&name1=value1.2&name2=value2&name2=value3",
                                true)
                        .toString();
        // When
        String canonicalizedUri = org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(
                canonicalizedUri,
                is(
                        equalTo(
                                "http://example.com/?name1=value1.1&name1=value1.2&name2=value2&name2=value3")));
    }

    @Test
    void shouldSortQueryParametersByNameAndValueWhenCanonicalizing() throws URIException {
        // Given
        String uri =
                new URI(
                                "http://example.com/?&name2=value2&name3=value3&name1=value1.2&name1=value1.1",
                                true)
                        .toString();
        // When
        String canonicalizedUri = org.zaproxy.zap.spider.URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(
                canonicalizedUri,
                is(
                        equalTo(
                                "http://example.com/?name1=value1.1&name1=value1.2&name2=value2&name3=value3")));
    }

    @Test
    void shouldReturnPercentEncodedUriWhenCleaningParametersIn_USE_ALL_mode() throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/%C3%A1/?par%C3%A2m=v%C3%A3lue", true);
        // When
        String cleanedUri =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.USE_ALL,
                        false);
        // Then
        assertThat(
                cleanedUri, is(equalTo("http://example.com/path/%C3%A1/?par%C3%A2m=v%C3%A3lue")));
    }

    @Test
    void shouldReturnPercentEncodedUriWhenCleaningParametersIn_IGNORE_VALUE_mode()
            throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/%C3%A1/?par%C3%A2m=v%C3%A3lue1", true);
        // When
        String cleanedUri =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_VALUE,
                        false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/%C3%A1/?par%C3%A2m")));
    }

    @Test
    void shouldReturnPercentEncodedUriWhenCleaningParametersIn_IGNORE_COMPLETELY_mode()
            throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/%C3%A1/?par%C3%A2m=v%C3%A3lue", true);
        // When
        String cleanedUri =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_COMPLETELY,
                        false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/%C3%A1/")));
    }

    @Test
    void
            shouldCorrectlyParseQueryParamNamesAndValuesWithAmpersandsAndEqualsWhenCleaningParametersIn_USE_ALL_mode()
                    throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/?par%3Dam1=val%26ue1&par%26am2=val%3Due2", true);
        // When
        String cleanedUri =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.USE_ALL,
                        false);
        // Then
        assertThat(
                cleanedUri,
                is(equalTo("http://example.com/path/?par%3Dam1=val%26ue1&par%26am2=val%3Due2")));
    }

    @Test
    void
            shouldCorrectlyParseQueryParamNamesAndValuesWithAmpersandsAndEqualsWhenCleaningParametersIn_IGNORE_VALUE_mode()
                    throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/?par%3Dam1=val%26ue1&par%26am2=val%3Due2", true);
        // When
        String cleanedUri =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_VALUE,
                        false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/?par%26am2&par%3Dam1")));
    }

    @Test
    void
            shouldCorrectlyParseQueryParamNamesAndValuesWithAmpersandsAndEqualsWhenCleaningParametersIn_IGNORE_COMPLETELY_mode()
                    throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/?par%3Dam1=val%26ue1&par%26am2=val%3Due2", true);
        // When
        String cleanedUri =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_COMPLETELY,
                        false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/")));
    }

    @Test
    void shouldPreserveQueryParametersWithSameNameWhenCleaningParametersIn_USE_ALL_mode()
            throws URIException {
        // Given
        URI uri =
                new URI(
                        "http://example.com/path/?param%5B%5D=value1.1&param%5B%5D=value1.2&param2=value2",
                        true);
        // When
        String cleanedUri =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.USE_ALL,
                        false);
        // Then
        assertThat(
                cleanedUri,
                is(
                        equalTo(
                                "http://example.com/path/?param%5B%5D=value1.1&param%5B%5D=value1.2&param2=value2")));
    }

    @Test
    void shouldKeepJustOneQueryParameterWithSameNameWhenCleaningParametersIn_IGNORE_VALUE_mode()
            throws URIException {
        // Given
        URI uri =
                new URI(
                        "http://example.com/path/?param1=value1.1&param1=value1.2&param2=value2",
                        true);
        // When
        String cleanedUri =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_VALUE,
                        false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/?param1&param2")));
    }

    @Test
    void shouldRemoveAllQueryParametersWhenCleaningParametersIn_IGNORE_COMPLETELY_mode()
            throws URIException {
        // Given
        URI uri =
                new URI(
                        "http://example.com/path/?param1=value1.1&param1=value1.2&param2=value2",
                        true);
        // When
        String cleanedUri =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_COMPLETELY,
                        false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/")));
    }

    // Test of the legacy behavior

    @Test
    void shouldCanonicalizeNormalURLWithoutParametersIn_USE_ALL_mode() throws URIException {
        URI uri = new URI("http", null, "host", 9001, "/myservlet");
        String visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.USE_ALL,
                        false /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/myservlet"));
    }

    @Test
    void shouldCanonicalizeNormalURLWithoutParametersIn_IGNORE_COMPLETELY_mode()
            throws URIException {
        URI uri = new URI("http", null, "host", 9001, "/myservlet");
        String visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_COMPLETELY,
                        false /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/myservlet"));
    }

    @Test
    void shouldCanonicalizeNormalURLWithoutParametersIn_IGNORE_VALUE_mode() throws URIException {
        URI uri = new URI("http", null, "host", 9001, "/myservlet");
        String visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_VALUE,
                        false /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/myservlet"));
    }

    @Test
    void shouldCanonicalizeNormalURLWithParametersIn_USE_ALL_mode() throws URIException {

        URI uri = new URI("http", null, "host", 9001, "/myservlet", "p1=2&p2=myparam");
        String visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.USE_ALL,
                        false /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/myservlet?p1=2&p2=myparam"));
    }

    @Test
    void shouldCanonicalizeNormalURLWithParametersIn_IGNORE_COMPLETELY_mode() throws URIException {
        URI uri = new URI("http", null, "host", 9001, "/myservlet", "p1=2&p2=myparam");
        String visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_COMPLETELY,
                        false /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/myservlet"));
    }

    @Test
    void shouldCanonicalizeNormalURLWithParametersIn_IGNORE_VALUE_mode() throws URIException {
        URI uri = new URI("http", null, "host", 9001, "/myservlet", "p1=2&p2=myparam");
        String visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_VALUE,
                        false /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/myservlet?p1&p2"));
    }

    // Test the OData behavior

    @Test
    void shouldCanonicalizeODataIDSimpleIn_USE_ALL_mode() throws URIException {
        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption spiderOption =
                org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.USE_ALL;

        URI uri = new URI("http", null, "host", 9001, "/app.svc/Book(1)");
        String visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri, spiderOption, true /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/app.svc/Book(1)"));

        uri = new URI("http", null, "host", 9001, "/app.svc/Book(1)/Author");
        visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri, spiderOption, true /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/app.svc/Book(1)/Author"));
    }

    @Test
    void shouldCanonicalizeODataIDSimpleIn_IGNORE_COMPLETELY_mode() throws URIException {
        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption spiderOption =
                org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_COMPLETELY;

        URI uri = new URI("http", null, "host", 9001, "/app.svc/Book(1)");
        String visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri, spiderOption, true /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/app.svc/Book()"));

        uri = new URI("http", null, "host", 9001, "/app.svc/Book(1)/Author");
        visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri, spiderOption, true /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/app.svc/Book()/Author"));
    }

    @Test
    void shouldCanonicalizeODataIDSimpleIn_IGNORE_VALUE_mode() throws URIException {
        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption spiderOption =
                org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_VALUE;

        URI uri = new URI("http", null, "host", 9001, "/app.svc/Book(1)");
        String visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri, spiderOption, true /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/app.svc/Book()"));

        uri = new URI("http", null, "host", 9001, "/app.svc/Book(1)/Author");
        visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri, spiderOption, true /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/app.svc/Book()/Author"));
    }

    @Test
    void shouldCanonicalizeODataIDMultipleIn_USE_ALL_mode() throws URIException {
        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption spiderOption =
                org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.USE_ALL;

        URI uri = new URI("http", null, "host", 9001, "/app.svc/Book(title='dummy',year=2012)");
        String visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri, spiderOption, true /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/app.svc/Book(title='dummy',year=2012)"));

        uri = new URI("http", null, "host", 9001, "/app.svc/Book(title='dummy',year=2012)/Author");
        visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri, spiderOption, true /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/app.svc/Book(title='dummy',year=2012)/Author"));
    }

    @Test
    void shouldCanonicalizeODataIDMultipleIn_IGNORE_COMPLETELY_mode() throws URIException {
        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption spiderOption =
                org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_COMPLETELY;

        URI uri = new URI("http", null, "host", 9001, "/app.svc/Book(title='dummy',year=2012)");
        String visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri, spiderOption, true /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/app.svc/Book()"));

        uri = new URI("http", null, "host", 9001, "/app.svc/Book(title='dummy',year=2012)/Author");
        visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri, spiderOption, true /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/app.svc/Book()/Author"));
    }

    @Test
    void shouldCanonicalizeODataIDMultipleIn_IGNORE_VALUE_mode() throws URIException {
        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption spiderOption =
                org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_VALUE;

        URI uri = new URI("http", null, "host", 9001, "/app.svc/Book(title='dummy',year=2012)");
        String visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri, spiderOption, true /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/app.svc/Book(title,year)"));

        uri = new URI("http", null, "host", 9001, "/app.svc/Book(title='dummy',year=2012)/Author");
        visitedURI =
                org.zaproxy.zap.spider.URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri, spiderOption, true /* handleODataParametersVisited */);
        assertThat(visitedURI, is("http://host:9001/app.svc/Book(title,year)/Author"));
    }

    @Test
    void shouldSkipIrrelevantQueryParametersWhenCanonicalizing() throws URIException {
        // Given
        String uri =
                new URI("http://example.com/?name1=value1&name2=value2&name3=value3", true)
                        .toString();

        Predicate<String> irrelevantParameters = Arrays.asList("name1", "name3")::contains;
        // When
        String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri, null, irrelevantParameters);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/?name2=value2")));
    }

    @Test
    void shouldSkipSessionTokensWhenCanonicalizing() throws URIException {
        // Given
        String uri =
                new URI("http://example.com/?jsessionid=id1&phpsessid=id2&aspsessionid=id3", true)
                        .toString();
        // When
        String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri, null);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/")));
    }

    @Test
    void shouldSkipIrrelevantQueryParametersWhenCleaningParametersIn_IGNORE_VALUE_mode()
            throws URIException {
        // Given
        URI uri = new URI("http://example.com/?name1=value1&name2=value2&name3=value3", true);
        Predicate<String> irrelevantParameters = Arrays.asList("name1", "name3")::contains;
        // When
        String cleanedUri =
                URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_VALUE,
                        false,
                        irrelevantParameters);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/?name2")));
    }

    @Test
    void shouldSkipSessionTokensWhenCleaningParametersIn_IGNORE_VALUE_mode() throws URIException {
        // Given
        URI uri =
                new URI("http://example.com/?jsessionid=id1&phpsessid=id2&aspsessionid=id3", true);
        // When
        String cleanedUri =
                URLCanonicalizer.buildCleanedParametersURIRepresentation(
                        uri,
                        org.zaproxy.zap.spider.SpiderParam.HandleParametersOption.IGNORE_VALUE,
                        false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/")));
    }
}

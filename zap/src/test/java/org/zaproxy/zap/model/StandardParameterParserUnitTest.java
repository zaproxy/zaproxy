/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

/** Unit test for {@link StandardParameterParser}. */
class StandardParameterParserUnitTest {

    private StandardParameterParser spp;

    @BeforeEach
    void setUp() throws Exception {
        spp = new StandardParameterParser();
    }

    @Test
    void defaultValues() {
        assertThat(spp.getDefaultKeyValuePairSeparator()).isEqualTo("&");
        assertThat(spp.getDefaultKeyValueSeparator()).isEqualTo("=");
    }

    @Test
    void defaultParser() {
        assertThat(spp.getKeyValuePairSeparators()).isEqualTo("&");
        assertThat(spp.getKeyValueSeparators()).isEqualTo("=");
        assertThat(spp.getStructuralParameters()).hasSize(0);

        @SuppressWarnings("deprecation")
        Map<String, String> res = spp.parse("a=b&b=c&d=f");
        assertThat(res).hasSize(3);
        assertThat(res.get("a")).isEqualTo("b");
        assertThat(res.get("b")).isEqualTo("c");
        assertThat(res.get("d")).isEqualTo("f");

        List<NameValuePair> res2 = spp.parseParameters("a=b&b=c&d=f&d=g");
        assertThat(res2).hasSize(4);
        assertThat(res2.get(0).getName()).isEqualTo("a");
        assertThat(res2.get(0).getValue()).isEqualTo("b");
        assertThat(res2.get(1).getName()).isEqualTo("b");
        assertThat(res2.get(1).getValue()).isEqualTo("c");
        assertThat(res2.get(2).getName()).isEqualTo("d");
        assertThat(res2.get(2).getValue()).isEqualTo("f");
        assertThat(res2.get(3).getName()).isEqualTo("d");
        assertThat(res2.get(3).getValue()).isEqualTo("g");
    }

    @Test
    void shouldReturnEmptyListWhenParsingNullString() {
        // Given / When
        List<NameValuePair> parameters = spp.parseParameters(null);
        // Then
        assertThat(parameters).isEmpty();
    }

    @Test
    void shouldReturnEmptyNameValuePairWhenParsingEmptyString() {
        // Given / When
        List<NameValuePair> parameters = spp.parseParameters("");
        // Then
        assertThat(parameters).hasSize(1);
        assertThat(parameters.get(0).getName()).isEmpty();
        assertThat(parameters.get(0).getValue()).isEmpty();
    }

    @Test
    void shouldKeepOriginalNameIfMalformedWhenParsing() {
        // Given / When
        List<NameValuePair> parameters = spp.parseParameters("%x=1&b=2");
        // Then
        assertThat(parameters).hasSize(2);
        assertThat(parameters.get(0).getName()).isEqualTo("%x");
        assertThat(parameters.get(0).getValue()).isEqualTo("1");
        assertThat(parameters.get(1).getName()).isEqualTo("b");
        assertThat(parameters.get(1).getValue()).isEqualTo("2");
    }

    @Test
    void shouldKeepOriginalValueIfMalformedWhenParsing() {
        // Given / When
        List<NameValuePair> parameters = spp.parseParameters("a=%x&b=2");
        // Then
        assertThat(parameters).hasSize(2);
        assertThat(parameters.get(0).getName()).isEqualTo("a");
        assertThat(parameters.get(0).getValue()).isEqualTo("%x");
        assertThat(parameters.get(1).getName()).isEqualTo("b");
        assertThat(parameters.get(1).getValue()).isEqualTo("2");
    }

    @Test
    void shouldParseParametersKeepingEmptyValueWhenAbsent() {
        // Given / When
        List<NameValuePair> parameters = spp.parseParameters("a&b");
        // Then
        assertThat(parameters).hasSize(2);
        assertThat(parameters.get(0).getName()).isEqualTo("a");
        assertThat(parameters.get(0).getValue()).isEmpty();
        assertThat(parameters.get(1).getName()).isEqualTo("b");
        assertThat(parameters.get(1).getValue()).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenRawParsingNullString() {
        // Given / When
        List<NameValuePair> parameters = spp.parseRawParameters(null);
        // Then
        assertThat(parameters).isEmpty();
    }

    @Test
    void shouldReturnEmptyNameAndNullValueWhenRawParsingEmptyString() {
        // Given / When
        List<NameValuePair> parameters = spp.parseRawParameters("");
        // Then
        assertThat(parameters).hasSize(1);
        assertThat(parameters.get(0).getName()).isEmpty();
        assertThat(parameters.get(0).getValue()).isNull();
    }

    @Test
    void shouldNotDecodeNameNorValueWhenRawParsing() {
        // Given / When
        List<NameValuePair> parameters = spp.parseRawParameters("%x=1&b%25=%20");
        // Then
        assertThat(parameters).hasSize(2);
        assertThat(parameters.get(0).getName()).isEqualTo("%x");
        assertThat(parameters.get(0).getValue()).isEqualTo("1");
        assertThat(parameters.get(1).getName()).isEqualTo("b%25");
        assertThat(parameters.get(1).getValue()).isEqualTo("%20");
    }

    @Test
    void shouldHaveEmptyNamesForMissingNamesWhenRawParsing() {
        // Given / When
        List<NameValuePair> parameters = spp.parseRawParameters("=1&=2");
        // Then
        assertThat(parameters).hasSize(2);
        assertThat(parameters.get(0).getName()).isEmpty();
        assertThat(parameters.get(0).getValue()).isEqualTo("1");
        assertThat(parameters.get(1).getName()).isEmpty();
        assertThat(parameters.get(1).getValue()).isEqualTo("2");
    }

    @Test
    void shouldHaveNullValuesForMissingValuesWhenRawParsing() {
        // Given / When
        List<NameValuePair> parameters = spp.parseRawParameters("a&b");
        // Then
        assertThat(parameters).hasSize(2);
        assertThat(parameters.get(0).getName()).isEqualTo("a");
        assertThat(parameters.get(0).getValue()).isNull();
        assertThat(parameters.get(1).getName()).isEqualTo("b");
        assertThat(parameters.get(1).getValue()).isNull();
    }

    @Test
    void nonDefaultParser() {
        spp.setKeyValuePairSeparators(";");
        spp.setKeyValueSeparators(":=");
        List<String> sps = new ArrayList<>();
        sps.add("page");
        spp.setStructuralParameters(sps);
        @SuppressWarnings("deprecation")
        Map<String, String> res = spp.parse("a=b&c;b:c");
        List<NameValuePair> res2 = spp.parseParameters("a=b&c;b:c");

        assertThat(spp.getKeyValuePairSeparators()).isEqualTo(";");
        assertThat(spp.getKeyValueSeparators()).isEqualTo(":=");
        assertThat(spp.getStructuralParameters()).hasSize(1);
        assertThat(spp.getStructuralParameters().get(0)).isEqualTo("page");

        assertThat(res).hasSize(2);
        assertThat(res.get("a")).isEqualTo("b&c");
        assertThat(res.get("b")).isEqualTo("c");

        assertThat(res2).hasSize(2);
        assertThat(res2.get(0).getName()).isEqualTo("a");
        assertThat(res2.get(0).getValue()).isEqualTo("b&c");
        assertThat(res2.get(1).getName()).isEqualTo("b");
        assertThat(res2.get(1).getValue()).isEqualTo("c");
    }

    @Test
    void saveAndLoad() {
        spp.setKeyValuePairSeparators(";");
        spp.setKeyValueSeparators(":=");
        List<String> sps = new ArrayList<>();
        sps.add("page");
        spp.setStructuralParameters(sps);

        StandardParameterParser spp2 = new StandardParameterParser();
        spp2.init(spp.getConfig());

        @SuppressWarnings("deprecation")
        Map<String, String> res = spp2.parse("a=b&c;b:c");
        List<NameValuePair> res2 = spp2.parseParameters("a=b&c;b:c");

        assertThat(spp2.getKeyValuePairSeparators()).isEqualTo(";");
        assertThat(spp2.getKeyValueSeparators()).isEqualTo(":=");
        assertThat(spp2.getStructuralParameters()).hasSize(1);
        assertThat(spp2.getStructuralParameters().get(0)).isEqualTo("page");

        assertThat(res).hasSize(2);
        assertThat(res.get("a")).isEqualTo("b&c");
        assertThat(res.get("b")).isEqualTo("c");
        assertThat(res2).hasSize(2);

        assertThat(res2.get(0).getName()).isEqualTo("a");
        assertThat(res2.get(0).getValue()).isEqualTo("b&c");
        assertThat(res2.get(1).getName()).isEqualTo("b");
        assertThat(res2.get(1).getValue()).isEqualTo("c");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldInitWithNullAndEmptyConfig(String config) {
        // Given
        StandardParameterParser parser = new StandardParameterParser("A", "B");
        parser.setStructuralParameters(Arrays.asList("C", "D"));
        // When
        parser.init(config);
        // Then
        assertThat(parser.getKeyValuePairSeparators()).isEqualTo("&");
        assertThat(parser.getDefaultKeyValuePairSeparator()).isEqualTo("&");
        assertThat(parser.getKeyValueSeparators()).isEqualTo("=");
        assertThat(parser.getDefaultKeyValueSeparator()).isEqualTo("=");
        assertThat(parser.getStructuralParameters()).hasSize(0);
    }

    /**
     * Gets the path of the URI's ancestor found at the given depth, taking into account any context
     * specific configuration (e.g. structural parameters). The depth could also be seen as the
     * number of path elements returned.
     *
     * <p>A few examples (uri, depth):
     *
     * <ul>
     *   <li>(<i>http://example.org/path/to/element</i>, 0) -> ""
     *   <li>(<i>http://example.org/path/to/element</i>, 1) -> "/path"
     *   <li>(<i>http://example.org/path/to/element</i>, 3) -> "/path/to/element"
     *   <li>(<i>http://example.org/path?page=12&data=123</i>, 2) -> "/path?page=12", if {@code
     *       page} is a structural parameter
     *   <li>(<i>http://example.org/path?page=12&data=123&type=1</i>, 3) -> "/path?page=12&type=1",
     *       if {@code page} and {@code type} are both structural parameter
     * </ul>
     *
     * @throws NullPointerException
     * @throws URIException if an error occurred while accessing the provided uri
     */
    @Test
    void ancestorPath() throws Exception {
        // standard urls
        assertThat(spp.getAncestorPath(new URI("http://example.org/path/to/element", true), 0))
                .isEmpty();
        assertThat(spp.getAncestorPath(new URI("http://example.org/path/to/element", true), 1))
                .isEqualTo("/path");
        assertThat(spp.getAncestorPath(new URI("http://example.org/path/to/element", true), 2))
                .isEqualTo("/path/to");
        assertThat(spp.getAncestorPath(new URI("http://example.org/path/to/element", true), 3))
                .isEqualTo("/path/to/element");
        assertThat(
                        spp.getAncestorPath(
                                new URI("http://example.org/path?page=12&data=123", true), 3))
                .isEqualTo("/path");
        assertThat(
                        spp.getAncestorPath(
                                new URI("http://example.org/path?page=12&data=123&type=1", true),
                                3))
                .isEqualTo("/path");

        // With structural params
        List<String> structuralParameters = new ArrayList<>();
        structuralParameters.add("page");
        structuralParameters.add("type");
        spp.setStructuralParameters(structuralParameters);
        assertThat(
                        spp.getAncestorPath(
                                new URI("http://example.org/path?page=12&data=123", true), 3))
                .isEqualTo("/path?page=12");
        assertThat(
                        spp.getAncestorPath(
                                new URI("http://example.org/path?page=12&data=123&type=1", true),
                                3))
                .isEqualTo("/path?page=12&type=1");

        // with data driven nodes
        Context context = new Context(null, 0);
        Pattern p = Pattern.compile("http://example.org/(path/to/)(.+?)(/.*)");
        StructuralNodeModifier ddn =
                new StructuralNodeModifier(StructuralNodeModifier.Type.DataDrivenNode, p, "DDN");
        context.addDataDrivenNodes(ddn);
        spp.setContext(context);
        assertThat(spp.getAncestorPath(new URI("http://example.org/path/to/ddn/aa", true), 3))
                .isEqualTo("/path/to/(.+?)");
        assertThat(spp.getAncestorPath(new URI("http://example.org/path/to/ddn/aa", true), 4))
                .isEqualTo("/path/to/(.+?)/aa");
    }
}

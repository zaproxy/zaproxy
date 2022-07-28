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
package org.zaproxy.zap.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.NameValuePair;
import org.parosproxy.paros.core.scanner.Variant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.ascan.VariantFactory;

class SessionStructureUnitTest {

    private Model model;
    private Session session;
    private HttpMessage msg;
    private VariantFactory factory;

    @BeforeEach
    void setUp() throws Exception {
        WithConfigsTest.setUpConstantMessages();
        factory = new VariantFactory();
        model = mock(Model.class);
        session = new Session(model);
        given(model.getSession()).willReturn(session);
        given(model.getVariantFactory()).willReturn(factory);
        Control.initSingletonForTesting(model);
        msg = new HttpMessage();
    }

    @AfterEach
    void cleanUp() {
        Constant.messages = null;
    }

    @ParameterizedTest
    @CsvSource({
        "example.com,http://example.com",
        "example.com:80,http://example.com",
        "example.com:443,https://example.com",
        "example.com:8080,http://example.com:8080"
    })
    void shouldReturnHostNameFromAuthority(String authority, String expectedHostName)
            throws Exception {
        // Given
        URI uri = URI.fromAuthority(authority);
        // When
        String hostName = SessionStructure.getHostName(uri);
        // Then
        assertThat(hostName, is(equalTo(expectedHostName)));
    }

    static Stream<String> methodProvider() {
        return Stream.of(HttpRequestHeader.METHODS);
    }

    @ParameterizedTest
    @MethodSource("methodProvider")
    void shouldReturnCorrectNameForNoPathNoSlashNoParams(String method) throws Exception {
        // Given
        msg.getRequestHeader().setMethod(method);
        String uri = "https://www.example.com";
        msg.getRequestHeader().setURI(new URI(uri, true));
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri)));
    }

    @ParameterizedTest
    @MethodSource("methodProvider")
    void shouldReturnCorrectNameForNoPathWithSlashNoParams(String method) throws Exception {
        // Given
        msg.getRequestHeader().setMethod(method);
        String uri = "https://www.example.com/";
        msg.getRequestHeader().setURI(new URI(uri, true));
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri)));
    }

    @ParameterizedTest
    @MethodSource("methodProvider")
    void shouldReturnCorrectNameForNoPathNoSlashWithParams(String method) throws Exception {
        // Given
        msg.getRequestHeader().setMethod(method);
        String uri = "https://www.example.com";
        msg.getRequestHeader().setURI(new URI(uri + "?a=b&c=d", true));
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)")));
    }

    @ParameterizedTest
    @MethodSource("methodProvider")
    void shouldReturnCorrectNameForNoPathWithSlashWithParams(String method) throws Exception {
        // Given
        msg.getRequestHeader().setMethod(method);
        String uri = "https://www.example.com/";
        msg.getRequestHeader().setURI(new URI(uri + "?a=b&c=d", true));
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)")));
    }

    @ParameterizedTest
    @MethodSource("methodProvider")
    void shouldReturnCorrectNameForWithPathNoSlashNoParams(String method) throws Exception {
        // Given
        msg.getRequestHeader().setMethod(method);
        String uri = "https://www.example.com/path";
        msg.getRequestHeader().setURI(new URI(uri, true));
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri)));
    }

    @ParameterizedTest
    @MethodSource("methodProvider")
    void shouldReturnCorrectNameForWithPathWithSlashNoParams(String method) throws Exception {
        // Given
        msg.getRequestHeader().setMethod(method);
        String uri = "https://www.example.com/path/";
        msg.getRequestHeader().setURI(new URI(uri, true));
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri)));
    }

    @ParameterizedTest
    @MethodSource("methodProvider")
    void shouldReturnCorrectNameForWithPathNoSlashWithParams(String method) throws Exception {
        // Given
        msg.getRequestHeader().setMethod(method);
        String uri = "https://www.example.com/path";
        msg.getRequestHeader().setURI(new URI(uri + "?a=b&c=d", true));
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)")));
    }

    @ParameterizedTest
    @MethodSource("methodProvider")
    void shouldReturnCorrectNameForWithPathWithSlashWithParams(String method) throws Exception {
        // Given
        msg.getRequestHeader().setMethod(method);
        String uri = "https://www.example.com/path/";
        msg.getRequestHeader().setURI(new URI(uri + "?a=b&c=d", true));
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)")));
    }

    @Test
    void shouldReturnCorrectNameForPostNoPathNoSlashNoParams() throws Exception {
        // Given
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        String uri = "https://www.example.com";
        msg.getRequestHeader().setURI(new URI(uri, true));
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri)));
    }

    @Test
    void shouldReturnCorrectNameForPostNoPathNoSlashNoUrlParams() throws Exception {
        // Given
        String uri = "https://www.example.com";
        createPostMsgWithFormParams(uri, null, "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " ()(e,g)")));
    }

    @Test
    void shouldReturnCorrectNameForPostNoPathWithSlashNoUrlParams() throws Exception {
        // Given
        String uri = "https://www.example.com/";
        createPostMsgWithFormParams(uri, null, "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " ()(e,g)")));
    }

    @Test
    void shouldReturnCorrectNameForPostNoPathNoSlashWithParams() throws Exception {
        // Given
        String uri = "https://www.example.com";
        createPostMsgWithFormParams(uri, "a=b&c=d", "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)(e,g)")));
    }

    @Test
    void shouldReturnCorrectNameForPostNoPathWithSlashWithParams() throws Exception {
        // Given
        String uri = "https://www.example.com/";
        createPostMsgWithFormParams(uri, "a=b&c=d", "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)(e,g)")));
    }

    @Test
    void shouldReturnCorrectNameForPostWithPathNoSlashNoUrlParams() throws Exception {
        // Given
        String uri = "https://www.example.com/path";
        createPostMsgWithFormParams(uri, null, "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " ()(e,g)")));
    }

    @Test
    void shouldReturnCorrectNameForPostWithPathWithSlashNoUrlParams() throws Exception {
        // Given
        String uri = "https://www.example.com/path/";
        createPostMsgWithFormParams(uri, null, "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " ()(e,g)")));
    }

    @Test
    void shouldReturnCorrectNameForPostWithPathNoSlashWithParams() throws Exception {
        // Given
        String uri = "https://www.example.com/path";
        createPostMsgWithFormParams(uri, "a=b&c=d", "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)(e,g)")));
    }

    @Test
    void shouldReturnCorrectNameForPostWithPathWithSlashWithParams() throws Exception {
        // Given
        String uri = "https://www.example.com/path/";
        createPostMsgWithFormParams(uri, "a=b&c=d", "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)(e,g)")));
    }

    @Test
    void shouldReturnCorrectNameForPostWithPathWithSlashWithSameUrlAndPostParams()
            throws Exception {
        // Given
        String uri = "https://www.example.com/path/";
        createPostMsgWithFormParams(uri, "a=b&c=d", "a=b&c=d");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)(a,c)")));
    }

    @Nested
    static class RegexGenerationTests {

        private StructuralSiteNode sitesNode;
        private StructuralSiteNode hostNode;

        @BeforeEach
        void setup() throws Exception {
            sitesNode = mock(StructuralSiteNode.class);
            given(sitesNode.isRoot()).willReturn(true);
            hostNode = mock(StructuralSiteNode.class);
            given(hostNode.getParent()).willReturn(sitesNode);
            given(hostNode.isLeaf()).willReturn(true);
            URI uri = new URI("https://www.example.com", true);
            given(hostNode.getURI()).willReturn(uri);
            given(hostNode.getName()).willReturn("https://www.example.com");
        }

        @Test
        void shouldReturnCorrectRegexForNoPathNoSlashNoParams() throws Exception {
            // Given / When
            String nodeRegex = SessionStructure.getRegexPattern(hostNode);
            // Then
            assertThat(nodeRegex, is(equalTo("https://www.example.com.*")));
        }

        @Test
        void shouldReturnCorrectRegexForNoPathWithSlashNoParams() throws Exception {
            // Given
            StructuralNode leafNode = getLeafNode("https://www.example.com/");
            // When
            String nodeRegex = SessionStructure.getRegexPattern(leafNode);
            // Then
            assertThat(nodeRegex, is(equalTo("https://www.example.com/.*")));
        }

        @Test
        void shouldReturnCorrectRegexForNoPathNoSlashWithParams() throws Exception {
            // Given
            URI uri = new URI("https://www.example.com?a=b&c=d", true);
            given(hostNode.getURI()).willReturn(uri);
            given(hostNode.getName()).willReturn("https://www.example.com?a=b&c=d");
            // When
            String nodeRegex = SessionStructure.getRegexPattern(hostNode);
            // Then
            assertThat(nodeRegex, is(equalTo("https://www.example.com\\?a\\=b&c\\=d.*")));
        }

        @Test
        void shouldReturnCorrectRegexForNoPathWithSlashWithParams() throws Exception {
            // Given
            StructuralSiteNode leafNode = getLeafNode("https://www.example.com/?a=b&c=d");
            // When
            String nodeRegex = SessionStructure.getRegexPattern(leafNode);
            // Then
            assertThat(nodeRegex, is(equalTo("https://www.example.com/\\?a\\=b&c\\=d.*")));
        }

        @Test
        void shouldReturnCorrectRegexForWithPathNoSlashNoParams() throws Exception {
            // Given
            StructuralSiteNode leafNode = getLeafNode("https://www.example.com/path");
            // When
            String nodeRegex = SessionStructure.getRegexPattern(leafNode);
            // Then
            assertThat(nodeRegex, is(equalTo("https://www.example.com/path.*")));
        }

        @Test
        void shouldReturnCorrectRegexForWithPathWithSlashNoParams() throws Exception {
            // Given
            StructuralSiteNode leafNode = getLeafNode("https://www.example.com/path");
            StructuralSiteNode leafLeafNode = getLeafNode("https://www.example.com/path/");
            given(leafLeafNode.getParent()).willReturn(leafNode);
            // When
            String nodeRegex = SessionStructure.getRegexPattern(leafLeafNode);
            // Then
            assertThat(nodeRegex, is(equalTo("https://www.example.com/path/.*")));
        }

        @Test
        void shouldReturnCorrectRegexForWithPathWithSlashWithParams() throws Exception {
            // Given
            StructuralSiteNode leafNode = getLeafNode("https://www.example.com/path");
            StructuralSiteNode leafLeafNode = getLeafNode("https://www.example.com/path/?a=b&c=d");
            given(leafLeafNode.getParent()).willReturn(leafNode);
            // When
            String nodeRegex = SessionStructure.getRegexPattern(leafLeafNode);
            // Then
            assertThat(nodeRegex, is(equalTo("https://www.example.com/path/\\?a\\=b&c\\=d.*")));
        }

        @Test
        void shouldReturnCorrectRegexForWithPathNoSlashWithParams() throws Exception {
            // Given
            StructuralSiteNode leafNode = getLeafNode("https://www.example.com/path?a=b&c=d");
            // When
            String nodeRegex = SessionStructure.getRegexPattern(leafNode);
            // Then
            assertThat(nodeRegex, is(equalTo("https://www.example.com/path\\?a\\=b&c\\=d.*")));
        }

        private StructuralSiteNode getLeafNode(String url) throws Exception {
            StructuralSiteNode leafNode = mock(StructuralSiteNode.class);
            URI leafUri = new URI(url, true);
            given(leafNode.getParent()).willReturn(hostNode);
            given(leafNode.isLeaf()).willReturn(true);
            given(leafNode.getURI()).willReturn(leafUri);
            given(leafNode.getName()).willReturn(url);
            return leafNode;
        }
    }

    @Test
    void shouldReturnShortPathTree() throws Exception {
        // Given
        URI uri = new URI("https://www.example.com/path", true);
        HttpMessage msg = new HttpMessage(uri);
        // When
        List<String> pathTree = SessionStructure.getTreePath(model, msg);
        // Then
        assertThat(pathTree.size(), is(equalTo(1)));
        assertThat(pathTree.get(0), is(equalTo("path")));
    }

    @Test
    void shouldReturnLongPathTree() throws Exception {
        // Given
        URI uri = new URI("https://www.example.com/path/a/b/c/d/e/f", true);
        HttpMessage msg = new HttpMessage(uri);
        // When
        List<String> pathTree = SessionStructure.getTreePath(model, msg);
        // Then
        assertThat(pathTree.size(), is(equalTo(7)));
        assertThat(pathTree.get(0), is(equalTo("path")));
        assertThat(pathTree.get(1), is(equalTo("a")));
        assertThat(pathTree.get(2), is(equalTo("b")));
        assertThat(pathTree.get(3), is(equalTo("c")));
        assertThat(pathTree.get(4), is(equalTo("d")));
        assertThat(pathTree.get(5), is(equalTo("e")));
        assertThat(pathTree.get(6), is(equalTo("f")));
    }

    @Test
    void shouldReturnOverridenPathTree() throws Exception {
        // Given
        URI uri = new URI("https://www.example.com/path?a=b", true);
        HttpMessage msg = new HttpMessage(uri);
        Variant variant = new PathTreeVariant();
        factory.addVariant(variant.getClass());
        List<String> expectedTreePath = variant.getTreePath(msg);

        // When
        List<String> actualTreePath = SessionStructure.getTreePath(model, msg);

        // Then
        assertThat(actualTreePath.size(), is(equalTo(expectedTreePath.size())));
        for (int i = 0; i < actualTreePath.size(); i++) {
            assertThat(actualTreePath.get(i), is(equalTo(expectedTreePath.get(i))));
        }
    }

    private void createPostMsgWithFormParams(String uri, String queryParams, String formParams)
            throws URIException {
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        queryParams = queryParams == null ? "" : "?" + queryParams;
        msg.getRequestHeader().setURI(new URI(uri + queryParams, true));
        msg.getRequestHeader()
                .setHeader(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        msg.setRequestBody(formParams);
    }

    public static final class PathTreeVariant implements Variant {
        private final List<String> expectedTreePath;

        public PathTreeVariant() {
            expectedTreePath = new ArrayList<>();
            expectedTreePath.add("Path1");
            expectedTreePath.add("Path2");
        }

        @Override
        public List<String> getTreePath(HttpMessage msg) {
            return expectedTreePath;
        }

        @Override
        public void setMessage(HttpMessage msg) {}

        @Override
        public List<NameValuePair> getParamList() {
            return null;
        }

        @Override
        public String setParameter(
                HttpMessage msg, NameValuePair originalPair, String param, String value) {
            return null;
        }

        @Override
        public String setEscapedParameter(
                HttpMessage msg, NameValuePair originalPair, String param, String value) {
            return null;
        }
    }
}

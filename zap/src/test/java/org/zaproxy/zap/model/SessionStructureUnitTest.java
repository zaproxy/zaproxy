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

import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(hostName).isEqualTo(expectedHostName);
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
        assertThat(nodeName).isEqualTo(uri);
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
        assertThat(nodeName).isEqualTo(uri);
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
        assertThat(nodeName).isEqualTo(uri + " (a,c)");
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
        assertThat(nodeName).isEqualTo(uri + " (a,c)");
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
        assertThat(nodeName).isEqualTo(uri);
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
        assertThat(nodeName).isEqualTo(uri);
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
        assertThat(nodeName).isEqualTo(uri + " (a,c)");
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
        assertThat(nodeName).isEqualTo(uri + " (a,c)");
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
        assertThat(nodeName).isEqualTo(uri);
    }

    @Test
    void shouldReturnCorrectNameForPostNoPathNoSlashNoUrlParams() throws Exception {
        // Given
        String uri = "https://www.example.com";
        createPostMsgWithFormParams(uri, null, "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName).isEqualTo(uri + " ()(e,g)");
    }

    @Test
    void shouldReturnCorrectNameForPostNoPathWithSlashNoUrlParams() throws Exception {
        // Given
        String uri = "https://www.example.com/";
        createPostMsgWithFormParams(uri, null, "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName).isEqualTo(uri + " ()(e,g)");
    }

    @Test
    void shouldReturnCorrectNameForPostNoPathNoSlashWithParams() throws Exception {
        // Given
        String uri = "https://www.example.com";
        createPostMsgWithFormParams(uri, "a=b&c=d", "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName).isEqualTo(uri + " (a,c)(e,g)");
    }

    @Test
    void shouldReturnCorrectNameForPostNoPathWithSlashWithParams() throws Exception {
        // Given
        String uri = "https://www.example.com/";
        createPostMsgWithFormParams(uri, "a=b&c=d", "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName).isEqualTo(uri + " (a,c)(e,g)");
    }

    @Test
    void shouldReturnCorrectNameForPostWithPathNoSlashNoUrlParams() throws Exception {
        // Given
        String uri = "https://www.example.com/path";
        createPostMsgWithFormParams(uri, null, "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName).isEqualTo(uri + " ()(e,g)");
    }

    @Test
    void shouldReturnCorrectNameForPostWithPathWithSlashNoUrlParams() throws Exception {
        // Given
        String uri = "https://www.example.com/path/";
        createPostMsgWithFormParams(uri, null, "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName).isEqualTo(uri + " ()(e,g)");
    }

    @Test
    void shouldReturnCorrectNameForPostWithPathNoSlashWithParams() throws Exception {
        // Given
        String uri = "https://www.example.com/path";
        createPostMsgWithFormParams(uri, "a=b&c=d", "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName).isEqualTo(uri + " (a,c)(e,g)");
    }

    @Test
    void shouldReturnCorrectNameForPostWithPathWithSlashWithParams() throws Exception {
        // Given
        String uri = "https://www.example.com/path/";
        createPostMsgWithFormParams(uri, "a=b&c=d", "e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName).isEqualTo(uri + " (a,c)(e,g)");
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
        assertThat(nodeName).isEqualTo(uri + " (a,c)(a,c)");
    }

    @Test
    void shouldReturnCorrectNameForJsonPost() throws Exception {
        // Given
        String uri = "https://www.example.com/path/";
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        msg.getRequestHeader().setURI(new URI(uri + "?aa=bb&cc=dd&ee=ff", true));
        msg.getRequestHeader().setHeader(HttpHeader.CONTENT_TYPE, "application/json");
        msg.setRequestBody("{\"aaa\":\"bbb\", \"ccc\":\"ddd\", \"eee\":\"fff\"}");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName).isEqualTo(uri + " (aa,cc,ee)({aaa,ccc,eee})");
    }

    @Test
    void shouldReturnCorrectNameForXmlPost() throws Exception {
        // Given
        String uri = "https://www.example.com/path/";
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        msg.getRequestHeader().setURI(new URI(uri + "?aa=bb&cc=dd&ee=ff", true));
        msg.getRequestHeader().setHeader(HttpHeader.CONTENT_TYPE, "text/xml");
        msg.setRequestBody("<aaa><bbb>BBB</bbb><ccc>CCC</ccc><ddd>DDD</ddd></aaa>");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName).isEqualTo(uri + " (aa,cc,ee)(<aaa:<bbb>,<ccc>,<ddd>>)");
    }

    @Test
    void shouldReturnCorrectNameForMultipartPost() throws Exception {
        // Given
        String uri = "https://www.example.com/path/";
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        msg.getRequestHeader().setURI(new URI(uri + "?aa=bb&cc=dd&ee=ff", true));
        String boundry = "----zaptestboundry6345896464398764398";
        msg.getRequestHeader()
                .setHeader(HttpHeader.CONTENT_TYPE, "multipart/form-data; boundary=" + boundry);
        msg.setRequestBody(getTestMultipartData(boundry));
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName).isEqualTo(uri + " (aa,cc,ee)(multipart:username,file1,file2,file3)");
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
            assertThat(nodeRegex).isEqualTo("https://www.example.com.*");
        }

        @Test
        void shouldReturnCorrectRegexForNoPathWithSlashNoParams() throws Exception {
            // Given
            StructuralNode leafNode = getLeafNode("https://www.example.com/");
            // When
            String nodeRegex = SessionStructure.getRegexPattern(leafNode);
            // Then
            assertThat(nodeRegex).isEqualTo("https://www.example.com/.*");
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
            assertThat(nodeRegex).isEqualTo("https://www.example.com\\?a\\=b&c\\=d.*");
        }

        @Test
        void shouldReturnCorrectRegexForNoPathWithSlashWithParams() throws Exception {
            // Given
            StructuralSiteNode leafNode = getLeafNode("https://www.example.com/?a=b&c=d");
            // When
            String nodeRegex = SessionStructure.getRegexPattern(leafNode);
            // Then
            assertThat(nodeRegex).isEqualTo("https://www.example.com/\\?a\\=b&c\\=d.*");
        }

        @Test
        void shouldReturnCorrectRegexForWithPathNoSlashNoParams() throws Exception {
            // Given
            StructuralSiteNode leafNode = getLeafNode("https://www.example.com/path");
            // When
            String nodeRegex = SessionStructure.getRegexPattern(leafNode);
            // Then
            assertThat(nodeRegex).isEqualTo("https://www.example.com/path.*");
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
            assertThat(nodeRegex).isEqualTo("https://www.example.com/path/.*");
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
            assertThat(nodeRegex).isEqualTo("https://www.example.com/path/\\?a\\=b&c\\=d.*");
        }

        @Test
        void shouldReturnCorrectRegexForWithPathNoSlashWithParams() throws Exception {
            // Given
            StructuralSiteNode leafNode = getLeafNode("https://www.example.com/path?a=b&c=d");
            // When
            String nodeRegex = SessionStructure.getRegexPattern(leafNode);
            // Then
            assertThat(nodeRegex).isEqualTo("https://www.example.com/path\\?a\\=b&c\\=d.*");
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
        assertThat(pathTree).hasSize(1);
        assertThat(pathTree.get(0)).isEqualTo("path");
    }

    @Test
    void shouldReturnLongPathTree() throws Exception {
        // Given
        URI uri = new URI("https://www.example.com/path/a/b/c/d/e/f", true);
        HttpMessage msg = new HttpMessage(uri);
        // When
        List<String> pathTree = SessionStructure.getTreePath(model, msg);
        // Then
        assertThat(pathTree).hasSize(7);
        assertThat(pathTree.get(0)).isEqualTo("path");
        assertThat(pathTree.get(1)).isEqualTo("a");
        assertThat(pathTree.get(2)).isEqualTo("b");
        assertThat(pathTree.get(3)).isEqualTo("c");
        assertThat(pathTree.get(4)).isEqualTo("d");
        assertThat(pathTree.get(5)).isEqualTo("e");
        assertThat(pathTree.get(6)).isEqualTo("f");
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
        assertThat(actualTreePath).hasSize(expectedTreePath.size());
        for (int i = 0; i < actualTreePath.size(); i++) {
            assertThat(actualTreePath.get(i)).isEqualTo(expectedTreePath.get(i));
        }
    }

    @Nested
    static class NodeNameTests {

        private Model model;
        private Session session;
        private VariantFactory factory;

        HttpMessage getParams;
        HttpMessage getNoParams;
        HttpMessage postParamsFormData;
        HttpMessage postNoParamsFormData;
        HttpMessage postParamsJsonData;
        HttpMessage postParamsXmlData;
        HttpMessage postMultipartData;

        @BeforeEach
        void setup() throws Exception {
            WithConfigsTest.setUpConstantMessages();
            model = mock(Model.class);
            session = new Session(model);
            factory = new VariantFactory();
            given(model.getSession()).willReturn(session);
            given(model.getVariantFactory()).willReturn(factory);
            getParams =
                    new HttpMessage(new URI("https://www.example.com/aaa/bbb?aa=bb&cc=dd", false));
            getNoParams = new HttpMessage(new URI("https://www.example.com/aaa/bbb", false));
            postParamsFormData =
                    getPostMsgWithFormParams(
                            "https://www.example.com/ccc", "cc=dd&aa=bb", "gg=hh&ee=ff");
            postNoParamsFormData =
                    getPostMsgWithFormParams("https://www.example.com/ccc", "", "ee=ff&gg=ee");
            postParamsJsonData =
                    getPostMsg(
                            "https://www.example.com/ccc",
                            "aa=bb&cc=dd",
                            "{\"aaa\":\"bbb\", \"ccc\":\"ddd\", \"eee\":\"fff\"}",
                            "application/json");
            postParamsXmlData =
                    getPostMsg(
                            "https://www.example.com/ccc",
                            "aa=bb&cc=dd",
                            "<aaa><bbb>BBB</bbb><ccc>CCC</ccc><ddd>DDD</ddd></aaa>",
                            "text/xml");

            String boundry = "----zaptestboundry6345896464398764398";

            postMultipartData =
                    getPostMsg(
                            "https://www.example.com/ddd/",
                            "aa=bb&cc=dd",
                            getTestMultipartData(boundry),
                            "multipart/form-data; boundary=" + boundry);

            Control.initSingletonForTesting(model);
        }

        @AfterEach
        void cleanUp() {
            Constant.messages = null;
        }

        @Test
        void shouldGetNodeName() throws URIException {
            assertThat(SessionStructure.getNodeName(model, getParams))
                    .isEqualTo("https://www.example.com/aaa/bbb (aa,cc)");
            assertThat(SessionStructure.getNodeName(model, getNoParams))
                    .isEqualTo("https://www.example.com/aaa/bbb");
            assertThat(SessionStructure.getNodeName(model, postParamsFormData))
                    .isEqualTo("https://www.example.com/ccc (aa,cc)(ee,gg)");
            assertThat(SessionStructure.getNodeName(model, postNoParamsFormData))
                    .isEqualTo("https://www.example.com/ccc ()(ee,gg)");
            assertThat(SessionStructure.getNodeName(model, postParamsJsonData))
                    .isEqualTo("https://www.example.com/ccc (aa,cc)({aaa,ccc,eee})");
            assertThat(SessionStructure.getNodeName(model, postParamsXmlData))
                    .isEqualTo("https://www.example.com/ccc (aa,cc)(<aaa:<bbb>,<ccc>,<ddd>>)");
            assertThat(SessionStructure.getNodeName(model, postMultipartData))
                    .isEqualTo(
                            "https://www.example.com/ddd/ (aa,cc)(multipart:username,file1,file2,file3)");
        }

        @Test
        void shouldGetLeafName1() throws URIException {
            assertThat(SessionStructure.getLeafName(model, "test", getParams))
                    .isEqualTo("GET:test(aa,cc)");
            assertThat(SessionStructure.getLeafName(model, "test", getNoParams))
                    .isEqualTo("GET:test");
            assertThat(SessionStructure.getLeafName(model, "test", postParamsFormData))
                    .isEqualTo("POST:test(aa,cc)(ee,gg)");
            assertThat(SessionStructure.getLeafName(model, "test", postNoParamsFormData))
                    .isEqualTo("POST:test()(ee,gg)");
            assertThat(SessionStructure.getLeafName(model, "test", postParamsJsonData))
                    .isEqualTo("POST:test(aa,cc)({aaa,ccc,eee})");
            assertThat(SessionStructure.getLeafName(model, "test", postParamsXmlData))
                    .isEqualTo("POST:test(aa,cc)(<aaa:<bbb>,<ccc>,<ddd>>)");
            assertThat(SessionStructure.getLeafName(model, "test", postMultipartData))
                    .isEqualTo("POST:test(aa,cc)(multipart:username,file1,file2,file3)");
        }

        @Test
        void shouldGetLeafName2() throws Exception {
            assertThat(getLeafName2(getParams)).isEqualTo("GET:test(aa,cc)");
            assertThat(getLeafName2(getNoParams)).isEqualTo("GET:test");
            assertThat(getLeafName2(postParamsFormData)).isEqualTo("POST:test(aa,cc)(ee,gg)");
            assertThat(getLeafName2(postNoParamsFormData)).isEqualTo("POST:test()(ee,gg)");
            assertThat(getLeafName2(postParamsJsonData))
                    .isEqualTo("POST:test(aa,cc)({aaa,ccc,eee})");
            assertThat(getLeafName2(postParamsXmlData))
                    .isEqualTo("POST:test(aa,cc)(<aaa:<bbb>,<ccc>,<ddd>>)");
            // FIXME: Should not get the duplicated fileX fields
            assertThat(getLeafName2(postMultipartData))
                    .isEqualTo(
                            "POST:test(aa,cc)(multipart:username,file1,file1,file1,file2,file2,file2,file3,file3,file3)");
        }

        @Test
        void shouldHandleInvalidXmlName() throws URIException {
            // Given / When
            HttpMessage msg =
                    getPostMsg("https://www.example.com/ccc", "aa=bb&cc=dd", "<a", "text/xml");
            // Then
            assertThat(SessionStructure.getNodeName(model, msg))
                    .isEqualTo("https://www.example.com/ccc (aa,cc)(<a)");
        }

        @Test
        void shouldHandleInvalidJsonName() throws URIException {
            // Given / When
            HttpMessage msg =
                    getPostMsg(
                            "https://www.example.com/ccc", "aa=bb&cc=dd", "{a", "application/json");
            // Then
            assertThat(SessionStructure.getNodeName(model, msg))
                    .isEqualTo("https://www.example.com/ccc (aa,cc)({a)");
        }

        String getLeafName2(HttpMessage msg) throws Exception {
            return SessionStructure.getLeafName(
                    model,
                    "test",
                    msg.getRequestHeader().getURI(),
                    msg.getRequestHeader().getMethod(),
                    msg.getRequestBody().toString());
        }
    }

    private void createPostMsgWithFormParams(String uri, String queryParams, String formParams)
            throws URIException {
        msg = getPostMsgWithFormParams(uri, queryParams, formParams);
    }

    private static HttpMessage getPostMsgWithFormParams(
            String uri, String queryParams, String formParams) throws URIException {
        return getPostMsg(uri, queryParams, formParams, "application/x-www-form-urlencoded");
    }

    private static HttpMessage getPostMsg(
            String uri, String queryParams, String formParams, String contentType)
            throws URIException {
        HttpMessage message = new HttpMessage();
        message.getRequestHeader().setMethod(HttpRequestHeader.POST);
        queryParams = queryParams == null ? "" : "?" + queryParams;
        message.getRequestHeader().setURI(new URI(uri + queryParams, true));
        message.getRequestHeader().setHeader(HttpHeader.CONTENT_TYPE, contentType);
        message.setRequestBody(formParams);
        return message;
    }

    private static String getTestMultipartData(String boundary) {
        return String.join(
                HttpHeader.CRLF,
                "--" + boundary,
                "Content-Disposition: form-data; name=\"username\"",
                "",
                "john_doe",
                "",
                "--" + boundary,
                "Content-Disposition: form-data; name=\"file1\"; filename=\"a.txt\"",
                "Content-Type: text/plan",
                "",
                "Text file content.",
                "",
                "--" + boundary,
                "Content-Disposition: form-data; name=\"file2\"; filename=\"a.html\"",
                "Content-Type: text/plan",
                "",
                "<!DOCTYPE html><title>HTML file content.</title>",
                "",
                "--" + boundary,
                "Content-Disposition: form-data; name=\"file3\"; filename=\"a.json\"",
                "Content-Type: application/json",
                "",
                "{\"age\":30,\"location\":\"New York\"}",
                "--" + boundary + "--");
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

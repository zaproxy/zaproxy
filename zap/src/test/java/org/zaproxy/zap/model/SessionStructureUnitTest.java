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

import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Variant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SessionUnitTest.PathTreeVariant;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.ascan.VariantFactory;

public class SessionStructureUnitTest {

    private Model model;
    private Session session;
    private HttpMessage msg;
    private VariantFactory factory;

    @BeforeEach
    public void setUp() throws Exception {
        WithConfigsTest.setUpConstant();
        factory = new VariantFactory();
        model = mock(Model.class);
        session = new Session(model);
        given(model.getSession()).willReturn(session);
        given(model.getVariantFactory()).willReturn(factory);
        Control.initSingletonForTesting(model);
        msg = new HttpMessage();
    }

    static Stream<String> methodProvider() {
        return Stream.of(HttpRequestHeader.METHODS);
    }

    @ParameterizedTest
    @MethodSource("methodProvider")
    public void shouldReturnCorrectNameForNoPathNoSlashNoParams(String method) throws Exception {
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
    public void shouldReturnCorrectNameForNoPathWithSlashNoParams(String method) throws Exception {
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
    public void shouldReturnCorrectNameForNoPathNoSlashWithParams(String method) throws Exception {
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
    public void shouldReturnCorrectNameForNoPathWithSlashWithParams(String method)
            throws Exception {
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
    public void shouldReturnCorrectNameForWithPathNoSlashNoParams(String method) throws Exception {
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
    public void shouldReturnCorrectNameForWithPathWithSlashNoParams(String method)
            throws Exception {
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
    public void shouldReturnCorrectNameForWithPathNoSlashWithParams(String method)
            throws Exception {
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
    public void shouldReturnCorrectNameForWithPathWithSlashWithParams(String method)
            throws Exception {
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
    public void shouldReturnCorrectNameForPostNoPathNoSlashNoParams() throws Exception {
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
    public void shouldReturnCorrectNameForPostNoPathNoSlashNoUrlParams() throws Exception {
        // Given
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        String uri = "https://www.example.com";
        msg.getRequestHeader().setURI(new URI(uri, true));
        msg.setRequestBody("e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " ()(e,g)")));
    }

    @Test
    public void shouldReturnCorrectNameForPostNoPathWithSlashNoUrlParams() throws Exception {
        // Given
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        String uri = "https://www.example.com/";
        msg.getRequestHeader().setURI(new URI(uri, true));
        msg.setRequestBody("e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " ()(e,g)")));
    }

    @Test
    public void shouldReturnCorrectNameForPostNoPathNoSlashWithParams() throws Exception {
        // Given
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        String uri = "https://www.example.com";
        msg.getRequestHeader().setURI(new URI(uri + "?a=b&c=d", true));
        msg.setRequestBody("e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)(e,g)")));
    }

    @Test
    public void shouldReturnCorrectNameForPostNoPathWithSlashWithParams() throws Exception {
        // Given
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        String uri = "https://www.example.com/";
        msg.getRequestHeader().setURI(new URI(uri + "?a=b&c=d", true));
        msg.setRequestBody("e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)(e,g)")));
    }

    @Test
    public void shouldReturnCorrectNameForPostWithPathNoSlashNoUrlParams() throws Exception {
        // Given
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        String uri = "https://www.example.com/path";
        msg.getRequestHeader().setURI(new URI(uri, true));
        msg.setRequestBody("e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " ()(e,g)")));
    }

    @Test
    public void shouldReturnCorrectNameForPostWithPathWithSlashNoUrlParams() throws Exception {
        // Given
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        String uri = "https://www.example.com/path/";
        msg.getRequestHeader().setURI(new URI(uri, true));
        msg.setRequestBody("e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " ()(e,g)")));
    }

    @Test
    public void shouldReturnCorrectNameForPostWithPathNoSlashWithParams() throws Exception {
        // Given
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        String uri = "https://www.example.com/path";
        msg.getRequestHeader().setURI(new URI(uri + "?a=b&c=d", true));
        msg.setRequestBody("e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)(e,g)")));
    }

    @Test
    public void shouldReturnCorrectNameForPostWithPathWithSlashWithParams() throws Exception {
        // Given
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        String uri = "https://www.example.com/path/";
        msg.getRequestHeader().setURI(new URI(uri + "?a=b&c=d", true));
        msg.setRequestBody("e=f&g=h");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)(e,g)")));
    }

    @Test
    public void shouldReturnCorrectNameForPostWithPathWithSlashWithSameUrlAndPostParams()
            throws Exception {
        // Given
        msg.getRequestHeader().setMethod(HttpRequestHeader.POST);
        String uri = "https://www.example.com/path/";
        msg.getRequestHeader().setURI(new URI(uri + "?a=b&c=d", true));
        msg.setRequestBody("a=b&c=d");
        // When
        String nodeName = SessionStructure.getNodeName(model, msg);
        // Then
        assertThat(nodeName, is(equalTo(uri + " (a,c)(a,c)")));
    }

    @Test
    public void shouldReturnShortPathTree() throws Exception {
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
    public void shouldReturnLongPathTree() throws Exception {
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
    public void shouldReturnOverridenPathTree() throws Exception {
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
}

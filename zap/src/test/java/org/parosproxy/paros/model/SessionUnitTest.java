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
package org.parosproxy.paros.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.NameValuePair;
import org.parosproxy.paros.core.scanner.Variant;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.ascan.VariantFactory;
import org.zaproxy.zap.utils.I18N;

public class SessionUnitTest {

    private Session session;
    private VariantFactory factory;

    @BeforeEach
    public void setUp() throws Exception {
        Constant.getInstance();
        I18N i18n = Mockito.mock(I18N.class, withSettings().lenient());
        given(i18n.getString(anyString())).willReturn("");
        given(i18n.getString(anyString(), any())).willReturn("");
        given(i18n.getLocal()).willReturn(Locale.getDefault());
        Constant.messages = i18n;
        factory = new VariantFactory();
        Model model = mock(Model.class);
        given(model.getVariantFactory()).willReturn(factory);
        Control.initSingletonForTesting(model);

        session = new Session(model);
        given(model.getSession()).willReturn(session);
    }

    @Test
    public void shouldReturnGetLeafNameWithOneParam() throws Exception {
        // Given
        String nodeName = "path";
        URI uri = new URI("https://www.example.com/path?a=b", true);
        // When
        String leafName = session.getLeafName(nodeName, uri, "GET", null);
        // Then
        assertThat(leafName, is(equalTo("GET:path(a)")));
    }

    @Test
    public void shouldReturnGetLeafNameWithTwoParams() throws Exception {
        // Given
        String nodeName = "path";
        URI uri = new URI("https://www.example.com/path?c=d&a=b", true);
        // When
        String leafName = session.getLeafName(nodeName, uri, "GET", null);
        // Then
        assertThat(leafName, is(equalTo("GET:path(c,a)")));
    }

    @Test
    public void shouldReturnGetLeafNameWithTruncatedParam() throws Exception {
        // Given
        String nodeName = "path";
        URI uri =
                new URI(
                        "https://www.example.com/path?averylongvariablenamewhichshouldbetruncated=b",
                        true);
        // When
        String leafName = session.getLeafName(nodeName, uri, "GET", null);
        // Then
        assertThat(leafName, is(equalTo("GET:path(averylongvariablenamewhichshouldbetrunca...)")));
    }

    @Test
    public void shouldReturnPostLeafNameWithOnePostParam() throws Exception {
        // Given
        String nodeName = "path";
        URI uri = new URI("https://www.example.com/path", true);
        // When
        String leafName = session.getLeafName(nodeName, uri, "POST", "a=b");
        // Then
        assertThat(leafName, is(equalTo("POST:path(a)")));
    }

    @Test
    public void shouldReturnPostLeafNameWithTruncatedParam() throws Exception {
        // Given
        String nodeName = "path";
        URI uri = new URI("https://www.example.com/path", true);
        // When
        String leafName =
                session.getLeafName(
                        nodeName, uri, "POST", "averylongvariablenamewhichshouldbetruncated=b");
        // Then
        assertThat(leafName, is(equalTo("POST:path(averylongvariablenamewhichshouldbetrunca...)")));
    }

    @Test
    public void shouldReturnPostLeafNameWithOnePostAndOneUrlParam() throws Exception {
        // Given
        String nodeName = "path";
        URI uri = new URI("https://www.example.com/path?a=b", true);
        // When
        String leafName = session.getLeafName(nodeName, uri, "POST", "c=d");
        // Then
        assertThat(leafName, is(equalTo("POST:path(a)(c)")));
    }

    @Test
    public void shouldReturnPostMultiPartLeafName() throws Exception {
        // Given
        String nodeName = "path";
        URI uri = new URI("https://www.example.com/path", true);
        HttpMessage msg = new HttpMessage(uri);
        msg.getRequestHeader().setMethod("POST");
        msg.getRequestHeader().setHeader(HttpHeader.CONTENT_TYPE, "multipart/form-data etc");
        // When
        String leafName = session.getLeafName(nodeName, msg);
        // Then
        assertThat(leafName, is(equalTo("POST:path(multipart/form-data)")));
    }

    @Test
    public void shouldReturnOverridenLeafNameWithOneParam() throws Exception {
        // Given
        String nodeName = "path";
        URI uri = new URI("https://www.example.com/path?a=b", true);
        HttpMessage msg = new HttpMessage(uri);
        Variant variant = new LeadNameVariant();
        factory.addVariant(variant.getClass());
        String expectedLeafName = variant.getLeafName(nodeName, msg);

        // When
        String actualLeafName = session.getLeafName(nodeName, uri, "GET", null);

        // Then
        assertThat(actualLeafName, is(equalTo(expectedLeafName)));
    }

    @Test
    public void shouldReturnDefaultLeafNameWhenVariantRemoved() throws Exception {
        // Given
        String nodeName = "path";
        URI uri = new URI("https://www.example.com/path?a=b", true);
        Variant variant = new LeadNameVariant();

        // When
        factory.addVariant(variant.getClass());
        factory.removeVariant(variant.getClass());
        String actualLeafName = session.getLeafName(nodeName, uri, "GET", null);

        // Then
        assertThat(actualLeafName, is(equalTo("GET:path(a)")));
    }

    @Test
    public void shouldReturnShortPathTree() throws Exception {
        // Given
        URI uri = new URI("https://www.example.com/path", true);
        HttpMessage msg = new HttpMessage(uri);
        // When
        List<String> pathTree = session.getTreePath(msg);
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
        List<String> pathTree = session.getTreePath(msg);
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
        List<String> actualTreePath = session.getTreePath(msg);

        // Then
        assertThat(actualTreePath.size(), is(equalTo(expectedTreePath.size())));
        for (int i = 0; i < actualTreePath.size(); i++) {
            assertThat(actualTreePath.get(i), is(equalTo(expectedTreePath.get(i))));
        }
    }

    public static final class LeadNameVariant implements Variant {

        public LeadNameVariant() {}

        @Override
        public String getLeafName(String nodeName, HttpMessage msg) {
            return "Test";
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

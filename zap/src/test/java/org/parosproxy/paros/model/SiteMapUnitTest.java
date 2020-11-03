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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

/** Unit test for {@link SiteMap}. */
class SiteMapUnitTest {

    private long sessionId;
    private TableHistory tableHistory;
    private TableAlert tableAlert;

    private SiteNode rootNode;
    private SiteMap siteMap;

    @BeforeEach
    void setup() throws Exception {
        Session session = mock(Session.class);
        given(session.getTreePath(any(URI.class)))
                .willAnswer(
                        e -> {
                            String path = ((URI) e.getArgument(0)).getPath();
                            if (path == null) {
                                return Collections.emptyList();
                            }

                            String[] segments = path.split("/");
                            if (segments.length == 0) {
                                return Collections.emptyList();
                            }
                            return Arrays.asList(Arrays.copyOfRange(segments, 1, segments.length));
                        });
        given(session.getTreePath(any(HttpMessage.class)))
                .willAnswer(
                        e -> {
                            String path =
                                    ((HttpMessage) e.getArgument(0))
                                            .getRequestHeader()
                                            .getURI()
                                            .getPath();
                            if (path == null) {
                                return Collections.emptyList();
                            }

                            String[] segments = path.split("/");
                            if (segments.length == 1) {
                                return Collections.emptyList();
                            }
                            return Arrays.asList(Arrays.copyOfRange(segments, 1, segments.length));
                        });

        given(session.getLeafName(any(), any(URI.class), any(), any()))
                .willAnswer(
                        e -> {
                            String nodeName = (String) e.getArgument(0);
                            String method = (String) e.getArgument(2);
                            String data = (String) e.getArgument(3);

                            return buildLeafName(nodeName, method, data);
                        });

        given(session.getLeafName(any(), any()))
                .willAnswer(
                        e -> {
                            String nodeName = (String) e.getArgument(0);
                            HttpMessage msg = ((HttpMessage) e.getArgument(1));
                            String method = msg.getRequestHeader().getMethod();
                            String data = msg.getRequestBody().toString();

                            return buildLeafName(nodeName, method, data);
                        });
        sessionId = 1234L;
        given(session.getSessionId()).willReturn(sessionId);

        tableHistory = mock(TableHistory.class);
        given(
                        tableHistory.write(
                                eq(sessionId),
                                eq(HistoryReference.TYPE_TEMPORARY),
                                any(HttpMessage.class)))
                .willReturn(mock(RecordHistory.class));
        HistoryReference.setTableHistory(tableHistory);

        tableAlert = mock(TableAlert.class);
        given(tableAlert.getAlertsBySourceHistoryId(anyInt())).willReturn(Collections.emptyList());
        HistoryReference.setTableAlert(tableAlert);

        Model model = mock(Model.class);
        given(model.getSession()).willReturn(session);

        rootNode = new SiteNode(null, -1, "Root Node");
        siteMap = new SiteMap(rootNode, model);
    }

    private static String buildLeafName(String nodeName, String method, String data) {
        // Method must not be null.
        assertThat("Can not create leaf name with null method.", method, is(notNullValue()));

        StringBuilder sb = new StringBuilder();
        sb.append(method).append(':');
        sb.append(nodeName);
        if (data != null && !data.isEmpty()) {
            sb.append(" Params: ").append(data);
        }
        return sb.toString();
    }

    @AfterEach
    void cleanup() {
        HistoryReference.setTableHistory(null);
        HistoryReference.setTableAlert(null);
    }

    @Test
    void shouldNotFindSiteRootNodeWithUriIfTreeEmpty() {
        // Given
        String uri = "http://example.com";
        // When
        SiteNode node = siteMap.findNode(createUri(uri));
        // Then
        assertThat(node, is(nullValue()));
    }

    @Test
    void shouldNotFindSiteRootNodeWithUriIfNotPresent() {
        // Given
        String uri = "http://example.com";
        siteMapWithNodes("http://api.example.com", "http://example.org");
        // When
        SiteNode node = siteMap.findNode(createUri(uri));
        // Then
        assertThat(node, is(nullValue()));
    }

    @Test
    void shouldFindSiteRootNodeWithUriIfPresent() {
        // Given
        String uri = "http://api.example.com";
        siteMapWithNodes(uri, "http://example.org");
        // When
        SiteNode node = siteMap.findNode(createUri(uri));
        // Then
        assertThat(node, is(notNullValue()));
        assertThat(node.getNodeName(), is(equalTo(uri)));
        assertThat(node.getParent(), is(equalTo(rootNode)));
    }

    @Test
    void shouldNotFindSiteLeafNodeWithUriIfTreeEmpty() {
        // Given
        String uri = "http://example.com/file.ext";
        // When
        SiteNode node = siteMap.findNode(createUri(uri));
        // Then
        assertThat(node, is(nullValue()));
    }

    @Test
    void shouldNotFindSiteLeafNodeWithUriIfNotPresent() {
        // Given
        String uri = "http://example.com/file.ext";
        siteMapWithNodes("http://api.example.com/file.ext", "http://example.org/file.ext");
        // When
        SiteNode node = siteMap.findNode(createUri(uri));
        // Then
        assertThat(node, is(nullValue()));
    }

    @Test
    void shouldNotFindSiteLeafNodeWithUriAndMethodIfMethodIsDifferent() {
        // Given
        String uri = "http://example.com/file.ext";
        siteMapWithNodes(uri, "http://example.org/file.ext");
        // When
        SiteNode node = siteMap.findNode(createUri(uri), "POST", "");
        // Then
        assertThat(node, is(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "//", "/a/", "/a/b/"})
    void shouldFindSiteLeafNodeWithUriIfPresent(String path) {
        // Given
        String uri = "http://example.com" + path + "file.ext";
        siteMapWithNodes(uri, "http://example.org" + path + "file.ext");
        // When
        SiteNode node = siteMap.findNode(createUri(uri));
        // Then
        assertThat(node, is(notNullValue()));
        assertThat(node.getNodeName(), is(equalTo("GET:file.ext")));
        SiteNode parent = siteMap.findNode(createUri("http://example.com" + path));
        assertThat(node.getParent(), is(equalTo(parent)));
    }

    @Test
    void shouldNotFindSiteBranchNodeWithUriIfTreeEmpty() {
        // Given
        String uri = "http://example.com/a/";
        // When
        SiteNode node = siteMap.findNode(createUri(uri));
        // Then
        assertThat(node, is(nullValue()));
    }

    @Test
    void shouldNotFindSiteBranchNodeWithUriIfNotPresent() {
        // Given
        String uri = "http://example.com/a/";
        siteMapWithNodes("http://api.example.com/a/", "http://example.org/a/");
        // When
        SiteNode node = siteMap.findNode(createUri(uri));
        // Then
        assertThat(node, is(nullValue()));
    }

    @Test
    void shouldFindSiteBranchNodeWithUriIfPresent() {
        // Given
        String uri = "http://example.com/a/";
        siteMapWithNodes("http://example.com/a/file.ext", "http://example.org/a/");
        // When
        SiteNode node = siteMap.findNode(createUri(uri));
        // Then
        assertThat(node, is(notNullValue()));
        assertThat(node.getNodeName(), is(equalTo("a")));
        SiteNode parent = siteMap.findNode(createUri("http://example.com/"));
        assertThat(node.getParent(), is(equalTo(parent)));
    }

    private void siteMapWithNodes(String... uris) {
        Arrays.stream(uris).forEach(uri -> siteMap.addPath(createHistoryReference(uri)));
    }

    private static HistoryReference createHistoryReference(String uri) {
        return createHistoryReference(uri, "GET");
    }

    private static HistoryReference createHistoryReference(String uri, String method) {
        URI requestUri = createUri(uri);
        HistoryReference historyReference = mock(HistoryReference.class);
        given(historyReference.getURI()).willReturn(requestUri);
        try {
            HttpMessage httpMessage = new HttpMessage(requestUri);
            given(historyReference.getHttpMessage()).willReturn(httpMessage);
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            throw new RuntimeException(e);
        }
        return historyReference;
    }

    private static URI createUri(String uri) {
        try {
            return new URI(uri, true);
        } catch (URIException | NullPointerException e) {
            throw new RuntimeException(e);
        }
    }
}

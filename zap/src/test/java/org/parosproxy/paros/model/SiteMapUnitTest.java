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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.ascan.VariantFactory;
import org.zaproxy.zap.model.StandardParameterParser;

/** Unit test for {@link SiteMap}. */
class SiteMapUnitTest {

    private long sessionId;
    private TableHistory tableHistory;
    private TableAlert tableAlert;

    private SiteNode rootNode;
    private SiteMap siteMap;
    private VariantFactory factory;

    private Session session;

    @BeforeEach
    void setup() throws Exception {
        session = mock(Session.class);
        StandardParameterParser spp = new StandardParameterParser();
        given(session.getUrlParamParser(any(String.class))).willReturn(spp);
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

        Control.initSingletonForTesting(model);
        given(model.getSession()).willReturn(session);

        factory = new VariantFactory();
        given(model.getVariantFactory()).willReturn(factory);

        rootNode = new SiteNode(null, -1, "Root Node");
        siteMap = new SiteMap(rootNode, model);
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
    @ValueSource(strings = {"/a", "//b", "/a/b", "/a/b/c"})
    void shouldFindSiteLeafNodeWithUriIfPresent(String path) {
        // Given
        String uri = "http://example.com" + path + "/file.ext";
        siteMapWithNodes(uri, "http://example.org" + path + "/file.ext");
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
        String uri = "http://example.com/a";
        siteMapWithNodes("http://example.com/a/file.ext", "http://example.org/a/");
        // When
        SiteNode node = siteMap.findNode(createUri(uri));
        // Then
        assertThat(node, is(notNullValue()));
        assertThat(node.getNodeName(), is(equalTo("a")));
        SiteNode parent = siteMap.findNode(createUri("http://example.com"));
        assertThat(node.getParent(), is(equalTo(parent)));
    }

    @Test
    void shouldCreateSlashNodeIfUriEndsWithASlash() {
        // Given
        String branchUri = "http://example.com/a";
        String leafUri = "http://example.com/a/";
        siteMapWithNodes(branchUri, leafUri);
        // When
        SiteNode branchNode = siteMap.findNode(createUri(branchUri));
        SiteNode leafNode = siteMap.findNode(createUri(leafUri));
        // Then
        assertThat(branchNode, is(notNullValue()));
        assertThat(leafNode, is(notNullValue()));
        assertThat(branchNode.getHierarchicNodeName(), is(equalTo(branchUri)));
        assertThat(leafNode.getHierarchicNodeName(), is(equalTo(leafUri)));
        assertThat(leafNode.getParent(), is(equalTo(branchNode)));
    }

    @Test
    void shouldNotReturnCachedHistoryReference() throws Exception {
        // Given
        String uri = "http://example.com";
        HistoryReference href = createHistoryReference(uri);
        given(href.getHistoryType()).willReturn(HistoryReference.TYPE_ZAP_USER);
        given(href.getHistoryId()).willReturn(100);
        // When
        siteMap.addPath(href);
        siteMap.addPath(href);
        siteMap.addPath(href, href.getHttpMessage(), false);
        // Then
        verify(session, times(3)).getUrlParamParser(anyString());
    }

    @Test
    void shouldNotUseCacheForDifferentHistoryReference() throws Exception {
        // Given
        String uri = "http://example.com";
        HistoryReference href1 = createHistoryReference(uri);
        HistoryReference href2 = createHistoryReference(uri);
        given(href1.getHistoryType()).willReturn(HistoryReference.TYPE_ZAP_USER);
        given(href2.getHistoryType()).willReturn(HistoryReference.TYPE_ZAP_USER);
        given(href1.getHistoryId()).willReturn(101);
        given(href2.getHistoryId()).willReturn(102);
        // When
        siteMap.addPath(href1);
        siteMap.addPath(href2);
        // Then
        verify(session, times(2)).getUrlParamParser(anyString());
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

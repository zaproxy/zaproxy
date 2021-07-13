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
package org.zaproxy.zap.spider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DatabaseServer;
import org.parosproxy.paros.db.DatabaseUnsupportedException;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.extension.spider.ExtensionSpider;
import org.zaproxy.zap.spider.parser.SpiderResourceFound;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link SpiderControllerResourcesFoundUnitTest}. */
@ExtendWith(MockitoExtension.class)
class SpiderControllerResourcesFoundUnitTest {

    /** Sets up the messages in {@link Constant}. */
    @BeforeEach
    void setUpZap() {
        Constant.getInstance();
        I18N i18n = Mockito.mock(I18N.class, withSettings().lenient());
        given(i18n.getString(anyString())).willReturn("");
        given(i18n.getString(anyString(), any())).willReturn("");
        given(i18n.getLocal()).willReturn(Locale.getDefault());
        Constant.messages = i18n;
    }

    @Test
    void shouldAddDifferentSpiderResources() {
        // Given
        TestSpiderController testSpiderController = new TestSpiderController(new TestSpider());
        List<HttpHeaderField> requestHeaders = new ArrayList<>();
        requestHeaders.add(new HttpHeaderField("X-Custom-Header-1", "xyz"));
        // When
        testSpiderController.resourceFound(
                createBasicGetSpiderResourceFound("https://example.com/test.html", 1));
        testSpiderController.resourceFound(
                createBasicGetSpiderResourceFound("https://example.com/test.html/", 1));
        testSpiderController.resourceFound(
                createGetSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", 1, false, requestHeaders));
        testSpiderController.resourceFound(
                createBasicPostSpiderResourceFound("https://example.com/test.html", "", 1));
        testSpiderController.resourceFound(
                createBasicPostSpiderResourceFound("https://example.com/test.html", "A=1", 1));
        testSpiderController.resourceFound(
                createBasicPostSpiderResourceFound("https://example.com/test.html", "A=2", 1));
        testSpiderController.resourceFound(
                createPostSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", "A=2", 1, false, requestHeaders));
        // Then
        assertThat(testSpiderController.getNumberOfSubmittedSpiderTasks(), is(equalTo(7)));
    }

    @Test
    void shouldAddSpiderResourcesWithDifferentMethods() {
        // Given
        TestSpiderController testSpiderController = new TestSpiderController(new TestSpider());
        SpiderResourceFound getResource =
                SpiderResourceFound.builder()
                        .setMethod(HttpRequestHeader.GET)
                        .setUri("http://test.com")
                        .build();
        SpiderResourceFound postResource =
                SpiderResourceFound.builder()
                        .setMethod(HttpRequestHeader.POST)
                        .setUri("http://test.com")
                        .build();
        SpiderResourceFound putResource =
                SpiderResourceFound.builder()
                        .setMethod(HttpRequestHeader.PUT)
                        .setUri("http://test.com")
                        .build();
        SpiderResourceFound deleteResource =
                SpiderResourceFound.builder()
                        .setMethod(HttpRequestHeader.DELETE)
                        .setUri("http://test.com")
                        .build();
        SpiderResourceFound headResource =
                SpiderResourceFound.builder()
                        .setMethod(HttpRequestHeader.HEAD)
                        .setUri("http://test.com")
                        .build();
        // When
        testSpiderController.resourceFound(getResource);
        testSpiderController.resourceFound(postResource);
        testSpiderController.resourceFound(putResource);
        testSpiderController.resourceFound(deleteResource);
        testSpiderController.resourceFound(headResource);
        // Then
        assertThat(testSpiderController.getNumberOfSubmittedSpiderTasks(), is(equalTo(5)));
    }

    @Test
    void shouldNotAddSameGetSpiderResourcesWithDifferentDepthAndIgnore() {
        // Given
        TestSpiderController testSpiderController = new TestSpiderController(new TestSpider());
        SpiderResourceFound spiderResourceFoundDepth1 =
                createGetSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", 1, false, Collections.emptyList());
        SpiderResourceFound spiderResourceFoundDepth2Ignore =
                createGetSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", 2, true, Collections.emptyList());
        // When
        testSpiderController.resourceFound(spiderResourceFoundDepth1);
        testSpiderController.resourceFound(spiderResourceFoundDepth1);
        testSpiderController.resourceFound(spiderResourceFoundDepth2Ignore);
        // Then
        assertThat(testSpiderController.getNumberOfSubmittedSpiderTasks(), is(equalTo(1)));
    }

    @Test
    void shouldNotAddSamePostSpiderResourcesWithDifferentDepthAndIgnore() {
        // Given
        TestSpiderController testSpiderController = new TestSpiderController(new TestSpider());
        SpiderResourceFound spiderResourceFoundDepth1 =
                createPostSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", "body", 1, false, Collections.emptyList());
        SpiderResourceFound spiderResourceFoundDepth2Ignore =
                createPostSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", "body", 2, true, Collections.emptyList());
        // When
        testSpiderController.resourceFound(spiderResourceFoundDepth1);
        testSpiderController.resourceFound(spiderResourceFoundDepth1);
        testSpiderController.resourceFound(spiderResourceFoundDepth2Ignore);
        // Then
        assertThat(testSpiderController.getNumberOfSubmittedSpiderTasks(), is(equalTo(1)));
    }

    @Test
    void shouldNotAddSameGetSpiderResourcesWithDifferentHeaderOrder() {
        // Given
        TestSpiderController testSpiderController = new TestSpiderController(new TestSpider());
        List<HttpHeaderField> requestHeadersOrder1 = new ArrayList<>();
        requestHeadersOrder1.add(new HttpHeaderField("X-Custom-Header-1", "xyz"));
        requestHeadersOrder1.add(new HttpHeaderField("X-Custom-Header-2", "123"));
        List<HttpHeaderField> requestHeadersOrder2 = new ArrayList<>();
        requestHeadersOrder2.add(new HttpHeaderField("X-Custom-Header-2", "123"));
        requestHeadersOrder2.add(new HttpHeaderField("X-Custom-Header-1", "xyz"));
        SpiderResourceFound spiderResourceFound1 =
                createGetSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", 2, false, requestHeadersOrder1);
        SpiderResourceFound spiderResourceFound2 =
                createGetSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", 2, false, requestHeadersOrder2);
        // When
        testSpiderController.resourceFound(spiderResourceFound1);
        testSpiderController.resourceFound(spiderResourceFound2);
        // Then
        assertThat(testSpiderController.getNumberOfSubmittedSpiderTasks(), is(equalTo(1)));
    }

    @Test
    void shouldNotAddSameGetSpiderResourcesWithDifferentHeaderWhitespaces() {
        // Given
        TestSpiderController testSpiderController = new TestSpiderController(new TestSpider());
        List<HttpHeaderField> requestHeadersWithoutWS = new ArrayList<>();
        requestHeadersWithoutWS.add(new HttpHeaderField("X-Custom-Header-1", "xyz"));
        List<HttpHeaderField> requestHeadersWithWS = new ArrayList<>();
        requestHeadersWithWS.add(new HttpHeaderField("\tX-Custom-Header-1  ", "\nxyz "));
        SpiderResourceFound spiderResourceFound1 =
                createGetSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", 2, false, requestHeadersWithoutWS);
        SpiderResourceFound spiderResourceFound2 =
                createGetSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", 2, false, requestHeadersWithWS);
        // When
        testSpiderController.resourceFound(spiderResourceFound1);
        testSpiderController.resourceFound(spiderResourceFound2);
        // Then
        assertThat(testSpiderController.getNumberOfSubmittedSpiderTasks(), is(equalTo(1)));
    }

    @Test
    void shouldNotAddSameGetSpiderResourcesWithDifferentHeaderCases() {
        // Given
        TestSpiderController testSpiderController = new TestSpiderController(new TestSpider());
        List<HttpHeaderField> requestHeadersUpperCase = new ArrayList<>();
        requestHeadersUpperCase.add(new HttpHeaderField("X-CUSTOM-HEADER-1", "XYZ"));
        List<HttpHeaderField> requestHeadersLowerCase = new ArrayList<>();
        requestHeadersLowerCase.add(new HttpHeaderField("x-custom-header-1", "xyz"));
        SpiderResourceFound spiderResourceFound1 =
                createGetSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", 2, false, requestHeadersUpperCase);
        SpiderResourceFound spiderResourceFound2 =
                createGetSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", 2, false, requestHeadersLowerCase);
        // When
        testSpiderController.resourceFound(spiderResourceFound1);
        testSpiderController.resourceFound(spiderResourceFound2);
        // Then
        assertThat(testSpiderController.getNumberOfSubmittedSpiderTasks(), is(equalTo(1)));
    }

    @Test
    void shouldNotAddSameGetSpiderResourcesWithDuplicateHeaders() {
        // Given
        TestSpiderController testSpiderController = new TestSpiderController(new TestSpider());
        List<HttpHeaderField> requestHeadersWithoutDuplicates = new ArrayList<>();
        requestHeadersWithoutDuplicates.add(new HttpHeaderField("X-Custom-Header-1", "xyz"));
        List<HttpHeaderField> requestHeadersWithDuplicates = new ArrayList<>();
        requestHeadersWithDuplicates.add(new HttpHeaderField("X-Custom-Header-1", "xyz"));
        requestHeadersWithDuplicates.add(new HttpHeaderField("X-Custom-Header-1", "xyz"));
        requestHeadersWithDuplicates.add(new HttpHeaderField("X-Custom-Header-1", "xyz"));
        SpiderResourceFound spiderResourceFound1 =
                createGetSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", 2, false, requestHeadersWithoutDuplicates);
        SpiderResourceFound spiderResourceFound2 =
                createGetSpiderResourceFoundWithHeaders(
                        "https://example.com/test.html", 2, false, requestHeadersWithDuplicates);
        // When
        testSpiderController.resourceFound(spiderResourceFound1);
        testSpiderController.resourceFound(spiderResourceFound2);
        // Then
        assertThat(testSpiderController.getNumberOfSubmittedSpiderTasks(), is(equalTo(1)));
    }

    private static SpiderResourceFound createBasicGetSpiderResourceFound(String uri, int depth) {
        return SpiderResourceFound.builder().setDepth(depth).setUri(uri).build();
    }

    private static SpiderResourceFound createGetSpiderResourceFoundWithHeaders(
            String uri, int depth, boolean shouldIgnore, List<HttpHeaderField> requestHeaders) {
        return SpiderResourceFound.builder()
                .setDepth(depth)
                .setUri(uri)
                .setShouldIgnore(shouldIgnore)
                .setRequestHeaders(requestHeaders)
                .build();
    }

    private static SpiderResourceFound createBasicPostSpiderResourceFound(
            String uri, String body, int depth) {
        return SpiderResourceFound.builder()
                .setDepth(depth)
                .setUri(uri)
                .setMethod(HttpRequestHeader.POST)
                .setBody(body)
                .build();
    }

    private static SpiderResourceFound createPostSpiderResourceFoundWithHeaders(
            String uri,
            String body,
            int depth,
            boolean shouldIgnore,
            List<HttpHeaderField> requestHeaders) {
        return SpiderResourceFound.builder()
                .setDepth(depth)
                .setUri(uri)
                .setShouldIgnore(shouldIgnore)
                .setMethod(HttpRequestHeader.POST)
                .setBody(body)
                .setRequestHeaders(requestHeaders)
                .build();
    }

    /** Test spider controller without any default parsers (expecting a test spider). */
    private static class TestSpiderController extends SpiderController {
        private TestSpider spider;

        TestSpiderController(TestSpider spider) {
            super(spider, new ArrayList<>());
            this.spider = spider;
        }

        @Override
        protected void prepareDefaultParsers() {
            // Do nothing
        }

        int getNumberOfSubmittedSpiderTasks() {
            return spider.getSubmittedSpiderTasks().size();
        }
    }

    /** Test spider with dummy messages and no spider task submission. */
    private static class TestSpider extends Spider {
        private static final ExtensionSpider DUMMY_SPIDER_EXTENSION;
        private List<SpiderTask> submittedSpiderTasks = new ArrayList<>();

        static {
            DUMMY_SPIDER_EXTENSION = new ExtensionSpider();
            DUMMY_SPIDER_EXTENSION.setMessages(
                    new ResourceBundle() {
                        @Override
                        protected Object handleGetObject(String key) {
                            return "";
                        }

                        @Override
                        public Enumeration<String> getKeys() {
                            return Collections.emptyEnumeration();
                        }
                    });
            HistoryReference.setTableHistory(new TestTableHistory());
            HistoryReference.setTableAlert(new TestTableAlert());
        }

        TestSpider() {
            super(
                    "test",
                    DUMMY_SPIDER_EXTENSION,
                    new SpiderParam(),
                    new ConnectionParam(),
                    Model.getSingleton(),
                    null);
        }

        @Override
        protected synchronized void submitTask(SpiderTask task) {
            submittedSpiderTasks.add(task);
        }

        List<SpiderTask> getSubmittedSpiderTasks() {
            return submittedSpiderTasks;
        }
    }

    /** Test table for history doing nothing. */
    private static class TestTableHistory implements TableHistory {

        @Override
        public void databaseOpen(DatabaseServer dbServer)
                throws DatabaseException, DatabaseUnsupportedException {
            // Do nothing
        }

        @Override
        public RecordHistory write(long sessionId, int histType, HttpMessage msg)
                throws HttpMalformedHeaderException, DatabaseException {
            return new RecordHistory();
        }

        @Override
        public void updateNote(int historyId, String note) throws DatabaseException {
            // Do nothing
        }

        @Override
        public RecordHistory read(int historyId)
                throws HttpMalformedHeaderException, DatabaseException {
            return new RecordHistory();
        }

        @Override
        public int lastIndex() {
            return 0;
        }

        @Override
        public List<Integer> getHistoryList(
                long sessionId, int histType, String filter, boolean isRequest)
                throws DatabaseException {
            return Collections.emptyList();
        }

        @Override
        public List<Integer> getHistoryIdsStartingAt(long sessionId, int startAtHistoryId)
                throws DatabaseException {
            return Collections.emptyList();
        }

        @Override
        public List<Integer> getHistoryIdsOfHistTypeStartingAt(
                long sessionId, int startAtHistoryId, int... histTypes) throws DatabaseException {
            return Collections.emptyList();
        }

        @Override
        public List<Integer> getHistoryIdsOfHistType(long sessionId, int... histTypes)
                throws DatabaseException {
            return Collections.emptyList();
        }

        @Override
        public List<Integer> getHistoryIdsExceptOfHistTypeStartingAt(
                long sessionId, int startAtHistoryId, int... histTypes) throws DatabaseException {
            return Collections.emptyList();
        }

        @Override
        public List<Integer> getHistoryIdsExceptOfHistType(long sessionId, int... histTypes)
                throws DatabaseException {
            return Collections.emptyList();
        }

        @Override
        public List<Integer> getHistoryIds(long sessionId) throws DatabaseException {
            return Collections.emptyList();
        }

        @Override
        public RecordHistory getHistoryCache(HistoryReference ref, HttpMessage reqMsg)
                throws DatabaseException, HttpMalformedHeaderException {
            return new RecordHistory();
        }

        @Override
        public void deleteTemporary() throws DatabaseException {
            // Do nothing
        }

        @Override
        public void deleteHistoryType(long sessionId, int historyType) throws DatabaseException {
            // Do nothing
        }

        @Override
        public void deleteHistorySession(long sessionId) throws DatabaseException {
            // Do nothing
        }

        @Override
        public void delete(List<Integer> ids, int batchSize) throws DatabaseException {
            // Do nothing
        }

        @Override
        public void delete(List<Integer> ids) throws DatabaseException {
            // Do nothing
        }

        @Override
        public void delete(int historyId) throws DatabaseException {
            // Do nothing
        }

        @Override
        public boolean containsURI(
                long sessionId, int historyType, String method, String uri, byte[] body)
                throws DatabaseException {
            return false;
        }
    }

    /** Test table for alerts doing nothing. */
    private static class TestTableAlert implements TableAlert {

        @Override
        public void databaseOpen(DatabaseServer dbServer)
                throws DatabaseException, DatabaseUnsupportedException {
            // Do nothing
        }

        @Override
        public RecordAlert read(int alertId) throws DatabaseException {
            return new RecordAlert();
        }

        @Override
        public RecordAlert write(
                int scanId,
                int pluginId,
                String alert,
                int risk,
                int confidence,
                String description,
                String uri,
                String param,
                String attack,
                String otherInfo,
                String solution,
                String reference,
                String evidence,
                int cweId,
                int wascId,
                int historyId,
                int sourceHistoryId,
                int sourceId,
                String alertRef)
                throws DatabaseException {
            return new RecordAlert();
        }

        @Override
        public Vector<Integer> getAlertListBySession(long sessionId) throws DatabaseException {
            return new Vector<>();
        }

        @Override
        public void deleteAlert(int alertId) throws DatabaseException {
            // Do nothing
        }

        @Override
        public int deleteAllAlerts() throws DatabaseException {
            return 0;
        }

        @Override
        public void update(
                int alertId,
                String alert,
                int risk,
                int confidence,
                String description,
                String uri,
                String param,
                String attack,
                String otherInfo,
                String solution,
                String reference,
                String evidence,
                int cweId,
                int wascId,
                int sourceHistoryId)
                throws DatabaseException {
            // Do nothing
        }

        @Override
        public void updateHistoryIds(int alertId, int historyId, int sourceHistoryId)
                throws DatabaseException {
            // Do nothing
        }

        @Override
        public List<RecordAlert> getAlertsBySourceHistoryId(int historyId)
                throws DatabaseException {
            return Collections.emptyList();
        }

        @Override
        public Vector<Integer> getAlertList() throws DatabaseException {
            return new Vector<>();
        }
    }
}

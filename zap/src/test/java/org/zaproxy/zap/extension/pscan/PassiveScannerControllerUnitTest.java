/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.zaproxy.zap.extension.pscan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import net.htmlparser.jericho.Source;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.testutils.TestUtils;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link PassiveScannerController}. */
class PassiveScannerControllerUnitTest extends TestUtils {

    private static final String EXAMPLE_URL = "https://www.example.com";
    private PassiveScanController psc;
    private PassiveScannerList passiveScannerList;
    private ExtensionHistory extHistory;
    private ExtensionPassiveScan extPscan;
    private ExtensionAlert extAlert;
    private PassiveScanParam passiveScanParam;
    private Session session;

    @BeforeEach
    void setUp() throws Exception {
        Constant.messages = mock(I18N.class);
        Control.initSingletonForTesting();

        passiveScannerList = mock(PassiveScannerList.class);
        extHistory = mock(ExtensionHistory.class);
        extPscan = mock(ExtensionPassiveScan.class);
        extAlert = mock(ExtensionAlert.class);
        passiveScanParam = mock(PassiveScanParam.class);
        session = mock(Session.class);

        given(extPscan.getPassiveScannerList()).willReturn(passiveScannerList);
        given(passiveScanParam.getPassiveScanThreads()).willReturn(2);

        psc = new PassiveScanController(extPscan, extHistory, extAlert, passiveScanParam, null);
        psc.setSession(session);
    }

    private void sleep(int msecs) {
        try {
            Thread.sleep(msecs);
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    @Test
    void shouldProcessInScopeHistoryRecord() throws Exception {
        // Given
        HttpMessage msg = new HttpMessage(new URI(EXAMPLE_URL, true));
        msg.setResponseFromTargetHost(true);

        HistoryReference href = mock(HistoryReference.class);
        given(href.getHttpMessage()).willReturn(msg);
        given(extHistory.getLastHistoryId()).willReturn(1);
        given(extHistory.getHistoryReference(1)).willReturn(href);

        TestPassiveScanner scanner = new TestPassiveScanner(true);
        List<PassiveScanner> pscanList = new ArrayList<>();
        pscanList.add(scanner);
        given(passiveScannerList.list()).willReturn(pscanList);

        // When
        psc.start();
        psc.onHttpResponseReceive(null);
        sleep(500);
        psc.shutdown();

        // Then
        assertThat(psc.getRecordsToScan(), is(equalTo(0)));
        assertThat(scanner.isScannedRequest(), is(equalTo(true)));
        assertThat(scanner.isScannedResponse(), is(equalTo(true)));
    }

    @Test
    void shouldProcessOutOfScopeHistoryRecordByDefault() throws Exception {
        // Given
        HttpMessage msg = new HttpMessage(new URI(EXAMPLE_URL, true));
        msg.setResponseFromTargetHost(true);

        HistoryReference href = mock(HistoryReference.class);
        given(href.getHttpMessage()).willReturn(msg);
        given(extHistory.getLastHistoryId()).willReturn(1);
        given(extHistory.getHistoryReference(1)).willReturn(href);

        // Key config
        given(session.isInScope(href)).willReturn(false);

        TestPassiveScanner scanner = new TestPassiveScanner(true);
        List<PassiveScanner> pscanList = new ArrayList<>();
        pscanList.add(scanner);
        given(passiveScannerList.list()).willReturn(pscanList);

        // When
        psc.start();
        psc.onHttpResponseReceive(null);
        sleep(500);
        psc.shutdown();

        // Then
        assertThat(psc.getRecordsToScan(), is(equalTo(0)));
        assertThat(scanner.isScannedRequest(), is(equalTo(true)));
        assertThat(scanner.isScannedResponse(), is(equalTo(true)));
    }

    @Test
    void shouldNotProcessOutOfScopeHistoryRecordIfOptionSet() throws Exception {
        // Given
        HttpMessage msg = new HttpMessage(new URI(EXAMPLE_URL, true));
        msg.setResponseFromTargetHost(true);

        HistoryReference href = mock(HistoryReference.class);
        given(href.getHttpMessage()).willReturn(msg);
        given(extHistory.getLastHistoryId()).willReturn(1);
        given(extHistory.getHistoryReference(1)).willReturn(href);

        // Key config
        given(session.isInScope(href)).willReturn(false);
        given(passiveScanParam.isScanOnlyInScope()).willReturn(true);

        TestPassiveScanner scanner = new TestPassiveScanner(true);
        List<PassiveScanner> pscanList = new ArrayList<>();
        pscanList.add(scanner);
        given(passiveScannerList.list()).willReturn(pscanList);

        // When
        psc.start();
        psc.onHttpResponseReceive(null);
        sleep(500);
        psc.shutdown();

        // Then
        assertThat(psc.getRecordsToScan(), is(equalTo(0)));
        assertThat(scanner.isScannedRequest(), is(equalTo(false)));
        assertThat(scanner.isScannedResponse(), is(equalTo(false)));
    }

    @Test
    void shouldReturnRunningTasks() throws Exception {
        // Given
        String exampleUrl1 = EXAMPLE_URL + "/1";
        String exampleUrl2 = EXAMPLE_URL + "/2";
        HttpMessage msg1 = new HttpMessage(new URI(exampleUrl1, true));
        msg1.setResponseFromTargetHost(true);
        HttpMessage msg2 = new HttpMessage(new URI(exampleUrl2, true));
        msg2.setResponseFromTargetHost(true);

        HistoryReference href1 = mock(HistoryReference.class);
        HistoryReference href2 = mock(HistoryReference.class);
        given(href1.getHistoryId()).willReturn(1);
        given(href2.getHistoryId()).willReturn(2);
        given(href1.getHttpMessage()).willReturn(msg1);
        given(href2.getHttpMessage()).willReturn(msg2);
        given(href1.getURI()).willReturn(new URI(exampleUrl1, true));
        given(href2.getURI()).willReturn(new URI(exampleUrl2, true));
        when(extHistory.getLastHistoryId()).thenReturn(1).thenReturn(2);
        given(extHistory.getHistoryReference(1)).willReturn(href1);
        given(extHistory.getHistoryReference(2)).willReturn(href2);

        TestPassiveScanner scanner = new TestPassiveScanner("TPS", true, true);
        List<PassiveScanner> pscanList = new ArrayList<>();
        pscanList.add(scanner);
        given(passiveScannerList.list()).willReturn(pscanList);

        // When
        psc.start();
        psc.onHttpResponseReceive(null);
        long testStartTime = System.currentTimeMillis();
        sleep(500);
        PassiveScanTask oldestTask = psc.getOldestRunningTask();
        List<PassiveScanTask> tasks = psc.getRunningTasks();
        int recordsToScan = psc.getRecordsToScan();
        scanner.setBlock(false);
        sleep(500);
        psc.shutdown();
        long testEndTime = System.currentTimeMillis();

        // Then
        assertThat(psc.getRecordsToScan(), is(equalTo(0)));
        assertThat(psc.getOldestRunningTask(), is(nullValue()));
        assertThat(psc.getRunningTasks().size(), is(equalTo(0)));
        assertThat(oldestTask.getCurrentScanner().getName(), is(equalTo("TPS")));
        assertThat(oldestTask.getURI().toString(), is(equalTo(exampleUrl1)));
        assertThat(oldestTask.getStartTime(), is(greaterThan(testStartTime)));
        assertThat(testEndTime, is(greaterThan(oldestTask.getStartTime())));
        assertThat(tasks.size(), is(equalTo(2)));
        assertThat(recordsToScan, is(equalTo(2)));
        assertThat(tasks.get(0).getCurrentScanner().getName(), is(equalTo("TPS")));
        assertThat(tasks.get(0).getHistoryReference().getHistoryId(), is(equalTo(1)));
        assertThat(tasks.get(1).getHistoryReference().getHistoryId(), is(equalTo(2)));
        assertThat(tasks.get(0).getURI().toString(), is(equalTo(exampleUrl1)));
        assertThat(tasks.get(1).getURI().toString(), is(equalTo(exampleUrl2)));
    }

    class TestPassiveScanner implements PassiveScanner {

        private boolean enabled;
        private boolean block = false;
        private boolean scannedRequest = false;
        private boolean scannedResponse = false;
        private String name;

        public TestPassiveScanner(boolean enabled) {
            this.enabled = enabled;
        }

        public TestPassiveScanner(String name, boolean enabled, boolean block) {
            this.name = name;
            this.enabled = enabled;
            this.block = block;
        }

        public void setBlock(boolean block) {
            this.block = block;
        }

        @Override
        public void scanHttpRequestSend(HttpMessage msg, int id) {
            while (block) {
                sleep(100);
            }
            scannedRequest = true;
        }

        @Override
        public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
            while (block) {
                sleep(100);
            }
            scannedResponse = true;
        }

        public boolean isScannedRequest() {
            return scannedRequest;
        }

        public boolean isScannedResponse() {
            return scannedResponse;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setEnabled(boolean enabled) {}

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public boolean appliesToHistoryType(int historyType) {
            return true;
        }
    }
}

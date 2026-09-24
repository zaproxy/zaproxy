/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.extension.ascan.ScanPolicy;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for Scanner pause timer functionality. */
class ScannerPauseTimerUnitTest {

    private Scanner scanner;
    private ScannerParam scannerParam;
    private ScanPolicy scanPolicy;

    @BeforeAll
    static void beforeAll() {
        Constant.messages = mock(I18N.class);
    }

    @AfterAll
    static void afterAll() {
        Constant.messages = null;
    }

    @BeforeEach
    @SuppressWarnings("deprecation")
    void setup() {
        Model model = mock(Model.class);
        Model.setSingletonForTesting(model);
        OptionsParam optionsParam = mock(OptionsParam.class);
        given(model.getOptionsParam()).willReturn(optionsParam);
        given(optionsParam.getConnectionParam())
                .willReturn(mock(org.parosproxy.paros.network.ConnectionParam.class));

        ExtensionLoader extensionLoader = mock(ExtensionLoader.class);
        Control.initSingletonForTesting(model, extensionLoader);

        scannerParam = new ScannerParam();
        scannerParam.load(new ZapXmlConfiguration());

        PluginFactory pluginFactory = mock(PluginFactory.class);
        scanPolicy = mock(ScanPolicy.class);
        given(scanPolicy.getPluginFactory()).willReturn(pluginFactory);
        given(pluginFactory.clone()).willReturn(pluginFactory);

        scanner = new Scanner(scannerParam, scanPolicy, null);
    }

    @AfterEach
    void tearDown() {
        scanner.stop();
    }

    @Test
    void shouldStartWithZeroElapsedTime() {
        // Given / When
        long elapsed = scanner.getElapsedMillis();
        // Then
        assertThat(elapsed, is(0L));
    }

    @Test
    void shouldNotBePausedInitially() {
        // Given / When / Then
        assertThat(scanner.isPaused(), is(false));
    }

    @Test
    void shouldBePausedAfterPauseCall() {
        // Given / When
        scanner.start(new SiteNode(null, 0, "http://example.com"));
        scanner.pause();
        // Then
        assertThat(scanner.isPaused(), is(true));
    }

    @Test
    void shouldNotBePausedAfterResumeCall() {
        // Given
        scanner.start(new SiteNode(null, 0, "http://example.com"));
        scanner.pause();
        // When
        scanner.resume();
        // Then
        assertThat(scanner.isPaused(), is(false));
    }

    @Test
    void shouldExcludePausedTimeFromElapsed() throws InterruptedException {
        // Run for a known time, pause for a longer time (must not be counted), run again.
        // Elapsed should increase only by the two running periods, not the pause.
        long runMs = 80L;
        long pauseMs = 150L;

        scanner.start(new SiteNode(null, 0, "http://example.com"));
        Thread.sleep(runMs);
        long elapsedAfterRun1 = scanner.getElapsedMillis();

        scanner.pause();
        Thread.sleep(pauseMs);
        scanner.resume();
        Thread.sleep(runMs);
        long elapsedAfterRun2 = scanner.getElapsedMillis();

        long deltaMs = elapsedAfterRun2 - elapsedAfterRun1;
        // First run was at least partly recorded
        assertThat(elapsedAfterRun1, greaterThanOrEqualTo(runMs / 2));
        // Delta = second run only (pause must be excluded): ~runMs, with tolerance for CI
        assertThat(deltaMs, greaterThanOrEqualTo(runMs / 2));
        // If pause were counted, delta would be ~runMs + pauseMs; upper bound catches that
        assertThat(
                "Paused time must not be included in elapsed",
                deltaMs,
                lessThanOrEqualTo(runMs + 100L));
    }

    @Test
    void shouldReturnNullTimeStartedWhenNotStarted() {
        // Given / When
        Date timeStarted = scanner.getTimeStarted();
        // Then
        assertThat(timeStarted, is(nullValue()));
    }

    @Test
    void shouldReturnNullTimeFinishedWhenNotFinished() {
        // Given / When
        Date timeFinished = scanner.getTimeFinished();
        // Then
        assertThat(timeFinished, is(nullValue()));
    }

    @Test
    void shouldReturnNullEffectiveInstantWhenNotStarted() {
        // Given / When
        Instant effectiveInstant = scanner.getEffectiveInstant();
        // Then
        assertThat(effectiveInstant, is(nullValue()));
    }

    @Test
    void shouldWaitIfPausedReturnImmediatelyWhenNotPaused() throws InterruptedException {
        // Given - scanner is not paused
        assertThat(scanner.isPaused(), is(false));
        // When / Then - waitIfPaused should return immediately
        CountDownLatch latch = new CountDownLatch(1);
        Thread thread =
                new Thread(
                        () -> {
                            scanner.waitIfPaused();
                            latch.countDown();
                        });
        thread.start();

        // Should complete very quickly
        boolean completed = latch.await(100, TimeUnit.MILLISECONDS);
        assertThat("waitIfPaused should return immediately when not paused", completed, is(true));
    }

    @Test
    void shouldBlockWaitIfPausedWhenPaused() throws InterruptedException {
        // Given
        scanner.start(new SiteNode(null, 0, "http://example.com"));
        scanner.pause();
        assertThat(scanner.isPaused(), is(true));

        AtomicBoolean waitCompleted = new AtomicBoolean(false);
        // When - start a thread that calls waitIfPaused
        Thread waitingThread =
                new Thread(
                        () -> {
                            scanner.waitIfPaused();
                            waitCompleted.set(true);
                        });
        waitingThread.start();
        // Then - thread should still be waiting after a short delay
        Thread.sleep(50);
        assertThat("Thread should be blocked while paused", waitCompleted.get(), is(false));
        // When - resume the scanner
        scanner.resume();
        // Then - thread should complete
        waitingThread.join(500);
        assertThat("Thread should complete after resume", waitCompleted.get(), is(true));
    }

    @Test
    void shouldUnblockWaitIfPausedWhenStopped() throws InterruptedException {
        // Given
        scanner.start(new SiteNode(null, 0, "http://example.com"));
        scanner.pause();

        AtomicBoolean waitCompleted = new AtomicBoolean(false);
        // When - start a thread that calls waitIfPaused
        Thread waitingThread =
                new Thread(
                        () -> {
                            scanner.waitIfPaused();
                            waitCompleted.set(true);
                        });
        waitingThread.start();
        // Ensure thread is blocked
        Thread.sleep(50);
        assertThat("Thread should be blocked while paused", waitCompleted.get(), is(false));
        // When - stop the scanner (which should resume first, then stop)
        scanner.stop();
        // Then - thread should complete
        waitingThread.join(500);
        assertThat("Thread should complete after stop", waitCompleted.get(), is(true));
    }

    @Test
    void shouldResumeScannerOnStopIfPaused() {
        // Given
        scanner.start(new SiteNode(null, 0, "http://example.com"));
        scanner.pause();
        assertThat(scanner.isPaused(), is(true));
        // When
        scanner.stop();
        // Then
        assertThat(scanner.isPaused(), is(false));
    }

    @Test
    void shouldIdempotentlyHandleMultiplePauseCalls() {
        // Given / When - call pause multiple times
        scanner.start(new SiteNode(null, 0, "http://example.com"));
        scanner.pause();
        scanner.pause();
        scanner.pause();
        // Then - should still be paused (no exception)
        assertThat(scanner.isPaused(), is(true));
    }

    @Test
    void shouldIdempotentlyHandleMultipleResumeCalls() {
        // Given - start/pause first
        scanner.start(new SiteNode(null, 0, "http://example.com"));
        scanner.pause();
        // When - call resume multiple times
        scanner.resume();
        scanner.resume();
        scanner.resume();
        // Then - should not be paused (no exception)
        assertThat(scanner.isPaused(), is(false));
    }
}

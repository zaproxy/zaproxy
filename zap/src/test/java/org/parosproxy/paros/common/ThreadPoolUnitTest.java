/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.parosproxy.paros.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class ThreadPoolUnitTest {

    @Test
    void shouldInterruptRunningThreads() throws Exception {
        // Given
        ThreadPool pool = new ThreadPool(1);
        CountDownLatch started = new CountDownLatch(1);
        AtomicBoolean wasInterrupted = new AtomicBoolean();
        Thread t =
                pool.getFreeThreadAndRun(
                        () -> {
                            started.countDown();
                            try {
                                Thread.sleep(Long.MAX_VALUE);
                            } catch (InterruptedException e) {
                                wasInterrupted.set(true);
                            }
                        });
        started.await();
        // When
        pool.interrupt();
        t.join(2000);
        // Then
        assertThat(wasInterrupted.get(), is(true));
    }

    @Test
    void shouldNotReturnThreadAfterInterrupt() {
        // Given
        ThreadPool pool = new ThreadPool(2);
        pool.interrupt();
        // When
        Thread t = pool.getFreeThreadAndRun(() -> {});
        // Then
        assertThat(t, is(nullValue()));
    }

    @Test
    void shouldRestoreInterruptedStateAfterWaitAllThreadComplete() throws Exception {
        // Given
        ThreadPool pool = new ThreadPool(1);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        pool.getFreeThreadAndRun(
                () -> {
                    started.countDown();
                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
        started.await();
        Thread.currentThread().interrupt();
        // When
        try {
            pool.waitAllThreadComplete(5000);
            // Then
            assertThat(Thread.currentThread().isInterrupted(), is(true));
        } finally {
            Thread.interrupted();
            release.countDown();
        }
    }
}

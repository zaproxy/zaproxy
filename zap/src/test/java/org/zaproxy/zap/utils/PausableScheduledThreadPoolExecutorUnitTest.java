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
package org.zaproxy.zap.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test for {@link PausableScheduledThreadPoolExecutor}. */
class PausableScheduledThreadPoolExecutorUnitTest {

    private static final int BULK_TASK_COUNT = 5;
    private static final long DELAY_MS = 300;

    private RecordingExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new RecordingExecutor(1);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @ParameterizedTest
    @MethodSource("taskDispatchMethods")
    void shouldExecuteTasksDelayedBySpecifiedAmount(ExecutorDispathMethod method) throws Exception {
        executor.setDefaultDelay(DELAY_MS, TimeUnit.MILLISECONDS);

        executor.freezeCompletion();
        executor.expectAfterExecutes(BULK_TASK_COUNT);

        dispatch(method, executor, BULK_TASK_COUNT);

        // Assert the computed delay values (deterministic, no wall-clock timing).
        assertThat(
                executor.drainRequestedDelaysMs(),
                is(Collections.nCopies(BULK_TASK_COUNT, DELAY_MS)));

        executor.unfreezeCompletion();
        executor.awaitAfterExecutes();
    }

    @ParameterizedTest
    @MethodSource("taskDispatchMethods")
    void shouldExecuteTasksIncrementallyDelayedBySpecifiedAmount(ExecutorDispathMethod method)
            throws Exception {
        executor.setDefaultDelay(DELAY_MS, TimeUnit.MILLISECONDS);
        executor.setIncrementalDefaultDelay(true);

        executor.freezeCompletion();
        executor.expectAfterExecutes(BULK_TASK_COUNT);

        dispatch(method, executor, BULK_TASK_COUNT);

        // While completion is frozen, queuedTaskCount cannot decrement, so delays are stable:
        assertThat(executor.drainRequestedDelaysMs(), is(expectedIncrementalDelays()));

        executor.unfreezeCompletion();
        executor.awaitAfterExecutes();
    }

    @ParameterizedTest
    @MethodSource("taskDispatchMethods")
    void shouldExecutePeriodicallySubmittedTasksIncrementallyDelayedBySpecifiedAmount(
            ExecutorDispathMethod method) throws Exception {
        executor.setDefaultDelay(DELAY_MS, TimeUnit.MILLISECONDS);
        executor.setIncrementalDefaultDelay(true);

        // Batch 1
        executor.freezeCompletion();
        executor.expectAfterExecutes(BULK_TASK_COUNT);

        dispatch(method, executor, BULK_TASK_COUNT);

        assertThat(executor.drainRequestedDelaysMs(), is(expectedIncrementalDelays()));

        executor.unfreezeCompletion();
        executor.awaitAfterExecutes();

        // Batch 2: verifies queuedTaskCount has been decremented back down by afterExecute()
        executor.freezeCompletion();
        executor.expectAfterExecutes(BULK_TASK_COUNT);

        dispatch(method, executor, BULK_TASK_COUNT);

        assertThat(executor.drainRequestedDelaysMs(), is(expectedIncrementalDelays()));

        executor.unfreezeCompletion();
        executor.awaitAfterExecutes();
    }

    @Test
    void shouldNotRunTaskWhilePausedAndRunAfterResume() throws Exception {
        CountDownLatch enteredBeforeExecute = new CountDownLatch(1);
        CountDownLatch ranTask = new CountDownLatch(1);

        PausableScheduledThreadPoolExecutor pausable =
                new PausableScheduledThreadPoolExecutor(1) {
                    @Override
                    protected void beforeExecute(Thread t, Runnable r) {
                        enteredBeforeExecute.countDown();
                        super.beforeExecute(t, r);
                    }
                };

        try {
            pausable.pause();
            pausable.execute(ranTask::countDown);

            // Ensure the worker thread reached the pause gate (beforeExecute).
            assertThat(enteredBeforeExecute.await(5, TimeUnit.SECONDS), is(true));

            // While paused, the task must not have run.
            assertThat(ranTask.getCount(), is(1L));

            pausable.resume();

            // After resume, task should run.
            assertThat(ranTask.await(5, TimeUnit.SECONDS), is(true));
        } finally {
            pausable.shutdownNow();
        }
    }

    @FunctionalInterface
    private static interface ExecutorDispathMethod {
        void dispatch(PausableScheduledThreadPoolExecutor executor, Runnable task);
    }

    static Stream<ExecutorDispathMethod> taskDispatchMethods() {
        return Stream.of(
                (executor, task) -> executor.execute(task),
                (executor, task) -> executor.submit(task),
                (executor, task) -> executor.submit(task, true),
                (executor, task) ->
                        executor.submit(
                                (Callable<Void>)
                                        () -> {
                                            task.run();
                                            return null;
                                        }));
    }

    private static void dispatch(
            ExecutorDispathMethod method, PausableScheduledThreadPoolExecutor executor, int count) {
        for (int i = 0; i < count; i++) {
            method.dispatch(executor, () -> {});
        }
    }

    private static List<Long> expectedIncrementalDelays() {
        List<Long> expected = new ArrayList<>(BULK_TASK_COUNT);
        for (int i = 1; i <= BULK_TASK_COUNT; i++) {
            expected.add(DELAY_MS * i);
        }
        return expected;
    }

    /**
     * Test-only executor that:
     *
     * <ul>
     *   <li>Records the computed delay passed to schedule(...)
     *   <li>Runs scheduled work with delay 0 for speed
     *   <li>Can freeze completion to prevent afterExecute() from decrementing the queued-task counter
     *       while tasks are still being submitted (keeps incremental-delay tests deterministic)
     * </ul>
     */
    private static class RecordingExecutor extends PausableScheduledThreadPoolExecutor {

        private final List<Long> requestedDelaysMs =
                Collections.synchronizedList(new ArrayList<>());

        private volatile CountDownLatch completionGate = new CountDownLatch(0);
        private volatile CountDownLatch afterExecuteLatch = new CountDownLatch(0);

        RecordingExecutor(int corePoolSize) {
            super(corePoolSize);
        }

        void freezeCompletion() {
            completionGate = new CountDownLatch(1);
        }

        void unfreezeCompletion() {
            completionGate.countDown();
        }

        void expectAfterExecutes(int count) {
            afterExecuteLatch = new CountDownLatch(count);
        }

        void awaitAfterExecutes() throws InterruptedException {
            if (!afterExecuteLatch.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting for tasks to complete.");
            }
        }

        List<Long> drainRequestedDelaysMs() {
            synchronized (requestedDelaysMs) {
                List<Long> copy = new ArrayList<>(requestedDelaysMs);
                requestedDelaysMs.clear();
                return copy;
            }
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            requestedDelaysMs.add(TimeUnit.MILLISECONDS.convert(delay, unit));

            Runnable wrapped =
                    () -> {
                        try {
                            completionGate.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        command.run();
                    };

            // Execute immediately to keep tests fast; delay correctness is asserted via recording.
            return super.schedule(wrapped, 0, TimeUnit.MILLISECONDS);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            requestedDelaysMs.add(TimeUnit.MILLISECONDS.convert(delay, unit));

            Callable<V> wrapped =
                    () -> {
                        try {
                            completionGate.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw e;
                        }
                        return callable.call();
                    };

            // Execute immediately to keep tests fast; delay correctness is asserted via recording.
            return super.schedule(wrapped, 0, TimeUnit.MILLISECONDS);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t); // preserves queuedTaskCount decrement logic
            afterExecuteLatch.countDown();
        }
    }
}


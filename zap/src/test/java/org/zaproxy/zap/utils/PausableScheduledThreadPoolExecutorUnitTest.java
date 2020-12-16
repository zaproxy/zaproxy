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

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test for {@link PausableScheduledThreadPoolExecutor}. */
class PausableScheduledThreadPoolExecutorUnitTest {

    private static final int BULK_TASK_COUNT = 5;
    private static final long DELAY_MS = 300;

    private PausableScheduledThreadPoolExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new PausableScheduledThreadPoolExecutor(1);
    }

    @ParameterizedTest
    @MethodSource("taskDispatchMethods")
    void shouldExecuteTasksDelayedBySpecifiedAmount(ExecutorDispathMethod method) {
        // Given
        executor.setDefaultDelay(DELAY_MS, TimeUnit.MILLISECONDS);
        List<TestTask> tasks = createTasks(BULK_TASK_COUNT);
        // When
        dispatch(method, executor, tasks);
        // Then
        tasks.forEach(task -> assertTaskExecutedAfter(task, DELAY_MS));
    }

    @ParameterizedTest
    @MethodSource("taskDispatchMethods")
    void shouldExecuteTasksIncrementallyDelayedBySpecifiedAmount(ExecutorDispathMethod method) {
        // Given
        executor.setDefaultDelay(DELAY_MS, TimeUnit.MILLISECONDS);
        executor.setIncrementalDefaultDelay(true);
        List<TestTask> tasks = createTasks(BULK_TASK_COUNT);
        // When
        dispatch(method, executor, tasks);
        // Then
        for (int i = 0; i < tasks.size(); i++) {
            assertTaskExecutedAfter(tasks.get(i), DELAY_MS * (i + 1));
        }
    }

    @ParameterizedTest
    @MethodSource("taskDispatchMethods")
    void shouldExecutePeriodicallySubmittedTasksIncrementallyDelayedBySpecifiedAmount(
            ExecutorDispathMethod method) {
        // Given
        executor.setDefaultDelay(DELAY_MS, TimeUnit.MILLISECONDS);
        executor.setIncrementalDefaultDelay(true);
        List<TestTask> tasks = createTasks(BULK_TASK_COUNT);
        // When
        dispatch(method, executor, tasks);
        waitForTasksExecuted(tasks);
        tasks.forEach(TestTask::reset);
        dispatch(method, executor, tasks);
        // Then
        for (int i = 0; i < tasks.size(); i++) {
            assertTaskExecutedAfter(tasks.get(i), DELAY_MS * (i + 1));
        }
    }

    private static interface ExecutorDispathMethod {
        void dispatch(PausableScheduledThreadPoolExecutor executor, TestTask task);
    }

    static Stream<ExecutorDispathMethod> taskDispatchMethods() {
        return Stream.of(
                (executor, task) -> {
                    task.dispatched();
                    executor.execute(task);
                },
                (executor, task) -> {
                    task.dispatched();
                    executor.submit(task);
                },
                (executor, task) -> {
                    task.dispatched();
                    executor.submit(task, true);
                },
                (executor, task) -> {
                    task.dispatched();
                    executor.submit(
                            () -> {
                                task.run();
                                return null;
                            });
                });
    }

    private static void dispatch(
            ExecutorDispathMethod method,
            PausableScheduledThreadPoolExecutor executor,
            List<TestTask> tasks) {
        tasks.forEach(task -> method.dispatch(executor, task));
    }

    private static void assertTaskExecuted(TestTask task) {
        assertTaskExcutedState(task, true);
    }

    private static void assertTaskExcutedState(TestTask task, boolean state) {
        int ellapsed = 0;
        long max = getDelayWithMargin(DELAY_MS);
        while (ellapsed <= max) {
            if (task.isExecuted()) {
                return;
            }

            try {
                ellapsed += 10;
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for the task.", e);
            }
        }
        assertThat(task.isExecuted(), is(equalTo(state)));
    }

    private static void assertTaskExecutedAfter(TestTask task, long delay) {
        assertTaskExecuted(task);
        assertThat(
                task.getEllapsedTime(),
                is(both(greaterThanOrEqualTo(delay)).and(lessThan(getDelayWithMargin(delay)))));
    }

    private static long getDelayWithMargin(long delay) {
        return delay + delay / 3;
    }

    private static void waitForTasksExecuted(List<TestTask> tasks) {
        tasks.forEach(PausableScheduledThreadPoolExecutorUnitTest::assertTaskExecuted);
    }

    private static class TestTask implements Runnable {

        private AtomicBoolean executed;
        private StopWatch watch;

        TestTask() {
            reset();
        }

        void reset() {
            executed = new AtomicBoolean();
            watch = new StopWatch();
        }

        void dispatched() {
            watch.start();
        }

        boolean isExecuted() {
            return executed.get();
        }

        long getEllapsedTime() {
            return watch.getTime();
        }

        @Override
        public void run() {
            watch.stop();
            executed.set(true);
        }
    }

    private static List<TestTask> createTasks(int count) {
        List<TestTask> tasks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            tasks.add(new TestTask());
        }
        return tasks;
    }
}

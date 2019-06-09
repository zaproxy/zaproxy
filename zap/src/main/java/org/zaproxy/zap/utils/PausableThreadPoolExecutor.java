/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@code ThreadPoolExecutor} that implements {@code PausableExecutorService}.
 *
 * @since 2.4.0
 * @see PausableExecutorService
 * @see ThreadPoolExecutor
 */
public class PausableThreadPoolExecutor extends ThreadPoolExecutor
        implements PausableExecutorService {

    private final ReentrantLock pauseLock = new ReentrantLock();
    private final Condition unpaused = pauseLock.newCondition();

    private boolean paused;

    private List<ExecutorTerminatedListener> listeners = new ArrayList<>(1);

    // NOTE: Constructors JavaDoc was copied from base class but with the name of the class replaced
    // with this one.

    /**
     * Creates a new {@code PausableThreadPoolExecutor} with the given initial parameters and
     * default thread factory and rejected execution handler. It may be more convenient to use one
     * of the {@link Executors} factory methods instead of this general purpose constructor.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
     *     {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
     *     time that excess idle threads will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are executed. This queue will
     *     hold only the {@code Runnable} tasks submitted by the {@code execute} method.
     * @throws IllegalArgumentException if one of the following holds:<br>
     *     {@code corePoolSize < 0}<br>
     *     {@code keepAliveTime < 0}<br>
     *     {@code maximumPoolSize <= 0}<br>
     *     {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue} is null
     */
    public PausableThreadPoolExecutor(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue) {
        super(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                Executors.defaultThreadFactory());
    }

    /**
     * Creates a new {@code PausableThreadPoolExecutor} with the given initial parameters and
     * default rejected execution handler.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
     *     {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
     *     time that excess idle threads will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are executed. This queue will
     *     hold only the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param threadFactory the factory to use when the executor creates a new thread
     * @throws IllegalArgumentException if one of the following holds:<br>
     *     {@code corePoolSize < 0}<br>
     *     {@code keepAliveTime < 0}<br>
     *     {@code maximumPoolSize <= 0}<br>
     *     {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue} or {@code threadFactory} is null
     */
    public PausableThreadPoolExecutor(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * Creates a new {@code PausableThreadPoolExecutor} with the given initial parameters and
     * default thread factory.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
     *     {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
     *     time that excess idle threads will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are executed. This queue will
     *     hold only the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param handler the handler to use when execution is blocked because the thread bounds and
     *     queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *     {@code corePoolSize < 0}<br>
     *     {@code keepAliveTime < 0}<br>
     *     {@code maximumPoolSize <= 0}<br>
     *     {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue} or {@code handler} is null
     */
    public PausableThreadPoolExecutor(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            RejectedExecutionHandler handler) {
        super(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                Executors.defaultThreadFactory(),
                handler);
    }

    /**
     * Creates a new {@code PausableThreadPoolExecutor} with the given initial parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
     *     {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
     *     time that excess idle threads will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are executed. This queue will
     *     hold only the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param threadFactory the factory to use when the executor creates a new thread
     * @param handler the handler to use when execution is blocked because the thread bounds and
     *     queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *     {@code corePoolSize < 0}<br>
     *     {@code keepAliveTime < 0}<br>
     *     {@code maximumPoolSize <= 0}<br>
     *     {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue} or {@code threadFactory} or {@code handler}
     *     is null
     */
    public PausableThreadPoolExecutor(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory,
            RejectedExecutionHandler handler) {
        super(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                handler);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        pauseLock.lock();
        try {
            while (paused) {
                unpaused.await();
            }
        } catch (InterruptedException ie) {
            t.interrupt();
        } finally {
            pauseLock.unlock();
        }
    }

    @Override
    public void pause() {
        pauseLock.lock();
        try {
            paused = true;
        } finally {
            pauseLock.unlock();
        }
    }

    @Override
    public void resume() {
        pauseLock.lock();
        try {
            if (!paused) {
                return;
            }
            paused = false;
            unpaused.signalAll();
        } finally {
            pauseLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Overridden to {@code resume()} before {@code shutdown()}.
     *
     * @see #resume()
     */
    @Override
    public void shutdown() {
        resume();
        super.shutdown();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Overridden to {@code resume()} before {@code shutdownNow()}.
     *
     * @see #resume()
     */
    @Override
    public List<Runnable> shutdownNow() {
        resume();
        return super.shutdownNow();
    }

    @Override
    protected void terminated() {
        super.terminated();

        // Notify with a copy to allow listeners to remove themselves from listening when notified.
        notifyListeners(new ArrayList<>(listeners));
    }

    /**
     * Notifies the given listeners that the executor has terminated.
     *
     * @param listeners the listeners that will be notified
     */
    private static void notifyListeners(List<ExecutorTerminatedListener> listeners) {
        for (ExecutorTerminatedListener listener : listeners) {
            listener.terminated();
        }
    }

    @Override
    public void addExecutorTerminatedListener(ExecutorTerminatedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeExecutorTerminatedListener(ExecutorTerminatedListener listener) {
        listeners.remove(listener);
    }
}

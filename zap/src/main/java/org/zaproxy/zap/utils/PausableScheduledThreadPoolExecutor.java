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
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@code ScheduledThreadPoolExecutor} that implements {@code PausableExecutorService}.
 *
 * <p>It's also possible to set a default delay, applied when tasks are submitted and executed.
 *
 * @since 2.4.0
 * @see PausableExecutorService
 * @see ScheduledThreadPoolExecutor
 * @see #setDefaultDelay(long, TimeUnit)
 */
public class PausableScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor
        implements PausableExecutorService {

    private final ReentrantLock pauseLock = new ReentrantLock();
    private final Condition unpaused = pauseLock.newCondition();

    private boolean paused;

    private List<ExecutorTerminatedListener> listeners = new ArrayList<>(1);

    private long defaultDelayInMs;

    private boolean incrementalDefaultDelay;
    private AtomicInteger queuedTaskCount;

    // NOTE: Constructors JavaDoc was copied from base class but with the name of the class replaced
    // with this one.

    /**
     * Creates a new {@code PausableScheduledThreadPoolExecutor} with the given core pool size.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
     *     {@code allowCoreThreadTimeOut} is set
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public PausableScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    /**
     * Creates a new {@code PausableScheduledThreadPoolExecutor} with the given initial parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
     *     {@code allowCoreThreadTimeOut} is set
     * @param threadFactory the factory to use when the executor creates a new thread
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException if {@code threadFactory} is null
     */
    public PausableScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    /**
     * Creates a new {@code PausableScheduledThreadPoolExecutor} with the given initial parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
     *     {@code allowCoreThreadTimeOut} is set
     * @param handler the handler to use when execution is blocked because the thread bounds and
     *     queue capacities are reached
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException if {@code handler} is null
     */
    public PausableScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    /**
     * Creates a new {@code PausableScheduledThreadPoolExecutor} with the given initial parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
     *     {@code allowCoreThreadTimeOut} is set
     * @param threadFactory the factory to use when the executor creates a new thread
     * @param handler the handler to use when execution is blocked because the thread bounds and
     *     queue capacities are reached
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException if {@code threadFactory} or {@code handler} is null
     */
    public PausableScheduledThreadPoolExecutor(
            int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    /**
     * Sets the default required delay when executing/submitting tasks.
     *
     * <p>Default value is zero, no required delay.
     *
     * @param delay the value delay
     * @param unit the time unit of delay
     * @throws IllegalArgumentException if {@code defaultDelayInMs} is negative.
     * @see #execute(Runnable)
     * @see #submit(Callable)
     * @see #submit(Runnable)
     * @see #submit(Runnable, Object)
     * @see #setIncrementalDefaultDelay(boolean)
     */
    public void setDefaultDelay(long delay, TimeUnit unit) {
        if (delay < 0) {
            throw new IllegalArgumentException("Parameter delay must be greater or equal to zero.");
        }
        if (unit == null) {
            throw new IllegalArgumentException("Parameter unit must not be null.");
        }
        this.defaultDelayInMs = unit.toMillis(delay);
    }

    /**
     * Gets the default delay in the given time unit.
     *
     * @param unit the unit that the delay will be returned
     * @return the default delay in the given unit.
     */
    public long getDefaultDelay(TimeUnit unit) {
        return unit.convert(defaultDelayInMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Sets whether or not the default delay should be incremental, that is, increased
     * proportionally to each task executed.
     *
     * <p>For example, with default delay of 500 milliseconds and incremental delay set to {@code
     * true} it will result in the following delays for each task:
     *
     * <ol>
     *   <li>500 milliseconds
     *   <li>1 second
     *   <li>1.5 seconds
     *   <li>2 seconds
     * </ol>
     *
     * Effectively making the tasks execute periodically around the default delay.
     *
     * <p>Default value is {@code false}.
     *
     * @param incremental {@code true} if the default delay should be incremental, {@code false}
     *     otherwise.
     * @see #isIncrementalDefaultDelay()
     * @see #setDefaultDelay(long, TimeUnit)
     */
    public void setIncrementalDefaultDelay(boolean incremental) {
        if (incrementalDefaultDelay == incremental) {
            return;
        }

        incrementalDefaultDelay = incremental;
        if (incrementalDefaultDelay) {
            queuedTaskCount = new AtomicInteger();
        } else {
            queuedTaskCount = null;
        }
    }

    /**
     * Tells whether or not the default delay is incremental.
     *
     * @return {@code true} if the default delay is incremental, {@code false} otherwise.
     * @see #setIncrementalDefaultDelay(boolean)
     */
    public boolean isIncrementalDefaultDelay() {
        return incrementalDefaultDelay;
    }

    /**
     * Resets the incremental default delay.
     *
     * <p>The call to this method has no effect it the incremental default delay is not enabled.
     *
     * @see #setIncrementalDefaultDelay(boolean)
     */
    public void resetIncrementalDefaultDelay() {
        if (incrementalDefaultDelay) {
            queuedTaskCount = new AtomicInteger();
        }
    }

    /**
     * If no default delay was specified the {@code command} is executed with zero required delay.
     * Otherwise the default delay is applied.
     *
     * @throws RejectedExecutionException at discretion of {@code RejectedExecutionHandler}, if the
     *     task cannot be accepted for execution because the executor has been shut down
     * @throws NullPointerException {@inheritDoc}
     * @see #setIncrementalDefaultDelay(boolean)
     * @see #schedule(Runnable, long, TimeUnit)
     * @see #setDefaultDelay(long, TimeUnit)
     */
    @Override
    public void execute(Runnable command) {
        schedule(command, getDefaultDelayForTask(), TimeUnit.MILLISECONDS);
    }

    private long getDefaultDelayForTask() {
        if (incrementalDefaultDelay) {
            return queuedTaskCount.incrementAndGet() * defaultDelayInMs;
        }
        return defaultDelayInMs;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Overridden to schedule with default delay, when non zero.
     *
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @see #setIncrementalDefaultDelay(boolean)
     * @see #schedule(Runnable, long, TimeUnit)
     * @see #setDefaultDelay(long, TimeUnit)
     */
    @Override
    public Future<?> submit(Runnable task) {
        return schedule(task, getDefaultDelayForTask(), TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Overridden to schedule with default delay, when non zero.
     *
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @see #setIncrementalDefaultDelay(boolean)
     * @see #schedule(Runnable, long, TimeUnit)
     * @see #setDefaultDelay(long, TimeUnit)
     */
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return schedule(
                Executors.callable(task, result), getDefaultDelayForTask(), TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Overridden to schedule with default delay, when non zero.
     *
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @see #setIncrementalDefaultDelay(boolean)
     * @see #schedule(Callable, long, TimeUnit)
     * @see #setDefaultDelay(long, TimeUnit)
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return schedule(task, getDefaultDelayForTask(), TimeUnit.MILLISECONDS);
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
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if (incrementalDefaultDelay) {
            queuedTaskCount.decrementAndGet();
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

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

import java.util.concurrent.ExecutorService;

/**
 * A {@code ExecutorService} that allows to pause and resume. Moreover it allows to listen for
 * termination of the executor.
 *
 * @since 2.4.0
 * @see ExecutorTerminatedListener
 * @see #pause()
 * @see #addExecutorTerminatedListener(ExecutorTerminatedListener)
 */
public interface PausableExecutorService extends ExecutorService {

    /**
     * Pauses the executor, so that no new task will be executed until {@code resume()} is called.
     *
     * @see #resume()
     */
    void pause();

    /**
     * Resumes the executor, so that the awaiting tasks are executed.
     *
     * <p>The call to this method has no effect if the executor is not paused.
     *
     * @see #pause()
     */
    void resume();

    /**
     * Adds the given {@code listener} to the list of listeners that will be notified when the
     * executor terminates.
     *
     * @param listener the listener for termination
     * @see #isTerminated()
     * @see #removeExecutorTerminatedListener(ExecutorTerminatedListener)
     */
    void addExecutorTerminatedListener(ExecutorTerminatedListener listener);

    /**
     * Removes the given {@code listener} from the list of listeners that are notified when the
     * executor terminates.
     *
     * @param listener the listener for termination
     * @see #isTerminated()
     * @see #addExecutorTerminatedListener(ExecutorTerminatedListener)
     */
    void removeExecutorTerminatedListener(ExecutorTerminatedListener listener);
}

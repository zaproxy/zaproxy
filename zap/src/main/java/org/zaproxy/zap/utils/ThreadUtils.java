/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.view.View;

/**
 * Utility class for event dispatch thread (EDT) related tasks.
 *
 * @since 2.4.0
 */
public class ThreadUtils {

    private static final Logger LOGGER = LogManager.getLogger(ThreadUtils.class);

    /**
     * Runs a {@code Runnable}, synchronously, on the event dispatch thread (EDT).
     *
     * <p>Behaves exactly like {@link EventQueue#invokeAndWait(Runnable)}, but can be called from
     * that thread as well.
     *
     * <p>If the {@link View} is not initialised the runnable is executed in the current thread.
     *
     * @param runnable the {@code Runnable} to be run in the EDT.
     * @throws InterruptedException if the current thread was interrupted while waiting for the EDT.
     * @throws InvocationTargetException if an exception occurred while running the {@code Runnable}
     *     in the EDT.
     */
    public static void invokeAndWait(Runnable runnable)
            throws InvocationTargetException, InterruptedException {
        if (!View.isInitialised() || EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            EventQueue.invokeAndWait(runnable);
        }
    }

    /**
     * Convenience method that handles the exceptions thrown by {@link #invokeAndWait(Runnable)}.
     *
     * <p>The {@code InvocationTargetException} is logged as an error and the {@code
     * InterruptedException} as warning.
     *
     * @param runnable the {@code Runnable} to be run in the EDT.
     * @since 2.9.0
     */
    public static void invokeAndWaitHandled(Runnable runnable) {
        try {
            invokeAndWait(runnable);
        } catch (InvocationTargetException e) {
            LOGGER.error("Error while executing in EDT:", e);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for EDT.", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Runs a {@code Runnable}, asynchronously, on the event dispatch thread (EDT).
     *
     * <p>Behaves exactly like {@link EventQueue#invokeLater(Runnable)}, but can be called from that
     * thread as well.
     *
     * <p>If the {@link View} is not initialised the runnable is executed in the current thread.
     *
     * @param runnable the {@code Runnable} to be run in the EDT.
     * @since 2.12.0
     */
    public static void invokeLater(Runnable runnable) {
        if (!View.isInitialised() || EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            EventQueue.invokeLater(runnable);
        }
    }
}

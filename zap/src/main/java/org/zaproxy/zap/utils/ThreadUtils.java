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

public class ThreadUtils {

    /**
     * Utility method used to run, synchronously, a Runnable on the main thread. Behaves exactly
     * like {@link EventQueue#invokeAndWait(Runnable)}, but can be called from the main thread as
     * well.
     *
     * @param runnable the {@code Runnable} to be run in the EDT
     * @throws InterruptedException if the current thread was interrupted while waiting for the EDT
     * @throws InvocationTargetException if an exception occurred while running the {@code Runnable}
     */
    public static void invokeAndWait(Runnable runnable)
            throws InvocationTargetException, InterruptedException {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            EventQueue.invokeAndWait(runnable);
        }
    }
}

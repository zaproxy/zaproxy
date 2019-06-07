/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2014/04/01 Changed to allow to set a name to created threads.
// ZAP: 2016/09/22 JavaDoc tweaks
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
package org.parosproxy.paros.common;

public class ThreadPool {

    private Thread[] pool = null;
    private final String threadsBaseName;

    public ThreadPool(int maxThreadCount) {
        this(maxThreadCount, null);
    }

    public ThreadPool(int maxThreadCount, String threadsBaseName) {
        pool = new Thread[maxThreadCount];
        this.threadsBaseName = threadsBaseName;
    }

    /**
     * Gets a free thread from the thread pool. If there is no free thread, returns {@code null}.
     *
     * @param runnable the {@code Runnable} to be run in the thread
     * @return the {@code Thread}, already started, with the given {@code Runnable}, or {@code null}
     *     if none available.
     */
    public synchronized Thread getFreeThreadAndRun(Runnable runnable) {

        for (int i = 0; i < pool.length; i++) {
            if (pool[i] == null || !pool[i].isAlive()) {
                pool[i] = new Thread(runnable);
                if (threadsBaseName != null) {
                    pool[i].setName(threadsBaseName + i);
                }
                pool[i].setDaemon(true);
                pool[i].start();
                return pool[i];
            }
        }
        return null;
    }

    /**
     * Waits until all threads finish its tasks (at most some time for each).
     *
     * <p>If not completed yet, do not wait. Each thread should kill itself.
     *
     * @param waitInMillis the number of milliseconds to wait for the threads
     */
    public void waitAllThreadComplete(int waitInMillis) {
        for (int i = 0; i < pool.length; i++) {
            if (pool[i] != null && pool[i].isAlive()) {
                try {
                    pool[i].join(waitInMillis);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public boolean isAllThreadComplete() {
        for (int i = 0; i < pool.length; i++) {
            if (pool[i] != null && pool[i].isAlive()) {
                return false;
            }
        }
        return true;
    }
}

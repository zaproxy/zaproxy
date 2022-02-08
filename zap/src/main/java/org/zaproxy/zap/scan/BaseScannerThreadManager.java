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
package org.zaproxy.zap.scan;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.zaproxy.zap.model.Context;

/**
 * An implementation of a {@link ScannerThreadManager} for threads used to scan {@link Context
 * Contexts}.
 *
 * @param <ScannerThread> the type of the scanner threads managed
 */
public abstract class BaseScannerThreadManager<ScannerThread extends BaseScannerThread<?>>
        implements ScannerThreadManager<ScannerThread, Integer> {

    private Map<Integer, ScannerThread> threadsMap;

    /** Instantiates a new base scanner thread manager. */
    public BaseScannerThreadManager() {
        this.threadsMap = new HashMap<>();
    }

    @Override
    public ScannerThread getScannerThread(Integer contextId) {
        ScannerThread thread = threadsMap.get(contextId);
        if (thread == null) {
            thread = createNewScannerThread(contextId);
            threadsMap.put(contextId, thread);
        }
        return thread;
    }

    @Override
    public ScannerThread recreateScannerThreadIfHasRun(Integer contextId) {
        ScannerThread thread = threadsMap.get(contextId);
        if (thread.hasRun()) {
            thread = createNewScannerThread(contextId);
            threadsMap.put(contextId, thread);
        }
        return thread;
    }

    @Override
    public Collection<ScannerThread> getAllThreads() {
        return threadsMap.values();
    }

    @Override
    public void clearThreads() {
        threadsMap.clear();
    }

    @Override
    public void stopAllScannerThreads() {
        for (ScannerThread scanner : getAllThreads()) {
            scanner.stopScan();
        }
        // TODO: Needs to be handled properly
        // Allow 2 secs for the threads to stop - if we wait 'for ever' then we can get deadlocks
        // for (int i = 0; i < 20; i++) {
        // if (activeScans.isEmpty()) {
        // break;
        // }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }
        // }
        clearThreads();
    }

    /**
     * Creates a new scanner thread for a given context.
     *
     * @param contextId the context id
     * @return the scanner thread
     */
    public abstract ScannerThread createNewScannerThread(int contextId);
}

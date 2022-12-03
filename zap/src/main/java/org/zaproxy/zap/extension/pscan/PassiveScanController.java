/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.zaproxy.zap.extension.pscan;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.utils.Stats;
import org.zaproxy.zap.view.ScanStatus;

/**
 * Controls the passive scanning.
 *
 * @since 2.12.0
 */
public class PassiveScanController extends Thread implements ProxyListener {

    private static final Logger logger = LogManager.getLogger(PassiveScanController.class);

    private ExtensionHistory extHist;
    private PassiveScanParam pscanOptions;
    private PassiveScanTaskHelper helper;
    private Session session;
    private ScanStatus scanStatus;

    private ThreadPoolExecutor executor;

    private int currentId = 1;
    private int lastId = -1;
    private int mainSleep = 2000;
    private int postSleep = 200;
    private volatile boolean shutDown = false;

    public PassiveScanController(
            ExtensionPassiveScan extPscan,
            ExtensionHistory extHistory,
            ExtensionAlert extAlert,
            PassiveScanParam passiveScanParam,
            ScanStatus scanStatus) {
        setName("ZAP-PassiveScanController");
        this.extHist = extHistory;
        this.pscanOptions = passiveScanParam;
        this.scanStatus = scanStatus;

        helper = new PassiveScanTaskHelper(extPscan, extAlert, passiveScanParam);

        // Get the last id - in case we've just opened an existing session
        currentId = this.getLastHistoryId();
        lastId = currentId;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void run() {
        logger.debug("Starting passive scan monitoring");
        try {
            scan();
        } finally {
            logger.debug("Stopping passive scan monitoring");
        }
    }

    private void scan() {
        // Get the last id - in case we've just opened an existing session
        currentId = this.getLastHistoryId();
        lastId = currentId;
        HistoryReference href = null;

        while (!shutDown) {
            try {
                if (href != null || lastId > currentId) {
                    currentId++;
                } else {
                    // Either just started or there are no new records
                    try {
                        Thread.sleep(mainSleep);
                        if (shutDown) {
                            return;
                        }
                        lastId = this.getLastHistoryId();
                    } catch (InterruptedException e) {
                        // New URL, but give it a chance to be processed first
                        try {
                            Thread.sleep(postSleep);
                        } catch (InterruptedException e2) {
                            // Ignore
                        }
                    }
                }
                href = getHistoryReference(currentId);

                if (shutDown) {
                    return;
                }

                if (href != null
                        && (!pscanOptions.isScanOnlyInScope() || session.isInScope(href))) {
                    logger.debug(
                            "Submitting request to executor: {} id {} type {}",
                            href.getURI(),
                            currentId,
                            href.getHistoryType());
                    getExecutor().submit(new PassiveScanTask(href, helper));
                }
                int recordsToScan = this.getRecordsToScan();
                Stats.setHighwaterMark("stats.pscan.recordsToScan", recordsToScan);
                if (scanStatus != null) {
                    scanStatus.setScanCount(recordsToScan);
                }

            } catch (Exception e) {
                if (shutDown) {
                    return;
                }
                if (href != null
                        && HistoryReference.getTemporaryTypes().contains(href.getHistoryType())) {
                    logger.debug("Temporary record {} no longer available:", currentId, e);
                } else {
                    logger.error("Failed on record {} from History table", currentId, e);
                }
            }
        }
    }

    private ThreadPoolExecutor getExecutor() {
        if (this.executor == null || this.executor.isShutdown()) {
            int threads = pscanOptions.getPassiveScanThreads();
            logger.debug("Creating new executor with {} threads", threads);

            this.executor =
                    (ThreadPoolExecutor)
                            Executors.newFixedThreadPool(
                                    threads, new PassiveScanThreadFactory("ZAP-PassiveScan-"));
        }
        return this.executor;
    }

    private HistoryReference getHistoryReference(final int historyReferenceId) {
        if (extHist != null) {
            return extHist.getHistoryReference(historyReferenceId);
        }

        try {
            return new HistoryReference(historyReferenceId);
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            return null;
        }
    }

    private int getLastHistoryId() {
        return this.extHist.getLastHistoryId();
    }

    protected int getRecordsToScan() {
        return this.getLastHistoryId() - getLastScannedId() + helper.getRunningTasks().size();
    }

    private int getLastScannedId() {
        if (currentId > lastId) {
            return currentId - 1;
        }
        return currentId;
    }

    protected void shutdown() {
        logger.debug("Shutdown");
        this.shutDown = true;
        if (this.executor != null) {
            this.executor.shutdown();
        }
        this.helper.shutdownTasks();
    }

    public List<PassiveScanTask> getRunningTasks() {
        return this.helper.getRunningTasks();
    }

    public PassiveScanTask getOldestRunningTask() {
        return this.helper.getOldestRunningTask();
    }

    /**
     * Empties the passive scan queue without passively scanning the messages. Currently running
     * rules will run to completion but new rules will only be run when new messages are added to
     * the queue.
     *
     * @since 2.12.0
     */
    public void clearQueue() {
        currentId = this.getLastHistoryId();
        lastId = currentId;
        this.helper.shutdownTasks();
    }

    @Override
    public int getArrangeableListenerOrder() {
        // Not actually used as we never register this listener, its always called from the
        // extension
        return ExtensionPassiveScan.PROXY_LISTENER_ORDER;
    }

    @Override
    public boolean onHttpRequestSend(HttpMessage msg) {
        // Ignore
        return true;
    }

    @Override
    public boolean onHttpResponseReceive(HttpMessage msg) {
        // Wakey wakey
        this.interrupt();
        return true;
    }

    private static class PassiveScanThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber;
        private final String namePrefix;
        private final ThreadGroup group;

        public PassiveScanThreadFactory(String namePrefix) {
            threadNumber = new AtomicInteger(1);
            this.namePrefix = namePrefix;
            group = Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY - 1) {
                t.setPriority(Thread.NORM_PRIORITY - 1);
            }
            return t;
        }
    }
}

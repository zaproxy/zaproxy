/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.DefaultListModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.ScannerListener;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.ruleconfig.RuleConfigParam;
import org.zaproxy.zap.model.GenericScanner2;
import org.zaproxy.zap.model.Target;

public class ActiveScan extends org.parosproxy.paros.core.scanner.Scanner
        implements GenericScanner2, ScannerListener {

    /** The maximum number of statistic history records cached */
    private static final int MAX_STATS_HISTORY_SIZE = 240;

    public static enum State {
        NOT_STARTED,
        RUNNING,
        PAUSED,
        FINISHED
    }

    private String displayName = null;
    private int progress = 0;
    private ActiveScanTableModel messagesTableModel = new ActiveScanTableModel();
    private FilterMessageTableModel filterMessageTableModel = new FilterMessageTableModel();
    private SiteNode startNode = null;
    private ResponseCountSnapshot rcTotals = new ResponseCountSnapshot();
    private ResponseCountSnapshot rcLastSnapshot = new ResponseCountSnapshot();
    private List<ResponseCountSnapshot> rcHistory = new ArrayList<>();

    private Date timeStarted = null;
    private Date timeFinished = null;
    private int maxResultsToList = 0;

    private final List<Integer> hRefs = Collections.synchronizedList(new ArrayList<>());
    private final List<Integer> alerts = Collections.synchronizedList(new ArrayList<>());

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> schedHandle;

    private static final Logger log = LogManager.getLogger(ActiveScan.class);

    @Deprecated
    public ActiveScan(
            String displayName,
            ScannerParam scannerParam,
            org.parosproxy.paros.network.ConnectionParam param,
            ScanPolicy scanPolicy) {
        this(displayName, scannerParam, param, scanPolicy, null);
    }

    /**
     * @deprecated (2.12.0) Use {@link #ActiveScan(String, ScannerParam, ScanPolicy,
     *     RuleConfigParam)} instead.
     */
    @Deprecated
    public ActiveScan(
            String displayName,
            ScannerParam scannerParam,
            org.parosproxy.paros.network.ConnectionParam param,
            ScanPolicy scanPolicy,
            RuleConfigParam ruleConfigParam) {
        this(displayName, scannerParam, scanPolicy, ruleConfigParam);
    }

    public ActiveScan(
            String displayName,
            ScannerParam scannerParam,
            ScanPolicy scanPolicy,
            RuleConfigParam ruleConfigParam) {
        super(scannerParam, scanPolicy, ruleConfigParam);
        this.displayName = displayName;
        this.maxResultsToList = scannerParam.getMaxResultsToList();
        // Easiest way to get the messages and alerts ;)
        this.addScannerListener(this);
    }

    @Override
    public int getMaximum() {
        return 100;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public boolean isRunning() {
        return !this.isStop();
    }

    @Override
    public boolean isStopped() {
        return super.isStop();
    }

    @Override
    public void pauseScan() {
        if (this.isRunning()) {
            super.pause();
        }
    }

    public int getTotalRequests() {
        int total = 0;
        for (HostProcess process : this.getHostProcesses()) {
            total += process.getRequestCount();
        }
        return total;
    }

    public int getTotalNewAlerts() {
        int totalNewAlerts = 0;
        for (HostProcess process : this.getHostProcesses()) {
            totalNewAlerts += process.getNewAlertCount();
        }
        return totalNewAlerts;
    }

    public ResponseCountSnapshot getRequestHistory() {
        if (this.rcHistory.size() > 0) {
            try {
                return this.rcHistory.remove(0);
            } catch (Exception e) {
                // Ignore - another thread must have just removed the last snapshot
            }
        }
        return null;
    }

    @Override
    public void start(Target target) {
        reset();
        this.timeStarted = new Date();
        this.progress = 0;
        final int period = 2;

        super.start(target);

        if (View.isInitialised()) {
            scheduler = Executors.newScheduledThreadPool(1);
            // For now this is only supported in the desktop UI
            final Runnable requestCounter =
                    new Runnable() {
                        @Override
                        public void run() {
                            if (isStop()) {
                                schedHandle.cancel(true);
                                return;
                            }
                            ResponseCountSnapshot currentSnapshot = rcTotals.clone();
                            rcHistory.add(currentSnapshot.getDifference(rcLastSnapshot));
                            if (rcHistory.size() > MAX_STATS_HISTORY_SIZE) {
                                // Trim it to prevent it from getting too big
                                rcHistory.remove(0);
                            }
                            rcLastSnapshot = currentSnapshot;
                        }
                    };
            schedHandle =
                    scheduler.scheduleWithFixedDelay(
                            requestCounter, period, period, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stopScan() {
        super.stop();
        if (schedHandle != null) {
            schedHandle.cancel(true);
        }
    }

    @Override
    public void resumeScan() {
        if (this.isPaused()) {
            super.resume();
        }
    }

    @Override
    public void alertFound(Alert alert) {
        int alertId = alert.getAlertId();
        if (alertId != -1) {
            alerts.add(alert.getAlertId());
        }
    }

    @Override
    public void hostComplete(int id, String hostAndPort) {}

    @Override
    public void hostNewScan(int id, String hostAndPort, HostProcess hostThread) {}

    @Override
    public void hostProgress(int id, String hostAndPort, String msg, int percentage) {
        // Calculate the percentage based on the average of all of the host processes
        // This is an approximation as different host process make significantly different times
        int tot = 0;
        for (HostProcess process : this.getHostProcesses()) {
            tot += process.getPercentageComplete();
        }
        int latestProgress = tot / this.getHostProcesses().size();
        if (latestProgress != this.progress) {
            this.progress = latestProgress;
            ActiveScanEventPublisher.publishScanProgressEvent(this.getId(), this.progress);
        }
    }

    @Override
    public void filteredMessage(HttpMessage msg, String reason) {
        filterMessageTableModel.addResult(msg.getRequestHeader().getURI().toString(), reason);
    }

    @Override
    public void scannerComplete(int id) {
        this.timeFinished = new Date();
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    // @Override
    public DefaultListModel<HistoryReference> getList() {
        return null;
    }

    FilterMessageTableModel getFilterMessageTableModel() {
        return filterMessageTableModel;
    }

    public ActiveScanTableModel getMessagesTableModel() {
        return messagesTableModel;
    }

    @Override
    public void notifyNewMessage(final HttpMessage msg) {
        HistoryReference hRef = msg.getHistoryRef();
        if (hRef == null) {
            try {
                hRef =
                        new HistoryReference(
                                Model.getSingleton().getSession(),
                                HistoryReference.TYPE_SCANNER_TEMPORARY,
                                msg);
                msg.setHistoryRef(null);
                hRefs.add(hRef.getHistoryId());
            } catch (HttpMalformedHeaderException | DatabaseException e) {
                log.error(e.getMessage(), e);
            }
        } else {
            hRefs.add(hRef.getHistoryId());
        }

        this.rcTotals.incResponseCodeCount(msg.getResponseHeader().getStatusCode());

        if (hRef != null && View.isInitialised()) {
            // Very large lists significantly impact the UI responsiveness
            // limiting them makes large scans _much_ quicker
            if (this.rcTotals.getTotal() > this.maxResultsToList) {
                removeFirstHistoryReferenceInEdt();
            }
            addHistoryReferenceInEdt(hRef);
        }
    }

    private void addHistoryReferenceInEdt(final HistoryReference hRef) {
        EventQueue.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {
                        messagesTableModel.addHistoryReference(hRef);
                    }
                });
    }

    private void removeFirstHistoryReferenceInEdt() {
        EventQueue.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {
                        messagesTableModel.removeHistoryReference(
                                getMessagesTableModel().getEntry(0).getHistoryReference());
                    }
                });
    }

    @Override
    public SiteNode getStartNode() {
        return this.startNode;
    }

    @Override
    public void setStartNode(SiteNode startNode) {
        this.startNode = startNode;
        super.setStartNode(startNode);
    }

    public void reset() {
        if (!View.isInitialised() || EventQueue.isDispatchThread()) {
            this.messagesTableModel.clear();
        } else {
            EventQueue.invokeLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            reset();
                        }
                    });
        }
    }

    public Date getTimeStarted() {
        return timeStarted;
    }

    public Date getTimeFinished() {
        return timeFinished;
    }

    /**
     * Returns the IDs of all messages sent/created during the scan. The message must be recreated
     * with a HistoryReference.
     *
     * <p><strong>Note:</strong> Iterations must be {@code synchronized} on returned object. Failing
     * to do so might result in {@code ConcurrentModificationException}.
     *
     * @return the IDs of all the messages sent/created during the scan
     * @see HistoryReference
     * @see ConcurrentModificationException
     */
    public List<Integer> getMessagesIds() {
        return hRefs;
    }

    /**
     * Returns the IDs of all alerts raised during the scan.
     *
     * <p><strong>Note:</strong> Iterations must be {@code synchronized} on returned object. Failing
     * to do so might result in {@code ConcurrentModificationException}.
     *
     * @return the IDs of all the alerts raised during the scan
     * @see ConcurrentModificationException
     */
    public List<Integer> getAlertsIds() {
        return alerts;
    }

    public State getState() {
        if (this.timeStarted == null) {
            return State.NOT_STARTED;
        } else if (this.isStop()) {
            return State.FINISHED;
        } else if (this.isPaused()) {
            return State.PAUSED;
        } else {
            return State.RUNNING;
        }
    }

    @Override
    public void setDisplayName(String name) {
        this.displayName = name;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setScanId(int id) {
        this.setId(id);
    }

    @Override
    public int getScanId() {
        return this.getId();
    }
}

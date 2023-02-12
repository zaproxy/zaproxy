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
package org.zaproxy.zap.extension.spider;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.table.TableModel;
import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.GenericScanner2;
import org.zaproxy.zap.model.ScanEventPublisher;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.ScanListenner2;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.Stats;

/** @deprecated (2.12.0) See the spider add-on in zap-extensions instead. */
@Deprecated
public class SpiderScan
        implements ScanListenner, org.zaproxy.zap.spider.SpiderListener, GenericScanner2 {

    public static final String SPIDER_SCAN_STARTED_STATS = "stats.spider.started";
    public static final String SPIDER_SCAN_STOPPED_STATS = "stats.spider.stopped";
    public static final String SPIDER_SCAN_TIME_STATS = "stats.spider.time";
    public static final String SPIDER_URL_FOUND_STATS = "stats.spider.url.found";
    public static final String SPIDER_URL_ERROR_STATS = "stats.spider.url.error";

    private static enum State {
        NOT_STARTED,
        RUNNING,
        PAUSED,
        FINISHED
    }

    private static final EnumSet<org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus>
            FETCH_STATUS_IN_SCOPE =
                    EnumSet.of(
                            org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.VALID,
                            org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.SEED);

    private static final EnumSet<org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus>
            FETCH_STATUS_OUT_OF_SCOPE =
                    EnumSet.of(
                            org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE,
                            org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_CONTEXT,
                            org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.USER_RULES);

    private final Lock lock;

    private int scanId;
    private Target target;
    private User user;

    private String displayName = "";

    /**
     * Counter for number of URIs, in and out of scope, found during the scan.
     *
     * <p>The counter is incremented when a new URI is found.
     *
     * @see #foundURI(String, String, FetchStatus)
     * @see #getNumberOfURIsFound()
     */
    private AtomicInteger numberOfURIsFound;

    private Set<String> foundURIs;

    private List<SpiderResource> resourcesFound;

    private List<SpiderResource> resourcesIoErrors;

    private Set<String> foundURIsOutOfScope;

    private SpiderThread spiderThread = null;

    private State state;

    private int progress;

    private ScanListenner2 listener = null;

    private volatile boolean cleared;

    /**
     * The table model of the messages sent.
     *
     * <p>Lazily initialised.
     *
     * @see #getMessagesTableModel()
     * @see #addMessageToMessagesTableModel(SpiderTaskResult)
     */
    private SpiderMessagesTableModel messagesTableModel;

    /**
     * Constructs a {@code SpiderScan} with the given data.
     *
     * @param extension the extension to obtain configurations and notify the view
     * @param spiderParams the spider options
     * @param target the spider target
     * @param spiderURI the starting URI, may be {@code null}.
     * @param scanUser the user to be used in the scan, may be {@code null}.
     * @param scanId the ID of the scan
     * @param name the name that identifies the target
     * @since 2.6.0
     */
    public SpiderScan(
            ExtensionSpider extension,
            org.zaproxy.zap.spider.SpiderParam spiderParams,
            Target target,
            URI spiderURI,
            User scanUser,
            int scanId,
            String name) {
        lock = new ReentrantLock();
        this.scanId = scanId;
        this.target = target;
        this.user = scanUser;
        setDisplayName(name);

        numberOfURIsFound = new AtomicInteger();
        foundURIs = Collections.synchronizedSet(new HashSet<>());
        resourcesFound = Collections.synchronizedList(new ArrayList<>());
        resourcesIoErrors = Collections.synchronizedList(new ArrayList<>());
        foundURIsOutOfScope = Collections.synchronizedSet(new HashSet<>());

        state = State.NOT_STARTED;

        spiderThread =
                new SpiderThread(Integer.toString(scanId), extension, spiderParams, name, this);

        spiderThread.setStartURI(spiderURI);
        spiderThread.setStartNode(target.getStartNode());
        spiderThread.setScanContext(target.getContext());
        spiderThread.setScanAsUser(scanUser);
        spiderThread.setJustScanInScope(target.isInScopeOnly());
        spiderThread.setScanChildren(target.isRecurse());
    }

    /**
     * Returns the ID of the scan.
     *
     * @return the ID of the scan
     */
    @Override
    public int getScanId() {
        return scanId;
    }

    /**
     * Returns the {@code String} representation of the scan state (not started, running, paused or
     * finished).
     *
     * @return the {@code String} representation of the scan state.
     */
    public String getState() {
        lock.lock();
        try {
            return state.toString();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the progress of the scan, an integer between 0 and 100.
     *
     * @return the progress of the scan.
     */
    @Override
    public int getProgress() {
        return progress;
    }

    /**
     * Starts the scan.
     *
     * <p>The call to this method has no effect if the scan was already started.
     */
    public void start() {
        lock.lock();
        try {
            if (State.NOT_STARTED.equals(state)) {
                spiderThread.addSpiderListener(this);
                spiderThread.start();
                state = State.RUNNING;
                SpiderEventPublisher.publishScanEvent(
                        ScanEventPublisher.SCAN_STARTED_EVENT, this.scanId, this.target, user);
                Stats.incCounter(SPIDER_SCAN_STARTED_STATS);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Pauses the scan.
     *
     * <p>The call to this method has no effect if the scan is not running.
     */
    @Override
    public void pauseScan() {
        lock.lock();
        try {
            if (State.RUNNING.equals(state)) {
                spiderThread.pauseScan();
                state = State.PAUSED;
                SpiderEventPublisher.publishScanEvent(
                        ScanEventPublisher.SCAN_PAUSED_EVENT, this.scanId);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Resumes the scan.
     *
     * <p>The call to this method has no effect if the scan is not paused.
     */
    @Override
    public void resumeScan() {
        lock.lock();
        try {
            if (State.PAUSED.equals(state)) {
                spiderThread.resumeScan();
                state = State.RUNNING;
                SpiderEventPublisher.publishScanEvent(
                        ScanEventPublisher.SCAN_RESUMED_EVENT, this.scanId);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Stops the scan.
     *
     * <p>The call to this method has no effect if the scan was not yet started or has already
     * finished.
     */
    @Override
    public void stopScan() {
        lock.lock();
        try {
            if (!State.NOT_STARTED.equals(state) && !State.FINISHED.equals(state)) {
                spiderThread.stopScan();
                state = State.FINISHED;
                SpiderEventPublisher.publishScanEvent(
                        ScanEventPublisher.SCAN_STOPPED_EVENT, this.scanId);
                Stats.incCounter(SPIDER_SCAN_STOPPED_STATS);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the URLs found during the scan.
     *
     * <p><strong>Note:</strong> Iterations must be {@code synchronized} on returned object. Failing
     * to do so might result in {@code ConcurrentModificationException}.
     *
     * @return the URLs found during the scan
     * @see ConcurrentModificationException
     */
    public Set<String> getResults() {
        return foundURIs;
    }

    /**
     * Returns the resources found during the scan.
     *
     * <p><strong>Note:</strong> Iterations must be {@code synchronized} on returned object. Failing
     * to do so might result in {@code ConcurrentModificationException}.
     *
     * @return the resources found during the scan
     * @see ConcurrentModificationException
     */
    public List<SpiderResource> getResourcesFound() {
        return resourcesFound;
    }

    /**
     * Returns the resources found during the scan that were not successfully obtained because of
     * I/O errors.
     *
     * <p><strong>Note:</strong> Iterations must be {@code synchronized} on returned object. Failing
     * to do so might result in {@code ConcurrentModificationException}.
     *
     * @return the resources found during the scan that were not successfully obtained
     * @since 2.6.0
     */
    public List<SpiderResource> getResourcesIoErrors() {
        return resourcesIoErrors;
    }

    /**
     * Returns the URLs, out of scope, found during the scan.
     *
     * <p><strong>Note:</strong> Iterations must be {@code synchronized} on returned object. Failing
     * to do so might result in {@code ConcurrentModificationException}.
     *
     * @return the URLs, out of scope, found during the scan
     * @see ConcurrentModificationException
     */
    public Set<String> getResultsOutOfScope() {
        return foundURIsOutOfScope;
    }

    @Override
    public void notifySpiderTaskResult(org.zaproxy.zap.spider.SpiderTaskResult spiderTaskResult) {
        HttpMessage msg = spiderTaskResult.getHttpMessage();
        HttpRequestHeader requestHeader = msg.getRequestHeader();
        HttpResponseHeader responseHeader = msg.getResponseHeader();
        SpiderResource resource =
                new SpiderResource(
                        msg.getHistoryRef() != null ? msg.getHistoryRef().getHistoryId() : -1,
                        requestHeader.getMethod(),
                        requestHeader.getURI().toString(),
                        responseHeader.getStatusCode(),
                        responseHeader.getReasonPhrase(),
                        spiderTaskResult.isProcessed(),
                        spiderTaskResult.getReasonNotProcessed());

        if (msg.isResponseFromTargetHost()) {
            resourcesFound.add(resource);
            Stats.incCounter(SPIDER_URL_FOUND_STATS);
        } else {
            resourcesIoErrors.add(resource);
            Stats.incCounter(SPIDER_URL_ERROR_STATS);
        }

        if (View.isInitialised()) {
            addMessageToMessagesTableModel(spiderTaskResult);
        }
    }

    private void addMessageToMessagesTableModel(
            final org.zaproxy.zap.spider.SpiderTaskResult spiderTaskResult) {
        if (spiderTaskResult.getHttpMessage().getHistoryRef() == null) {
            return;
        }

        if (EventQueue.isDispatchThread() || cleared) {
            if (cleared) {
                return;
            }

            if (messagesTableModel == null) {
                messagesTableModel = new SpiderMessagesTableModel();
            }
            messagesTableModel.addHistoryReference(
                    spiderTaskResult.getHttpMessage().getHistoryRef(),
                    spiderTaskResult.isProcessed(),
                    spiderTaskResult.getReasonNotProcessed());
            return;
        }

        EventQueue.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {
                        addMessageToMessagesTableModel(spiderTaskResult);
                    }
                });
    }

    @Override
    public void spiderComplete(boolean successful) {
        lock.lock();
        try {
            state = State.FINISHED;
            SpiderEventPublisher.publishScanEvent(
                    ScanEventPublisher.SCAN_COMPLETED_EVENT, this.scanId);
        } finally {
            lock.unlock();
        }
        if (listener != null) {
            listener.scanFinshed(this.getScanId(), this.getDisplayName());
        }
    }

    @Override
    public void spiderProgress(int percentageComplete, int numberCrawled, int numberToCrawl) {
        if (this.progress != percentageComplete) {
            this.progress = percentageComplete;
            SpiderEventPublisher.publishScanProgressEvent(scanId, percentageComplete);
        }

        if (listener != null) {
            listener.scanProgress(this.getScanId(), this.getDisplayName(), percentageComplete, 100);
        }
    }

    @Override
    public void foundURI(
            String uri,
            String method,
            org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status) {
        numberOfURIsFound.incrementAndGet();
        if (FETCH_STATUS_IN_SCOPE.contains(status)) {
            foundURIs.add(uri);
        } else if (FETCH_STATUS_OUT_OF_SCOPE.contains(status)) {
            foundURIsOutOfScope.add(uri);
        }
    }

    @Override
    public void run() {
        // Nothing to do.
    }

    @Override
    public void setScanId(int id) {
        this.scanId = id;
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
    public boolean isStopped() {
        return this.spiderThread.isStopped();
    }

    @Override
    public int getMaximum() {
        return 100;
    }

    /**
     * Gets the number of URIs, in and out of scope, found during the scan.
     *
     * @return the number of URIs found during the scan
     * @since 2.4.3
     */
    public int getNumberOfURIsFound() {
        return numberOfURIsFound.get();
    }

    public int getNumberOfNodesAdded() {
        return this.spiderThread.getNumberOfNodesAdded();
    }

    @Override
    public boolean isPaused() {
        return this.spiderThread.isPaused();
    }

    @Override
    public boolean isRunning() {
        return this.spiderThread.isRunning();
    }

    @Override
    public void scanFinshed(String host) {
        this.spiderComplete(true);
        Stats.incCounter(SPIDER_SCAN_TIME_STATS, this.spiderThread.getTimeTakenInMs());
    }

    @Override
    public void scanProgress(String host, int progress, int maximum) {}

    public TableModel getResultsTableModel() {
        return this.spiderThread.getResultsTableModel();
    }

    public SpiderPanelTableModel getAddedNodesTableModel() {
        return this.spiderThread.getAddedNodesTableModel();
    }

    /**
     * Gets the {@code TableModel} of the messages sent during the spidering process.
     *
     * @return a {@code TableModel} with the messages sent
     * @since 2.5.0
     */
    TableModel getMessagesTableModel() {
        if (messagesTableModel == null) {
            messagesTableModel = new SpiderMessagesTableModel();
        }
        return messagesTableModel;
    }

    public void setListener(ScanListenner2 listener) {
        this.listener = listener;
    }

    public void setCustomSpiderParsers(
            List<org.zaproxy.zap.spider.parser.SpiderParser> customSpiderParsers) {
        spiderThread.setCustomSpiderParsers(customSpiderParsers);
    }

    public void setCustomFetchFilters(
            List<org.zaproxy.zap.spider.filters.FetchFilter> customFetchFilters) {
        spiderThread.setCustomFetchFilters(customFetchFilters);
    }

    public void setCustomParseFilters(
            List<org.zaproxy.zap.spider.filters.ParseFilter> customParseFilters) {
        spiderThread.setCustomParseFilters(customParseFilters);
    }

    /**
     * Clears the table model of the HTTP messages sent.
     *
     * @since 2.5.0
     * @see #getMessagesTableModel()
     */
    void clear() {
        cleared = true;
        if (messagesTableModel != null) {
            messagesTableModel.clear();
            messagesTableModel = null;
        }
    }
}

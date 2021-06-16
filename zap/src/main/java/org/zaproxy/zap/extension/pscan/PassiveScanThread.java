/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.htmlparser.jericho.MasonTagTypes;
import net.htmlparser.jericho.MicrosoftConditionalCommentTagTypes;
import net.htmlparser.jericho.PHPTagTypes;
import net.htmlparser.jericho.Source;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.utils.Stats;

public class PassiveScanThread extends Thread implements ProxyListener, SessionChangedListener {

    private static final Logger logger = LogManager.getLogger(PassiveScanThread.class);

    // Could be after the last one that saves the HttpMessage, as this ProxyListener doesn't change
    // the HttpMessage.
    public static final int PROXY_LISTENER_ORDER = ProxyListenerLog.PROXY_LISTENER_ORDER + 1;

    private static Set<Integer> optedInHistoryTypes = new HashSet<>();

    @SuppressWarnings("unused")
    private OptionsPassiveScan options = null;

    private PassiveScannerList scannerList = null;
    private int currentId = 1;
    private int lastId = -1;
    private int mainSleep = 5000;
    private int postSleep = 200;
    private volatile boolean shutDown = false;

    private final ExtensionHistory extHist;
    private final ExtensionAlert extAlert;
    private final PassiveScanParam pscanOptions;

    private TableHistory historyTable = null;
    private HistoryReference href = null;
    private Session session;

    private String currentRuleName = "";
    private String currentUrl = "";
    private long currentRuleStartTime = 0;
    private Map<Integer, Integer> alertCounts = new HashMap<>();

    /**
     * Constructs a {@code PassiveScanThread} with the given data.
     *
     * @param passiveScannerList the passive scanners, must not be {@code null}.
     * @param extHist the extension to obtain the (cached) history references, might be {@code
     *     null}.
     * @param extensionAlert the extension used to raise the alerts, must not be {@code null}.
     * @deprecated (2.6.0) Use {@link #PassiveScanThread(PassiveScannerList, ExtensionHistory,
     *     ExtensionAlert, PassiveScanParam)} instead. It will be removed in a future release.
     */
    @Deprecated
    public PassiveScanThread(
            PassiveScannerList passiveScannerList,
            ExtensionHistory extHist,
            ExtensionAlert extensionAlert) {
        this(passiveScannerList, extHist, extensionAlert, new PassiveScanParam());
    }

    /**
     * Constructs a {@code PassiveScanThread} with the given data.
     *
     * @param passiveScannerList the passive scanners, must not be {@code null}.
     * @param extHist the extension to obtain the (cached) history references, might be {@code
     *     null}.
     * @param extensionAlert the extension used to raise the alerts, must not be {@code null}.
     * @param pscanOptions the passive scanner options, must not be {@code null}.
     * @since 2.6.0
     */
    public PassiveScanThread(
            PassiveScannerList passiveScannerList,
            ExtensionHistory extHist,
            ExtensionAlert extensionAlert,
            PassiveScanParam pscanOptions) {
        super("ZAP-PassiveScanner");
        this.setDaemon(true);

        if (extensionAlert == null) {
            throw new IllegalArgumentException("Parameter extensionAlert must not be null.");
        }

        this.scannerList = passiveScannerList;

        MicrosoftConditionalCommentTagTypes.register();
        PHPTagTypes.register();
        PHPTagTypes.PHP_SHORT
                .deregister(); // remove PHP short tags otherwise they override processing
        // instructions
        MasonTagTypes.register();

        extAlert = extensionAlert;
        this.extHist = extHist;
        this.pscanOptions = pscanOptions;
    }

    @Override
    public void run() {
        historyTable = Model.getSingleton().getDb().getTableHistory();
        session = Model.getSingleton().getSession();
        // Get the last id - in case we've just opened an existing session
        currentId = this.getLastHistoryId();
        lastId = currentId;

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
                try {
                    href = getHistoryReference(currentId);
                    // historyRecord = historyTable.read(currentId);
                } catch (Exception e) {
                    if (shutDown) {
                        return;
                    }
                    logger.error("Failed to read record " + currentId + " from History table", e);
                }

                if (href != null
                        && (!pscanOptions.isScanOnlyInScope() || session.isInScope(href))) {
                    try {
                        // Parse the record
                        HttpMessage msg = href.getHttpMessage();
                        Source src = new Source(msg.getResponseBody().toString());
                        currentUrl = msg.getRequestHeader().getURI().toString();
                        PassiveScanData passiveScanData = new PassiveScanData(msg);
                        int maxBodySize = this.pscanOptions.getMaxBodySizeInBytesToScan();

                        for (PassiveScanner scanner : scannerList.list()) {
                            try {
                                if (shutDown) {
                                    return;
                                }
                                int hrefHistoryType = href.getHistoryType();
                                if (scanner.isEnabled()
                                        && (scanner.appliesToHistoryType(hrefHistoryType)
                                                || optedInHistoryTypes.contains(hrefHistoryType))) {
                                    boolean cleanScanner = false;
                                    if (scanner instanceof PluginPassiveScanner) {
                                        ((PluginPassiveScanner) scanner)
                                                .init(this, msg, passiveScanData);
                                        cleanScanner = true;
                                    } else {
                                        scanner.setParent(this);
                                    }
                                    currentRuleName = scanner.getName();
                                    currentRuleStartTime = System.currentTimeMillis();
                                    boolean scanned = false;
                                    if (maxBodySize <= 0
                                            || msg.getRequestBody().length() < maxBodySize) {
                                        scanner.scanHttpRequestSend(msg, href.getHistoryId());
                                        scanned = true;
                                    } else {
                                        Stats.incCounter("stats.pscan.reqBodyTooBig");
                                        if (logger.isDebugEnabled()) {
                                            logger.debug(
                                                    "Request to "
                                                            + msg.getRequestHeader().getURI()
                                                            + " body size "
                                                            + msg.getRequestBody().length()
                                                            + " larger than max configured "
                                                            + maxBodySize);
                                        }
                                    }
                                    if (msg.isResponseFromTargetHost()) {
                                        if (maxBodySize <= 0
                                                || msg.getResponseBody().length() < maxBodySize) {
                                            scanner.scanHttpResponseReceive(
                                                    msg, href.getHistoryId(), src);
                                            scanned = true;
                                        } else {
                                            Stats.incCounter("stats.pscan.respBodyTooBig");
                                            if (logger.isDebugEnabled()) {
                                                logger.debug(
                                                        "Response from "
                                                                + msg.getRequestHeader().getURI()
                                                                + " body size "
                                                                + msg.getResponseBody().length()
                                                                + " larger than max configured "
                                                                + maxBodySize);
                                            }
                                        }
                                    }
                                    if (cleanScanner) {
                                        ((PluginPassiveScanner) scanner).clean();
                                    }
                                    if (scanned) {
                                        long timeTaken =
                                                System.currentTimeMillis() - currentRuleStartTime;
                                        Stats.incCounter(
                                                "stats.pscan." + currentRuleName, timeTaken);
                                        if (timeTaken > 5000) {
                                            // Took over 5 seconds, thats not ideal
                                            String responseInfo = "";
                                            if (msg.isResponseFromTargetHost()) {
                                                responseInfo =
                                                        msg.getResponseHeader()
                                                                        .getHeader(
                                                                                HttpHeader
                                                                                        .CONTENT_TYPE)
                                                                + " "
                                                                + msg.getResponseBody().length();
                                            }
                                            logger.warn(
                                                    "Passive Scan rule "
                                                            + currentRuleName
                                                            + " took "
                                                            + (timeTaken / 1000)
                                                            + " seconds to scan "
                                                            + currentUrl
                                                            + " "
                                                            + responseInfo);
                                        }
                                    }
                                }
                            } catch (Throwable e) {
                                if (shutDown) {
                                    return;
                                }
                                logger.error(
                                        "Scanner "
                                                + scanner.getName()
                                                + " failed on record "
                                                + currentId
                                                + " from History table: "
                                                + href.getMethod()
                                                + " "
                                                + href.getURI(),
                                        e);
                            }
                            // Unset in case this is the last one that gets run for a while
                            currentRuleName = "";
                            currentRuleStartTime = 0;
                        }
                    } catch (Exception e) {
                        if (HistoryReference.getTemporaryTypes().contains(href.getHistoryType())) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "Temporary record " + currentId + " no longer available:",
                                        e);
                            }
                        } else {
                            logger.error(
                                    "Parser failed on record " + currentId + " from History table",
                                    e);
                        }
                    }
                    currentUrl = "";
                }
            } catch (Exception e) {
                if (shutDown) {
                    return;
                }
                logger.error("Failed on record " + currentId + " from History table", e);
            }
            if (View.isInitialised()) {
                Control.getSingleton()
                        .getExtensionLoader()
                        .getExtension(ExtensionPassiveScan.class)
                        .getScanStatus()
                        .setScanCount(getRecordsToScan());
            }
        }
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
        return historyTable.lastIndex();
    }

    protected int getRecordsToScan() {
        if (historyTable == null) {
            return 0;
        }
        return this.getLastHistoryId() - getLastScannedId();
    }

    private int getLastScannedId() {
        if (currentId > lastId) {
            return currentId - 1;
        }
        return currentId;
    }

    public void raiseAlert(int id, Alert alert) {
        if (shutDown) {
            return;
        }

        if (currentId != id) {
            logger.error("Alert id != currentId! " + id + " " + currentId);
        }

        alert.setSource(Alert.Source.PASSIVE);
        // Raise the alert
        extAlert.alertFound(alert, href);

        if (this.pscanOptions.getMaxAlertsPerRule() > 0) {
            // Theres a limit on how many each rule can raise
            Integer count = alertCounts.get(alert.getPluginId());
            if (count == null) {
                count = Integer.valueOf(0);
            }
            alertCounts.put(alert.getPluginId(), count + 1);
            if (count > this.pscanOptions.getMaxAlertsPerRule()) {
                // Disable the plugin
                PassiveScanner scanner = this.scannerList.getScanner(alert.getPluginId());
                if (scanner != null) {
                    logger.info(
                            "Disabling passive scanner "
                                    + scanner.getName()
                                    + " as it has raised more than "
                                    + this.pscanOptions.getMaxAlertsPerRule()
                                    + " alerts.");
                    scanner.setEnabled(false);
                }
            }
        }
    }

    /**
     * Adds the given tag to the message being passive scanned.
     *
     * @param id not used.
     * @param tag the name of the tag.
     * @deprecated (2.11.0) Use {@link #addTag(String)} instead, the id is not used.
     */
    @Deprecated
    public void addTag(int id, String tag) {
        addTag(tag);
    }

    /**
     * Adds the given tag to the message being passive scanned.
     *
     * @param tag the name of the tag.
     * @since 2.11.0
     */
    public void addTag(String tag) {
        if (shutDown) {
            return;
        }

        try {
            if (!href.getTags().contains(tag)) {
                href.addTag(tag);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public int getArrangeableListenerOrder() {
        return PROXY_LISTENER_ORDER;
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

    @Override
    public void sessionChanged(Session session) {
        // Reset the currentId
        historyTable = Model.getSingleton().getDb().getTableHistory();
        href = null;
        // Get the last id - in case we've just opened an existing session
        currentId = historyTable.lastIndex();
        lastId = currentId;
    }

    @Override
    public void sessionScopeChanged(Session session) {}

    public void shutdown() {
        this.shutDown = true;
    }

    @Override
    public void sessionAboutToChange(Session session) {}

    @Override
    public void sessionModeChanged(Mode mode) {
        // Ignore
    }

    /**
     * Add the History Type ({@code int}) to the set of applicable history types.
     *
     * @param type the type to be added to the set of applicable history types
     * @since 2.8.0
     */
    public static void addApplicableHistoryType(int type) {
        optedInHistoryTypes.add(type);
    }

    /**
     * Remove the History Type ({@code int}) from the set of applicable history types.
     *
     * @param type the type to be removed from the set of applicable history types
     * @since 2.8.0
     */
    public static void removeApplicableHistoryType(int type) {
        optedInHistoryTypes.remove(type);
    }

    /**
     * Returns the set of History Types which have "opted-in" to be applicable for passive scanning.
     *
     * @return a set of {@code Integer} representing all of the History Types which have "opted-in"
     *     for passive scanning.
     * @since 2.8.0
     */
    public static Set<Integer> getOptedInHistoryTypes() {
        return Collections.unmodifiableSet(optedInHistoryTypes);
    }

    /**
     * Returns the full set (both default and "opted-in") which are to be applicable for passive
     * scanning.
     *
     * @return a set of {@code Integer} representing all of the History Types which are applicable
     *     for passive scanning.
     * @since 2.8.0
     */
    public static Set<Integer> getApplicableHistoryTypes() {
        Set<Integer> allApplicableTypes = new HashSet<>();
        allApplicableTypes.addAll(PluginPassiveScanner.getDefaultHistoryTypes());
        if (!optedInHistoryTypes.isEmpty()) {
            allApplicableTypes.addAll(optedInHistoryTypes);
        }
        return allApplicableTypes;
    }

    public String getCurrentRuleName() {
        return currentRuleName;
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public long getCurrentRuleStartTime() {
        return currentRuleStartTime;
    }
}

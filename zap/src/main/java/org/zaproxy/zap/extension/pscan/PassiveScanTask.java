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

import net.htmlparser.jericho.Source;
import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.utils.Stats;

/**
 * A class which runs all of the enabled passive scanners against a specified HistoryReference
 *
 * @since 2.12.0
 */
public class PassiveScanTask implements Runnable {

    /** I think this should be a thread which runs just one scan rule against one history record */
    private HistoryReference href;

    private PassiveScanTaskHelper helper;

    @SuppressWarnings("deprecation")
    private PassiveScanThread psThread;

    private int maxBodySize;
    private Boolean completed = null;
    private boolean shutdown = false;
    private PassiveScanner currentScanner;
    private long startTime;
    private long stopTime;

    private static final Logger logger = LogManager.getLogger(PassiveScanTask.class);

    @SuppressWarnings("deprecation")
    public PassiveScanTask(HistoryReference hr, PassiveScanTaskHelper helper) {
        this.href = hr;
        this.helper = helper;
        this.psThread = new PassiveScanThread(helper, href);
        this.maxBodySize = helper.getMaxBodySizeInBytesToScan();
        helper.addTaskToList(this);
    }

    public Boolean hasCompleted() {
        return completed;
    }

    public void shutdown() {
        this.shutdown = true;
    }

    public PassiveScanner getCurrentScanner() {
        return this.currentScanner;
    }

    public URI getURI() {
        return this.href.getURI();
    }

    public HistoryReference getHistoryReference() {
        return this.href;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    @Override
    public void run() {
        boolean scanned = false;
        startTime = System.currentTimeMillis();

        completed = false;

        try {
            // Parse the record
            HttpMessage msg = href.getHttpMessage();
            Source src = new Source(msg.getResponseBody().toString());
            PassiveScanData passiveScanData = new PassiveScanData(msg);

            for (PassiveScanner scanner : helper.getPassiveScannerList().list()) {
                currentScanner = scanner;
                try {
                    if (shutdown) {
                        return;
                    }
                    int hrefHistoryType = href.getHistoryType();
                    if (scanner.isEnabled()
                            && (scanner.appliesToHistoryType(hrefHistoryType)
                                    || PassiveScanTaskHelper.getOptedInHistoryTypes()
                                            .contains(hrefHistoryType))) {

                        if (scanner instanceof PluginPassiveScanner) {
                            PluginPassiveScanner pps = ((PluginPassiveScanner) scanner).copy();
                            pps.setHelper(passiveScanData);
                            scanner = pps;
                        }
                        scanner.setParent(psThread);
                        scanner.setTaskHelper(helper);

                        logger.debug(
                                "Running scan rule, URL {} plugin {}",
                                msg.getRequestHeader().getURI(),
                                scanner.getName());
                        long scanRuleStartTime = System.currentTimeMillis();

                        if (maxBodySize <= 0 || msg.getRequestBody().length() < maxBodySize) {
                            scanner.scanHttpRequestSend(msg, href.getHistoryId());
                            scanned = true;
                        } else {
                            Stats.incCounter("stats.pscan.reqBodyTooBig");
                            logger.debug(
                                    "Request to {} body size {} larger than max configured {}",
                                    msg.getRequestHeader().getURI(),
                                    msg.getRequestBody().length(),
                                    maxBodySize);
                        }
                        if (msg.isResponseFromTargetHost()) {
                            if (maxBodySize <= 0 || msg.getResponseBody().length() < maxBodySize) {
                                scanner.scanHttpResponseReceive(msg, href.getHistoryId(), src);
                                scanned = true;
                            } else {
                                Stats.incCounter("stats.pscan.respBodyTooBig");
                                logger.debug(
                                        "Response from {} body size {} larger than max configured {}",
                                        msg.getRequestHeader().getURI(),
                                        msg.getResponseBody().length(),
                                        maxBodySize);
                            }
                        }
                        if (scanned) {
                            long timeTaken = System.currentTimeMillis() - scanRuleStartTime;
                            if (scanner instanceof PluginPassiveScanner) {
                                PluginPassiveScanner pps = (PluginPassiveScanner) scanner;
                                Stats.incCounter(
                                        "stats.pscan." + pps.getPluginId() + ".time", timeTaken);
                            }
                            // TODO remove at some point
                            Stats.incCounter("stats.pscan." + scanner.getName(), timeTaken);
                            if (timeTaken > 5000) {
                                // Took over 5 seconds, thats not ideal
                                String responseInfo = "";
                                if (msg.isResponseFromTargetHost()) {
                                    responseInfo =
                                            msg.getResponseHeader()
                                                            .getHeader(HttpHeader.CONTENT_TYPE)
                                                    + " "
                                                    + msg.getResponseBody().length();
                                }
                                logger.warn(
                                        "Passive Scan rule {} took {} seconds to scan {} {}",
                                        scanner.getName(),
                                        timeTaken / 1000,
                                        msg.getRequestHeader().getURI(),
                                        responseInfo);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(
                            "Scan rule '{}' failed on record {} from History table: {} {}",
                            scanner.getName(),
                            href.getHistoryId(),
                            href.getMethod(),
                            href.getURI(),
                            e);
                }
            }

        } catch (Exception e) {
            if (HistoryReference.getTemporaryTypes().contains(href.getHistoryType())) {
                logger.debug("Temporary record {} no longer available:", href.getHistoryId(), e);
            } else {
                RecordHistory rec = null;
                try {
                    rec = Model.getSingleton().getDb().getTableHistory().read(href.getHistoryId());
                } catch (HttpMalformedHeaderException | DatabaseException e2) {
                    // Ignore
                }
                if (rec == null) {
                    return;
                }
                logger.error(
                        "Parser failed on record {} from History table", href.getHistoryId(), e);
                HttpMessage msg;
                try {
                    msg = href.getHttpMessage();
                    logger.error("Req Header {}", msg.getRequestHeader(), e);
                } catch (Exception e1) {
                    // Ignore
                }
            }
        } finally {
            completed = true;
            stopTime = System.currentTimeMillis();
            helper.removeTaskFromList(this);
        }
    }
}

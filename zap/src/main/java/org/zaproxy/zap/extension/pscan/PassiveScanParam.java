/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.extension.api.ZapApiIgnore;
import org.zaproxy.zap.extension.pscan.scanner.RegexAutoTagScanner;

public class PassiveScanParam extends AbstractParam {

    private static final Logger logger = LogManager.getLogger(PassiveScanParam.class);

    static final String PASSIVE_SCANS_BASE_KEY = "pscans";
    private static final String ALL_AUTO_TAG_SCANNERS_KEY =
            PASSIVE_SCANS_BASE_KEY + ".autoTagScanners.scanner";

    private static final String AUTO_TAG_SCANNER_NAME_KEY = "name";
    private static final String AUTO_TAG_SCANNER_TYPE_KEY = "type";
    private static final String AUTO_TAG_SCANNER_CONFIG_KEY = "config";
    private static final String AUTO_TAG_SCANNER_REQ_URL_REGEX_KEY = "reqUrlRegex";
    private static final String AUTO_TAG_SCANNER_REQ_HEAD_REGEX_KEY = "reqHeadRegex";
    private static final String AUTO_TAG_SCANNER_RES_HEAD_REGEX_KEY = "resHeadRegex";
    private static final String AUTO_TAG_SCANNER_RES_BODY_REGEX_KEY = "resBodyRegex";
    private static final String AUTO_TAG_SCANNER_ENABLED_KEY = "enabled";

    private static final String CONFIRM_REMOVE_AUTO_TAG_SCANNER_KEY =
            PASSIVE_SCANS_BASE_KEY + ".confirmRemoveAutoTagScanner";

    private static final String SCAN_ONLY_IN_SCOPE_KEY =
            PASSIVE_SCANS_BASE_KEY + ".scanOnlyInScope";
    private static final String SCAN_FUZZER_MESSAGES_KEY =
            PASSIVE_SCANS_BASE_KEY + ".scanFuzzerMessages";
    private static final String PASSIVE_SCAN_THREADS = PASSIVE_SCANS_BASE_KEY + ".threads";
    private static final String MAX_ALERTS_PER_RULE = PASSIVE_SCANS_BASE_KEY + ".maxAlertsPerRule";
    private static final String MAX_BODY_SIZE_IN_BYTES =
            PASSIVE_SCANS_BASE_KEY + ".maxBodySizeInBytes";

    public static final int PASSIVE_SCAN_DEFAULT_THREADS = 4;

    private List<RegexAutoTagScanner> autoTagScanners = new ArrayList<>(0);

    private boolean confirmRemoveAutoTagScanner = true;

    /**
     * Flag that indicates whether or not the passive scan should be performed only on messages that
     * are in scope.
     *
     * <p>Default is {@code false}, all messages are scanned.
     */
    private boolean scanOnlyInScope;

    /**
     * Flag that indicates whether or not the passive scan should be performed on traffic generated
     * by the fuzzer.
     *
     * <p>Default is {@code false}, fuzzer traffic is not scanned.
     */
    private boolean scanFuzzerMessages;

    /**
     * The maximum number of alerts any passive scan rule should raise. Rules will be disabled once
     * they exceed this threshold. Default 0, which means there is no limit. This is typically only
     * useful for automated scanning.
     */
    private int maxAlertsPerRule;

    private int maxBodySizeInBytesToScan;

    private int passiveScanThreads;

    public PassiveScanParam() {}

    @Override
    protected void parse() {
        try {
            List<HierarchicalConfiguration> fields =
                    ((HierarchicalConfiguration) getConfig())
                            .configurationsAt(ALL_AUTO_TAG_SCANNERS_KEY);
            this.autoTagScanners = new ArrayList<>(fields.size());
            List<String> tempListNames = new ArrayList<>(fields.size());
            for (HierarchicalConfiguration sub : fields) {
                String name = sub.getString(AUTO_TAG_SCANNER_NAME_KEY, "");
                if (!"".equals(name) && !tempListNames.contains(name)) {
                    tempListNames.add(name);

                    RegexAutoTagScanner app =
                            new RegexAutoTagScanner(
                                    sub.getString(AUTO_TAG_SCANNER_NAME_KEY),
                                    RegexAutoTagScanner.TYPE.valueOf(
                                            sub.getString(AUTO_TAG_SCANNER_TYPE_KEY)),
                                    sub.getString(AUTO_TAG_SCANNER_CONFIG_KEY),
                                    sub.getString(AUTO_TAG_SCANNER_REQ_URL_REGEX_KEY),
                                    sub.getString(AUTO_TAG_SCANNER_REQ_HEAD_REGEX_KEY),
                                    sub.getString(AUTO_TAG_SCANNER_RES_HEAD_REGEX_KEY),
                                    sub.getString(AUTO_TAG_SCANNER_RES_BODY_REGEX_KEY),
                                    sub.getBoolean(AUTO_TAG_SCANNER_ENABLED_KEY, true));

                    autoTagScanners.add(app);
                }
            }
        } catch (ConversionException e) {
            logger.error("Error while loading the auto tag scanners: {}", e.getMessage(), e);
        }

        this.confirmRemoveAutoTagScanner = getBoolean(CONFIRM_REMOVE_AUTO_TAG_SCANNER_KEY, true);
        this.scanOnlyInScope = getBoolean(SCAN_ONLY_IN_SCOPE_KEY, false);
        this.scanFuzzerMessages = getBoolean(SCAN_FUZZER_MESSAGES_KEY, false);
        setFuzzerOptin(this.scanFuzzerMessages);
        this.passiveScanThreads = this.getInt(PASSIVE_SCAN_THREADS, PASSIVE_SCAN_DEFAULT_THREADS);
        if (this.passiveScanThreads <= 0) {
            // Must be greater that zero
            this.passiveScanThreads = PASSIVE_SCAN_DEFAULT_THREADS;
        }
        this.maxAlertsPerRule = this.getInt(MAX_ALERTS_PER_RULE, 0);
        this.maxBodySizeInBytesToScan = this.getInt(MAX_BODY_SIZE_IN_BYTES, 0);
    }

    public void setAutoTagScanners(List<RegexAutoTagScanner> scanners) {
        this.autoTagScanners = scanners;

        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_AUTO_TAG_SCANNERS_KEY);

        for (int i = 0, size = scanners.size(); i < size; ++i) {
            String elementBaseKey = ALL_AUTO_TAG_SCANNERS_KEY + "(" + i + ").";
            RegexAutoTagScanner scanner = scanners.get(i);

            getConfig().setProperty(elementBaseKey + AUTO_TAG_SCANNER_NAME_KEY, scanner.getName());
            getConfig()
                    .setProperty(
                            elementBaseKey + AUTO_TAG_SCANNER_TYPE_KEY,
                            scanner.getType().toString());
            getConfig()
                    .setProperty(elementBaseKey + AUTO_TAG_SCANNER_CONFIG_KEY, scanner.getConf());
            getConfig()
                    .setProperty(
                            elementBaseKey + AUTO_TAG_SCANNER_REQ_URL_REGEX_KEY,
                            scanner.getRequestUrlRegex());
            getConfig()
                    .setProperty(
                            elementBaseKey + AUTO_TAG_SCANNER_REQ_HEAD_REGEX_KEY,
                            scanner.getRequestHeaderRegex());
            getConfig()
                    .setProperty(
                            elementBaseKey + AUTO_TAG_SCANNER_RES_HEAD_REGEX_KEY,
                            scanner.getResponseHeaderRegex());
            getConfig()
                    .setProperty(
                            elementBaseKey + AUTO_TAG_SCANNER_RES_BODY_REGEX_KEY,
                            scanner.getResponseBodyRegex());
            getConfig()
                    .setProperty(
                            elementBaseKey + AUTO_TAG_SCANNER_ENABLED_KEY, scanner.isEnabled());
        }
    }

    public List<RegexAutoTagScanner> getAutoTagScanners() {
        return autoTagScanners;
    }

    @ZapApiIgnore
    public boolean isConfirmRemoveAutoTagScanner() {
        return this.confirmRemoveAutoTagScanner;
    }

    @ZapApiIgnore
    public void setConfirmRemoveAutoTagScanner(boolean confirmRemove) {
        this.confirmRemoveAutoTagScanner = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_AUTO_TAG_SCANNER_KEY, confirmRemoveAutoTagScanner);
    }

    /**
     * Sets whether or not the passive scan should be performed only on messages that are in scope.
     *
     * @param scanOnlyInScope {@code true} if the scan should be performed only on messages that are
     *     in scope, {@code false} otherwise.
     * @since 2.6.0
     * @see #isScanOnlyInScope()
     * @see org.parosproxy.paros.model.Session#isInScope(String) Session.isInScope(String)
     */
    public void setScanOnlyInScope(boolean scanOnlyInScope) {
        this.scanOnlyInScope = scanOnlyInScope;
        getConfig().setProperty(SCAN_ONLY_IN_SCOPE_KEY, scanOnlyInScope);
    }

    /**
     * Tells whether or not the passive scan should be performed only on messages that are in scope.
     *
     * @return {@code true} if the scan should be performed only on messages that are in scope,
     *     {@code false} otherwise.
     * @since 2.6.0
     * @see #setScanOnlyInScope(boolean)
     */
    public boolean isScanOnlyInScope() {
        return scanOnlyInScope;
    }

    /**
     * Sets whether or not the passive scan should be performed on traffic from the fuzzer.
     *
     * @param scanFuzzerMessages {@code true} if the scan should be performed on traffic generated
     *     by the fuzzer, {@code false} otherwise.
     * @since 2.8.0
     * @see #isScanFuzzerMessages()
     */
    public void setScanFuzzerMessages(boolean scanFuzzerMessages) {
        this.scanFuzzerMessages = scanFuzzerMessages;
        getConfig().setProperty(SCAN_FUZZER_MESSAGES_KEY, scanFuzzerMessages);
        setFuzzerOptin(scanFuzzerMessages);
    }

    /**
     * Adds or removes the {@code HistoryReference} types that should be included when passive
     * scanning traffic from the fuzzer.
     *
     * @param shouldOptin {@code true} if the types should be enabled (added) {@code false} if the
     *     types should be disabled (removed)
     * @since 2.8.0
     * @see #isScanFuzzerMessages()
     * @see #setScanFuzzerMessages()
     */
    private void setFuzzerOptin(boolean shouldOptin) {
        if (shouldOptin) {
            PassiveScanTaskHelper.addApplicableHistoryType(HistoryReference.TYPE_FUZZER);
            PassiveScanTaskHelper.addApplicableHistoryType(HistoryReference.TYPE_FUZZER_TEMPORARY);
        } else {
            PassiveScanTaskHelper.removeApplicableHistoryType(HistoryReference.TYPE_FUZZER);
            PassiveScanTaskHelper.removeApplicableHistoryType(
                    HistoryReference.TYPE_FUZZER_TEMPORARY);
        }
    }

    /**
     * Tells whether or not the passive scan should be performed on traffic from the fuzzer.
     *
     * @return {@code true} if the scan should be performed on traffic from the fuzzer, {@code
     *     false} otherwise.
     * @since 2.8.0
     * @see #setScanFuzzerMessages(boolean)
     */
    public boolean isScanFuzzerMessages() {
        return scanFuzzerMessages;
    }

    public int getMaxAlertsPerRule() {
        return maxAlertsPerRule;
    }

    public void setMaxAlertsPerRule(int maxAlertsPerRule) {
        this.maxAlertsPerRule = maxAlertsPerRule;
        getConfig().setProperty(MAX_ALERTS_PER_RULE, maxAlertsPerRule);
    }

    public int getMaxBodySizeInBytesToScan() {
        return maxBodySizeInBytesToScan;
    }

    public void setMaxBodySizeInBytesToScan(int maxBodySizeInBytesToScan) {
        this.maxBodySizeInBytesToScan = maxBodySizeInBytesToScan;
        getConfig().setProperty(MAX_BODY_SIZE_IN_BYTES, maxBodySizeInBytesToScan);
    }

    /**
     * Get the number of passive scan threads
     *
     * @since 2.12.0
     */
    public int getPassiveScanThreads() {
        return passiveScanThreads;
    }

    /**
     * Set the number of passive scan threads
     *
     * @param passiveScanThreads the number of passive scan threads, must be &gt; 0
     * @since 2.12.0
     */
    public void setPassiveScanThreads(int passiveScanThreads) {
        if (passiveScanThreads > 0) {
            this.passiveScanThreads = passiveScanThreads;
            getConfig().setProperty(PASSIVE_SCAN_THREADS, passiveScanThreads);
        }
    }
}

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
package org.zaproxy.zap.extension.pscan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.view.ScanStatus;

@SuppressWarnings("removal")
public class ExtensionPassiveScan extends ExtensionAdaptor implements SessionChangedListener {

    public static final String NAME = "ExtensionPassiveScan";

    @Deprecated(forRemoval = true, since = "2.16.0")
    public static final String SCRIPT_TYPE_PASSIVE = "passive";

    // Should be after the last one that saves the HttpMessage, as this ProxyListener doesn't change
    // the HttpMessage.
    @Deprecated(forRemoval = true, since = "2.16.0")
    public static final int PROXY_LISTENER_ORDER = ProxyListenerLog.PROXY_LISTENER_ORDER + 1;

    private static final PassiveScanRuleManager NOOP_PASSIVE_SCAN_RULE_MANAGER =
            new PassiveScanRuleManager() {

                @Override
                public boolean add(PassiveScanner scanRule) {
                    // Nothing to do.
                    return false;
                }

                @Override
                public PassiveScanner getScanRule(int id) {
                    // Nothing to do.
                    return null;
                }

                @Override
                public List<PassiveScanner> getScanRules() {
                    // Nothing to do.
                    return null;
                }

                @Override
                public List<PluginPassiveScanner> getPluginScanRules() {
                    // Nothing to do.
                    return null;
                }

                @Override
                public boolean remove(PassiveScanner scanRule) {
                    // Nothing to do.
                    return false;
                }

                @Override
                public boolean remove(String className) {
                    // Nothing to do.
                    return false;
                }
            };

    private static final PassiveController NOOP_PASSIVE_CONTROLLER =
            new PassiveController() {

                @Override
                public int getRecordsToScan() {
                    // Nothing to do.
                    return 0;
                }

                @Override
                public void clearQueue() {
                    // Nothing to do.
                }
            };

    private static final List<Class<? extends Extension>> DEPENDENCIES;

    private PassiveScanRuleManager scanRuleManager = NOOP_PASSIVE_SCAN_RULE_MANAGER;

    private PassiveController controller = NOOP_PASSIVE_CONTROLLER;

    static {
        List<Class<? extends Extension>> dep = new ArrayList<>(1);
        dep.add(ExtensionAlert.class);

        DEPENDENCIES = Collections.unmodifiableList(dep);
    }

    public ExtensionPassiveScan() {
        super();
        this.setOrder(26);
        this.setName(NAME);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("pscan.name");
    }

    @Override
    public List<String> getActiveActions() {
        int recordsToScan = getRecordsToScan();
        if (recordsToScan == 0) {
            return Collections.emptyList();
        }

        List<String> activeActions = new ArrayList<>(1);
        activeActions.add(Constant.messages.getString("pscan.activeAction", recordsToScan));
        return activeActions;
    }

    public boolean removePassiveScanner(String className) {
        return scanRuleManager.remove(className);
    }

    /**
     * Adds the given passive scanner to the list of passive scanners that will be used to scan
     * proxied messages.
     *
     * <p>The passive scanner will not be added if there is already a passive scanner with the same
     * name.
     *
     * <p>If the passive scanner extends from {@code PluginPassiveScanner} it will be added with the
     * method {@code addPluginPassiveScanner(PluginPassiveScanner)}.
     *
     * @param passiveScanner the passive scanner that will be added
     * @return {@code true} if the scanner was added, {@code false} otherwise.
     * @throws IllegalArgumentException if the given passive scanner is {@code null}.
     * @see PluginPassiveScanner
     * @see #addPluginPassiveScanner(PluginPassiveScanner)
     * @see PassiveScanner
     */
    public boolean addPassiveScanner(PassiveScanner passiveScanner) {
        return scanRuleManager.add(passiveScanner);
    }

    /**
     * Removes the given passive scanner from the list of passive scanners that are used to scan
     * proxied messages.
     *
     * <p>The passive scanners are removed using their class name.
     *
     * @param passiveScanner the passive scanner that will be removed
     * @return {@code true} if the scanner was removed, {@code false} otherwise.
     * @throws IllegalArgumentException if the given passive scanner is {@code null}.
     * @see PassiveScanner
     */
    public boolean removePassiveScanner(PassiveScanner passiveScanner) {
        if (passiveScanner == null) {
            throw new IllegalArgumentException("Parameter passiveScanner must not be null.");
        }
        return removePassiveScanner(passiveScanner.getClass().getName());
    }

    /**
     * Adds the given plug-in passive scanner to the list of passive scanners that will be used to
     * scan proxied messages.
     *
     * <p>The passive scanner will not be added if there is already a passive scanner with the same
     * name.
     *
     * @param pluginPassiveScanner the plug-in passive scanner that will be added
     * @return {@code true} if the plug-in scanner was added, {@code false} otherwise.
     * @throws IllegalArgumentException if the given plug-in passive scanner is {@code null}.
     * @see PluginPassiveScanner
     */
    public boolean addPluginPassiveScanner(PluginPassiveScanner pluginPassiveScanner) {
        return scanRuleManager.add(pluginPassiveScanner);
    }

    /**
     * Removes the given plug-in passive scanner from the list of passive scanners that are used to
     * scan proxied messages.
     *
     * <p>The plug-in passive scanners are removed using their class name.
     *
     * @param pluginPassiveScanner the passive scanner that will be removed
     * @return {@code true} if the plug-in scanner was removed, {@code false} otherwise.
     * @throws IllegalArgumentException if the given plug-in passive scanner is {@code null}.
     * @see PluginPassiveScanner
     */
    public boolean removePluginPassiveScanner(PluginPassiveScanner pluginPassiveScanner) {
        return scanRuleManager.remove(pluginPassiveScanner);
    }

    /** <strong>Note:</strong> Not part of the public API. */
    public void setPassiveScanRuleManager(PassiveScanRuleManager scanRuleManager) {
        if (scanRuleManager == null) {
            this.scanRuleManager = NOOP_PASSIVE_SCAN_RULE_MANAGER;
        } else {
            this.scanRuleManager = scanRuleManager;
        }
    }

    protected PassiveScanRuleManager getScanRuleManager() {
        return scanRuleManager;
    }

    protected PassiveScannerList getPassiveScannerList() {
        return new PassiveScannerList();
    }

    public List<PluginPassiveScanner> getPluginPassiveScanners() {
        return scanRuleManager.getPluginScanRules();
    }

    /**
     * Sets whether or not all plug-in passive scanners are {@code enabled}.
     *
     * @param enabled {@code true} if the scanners should be enabled, {@code false} otherwise
     */
    void setAllPluginPassiveScannersEnabled(boolean enabled) {
        for (PluginPassiveScanner scanner : getPluginPassiveScanners()) {
            scanner.setEnabled(enabled);
            scanner.save();
        }
    }

    /**
     * Sets whether or not the plug-in passive scanner with the given {@code pluginId} is {@code
     * enabled}.
     *
     * @param pluginId the ID of the plug-in passive scanner
     * @param enabled {@code true} if the scanner should be enabled, {@code false} otherwise
     */
    void setPluginPassiveScannerEnabled(int pluginId, boolean enabled) {
        PluginPassiveScanner scanner = getPluginPassiveScanner(pluginId);
        if (scanner != null) {
            scanner.setEnabled(enabled);
            scanner.save();
        }
    }

    /**
     * Gets the {@code PluginPassiveScanner} with the given ID.
     *
     * @param id the ID of the plugin.
     * @return the {@code PluginPassiveScanner}, or {@code null} if not found (e.g. not installed).
     * @since 2.7.0
     */
    public PluginPassiveScanner getPluginPassiveScanner(int id) {
        for (PluginPassiveScanner scanner : getPluginPassiveScanners()) {
            if (id == scanner.getPluginId()) {
                return scanner;
            }
        }
        return null;
    }

    /**
     * Tells whether or not a plug-in passive scanner with the given {@code pluginId} exist.
     *
     * @param pluginId the ID of the plug-in passive scanner
     * @return {@code true} if the scanner exist, {@code false} otherwise.
     */
    boolean hasPluginPassiveScanner(int pluginId) {
        return getPluginPassiveScanner(pluginId) != null;
    }

    /**
     * Sets the value of {@code alertThreshold} of the plug-in passive scanner with the given {@code
     * pluginId}.
     *
     * <p>If the {@code alertThreshold} is {@code OFF} the scanner is also disabled. The call to
     * this method has no effect if no scanner with the given {@code pluginId} exist.
     *
     * @param pluginId the ID of the plug-in passive scanner
     * @param alertThreshold the alert threshold that will be set
     * @see org.parosproxy.paros.core.scanner.Plugin.AlertThreshold
     */
    void setPluginPassiveScannerAlertThreshold(int pluginId, Plugin.AlertThreshold alertThreshold) {
        PluginPassiveScanner scanner = getPluginPassiveScanner(pluginId);
        if (scanner != null) {
            scanner.setAlertThreshold(alertThreshold);
            scanner.setEnabled(!Plugin.AlertThreshold.OFF.equals(alertThreshold));
            scanner.save();
        }
    }

    /**
     * @param at
     */
    public void setAllScannerThreshold(AlertThreshold at) {
        for (PluginPassiveScanner test : getPluginPassiveScanners()) {
            test.setAlertThreshold(at);
            test.setEnabled(!AlertThreshold.OFF.equals(at));
            test.save();
        }
    }

    /**
     * @return
     */
    public AlertThreshold getAllScannerThreshold() {
        AlertThreshold at = null;

        for (PluginPassiveScanner test : getPluginPassiveScanners()) {
            if (at == null) {
                at = test.getAlertThreshold();

            } else if (!at.equals(test.getAlertThreshold())) {
                // Not all the same
                return null;
            }
        }

        return at;
    }

    protected PolicyPassiveScanPanel getPolicyPanel() {
        return new PolicyPassiveScanPanel();
    }

    /** <strong>Note:</strong> Not part of the public API. */
    public void setPassiveController(PassiveController controller) {
        if (controller == null) {
            this.controller = NOOP_PASSIVE_CONTROLLER;
        } else {
            this.controller = controller;
        }
    }

    @Deprecated(forRemoval = true, since = "2.16.0")
    public int getRecordsToScan() {
        return controller.getRecordsToScan();
    }

    /**
     * Empties the passive scan queue without passively scanning the messages. Currently running
     * rules will run to completion but new rules will only be run when new messages are added to
     * the queue.
     *
     * @since 2.12.0
     */
    @Deprecated(forRemoval = true, since = "2.16.0")
    public void clearQueue() {
        controller.clearQueue();
    }

    PassiveScanParam getPassiveScanParam() {
        return getModel().getOptionsParam().getParamSet(PassiveScanParam.class);
    }

    @Override
    public void sessionAboutToChange(Session session) {
        // Nothing to do.
    }

    @Override
    public void sessionChanged(Session session) {
        // Nothing to do.
    }

    /**
     * @deprecated (2.12.0) use #getOldestRunningTask()
     * @return the oldest rule name running
     */
    @Deprecated
    public String getCurrentRuleName() {
        return null;
    }

    /**
     * @deprecated (2.12.0) use #getOldestRunningTask()
     * @return the oldest URL being scanned
     */
    @Deprecated
    public String getCurrentUrl() {
        return null;
    }

    /**
     * @deprecated (2.12.0) use #getOldestRunningTask()
     * @return the start time of oldest rule running
     */
    @Deprecated
    public long getCurrentRuleStartTime() {
        return 0;
    }

    /**
     * Returns the oldest running task (if any).
     *
     * @since 2.12.0
     * @return the oldest running task
     */
    public PassiveScanTask getOldestRunningTask() {
        return null;
    }

    public List<PassiveScanTask> getRunningTasks() {
        return null;
    }

    @Override
    public List<Class<? extends Extension>> getDependencies() {
        return DEPENDENCIES;
    }

    @Override
    public void sessionScopeChanged(Session session) {
        // Ignore
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("pscan.desc");
    }

    @Override
    public void sessionModeChanged(Mode mode) {
        // Ignore
    }

    @Deprecated(forRemoval = true, since = "2.16.0")
    public void saveTo(Configuration conf) {
        // Method was not in use.
    }

    @Deprecated(forRemoval = true, since = "2.16.0")
    public void loadFrom(Configuration conf) {
        // Method was not in use.
    }

    @Deprecated(forRemoval = true, since = "2.16.0")
    protected ScanStatus getScanStatus() {
        return null;
    }

    @Override
    public boolean supportsLowMemory() {
        return true;
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }
}

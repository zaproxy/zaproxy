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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.htmlparser.jericho.MasonTagTypes;
import net.htmlparser.jericho.MicrosoftConditionalCommentTagTypes;
import net.htmlparser.jericho.PHPTagTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.extension.alert.ExtensionAlert;

/** @since 2.12.0 */
public class PassiveScanTaskHelper {

    private static final Logger logger = LogManager.getLogger(PassiveScanTaskHelper.class);

    private static Set<Integer> optedInHistoryTypes = new HashSet<>();

    private volatile boolean shutDown = false;

    private final ExtensionPassiveScan extPscan;
    private final ExtensionAlert extAlert;
    private final PassiveScanParam pscanParams;
    private Map<Integer, Integer> alertCounts = new HashMap<>();

    private List<PassiveScanner> activeList = Collections.synchronizedList(new ArrayList<>());
    private List<PassiveScanTask> taskList = Collections.synchronizedList(new ArrayList<>());

    public PassiveScanTaskHelper(
            ExtensionPassiveScan extensionPscan,
            ExtensionAlert extensionAlert,
            PassiveScanParam pscanParams) {

        if (extensionAlert == null) {
            throw new IllegalArgumentException("Parameter extensionAlert must not be null.");
        }

        this.extPscan = extensionPscan;
        this.extAlert = extensionAlert;
        this.pscanParams = pscanParams;

        MicrosoftConditionalCommentTagTypes.register();
        PHPTagTypes.register();
        PHPTagTypes.PHP_SHORT
                .deregister(); // remove PHP short tags otherwise they override processing
        // instructions
        MasonTagTypes.register();
    }

    public void addActivePassiveScanner(PassiveScanner scanner) {
        this.activeList.add(scanner);
    }

    public void removeActivePassiveScanner(PassiveScanner scanner) {
        this.activeList.remove(scanner);
    }

    public synchronized void addTaskToList(PassiveScanTask task) {
        this.taskList.add(task);
    }

    public synchronized void removeTaskFromList(PassiveScanTask task) {
        this.taskList.remove(task);
    }

    public int getTaskListSize() {
        return this.taskList.size();
    }

    public synchronized void shutdownTasks() {
        this.taskList.stream().forEach(PassiveScanTask::shutdown);
    }

    public synchronized PassiveScanTask getOldestRunningTask() {
        for (PassiveScanTask task : this.taskList) {
            if (Boolean.FALSE.equals(task.hasCompleted())) {
                return task;
            }
        }
        return null;
    }

    public synchronized List<PassiveScanTask> getRunningTasks() {
        return this.taskList.stream()
                .filter(task -> Boolean.FALSE.equals(task.hasCompleted()))
                .collect(Collectors.toList());
    }

    public synchronized PassiveScanner getOldestRunningScanner() {
        PassiveScanner scanner;
        for (PassiveScanTask task : this.taskList) {
            if (Boolean.FALSE.equals(task.hasCompleted())) {
                scanner = task.getCurrentScanner();
                if (scanner != null) {
                    return scanner;
                }
            }
        }
        return null;
    }

    public PassiveScannerList getPassiveScannerList() {
        return this.extPscan.getPassiveScannerList();
    }

    public int getMaxBodySizeInBytesToScan() {
        return this.pscanParams.getMaxBodySizeInBytesToScan();
    }

    public void raiseAlert(HistoryReference href, Alert alert) {
        if (shutDown) {
            return;
        }

        alert.setSource(Alert.Source.PASSIVE);
        // Raise the alert
        extAlert.alertFound(alert, href);

        if (this.pscanParams.getMaxAlertsPerRule() > 0) {
            // Theres a limit on how many each rule can raise
            Integer count = alertCounts.get(alert.getPluginId());
            if (count == null) {
                count = Integer.valueOf(0);
            }
            alertCounts.put(alert.getPluginId(), count + 1);
            if (count > this.pscanParams.getMaxAlertsPerRule()) {
                // Disable the plugin
                PassiveScanner scanner = getPassiveScannerList().getScanner(alert.getPluginId());
                if (scanner != null) {
                    logger.info(
                            "Disabling passive scan rule {} as it has raised more than {} alerts.",
                            scanner.getName(),
                            this.pscanParams.getMaxAlertsPerRule());
                    scanner.setEnabled(false);
                }
            }
        }
    }

    /**
     * Adds the given tag to the specified message.
     *
     * @param tag the name of the tag.
     */
    public void addHistoryTag(HistoryReference href, String tag) {
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

    /**
     * Add the History Type ({@code int}) to the set of applicable history types.
     *
     * @param type the type to be added to the set of applicable history types
     */
    public static void addApplicableHistoryType(int type) {
        optedInHistoryTypes.add(type);
    }

    /**
     * Remove the History Type ({@code int}) from the set of applicable history types.
     *
     * @param type the type to be removed from the set of applicable history types
     */
    public static void removeApplicableHistoryType(int type) {
        optedInHistoryTypes.remove(type);
    }

    /**
     * Returns the set of History Types which have "opted-in" to be applicable for passive scanning.
     *
     * @return a set of {@code Integer} representing all of the History Types which have "opted-in"
     *     for passive scanning.
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
     */
    public static Set<Integer> getApplicableHistoryTypes() {
        Set<Integer> allApplicableTypes = new HashSet<>();
        allApplicableTypes.addAll(PluginPassiveScanner.getDefaultHistoryTypes());
        if (!optedInHistoryTypes.isEmpty()) {
            allApplicableTypes.addAll(optedInHistoryTypes);
        }
        return allApplicableTypes;
    }
}

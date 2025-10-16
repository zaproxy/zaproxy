/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
package org.zaproxy.zap.extension.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.parosproxy.paros.core.scanner.Alert;

/**
 * A collection of Alerts with unique alertIds.
 *
 * @since 2.17.0
 */
public class AlertSet {

    private Map<Integer, Alert> map = new ConcurrentHashMap<>();
    private Alert highestRisk;

    public AlertSet() {}

    /**
     * Add the specified alert to the set.
     *
     * @param alert the alert
     * @return true if the alert was not already in the set.
     */
    public synchronized boolean add(Alert alert) {
        boolean added = map.put(alert.getAlertId(), alert) == null;
        highestRisk = getHighestRisk(highestRisk, alert);
        return added;
    }

    /**
     * Adds all of the specified alerts to the set.
     *
     * @param alerts the alerts
     * @return true if one or more of the alerts were not already in the set.
     */
    public boolean addAll(List<Alert> alerts) {
        AtomicBoolean changed = new AtomicBoolean(false);
        alerts.forEach(
                a -> {
                    if (add(a)) {
                        changed.set(true);
                    }
                });
        return changed.get();
    }

    /**
     * Removes the specified alert using the alertId.
     *
     * @param alert the alert to remove
     * @return true if the alert was removed
     */
    public synchronized boolean remove(Alert alert) {
        Alert a = map.remove(alert.getAlertId());
        if (a != null) {
            if (highestRisk != null && a.getAlertId() == highestRisk.getAlertId()) {
                calculateHighestRisk();
            }
            return true;
        }
        return false;
    }

    /**
     * Removes all of the specified alerts.
     *
     * @param alerts the alerts to remove
     * @return true if one or more of the alerts were removed.
     */
    public boolean removeAll(List<Alert> alerts) {
        AtomicBoolean changed = new AtomicBoolean(false);
        alerts.forEach(
                a -> {
                    if (remove(a)) {
                        changed.set(true);
                    }
                });
        return changed.get();
    }

    /** Returns true if the set contains an alert with the given id. */
    public boolean hasAlert(int alertId) {
        return map.containsKey(alertId);
    }

    /**
     * Returns true if the set contains a similar alert, based on the alert properties but using the
     * alert nodeName instead of the URI.
     */
    public boolean hasSimilar(Alert alert) {
        return map.values().stream().anyMatch(a -> a.compareTo(alert) == 0);
    }

    /** Returns one of the alerts. */
    public Alert get() {
        return map.values().stream().findAny().orElse(null);
    }

    /** Returns all of the alerts. */
    public List<Alert> getAll() {
        return map.values().stream().toList();
    }

    /** Returns all of the unique alerts. */
    public List<Alert> getAllUnique() {
        return new ArrayList<>(new TreeSet<>(map.values()));
    }

    /** Clears the set. */
    public void clear() {
        map.clear();
    }

    /**
     * Returns true if the set is empty.
     *
     * @return
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns the alert with the highest risk, or null if there are no non false positive alerts.
     */
    public Alert getHighestRisk() {
        return highestRisk;
    }

    private void calculateHighestRisk() {
        highestRisk = null;
        for (Alert a : map.values()) {
            highestRisk = getHighestRisk(highestRisk, a);
        }
    }

    /**
     * Returns the number of alerts in the set.
     *
     * @return
     */
    public int size() {
        return map.size();
    }

    /**
     * Returns the alert with the highest risk, or null if the alerts are null or false positives.
     *
     * @param a1 the first alert, may be null
     * @param a2 the second alert, must not be null
     * @return the alert with the highest risk, or null
     */
    private static Alert getHighestRisk(Alert a1, Alert a2) {
        if (a2.getConfidence() == Alert.CONFIDENCE_FALSE_POSITIVE) {
            if (a1 != null && a1.getConfidence() == Alert.CONFIDENCE_FALSE_POSITIVE) {
                return null;
            }
            return a1;
        }
        if (a1 == null) {
            return a2;
        }
        return a2.getRisk() > a1.getRisk() ? a2 : a1;
    }

    @Override
    public String toString() {
        return "AlertSet["
                + map.values().stream()
                        .map(Alert::getAlertId)
                        .map(Object::toString)
                        .collect(Collectors.joining(","))
                + "]";
    }
}

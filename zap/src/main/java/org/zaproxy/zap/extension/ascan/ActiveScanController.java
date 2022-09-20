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
package org.zaproxy.zap.extension.ascan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.ascan.filters.ScanFilter;
import org.zaproxy.zap.extension.ruleconfig.ExtensionRuleConfig;
import org.zaproxy.zap.extension.ruleconfig.RuleConfigParam;
import org.zaproxy.zap.extension.script.ScriptCollection;
import org.zaproxy.zap.model.ScanController;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.users.User;

public class ActiveScanController implements ScanController<ActiveScan> {

    private ExtensionActiveScan extension;
    private static final Logger logger = LogManager.getLogger(ActiveScanController.class);

    private ExtensionAlert extAlert = null;

    /**
     * The {@code Lock} for exclusive access of instance variables related to multiple active scans.
     *
     * @see #activeScanMap
     * @see #scanIdCounter
     */
    private final Lock activeScansLock;

    /**
     * The counter used to give an unique ID to active scans.
     *
     * <p><strong>Note:</strong> All accesses (both write and read) should be done while holding the
     * {@code Lock} {@code activeScansLock}.
     *
     * @see #activeScansLock
     * @see #startScan(String, Target, User, Object[])
     */
    private int scanIdCounter;

    /**
     * A map that contains all {@code ActiveScan}s created (and not yet removed). Used to control
     * (i.e. pause/resume and stop) the multiple active scans and get its results. The instance
     * variable is never {@code null}. The map key is the ID of the scan.
     *
     * <p><strong>Note:</strong> All accesses (both write and read) should be done while holding the
     * {@code Lock} {@code activeScansLock}.
     *
     * @see #activeScansLock
     * @see #startScan(String, Target, User, Object[])
     * @see #scanIdCounter
     */
    private Map<Integer, ActiveScan> activeScanMap;

    /**
     * An ordered list of all of the {@code ActiveScan}s created (and not yet removed). Used to get
     * provide the 'last' scan for client using the 'old' API that didn't support concurrent scans.
     */
    private List<ActiveScan> activeScanList;

    public ActiveScanController(ExtensionActiveScan extension) {
        this.activeScansLock = new ReentrantLock();
        this.extension = extension;
        this.activeScanMap = new HashMap<>();
        this.activeScanList = new ArrayList<>();
    }

    public void setExtAlert(ExtensionAlert extAlert) {
        this.extAlert = extAlert;
    }

    @Override
    public int startScan(String name, Target target, User user, Object[] contextSpecificObjects) {
        activeScansLock.lock();
        try {
            int id = this.scanIdCounter++;

            RuleConfigParam ruleConfigParam = null;
            ExtensionRuleConfig extRC =
                    Control.getSingleton()
                            .getExtensionLoader()
                            .getExtension(ExtensionRuleConfig.class);
            if (extRC != null) {
                ruleConfigParam = extRC.getRuleConfigParam();
            }

            ActiveScan ascan =
                    new ActiveScan(name, extension.getScannerParam(), null, ruleConfigParam) {
                        @Override
                        public void alertFound(Alert alert) {
                            alert.setSource(Alert.Source.ACTIVE);
                            if (extAlert != null) {
                                extAlert.alertFound(alert, null);
                            }
                            super.alertFound(alert);
                        }
                    };

            Session session = extension.getModel().getSession();
            List<String> excludeList = new ArrayList<>();
            excludeList.addAll(extension.getExcludeList());
            excludeList.addAll(session.getExcludeFromScanRegexs());
            excludeList.addAll(session.getGlobalExcludeURLRegexs());
            ascan.setExcludeList(excludeList);
            ScanPolicy policy = null;

            ascan.setId(id);
            ascan.setUser(user);

            boolean techOverridden = false;

            if (contextSpecificObjects != null) {
                for (Object obj : contextSpecificObjects) {
                    if (obj instanceof ScannerParam) {
                        logger.debug("Setting custom scanner params");
                        ascan.setScannerParam((ScannerParam) obj);
                    } else if (obj instanceof ScanPolicy) {
                        policy = (ScanPolicy) obj;
                        logger.debug("Setting custom policy {}", policy.getName());
                        ascan.setScanPolicy(policy);
                    } else if (obj instanceof TechSet) {
                        ascan.setTechSet((TechSet) obj);
                        techOverridden = true;
                    } else if (obj instanceof ScriptCollection) {
                        ascan.addScriptCollection((ScriptCollection) obj);
                    } else if (obj instanceof ScanFilter) {
                        ascan.addScanFilter((ScanFilter) obj);
                    } else {
                        logger.error(
                                "Unexpected contextSpecificObject: {}",
                                obj.getClass().getCanonicalName());
                    }
                }
            }
            if (policy == null) {
                // use the default
                policy = extension.getPolicyManager().getDefaultScanPolicy();
                logger.debug("Setting default policy {}", policy.getName());
                ascan.setScanPolicy(policy);
            }

            if (!techOverridden && target.getContext() != null) {
                ascan.setTechSet(target.getContext().getTechSet());
            }

            this.activeScanMap.put(id, ascan);
            this.activeScanList.add(ascan);
            ascan.start(target);

            return id;
        } finally {
            activeScansLock.unlock();
        }
    }

    public int registerScan(ActiveScan ascan) {
        activeScansLock.lock();
        try {
            int id = this.scanIdCounter++;
            ascan.setScanId(id);
            this.activeScanMap.put(id, ascan);
            this.activeScanList.add(ascan);
            return id;
        } finally {
            activeScansLock.unlock();
        }
    }

    @Override
    public ActiveScan getScan(int id) {
        return this.activeScanMap.get(id);
    }

    @Override
    public ActiveScan getLastScan() {
        activeScansLock.lock();
        try {
            if (activeScanList.isEmpty()) {
                return null;
            }
            return activeScanList.get(activeScanList.size() - 1);
        } finally {
            activeScansLock.unlock();
        }
    }

    @Override
    public List<ActiveScan> getAllScans() {
        List<ActiveScan> list = new ArrayList<>();
        activeScansLock.lock();
        try {
            for (ActiveScan scan : activeScanList) {
                list.add(scan);
            }
            return list;
        } finally {
            activeScansLock.unlock();
        }
    }

    @Override
    public List<ActiveScan> getActiveScans() {
        List<ActiveScan> list = new ArrayList<>();
        activeScansLock.lock();
        try {
            for (ActiveScan scan : activeScanList) {
                if (!scan.isStopped()) {
                    list.add(scan);
                }
            }
            return list;
        } finally {
            activeScansLock.unlock();
        }
    }

    @Override
    public ActiveScan removeScan(int id) {
        activeScansLock.lock();

        try {
            ActiveScan ascan = this.activeScanMap.get(id);
            if (!activeScanMap.containsKey(id)) {
                // throw new IllegalArgumentException("Unknown id " + id);
                return null;
            }
            ascan.stopScan();
            activeScanMap.remove(id);
            activeScanList.remove(ascan);
            return ascan;
        } finally {
            activeScansLock.unlock();
        }
    }

    public int getTotalNumberScans() {
        return activeScanMap.size();
    }

    @Override
    public void stopAllScans() {
        activeScansLock.lock();
        try {
            for (ActiveScan scan : activeScanMap.values()) {
                scan.stopScan();
            }
        } finally {
            activeScansLock.unlock();
        }
    }

    @Override
    public void pauseAllScans() {
        activeScansLock.lock();
        try {
            for (ActiveScan scan : activeScanMap.values()) {
                scan.pauseScan();
            }
        } finally {
            activeScansLock.unlock();
        }
    }

    @Override
    public void resumeAllScans() {
        activeScansLock.lock();
        try {
            for (ActiveScan scan : activeScanMap.values()) {
                scan.resumeScan();
            }
        } finally {
            activeScansLock.unlock();
        }
    }

    @Override
    public int removeAllScans() {
        activeScansLock.lock();
        try {
            int count = 0;
            for (Iterator<ActiveScan> it = activeScanMap.values().iterator(); it.hasNext(); ) {
                ActiveScan ascan = it.next();
                ascan.stopScan();
                it.remove();
                activeScanList.remove(ascan);
                count++;
            }
            return count;
        } finally {
            activeScansLock.unlock();
        }
    }

    @Override
    public int removeFinishedScans() {
        activeScansLock.lock();
        try {
            int count = 0;
            for (Iterator<ActiveScan> it = activeScanMap.values().iterator(); it.hasNext(); ) {
                ActiveScan ascan = it.next();
                if (ascan.isStopped()) {
                    ascan.stopScan();
                    it.remove();
                    activeScanList.remove(ascan);
                    count++;
                }
            }
            return count;
        } finally {
            activeScansLock.unlock();
        }
    }

    @Override
    public void stopScan(int id) {
        activeScansLock.lock();
        try {
            if (this.activeScanMap.containsKey(id)) {
                this.activeScanMap.get(id).stopScan();
            }
        } finally {
            activeScansLock.unlock();
        }
    }

    @Override
    public void pauseScan(int id) {
        activeScansLock.lock();
        try {
            if (this.activeScanMap.containsKey(id)) {
                this.activeScanMap.get(id).pauseScan();
            }
        } finally {
            activeScansLock.unlock();
        }
    }

    @Override
    public void resumeScan(int id) {
        activeScansLock.lock();
        try {
            if (this.activeScanMap.containsKey(id)) {
                this.activeScanMap.get(id).resumeScan();
            }
        } finally {
            activeScansLock.unlock();
        }
    }

    public void reset() {
        this.removeAllScans();
        activeScansLock.lock();
        try {
            this.scanIdCounter = 0;
        } finally {
            activeScansLock.unlock();
        }
    }
}

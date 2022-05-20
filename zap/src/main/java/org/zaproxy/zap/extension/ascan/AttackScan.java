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

import org.parosproxy.paros.core.scanner.ScannerParam;
import org.zaproxy.zap.extension.ascan.AttackModeScanner.AttackModeScannerThread;
import org.zaproxy.zap.extension.ruleconfig.RuleConfigParam;
import org.zaproxy.zap.model.Target;

/**
 * A 'dummy' ActiveScan class just used for recording the Attack mode scan results.
 *
 * @author simon
 */
public class AttackScan extends ActiveScan {

    private final AttackModeScannerThread attackModeScannerThread;

    @SuppressWarnings("deprecation")
    public AttackScan(
            String displayName,
            ScannerParam scannerParam,
            org.parosproxy.paros.network.ConnectionParam param,
            ScanPolicy scanPolicy,
            RuleConfigParam ruleConfigParam) {
        this(displayName, scannerParam, scanPolicy, ruleConfigParam, null);
    }

    AttackScan(
            String displayName,
            ScannerParam scannerParam,
            ScanPolicy scanPolicy,
            RuleConfigParam ruleConfigParam,
            AttackModeScannerThread attackModeScannerThread) {
        super(displayName, scannerParam, scanPolicy, ruleConfigParam);
        this.attackModeScannerThread = attackModeScannerThread;
    }

    @Override
    public void start(Target target) {
        // Do nothing
    }

    @Override
    public void stopScan() {}

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public int getMaximum() {
        return 100;
    }

    @Override
    public void pauseScan() {
        // Do nothing
    }

    @Override
    public void resumeScan() {
        // Do nothing
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    boolean isDone() {
        if (attackModeScannerThread == null) {
            return false;
        }
        return !attackModeScannerThread.isRunning() || !attackModeScannerThread.isActive();
    }
}

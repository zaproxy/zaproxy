/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.control;

/**
 * A callback that receives updates of the uninstallation progress of an {@code AddOn}.
 *
 * @since 2.4.0
 * @see AddOn
 */
public interface AddOnUninstallationProgressCallback {

    /**
     * Called before uninstalling the add-on.
     *
     * @param addOn the add-on that will be uninstalled
     * @param updating {@code true} if the add-on is being uninstalled to install a new version,
     *     {@code false} otherwise
     */
    void uninstallingAddOn(AddOn addOn, boolean updating);

    /**
     * Called before removing the active scanners of the add-on.
     *
     * @param numberOfRules the number of active scanners that will be removed
     */
    void activeScanRulesWillBeRemoved(int numberOfRules);

    /**
     * Called after an active scanner has been removed.
     *
     * @param name the name of the active scanner that was removed
     * @see org.parosproxy.paros.core.scanner.Plugin#getName()
     */
    void activeScanRuleRemoved(String name);

    /**
     * Called before removing the passive scanners of the add-on.
     *
     * @param numberOfRules the number of passive scanners that will be removed
     */
    void passiveScanRulesWillBeRemoved(int numberOfRules);

    /**
     * Called after a passive scanner has been removed.
     *
     * @param name the name of the passive scanner that was removed
     * @see org.zaproxy.zap.extension.pscan.PluginPassiveScanner#getName()
     */
    void passiveScanRuleRemoved(String name);

    /**
     * Called before removing the files of the add-on.
     *
     * @param numberOfFiles the number of files that will be removed
     */
    void filesWillBeRemoved(int numberOfFiles);

    /** Called after a file has been removed. */
    void fileRemoved();

    /**
     * Called before removing the extensions of the add-on.
     *
     * @param numberOfExtensions the number of extensions that will be removed
     */
    void extensionsWillBeRemoved(int numberOfExtensions);

    /**
     * Called after an extension has been removed.
     *
     * @param name the (UI) name of the extension that was removed
     * @see org.parosproxy.paros.extension.Extension#getUIName()
     */
    void extensionRemoved(String name);

    /**
     * Called after uninstalling the add-on.
     *
     * @param uninstalled {@code true} if the add-on was successfully uninstalled , {@code false}
     *     otherwise
     */
    void addOnUninstalled(boolean uninstalled);
}

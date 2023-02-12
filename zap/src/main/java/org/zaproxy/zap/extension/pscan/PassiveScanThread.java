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

import java.util.Set;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.model.HistoryReference;

/**
 * @deprecated (2.12.0) Use {@link org.zaproxy.zap.extension.pscan.PassiveScanController} instead.
 */
@Deprecated
public class PassiveScanThread extends Thread {

    public static final int PROXY_LISTENER_ORDER = ProxyListenerLog.PROXY_LISTENER_ORDER + 1;

    private PassiveScanTaskHelper helper;

    private HistoryReference href = null;

    private String currentRuleName = "";
    private String currentUrl = "";
    private long currentRuleStartTime = 0;

    /**
     * Constructs a {@code PassiveScanThread} with the given data.
     *
     * @param helper the helper class
     * @param href the history reference being acted upon
     * @since 2.12.0
     */
    public PassiveScanThread(PassiveScanTaskHelper helper, HistoryReference href) {
        if (helper == null) {
            throw new IllegalArgumentException("Parameter helper must not be null.");
        }

        this.helper = helper;
        this.href = href;
    }

    @Override
    public void run() {
        // Do nothing
    }

    public void raiseAlert(int id, Alert alert) {
        helper.raiseAlert(href, alert);
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
        helper.addHistoryTag(href, tag);
    }
    /**
     * Add the History Type ({@code int}) to the set of applicable history types.
     *
     * @param type the type to be added to the set of applicable history types
     * @since 2.8.0
     */
    public static void addApplicableHistoryType(int type) {
        PassiveScanTaskHelper.addApplicableHistoryType(type);
    }

    /**
     * Remove the History Type ({@code int}) from the set of applicable history types.
     *
     * @param type the type to be removed from the set of applicable history types
     * @since 2.8.0
     */
    public static void removeApplicableHistoryType(int type) {
        PassiveScanTaskHelper.removeApplicableHistoryType(type);
    }

    /**
     * Returns the set of History Types which have "opted-in" to be applicable for passive scanning.
     *
     * @return a set of {@code Integer} representing all of the History Types which have "opted-in"
     *     for passive scanning.
     * @since 2.8.0
     */
    public static Set<Integer> getOptedInHistoryTypes() {
        return PassiveScanTaskHelper.getOptedInHistoryTypes();
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
        return PassiveScanTaskHelper.getApplicableHistoryTypes();
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

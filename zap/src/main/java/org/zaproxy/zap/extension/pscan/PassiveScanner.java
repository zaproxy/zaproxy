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

import net.htmlparser.jericho.Source;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.network.HttpMessage;

public interface PassiveScanner {

    default void scanHttpRequestSend(HttpMessage msg, int id) {}

    default void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {}

    @SuppressWarnings("deprecation")
    default void setParent(PassiveScanThread parent) {}

    String getName();

    void setEnabled(boolean enabled);

    boolean isEnabled();

    /**
     * Gets the alert threshold of the scanner.
     *
     * <p>Default implementation returns always {@link AlertThreshold#MEDIUM}.
     *
     * @return the alert threshold of the scanner.
     * @deprecated (2.7.0) No longer used, the {@code AlertThreshold} is only needed for/by {@link
     *     PluginPassiveScanner}.
     */
    @Deprecated
    default AlertThreshold getLevel() {
        return AlertThreshold.MEDIUM;
    }

    /**
     * Sets the alert threshold of the scanner.
     *
     * <p>Default implementation does nothing.
     *
     * @param level the new alert threshold.
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @deprecated (2.7.0) No longer used, the {@code AlertThreshold} is only needed for/by {@link
     *     PluginPassiveScanner}.
     */
    @Deprecated
    default void setLevel(AlertThreshold level) {}

    boolean appliesToHistoryType(int historyType);

    default void setTaskHelper(PassiveScanTaskHelper helper) {}

    default PassiveScanTaskHelper getTaskHelper() {
        return null;
    }
}

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
package org.zaproxy.zap.extension.anticsrf;

import java.util.List;
import net.htmlparser.jericho.Source;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanTaskHelper;
import org.zaproxy.zap.extension.pscan.PassiveScanner;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.utils.Stats;

/**
 * A {@code PassiveScanner} that {@link ExtensionAntiCSRF#getTokensFromResponse(HttpMessage,Source)
 * extracts} and {@link ExtensionAntiCSRF#registerAntiCsrfToken(AntiCsrfToken) registers} anti-csrf
 * tokens found in {@link PluginPassiveScanner#getDefaultHistoryTypes() HTTP messages}.
 *
 * <p>Additionally, the messages with anti-csrf tokens are tagged with {@link
 * ExtensionAntiCSRF#TAG}.
 *
 * @since 1.3.0
 */
public class AntiCsrfDetectScanner implements PassiveScanner {

    public static final String ACSRF_STATS_PREFIX = "stats.acsrf.";

    private final ExtensionAntiCSRF extAntiCSRF;
    private PassiveScanTaskHelper helper;

    public AntiCsrfDetectScanner(ExtensionAntiCSRF extAntiCSRF) {
        this.extAntiCSRF = extAntiCSRF;
    }

    @Override
    public void setTaskHelper(PassiveScanTaskHelper helper) {
        this.helper = helper;
    }

    @Override
    public void scanHttpRequestSend(HttpMessage msg, int id) {
        // Ignore
    }

    @Override
    public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
        List<AntiCsrfToken> list = extAntiCSRF.getTokensFromResponse(msg, source);
        for (AntiCsrfToken token : list) {
            if (this.registerToken(msg.getHistoryRef().getHistoryType())) {
                if (helper != null) {
                    helper.addHistoryTag(msg.getHistoryRef(), ExtensionAntiCSRF.TAG);
                }
                extAntiCSRF.registerAntiCsrfToken(token);
            }
            // Always record stats
            try {
                Stats.incCounter(
                        SessionStructure.getHostName(msg), ACSRF_STATS_PREFIX + token.getName());
            } catch (URIException e) {
                // Ignore
            }
        }
    }

    @Override
    public String getName() {
        return "Anti CSRF Token Detection";
    }

    @Override
    public boolean isEnabled() {
        // Always enabled
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Ignore
    }

    private boolean registerToken(int historyType) {
        return PluginPassiveScanner.getDefaultHistoryTypes().contains(historyType);
    }

    @Override
    public boolean appliesToHistoryType(int historyType) {
        return true;
    }
}

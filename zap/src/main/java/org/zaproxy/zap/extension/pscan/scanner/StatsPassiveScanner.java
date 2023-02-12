/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.extension.pscan.scanner;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import net.htmlparser.jericho.Source;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringUtils;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.utils.Stats;

public class StatsPassiveScanner extends PluginPassiveScanner {

    public static final String CODE_STATS_PREFIX = "stats.code.";
    public static final String CONTENT_TYPE_STATS_PREFIX = "stats.contentType.";
    public static final String RESPONSE_TIME_STATS_PREFIX = "stats.responseTime.";

    public StatsPassiveScanner() {}

    @Override
    public String getName() {
        return Constant.messages.getString("pscan.stats.passivescanner.title");
    }

    @Override
    public int getPluginId() {
        return 50003;
    }

    @Override
    public StatsPassiveScanner copy() {
        return new StatsPassiveScanner();
    }

    @Override
    public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
        try {
            String site = SessionStructure.getHostName(msg);
            Stats.incCounter(site, CODE_STATS_PREFIX + msg.getResponseHeader().getStatusCode());
            String contentType = msg.getResponseHeader().getHeader(HttpHeader.CONTENT_TYPE);
            if (contentType != null) {
                Stats.incCounter(
                        site, CONTENT_TYPE_STATS_PREFIX + getContentTypePostfix(contentType));
            }
            // Multiply by 2 so we inc the 'next highest' stat
            Stats.incCounter(
                    site,
                    RESPONSE_TIME_STATS_PREFIX
                            + (Integer.highestOneBit(msg.getTimeElapsedMillis()) * 2));
        } catch (URIException e) {
            // Ignore
        }
    }

    private static String getContentTypePostfix(String contentTypeValue) {
        String[] ctvArray = contentTypeValue.split(";");
        if (ctvArray.length == 1) {
            return ctvArray[0].trim();
        }
        return Arrays.stream(ctvArray)
                .filter(StringUtils::isNotBlank)
                .filter(segment -> !segment.toLowerCase(Locale.ROOT).contains("boundary"))
                .map(String::trim)
                .collect(Collectors.joining("; "));
    }

    @Override
    public boolean appliesToHistoryType(int historyType) {
        return true;
    }
}

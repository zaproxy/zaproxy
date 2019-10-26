/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
package org.zaproxy.zap.extension.ascan.filters.impl;

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.extension.ascan.filters.FilterResult;
import org.zaproxy.zap.model.StructuralNode;

/**
 * ScanFilter implementation to filter based on Confidence Level.
 *
 * @author KSASAN preetkaran20@gmail.com
 */
public class ConfidenceLevelScanFilter extends AbstractScanFilter<String> {

    private static final String FILTER_TYPE = "scan.filter.filterType.ConfidenceLevel";

    @Override
    public FilterResult isFiltered(StructuralNode node) {
        HistoryReference href = node.getHistoryReference();
        if (href != null) {
            List<Alert> alerts = href.getAlerts();
            List<String> confidenceLevel = new ArrayList<>();
            for (Alert alert : alerts) {
                confidenceLevel.add(Alert.MSG_CONFIDENCE[alert.getConfidence()]);
            }
            return this.isFiltered(confidenceLevel);
        } else {
            return FilterResult.NOT_FILTERED;
        }
    }

    @Override
    public String getFilterType() {
        return Constant.messages.getString(FILTER_TYPE);
    }
}

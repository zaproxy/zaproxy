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
package org.zaproxy.zap.scan.filters.impl;

import java.util.List;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.scan.filters.FilterResult;

/** @author KSASAN preetkaran20@gmail.com */
public class TagScanFilter extends AbstractScanFilter<String> {

    private static final String FILTER_TYPE = "scan.filter.filterType.Tag";

    @Override
    public FilterResult isFiltered(StructuralNode node) {
        HistoryReference href = node.getHistoryReference();
        if (href != null) {
            List<String> nodeTags = href.getTags();
            return this.isFiltered(nodeTags);
        } else {
            return FilterResult.FILTERED_RESULT;
        }
    }

    @Override
    public String getFilterType() {
        return Constant.messages.getString(FILTER_TYPE);
    }
}

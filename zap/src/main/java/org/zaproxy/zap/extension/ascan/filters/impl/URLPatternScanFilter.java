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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.extension.ascan.filters.FilterResult;
import org.zaproxy.zap.extension.ascan.filters.GenericFilterUtility;
import org.zaproxy.zap.extension.ascan.filters.ScanFilter;
import org.zaproxy.zap.extension.ascan.filters.UrlPatternFilterData;
import org.zaproxy.zap.model.StructuralNode;

/**
 * ScanFilter implementation for filtering based on Url Patterns.
 *
 * @author KSASAN preetkaran20@gmail.com
 * @since 2.9.0
 */
public class URLPatternScanFilter implements ScanFilter {

    private static final String FILTER_TYPE = "scan.filter.filterType.URLPattern";
    private Set<UrlPatternFilterData> urlPatternFilterDataSet = new LinkedHashSet<>();

    public Set<UrlPatternFilterData> getUrlPatternFilterDataSet() {
        return urlPatternFilterDataSet;
    }

    public void setUrlPatternFilterDataSet(Set<UrlPatternFilterData> urlPatternFilterDataSet) {
        this.urlPatternFilterDataSet = urlPatternFilterDataSet;
    }

    @Override
    public FilterResult isFiltered(StructuralNode node) {
        HistoryReference hRef = node.getHistoryReference();
        if (hRef == null) {
            return FilterResult.NOT_FILTERED;
        }

        for (UrlPatternFilterData urlPatternFilterData : this.urlPatternFilterDataSet) {
            switch (urlPatternFilterData.getFilterCriteria()) {
                case INCLUDE:
                    if (urlPatternFilterData.getUrlPatterns().isEmpty()) {
                        return FilterResult.NOT_FILTERED;
                    }
                    for (Pattern pattern : urlPatternFilterData.getUrlPatterns()) {
                        if (pattern.matcher(hRef.getURI().toString()).matches()) {
                            return FilterResult.NOT_FILTERED;
                        }
                    }
                    return new FilterResult(
                            Constant.messages.getString(
                                    GenericFilterUtility.INCLUDE_FILTER_CRITERIA_MESSAGE_KEY,
                                    new Object[] {
                                        this.getFilterType(), urlPatternFilterData.getUrlPatterns()
                                    }));
                case EXCLUDE:
                    for (Pattern pattern : urlPatternFilterData.getUrlPatterns()) {
                        if (pattern.matcher(hRef.getURI().toString()).matches()) {
                            return new FilterResult(
                                    Constant.messages.getString(
                                            GenericFilterUtility
                                                    .EXCLUDE_FILTER_CRITERIA_MESSAGE_KEY,
                                            new Object[] {
                                                this.getFilterType(), "[" + pattern + "]"
                                            }));
                        }
                    }
                    return FilterResult.NOT_FILTERED;
                default:
                    return FilterResult.NOT_FILTERED;
            }
        }
        return FilterResult.NOT_FILTERED;
    }

    public String getFilterType() {
        return Constant.messages.getString(FILTER_TYPE);
    }
}

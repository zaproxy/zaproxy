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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.ascan.filters.FilterCriteria;
import org.zaproxy.zap.extension.ascan.filters.FilterResult;
import org.zaproxy.zap.extension.ascan.filters.ScanFilter;

/**
 * Abstract ScanFilter for handling generic filter usecases.
 *
 * @author KSASAN preetkaran20@gmail.com
 * @since 2.9.0
 */
public abstract class AbstractGenericScanFilter<T, V> implements ScanFilter {

    public static final String INCLUDE_FILTER_CRITERIA_MESSAGE_KEY =
            "scan.filter.filtercriteria.include";
    public static final String EXCLUDE_FILTER_CRITERIA_MESSAGE_KEY =
            "scan.filter.filtercriteria.exclude";

    private final BiPredicate<Collection<T>, V> matcher;

    private FilterCriteria filterCriteria = FilterCriteria.INCLUDE;

    private Collection<T> filterData = new LinkedHashSet<>();

    public AbstractGenericScanFilter() {
        this((filterData, value) -> filterData.contains((Object) value));
    }

    public AbstractGenericScanFilter(BiPredicate<Collection<T>, V> matcher) {
        this.matcher = matcher;
    }

    public abstract String getFilterType();

    public FilterCriteria getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(FilterCriteria filterCriteria) {
        Objects.requireNonNull(filterCriteria);
        this.filterCriteria = filterCriteria;
    }

    public Collection<T> getFilterData() {
        return filterData;
    }

    public void setFilterData(Collection<T> filterData) {
        Objects.requireNonNull(filterData);
        this.filterData = filterData;
    }

    protected FilterResult isFiltered(Collection<V> values) {
        Objects.requireNonNull(values);

        if (filterData.isEmpty()) {
            return FilterResult.NOT_FILTERED;
        }

        FilterCriteria filterCriteria = this.getFilterCriteria();
        switch (filterCriteria) {
            case INCLUDE:
                if (values.stream().anyMatch(value -> matcher.test(filterData, value))) {
                    return FilterResult.NOT_FILTERED;
                }

                return new FilterResult(
                        Constant.messages.getString(
                                INCLUDE_FILTER_CRITERIA_MESSAGE_KEY,
                                this.getFilterType(),
                                filterData));
            case EXCLUDE:
                for (V value : values) {
                    if (matcher.test(filterData, value)) {
                        return new FilterResult(
                                Constant.messages.getString(
                                        EXCLUDE_FILTER_CRITERIA_MESSAGE_KEY,
                                        this.getFilterType(),
                                        "[" + value + "]"));
                    }
                }
                return FilterResult.NOT_FILTERED;
            default:
                return FilterResult.NOT_FILTERED;
        }
    }

    protected FilterResult isFiltered(V value) {
        Objects.requireNonNull(value);
        Set<V> nodeValues = new LinkedHashSet<>();
        nodeValues.add(value);
        return this.isFiltered(nodeValues);
    }
}

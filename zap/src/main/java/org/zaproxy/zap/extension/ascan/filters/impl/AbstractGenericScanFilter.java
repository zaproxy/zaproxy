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
public abstract class AbstractGenericScanFilter<T> implements ScanFilter {

    public static final String INCLUDE_FILTER_CRITERIA_MESSAGE_KEY =
            "scan.filter.filtercriteria.include";
    public static final String EXCLUDE_FILTER_CRITERIA_MESSAGE_KEY =
            "scan.filter.filtercriteria.exclude";

    private BiPredicate<Collection<T>, T> matcher =
            (genericFilterDataCollection, value) -> {
                return genericFilterDataCollection.contains(value);
            };
    private FilterCriteria filterCriteria = FilterCriteria.INCLUDE;

    private Collection<T> genericFilterDataCollection = new LinkedHashSet<>();

    public abstract String getFilterType();

    public FilterCriteria getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(FilterCriteria filterCriteria) {
        Objects.requireNonNull(filterCriteria);
        this.filterCriteria = filterCriteria;
    }

    public Collection<T> getGenericFilterDataCollection() {
        return genericFilterDataCollection;
    }

    public void setGenericFilterDataCollection(Collection<T> genericFilterDataCollection) {
        this.genericFilterDataCollection.addAll(genericFilterDataCollection);
    }

    public <R extends BiPredicate<Collection<T>, T>> FilterResult isFiltered(
            Collection<T> values, R providedMatcher) {
        FilterCriteria filterCriteria = this.getFilterCriteria();
        if (providedMatcher != null) {
            matcher = providedMatcher;
        }
        switch (filterCriteria) {
            case INCLUDE:
                if (genericFilterDataCollection.isEmpty()) {
                    return FilterResult.NOT_FILTERED;
                }
                if (values.stream()
                        .anyMatch(value -> matcher.test(genericFilterDataCollection, value))) {
                    return FilterResult.NOT_FILTERED;
                }

                return new FilterResult(
                        Constant.messages.getString(
                                INCLUDE_FILTER_CRITERIA_MESSAGE_KEY,
                                this.getFilterType(),
                                genericFilterDataCollection));
            case EXCLUDE:
                for (T value : values) {
                    if (matcher.test(genericFilterDataCollection, value)) {
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

    public <R extends BiPredicate<Collection<T>, T>> FilterResult isFiltered(T value, R matcher) {
        Set<T> nodeValues = new LinkedHashSet<>();
        nodeValues.add(value);
        return this.isFiltered(nodeValues, matcher);
    }
}

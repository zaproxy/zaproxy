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
package org.zaproxy.zap.extension.ascan.filters;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.parosproxy.paros.Constant;

/**
 * Generic Filter Utility Class for handling most of the cases.
 *
 * @author KSASAN preetkaran20@gmail.com
 */
public class GenericFilterUtility {

    public static final String INCLUDE_FILTER_CRITERIA_MESSAGE_KEY =
            "scan.filter.filtercriteria.include";
    public static final String EXCLUDE_FILTER_CRITERIA_MESSAGE_KEY =
            "scan.filter.filtercriteria.exclude";
    public static final String INCLUDE_ALL_FILTER_CRITERIA_MESSAGE_KEY =
            "scan.filter.filtercriteria.includeall";

    /**
     * this Utility is Generic Utility for various criteria's and can be used by any Filter which is
     * based on classes which are implementing hashCode and equals
     *
     * @param <T> any object implementing hashCode and equals method
     * @param genericFilterDataCollection Collection of data provided by setting filter criteria
     * @param nodeValues data associated with the scanned node.
     * @param filterType type of the filter ie Tag/Http Status code/Method etc
     * @return FilterResult contains result after applying filter.
     */
    public static <T> FilterResult isFiltered(
            Collection<GenericFilterData<T>> genericFilterDataCollection,
            Collection<T> nodeValues,
            String filterType) {
        for (GenericFilterData<T> genericFilterData : genericFilterDataCollection) {
            FilterCriteria filterCriteria = genericFilterData.getFilterCriteria();
            switch (filterCriteria) {
                case INCLUDE:
                    if (genericFilterData.getValues().isEmpty()) {
                        return FilterResult.NOT_FILTERED;
                    }
                    for (T value : nodeValues) {
                        if (genericFilterData.getValues().contains(value)) {
                            return FilterResult.NOT_FILTERED;
                        }
                    }

                    return new FilterResult(
                            Constant.messages.getString(
                                    INCLUDE_FILTER_CRITERIA_MESSAGE_KEY,
                                    new Object[] {filterType, genericFilterData.getValues()}));
                case EXCLUDE:
                    for (T value : nodeValues) {
                        if (genericFilterData.getValues().contains(value)) {
                            return new FilterResult(
                                    Constant.messages.getString(
                                            EXCLUDE_FILTER_CRITERIA_MESSAGE_KEY,
                                            new Object[] {filterType, "[" + value + "]"}));
                        }
                    }
                    return FilterResult.NOT_FILTERED;
                case INCLUDE_ALL:
                    boolean isFiltered = nodeValues.containsAll(genericFilterData.getValues());
                    if (!isFiltered) {
                        return new FilterResult(
                                Constant.messages.getString(
                                        INCLUDE_ALL_FILTER_CRITERIA_MESSAGE_KEY,
                                        new Object[] {filterType, genericFilterData.getValues()}));
                    }
                    return FilterResult.NOT_FILTERED;
                default:
                    return FilterResult.NOT_FILTERED;
            }
        }
        return FilterResult.NOT_FILTERED;
    }

    /**
     * @param <T> any object implementing hashCode and equals method
     * @param genericFilterDataCollection Collection of data provided by setting filter criteria
     * @param nodeValue data associated with the scanned node.
     * @param filterType type of the filter ie Tag/Http Status code/Method etc
     * @return FilterResult contains result after applying filter.
     *     <p>this method will call {@code GenericFilterUtility#isFiltered(Collection, Collection,
     *     String)} method internally so basic requirement of equals and hasCode for the Classes
     *     holds same.
     */
    public static <T> FilterResult isFiltered(
            Collection<GenericFilterData<T>> genericFilterDataCollection,
            T nodeValue,
            String filterType) {
        Set<T> nodeValues = new LinkedHashSet<>();
        nodeValues.add(nodeValue);
        return isFiltered(genericFilterDataCollection, nodeValues, filterType);
    }
}

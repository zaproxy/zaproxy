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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.collections.CollectionUtils;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.ascan.filters.impl.AlertScanFilter;
import org.zaproxy.zap.extension.ascan.filters.impl.ConfidenceLevelScanFilter;
import org.zaproxy.zap.extension.ascan.filters.impl.HttpStatusCodeScanFilter;
import org.zaproxy.zap.extension.ascan.filters.impl.MethodScanFilter;
import org.zaproxy.zap.extension.ascan.filters.impl.TagScanFilter;
import org.zaproxy.zap.extension.ascan.filters.impl.URLPatternScanFilter;

/**
 * Generic Filter Utility Class.
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
                default:
                    return FilterResult.NOT_FILTERED;
            }
        }
        return FilterResult.NOT_FILTERED;
    }

    /**
     * this method will call {@code GenericFilterUtility#isFiltered(Collection, Collection, String)}
     * method internally so basic requirement of equals and hasCode for the Classes holds same.
     *
     * @param <T> any object implementing hashCode and equals method
     * @param genericFilterDataCollection Collection of data provided by setting filter criteria
     * @param nodeValue data associated with the scanned node.
     * @param filterType type of the filter ie Tag/Http Status code/Method etc
     * @return FilterResult contains result after applying filter.
     */
    public static <T> FilterResult isFiltered(
            Collection<GenericFilterData<T>> genericFilterDataCollection,
            T nodeValue,
            String filterType) {
        Set<T> nodeValues = new LinkedHashSet<>();
        nodeValues.add(nodeValue);
        return isFiltered(genericFilterDataCollection, nodeValues, filterType);
    }

    /**
     * This method is used to create {@link TagScanFilter} instance using Tag List for Include and
     * Exclude {@code FilterCriteria}
     *
     * @param incTagList
     * @param excTagList
     * @return List of ScanFilter
     */
    public static List<ScanFilter> getTagScanFilters(
            List<String> incTagList, List<String> excTagList) {
        List<ScanFilter> scanFilterList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(incTagList)) {
            GenericFilterData<String> genericFilterData =
                    new GenericFilterData<String>(FilterCriteria.INCLUDE, incTagList);
            TagScanFilter tagScanFilter = new TagScanFilter();
            tagScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(tagScanFilter);
        }

        if (!CollectionUtils.isEmpty(excTagList)) {
            GenericFilterData<String> genericFilterData =
                    new GenericFilterData<String>(FilterCriteria.EXCLUDE, excTagList);
            TagScanFilter tagScanFilter = new TagScanFilter();
            tagScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(tagScanFilter);
        }

        return scanFilterList;
    }

    /**
     * This method is used to create {@link MethodScanFilter} instance using Method List for Include
     * and Exclude {@code FilterCriteria}
     *
     * @param incMethodList
     * @param excMethodList
     * @return List of ScanFilter
     */
    public static List<ScanFilter> getMethodScanFilters(
            List<String> incMethodList, List<String> excMethodList) {
        List<ScanFilter> scanFilterList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(incMethodList)) {
            GenericFilterData<String> genericFilterData =
                    new GenericFilterData<String>(FilterCriteria.INCLUDE, incMethodList);
            MethodScanFilter methodScanFilter = new MethodScanFilter();
            methodScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(methodScanFilter);
        }

        if (!CollectionUtils.isEmpty(excMethodList)) {
            GenericFilterData<String> genericFilterData =
                    new GenericFilterData<String>(FilterCriteria.EXCLUDE, excMethodList);
            MethodScanFilter methodScanFilter = new MethodScanFilter();
            methodScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(methodScanFilter);
        }
        return scanFilterList;
    }

    /**
     * This method is used to create {@link HttpStatusCodeScanFilter} instance using Method List for
     * Include and Exclude {@code FilterCriteria}
     *
     * @param incHttpStatusCode
     * @param excHttpStatusCode
     * @return List of ScanFilter
     */
    public static List<ScanFilter> getStatusCodeScanFilters(
            List<Integer> incHttpStatusCode, List<Integer> excHttpStatusCode) {
        List<ScanFilter> scanFilterList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(incHttpStatusCode)) {
            GenericFilterData<Integer> genericFilterData =
                    new GenericFilterData<Integer>(FilterCriteria.INCLUDE, incHttpStatusCode);
            HttpStatusCodeScanFilter httpStatusCodeScanFilter = new HttpStatusCodeScanFilter();
            httpStatusCodeScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(httpStatusCodeScanFilter);
        }

        if (!CollectionUtils.isEmpty(excHttpStatusCode)) {
            GenericFilterData<Integer> genericFilterData =
                    new GenericFilterData<Integer>(FilterCriteria.EXCLUDE, excHttpStatusCode);
            HttpStatusCodeScanFilter httpStatusCodeScanFilter = new HttpStatusCodeScanFilter();
            httpStatusCodeScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(httpStatusCodeScanFilter);
        }
        return scanFilterList;
    }

    /**
     * This method is used to create {@link URLPatternScanFilter} instance using Method List for
     * Include and Exclude {@code FilterCriteria}
     *
     * @param urlIncPatternList
     * @param urlExcPatternList
     * @return List of ScanFilter
     */
    public static List<ScanFilter> getUrlPatternScanFilters(
            List<Pattern> urlIncPatternList, List<Pattern> urlExcPatternList) {
        List<ScanFilter> scanFilterList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(urlIncPatternList)) {
            UrlPatternFilterData urlPatternFilterData =
                    new UrlPatternFilterData(FilterCriteria.INCLUDE, urlIncPatternList);
            URLPatternScanFilter urlPatternScanFilter = new URLPatternScanFilter();
            urlPatternScanFilter.getUrlPatternFilterDataSet().add(urlPatternFilterData);
            scanFilterList.add(urlPatternScanFilter);
        }

        if (!CollectionUtils.isEmpty(urlExcPatternList)) {
            UrlPatternFilterData urlPatternFilterData =
                    new UrlPatternFilterData(FilterCriteria.EXCLUDE, urlExcPatternList);
            URLPatternScanFilter urlPatternScanFilter = new URLPatternScanFilter();
            urlPatternScanFilter.getUrlPatternFilterDataSet().add(urlPatternFilterData);
            scanFilterList.add(urlPatternScanFilter);
        }
        return scanFilterList;
    }

    /**
     * This method is used to create {@link AlertScanFilter} instance using Method List for Include
     * and Exclude {@code FilterCriteria}
     *
     * @param incAlertList
     * @return List of ScanFilter
     */
    public static List<ScanFilter> getAlertScanFilters(List<String> incAlertList) {
        List<ScanFilter> scanFilterList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(incAlertList)) {
            GenericFilterData<String> genericFilterData =
                    new GenericFilterData<String>(FilterCriteria.INCLUDE, incAlertList);
            AlertScanFilter alertScanFilter = new AlertScanFilter();
            alertScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(alertScanFilter);
        }
        return scanFilterList;
    }

    /**
     * This method is used to create {@link ConfidenceLevelScanFilter} instance using Method List
     * for Include and Exclude {@code FilterCriteria}
     *
     * @param incConfidenceList
     * @return List of ScanFilter
     */
    public static List<ScanFilter> getConfidenceScanFilters(List<String> incConfidenceList) {
        List<ScanFilter> scanFilterList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(incConfidenceList)) {
            GenericFilterData<String> genericFilterData =
                    new GenericFilterData<String>(FilterCriteria.INCLUDE, incConfidenceList);
            ConfidenceLevelScanFilter alertScanFilter = new ConfidenceLevelScanFilter();
            alertScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(alertScanFilter);
        }
        return scanFilterList;
    }
}

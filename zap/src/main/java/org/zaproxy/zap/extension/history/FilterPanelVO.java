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
package org.zaproxy.zap.extension.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.collections.CollectionUtils;
import org.zaproxy.zap.extension.ascan.ActiveScan;
import org.zaproxy.zap.extension.ascan.filters.FilterCriteria;
import org.zaproxy.zap.extension.ascan.filters.GenericFilterData;
import org.zaproxy.zap.extension.ascan.filters.ScanFilter;
import org.zaproxy.zap.extension.ascan.filters.UrlPatternFilterData;
import org.zaproxy.zap.extension.ascan.filters.impl.HttpStatusCodeScanFilter;
import org.zaproxy.zap.extension.ascan.filters.impl.MethodScanFilter;
import org.zaproxy.zap.extension.ascan.filters.impl.TagScanFilter;
import org.zaproxy.zap.extension.ascan.filters.impl.URLPatternScanFilter;

/**
 * This VO is populated by {@link FilterPanel} from Active Scanner and then passed to the {@link
 * ActiveScan} where a Node is Filtered based on these Selections.
 *
 * @author KSASAN preetkaran20@gmail.com
 */
public class FilterPanelVO {

    private List<String> incMethodList = new ArrayList<>();
    private List<String> excMethodList = new ArrayList<>();
    private List<Integer> incCodeList = new ArrayList<>();
    private List<Integer> excCodeList = new ArrayList<>();
    // Need to add Filters for Risk List and Confidence List
    private List<String> riskList = new ArrayList<>();
    private List<String> confidenceList = new ArrayList<>();
    private List<String> incTagList = new ArrayList<>();
    private List<String> excTagList = new ArrayList<>();
    private List<Pattern> urlIncPatternList = new ArrayList<>();
    private List<Pattern> urlExcPatternList = new ArrayList<>();

    public void setIncMethodList(List<String> incMethodList) {
        Objects.requireNonNull(excMethodList, "Include method list should not be null");
        this.incMethodList = incMethodList;
    }

    public void setExcMethodList(List<String> excMethodList) {
        Objects.requireNonNull(excMethodList, "Exclude method list should not be null");
        this.excMethodList = excMethodList;
    }

    public void setIncCodeList(List<Integer> incCodeList) {
        Objects.requireNonNull(incCodeList, "Include code list should not be null");
        this.incCodeList = incCodeList;
    }

    public void setExcCodeList(List<Integer> excCodeList) {
        Objects.requireNonNull(incCodeList, "Exclude code list should not be null");
        this.excCodeList = excCodeList;
    }

    public void setRisks(List<String> risks) {
        riskList.clear();
        riskList.addAll(risks);
    }

    public void setReliabilities(List<String> reliabilities) {
        confidenceList.clear();
        confidenceList.addAll(reliabilities);
    }

    public void reset() {
        this.incMethodList.clear();
        this.excMethodList.clear();
        this.incCodeList.clear();
        this.excCodeList.clear();
        this.excTagList.clear();
        this.incTagList.clear();
        this.riskList.clear();
        this.confidenceList.clear();
    }

    public void setUrlIncPatternList(List<Pattern> urlIncPatternList) {
        Objects.requireNonNull(urlIncPatternList, "Include url list should not be null");
        this.urlIncPatternList = urlIncPatternList;
    }

    public void setUrlExcPatternList(List<Pattern> urlExcPatternList) {
        Objects.requireNonNull(urlExcPatternList, "Exclude url list should not be null");
        this.urlExcPatternList = urlExcPatternList;
    }

    public void setIncTagList(List<String> incTagList) {
        Objects.requireNonNull(incTagList, "Include tag list should not be null");
        this.incTagList = incTagList;
    }

    public void setExcTagList(List<String> excTagList) {
        Objects.requireNonNull(excTagList, "Exclude tag list should not be null");
        this.excTagList = excTagList;
    }

    public List<ScanFilter> getScanFilters() {
        List<ScanFilter> scanFilterList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(this.incTagList)) {
            GenericFilterData<String> genericFilterData =
                    new GenericFilterData<String>(FilterCriteria.INCLUDE, this.incTagList);
            TagScanFilter tagScanFilter = new TagScanFilter();
            tagScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(tagScanFilter);
        }

        if (!CollectionUtils.isEmpty(this.excTagList)) {
            GenericFilterData<String> genericFilterData =
                    new GenericFilterData<String>(FilterCriteria.EXCLUDE, this.excTagList);
            TagScanFilter tagScanFilter = new TagScanFilter();
            tagScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(tagScanFilter);
        }

        if (!CollectionUtils.isEmpty(this.incMethodList)) {
            GenericFilterData<String> genericFilterData =
                    new GenericFilterData<String>(FilterCriteria.INCLUDE, this.incMethodList);
            MethodScanFilter methodScanFilter = new MethodScanFilter();
            methodScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(methodScanFilter);
        }

        if (!CollectionUtils.isEmpty(this.excMethodList)) {
            GenericFilterData<String> genericFilterData =
                    new GenericFilterData<String>(FilterCriteria.EXCLUDE, this.excMethodList);
            MethodScanFilter methodScanFilter = new MethodScanFilter();
            methodScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(methodScanFilter);
        }

        if (!CollectionUtils.isEmpty(this.incCodeList)) {
            GenericFilterData<Integer> genericFilterData =
                    new GenericFilterData<Integer>(FilterCriteria.INCLUDE, this.incCodeList);
            HttpStatusCodeScanFilter httpStatusCodeScanFilter = new HttpStatusCodeScanFilter();
            httpStatusCodeScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(httpStatusCodeScanFilter);
        }

        if (!CollectionUtils.isEmpty(this.excCodeList)) {
            GenericFilterData<Integer> genericFilterData =
                    new GenericFilterData<Integer>(FilterCriteria.EXCLUDE, this.excCodeList);
            HttpStatusCodeScanFilter httpStatusCodeScanFilter = new HttpStatusCodeScanFilter();
            httpStatusCodeScanFilter.getGenericFilterDataCollection().add(genericFilterData);
            scanFilterList.add(httpStatusCodeScanFilter);
        }

        if (!CollectionUtils.isEmpty(this.urlIncPatternList)) {
            UrlPatternFilterData urlPatternFilterData =
                    new UrlPatternFilterData(FilterCriteria.INCLUDE, this.urlIncPatternList);
            URLPatternScanFilter urlPatternScanFilter = new URLPatternScanFilter();
            urlPatternScanFilter.getUrlPatternFilterDataSet().add(urlPatternFilterData);
            scanFilterList.add(urlPatternScanFilter);
        }

        if (!CollectionUtils.isEmpty(this.urlExcPatternList)) {
            UrlPatternFilterData urlPatternFilterData =
                    new UrlPatternFilterData(FilterCriteria.EXCLUDE, this.urlExcPatternList);
            URLPatternScanFilter urlPatternScanFilter = new URLPatternScanFilter();
            urlPatternScanFilter.getUrlPatternFilterDataSet().add(urlPatternFilterData);
            scanFilterList.add(urlPatternScanFilter);
        }

        return scanFilterList;
    }
}

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
package org.zaproxy.zap.extension.ascan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.zaproxy.zap.extension.ascan.filters.GenericFilterUtility;
import org.zaproxy.zap.extension.ascan.filters.ScanFilter;

/**
 * FilterPanelVO is populated by {@link FilterPanel} and then passed to the {@link ActiveScan} where
 * a Node is Filtered based on these Selections.
 *
 * @author KSASAN preetkaran20@gmail.com
 * @since TODO add version
 */
public class FilterPanelVO {

    private List<String> incMethodList = new ArrayList<>();
    private List<String> excMethodList = new ArrayList<>();
    private List<Integer> incCodeList = new ArrayList<>();
    private List<Integer> excCodeList = new ArrayList<>();
    private List<String> alertList = new ArrayList<>();
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

    public void setAlertList(List<String> alerts) {
        Objects.requireNonNull(alerts, "Alert list should not be null");
        alertList.addAll(alerts);
    }

    public void setConfidenceList(List<String> confidenceList) {
        Objects.requireNonNull(confidenceList, "Confidence list should not be null");
        this.confidenceList.addAll(confidenceList);
    }

    public void reset() {
        this.incMethodList.clear();
        this.excMethodList.clear();
        this.incCodeList.clear();
        this.excCodeList.clear();
        this.excTagList.clear();
        this.incTagList.clear();
        this.alertList.clear();
        this.urlIncPatternList.clear();
        this.urlExcPatternList.clear();
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

    public List<String> getIncMethodList() {
        return incMethodList;
    }

    public List<String> getExcMethodList() {
        return excMethodList;
    }

    public List<Integer> getIncCodeList() {
        return incCodeList;
    }

    public List<Integer> getExcCodeList() {
        return excCodeList;
    }

    public List<String> getAlertList() {
        return alertList;
    }

    public List<String> getConfidenceList() {
        return confidenceList;
    }

    public List<String> getIncTagList() {
        return incTagList;
    }

    public List<String> getExcTagList() {
        return excTagList;
    }

    public List<Pattern> getUrlIncPatternList() {
        return urlIncPatternList;
    }

    public List<Pattern> getUrlExcPatternList() {
        return urlExcPatternList;
    }

    public List<ScanFilter> getScanFilters() {
        List<ScanFilter> scanFilterList = new ArrayList<>();
        scanFilterList.addAll(
                GenericFilterUtility.getTagScanFilters(this.getIncTagList(), this.getExcTagList()));
        scanFilterList.addAll(
                GenericFilterUtility.getMethodScanFilters(
                        this.getIncMethodList(), this.getExcMethodList()));
        scanFilterList.addAll(
                GenericFilterUtility.getStatusCodeScanFilters(
                        this.getIncCodeList(), this.getExcCodeList()));
        scanFilterList.addAll(
                GenericFilterUtility.getUrlPatternScanFilters(
                        this.getUrlIncPatternList(), this.getUrlExcPatternList()));
        scanFilterList.addAll(GenericFilterUtility.getAlertScanFilters(this.getAlertList()));
        scanFilterList.addAll(
                GenericFilterUtility.getConfidenceScanFilters(this.getConfidenceList()));

        return scanFilterList;
    }
}

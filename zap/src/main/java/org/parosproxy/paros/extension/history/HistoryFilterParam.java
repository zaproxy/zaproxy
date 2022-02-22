/*
 *  Zed Attack Proxy (ZAP) and its related class files.
 *
 *  ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 *  Copyright 2018 The ZAP Development Team
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.parosproxy.paros.extension.history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.zaproxy.zap.extension.api.ZapApiIgnore;

public class HistoryFilterParam extends AbstractParam {

    private static final Logger logger = Logger.getLogger(HistoryFilterParam.class);

    private static final String FILTER_BASE_KEY = "historyfilter";
    private static final String ALL_FILTERS_KEY = FILTER_BASE_KEY + ".list";
    private static final String FILTER_ENABLED_KEY = "enabled";
    private static final String FILTER_NAME_KEY = "name";
    private static final String FILTER_URL_INC_PATTERN_LIST_KEY = "urlincpatternlist";
    private static final String FILTER_URL_EXC_PATTERN_LIST_KEY = "urlexcpatternlist";
    private static final String FILTER_METHOD_LIST_KEY = "methodlist";
    private static final String FILTER_CODE_LIST_KEY = "codelist";
    private static final String FILTER_CONFIDENCE_LIST_KEY = "confidencelist";
    private static final String FILTER_RISKS_LIST_KEY = "risklist";
    private static final String FILTER_TAG_LIST_KEY = "taglist";
    private static final String FILTER_NOTE_KEY = "note";
    private static final String FILTER_START_TIME_SENT_KEY = "starttimesent";

    private static final String CONFIRM_REMOVE_FILTER_KEY = FILTER_BASE_KEY + ".confirmRemoveToken";

    private List<HistoryFilter> filters = new ArrayList<>();
    private boolean confirmRemoveToken = true;

    public HistoryFilterParam() {
        super();
    }

    @Override
    protected void parse() {
        try {
            List<HierarchicalConfiguration> filtersConfig =
                    ((HierarchicalConfiguration) getConfig()).configurationsAt(ALL_FILTERS_KEY);
            this.filters = new ArrayList<>(filtersConfig.size());
            for (HierarchicalConfiguration filterConfig : filtersConfig) {
                HistoryFilter filter = new HistoryFilter();
                filter.setEnabled(filterConfig.getBoolean(FILTER_ENABLED_KEY, true));
                filter.setName(filterConfig.getString(FILTER_NAME_KEY, ""));
                filter.setUrlIncPatternList(
                        filterConfig.getStringArray(FILTER_URL_INC_PATTERN_LIST_KEY));
                filter.setUrlExcPatternList(
                        filterConfig.getStringArray(FILTER_URL_EXC_PATTERN_LIST_KEY));
                filter.setMethods(
                        asStringList(filterConfig.getStringArray(FILTER_METHOD_LIST_KEY)));
                filter.setCodes(asIntList(filterConfig.getStringArray(FILTER_CODE_LIST_KEY)));
                filter.setReliabilities(
                        asStringList(filterConfig.getStringArray(FILTER_CONFIDENCE_LIST_KEY)));
                filter.setRisks(asStringList(filterConfig.getStringArray(FILTER_RISKS_LIST_KEY)));
                filter.setTags(asStringList(filterConfig.getStringArray(FILTER_TAG_LIST_KEY)));
                filter.setNote(filterConfig.getString(FILTER_NOTE_KEY));
                filter.setStartTimeSentInMs(
                        asOptional(filterConfig.getLong(FILTER_START_TIME_SENT_KEY, null)));
                filters.add(filter);
            }
        } catch (ConversionException e) {
            logger.error("Error while loading the History Filters: " + e.getMessage(), e);
        }

        this.confirmRemoveToken = getBoolean(CONFIRM_REMOVE_FILTER_KEY, true);
    }

    private Optional<Long> asOptional(Long value) {
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    private void saveRules() {
        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_FILTERS_KEY);
        for (int i = 0, size = filters.size(); i < size; ++i) {
            String elementBaseKey = ALL_FILTERS_KEY + "(" + i + ").";
            HistoryFilter filter = filters.get(i);
            getConfig()
                    .setProperty(
                            elementBaseKey + FILTER_ENABLED_KEY,
                            Boolean.valueOf(filter.isEnabled()));
            getConfig().setProperty(elementBaseKey + FILTER_NAME_KEY, filter.getName());
            getConfig()
                    .setProperty(
                            elementBaseKey + FILTER_URL_INC_PATTERN_LIST_KEY,
                            filter.getUrlIncPatternValueList());
            getConfig()
                    .setProperty(
                            elementBaseKey + FILTER_URL_EXC_PATTERN_LIST_KEY,
                            filter.getUrlExcPatternValueList());
            getConfig()
                    .setProperty(
                            elementBaseKey + FILTER_METHOD_LIST_KEY,
                            asStringArrayFromStringList(filter.getMethodList()));
            getConfig()
                    .setProperty(
                            elementBaseKey + FILTER_CODE_LIST_KEY,
                            asStringArrayFromIntList(filter.getCodeList()));
            getConfig()
                    .setProperty(
                            elementBaseKey + FILTER_CONFIDENCE_LIST_KEY,
                            asStringArrayFromStringList(filter.getConfidenceList()));
            getConfig()
                    .setProperty(
                            elementBaseKey + FILTER_RISKS_LIST_KEY,
                            asStringArrayFromStringList(filter.getRiskList()));
            getConfig()
                    .setProperty(
                            elementBaseKey + FILTER_TAG_LIST_KEY,
                            asStringArrayFromStringList(filter.getTagList()));
            getConfig().setProperty(elementBaseKey + FILTER_NOTE_KEY, filter.getNote());
            if (filter.getStartTimeSentInMs().isPresent()) {
                getConfig()
                        .setProperty(
                                elementBaseKey + FILTER_START_TIME_SENT_KEY,
                                filter.getStartTimeSentInMs().get());
            }
        }

        getConfig().setProperty(CONFIRM_REMOVE_FILTER_KEY, this.confirmRemoveToken);
    }

    private String[] asStringArrayFromIntList(List<Integer> values) {
        return asStringArrayFromStringList(
                values.stream().map(v -> v.toString()).collect(Collectors.toList()));
    }

    private String[] asStringArrayFromStringList(List<String> values) {
        return values.toArray(new String[] {});
    }

    private List<Integer> asIntList(String[] stringArray) {
        return asList(stringArray, s -> Integer.parseInt(s));
    }

    private List<String> asStringList(String[] stringArray) {
        return asList(stringArray, s -> s);
    }

    private <T> List<T> asList(
            String[] stringArray, java.util.function.Function<String, T> convert) {
        return Arrays.stream(stringArray).map(s -> convert.apply(s)).collect(Collectors.toList());
    }

    public List<HistoryFilter> getFilters() {
        return filters;
    }

    public List<String> getFilterNames() {
        return getFilters().stream().map(f -> f.getName()).collect(Collectors.toList());
    }

    public List<String> getEnabledFilterNames() {
        return getFilters().stream()
                .filter(f -> f.isEnabled())
                .map(f -> f.getName())
                .collect(Collectors.toList());
    }

    public void setFilters(List<HistoryFilter> rules) {
        this.filters = new ArrayList<>(rules);
        saveRules();
    }

    public HistoryFilter getFilter(String name) {
        for (HistoryFilter filter : filters) {
            if (StringUtils.equals(filter.getName(), name)) {
                return filter;
            }
        }
        return null;
    }

    public boolean setEnabled(String name, boolean enabled) {
        HistoryFilter rule = this.getFilter(name);
        if (rule != null) {
            rule.setEnabled(enabled);
            this.saveRules();
            return true;
        }
        return false;
    }

    public void addFilter(HistoryFilter filter) {
        if (StringUtils.isBlank(filter.getName())) {
            throw new IllegalArgumentException("Name not set");
        }

        if (getFilterNames().contains(filter.getName())) {
            throw new IllegalArgumentException("Name already exists");
        }

        this.filters.add(filter);
        this.saveRules();
    }

    public boolean removeFilter(String name) {
        HistoryFilter filter = this.getFilter(name);
        if (filter != null) {
            this.filters.remove(filter);
            this.saveRules();
            return true;
        }
        return false;
    }

    @ZapApiIgnore
    public boolean isConfirmRemoveToken() {
        return this.confirmRemoveToken;
    }

    @ZapApiIgnore
    public void setConfirmRemoveToken(boolean confirmRemove) {
        this.confirmRemoveToken = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_FILTER_KEY, Boolean.valueOf(confirmRemoveToken));
    }
}

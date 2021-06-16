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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.ascan.filters.FilterCriteria;
import org.zaproxy.zap.extension.ascan.filters.FilterResult;
import org.zaproxy.zap.model.StructuralNode;

/** @author KSASAN preetkaran20@gmail.com */
class GenericFilterUtilityTest extends WithConfigsTest {

    private AbstractGenericScanFilter<String, String> abstractGenericScanFilter;

    @BeforeEach
    void init() {
        abstractGenericScanFilter =
                new AbstractGenericScanFilter<String, String>() {

                    @Override
                    public FilterResult isFiltered(StructuralNode node) {
                        return null;
                    }

                    @Override
                    public String getFilterType() {
                        return null;
                    }
                };
    }

    @Test
    void testEmptyFilterValuesIncludeCriteria() {
        // Given
        List<String> genericFilterData = new ArrayList<>();
        abstractGenericScanFilter.setFilterData(genericFilterData);

        Set<String> values = new HashSet<>();
        values.add("Dummy");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(values);

        // Then
        assertThat(filterResult.isFiltered(), is(false));
    }

    @Test
    void testIncludeCriteriaWithSameValues() {
        // Given
        List<String> genericFilterData = new ArrayList<>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setFilterData(genericFilterData);

        Set<String> values = new HashSet<>();
        values.add("Dummy");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(values);

        // Then
        assertThat(filterResult.isFiltered(), is(false));
    }

    @Test
    void testIncludeCriteriaWithMoreFilterValues() {
        // Given
        List<String> genericFilterData = new ArrayList<>();
        genericFilterData.add("Dummy");
        genericFilterData.add("Dummy1");

        abstractGenericScanFilter.setFilterData(genericFilterData);

        Set<String> values = new HashSet<>();
        values.add("Dummy");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(values);

        // Then
        assertThat(filterResult.isFiltered(), is(false));
    }

    @Test
    void testIncludeCriteriaWithMoreValues() {
        // Given
        List<String> genericFilterData = new ArrayList<>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setFilterData(genericFilterData);
        Set<String> values = new HashSet<>();
        values.add("Dummy");
        values.add("Dummy1");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(values);

        // Then
        assertThat(filterResult.isFiltered(), is(false));
    }

    @Test
    void testShouldFailWhenValuesAndFilterDataValuesMismatchIncludeCriteria() {
        // Given
        List<String> genericFilterData = new ArrayList<>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setFilterData(genericFilterData);

        Set<String> values = new HashSet<>();
        values.add("Dummy1");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(values);

        // Then
        assertThat(filterResult.isFiltered(), is(true));
    }

    @Test
    void testEmptyValuesInExcludeCriteria() {
        // Given
        List<String> genericFilterData = new ArrayList<>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setFilterData(genericFilterData);
        abstractGenericScanFilter.setFilterCriteria(FilterCriteria.EXCLUDE);

        Set<String> values = new HashSet<>();

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(values);

        // Then
        assertThat(filterResult.isFiltered(), is(false));
    }

    @Test
    void testShouldFailWhenSameValuesInExcludeCriteria() {
        // Given
        List<String> genericFilterData = new ArrayList<>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setFilterData(genericFilterData);
        abstractGenericScanFilter.setFilterCriteria(FilterCriteria.EXCLUDE);

        Set<String> values = new HashSet<>();
        values.add("Dummy");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(values);

        // Then
        assertThat(filterResult.isFiltered(), is(true));
    }

    @Test
    void testShouldFailInExcludeCriteriaWithMoreFilterValues() {
        // Given
        List<String> genericFilterData = new ArrayList<>();
        genericFilterData.add("Dummy");
        genericFilterData.add("Dummy1");

        abstractGenericScanFilter.setFilterData(genericFilterData);
        abstractGenericScanFilter.setFilterCriteria(FilterCriteria.EXCLUDE);

        Set<String> values = new HashSet<>();
        values.add("Dummy");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(values);

        // Then
        assertThat(filterResult.isFiltered(), is(true));
    }

    @Test
    void testShouldFailWhenExcludeCriteriaWithMoreValues() {
        // Given
        List<String> genericFilterData = new ArrayList<>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setFilterData(genericFilterData);
        abstractGenericScanFilter.setFilterCriteria(FilterCriteria.EXCLUDE);

        Set<String> values = new HashSet<>();
        values.add("Dummy");
        values.add("Dummy1");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(values);

        // Then
        assertThat(filterResult.isFiltered(), is(true));
    }

    @Test
    void testShouldPassForValuesAndFilterValuesMismatchInExcludeCriteria() {
        // Given
        List<String> genericFilterData = new ArrayList<>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setFilterData(genericFilterData);
        abstractGenericScanFilter.setFilterCriteria(FilterCriteria.EXCLUDE);

        Set<String> values = new HashSet<>();
        values.add("Dummy1");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(values);

        // Then
        assertThat(filterResult.isFiltered(), is(false));
    }
}

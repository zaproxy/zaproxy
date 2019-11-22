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
package org.zaproxy.zap.scan.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.ascan.filters.FilterCriteria;
import org.zaproxy.zap.extension.ascan.filters.FilterResult;
import org.zaproxy.zap.extension.ascan.filters.impl.AbstractGenericScanFilter;
import org.zaproxy.zap.model.StructuralNode;

/** @author KSASAN preetkaran20@gmail.com */
public class GenericFilterUtilityTest extends WithConfigsTest {

    private AbstractGenericScanFilter<String> abstractGenericScanFilter;

    @Before
    public void init() {
        abstractGenericScanFilter =
                new AbstractGenericScanFilter<String>() {

                    @Override
                    public FilterResult isFiltered(StructuralNode node) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String getFilterType() {
                        // TODO Auto-generated method stub
                        return null;
                    }
                };
    }

    @Test
    public void testEmptyNodeValuesAndFilterValuesIncludeCriteria() {
        // Given
        List<String> genericFilterData = new ArrayList<String>();
        abstractGenericScanFilter.setGenericFilterDataCollection(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(nodeValues, null);

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }

    @Test
    public void testEmptyNodeValuesIncludeCriteria() {
        // Given
        List<String> genericFilterData = new ArrayList<String>();
        abstractGenericScanFilter.setGenericFilterDataCollection(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(nodeValues, null);

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }

    @Test
    public void testNodeValuesIncludeCriteriaWithSameValues() {
        // Given
        List<String> genericFilterData = new ArrayList<String>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setGenericFilterDataCollection(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(nodeValues, null);

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }

    @Test
    public void testNodeValuesIncludeCriteriaMoreFilterValues() {
        // Given
        List<String> genericFilterData = new ArrayList<String>();
        genericFilterData.add("Dummy");
        genericFilterData.add("Dummy1");

        abstractGenericScanFilter.setGenericFilterDataCollection(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(nodeValues, null);

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }

    @Test
    public void testNodeValuesIncludeCriteriaMoreNodeValues() {
        // Given
        List<String> genericFilterData = new ArrayList<String>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setGenericFilterDataCollection(genericFilterData);
        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");
        nodeValues.add("Dummy1");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(nodeValues, null);

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }

    @Test
    public void testShouldFailNodeValuesFilterValuesMismatchIncludeCriteria() {
        // Given
        List<String> genericFilterData = new ArrayList<String>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setGenericFilterDataCollection(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy1");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(nodeValues, null);

        // Then
        Assert.assertEquals(filterResult.isFiltered(), true);
    }

    @Test
    public void testEmptyNodeValuesExcludeCriteria() {
        // Given
        List<String> genericFilterData = new ArrayList<String>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setGenericFilterDataCollection(genericFilterData);
        abstractGenericScanFilter.setFilterCriteria(FilterCriteria.EXCLUDE);

        Set<String> nodeValues = new HashSet<>();

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(nodeValues, null);

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }

    @Test
    public void testShouldFailNodeValuesExcludeCriteriaWithSameValues() {
        // Given
        List<String> genericFilterData = new ArrayList<String>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setGenericFilterDataCollection(genericFilterData);
        abstractGenericScanFilter.setFilterCriteria(FilterCriteria.EXCLUDE);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(nodeValues, null);

        // Then
        Assert.assertEquals(filterResult.isFiltered(), true);
    }

    @Test
    public void testShouldFailNodeValuesExcludeCriteriaWithMoreFilterValues() {
        // Given
        List<String> genericFilterData = new ArrayList<String>();
        genericFilterData.add("Dummy");
        genericFilterData.add("Dummy1");

        abstractGenericScanFilter.setGenericFilterDataCollection(genericFilterData);
        abstractGenericScanFilter.setFilterCriteria(FilterCriteria.EXCLUDE);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(nodeValues, null);

        // Then
        Assert.assertEquals(filterResult.isFiltered(), true);
    }

    @Test
    public void testShouldFailNodeValuesExcludeCriteriaWithMoreNodeValues() {
        // Given
        List<String> genericFilterData = new ArrayList<String>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setGenericFilterDataCollection(genericFilterData);
        abstractGenericScanFilter.setFilterCriteria(FilterCriteria.EXCLUDE);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");
        nodeValues.add("Dummy1");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(nodeValues, null);

        // Then
        Assert.assertEquals(filterResult.isFiltered(), true);
    }

    @Test
    public void testShouldPassForNodeValuesAndFilterValuesMismatchExcludeCriteria() {
        // Given
        List<String> genericFilterData = new ArrayList<String>();
        genericFilterData.add("Dummy");

        abstractGenericScanFilter.setGenericFilterDataCollection(genericFilterData);
        abstractGenericScanFilter.setFilterCriteria(FilterCriteria.EXCLUDE);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy1");

        // When
        FilterResult filterResult = abstractGenericScanFilter.isFiltered(nodeValues, null);

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }
}

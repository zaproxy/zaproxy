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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.zaproxy.zap.WithConfigsTest;

/** @author KSASAN preetkaran20@gmail.com */
public class GenericFilterUtilityTest extends WithConfigsTest {

    @Test
    public void testEmptyNodeValuesAndFilterValuesIncludeCriteria() {
        // Given
        GenericFilterData<String> genericFilterData = new GenericFilterData<String>();
        genericFilterData.setFilterCriteria(FilterCriteria.INCLUDE);

        Collection<GenericFilterData<String>> genericFilterDataCollection = new LinkedHashSet<>();
        genericFilterDataCollection.add(genericFilterData);

        Set<String> nodeValues = new HashSet<>();

        // When
        FilterResult filterResult =
                GenericFilterUtility.isFiltered(genericFilterDataCollection, nodeValues, "Filter");

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }

    @Test
    public void testEmptyNodeValuesIncludeCriteria() {
        // Given
        GenericFilterData<String> genericFilterData = new GenericFilterData<String>();
        genericFilterData.setFilterCriteria(FilterCriteria.INCLUDE);

        Collection<GenericFilterData<String>> genericFilterDataCollection = new LinkedHashSet<>();
        genericFilterDataCollection.add(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");

        // When
        FilterResult filterResult =
                GenericFilterUtility.isFiltered(genericFilterDataCollection, nodeValues, "Filter");

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }

    @Test
    public void testNodeValuesIncludeCriteriaWithSameValues() {
        // Given
        GenericFilterData<String> genericFilterData = new GenericFilterData<String>();
        genericFilterData.setFilterCriteria(FilterCriteria.INCLUDE);
        genericFilterData.getValues().add("Dummy");

        Collection<GenericFilterData<String>> genericFilterDataCollection = new LinkedHashSet<>();
        genericFilterDataCollection.add(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");

        // When
        FilterResult filterResult =
                GenericFilterUtility.isFiltered(genericFilterDataCollection, nodeValues, "Filter");

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }

    @Test
    public void testNodeValuesIncludeCriteriaMoreFilterValues() {
        // Given
        GenericFilterData<String> genericFilterData = new GenericFilterData<String>();
        genericFilterData.setFilterCriteria(FilterCriteria.INCLUDE);
        genericFilterData.getValues().add("Dummy");
        genericFilterData.getValues().add("Dummy1");

        Collection<GenericFilterData<String>> genericFilterDataCollection = new LinkedHashSet<>();
        genericFilterDataCollection.add(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");

        // When
        FilterResult filterResult =
                GenericFilterUtility.isFiltered(genericFilterDataCollection, nodeValues, "Filter");

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }

    @Test
    public void testNodeValuesIncludeCriteriaMoreNodeValues() {
        // Given
        GenericFilterData<String> genericFilterData = new GenericFilterData<String>();
        genericFilterData.setFilterCriteria(FilterCriteria.INCLUDE);
        genericFilterData.getValues().add("Dummy");

        Collection<GenericFilterData<String>> genericFilterDataCollection = new LinkedHashSet<>();
        genericFilterDataCollection.add(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");
        nodeValues.add("Dummy1");

        // When
        FilterResult filterResult =
                GenericFilterUtility.isFiltered(genericFilterDataCollection, nodeValues, "Filter");

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }

    @Test
    public void testShouldFailNodeValuesFilterValuesMismatchIncludeCriteria() {
        // Given
        GenericFilterData<String> genericFilterData = new GenericFilterData<String>();
        genericFilterData.setFilterCriteria(FilterCriteria.INCLUDE);
        genericFilterData.getValues().add("Dummy");

        Collection<GenericFilterData<String>> genericFilterDataCollection = new LinkedHashSet<>();
        genericFilterDataCollection.add(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy1");

        // When
        FilterResult filterResult =
                GenericFilterUtility.isFiltered(genericFilterDataCollection, nodeValues, "Filter");

        // Then
        Assert.assertEquals(filterResult.isFiltered(), true);
    }

    @Test
    public void testEmptyNodeValuesExcludeCriteria() {
        // Given
        GenericFilterData<String> genericFilterData = new GenericFilterData<String>();
        genericFilterData.setFilterCriteria(FilterCriteria.EXCLUDE);

        Collection<GenericFilterData<String>> genericFilterDataCollection = new LinkedHashSet<>();
        genericFilterDataCollection.add(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");

        // When
        FilterResult filterResult =
                GenericFilterUtility.isFiltered(genericFilterDataCollection, nodeValues, "Filter");

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }

    @Test
    public void testShouldFailNodeValuesExcludeCriteriaWithSameValues() {
        // Given
        GenericFilterData<String> genericFilterData = new GenericFilterData<String>();
        genericFilterData.setFilterCriteria(FilterCriteria.EXCLUDE);
        genericFilterData.getValues().add("Dummy");

        Collection<GenericFilterData<String>> genericFilterDataCollection = new LinkedHashSet<>();
        genericFilterDataCollection.add(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");

        // When
        FilterResult filterResult =
                GenericFilterUtility.isFiltered(genericFilterDataCollection, nodeValues, "Filter");

        // Then
        Assert.assertEquals(filterResult.isFiltered(), true);
    }

    @Test
    public void testShouldFailNodeValuesExcludeCriteriaWithMoreFilterValues() {
        // Given
        GenericFilterData<String> genericFilterData = new GenericFilterData<String>();
        genericFilterData.setFilterCriteria(FilterCriteria.EXCLUDE);
        genericFilterData.getValues().add("Dummy");
        genericFilterData.getValues().add("Dummy1");

        Collection<GenericFilterData<String>> genericFilterDataCollection = new LinkedHashSet<>();
        genericFilterDataCollection.add(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");

        // When
        FilterResult filterResult =
                GenericFilterUtility.isFiltered(genericFilterDataCollection, nodeValues, "Filter");

        // Then
        Assert.assertEquals(filterResult.isFiltered(), true);
    }

    @Test
    public void testShouldFailNodeValuesExcludeCriteriaWithMoreNodeValues() {
        // Given
        GenericFilterData<String> genericFilterData = new GenericFilterData<String>();
        genericFilterData.setFilterCriteria(FilterCriteria.EXCLUDE);
        genericFilterData.getValues().add("Dummy");

        Collection<GenericFilterData<String>> genericFilterDataCollection = new LinkedHashSet<>();
        genericFilterDataCollection.add(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy");
        nodeValues.add("Dummy1");
        // When
        FilterResult filterResult =
                GenericFilterUtility.isFiltered(genericFilterDataCollection, nodeValues, "Filter");

        // Then
        Assert.assertEquals(filterResult.isFiltered(), true);
    }

    @Test
    public void testShouldPassForNodeValuesAndFilterValuesMismatchExcludeCriteria() {
        // Given
        GenericFilterData<String> genericFilterData = new GenericFilterData<String>();
        genericFilterData.setFilterCriteria(FilterCriteria.EXCLUDE);
        genericFilterData.getValues().add("Dummy");

        Collection<GenericFilterData<String>> genericFilterDataCollection = new LinkedHashSet<>();
        genericFilterDataCollection.add(genericFilterData);

        Set<String> nodeValues = new HashSet<>();
        nodeValues.add("Dummy1");
        // When
        FilterResult filterResult =
                GenericFilterUtility.isFiltered(genericFilterDataCollection, nodeValues, "Filter");

        // Then
        Assert.assertEquals(filterResult.isFiltered(), false);
    }
}

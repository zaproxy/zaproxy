package org.zaproxy.zap.scan.filters;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.parosproxy.paros.Constant;

/**
 * @author KSASAN preetkaran20@gmail.com
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class GenericFilterUtilityTest {

	@BeforeClass
	public static void init() {
		/** Used to initialize Messages */
		Constant constant = new Constant();
	}

	@Test
	public void testEmptyNodeValuesAndFilterValuesIncludeCriteria() {
		// Given
		GenericFilterBean<String> genericFilterBean = new GenericFilterBean<String>();
		genericFilterBean.setFilterCriteria(FilterCriteria.INCLUDE);

		Collection<GenericFilterBean<String>> genericFilterBeans = new LinkedHashSet<>();
		genericFilterBeans.add(genericFilterBean);

		Set<String> nodeValues = new HashSet<>();

		// When
		FilterResult filterResult = GenericFilterUtility.isFiltered(genericFilterBeans, nodeValues, "Filter");

		// Then
		Assert.assertEquals(filterResult.isFiltered(), true);
	}
	
	@Test
	public void testEmptyNodeValuesIncludeCriteria() {
		// Given
		GenericFilterBean<String> genericFilterBean = new GenericFilterBean<String>();
		genericFilterBean.setFilterCriteria(FilterCriteria.INCLUDE);

		Collection<GenericFilterBean<String>> genericFilterBeans = new LinkedHashSet<>();
		genericFilterBeans.add(genericFilterBean);

		Set<String> nodeValues = new HashSet<>();
		nodeValues.add("Dummy");

		// When
		FilterResult filterResult = GenericFilterUtility.isFiltered(genericFilterBeans, nodeValues, "Filter");

		// Then
		Assert.assertEquals(filterResult.isFiltered(), true);
	}

	@Test
	public void testNodeValuesIncludeCriteriaWithSameValues() {
		// Given
		GenericFilterBean<String> genericFilterBean = new GenericFilterBean<String>();
		genericFilterBean.setFilterCriteria(FilterCriteria.INCLUDE);
		genericFilterBean.getValues().add("Dummy");

		Collection<GenericFilterBean<String>> genericFilterBeans = new LinkedHashSet<>();
		genericFilterBeans.add(genericFilterBean);

		Set<String> nodeValues = new HashSet<>();
		nodeValues.add("Dummy");

		// When
		FilterResult filterResult = GenericFilterUtility.isFiltered(genericFilterBeans, nodeValues, "Filter");

		// Then
		Assert.assertEquals(filterResult.isFiltered(), true);
	}

	@Test
	public void testNodeValuesIncludeCriteriaMoreFilterValues() {
		// Given
		GenericFilterBean<String> genericFilterBean = new GenericFilterBean<String>();
		genericFilterBean.setFilterCriteria(FilterCriteria.INCLUDE);
		genericFilterBean.getValues().add("Dummy");
		genericFilterBean.getValues().add("Dummy1");

		Collection<GenericFilterBean<String>> genericFilterBeans = new LinkedHashSet<>();
		genericFilterBeans.add(genericFilterBean);

		Set<String> nodeValues = new HashSet<>();
		nodeValues.add("Dummy");

		// When
		FilterResult filterResult = GenericFilterUtility.isFiltered(genericFilterBeans, nodeValues, "Filter");

		// Then
		Assert.assertEquals(filterResult.isFiltered(), true);
	}

	@Test
	public void testNodeValuesIncludeCriteriaMoreNodeValues() {
		// Given
		GenericFilterBean<String> genericFilterBean = new GenericFilterBean<String>();
		genericFilterBean.setFilterCriteria(FilterCriteria.INCLUDE);
		genericFilterBean.getValues().add("Dummy");

		Collection<GenericFilterBean<String>> genericFilterBeans = new LinkedHashSet<>();
		genericFilterBeans.add(genericFilterBean);

		Set<String> nodeValues = new HashSet<>();
		nodeValues.add("Dummy");
		nodeValues.add("Dummy1");

		// When
		FilterResult filterResult = GenericFilterUtility.isFiltered(genericFilterBeans, nodeValues, "Filter");

		// Then
		Assert.assertEquals(filterResult.isFiltered(), true);
	}
	
	@Test
	public void testShouldFailNodeValuesFilterValuesMismatchIncludeCriteria() {
		// Given
		GenericFilterBean<String> genericFilterBean = new GenericFilterBean<String>();
		genericFilterBean.setFilterCriteria(FilterCriteria.INCLUDE);
		genericFilterBean.getValues().add("Dummy");

		Collection<GenericFilterBean<String>> genericFilterBeans = new LinkedHashSet<>();
		genericFilterBeans.add(genericFilterBean);

		Set<String> nodeValues = new HashSet<>();
		nodeValues.add("Dummy1");

		// When
		FilterResult filterResult = GenericFilterUtility.isFiltered(genericFilterBeans, nodeValues, "Filter");

		// Then
		Assert.assertEquals(filterResult.isFiltered(), false);
	}

	@Test
	public void testEmptyNodeValuesExcludeCriteria() {
		// Given
		GenericFilterBean<String> genericFilterBean = new GenericFilterBean<String>();
		genericFilterBean.setFilterCriteria(FilterCriteria.EXCLUDE);

		Collection<GenericFilterBean<String>> genericFilterBeans = new LinkedHashSet<>();
		genericFilterBeans.add(genericFilterBean);

		Set<String> nodeValues = new HashSet<>();
		nodeValues.add("Dummy");

		// When
		FilterResult filterResult = GenericFilterUtility.isFiltered(genericFilterBeans, nodeValues, "Filter");

		// Then
		Assert.assertEquals(filterResult.isFiltered(), true);
	}

	@Test
	public void testShouldFailNodeValuesExcludeCriteriaWithSameValues() {
		// Given
		GenericFilterBean<String> genericFilterBean = new GenericFilterBean<String>();
		genericFilterBean.setFilterCriteria(FilterCriteria.EXCLUDE);
		genericFilterBean.getValues().add("Dummy");

		Collection<GenericFilterBean<String>> genericFilterBeans = new LinkedHashSet<>();
		genericFilterBeans.add(genericFilterBean);

		Set<String> nodeValues = new HashSet<>();
		nodeValues.add("Dummy");

		// When
		FilterResult filterResult = GenericFilterUtility.isFiltered(genericFilterBeans, nodeValues, "Filter");

		// Then
		Assert.assertEquals(filterResult.isFiltered(), false);
	}

	@Test
	public void testShouldFailNodeValuesExcludeCriteriaWithMoreFilterValues() {
		// Given
		GenericFilterBean<String> genericFilterBean = new GenericFilterBean<String>();
		genericFilterBean.setFilterCriteria(FilterCriteria.EXCLUDE);
		genericFilterBean.getValues().add("Dummy");
		genericFilterBean.getValues().add("Dummy1");

		Collection<GenericFilterBean<String>> genericFilterBeans = new LinkedHashSet<>();
		genericFilterBeans.add(genericFilterBean);

		Set<String> nodeValues = new HashSet<>();
		nodeValues.add("Dummy");

		// When
		FilterResult filterResult = GenericFilterUtility.isFiltered(genericFilterBeans, nodeValues, "Filter");

		// Then
		Assert.assertEquals(filterResult.isFiltered(), false);
	}

	@Test
	public void testShouldFailNodeValuesExcludeCriteriaWithMoreNodeValues() {
		// Given
		GenericFilterBean<String> genericFilterBean = new GenericFilterBean<String>();
		genericFilterBean.setFilterCriteria(FilterCriteria.EXCLUDE);
		genericFilterBean.getValues().add("Dummy");

		Collection<GenericFilterBean<String>> genericFilterBeans = new LinkedHashSet<>();
		genericFilterBeans.add(genericFilterBean);

		Set<String> nodeValues = new HashSet<>();
		nodeValues.add("Dummy");
		nodeValues.add("Dummy1");
		// When
		FilterResult filterResult = GenericFilterUtility.isFiltered(genericFilterBeans, nodeValues, "Filter");

		// Then
		Assert.assertEquals(filterResult.isFiltered(), false);
	}
	
	@Test
	public void testShouldPassForNodeValuesAndFilterValuesMismatchExcludeCriteria() {
		// Given
		GenericFilterBean<String> genericFilterBean = new GenericFilterBean<String>();
		genericFilterBean.setFilterCriteria(FilterCriteria.EXCLUDE);
		genericFilterBean.getValues().add("Dummy");

		Collection<GenericFilterBean<String>> genericFilterBeans = new LinkedHashSet<>();
		genericFilterBeans.add(genericFilterBean);

		Set<String> nodeValues = new HashSet<>();
		nodeValues.add("Dummy1");
		// When
		FilterResult filterResult = GenericFilterUtility.isFiltered(genericFilterBeans, nodeValues, "Filter");

		// Then
		Assert.assertEquals(filterResult.isFiltered(), true);
	}
}

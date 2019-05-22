package org.parosproxy.paros.core.scanner;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Vector;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class KbUnitTest {

	private static final String TEST_KEY = "key";
	private static final String ANOTHER_KEY = "otherKey";
	private static final Object TEST_OBJECT_1 = new Object();
	private static final Object TEST_OBJECT_2 = new Object();
	private static final Boolean TEST_BOOLEAN = Boolean.TRUE;
	private static final String TEST_STRING = "Test";

	Kb knowledgeBase;

	@Before
	public void setUp() throws Exception {
		knowledgeBase = new Kb();
	}

	@Test
	public void shouldStoreValueForGivenKey() {
		// Given/When
		knowledgeBase.add(TEST_KEY, TEST_OBJECT_1);
		// Then
		assertThat(knowledgeBase.get(TEST_KEY), is(equalTo(TEST_OBJECT_1)));
	}

	@Test
	@Ignore
	public void shouldStoreValueForGivenUriAndKey() {
		fail("Not yet implemented");
	}

	@Test
	public void shouldRetrieveStoredObjectsForGivenKey() {
		// Given/When
		knowledgeBase.add(TEST_KEY, TEST_OBJECT_1);
		knowledgeBase.add(TEST_KEY, TEST_OBJECT_2);
		// Then
		Vector<Object> result = knowledgeBase.getList(TEST_KEY);
		assertThat(result, hasSize(2));
		assertThat(result, contains(TEST_OBJECT_1, TEST_OBJECT_2));
	}

	@Test
	@Ignore
	public void shouldRetrieveStoredObjectsForGivenUriAndKey() {
		fail("Not yet implemented");
	}

	@Test
	public void shouldRetrieveStoredBooleanForGivenKey() {
		// Given/When
		knowledgeBase.add(TEST_KEY, TEST_BOOLEAN);
		// Then
		assertThat(knowledgeBase.getBoolean(TEST_KEY), is(equalTo(TEST_BOOLEAN)));
	}

	@Test
	@Ignore
	public void shouldRetrieveStoredBooleanForGivenUriAndKey() {
		fail("Not yet implemented");
	}

	@Test
	public void shouldRetrieveStoredStringForGivenKey() {
		// Given/When
		knowledgeBase.add(TEST_KEY, TEST_STRING);
		// Then
		assertThat(knowledgeBase.getString(TEST_KEY), is(equalTo(TEST_STRING)));
	}

	@Test
	@Ignore
	public void shouldRetrieveStoredStringForGivenUriAndKey() {
		fail("Not yet implemented");
	}

	@Test
	public void shouldReturnNullWhenGivenKeyHasNoStoredValue() {
		// Given/When
		knowledgeBase.add(TEST_KEY, TEST_OBJECT_1);
		// Then
		assertThat(knowledgeBase.get(ANOTHER_KEY), is(nullValue()));
	}

	@Test
	public void shouldReturnFalseWhenRetrievingNonBooleanValueAsBoolean() {
		// Given/When
		knowledgeBase.add(TEST_KEY, TEST_OBJECT_1);
		// Then
		assertThat(knowledgeBase.getBoolean(TEST_KEY), is(false));
	}

	@Test
	public void shouldReturnNullWhenRetrievingNonStringValueAsString() {
		// Given/When
		knowledgeBase.add(TEST_KEY, TEST_OBJECT_1);
		// Then
		assertThat(knowledgeBase.getString(TEST_KEY), is(nullValue()));
	}

}

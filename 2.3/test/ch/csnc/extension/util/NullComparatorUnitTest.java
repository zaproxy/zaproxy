package ch.csnc.extension.util;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NullComparatorUnitTest {

	private static final int BOTH_EQUAL = 0;

	private NullComparator nullComparator;

	@Before
	public void setUp() throws Exception {
		nullComparator = new NullComparator();
	}

	@Test
	public void shouldConsiderTwoNullValuesAsEqual() {
		assertThat(nullComparator.compare(null, null), is(BOTH_EQUAL));
	}

	// TODO Implement more tests

}

package org.parosproxy.paros.core.scanner;

import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UtilUnitTest {

	@Test
	public void shouldPauseForGivenDuration() {
		// Given
		int intendedPause = 500;
		// When
		long startTime = System.currentTimeMillis();
		Util.sleep(intendedPause);
		long endTime = System.currentTimeMillis();
		double pause = endTime - startTime;
		// Then
		assertThat(pause, is(closeTo(intendedPause, 100d))); // allow 20% variance
	}

}

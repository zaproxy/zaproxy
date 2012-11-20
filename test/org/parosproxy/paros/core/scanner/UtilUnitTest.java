package org.parosproxy.paros.core.scanner;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class UtilUnitTest {

	@Test
	public void shouldPauseForGivenDuration() {
		// Given
		int intendedPause = 100;
		// When
		long startTime = System.currentTimeMillis();
		Util.sleep(intendedPause);
		long endTime = System.currentTimeMillis();
		double pause = endTime - startTime;
		// Then
		assertThat(pause, is(closeTo(intendedPause, 20d))); // allow 20% variance
	}

}

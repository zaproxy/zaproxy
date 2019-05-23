package org.zaproxy.zap.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.parosproxy.paros.model.Session;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link Context}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContextUnitTest {

	@Mock
	Session session;

	private Context context;

	@Before
	public void setUp() throws Exception {
		context = new Context(session, 1);
	}

	@Test
	public void shouldNullUrlNeverBeIncluded() {
		assertThat(context.isIncluded((String)null), is(false));
	}

	@Test
	public void shouldUseIndexAsDefaultName() {
		// Given
		int index = 1010;
		// When
		Context context = new Context(session, index);
		// Then
		assertThat(context.getName(), is(equalTo(String.valueOf(index))));
	}

	@Test(expected = IllegalContextNameException.class)
	public void shouldNotAllowToSetNullName() {
		// Given
		String name = null;
		// When
		context.setName(name);
		// Then = IllegalContextNameException.class
	}

	@Test(expected = IllegalContextNameException.class)
	public void shouldNotAllowToSetAnEmptyName() {
		// Given
		String name = "";
		// When
		context.setName(name);
		// Then = IllegalContextNameException.class
	}

	@Test
	public void shouldSetNonEmptyName() {
		// Given
		String name = "Default Context";
		// When
		context.setName(name);
		// Then
		assertThat(context.getName(), is(equalTo(name)));
	}

	// TODO Implement more tests


}

package org.zaproxy.zap.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.parosproxy.paros.model.Session;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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

	// TODO Implement more tests


}

package org.zaproxy.zap.extension.auth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.reauth.ContextAuth;

import java.util.regex.Pattern;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ContextAuthUnitTest {

	@Mock
	private HttpMessage loginMessage;
	private static final Pattern LOGGED_IN_INDICATION = Pattern.compile("loggedIn");
	private static final Pattern LOGGED_OUT_INDICATION = Pattern.compile("loggedOut");


	private ContextAuth contextAuthenticator;

	@Before
	public void setUp() throws Exception {
		contextAuthenticator = new ContextAuth(0);
	}

	@Test
	public void canAuthenticateWhenLoginMessageAndLoggedInIndicationPatternArePresent() {
		// Given
		contextAuthenticator.setLoginMsg(loginMessage);
		contextAuthenticator.setLoggedInIndicationPattern(LOGGED_IN_INDICATION);
		// When/Then
		assertThat(contextAuthenticator.canAuthenticate(), is(true));
	}

	@Test
	public void canAuthenticateWhenLoginMessageAndLoggedOutIndicationPatternArePresent() {
		// Given
		contextAuthenticator.setLoginMsg(loginMessage);
		contextAuthenticator.setLoggedOutIndicationPattern(LOGGED_OUT_INDICATION);
		// When/Then
		assertThat(contextAuthenticator.canAuthenticate(), is(true));
	}

	@Test
	public void canAuthenticateWhenLoginMessageAndLoggedInAndOutIndicationPatternsArePresent() {
		// Given
		contextAuthenticator.setLoginMsg(loginMessage);
		contextAuthenticator.setLoggedInIndicationPattern(LOGGED_IN_INDICATION);
		contextAuthenticator.setLoggedOutIndicationPattern(LOGGED_OUT_INDICATION);
		// When/Then
		assertThat(contextAuthenticator.canAuthenticate(), is(true));
	}

	@Test
	public void cannotAuthenticateWhenLoginMessageIsNotPresent() {
		// Given
		contextAuthenticator.setLoginMsg(null);
		// When/Then
		assertThat(contextAuthenticator.canAuthenticate(), is(false));
	}
}

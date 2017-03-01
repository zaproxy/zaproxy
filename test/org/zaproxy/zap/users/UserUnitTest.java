package org.zaproxy.zap.users;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.authentication.AuthenticationCredentials;
import org.zaproxy.zap.authentication.AuthenticationMethod;
import org.zaproxy.zap.authentication.AuthenticationMethodType;
import org.zaproxy.zap.extension.authentication.ExtensionAuthentication;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;

@RunWith(MockitoJUnitRunner.class)
public class UserUnitTest {

	private static final String USER_NAME = "username";
	private static int CONTEXT_ID = 23;
	private static ExtensionAuthentication mockedExtension;
	private static AuthenticationMethodType mockedType;
	private static AuthenticationMethod mockedAuthenticationMethod;
	private static Context mockedContext;
	private static AuthenticationCredentials mockedCredentials;
	private static SessionManagementMethod mockedSessionManagementMethod;

	@BeforeClass
	public static void classSetUp() {
		// Make sure something is returned for the encoder
		mockedCredentials = Mockito.mock(AuthenticationCredentials.class);
		when(mockedCredentials.encode(anyString())).thenReturn("[credentials]");

		// Make sure fake identifier is returned and mocked credentials (for encoding)
		mockedType = Mockito.mock(AuthenticationMethodType.class);
		when(mockedType.getUniqueIdentifier()).thenReturn(99);
		when(mockedType.createAuthenticationCredentials()).thenReturn(mockedCredentials);

		// Make sure mocked type is returned (for encoding) and no actual authentication is done
		mockedAuthenticationMethod = Mockito.mock(AuthenticationMethod.class);
		when(mockedAuthenticationMethod.getType()).thenReturn(mockedType);
		when(
				mockedAuthenticationMethod.authenticate((SessionManagementMethod) anyObject(),
						(AuthenticationCredentials) anyObject(), (User) anyObject())).thenReturn(
				Mockito.mock(WebSession.class));

		// Make sure no actual message processing is done
		mockedSessionManagementMethod = Mockito.mock(SessionManagementMethod.class);
		doNothing().when(mockedSessionManagementMethod).processMessageToMatchSession(
				(HttpMessage) anyObject(), (WebSession) anyObject());

		// Make sure mocked session management and authentication methods are returned
		mockedContext = Mockito.mock(Context.class);
		when(mockedContext.getAuthenticationMethod()).thenReturn(mockedAuthenticationMethod);
		when(mockedContext.getSessionManagementMethod()).thenReturn(mockedSessionManagementMethod);

		// Make sure mocked type is returned for identifier
		mockedExtension = Mockito.mock(ExtensionAuthentication.class);
		when(mockedExtension.getAuthenticationMethodTypeForIdentifier(anyInt())).thenReturn(mockedType);
	}

	@Test
	public void shouldEncodeAndDecodeProperly() {
		// Given
		User user = spy(new User(CONTEXT_ID, USER_NAME));
		user.setAuthenticationCredentials(mockedCredentials);
		doReturn(mockedContext).when(user).getContext();
		// When
		String encoded = User.encode(user);
		User result = User.decode(CONTEXT_ID, encoded, mockedExtension);
		// Then
		assertEquals(user.getName(), result.getName());
		assertEquals(user.isEnabled(), result.isEnabled());
		assertEquals(user.getId(), result.getId());
		assertEquals(user.getContextId(), result.getContextId());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldGenerateUniqueIds() {
		// Given
		User u1 = new User(CONTEXT_ID, USER_NAME);
		User u2 = new User(CONTEXT_ID, USER_NAME);
		User u3 = new User(CONTEXT_ID, USER_NAME);
		User u4 = new User(CONTEXT_ID, USER_NAME);
		// When/Then
		assertThat(u1.getId(), not(anyOf(is(u2.getId()), is(u3.getId()), is(u4.getId()))));
		assertThat(u2.getId(), not(anyOf(is(u3.getId()), is(u4.getId()))));
		assertThat(u3.getId(), not(is(u4.getId())));
		User u5 = new User(CONTEXT_ID, USER_NAME, u4.getId() + 5);
		User u6 = new User(CONTEXT_ID, USER_NAME);
		assertThat(u6.getId(), greaterThan(u5.getId()));
	}

	@Test
	public void shouldRequireAuthenticationAfterInitialization() {
		// Given
		User u = new User(CONTEXT_ID, USER_NAME);
		// When/Then
		assertTrue(u.requiresAuthentication());
	}

	@Test
	public void shouldRequireAuthenticationAfterAuthentication() {
		// Given
		User u = new User(CONTEXT_ID, USER_NAME);
		// When
		u.setAuthenticatedSession(Mockito.mock(WebSession.class));
		// Then
		assertFalse(u.requiresAuthentication());
	}

	@Test
	public void shouldRequireAuthenticationAfterResetWithNewerMessage() {
		// Given
		User u = spy(new User(CONTEXT_ID, USER_NAME));
		u.setAuthenticatedSession(Mockito.mock(WebSession.class));
		doReturn(3500l).when(u).getLastSuccessfulAuthTime();
		// When
		HttpMessage msg = Mockito.mock(HttpMessage.class);
		when(msg.getTimeSentMillis()).thenReturn(5000l);
		u.queueAuthentication(msg);
		// Then
		assertTrue(u.requiresAuthentication());
	}

	@Test
	public void shouldRequireAuthenticationAfterResetWithOlderMessage() {
		// Given
		User u = spy(new User(CONTEXT_ID, USER_NAME));
		u.setAuthenticatedSession(Mockito.mock(WebSession.class));
		doReturn(3500l).when(u).getLastSuccessfulAuthTime();
		// When
		HttpMessage msg = Mockito.mock(HttpMessage.class);
		when(msg.getTimeSentMillis()).thenReturn(3200l);
		u.queueAuthentication(msg);
		// Then
		verify(msg).getTimeSentMillis();
		assertFalse(u.requiresAuthentication());
	}

	@Test
	public void shouldAuthenticateWhenRequired() {
		// Given
		User user = spy(new User(CONTEXT_ID, USER_NAME));
		doReturn(mockedContext).when(user).getContext();
		// When
		doReturn(true).when(user).requiresAuthentication();
		doNothing().when(user).authenticate();
		user.processMessageToMatchUser(Mockito.mock(HttpMessage.class));
		// Then
		verify(user).authenticate();
	}

	@Test
	public void shouldNotAuthenticateIfNotRequired() {
		// Given
		User user = spy(new User(CONTEXT_ID, USER_NAME));
		doReturn(mockedContext).when(user).getContext();
		// When
		doReturn(false).when(user).requiresAuthentication();
		doNothing().when(user).authenticate();
		user.processMessageToMatchUser(Mockito.mock(HttpMessage.class));
		// Then
		verify(user, never()).authenticate();
	}

	@Test
	public void shouldNotRequireAuthenticationAfterAuthentication() {
		// Given
		User user = spy(new User(CONTEXT_ID, USER_NAME));
		doReturn(mockedContext).when(user).getContext();
		// When
		user.authenticate();
		// Then
		assertFalse(user.requiresAuthentication());
	}

}

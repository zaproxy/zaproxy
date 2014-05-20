package org.zaproxy.zap.extension.authorization;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.extension.authorization.BasicAuthorizationDetectionMethod.LogicalOperator;
import org.zaproxy.zap.network.HttpResponseBody;

@RunWith(MockitoJUnitRunner.class)
public class BasicAuthorizationDetectionMethodUnitTest {

	private static final String RESPONSE_TARGET_TEXT = "Unauthorized";
	private static final String LOCATION_URL = "http://www.example.com/login";
	private static final int STATUS_CODE = 302;
	private static final String RESPONSE_HEADER = "HTTP/1.1 " + STATUS_CODE + " Found\n"
			+ "Content-Type: text/html; charset=utf-8\n" + "Location: " + LOCATION_URL + "\n"
			+ "Date: Sun, 18 May 2014 16:16:45 GMT\n" + "Server: Google Frontend\n" + "Content-Length: 0\n"
			+ "Alternate-Protocol: 80:quic,80:quic\n";
	private static final String RESPONSE_BODY = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
			+ "Pellentesque auctor nulla id turpis placerat vulputate." + RESPONSE_TARGET_TEXT
			+ " Proin tempor bibendum eros rutrum. ";

	private HttpMessage message;
	private BasicAuthorizationDetectionMethod authorizationMethod;

	@Before
	public void setUp() throws Exception {
		message = Mockito.mock(HttpMessage.class);
		HttpResponseHeader mockedHeader = Mockito.mock(HttpResponseHeader.class);
		HttpResponseBody mockedBody = Mockito.mock(HttpResponseBody.class);
		Mockito.when(message.getResponseHeader()).thenReturn(mockedHeader);
		Mockito.when(message.getResponseBody()).thenReturn(mockedBody);
		Mockito.when(mockedBody.toString()).thenReturn(RESPONSE_BODY);
		Mockito.when(mockedHeader.getStatusCode()).thenReturn(STATUS_CODE);
		Mockito.when(mockedHeader.toString()).thenReturn(RESPONSE_HEADER);
	}

	@Test
	public void shouldNotReturnUnauthorizeWhenNothingIsSetWithAnd() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(null, null, null, LogicalOperator.AND);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
	}

	@Test
	public void shouldNotReturnUnauthorizeWhenNothingIsSetWithOr() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(null, "", "", LogicalOperator.OR);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
	}

	@Test
	public void shouldReturnUnauthorizeWhenJustStatusCodeIsSetWithOr() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(STATUS_CODE, "", "", LogicalOperator.OR);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
	}

	@Test
	public void shouldReturnUnauthorizeWhenJustBodyIsSetWithOr() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(null, "", RESPONSE_TARGET_TEXT,
				LogicalOperator.OR);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
	}

	@Test
	public void shouldReturnUnauthorizeWhenJustHeaderIsSetWithOr() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(null, "Location: " + LOCATION_URL, null,
				LogicalOperator.OR);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
	}

	@Test
	public void shouldNotReturnUnauthorizeWhenJustStatusCodeIsSetWithOr() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(STATUS_CODE + 1, "", "",
				LogicalOperator.OR);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
	}

	@Test
	public void shouldNotReturnUnauthorizeWhenJustBodyIsSetWithOr() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(null, "",
				RESPONSE_TARGET_TEXT + "RANDOM", LogicalOperator.OR);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
	}

	@Test
	public void shouldReturnNotUnauthorizeWhenJustHeaderIsSetWithOr() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(null, "Location: " + LOCATION_URL
				+ "/extra", null, LogicalOperator.OR);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
	}

	@Test
	public void shouldReturnUnauthorizeWhenJustStatusCodeIsSetWithAnd() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(STATUS_CODE, "", "", LogicalOperator.AND);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
	}

	@Test
	public void shouldReturnUnauthorizeWhenJustBodyIsSetWithAnd() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(null, "", RESPONSE_TARGET_TEXT,
				LogicalOperator.AND);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
	}

	@Test
	public void shouldReturnUnauthorizeWhenJustHeaderIsSetWithAnd() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(null, "Location: " + LOCATION_URL, null,
				LogicalOperator.AND);

		// When/Then
		assertEquals(true, authorizationMethod.isResponseForUnauthorizedRequest(message));
	}

	@Test
	public void shouldReturnUnauthorizedWithOr() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(STATUS_CODE + 1, "",
				RESPONSE_TARGET_TEXT, LogicalOperator.OR);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);

		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(STATUS_CODE, null, RESPONSE_TARGET_TEXT
				+ "?TEST", LogicalOperator.OR);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);

		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(STATUS_CODE + 1, LOCATION_URL,
				RESPONSE_TARGET_TEXT + "??", LogicalOperator.OR);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
	}

	@Test
	public void shouldNotReturnUnauthorizedWithOr() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(STATUS_CODE + 1, "", RESPONSE_TARGET_TEXT
				+ "EXTRA", LogicalOperator.OR);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);

		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(STATUS_CODE + 1, "Location: wrongUrl",
				RESPONSE_TARGET_TEXT + "EXTRA", LogicalOperator.OR);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
	}

	@Test
	public void shouldReturnUnauthorizedWithAnd() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(STATUS_CODE, "", RESPONSE_TARGET_TEXT,
				LogicalOperator.AND);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);

		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(STATUS_CODE, LOCATION_URL,
				RESPONSE_TARGET_TEXT, LogicalOperator.AND);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), true);
	}

	@Test
	public void shouldNotReturnUnauthorizedWithAnd() {
		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(STATUS_CODE + 2, null,
				RESPONSE_TARGET_TEXT, LogicalOperator.AND);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);

		// Given
		authorizationMethod = new BasicAuthorizationDetectionMethod(STATUS_CODE, "No Location",
				RESPONSE_TARGET_TEXT, LogicalOperator.AND);

		// When/Then
		assertEquals(authorizationMethod.isResponseForUnauthorizedRequest(message), false);
	}
}

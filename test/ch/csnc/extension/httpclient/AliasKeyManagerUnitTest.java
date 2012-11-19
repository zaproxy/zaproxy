package ch.csnc.extension.httpclient;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test for {@link AliasKeyManager}
 * 
 * @author bjoern.kimminich@gmx.de
 */
public class AliasKeyManagerUnitTest {
	
	private static final String ALIAS = "alias";
	private static final String PASSWORD = "password";
	
	private AliasKeyManager aliasKeyManager;
	
	@Mock
	private KeyStore keyStoreMock;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldAlwaysChooseInitiallyGivenAliasAsClientAlias() {
		// Given
		aliasKeyManager = new AliasKeyManager(keyStoreMock, ALIAS, PASSWORD);
		// When
		String clientAlias = aliasKeyManager.chooseClientAlias(new String[0], new Principal[]{mock(Principal.class)}, mock(Socket.class));
		// Then
		assertThat(clientAlias, is(equalTo(ALIAS)));
	}

	@Test
	public void shouldOnlyReturnInitiallyGivenAliasAsClientAlias() {
		// Given
		aliasKeyManager = new AliasKeyManager(keyStoreMock, ALIAS, PASSWORD);
		// When
		String[] clientAliases = aliasKeyManager.getClientAliases("", new Principal[]{mock(Principal.class)});
		// Then
		assertThat(clientAliases.length, is(1));
		assertThat(clientAliases, hasItemInArray(ALIAS));
	}

	@Test
	public void shouldAlwaysChooseInitiallyGivenAliasAsServerAlias() {
		// Given
		aliasKeyManager = new AliasKeyManager(keyStoreMock, ALIAS, PASSWORD);
		// When
		String serverAlias = aliasKeyManager.chooseServerAlias("", new Principal[]{mock(Principal.class)}, mock(Socket.class));
		// Then
		assertThat(serverAlias, is(equalTo(ALIAS)));
	}
	
	@Test
	public void shouldOnlyReturnInitiallyGivenAliasAsServerAlias() {
		// Given
		aliasKeyManager = new AliasKeyManager(keyStoreMock, ALIAS, PASSWORD);
		// When
		String[] serverAliases = aliasKeyManager.getServerAliases("", new Principal[]{mock(Principal.class)});
		// Then
		assertThat(serverAliases.length, is(1));
		assertThat(serverAliases, hasItemInArray(ALIAS));
	}	

}

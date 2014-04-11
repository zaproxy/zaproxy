package ch.csnc.extension.httpclient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.Socket;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link ch.csnc.extension.httpclient.AliasKeyManager}
 *
 * @author bjoern.kimminich@gmx.de
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(KeyStore.class)
public class AliasKeyManagerUnitTest {

	private static final String ALIAS = "alias";
	private static final String PASSWORD = "password";

	private AliasKeyManager aliasKeyManager;

	private KeyStore keyStore;

	@Mock
	private KeyStoreSpi keyStoreSpi;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		keyStore = PowerMockito.mock(KeyStore.class);
		Whitebox.setInternalState(keyStore, "initialized", true);
		Whitebox.setInternalState(keyStore, "keyStoreSpi", keyStoreSpi);
	}

	@Test
	public void shouldAlwaysChooseInitiallyGivenAliasAsClientAlias() {
		// Given
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		String clientAlias = aliasKeyManager.chooseClientAlias(new String[0],
				new Principal[] { mock(Principal.class) }, mock(Socket.class));
		// Then
		assertThat(clientAlias, is(equalTo(ALIAS)));
	}

	@Test
	public void shouldOnlyReturnInitiallyGivenAliasAsClientAlias() {
		// Given
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		String[] clientAliases = aliasKeyManager.getClientAliases("",
				new Principal[] { mock(Principal.class) });
		// Then
		assertThat(clientAliases.length, is(1));
		assertThat(clientAliases, hasItemInArray(ALIAS));
	}

	@Test
	public void shouldAlwaysChooseInitiallyGivenAliasAsServerAlias() {
		// Given
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		String serverAlias = aliasKeyManager.chooseServerAlias("",
				new Principal[] { mock(Principal.class) }, mock(Socket.class));
		// Then
		assertThat(serverAlias, is(equalTo(ALIAS)));
	}

	@Test
	public void shouldOnlyReturnInitiallyGivenAliasAsServerAlias() {
		// Given
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		String[] serverAliases = aliasKeyManager.getServerAliases("",
				new Principal[] { mock(Principal.class) });
		// Then
		assertThat(serverAliases, is(arrayWithSize(1)));
		assertThat(serverAliases, is(arrayContaining(ALIAS)));
	}

	@Test
	public void shouldReturnNullWhenNoCertificatesAreFound() throws Exception {
		// Given
		given(keyStoreSpi.engineGetCertificateChain(ALIAS)).willReturn(null);
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		X509Certificate[] certificates = aliasKeyManager
				.getCertificateChain(ALIAS);
		// Then
		assertThat(certificates, is(equalTo(null)));
	}

	@Test
	public void shouldReturnCertificatesFromKeyStoreAsX509Certificates()
			throws Exception {
		// Given
		Certificate[] originalCertificates = new Certificate[] {
				mock(X509Certificate.class), mock(X509Certificate.class) };
		given(keyStoreSpi.engineGetCertificateChain(ALIAS)).willReturn(
				originalCertificates);
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		X509Certificate[] certificates = aliasKeyManager
				.getCertificateChain(ALIAS);
		// Then
		assertThat(certificates, is(arrayWithSize(2)));
		assertThat(certificates, arrayContaining(originalCertificates));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnNullAsCertificatesWhenExceptionOccursAccessingKeyStore()
			throws Exception {
		// Given
		given(keyStoreSpi.engineGetCertificateChain(ALIAS)).willThrow(
				KeyStoreException.class);
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		X509Certificate[] certificates = aliasKeyManager
				.getCertificateChain(ALIAS);
		// Then
		assertThat(certificates, is(equalTo(null)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnNullAsKeyWhenExceptionOccursAccessingKeyStore()
			throws Exception {
		// Given
		given(keyStoreSpi.engineGetKey(ALIAS, PASSWORD.toCharArray()))
				.willThrow(KeyStoreException.class,
						NoSuchAlgorithmException.class,
						UnrecoverableKeyException.class);
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When/Then
		assertThat(aliasKeyManager.getPrivateKey(ALIAS), is(equalTo(null))); // KeyStoreExcpeption
		assertThat(aliasKeyManager.getPrivateKey(ALIAS), is(equalTo(null))); // NoSuchAlgorithmException
		assertThat(aliasKeyManager.getPrivateKey(ALIAS), is(equalTo(null))); // UnrecoverableKeyException
	}

	@Test
	public void shouldReturnPrivateKeyFromKeyStore()
			throws Exception {
		// Given
		Key originalKey = mock(PrivateKey.class);
		given(keyStoreSpi.engineGetKey(ALIAS, PASSWORD.toCharArray()))
		.willReturn(originalKey);
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When/Then
		assertThat(aliasKeyManager.getPrivateKey(ALIAS), is(equalTo(originalKey)));
	}

}

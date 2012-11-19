package ch.csnc.extension.httpclient;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit test for {@link AliasKeyManager}
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
		String clientAlias = aliasKeyManager.chooseClientAlias(new String[0], new Principal[]{mock(Principal.class)}, mock(Socket.class));
		// Then
		assertThat(clientAlias, is(equalTo(ALIAS)));
	}

	@Test
	public void shouldOnlyReturnInitiallyGivenAliasAsClientAlias() {
		// Given
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		String[] clientAliases = aliasKeyManager.getClientAliases("", new Principal[]{mock(Principal.class)});
		// Then
		assertThat(clientAliases.length, is(1));
		assertThat(clientAliases, hasItemInArray(ALIAS));
	}

	@Test
	public void shouldAlwaysChooseInitiallyGivenAliasAsServerAlias() {
		// Given
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		String serverAlias = aliasKeyManager.chooseServerAlias("", new Principal[]{mock(Principal.class)}, mock(Socket.class));
		// Then
		assertThat(serverAlias, is(equalTo(ALIAS)));
	}
	
	@Test
	public void shouldOnlyReturnInitiallyGivenAliasAsServerAlias() {
		// Given
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		String[] serverAliases = aliasKeyManager.getServerAliases("", new Principal[]{mock(Principal.class)});
		// Then
		assertThat(serverAliases, is(arrayWithSize(1)));
		assertThat(serverAliases, is(arrayContaining(ALIAS)));
	}	
	
	@Test
	public void shouldReturnNullWhenNoCertificatesAreFound() throws Exception {
		// Given
		doReturn(null).when(keyStoreSpi).engineGetCertificateChain(ALIAS);
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		X509Certificate[] certificates = aliasKeyManager.getCertificateChain(ALIAS);
		// Then
		assertThat(certificates, is(equalTo(null)));
	}		

	@Test
	public void shouldReturnCertificatesFromKeyStoreAsX509Certificates() throws Exception {
		// Given
		Certificate[] originalCertificates = new Certificate[] {mock(X509Certificate.class), mock(X509Certificate.class)};
		doReturn(originalCertificates).when(keyStoreSpi).engineGetCertificateChain(ALIAS);
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		X509Certificate[] certificates = aliasKeyManager.getCertificateChain(ALIAS);
		// Then
		assertThat(certificates, is(arrayWithSize(2)));
		assertThat(certificates, arrayContaining(originalCertificates));
	}

	@Test
	public void shouldReturnNullAsCertificatesWhenExceptionOccursAccessingKeyStore() throws Exception {
		// Given
		doThrow(KeyStoreException.class).when(keyStoreSpi).engineGetCertificateChain(ALIAS);
		aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
		// When
		X509Certificate[] certificates = aliasKeyManager.getCertificateChain(ALIAS);
		// Then
		assertThat(certificates, is(equalTo(null)));
	}		

}

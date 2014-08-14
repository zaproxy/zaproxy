package ch.csnc.extension.httpclient;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SSLContextManagerUnitTest {
	
	private SSLContextManager sslContextManager;
	
	@Before
	public void setUp() throws Exception {
		sslContextManager = new SSLContextManager();
	}

	@Test
	public void shouldReturnAvailabilityOfPKCS11Provider() {
		// Given
		boolean pkcs11ProviderAvailable = true;
		try {
			Class.forName("sun.security.pkcs11.SunPKCS11");
		} catch (ClassNotFoundException e) {
			pkcs11ProviderAvailable = false;			
		}
		// When
		boolean result = sslContextManager.isProviderAvailable("PKCS11");
		// Then
		assertThat(result, is(equalTo(pkcs11ProviderAvailable)));
	}
	
	@Test
	public void shouldReturnAvailabilityOfMsksProvider() {
		// Given
		boolean msks11ProviderAvailable = true;
		try {
			Class.forName("se.assembla.jce.provider.ms.MSProvider");
		} catch (ClassNotFoundException e) {
			msks11ProviderAvailable = false;			
		}
		// When
		boolean result = sslContextManager.isProviderAvailable("msks");
		// Then
		assertThat(result, is(equalTo(msks11ProviderAvailable)));
	}
	
	@Test
	public void shouldAlwaysReturnFalseForOtherThanPKCS11AndMsksProvider() {
		// Given
		// When
		boolean result = sslContextManager.isProviderAvailable("thisProviderDoesNotExist");
		// Then
		assertThat(result, is(false));
	}
	

}

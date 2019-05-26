package ch.csnc.extension.httpclient;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.cert.Certificate;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class AliasCertificateUnitTest {
	
	private AliasCertificate aliasCertificate;
	
	@Mock
	private Certificate certificate;

	@Test
	public void shouldRetrieveCnFromUnderlyingCertificate() {
		// Given
		given(certificate.toString()).willReturn("CN=test\\,certificate,post");
		aliasCertificate = new AliasCertificate(certificate, "");
		// When
		String cn = aliasCertificate.getCN();
		// Then
		assertThat(cn, is("test\\,certificate"));
	}
	
	@Test
	public void shouldMergeCnAndAliasIntoName() {
		// Given
		given(certificate.toString()).willReturn("CN=test\\,certificate,post");
		aliasCertificate = new AliasCertificate(certificate, "alias");
		// When
		String name = aliasCertificate.getName();
		// Then
		assertThat(name, is("test\\,certificate [alias]"));
	}
	
	@Test
	public void shouldReturnNullAsCnOnUnexpectedUnderlyingCertificateString() {
		// Given
		given(certificate.toString()).willReturn("xxx");
		aliasCertificate = new AliasCertificate(certificate, "");
		// When
		String cn = aliasCertificate.getCN();
		// Then
		assertThat(cn, is(nullValue()));
	}	
	
	@Test
	public void shouldFailRetrievingNameOnNullCn() {
		// Given
		given(certificate.toString()).willReturn("xxx");
		aliasCertificate = new AliasCertificate(certificate, "");
		// When
		String name = aliasCertificate.getName();
		// Then
		assertThat(name, is(""));
	}

}

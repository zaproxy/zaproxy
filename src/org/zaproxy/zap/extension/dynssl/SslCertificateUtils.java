/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 mawoki@ymail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.dynssl;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.parosproxy.paros.security.SslCertificateService;

/**
 * @author MaWoKi
 */
public class SslCertificateUtils {

	private static final long DEFAULT_VALID_DAYS = 365L;

	/**
	 * Creates a new Root CA certificate and returns private and public key as
	 * {@link KeyStore}. The {@link KeyStore#getDefaultType()} is used.
	 *
	 * @return
	 * @throws NoSuchAlgorithmException If no providers are found
	 * for 'RSA' key pair generator
	 * or 'SHA1PRNG' Secure random number generator
	 * @throws IllegalStateException in case of errors during assembling {@link KeyStore}
	 */
	public final static KeyStore createRootCA() throws NoSuchAlgorithmException {
		final Date startDate = Calendar.getInstance().getTime();
		final Date expireDate = new Date(startDate.getTime()+ (DEFAULT_VALID_DAYS * 24L * 60L * 60L * 1000L));
		// The Root CA serial number is '1'
		final BigInteger serialNumber = new BigInteger("1");

		final KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
		final SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
		rnd.setSeed(System.currentTimeMillis());
		g.initialize(2048, rnd);
		final KeyPair keypair = g.genKeyPair();

		final X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		// using the hash code of the user's name and home path, keeps anonymity
		// but also gives user a chance to distinguish between each other
		final X500Principal x500principal = new X500Principal(
						"CN = OWASP Zed Attack Proxy Root CA, " +
						"L = " + Integer.toHexString(System.getProperty("user.name").hashCode()) +
								Integer.toHexString(System.getProperty("user.home").hashCode()) + ", " +
						"O = OWASP Root CA, " +
						"OU = OWASP ZAP Root CA, " +
						"C = XX"
				);

		certGen.setSerialNumber(serialNumber);
		certGen.setSubjectDN(x500principal);
		certGen.setIssuerDN(x500principal);
		certGen.setNotBefore(startDate);
		certGen.setNotAfter(expireDate);
		certGen.setPublicKey(keypair.getPublic());
		certGen.setSignatureAlgorithm("SHA1withRSA");

		KeyStore ks = null;
		try {
			certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(keypair.getPublic()));
			certGen.addExtension(X509Extensions.BasicConstraints, false, new BasicConstraints(true));
			final X509Certificate cert = certGen.generate(keypair.getPrivate());
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null, null);
			ks.setKeyEntry(SslCertificateService.ZAPROXY_JKS_ALIAS, keypair.getPrivate(), SslCertificateService.PASSPHRASE, new Certificate[]{cert});
		} catch (final Exception e) {
			throw new IllegalStateException("Errors during assembling root CA.", e);
		}
		return ks;
	}

}

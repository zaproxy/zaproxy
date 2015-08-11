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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
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
	public static final KeyStore createRootCA() throws NoSuchAlgorithmException {
		final Date startDate = Calendar.getInstance().getTime();
		final Date expireDate = new Date(startDate.getTime()+ (DEFAULT_VALID_DAYS * 24L * 60L * 60L * 1000L));

		final KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
		g.initialize(2048, SecureRandom.getInstance("SHA1PRNG"));
		final KeyPair keypair = g.genKeyPair();
		final PrivateKey privKey = keypair.getPrivate();
		final PublicKey  pubKey = keypair.getPublic();
		Security.addProvider(new BouncyCastleProvider());
		Random rnd = new Random();

		// using the hash code of the user's name and home path, keeps anonymity
		// but also gives user a chance to distinguish between each other
		X500NameBuilder namebld = new X500NameBuilder(BCStyle.INSTANCE); 
		namebld.addRDN(BCStyle.CN, "OWASP Zed Attack Proxy Root CA");
		namebld.addRDN(BCStyle.L, Integer.toHexString(System.getProperty("user.name").hashCode())
									+ Integer.toHexString(System.getProperty("user.home").hashCode()));
		namebld.addRDN(BCStyle.O, "OWASP Root CA");
		namebld.addRDN(BCStyle.OU, "OWASP ZAP Root CA");
		namebld.addRDN(BCStyle.C, "xx");
		
		X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder (
												namebld.build(),
												BigInteger.valueOf(rnd.nextInt()),
												startDate,
												expireDate,
												namebld.build(),
												pubKey
											);
		
		KeyStore ks = null;
		try {
			certGen.addExtension(Extension.subjectKeyIdentifier, false, new SubjectKeyIdentifier(pubKey.getEncoded()));
			certGen.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
			certGen.addExtension(Extension.keyUsage, false, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.digitalSignature | KeyUsage.keyEncipherment | KeyUsage.dataEncipherment | KeyUsage.cRLSign));
			
			KeyPurposeId[] eku = {
				KeyPurposeId.id_kp_serverAuth,
				KeyPurposeId.id_kp_clientAuth,
				KeyPurposeId.anyExtendedKeyUsage
			};
			certGen.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(eku));
 
			final ContentSigner sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider("BC").build(privKey);
			final X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certGen.build(sigGen));

			ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null, null);
			ks.setKeyEntry(SslCertificateService.ZAPROXY_JKS_ALIAS, privKey, SslCertificateService.PASSPHRASE, new Certificate[]{cert});
		} catch (final Exception e) {
			throw new IllegalStateException("Errors during assembling root CA.", e);
		}
		return ks;
	}

	/**
	 * @param keystore
	 * @return
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	public static final String keyStore2String(KeyStore keystore) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		keystore.store(baos, SslCertificateService.PASSPHRASE);
		final byte[] bytes = baos.toByteArray();
		baos.close();
		return Base64.encodeBase64URLSafeString(bytes);
	}

	/**
	 * @param str
	 * @return
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	public static final KeyStore string2Keystore(String str) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		final byte[] bytes = Base64.decodeBase64(str);
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(bais, SslCertificateService.PASSPHRASE);
		bais.close();
		return ks;
	}
	
	/**
	 * Code c/o http://stackoverflow.com/questions/12501117/programmatically-obtain-keystore-from-pem
	 * @param pemFile
	 * @return
	 * @throws IOException
	 * @throws CertificateException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 */
	public static KeyStore pem2Keystore(File pemFile) throws IOException, CertificateException, 
			InvalidKeySpecException, NoSuchAlgorithmException, KeyStoreException {
		byte[] certAndKey = FileUtils.readFileToByteArray(pemFile);
	    byte[] certBytes = parseDERFromPEM(certAndKey, "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
	    byte[] keyBytes = parseDERFromPEM(certAndKey, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");

	    X509Certificate cert = generateCertificateFromDER(certBytes);              
	    RSAPrivateKey key  = generatePrivateKeyFromDER(keyBytes);
	    
	    KeyStore keystore = KeyStore.getInstance("JKS");
	    keystore.load(null);
	    keystore.setCertificateEntry("cert-alias", cert);
	    keystore.setKeyEntry(SslCertificateService.ZAPROXY_JKS_ALIAS, key, SslCertificateService.PASSPHRASE, new Certificate[] {cert});
	    return keystore;
	}

	private static byte[] parseDERFromPEM(byte[] pem, String beginDelimiter, String endDelimiter) {
	    String data = new String(pem);
	    String[] tokens = data.split(beginDelimiter);
	    tokens = tokens[1].split(endDelimiter);
	    return DatatypeConverter.parseBase64Binary(tokens[0]);        
	}

	private static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
	    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

	    KeyFactory factory = KeyFactory.getInstance("RSA");

	    return (RSAPrivateKey)factory.generatePrivate(spec);        
	}

	private static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
	    CertificateFactory factory = CertificateFactory.getInstance("X.509");

	    return (X509Certificate)factory.generateCertificate(new ByteArrayInputStream(certBytes));      
	}
}

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
package org.parosproxy.paros.security;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;


/**
 * This is a singleton class. Use {@link #getService()} method to
 * obtain a service bean. This implementation is totally unbuffered and creates
 * every time you call {@link #createCertForHost(String)} a new certificate.
 * If you want to have a cached solution, have a look at {@link CachedSslCertifificateServiceImpl}.
 * This class is designed to be thread safe.
 *
 * @author MaWoKi
 * @see {@link org.bouncycastle.x509.examples.AttrCertExample} how to manage CAs and stuff
 * @see {@link CachedSslCertifificateServiceImpl} for a cached {@link SslCertificateService}
 */
public final class SslCertificateServiceImpl implements SslCertificateService {

	/**
	 * where to find the .JKS file in classpath
	 */
	private static final String RESOURCE_CA = "resource/owasp_zap.jks";
	
	private X509Certificate caCert = null;
	private PublicKey caPubKey = null;
	private PrivateKey caPrivKey = null;

	private long serial = 0;

	private static final SslCertificateService singleton = new SslCertificateServiceImpl();

	private SslCertificateServiceImpl() {
		Security.addProvider(new BouncyCastleProvider());
		final Random rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());
		// prevent browser certificate caches, cause of doubled serial numbers
		// using 48bit random number
		this.serial = ((long)rnd.nextInt()) << 32 | (rnd.nextInt() & 0xFFFFFFFFL);
		// let reserve of 16 bit for increasing, serials have to be positive
		this.serial = this.serial & 0x0000FFFFFFFFFFFFL;
	}

	/**
	 * Loads CA's private key, public key and X.509 certificate into this bean.
	 * This method automatically closes the {@link InputStream};
	 *
	 * @param inputstream
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 */
	private synchronized final void loadCA() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream(RESOURCE_CA);
		if (is == null) {
			throw new FileNotFoundException("Error, couldn't find the root CA key store '"+RESOURCE_CA+"' in classpath!");
		}
		final KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(is, SslCertificateService.PASSPHRASE);
		this.caCert = (X509Certificate)ks.getCertificate(ZAPROXY_JKS_ALIAS);
		this.caPrivKey = (RSAPrivateKey) ks.getKey(ZAPROXY_JKS_ALIAS, SslCertificateService.PASSPHRASE);
		this.caPubKey = this.caCert.getPublicKey();
		is.close();
	}

	/* (non-Javadoc)
	 * @see org.parosproxy.paros.security.SslCertificateService#createCertForHost(java.lang.String)
	 */
    @Override
	public KeyStore createCertForHost(String hostname) throws NoSuchAlgorithmException, InvalidKeyException, CertificateException, NoSuchProviderException, SignatureException, KeyStoreException, IOException, UnrecoverableKeyException {

    	if (hostname == null) {
    		throw new IllegalArgumentException("Error, 'hostname' is not allowed to be null!");
    	}

    	if (this.caCert == null || this.caPrivKey == null || this.caPubKey == null) {
    		// lazy loading
    		loadCA();
    	}

        final KeyPair mykp = this.createKeyPair();
        final PrivateKey privKey = mykp.getPrivate();
        final PublicKey pubKey = mykp.getPublic();

        //
        // subjects name table.
        //
        final Hashtable<Object, String> attrs = new Hashtable<Object, String>();
        final Vector<Object> order = new Vector<Object>();

        attrs.put(X509Name.CN, hostname);
        attrs.put(X509Name.OU, "Zed Attack Proxy Project");
        attrs.put(X509Name.O, "OWASP");
        attrs.put(X509Name.C, "XX");
        attrs.put(X509Name.EmailAddress, "owasp-zed-attack-proxy@lists.owasp.org");

        order.addElement(X509Name.CN);
        order.addElement(X509Name.OU);
        order.addElement(X509Name.O);
        order.addElement(X509Name.C);
        order.addElement(X509Name.EmailAddress);

        //
        // create the certificate - version 3
        //
        final X509V3CertificateGenerator  v3CertGen = new X509V3CertificateGenerator();
        v3CertGen.reset();

        v3CertGen.setSerialNumber(BigInteger.valueOf(getNextSerial()));
        v3CertGen.setIssuerDN(PrincipalUtil.getSubjectX509Principal(caCert));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + 100*(1000L * 60 * 60 * 24 * 30)));
        v3CertGen.setSubjectDN(new X509Principal(order, attrs));
        v3CertGen.setPublicKey(pubKey);
        v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

        //
        // add the extensions
        //
        v3CertGen.addExtension(
            X509Extensions.SubjectKeyIdentifier,
            false,
            new SubjectKeyIdentifierStructure(pubKey));

        v3CertGen.addExtension(
            X509Extensions.AuthorityKeyIdentifier,
            false,
            new AuthorityKeyIdentifierStructure(caCert.getPublicKey()));

        v3CertGen.addExtension(
                X509Extensions.BasicConstraints,
                true,
                new BasicConstraints(0));

//        X509Certificate cert = v3CertGen.generateX509Certificate(caPrivKey);
        final X509Certificate cert = v3CertGen.generate(caPrivKey, "BC");
        cert.checkValidity(new Date());
        cert.verify(caCert.getPublicKey());

        final KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        final Certificate[] chain = new Certificate[2];
        chain[1] = this.caCert;
        chain[0] = cert;
        ks.setKeyEntry(ZAPROXY_JKS_ALIAS, privKey, PASSPHRASE, chain);
        return ks;
    }

	/**
	 * Generates increasing serial numbers, cause if multiple certificates
	 * with different finger prints do have the same serial from the same CA,
	 * the browser gets crazy. At least, Firefox v3.x does.
	 *
	 * @return
	 */
	private synchronized final long getNextSerial() {
		return serial++;
	}

	/**
	 * Generates an 1024 bit RSA key pair using SHA1PRNG.
	 * 
	 * Thoughts: 2048 takes much longer time on older CPUs.
	 * And for almost every client, 1024 is sufficient.
	 *
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private final KeyPair createKeyPair() throws NoSuchAlgorithmException {
		final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		final SecureRandom random  = SecureRandom.getInstance("SHA1PRNG");
		random.setSeed(Long.toString(System.currentTimeMillis()).getBytes());
		keyGen.initialize(1024, random);
		final KeyPair keypair = keyGen.generateKeyPair();
		return keypair;
	}

	/**
	 * @return return the current {@link SslCertificateService}
	 */
	public final static SslCertificateService getService() {
		return singleton;
	}

}

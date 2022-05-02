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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.parosproxy.paros.security;

import java.io.IOException;
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
import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * This is a singleton class. Use {@link #getService()} method to obtain a service bean. This
 * implementation is totally unbuffered and creates every time you call {@link
 * SslCertificateService#createCertForHost(CertData)} a new certificate. If you want to have a
 * cached solution, have a look at {@link CachedSslCertifificateServiceImpl}. This class is designed
 * to be thread safe.
 *
 * <p>A word about serial numbers ... There have to be different serial numbers generated, cause if
 * multiple certificates with different finger prints do have the same serial from the same CA, the
 * browser gets crazy. At least, Firefox v3.x does.
 *
 * @author MaWoKi
 * @see CachedSslCertifificateServiceImpl for a cached SslCertificateService
 */
@Deprecated
public final class SslCertificateServiceImpl implements SslCertificateService {

    /**
     * Constant used to define the start validity date for site certificates. Used as 30d before
     * "now"
     */
    private static final int SITE_CERTIFICATE_START_ADJUSTMENT = 30;
    /**
     * Constant used to define the end validity date for site certificates. 1year minus start
     * adjustment. Per: https://cabforum.org/2017/02/24/ballot-185-limiting-lifetime-certificates/
     * and https://www.ssl.com/blogs/apple-limits-ssl-tls-certificate-lifetimes-to-398-days/
     */
    private static final int SITE_CERTIFICATE_END_VALIDITY_PERIOD =
            398 - SITE_CERTIFICATE_START_ADJUSTMENT;

    private X509Certificate caCert = null;
    private PublicKey caPubKey = null;
    private PrivateKey caPrivKey = null;

    private final AtomicLong serial;

    private static final SslCertificateService singleton = new SslCertificateServiceImpl();

    private SslCertificateServiceImpl() {
        Security.addProvider(new BouncyCastleProvider());
        final Random rnd = new Random();
        rnd.setSeed(System.currentTimeMillis());
        // prevent browser certificate caches, cause of doubled serial numbers
        // using 48bit random number
        long sl = ((long) rnd.nextInt()) << 32 | (rnd.nextInt() & 0xFFFFFFFFL);
        // let reserve of 16 bit for increasing, serials have to be positive
        sl = sl & 0x0000FFFFFFFFFFFFL;
        this.serial = new AtomicLong(sl);
    }

    @Override
    public synchronized void initializeRootCA(KeyStore keystore)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        if (keystore == null) {
            this.caCert = null;
            this.caPrivKey = null;
            this.caPubKey = null;
        } else {
            this.caCert = (X509Certificate) keystore.getCertificate(ZAPROXY_JKS_ALIAS);
            this.caPrivKey =
                    (RSAPrivateKey)
                            keystore.getKey(ZAPROXY_JKS_ALIAS, SslCertificateService.PASSPHRASE);
            this.caPubKey = this.caCert.getPublicKey();
        }
    }

    @Override
    public KeyStore createCertForHost(String hostname)
            throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
                    KeyStoreException, SignatureException, NoSuchProviderException,
                    InvalidKeyException, IOException {
        return createCertForHost(new CertData(hostname));
    }

    @Override
    public KeyStore createCertForHost(CertData certData)
            throws NoSuchAlgorithmException, InvalidKeyException, CertificateException,
                    NoSuchProviderException, SignatureException, KeyStoreException, IOException,
                    UnrecoverableKeyException {

        if (this.caCert == null || this.caPrivKey == null || this.caPubKey == null) {
            throw new MissingRootCertificateException(
                    this.getClass()
                            + " wasn't initialized! Got to options 'Dynamic SSL Certs' and create one.");
        }

        CertData.Name[] certDataNames = certData.getSubjectAlternativeNames();
        GeneralName[] subjectAlternativeNames = new GeneralName[certDataNames.length];
        for (int i = 0; i < certDataNames.length; i++) {
            CertData.Name certDataName = certDataNames[i];
            subjectAlternativeNames[i] =
                    new GeneralName(certDataName.getType(), certDataName.getValue());
        }

        if (certData.getCommonName() == null && subjectAlternativeNames.length == 0) {
            throw new IllegalArgumentException(
                    "commonName is null and no subjectAlternativeNames are specified");
        }

        final KeyPair mykp = this.createKeyPair();
        final PrivateKey privKey = mykp.getPrivate();
        final PublicKey pubKey = mykp.getPublic();

        X500NameBuilder namebld = new X500NameBuilder(BCStyle.INSTANCE);
        if (certData.getCommonName() != null) {
            namebld.addRDN(BCStyle.CN, certData.getCommonName());
        }
        namebld.addRDN(BCStyle.OU, "Zed Attack Proxy Project");
        namebld.addRDN(BCStyle.O, "OWASP");
        namebld.addRDN(BCStyle.C, "xx");
        namebld.addRDN(BCStyle.EmailAddress, "zaproxy-develop@googlegroups.com");

        long currentTime = System.currentTimeMillis();
        X509v3CertificateBuilder certGen =
                new JcaX509v3CertificateBuilder(
                        new X509CertificateHolder(caCert.getEncoded()).getSubject(),
                        BigInteger.valueOf(serial.getAndIncrement()),
                        new Date(
                                currentTime
                                        - Duration.ofDays(SITE_CERTIFICATE_START_ADJUSTMENT)
                                                .toMillis()),
                        new Date(
                                currentTime
                                        + Duration.ofDays(SITE_CERTIFICATE_END_VALIDITY_PERIOD)
                                                .toMillis()),
                        namebld.build(),
                        pubKey);

        certGen.addExtension(
                Extension.subjectKeyIdentifier,
                false,
                new SubjectKeyIdentifier(pubKey.getEncoded()));
        certGen.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
        certGen.addExtension(
                Extension.extendedKeyUsage,
                false,
                new ExtendedKeyUsage(new KeyPurposeId[] {KeyPurposeId.id_kp_serverAuth}));

        if (subjectAlternativeNames.length > 0) {
            certGen.addExtension(
                    Extension.subjectAlternativeName,
                    certData.isSubjectAlternativeNameIsCritical(),
                    new GeneralNames(subjectAlternativeNames));
        }

        ContentSigner sigGen;
        try {
            sigGen =
                    new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                            .setProvider("BC")
                            .build(caPrivKey);
        } catch (OperatorCreationException e) {
            throw new CertificateException(e);
        }
        final X509Certificate cert =
                new JcaX509CertificateConverter()
                        .setProvider("BC")
                        .getCertificate(certGen.build(sigGen));
        cert.checkValidity(new Date());
        cert.verify(caPubKey);

        final KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        final Certificate[] chain = new Certificate[2];
        chain[1] = this.caCert;
        chain[0] = cert;
        ks.setKeyEntry(ZAPROXY_JKS_ALIAS, privKey, PASSPHRASE, chain);
        return ks;
    }

    /**
     * Generates a 2048 bit RSA key pair using SHA1PRNG.
     *
     * @return the key pair
     * @throws NoSuchAlgorithmException if no provider supports the used algorithms.
     */
    private KeyPair createKeyPair() throws NoSuchAlgorithmException {
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(Long.toString(System.currentTimeMillis()).getBytes());
        keyGen.initialize(2048, random);
        final KeyPair keypair = keyGen.generateKeyPair();
        return keypair;
    }

    /** @return return the current {@link SslCertificateService} */
    public static SslCertificateService getService() {
        return singleton;
    }
}

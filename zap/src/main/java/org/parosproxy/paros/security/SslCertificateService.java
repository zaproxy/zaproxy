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
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Create SSL certificates on the fly, signed by Paros Proxy CA.
 *
 * @author MaWoKi
 */
@Deprecated
public interface SslCertificateService {

    /** The passphrase which is used for all Paros Proxy SSL crypto stuff */
    char[] PASSPHRASE = "0w45P.Z4p".toCharArray();
    /** The alias name used in key stores. */
    String ZAPROXY_JKS_ALIAS = "owasp_zap_root_ca";

    /**
     * Generate a certificate signed by our CA's intermediate certificate. Thy certificate, private
     * key and public key are returned in one {@link KeyStore} available with alias {@link
     * #ZAPROXY_JKS_ALIAS}.
     *
     * @param hostname
     * @return a {@link KeyStore} which contains root certificate, signed certificate, private key
     *     and public key of signed certificate
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws CertificateException
     * @throws NoSuchProviderException
     * @throws SignatureException
     * @throws KeyStoreException
     * @throws IOException
     * @throws UnrecoverableKeyException
     * @throws MissingRootCertificateException when it wasn't initialized.
     */
    KeyStore createCertForHost(String hostname)
            throws NoSuchAlgorithmException, InvalidKeyException, CertificateException,
                    NoSuchProviderException, SignatureException, KeyStoreException, IOException,
                    UnrecoverableKeyException;

    /**
     * Generate a certificate signed by our CA's intermediate certificate. Thy certificate, private
     * key and public key are returned in one {@link KeyStore} available with alias {@link
     * #ZAPROXY_JKS_ALIAS}.
     *
     * @param certData
     * @return a {@link KeyStore} which contains root certificate, signed certificate, private key
     *     and public key of signed certificate
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws CertificateException
     * @throws NoSuchProviderException
     * @throws SignatureException
     * @throws KeyStoreException
     * @throws IOException
     * @throws UnrecoverableKeyException
     * @throws MissingRootCertificateException when it wasn't initialized.
     */
    default KeyStore createCertForHost(CertData certData)
            throws NoSuchAlgorithmException, InvalidKeyException, CertificateException,
                    NoSuchProviderException, SignatureException, KeyStoreException, IOException,
                    UnrecoverableKeyException {
        return createCertForHost(certData.getCommonName());
    }

    /**
     * Loads CA's private key, public key and X.509 certificate into this bean.
     *
     * @param keystore
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    void initializeRootCA(KeyStore keystore)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException;
}

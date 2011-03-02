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

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is an in-memory cache implementation using {@link SslCertificateServiceImpl}.
 * It's not persisting certificates on hard disk.
 * This class is designed to be thread safe.
 *
 * @author MaWoKi
 */
public final class CachedSslCertifificateServiceImpl implements SslCertificateService {

	private static final SslCertificateService singleton = new CachedSslCertifificateServiceImpl();
	private final SslCertificateService delegate;

	private CachedSslCertifificateServiceImpl() {
		// avoid direct creating of instances
		delegate = SslCertificateServiceImpl.getService();
	}

	private Map<String, KeyStore> cache = new HashMap<String, KeyStore>();

	/* (non-Javadoc)
	 * @see org.parosproxy.paros.security.SslCertificateService#createCertForHost(java.lang.String)
	 */
	@Override
	public synchronized final KeyStore createCertForHost(String hostname)
			throws NoSuchAlgorithmException, InvalidKeyException,
			CertificateException, NoSuchProviderException, SignatureException,
			KeyStoreException, IOException, UnrecoverableKeyException {
		if (this.cache.containsKey(hostname)) {
			return this.cache.get(hostname);
		}
		final KeyStore ks = delegate.createCertForHost(hostname);
		this.cache.put(hostname, ks);
		return ks;
	}

	/**
	 * Appends the certificate, private key and public key to the user's
	 * key store. The users key store is located in the user's home directory
	 * and a file called "paros_certs.jks".
	 * Any existing file under that name will be overwritten.
	 *
	 * @param hostCertBundle
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 */

	/*
	 * Sample code, to implement persistence .... HostBundle is an alias container for keystore
	 */

//	protected final void saveUserCerts(HostCertBundle hostCertBundle, char[] passphrase) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
//		String userkeystore = System.getProperty("user.home", ".") + File.pathSeparator + "paros_certs.jks";
//		KeyStore store = KeyStore.getInstance("JKS");
//		try {
//			FileInputStream fis = new FileInputStream(userkeystore);
//			store.load(fis, passphrase);
//			fis.close();
//		} catch (Exception e) {
//			store.load(null, null);
//		}
//        Certificate[] chain = new Certificate[2];
//        chain[1] = this.getCaCert();
//        chain[0] = hostCertBundle.getCert();
//        store.setKeyEntry(URLEncoder.encode(hostCertBundle.getHostName(), "UTF-8"), hostCertBundle.getPrivKey(), passphrase, chain);
//        FileOutputStream fOut = new FileOutputStream("id.jks");
//        store.store(fOut, passphrase);
//        fOut.close();
//
//	}

	/**
	 * @return return the current {@link SslCertificateService}
	 */
	public final static SslCertificateService getService() {
		return singleton;
	}
}

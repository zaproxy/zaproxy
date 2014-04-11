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

	private Map<String, KeyStore> cache = new HashMap<>();

	@Override
	public synchronized KeyStore createCertForHost(String hostname)
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
	 * @return return the current {@link SslCertificateService}
	 */
	public static SslCertificateService getService() {
		return singleton;
	}

	@Override
	public void initializeRootCA(KeyStore keystore) throws KeyStoreException,
			UnrecoverableKeyException, NoSuchAlgorithmException {
		this.delegate.initializeRootCA(keystore);
	}

}

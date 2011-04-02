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
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.security.SslCertificateService;

/**
 * @author MaWoKi
 */
public class DynSSLParam extends AbstractParam {

	private static final String ROOT_CA = "dynssl.param.rootca";

	private KeyStore rootca = null;

	private final Logger logger = Logger.getLogger(DynSSLParam.class);

	@Override
	protected void parse() {
		try {
			setRootca(getConfig().getString(ROOT_CA, null));
		} catch (final Exception e) {
			logger.warn("Couldn't load Root CA parameter.", e);
		}
	}

	/**
	 * @param rootca
	 */
	public void setRootca(String rootca) {
		try {
			setRootca(String2Keystore(rootca));
		} catch (final Exception e) {
			logger.error("Couldn't save Root CA parameter.", e);
		}
	}

	public KeyStore getRootca() {
		return rootca;
	}

	/**
	 * @param rootca
	 */
	public void setRootca(KeyStore rootca) {
		this.rootca = rootca;
		try {
			getConfig().setProperty(ROOT_CA, keyStore2String(rootca));
		} catch (final Exception e) {
			logger.error("Couldn't save Root CA parameter.", e);
		}
	}

	/*
	 * ========================================================================
	 */

	/**
	 * @param keystore
	 * @return
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	private final static String keyStore2String(KeyStore keystore) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
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
	private final static KeyStore String2Keystore(String str) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		final byte[] bytes = Base64.decodeBase64(str);
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(bais, SslCertificateService.PASSPHRASE);
		bais.close();
		return ks;
	}

}


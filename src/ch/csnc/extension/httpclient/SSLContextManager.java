/*
 * This file is part of WebScarab, an Open Web Application Security
 * Project utility. For details, please see http://www.owasp.org/
 *
 * Copyright (c) 2002 - 2004 Rogan Dawes
 *
 * Please note that this file was originally released under the 
 * GNU General Public License  as published by the Free Software Foundation; 
 * either version 2 of the License, or (at your option) any later version.
 * 
 * As of October 2014 Rogan Dawes granted the OWASP ZAP Project permission to 
 * redistribute this code under the Apache License, Version 2.0: 
 *
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

package ch.csnc.extension.httpclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import ch.csnc.extension.util.Encoding;

public class SSLContextManager {

	/**
	 * The canonical class name of Sun PKCS#11 Provider.
	 */
	public static final String SUN_PKCS11_CANONICAL_CLASS_NAME = "sun.security.pkcs11.SunPKCS11";

	/**
	 * The canonical class name of IBMPKCS11Impl Provider.
	 */
	public static final String IBM_PKCS11_CONONICAL_CLASS_NAME = "com.ibm.crypto.pkcs11impl.provider.IBMPKCS11Impl";

	/**
	 * The name for providers of type PKCS#11.
	 * 
	 * @see #isProviderAvailable(String)
	 */
	public static final String PKCS11_PROVIDER_TYPE = "PKCS11";

	/**
	 * The name of the {@code KeyStore} type of Sun PKCS#11 Provider.
	 * 
	 * @see KeyStore#getInstance(String, Provider)
	 */
	private static final String SUN_PKCS11_KEYSTORE_TYPE = "PKCS11";

	/**
	 * The name of the {@code KeyStore} type of IBMPKCS11Impl Provider.
	 * 
	 * @see KeyStore#getInstance(String, Provider)
	 */
	private static final String IBM_PKCS11_KEYSTORE_TYPE = "PKCS11IMPLKS";

	private Map<String, SSLContext> _contextMaps = new TreeMap<String, SSLContext>();
	private SSLContext _noClientCertContext;
	private String _defaultKey = null;
	private Map<String, Map<?, ?>> _aliasPasswords = new HashMap<String, Map<?, ?>>();
	private List<KeyStore> _keyStores = new ArrayList<KeyStore>();
	private Map<KeyStore, String> _keyStoreDescriptions = new HashMap<KeyStore, String>();
	private Map<KeyStore, String> _keyStorePasswords = new HashMap<KeyStore, String>();
	
	private static Logger log = Logger.getLogger(SSLContextManager.class);


	private static TrustManager[] _trustAllCerts = new TrustManager[] {
		new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		}
	};

	private int _defaultKeystoreIndex = -1;
	private int _defaultAliasIndex = -1;

	/** Creates a new instance of SSLContextManager */
	public SSLContextManager() {
		try {
			_noClientCertContext = SSLContext.getInstance("SSL");
			_noClientCertContext.init(null, _trustAllCerts, new SecureRandom());
		} catch (NoSuchAlgorithmException nsao) {
			log.error("Could not get an instance of the SSL algorithm: " + nsao.getMessage(), nsao);
		} catch (KeyManagementException kme) {
			log.error("Error initialising the SSL Context:  " + kme.getMessage(), kme);
		}
		
		try {
			initMSCAPI();
		} catch (Exception e) {
		}
	}

	public boolean isProviderAvailable(String type) {
		try {
			if (type.equals(PKCS11_PROVIDER_TYPE)) {	
				try {
					Class.forName(SUN_PKCS11_CANONICAL_CLASS_NAME);
					return true;
				} catch (Throwable ignore) {
					Class.forName(IBM_PKCS11_CONONICAL_CLASS_NAME);
					return true;
				}
			} else if (type.equals("msks")) {
				Class.forName("se.assembla.jce.provider.ms.MSProvider");
				return true;
			}
		} catch (Throwable ignore) {
		}
		return false;
	}


	private int addKeyStore(KeyStore ks, String description, String password) {
		int index = _keyStores.indexOf(ks);
		if (index == -1) {
			_keyStores.add(ks);
			index = _keyStores.size() - 1;
		}
		_keyStoreDescriptions.put(ks, description);
		_keyStorePasswords.put(ks, password);
		return index;
	}

	public boolean removeKeyStore(int keystoreIndex) {
		boolean isDefaultKeyStore = (keystoreIndex == _defaultKeystoreIndex);
		KeyStore ks = _keyStores.get(keystoreIndex);

		_keyStores.remove(ks);
		_keyStoreDescriptions.remove(ks);
		_keyStorePasswords.remove(ks);

		if (isDefaultKeyStore) {
			_defaultKeystoreIndex = -1;
			_defaultAliasIndex = -1;
		}
		return isDefaultKeyStore;
	}

	public int getKeyStoreCount() {
		return _keyStores.size();
	}

	public String getKeyStoreDescription(int keystoreIndex) {
		return _keyStoreDescriptions.get(_keyStores.get(keystoreIndex));
	}

	public String getKeyStorePassword(int keystoreIndex) {
		return _keyStorePasswords.get(_keyStores.get(keystoreIndex));
	}

	public int getAliasCount(int keystoreIndex) {
		return getAliases(_keyStores.get(keystoreIndex)).size();
	}

	public String getAliasAt(int keystoreIndex, int aliasIndex) {
		return getAliases(_keyStores.get(keystoreIndex)).get(aliasIndex).getAlias();
	}

	private List<AliasCertificate> getAliases(KeyStore ks) {
		List<AliasCertificate> aliases = new ArrayList<AliasCertificate>();
		try {
			Enumeration<String> en = ks.aliases();

			boolean isIbm = isIbmPKCS11Provider();
			while (en.hasMoreElements()) {
				String alias = en.nextElement();
				// Sun's and IBM's KeyStore implementations behave differently...
				// With IBM's KeyStore impl #getCertificate(String) returns null when #isKeyEntry(String) returns true.
				// If IBM add all certificates and let the user choose the correct one. 
				if (ks.isKeyEntry(alias) || (isIbm && ks.isCertificateEntry(alias))) {
					Certificate cert = ks.getCertificate(alias);
					// IBM: Maybe we should check the KeyUsage?
					// ((X509Certificate) cert).getKeyUsage()[0]
					AliasCertificate aliasCert = new AliasCertificate(cert, alias);
					aliases.add(aliasCert);
				}
			}
		} catch (KeyStoreException kse) {
			kse.printStackTrace();
		}
		return aliases;
	}

	public List<AliasCertificate> getAliases(int ks) {
		return getAliases(_keyStores.get(ks));
	}

	public Certificate getCertificate(int keystoreIndex, int aliasIndex) {
		try {
			KeyStore ks = _keyStores.get(keystoreIndex);
			String alias = getAliasAt(keystoreIndex, aliasIndex);
			return ks.getCertificate(alias);
		} catch (Exception e) {
			return null;
		}
	}

	public String getFingerPrint(Certificate cert) throws KeyStoreException {
		if (!(cert instanceof X509Certificate)) {
			return null;
		}
		
		StringBuffer buff = new StringBuffer();
		X509Certificate x509 = (X509Certificate) cert;
		
		try {
			String fingerprint = Encoding.hashMD5(cert.getEncoded());
			for (int i = 0; i < fingerprint.length(); i += 2) {
				buff.append(fingerprint.substring(i, i + 1)).append(":");
			}
			buff.deleteCharAt(buff.length() - 1);
		} catch (CertificateEncodingException e) {
			throw new KeyStoreException(e.getMessage());
		}
		
		String dn = x509.getSubjectDN().getName();
		
		log.info("Fingerprint is " + buff.toString().toUpperCase());
		
		return buff.toString().toUpperCase() + " " + dn;
	}

	public boolean isKeyUnlocked(int keystoreIndex, int aliasIndex) {
		KeyStore ks = _keyStores.get(keystoreIndex);
		String alias = getAliasAt(keystoreIndex, aliasIndex);

		Map<?, ?> pwmap = _aliasPasswords.get(ks);
		if (pwmap == null) {
			return false;
		}
		
		return pwmap.containsKey(alias);
	}

	public void setDefaultKey(int keystoreIndex, int aliasIndex) throws KeyStoreException {

		_defaultKeystoreIndex = keystoreIndex;
		_defaultAliasIndex = aliasIndex;

		if ((_defaultKeystoreIndex == -1) || (_defaultAliasIndex == -1)) {
			_defaultKey = "";
		} else {
			_defaultKey = getFingerPrint(getCertificate(keystoreIndex, aliasIndex));
		}
	}

	public String getDefaultKey() {
		return _defaultKey;
	}

	public Certificate getDefaultCertificate() {
		return getCertificate(_defaultKeystoreIndex, _defaultAliasIndex);
	}

	public int initMSCAPI() throws KeyStoreException, NoSuchProviderException, IOException, NoSuchAlgorithmException, CertificateException {
		try {
			if (!isProviderAvailable("msks")) {
				return -1;
			}

			Provider mscapi = (Provider) Class.forName("se.assembla.jce.provider.ms.MSProvider").newInstance();
			Security.addProvider(mscapi);

			// init the key store
			KeyStore ks = KeyStore.getInstance("msks", "assembla");
			ks.load(null, null);
			
			return addKeyStore(ks, "Microsoft CAPI Store", null);
			
		} catch (Exception e) {
			log.error("Error instantiating the MSCAPI provider: " + e.getMessage(), e);
			return -1;
		}
	}

	/*
	 * public int initCryptoApi() throws KeyStoreException,
	 * NoSuchAlgorithmException, CertificateException, IOException{
	 * 
	 * Provider mscapi = new sun.security.mscapi.SunMSCAPI();
	 * Security.addProvider(mscapi);
	 * 
	 * KeyStore ks = KeyStore.getInstance("Windows-MY"); ks.load(null, null);
	 * 
	 * return addKeyStore(ks, "CryptoAPI", null); }
	 */
	public int initPKCS11(PKCS11Configuration configuration, String kspassword)
		throws IOException, KeyStoreException,
			CertificateException, NoSuchAlgorithmException,
			ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {

		if (!isProviderAvailable(PKCS11_PROVIDER_TYPE)) {
			return -1;
		}

		Provider pkcs11 = createPKCS11Provider(configuration);

        Security.addProvider(pkcs11);

		// init the key store
		KeyStore ks = getPKCS11KeyStore(pkcs11.getName());
		ks.load(null, kspassword == null ? null : kspassword.toCharArray());
		return addKeyStore(ks, "PKCS#11: " + configuration.getName(), ""); // do not store pin code
	}

	private static Provider createPKCS11Provider(PKCS11Configuration configuration) throws ClassNotFoundException,
			NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Provider pkcs11 = null;
		if (isSunPKCS11Provider()) {
			pkcs11 = createInstance(SUN_PKCS11_CANONICAL_CLASS_NAME, InputStream.class, configuration.toInpuStream());
		} else if (isIbmPKCS11Provider()) {
			pkcs11 = createInstance(IBM_PKCS11_CONONICAL_CLASS_NAME, BufferedReader.class, new BufferedReader(
					new InputStreamReader(configuration.toInpuStream())));
		}
		return pkcs11;
	}

	private static Provider createInstance(String name, Class<?> paramClass, Object param) throws ClassNotFoundException,
			NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<?> instanceClass = Class.forName(name);
		Constructor<?> c = instanceClass.getConstructor(new Class<?>[] { paramClass });
		return (Provider) c.newInstance(new Object[] { param });
	}

	private static boolean isSunPKCS11Provider() {
		try {
			Class.forName(SUN_PKCS11_CANONICAL_CLASS_NAME);
			return true;
		} catch (Throwable ignore) {
		}
		return false;
	}

	private static boolean isIbmPKCS11Provider() {
		try {
			Class.forName(IBM_PKCS11_CONONICAL_CLASS_NAME);
			return true;
		} catch (Throwable ignore) {
		}
		return false;
	}

	private static KeyStore getPKCS11KeyStore(String providerName) throws KeyStoreException {
		String keyStoreType = SUN_PKCS11_KEYSTORE_TYPE;
		if (isIbmPKCS11Provider()) {
			keyStoreType = IBM_PKCS11_KEYSTORE_TYPE;
		}
		return KeyStore.getInstance(keyStoreType, Security.getProvider(providerName));
	}

	public int loadPKCS12Certificate(String filename, String ksPassword)
		throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
		
		// Get Filename
		File file = new File(filename);
		if (!file.exists()) {
			throw new FileNotFoundException(filename + " could not be found");
		}
		String name = file.getName();

		// Open the file
		InputStream is = new FileInputStream(file);

		// create the keystore
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(is, ksPassword == null ? null : ksPassword.toCharArray());

		return addKeyStore(ks, "PKCS#12: " + name, ksPassword);
	}

	public boolean unlockKeyWithDefaultPassword(int keystoreIndex, int aliasIndex)
	throws KeyManagementException, KeyStoreException {
		
		return unlockKey(keystoreIndex, aliasIndex, getKeyStorePassword(keystoreIndex));
	}

	public boolean unlockKey(int keystoreIndex, int aliasIndex, String keyPassword)
	throws KeyStoreException, KeyManagementException {
		
		KeyStore ks = _keyStores.get(keystoreIndex);
		String alias = getAliasAt(keystoreIndex, aliasIndex);

		AliasKeyManager akm = new AliasKeyManager(ks, alias, keyPassword);

		try {
			akm.getPrivateKey(alias).toString();
		} catch (NullPointerException ex) {
			log.error("Could not get private key: " + ex.getMessage(), ex);
			return false;
		}

		String fingerprint = getFingerPrint(getCertificate(keystoreIndex, aliasIndex));

		if (fingerprint == null) {
			log.info("No fingerprint found");
			return false;
		}

		SSLContext sc;
		try {
			sc = SSLContext.getInstance("SSL");
		} catch (NoSuchAlgorithmException nsao) {
			log.error("Could not get an instance of the SSL algorithm: " + nsao.getMessage(), nsao);
			return false;
		}

		sc.init(new KeyManager[] { akm }, _trustAllCerts, new SecureRandom());

		String key = fingerprint;
		if (key.indexOf(" ") > 0) {
			key = key.substring(0, key.indexOf(" "));
		}
		
		_contextMaps.put(key, sc);
		log.info("Key has been unlocked.");

		return true;
	}

	public void invalidateSessions() {
		invalidateSession(_noClientCertContext);
		Iterator<String> it = _contextMaps.keySet().iterator();
		while (it.hasNext()) {
			invalidateSession(_contextMaps.get(it.next()));
		}
	}

	private void invalidateSession(SSLContext sc) {
		SSLSessionContext sslsc = sc.getClientSessionContext();
		if (sslsc != null) {
			int timeout = sslsc.getSessionTimeout();
			// force sessions to be timed out
			sslsc.setSessionTimeout(1);
			sslsc.setSessionTimeout(timeout);
		}
		sslsc = sc.getServerSessionContext();
		if (sslsc != null) {
			int timeout = sslsc.getSessionTimeout();
			// force sessions to be timed out
			sslsc.setSessionTimeout(1);
			sslsc.setSessionTimeout(timeout);
		}
	}

	public SSLContext getSSLContext(String fingerprint) {
		log.info("Requested SSLContext for " + fingerprint);

		if (fingerprint == null || fingerprint.equals("none")) {
			return _noClientCertContext;
		}
		
		if (fingerprint.indexOf(" ") > 0) {
			fingerprint = fingerprint.substring(0, fingerprint.indexOf(" "));
		}
		
		return _contextMaps.get(fingerprint);
	}
}

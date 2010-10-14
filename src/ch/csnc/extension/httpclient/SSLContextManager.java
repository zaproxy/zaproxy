/*
 * This file is part of WebScarab, an Open Web Application Security
 * Project utility. For details, please see http://www.owasp.org/
 *
 * Copyright (c) 2002 - 2004 Rogan Dawes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package ch.csnc.extension.httpclient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ch.csnc.extension.util.Encoding;
import ch.csnc.extension.util.NullComparator;

public class SSLContextManager {
    
    private Map<String, SSLContext> _contextMaps = new TreeMap<String, SSLContext>(new NullComparator());
    private SSLContext _noClientCertContext;
    private String _defaultKey = null;
    private Map _aliasPasswords = new HashMap();
    private List<KeyStore> _keyStores = new ArrayList<KeyStore>();
    private Map<KeyStore, String> _keyStoreDescriptions = new HashMap<KeyStore, String>();
    private Map<KeyStore, String> _keyStorePasswords = new HashMap<KeyStore, String>();
    
    private static TrustManager[] _trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }
    };
    
    private Logger _logger = Logger.getLogger(getClass().getName());
	private int _defaultKeystoreIndex = -1;
	private int _defaultAliasIndex = -1;
    
    /** Creates a new instance of SSLContextManager */
    public SSLContextManager() {
        try {
            _noClientCertContext = SSLContext.getInstance("SSL");
            _noClientCertContext.init(null, _trustAllCerts, new SecureRandom());
        } catch (NoSuchAlgorithmException nsao) {
            _logger.severe("Could not get an instance of the SSL algorithm: " + nsao.getMessage());
        } catch (KeyManagementException kme) {
            _logger.severe("Error initialising the SSL Context: " + kme);
        }
        try {
            initMSCAPI();
        } catch (Exception e) {}
    }
    
    public boolean isProviderAvailable(String type) {
        try {
            if (type.equals("PKCS11")) {
                Class.forName("sun.security.pkcs11.SunPKCS11");
            } else if (type.equals("msks")) {
                Class.forName("se.assembla.jce.provider.ms.MSProvider");
            }
        } catch (Throwable t) {
            return false;
        }
        return true;
    }
    
    private boolean isProviderLoaded(String keyStoreType) {
        return Security.getProvider(keyStoreType) != null ? true : false;
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
    
    public boolean removeKeyStore(int keystoreIndex){
    	boolean isDefaultKeyStore = (keystoreIndex == _defaultKeystoreIndex);
    	KeyStore ks = (KeyStore)_keyStores.get(keystoreIndex);
    	
    	_keyStores.remove(ks);
    	_keyStoreDescriptions.remove(ks);
    	_keyStorePasswords.remove(ks);
    	
    	if(isDefaultKeyStore){
	    	_defaultKeystoreIndex = -1;
	    	_defaultAliasIndex = -1;
    	}
    	return isDefaultKeyStore;
    }
    
    public int getKeyStoreCount() {
        return _keyStores.size();
    }
    
    public String getKeyStoreDescription(int keystoreIndex) {
        return (String) _keyStoreDescriptions.get(_keyStores.get(keystoreIndex));
    }
    
    public String getKeyStorePassword(int keystoreIndex) {
        return (String) _keyStorePasswords.get(_keyStores.get(keystoreIndex));
    }
    
    public int getAliasCount(int keystoreIndex) {
        return getAliases((KeyStore) _keyStores.get(keystoreIndex)).size();
    }
    
    public String getAliasAt(int keystoreIndex, int aliasIndex) {
        return getAliases((KeyStore) _keyStores.get(keystoreIndex)).get(aliasIndex).getAlias();
    }
    
    private List<AliasCertificate> getAliases(KeyStore ks) {
        List<AliasCertificate> aliases = new ArrayList<AliasCertificate>();
        try {
            Enumeration<String> en = ks.aliases();
            
            while (en.hasMoreElements()) {
                String alias = en.nextElement();
                if (ks.isKeyEntry(alias)){
                    Certificate cert = ks.getCertificate(alias);
                    
                    AliasCertificate alcer = new AliasCertificate(cert, alias);
                	
                	aliases.add(alcer);
                }
            }
        } catch (KeyStoreException kse) {
            kse.printStackTrace();
        }
        return aliases;
    }
    
    public List<AliasCertificate> getAliases(int ks) {
    	return getAliases((KeyStore)_keyStores.get(ks));
    }
    
    public Certificate getCertificate(int keystoreIndex, int aliasIndex) {
        try {
            KeyStore ks = (KeyStore) _keyStores.get(keystoreIndex);
            String alias = getAliasAt(keystoreIndex, aliasIndex);
            return ks.getCertificate(alias);
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getFingerPrint(Certificate cert) throws KeyStoreException {
        if (!(cert instanceof X509Certificate)) return null;
        StringBuffer buff = new StringBuffer();
        X509Certificate x509 = (X509Certificate) cert;
        try {
            String fingerprint = Encoding.hashMD5(cert.getEncoded());
            for (int i=0; i<fingerprint.length(); i+=2) {
                buff.append(fingerprint.substring(i, i+1)).append(":");
            }
            buff.deleteCharAt(buff.length()-1);
        } catch (CertificateEncodingException e) {
            throw new KeyStoreException(e.getMessage());
        }
        String dn = x509.getSubjectDN().getName();
        _logger.info("Fingerprint is " + buff.toString().toUpperCase());
        return buff.toString().toUpperCase() + " " + dn;
    }
    
    public boolean isKeyUnlocked(int keystoreIndex, int aliasIndex) {
        KeyStore ks = (KeyStore) _keyStores.get(keystoreIndex);
        String alias = getAliasAt(keystoreIndex, aliasIndex);
        
        Map pwmap = (Map) _aliasPasswords.get(ks);
        if (pwmap == null) return false;
        return pwmap.containsKey(alias);
    }
    
    public void setDefaultKey(int keystoreIndex, int aliasIndex) throws KeyStoreException {
    	
    	_defaultKeystoreIndex = keystoreIndex;
    	_defaultAliasIndex = aliasIndex;
    	
    	if((_defaultKeystoreIndex == -1) || (_defaultAliasIndex == -1)){
    		_defaultKey = "";
    	}else{
    		_defaultKey = getFingerPrint(getCertificate(keystoreIndex, aliasIndex));
    	}
    }
    
    public String getDefaultKey() {
        return _defaultKey;
    }
    
    public Certificate getDefaultCertificate(){
    	
    	return getCertificate(_defaultKeystoreIndex, _defaultAliasIndex);
    }
    
    public int initMSCAPI()
    throws KeyStoreException, NoSuchProviderException, IOException, NoSuchAlgorithmException, CertificateException {
        try {
            if (!isProviderAvailable("msks")) return -1;
            
            Provider mscapi = (Provider) Class.forName("se.assembla.jce.provider.ms.MSProvider").newInstance();
            Security.addProvider(mscapi);
            
            // init the key store
            KeyStore ks = KeyStore.getInstance("msks", "assembla");
            ks.load(null, null);
            return addKeyStore(ks, "Microsoft CAPI Store", null);
        } catch (Exception e) {
            System.err.println("Error instantiating the MSCAPI provider");
            e.printStackTrace();
            return -1;
        }
    }
    
    /*
    public int initCryptoApi() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
    	
    	Provider mscapi = new sun.security.mscapi.SunMSCAPI();
    	Security.addProvider(mscapi);
    	
    	KeyStore ks = KeyStore.getInstance("Windows-MY");
    	ks.load(null, null);
    	
    	return addKeyStore(ks, "CryptoAPI", null);
    }
    */
    
    public int initPKCS11(String name, String library, int slot, String kspassword)
    throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        
            if (!isProviderAvailable("PKCS11")) return -1;
            
            // Set up a virtual config file
            StringBuffer cardConfig = new StringBuffer();
            cardConfig.append("name=").append(name).append("\n");
            cardConfig.append("library=").append(library).append("\n");
            cardConfig.append("slot=").append(slot).append("\n");
            InputStream is = new ByteArrayInputStream(cardConfig.toString().getBytes());
            
            // create the provider
            Class pkcs11Class = Class.forName("sun.security.pkcs11.SunPKCS11");
            Constructor c = pkcs11Class.getConstructor(new Class[] { InputStream.class });
            Provider pkcs11 = (Provider) c.newInstance(new Object[] { is });
            Security.addProvider(pkcs11);

            
            // init the key store
            KeyStore ks = KeyStore.getInstance("PKCS11");
            ks.load(null, kspassword == null ? null : kspassword.toCharArray());
            return addKeyStore(ks, "PKCS#11: "+name, ""); //do not store pin code
    }
    
    public int loadPKCS12Certificate(String filename, String ksPassword)
    throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        // Open the file
        InputStream is = new FileInputStream(filename);
        if (is == null)
            throw new FileNotFoundException(filename + " could not be found");
        
        //Get Filename
        File file = new File(filename);
        String name = file.getName();
        
        // create the keystore
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(is, ksPassword == null ? null : ksPassword.toCharArray());
        
        return addKeyStore(ks, "PKCS#12: " + name, ksPassword); 
    }
    
    public boolean unlockKeyWithDefaultPassword(int keystoreIndex, int aliasIndex) throws KeyManagementException, KeyStoreException{
    	return unlockKey(keystoreIndex, aliasIndex, getKeyStorePassword(keystoreIndex));
    }
    
    public boolean unlockKey(int keystoreIndex, int aliasIndex, String keyPassword) throws KeyStoreException, KeyManagementException {
        KeyStore ks = (KeyStore) _keyStores.get(keystoreIndex);
        String alias = getAliasAt(keystoreIndex, aliasIndex);
        
        AliasKeyManager akm = new AliasKeyManager(ks, alias, keyPassword);
        
        try{
        	akm.getPrivateKey(alias).toString();
        }
        catch (NullPointerException ex){
        	_logger.severe("Could not get private key");
        	return false;
        }
        
        String fingerprint = getFingerPrint(getCertificate(keystoreIndex, aliasIndex));
        
        if (fingerprint == null) {
            _logger.severe("No fingerprint found");
            return false;
        }
        
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException nsao) {
            _logger.severe("Could not get an instance of the SSL algorithm: " + nsao.getMessage());
            return false;
        }
        
        sc.init(new KeyManager[] { akm }, _trustAllCerts, new SecureRandom());
        
        String key = fingerprint;
        if (key.indexOf(" ")>0)
            key = key.substring(0, key.indexOf(" "));
        _contextMaps.put(key, sc);
        
        _logger.info("Key is unlocked.");
        
        return true;
    }
    
    public void invalidateSessions() {
        invalidateSession(_noClientCertContext);
        Iterator<String> it = _contextMaps.keySet().iterator();
        while (it.hasNext()) {
            invalidateSession((SSLContext)_contextMaps.get(it.next()));
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
        _logger.info("Requested SSLContext for " + fingerprint);
        
        if (fingerprint == null || fingerprint.equals("none"))
            return _noClientCertContext;
        if (fingerprint.indexOf(" ")>0)
            fingerprint = fingerprint.substring(0, fingerprint.indexOf(" "));
        return (SSLContext) _contextMaps.get(fingerprint);
    }
    
}

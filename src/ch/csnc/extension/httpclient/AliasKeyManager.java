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

import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

/**
 * A KeyManager implementation that only ever selects a single alias,
 * rather than considering the "best" alias for the circumstances
 * @author rdawes
 */
public class AliasKeyManager implements X509KeyManager {
    
    private KeyStore _ks;
    private String _alias;
    private String _keyPassword;
    
    /**
     * Creates a new instance of AliasKeyManager
     * @param ks The KeyStore that contains the keypair to use
     * @param password the password for the key (not the keystore)
     * @param alias the alias of the certificate to use
     */
    public AliasKeyManager(KeyStore ks, String alias, String keyPassword) {
        _ks = ks;
        _alias = alias;
        _keyPassword = keyPassword;
    }
    
    public String chooseClientAlias(String[] str, Principal[] principal, Socket socket) {
        return _alias;
    }

    public String chooseServerAlias(String str, Principal[] principal, Socket socket) {
        return _alias;
    }

    public X509Certificate[] getCertificateChain(String alias) {
        try {
            Certificate[] certs = _ks.getCertificateChain(alias);
            if (certs == null) return null;
            X509Certificate[] x509certs = new X509Certificate[certs.length];
            for (int i=0; i<certs.length; i++) {
                x509certs[i]=(X509Certificate) certs[i];
            }
            return x509certs;
        } catch (KeyStoreException kse) {
            kse.printStackTrace();
            return null;
        }
    }

    public String[] getClientAliases(String str, Principal[] principal) {
        return new String[] { _alias };
    }

    public PrivateKey getPrivateKey(String alias) {
        try {
            return (PrivateKey) _ks.getKey(alias, _keyPassword.toCharArray());
        } catch (KeyStoreException kse) {
            kse.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException nsao) {
            nsao.printStackTrace();
            return null;
        } catch (UnrecoverableKeyException uke) {
            uke.printStackTrace();
            return null;
        }
    }

    public String[] getServerAliases(String str, Principal[] principal) {
        return new String[] { _alias };
    }
    
}

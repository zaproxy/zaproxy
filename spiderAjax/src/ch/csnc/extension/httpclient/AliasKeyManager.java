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

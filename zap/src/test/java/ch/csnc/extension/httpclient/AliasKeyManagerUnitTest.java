/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package ch.csnc.extension.httpclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.net.Socket;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test for {@link ch.csnc.extension.httpclient.AliasKeyManager}
 *
 * @author bjoern.kimminich@gmx.de
 */
@ExtendWith(MockitoExtension.class)
class AliasKeyManagerUnitTest {

    private static final String ALIAS = "alias";
    private static final String PASSWORD = "password";

    private AliasKeyManager aliasKeyManager;

    private KeyStore keyStore;

    @Mock private KeyStoreSpi keyStoreSpi;

    @BeforeEach
    void setUp() throws Exception {
        keyStore = new KeyStore(keyStoreSpi, null, null) {};
        keyStore.load(null);
    }

    @Test
    void shouldAlwaysChooseInitiallyGivenAliasAsClientAlias() {
        // Given
        aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
        // When
        String clientAlias =
                aliasKeyManager.chooseClientAlias(
                        new String[0], new Principal[] {mock(Principal.class)}, mock(Socket.class));
        // Then
        assertThat(clientAlias, is(equalTo(ALIAS)));
    }

    @Test
    void shouldOnlyReturnInitiallyGivenAliasAsClientAlias() {
        // Given
        aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
        // When
        String[] clientAliases =
                aliasKeyManager.getClientAliases("", new Principal[] {mock(Principal.class)});
        // Then
        assertThat(clientAliases.length, is(1));
        assertThat(clientAliases, hasItemInArray(ALIAS));
    }

    @Test
    void shouldAlwaysChooseInitiallyGivenAliasAsServerAlias() {
        // Given
        aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
        // When
        String serverAlias =
                aliasKeyManager.chooseServerAlias(
                        "", new Principal[] {mock(Principal.class)}, mock(Socket.class));
        // Then
        assertThat(serverAlias, is(equalTo(ALIAS)));
    }

    @Test
    void shouldOnlyReturnInitiallyGivenAliasAsServerAlias() {
        // Given
        aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
        // When
        String[] serverAliases =
                aliasKeyManager.getServerAliases("", new Principal[] {mock(Principal.class)});
        // Then
        assertThat(serverAliases, is(arrayWithSize(1)));
        assertThat(serverAliases, is(arrayContaining(ALIAS)));
    }

    @Test
    void shouldReturnNullWhenNoCertificatesAreFound() throws Exception {
        // Given
        given(keyStoreSpi.engineGetCertificateChain(ALIAS)).willReturn(null);
        aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
        // When
        X509Certificate[] certificates = aliasKeyManager.getCertificateChain(ALIAS);
        // Then
        assertThat(certificates, is(equalTo(null)));
    }

    @Test
    void shouldReturnCertificatesFromKeyStoreAsX509Certificates() throws Exception {
        // Given
        Certificate[] originalCertificates =
                new Certificate[] {mock(X509Certificate.class), mock(X509Certificate.class)};
        given(keyStoreSpi.engineGetCertificateChain(ALIAS)).willReturn(originalCertificates);
        aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
        // When
        X509Certificate[] certificates = aliasKeyManager.getCertificateChain(ALIAS);
        // Then
        assertThat(certificates, is(arrayWithSize(2)));
        assertThat(certificates, arrayContaining(originalCertificates));
    }

    @Test
    void shouldReturnNullAsCertificatesWhenExceptionOccursAccessingKeyStore() throws Exception {
        // Given
        given(keyStoreSpi.engineGetCertificateChain(ALIAS))
                .willAnswer(this::throwKeyStoreException);
        aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
        // When
        X509Certificate[] certificates = aliasKeyManager.getCertificateChain(ALIAS);
        // Then
        assertThat(certificates, is(equalTo(null)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnNullAsKeyWhenExceptionOccursAccessingKeyStore() throws Exception {
        // Given
        given(keyStoreSpi.engineGetKey(ALIAS, PASSWORD.toCharArray()))
                .willAnswer(this::throwKeyStoreException)
                .willThrow(NoSuchAlgorithmException.class, UnrecoverableKeyException.class);
        aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
        // When/Then
        assertThat(aliasKeyManager.getPrivateKey(ALIAS), is(equalTo(null))); // KeyStoreException
        assertThat(
                aliasKeyManager.getPrivateKey(ALIAS),
                is(equalTo(null))); // NoSuchAlgorithmException
        assertThat(
                aliasKeyManager.getPrivateKey(ALIAS),
                is(equalTo(null))); // UnrecoverableKeyException
    }

    @Test
    void shouldReturnPrivateKeyFromKeyStore() throws Exception {
        // Given
        Key originalKey = mock(PrivateKey.class);
        given(keyStoreSpi.engineGetKey(ALIAS, PASSWORD.toCharArray())).willReturn(originalKey);
        aliasKeyManager = new AliasKeyManager(keyStore, ALIAS, PASSWORD);
        // When/Then
        assertThat(aliasKeyManager.getPrivateKey(ALIAS), is(equalTo(originalKey)));
    }

    private <T> T throwKeyStoreException(T arg) throws KeyStoreException {
        throw new KeyStoreException();
    }
}

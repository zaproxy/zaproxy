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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SSLContextManagerUnitTest {

    private SSLContextManager sslContextManager;

    @BeforeEach
    void setUp() throws Exception {
        sslContextManager = new SSLContextManager();
    }

    @Test
    void shouldReturnAvailabilityOfPKCS11Provider() {
        // Given
        boolean pkcs11ProviderAvailable = true;
        try {
            Class.forName(SSLContextManager.SUN_PKCS11_CANONICAL_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            try {
                Class.forName(SSLContextManager.IBM_PKCS11_CANONICAL_CLASS_NAME);
            } catch (ClassNotFoundException e2) {
                pkcs11ProviderAvailable = false;
            }
        }
        // When
        boolean result =
                sslContextManager.isProviderAvailable(SSLContextManager.PKCS11_PROVIDER_TYPE);
        // Then
        assertThat(result, is(equalTo(pkcs11ProviderAvailable)));
    }

    @Test
    void shouldReturnAvailabilityOfMsksProvider() {
        // Given
        boolean msks11ProviderAvailable = true;
        try {
            Class.forName("se.assembla.jce.provider.ms.MSProvider");
        } catch (ClassNotFoundException e) {
            msks11ProviderAvailable = false;
        }
        // When
        boolean result = sslContextManager.isProviderAvailable("msks");
        // Then
        assertThat(result, is(equalTo(msks11ProviderAvailable)));
    }

    @Test
    void shouldAlwaysReturnFalseForOtherThanPKCS11AndMsksProvider() {
        // Given
        // When
        boolean result = sslContextManager.isProviderAvailable("thisProviderDoesNotExist");
        // Then
        assertThat(result, is(false));
    }
}

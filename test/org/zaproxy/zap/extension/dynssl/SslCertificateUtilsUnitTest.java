/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.extension.dynssl;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.parosproxy.paros.extension.encoder.Base64;

/**
 * Unit test for {@link SslCertificateUtils}.
 */
public class SslCertificateUtilsUnitTest {

    private static final String CERT_DATA = "Certificate data...";
    private static final String CERT_DATA_BASE64 = Base64.encodeBytes(CERT_DATA.getBytes(StandardCharsets.US_ASCII));

    private static final String PRIV_KEY_DATA = "Private key...";
    private static final String PRIV_KEY_BASE64 = Base64.encodeBytes(PRIV_KEY_DATA.getBytes(StandardCharsets.US_ASCII));

    @Test
    public void shouldReturnEmptyByteArrayIfNotAbleToFindCertSectionInPemData() {
        // Given
        String pem = CERT_DATA_BASE64;
        // When
        byte[] cert = SslCertificateUtils.extractCertificate(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    public void shouldReturnEmptyByteArrayIfBeginCertTokenWasNotFoundInPemData() {
        // Given
        String pem = CERT_DATA_BASE64 + SslCertificateUtils.END_CERTIFICATE_TOKEN;
        // When
        byte[] cert = SslCertificateUtils.extractCertificate(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    public void shouldReturnEmptyByteArrayIfEndCertTokenWasNotFoundInPemData() {
        // Given
        String pem = SslCertificateUtils.BEGIN_CERTIFICATE_TOKEN + CERT_DATA_BASE64;
        // When
        byte[] cert = SslCertificateUtils.extractCertificate(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    public void shouldReturnEmptyByteArrayIfEndCertTokenIsBeforeBeginCertTokenInPemData() {
        // Given
        String pem = SslCertificateUtils.END_CERTIFICATE_TOKEN + CERT_DATA_BASE64 + SslCertificateUtils.BEGIN_CERTIFICATE_TOKEN;
        // When
        byte[] cert = SslCertificateUtils.extractCertificate(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    public void shouldReturnCertificateBetweenBeginAndEndCertTokensFromPemData() {
        // Given
        String pem = SslCertificateUtils.BEGIN_CERTIFICATE_TOKEN + CERT_DATA_BASE64 + SslCertificateUtils.END_CERTIFICATE_TOKEN;
        // When
        byte[] cert = SslCertificateUtils.extractCertificate(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(CERT_DATA.length())));
        assertThat(cert, is(equalTo(CERT_DATA.getBytes(StandardCharsets.US_ASCII))));
    }

    @Test
    public void shouldReturnEmptyByteArrayIfNotAbleToFindPrivKeySectionInPemData() {
        // Given
        String pem = PRIV_KEY_BASE64;
        // When
        byte[] cert = SslCertificateUtils.extractPrivateKey(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    public void shouldReturnEmptyByteArrayIfBeginPrivKeyTokenWasNotFoundInPemData() {
        // Given
        String pem = PRIV_KEY_BASE64 + SslCertificateUtils.END_PRIVATE_KEY_TOKEN;
        // When
        byte[] cert = SslCertificateUtils.extractPrivateKey(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    public void shouldReturnEmptyByteArrayIfEndPrivKeyTokenWasNotFoundInPemData() {
        // Given
        String pem = SslCertificateUtils.BEGIN_PRIVATE_KEY_TOKEN + PRIV_KEY_BASE64;
        // When
        byte[] cert = SslCertificateUtils.extractPrivateKey(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    public void shouldReturnEmptyByteArrayIfEndPrivKeyTokenIsBeforeBeginPrivKeyTokenInPemData() {
        // Given
        String pem = SslCertificateUtils.END_PRIVATE_KEY_TOKEN + PRIV_KEY_BASE64 + SslCertificateUtils.BEGIN_PRIVATE_KEY_TOKEN;
        // When
        byte[] cert = SslCertificateUtils.extractPrivateKey(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    public void shouldReturnPrivateKeyBetweenBeginAndEndPrivKeyTokensFromPemData() {
        // Given
        String pem = SslCertificateUtils.BEGIN_PRIVATE_KEY_TOKEN + PRIV_KEY_BASE64 + SslCertificateUtils.END_PRIVATE_KEY_TOKEN;
        // When
        byte[] cert = SslCertificateUtils.extractPrivateKey(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(PRIV_KEY_DATA.length())));
        assertThat(cert, is(equalTo(PRIV_KEY_DATA.getBytes(StandardCharsets.US_ASCII))));
    }
}

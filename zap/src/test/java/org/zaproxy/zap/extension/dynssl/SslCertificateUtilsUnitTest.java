/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

/** Unit test for {@link SslCertificateUtils}. */
class SslCertificateUtilsUnitTest {

    private static final String CERT_DATA = "Certificate data...";
    private static final String CERT_DATA_BASE64 =
            Base64.getEncoder().encodeToString(CERT_DATA.getBytes(StandardCharsets.US_ASCII));

    private static final String PRIV_KEY_DATA = "Private key...";
    private static final String PRIV_KEY_BASE64 =
            Base64.getEncoder().encodeToString(PRIV_KEY_DATA.getBytes(StandardCharsets.US_ASCII));

    private static final String FISH_CERT_BASE64 =
            "MIIC9TCCAl6gAwIBAgIJANL8E4epRNznMA0GCSqGSIb3DQEBBQUAMFsxGDAWBgNV\n"
                    + "BAoTD1N1cGVyZmlzaCwgSW5jLjELMAkGA1UEBxMCU0YxCzAJBgNVBAgTAkNBMQsw\n"
                    + "CQYDVQQGEwJVUzEYMBYGA1UEAxMPU3VwZXJmaXNoLCBJbmMuMB4XDTE0MDUxMjE2\n"
                    + "MjUyNloXDTM0MDUwNzE2MjUyNlowWzEYMBYGA1UEChMPU3VwZXJmaXNoLCBJbmMu\n"
                    + "MQswCQYDVQQHEwJTRjELMAkGA1UECBMCQ0ExCzAJBgNVBAYTAlVTMRgwFgYDVQQD\n"
                    + "Ew9TdXBlcmZpc2gsIEluYy4wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAOjz\n"
                    + "Shh2Xxk/sc9Y6X9DBwmVgDXFD/5xMSeBmRImIKXfj2r8QlU57gk4idngNsSsAYJb\n"
                    + "1Tnm+Y8HiN/+7vahFM6pdEXY/fAXVyqC4XouEpNarIrXFWPRt5tVgA9YvBxJ7SBi\n"
                    + "3bZMpTrrHD2g/3pxptMQeDOuS8Ic/ZJKocPnQaQtAgMBAAGjgcAwgb0wDAYDVR0T\n"
                    + "BAUwAwEB/zAdBgNVHQ4EFgQU+5izU38URC7o7tUJml4OVoaoNYgwgY0GA1UdIwSB\n"
                    + "hTCBgoAU+5izU38URC7o7tUJml4OVoaoNYihX6RdMFsxGDAWBgNVBAoTD1N1cGVy\n"
                    + "ZmlzaCwgSW5jLjELMAkGA1UEBxMCU0YxCzAJBgNVBAgTAkNBMQswCQYDVQQGEwJV\n"
                    + "UzEYMBYGA1UEAxMPU3VwZXJmaXNoLCBJbmMuggkA0vwTh6lE3OcwDQYJKoZIhvcN\n"
                    + "AQEFBQADgYEApHyg7ApKx3DEcWjzOyLi3JyN0JL+c35yK1VEmxu0Qusfr76645Oj\n"
                    + "1IsYwpTws6a9ZTRMzST4GQvFFQra81eLqYbPbMPuhC+FCxkUF5i0DNSWi+kczJXJ\n"
                    + "TtCqSwGl9t9JEoFqvtW+znZ9TqyLiOMw7TGEUI+88VAqW0qmXnwPcfo=\n";

    private static final String FISH_PRIV_KEY_BASE64 =
            "MIICXgIBAAKBgQDo80oYdl8ZP7HPWOl/QwcJlYA1xQ/+cTEngZkSJiCl349q/EJV\n"
                    + "Oe4JOInZ4DbErAGCW9U55vmPB4jf/u72oRTOqXRF2P3wF1cqguF6LhKTWqyK1xVj\n"
                    + "0bebVYAPWLwcSe0gYt22TKU66xw9oP96cabTEHgzrkvCHP2SSqHD50GkLQIDAQAB\n"
                    + "AoGBAKepW14J7F5e0ppa8wvOcUU7neCVafKHA4rcoxBF8t+P7UhiMVfn7uQiFk2D\n"
                    + "K8gXyKpLcEdRb7K7CI+3i8RkoXTRDEZU5XPMJnZsE5LWgNQ+pi3HwMEdR0vD2Iyv\n"
                    + "vIH3tq6mNKgDu+vozm8DWsEP96jrhVbo1U1rzyEtX46afo79AkEA/VXanGaqj4ua\n"
                    + "EsqfY6n/7+MTm4iPOM7qfoyI4EppJXZklc/FbcV2lAjY2Jl9U6X7WnqCPn+/zg44\n"
                    + "6lKWTnhAawJBAOtmi6nw8WjY6uyXZosE/0r4SkSSo20EJbBCJcgdofKT+VCGB4hp\n"
                    + "h6XwGdls0ca+qa5ZE1a196dpwwVre0hm88cCQQDrUm3QbHmw/39uRzOJs6dfYPKc\n"
                    + "vlwz69jdFpQqrFRBjVlf4/FDx3IfjpxHj0RgiEUUxcnoXmh/8qwh1fdzCrbjAkB4\n"
                    + "afg/chTLQUrKw5ecvW2p9+Blu20Fsv1kcDHLb/0LjU4XNrhbuz+8TlmqstOMCrPZ\n"
                    + "j48o5+RLKvqrpxNlMeS5AkEA6qIdW/yp5N8b1j2OxYZ9u5O//BvspwRITGM60Cps\n"
                    + "yemZE/ua8wm34SKvDHf5uxcmofShW17PLICrsLJ7P35y/A==\n";

    private static final String FISH_CERT_BASE64_STR =
            "_u3-7QAAAAIAAAACAAAAAgAKY2VydC1hbGlhcwAAAWB_rTdKAAVYLjUwOQAAAvkwggL1MIICXqADAgECAgkA0vwTh6lE3OcwDQYJKoZIhvcNAQEFBQAwWzEYMBYGA1UEChMPU3VwZXJmaXNoLCBJbmMuMQswCQYDVQQHEwJTRjELMAkGA1UECBMCQ0ExCzAJBgNVBAYTAlVTMRgwFgYDVQQDEw9TdXBlcmZpc2gsIEluYy4wHhcNMTQwNTEyMTYyNTI2WhcNMzQwNTA3MTYyNTI2WjBbMRgwFgYDVQQKEw9TdXBlcmZpc2gsIEluYy4xCzAJBgNVBAcTAlNGMQswCQYDVQQIEwJDQTELMAkGA1UEBhMCVVMxGDAWBgNVBAMTD1N1cGVyZmlzaCwgSW5jLjCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA6PNKGHZfGT-xz1jpf0MHCZWANcUP_nExJ4GZEiYgpd-PavxCVTnuCTiJ2eA2xKwBglvVOeb5jweI3_7u9qEUzql0Rdj98BdXKoLhei4Sk1qsitcVY9G3m1WAD1i8HEntIGLdtkylOuscPaD_enGm0xB4M65Lwhz9kkqhw-dBpC0CAwEAAaOBwDCBvTAMBgNVHRMEBTADAQH_MB0GA1UdDgQWBBT7mLNTfxRELuju1QmaXg5Whqg1iDCBjQYDVR0jBIGFMIGCgBT7mLNTfxRELuju1QmaXg5Whqg1iKFfpF0wWzEYMBYGA1UEChMPU3VwZXJmaXNoLCBJbmMuMQswCQYDVQQHEwJTRjELMAkGA1UECBMCQ0ExCzAJBgNVBAYTAlVTMRgwFgYDVQQDEw9TdXBlcmZpc2gsIEluYy6CCQDS_BOHqUTc5zANBgkqhkiG9w0BAQUFAAOBgQCkfKDsCkrHcMRxaPM7IuLcnI3Qkv5zfnIrVUSbG7RC6x-vvrrjk6PUixjClPCzpr1lNEzNJPgZC8UVCtrzV4uphs9sw-6EL4ULGRQXmLQM1JaL6RzMlclO0KpLAaX230kSgWq-1b7Odn1OrIuI4zDtMYRQj7zxUCpbSqZefA9x-gAAAAEAEW93YXNwX3phcF9yb290X2NhAAABYH-tN0oAAAK8MIICuDAOBgorBgEEASoCEQEBBQAEggKkpEg-GU1c0LSAqPTCS8ZthP0BRqVvB14nWLF8jJohHp2xHy3mOckHPsO8ecYB2NBeMvmq2QP3NpQZAmQ1pDo2pi4Dk4lCbBmfp7E6_jKWhxeI2JhhTLlIkaCymQ6H8I2MY2gEXLikMU_Q6zTFqnL1Qw0M04Xkhy0PV9reb2qM2D9B2p3YwKFrDgUk4oAH_3vvqD56v-KdyO6b0CkzpR5e9sQsjq5odRvErgKmHTgUZ1kNMVacEojrFYVdqaBPR8jreGxQJMsCuFG6ZfMMS1R9Xt1iR5O_dtWvtlpt8RBoBgLVKQLTbjBuJe6pJ9wL9gkQYSolx0naqLAJpHirkGaEmQjnGq0DdwpnMvNlgBMl4sz7U_1eD0_j_D_h-BZud5fx2WkBwBVJvl6oirrg_YMQ6q5xxqhHp3BXwYMZktIuhWKK_ESiu4tqtc3_K8bzStuvORUzjOlZuT6XuR0d57wtt5nlf8NeQGXVJFCA0b1EZivtcPDCpAm8qW4IuTVtmV7aS7q5zPSmXFvNpJ_GrOWoErCmYSn0_7dtKjObCs3l3iglr2syvE7QPwdJHSBdurg6MBaMnmFQUuWax-318T-prQxvJ7uBEY1q6dHHlDUmUaRZ3-CHRVGrFtuXanOYSLCZwZAvz4yrR7qU2ct6HJVMfd7-obhHtptlzSJdlBT-ssvuKoNApvo06VTNUB3TAXwED6T5JZciUSD3iA0T4qkzgyRSAnru9zAsinfKyONZPJIvk2z-h0FljdtaqSJJT4wz0-ASKbPzMfMLNOhtU2p2DB4Tbc9X63COU2VwyC4iDasPTg1AralosvKUMje4sPcJBwoaBHsJmHd0xQowfQa0mIpZGPe-HnXesGSNH_PFNlykzKxD4aIcx4syqIFedT1il0FNiwAAAAEABVguNTA5AAAC-TCCAvUwggJeoAMCAQICCQDS_BOHqUTc5zANBgkqhkiG9w0BAQUFADBbMRgwFgYDVQQKEw9TdXBlcmZpc2gsIEluYy4xCzAJBgNVBAcTAlNGMQswCQYDVQQIEwJDQTELMAkGA1UEBhMCVVMxGDAWBgNVBAMTD1N1cGVyZmlzaCwgSW5jLjAeFw0xNDA1MTIxNjI1MjZaFw0zNDA1MDcxNjI1MjZaMFsxGDAWBgNVBAoTD1N1cGVyZmlzaCwgSW5jLjELMAkGA1UEBxMCU0YxCzAJBgNVBAgTAkNBMQswCQYDVQQGEwJVUzEYMBYGA1UEAxMPU3VwZXJmaXNoLCBJbmMuMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDo80oYdl8ZP7HPWOl_QwcJlYA1xQ_-cTEngZkSJiCl349q_EJVOe4JOInZ4DbErAGCW9U55vmPB4jf_u72oRTOqXRF2P3wF1cqguF6LhKTWqyK1xVj0bebVYAPWLwcSe0gYt22TKU66xw9oP96cabTEHgzrkvCHP2SSqHD50GkLQIDAQABo4HAMIG9MAwGA1UdEwQFMAMBAf8wHQYDVR0OBBYEFPuYs1N_FEQu6O7VCZpeDlaGqDWIMIGNBgNVHSMEgYUwgYKAFPuYs1N_FEQu6O7VCZpeDlaGqDWIoV-kXTBbMRgwFgYDVQQKEw9TdXBlcmZpc2gsIEluYy4xCzAJBgNVBAcTAlNGMQswCQYDVQQIEwJDQTELMAkGA1UEBhMCVVMxGDAWBgNVBAMTD1N1cGVyZmlzaCwgSW5jLoIJANL8E4epRNznMA0GCSqGSIb3DQEBBQUAA4GBAKR8oOwKSsdwxHFo8zsi4tycjdCS_nN-citVRJsbtELrH6--uuOTo9SLGMKU8LOmvWU0TM0k-BkLxRUK2vNXi6mGz2zD7oQvhQsZFBeYtAzUlovpHMyVyU7QqksBpfbfSRKBar7Vvs52fU6si4jjMO0xhFCPvPFQKltKpl58D3H6JOMFodMMw2IeCb55eEfrh6zCSzM=";

    @Test
    void shouldReturnEmptyByteArrayIfNotAbleToFindCertSectionInPemData() {
        // Given
        String pem = CERT_DATA_BASE64;
        // When
        byte[] cert = SslCertificateUtils.extractCertificate(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    void shouldReturnEmptyByteArrayIfBeginCertTokenWasNotFoundInPemData() {
        // Given
        String pem = CERT_DATA_BASE64 + SslCertificateUtils.END_CERTIFICATE_TOKEN;
        // When
        byte[] cert = SslCertificateUtils.extractCertificate(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    void shouldReturnEmptyByteArrayIfEndCertTokenWasNotFoundInPemData() {
        // Given
        String pem = SslCertificateUtils.BEGIN_CERTIFICATE_TOKEN + CERT_DATA_BASE64;
        // When
        byte[] cert = SslCertificateUtils.extractCertificate(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    void shouldReturnEmptyByteArrayIfEndCertTokenIsBeforeBeginCertTokenInPemData() {
        // Given
        String pem =
                SslCertificateUtils.END_CERTIFICATE_TOKEN
                        + CERT_DATA_BASE64
                        + SslCertificateUtils.BEGIN_CERTIFICATE_TOKEN;
        // When
        byte[] cert = SslCertificateUtils.extractCertificate(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    void shouldReturnCertificateBetweenBeginAndEndCertTokensFromPemData() {
        // Given
        String pem =
                SslCertificateUtils.BEGIN_CERTIFICATE_TOKEN
                        + CERT_DATA_BASE64
                        + SslCertificateUtils.END_CERTIFICATE_TOKEN;
        // When
        byte[] cert = SslCertificateUtils.extractCertificate(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(CERT_DATA.length())));
        assertThat(cert, is(equalTo(CERT_DATA.getBytes(StandardCharsets.US_ASCII))));
    }

    @Test
    void shouldReturnEmptyByteArrayIfNotAbleToFindPrivKeySectionInPemData() {
        // Given
        String pem = PRIV_KEY_BASE64;
        // When
        byte[] cert = SslCertificateUtils.extractPrivateKey(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    void shouldReturnEmptyByteArrayIfBeginPrivKeyTokenWasNotFoundInPemData() {
        // Given
        String pem = PRIV_KEY_BASE64 + SslCertificateUtils.END_PRIVATE_KEY_TOKEN;
        // When
        byte[] cert = SslCertificateUtils.extractPrivateKey(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    void shouldReturnEmptyByteArrayIfEndPrivKeyTokenWasNotFoundInPemData() {
        // Given
        String pem = SslCertificateUtils.BEGIN_PRIVATE_KEY_TOKEN + PRIV_KEY_BASE64;
        // When
        byte[] cert = SslCertificateUtils.extractPrivateKey(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    void shouldReturnEmptyByteArrayIfEndPrivKeyTokenIsBeforeBeginPrivKeyTokenInPemData() {
        // Given
        String pem =
                SslCertificateUtils.END_PRIVATE_KEY_TOKEN
                        + PRIV_KEY_BASE64
                        + SslCertificateUtils.BEGIN_PRIVATE_KEY_TOKEN;
        // When
        byte[] cert = SslCertificateUtils.extractPrivateKey(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(0)));
    }

    @Test
    void shouldReturnPrivateKeyBetweenBeginAndEndPrivKeyTokensFromPemData() {
        // Given
        String pem =
                SslCertificateUtils.BEGIN_PRIVATE_KEY_TOKEN
                        + PRIV_KEY_BASE64
                        + SslCertificateUtils.END_PRIVATE_KEY_TOKEN;
        // When
        byte[] cert = SslCertificateUtils.extractPrivateKey(pem);
        // Then
        assertThat(cert, is(notNullValue()));
        assertThat(cert.length, is(equalTo(PRIV_KEY_DATA.length())));
        assertThat(cert, is(equalTo(PRIV_KEY_DATA.getBytes(StandardCharsets.US_ASCII))));
    }

    @Test
    void shouldConvertPem2Keystore() throws Exception {
        Provider provider = new BouncyCastleProvider();
        try {
            // Given
            Security.addProvider(provider);
            byte[] cert = Base64.getMimeDecoder().decode(FISH_CERT_BASE64);
            byte[] key = Base64.getMimeDecoder().decode(FISH_PRIV_KEY_BASE64);
            // When
            KeyStore ks = SslCertificateUtils.pem2KeyStore(cert, key);
            // Then
            assertThat(ks, is(notNullValue()));
            assertThat(ks.getCertificate("cert-alias"), is(notNullValue()));
        } finally {
            Security.removeProvider(provider.getName());
        }
    }

    @Test
    void shouldConvertStringCertToAndFromKeyStore() throws Exception {
        // Given
        String certBase64 = FISH_CERT_BASE64_STR;
        // When
        KeyStore ks = SslCertificateUtils.string2Keystore(certBase64);
        String newCertBase64 = SslCertificateUtils.keyStore2String(ks);
        // Then
        assertThat(newCertBase64, is(equalTo(certBase64)));
    }
}

/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.zaproxy.zap.network.SocksProxy.Version;

/** Unit test for {@link SocksProxy}. */
class SocksProxyUnitTest {

    private static final String HOST = "localhost";
    private static final int PORT = 1080;

    @Test
    void shouldNotCreateSocksProxyWithNullHost() {
        // Given
        String host = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> new SocksProxy(host, PORT));
        // When / Then
        assertThrows(
                NullPointerException.class, () -> new SocksProxy(host, PORT, Version.SOCKS5, true));
    }

    @Test
    void shouldNotCreateSocksProxyWithEmptyHost() {
        // Given
        String host = "";
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> new SocksProxy(host, PORT));
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new SocksProxy(host, PORT, Version.SOCKS5, true));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 65546})
    void shouldNotCreateSocksProxyWithInvalidPort(int port) {
        // Given port
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> new SocksProxy(HOST, port));
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new SocksProxy(HOST, port, Version.SOCKS5, true));
    }

    @Test
    void shouldNotCreateSocksProxyWithNullVersion() {
        // Given
        Version version = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> new SocksProxy(HOST, PORT, version, true));
    }

    @Test
    void shouldCreateSocksProxy() {
        // Given
        String host = "127.0.1.1";
        int port = 1234;
        // When
        SocksProxy socksProxy = new SocksProxy(host, port);
        // Then
        assertThat(socksProxy.getHost(), is(equalTo(host)));
        assertThat(socksProxy.getPort(), is(equalTo(port)));
        assertThat(socksProxy.getVersion(), is(equalTo(Version.SOCKS5)));
        assertThat(socksProxy.isUseDns(), is(equalTo(true)));

        // Given
        Version version = Version.SOCKS4a;
        boolean useDns = false;
        // When
        socksProxy = new SocksProxy(host, port, version, useDns);
        // Then
        assertThat(socksProxy.getHost(), is(equalTo(host)));
        assertThat(socksProxy.getPort(), is(equalTo(port)));
        assertThat(socksProxy.getVersion(), is(equalTo(version)));
        assertThat(socksProxy.isUseDns(), is(equalTo(useDns)));
    }

    @ParameterizedTest
    @MethodSource
    void shouldProduceConsistentHashCodes(SocksProxy instance, int hashCode) {
        // Given instance, hashCode
        // When / Then
        assertThat(instance.hashCode(), is(equalTo(hashCode)));
    }

    static Stream<Arguments> shouldProduceConsistentHashCodes() {
        return Stream.of(
                arguments(new SocksProxy(HOST, PORT), -1995911588),
                arguments(new SocksProxy(HOST, PORT, Version.SOCKS4a, true), -1995911589),
                arguments(new SocksProxy("127.0.0.1", 9150, Version.SOCKS5, false), -26093838));
    }

    @Test
    void shouldBeEqualToItself() {
        // Given
        SocksProxy socksProxy = new SocksProxy(HOST, PORT);
        // When
        boolean equals = socksProxy.equals(socksProxy);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    void shouldBeEqualToDifferentSocksProxyWithSameContents() {
        // Given
        SocksProxy socksProxy = new SocksProxy(HOST, PORT, Version.SOCKS4a, false);
        SocksProxy otherEqualSocksProxy = new SocksProxy(HOST, PORT, Version.SOCKS4a, false);
        // When
        boolean equals = socksProxy.equals(otherEqualSocksProxy);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    void shouldNotBeEqualToNull() {
        // Given
        SocksProxy socksProxy = new SocksProxy(HOST, PORT, Version.SOCKS4a, false);
        // When
        boolean equals = socksProxy.equals(null);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    void shouldNotBeEqualToSocksProxyWithJustDifferentHost() {
        // Given
        SocksProxy socksProxy = new SocksProxy(HOST, PORT);
        SocksProxy otherSocksProxy = new SocksProxy("example.com", PORT);
        // When
        boolean equals = socksProxy.equals(otherSocksProxy);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    void shouldNotBeEqualToSocksProxyWithJustDifferentPort() {
        // Given
        SocksProxy socksProxy = new SocksProxy(HOST, PORT);
        SocksProxy otherSocksProxy = new SocksProxy(HOST, 1234);
        // When
        boolean equals = socksProxy.equals(otherSocksProxy);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    void shouldNotBeEqualToSocksProxyWithJustDifferentVersion() {
        // Given
        SocksProxy socksProxy = new SocksProxy(HOST, PORT, Version.SOCKS4a, false);
        SocksProxy otherSocksProxy = new SocksProxy(HOST, PORT, Version.SOCKS5, false);
        // When
        boolean equals = socksProxy.equals(otherSocksProxy);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    void shouldNotBeEqualToSocksProxyWithJustDifferentUseDns() {
        // Given
        SocksProxy socksProxy = new SocksProxy(HOST, PORT, Version.SOCKS4a, false);
        SocksProxy otherSocksProxy = new SocksProxy(HOST, PORT, Version.SOCKS4a, true);
        // When
        boolean equals = socksProxy.equals(otherSocksProxy);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    void shouldNotBeEqualToExtendedSocksProxy() {
        // Given
        SocksProxy socksProxy = new SocksProxy(HOST, PORT);
        SocksProxy otherSocksProxy = new SocksProxy(HOST, PORT) {
                    // Anonymous SocksProxy
                };
        // When
        boolean equals = socksProxy.equals(otherSocksProxy);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @ParameterizedTest
    @MethodSource
    void shouldProduceConsistentStringRepresentations(
            SocksProxy socksProxy, String representation) {
        // Given socksProxy, representation
        // When / Then
        assertThat(socksProxy.toString(), is(equalTo(representation)));
    }

    static Stream<Arguments> shouldProduceConsistentStringRepresentations() {
        return Stream.of(
                arguments(
                        new SocksProxy("127.0.0.1", 1234),
                        "[Host=127.0.0.1, Port=1234, Version=5, UseDns=true]"),
                arguments(
                        new SocksProxy("localhost", 1080, Version.SOCKS4a, false),
                        "[Host=localhost, Port=1080, Version=4, UseDns=false]"));
    }

    @Test
    void shouldGetSocks4From4() {
        // Given
        String value = "4";
        // When
        Version version = Version.from(value);
        // Then
        assertThat(version, is(equalTo(Version.SOCKS4a)));
    }

    @Test
    void shouldGetSocks5From5() {
        // Given
        String value = "5";
        // When
        Version version = Version.from(value);
        // Then
        assertThat(version, is(equalTo(Version.SOCKS5)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"3", "NotAVersion"})
    void shouldGetSocks5FromInvalidValues(String value) {
        // Given value
        // When
        Version version = Version.from(value);
        // Then
        assertThat(version, is(equalTo(Version.SOCKS5)));
    }

    @ParameterizedTest
    @MethodSource
    void shouldGetExpectedVersionNumberFromVersion(Version version, int number) {
        // Given version, number
        // When / Then
        assertThat(version.number(), is(equalTo(number)));
    }

    static Stream<Arguments> shouldGetExpectedVersionNumberFromVersion() {
        return Stream.of(arguments(Version.SOCKS4a, 4), arguments(Version.SOCKS5, 5));
    }
}

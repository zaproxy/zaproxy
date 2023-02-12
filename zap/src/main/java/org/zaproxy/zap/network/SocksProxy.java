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

import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A SOCKS proxy.
 *
 * <p>Contains the host, port, version, and if names should be resolved by the proxy.
 *
 * @since 2.10.0
 * @deprecated (2.12.0) No longer in use.
 */
@Deprecated
public class SocksProxy {

    private static final Logger logger = LogManager.getLogger(SocksProxy.class);

    /** The version of the SOCKS proxy. */
    public enum Version {
        /** Version 4. */
        SOCKS4a(4),
        /**
         * Version 5.
         *
         * <p>Can resolve names and require authentication.
         */
        SOCKS5(5);

        private final int number;

        Version(int number) {
            this.number = number;
        }

        /**
         * Gets the number of the version.
         *
         * @return the number of the version.
         */
        public int number() {
            return number;
        }

        /**
         * Gets a {@code Version} from the given value (version number).
         *
         * <p>Defaults to {@link #SOCKS5} when the {@code value} is:
         *
         * <ul>
         *   <li>null
         *   <li>empty
         *   <li>not a number
         *   <li>unknown version
         * </ul>
         *
         * @param value the value to convert
         * @return the version.
         */
        public static Version from(String value) {
            if (value == null || value.isEmpty()) {
                return SOCKS5;
            }
            int number;
            try {
                number = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse the version: {}", value, e);
                return SOCKS5;
            }

            if (number == SOCKS4a.number) {
                return SOCKS4a;
            }
            if (number == SOCKS5.number) {
                return SOCKS5;
            }

            logger.warn("Unknown version: {}", value);
            return SOCKS5;
        }
    }

    private final String host;
    private final int port;
    private final Version version;
    private final boolean useDns;

    /**
     * Constructs a {@code SocksProxy} with the given host and port.
     *
     * <p>Uses {@link Version#SOCKS5} and names are resolved by the proxy.
     *
     * @param host the host, must not be {@code null} or empty.
     * @param port the port.
     * @throws NullPointerException if the {@code host} is null.
     * @throws IllegalArgumentException if the {@code host} is empty or the {@code port} is not a
     *     valid port number.
     */
    public SocksProxy(String host, int port) {
        this(host, port, Version.SOCKS5, true);
    }

    /**
     * Constructs a {@code SocksProxy} with the given data.
     *
     * @param host the host, must not be {@code null} or empty.
     * @param port the port.
     * @param version the version, must not be {@code null}.
     * @param useDns {@code true} if the names should be resolved by the proxy, {@code false}
     *     otherwise.
     * @throws NullPointerException if the {@code host} or {@code version} is {@code null}.
     * @throws IllegalArgumentException if the {@code host} is empty or the {@code port} is not a
     *     valid port number.
     */
    public SocksProxy(String host, int port, Version version, boolean useDns) {
        Objects.requireNonNull(host, "The host must not be null.");
        Objects.requireNonNull(version, "The version must not be null.");
        if (host.isEmpty()) {
            throw new IllegalArgumentException("The host must not be empty.");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException(
                    "The port is not valid, must be between 0 and 65535.");
        }
        this.host = host;
        this.port = port;
        this.version = version;
        this.useDns = useDns;
    }

    /**
     * Gets the host (name or address).
     *
     * @return the host, never {@code null} or empty.
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the port.
     *
     * @return the port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the version.
     *
     * @return the version, never {@code null}.
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Tells whether or not the names should be resolved by the proxy.
     *
     * <p>Only supported by {@link Version#SOCKS5}.
     *
     * @return {@code true} if the names should be resolved by the proxy, {@code false} otherwise.
     */
    public boolean isUseDns() {
        return useDns;
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, useDns, version.number);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SocksProxy other = (SocksProxy) obj;
        return Objects.equals(host, other.host)
                && port == other.port
                && useDns == other.useDns
                && version == other.version;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder(75);
        strBuilder.append("[Host=").append(host);
        strBuilder.append(", Port=").append(port);
        strBuilder.append(", Version=").append(version.number);
        strBuilder.append(", UseDns=").append(useDns);
        strBuilder.append(']');
        return strBuilder.toString();
    }
}

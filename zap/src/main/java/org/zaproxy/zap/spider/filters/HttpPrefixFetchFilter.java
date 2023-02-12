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
package org.zaproxy.zap.spider.filters;

import java.util.Arrays;
import java.util.Locale;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A {@code FetchFilter} that filters based on a HTTP or HTTPS {@code URI}.
 *
 * <p>The filtered {@code URI}s are required to start with the {@code URI} (the prefix) to be
 * considered valid.
 *
 * @since 2.5.0
 * @see #checkFilter(URI)
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class HttpPrefixFetchFilter extends FetchFilter {

    private static final Logger LOGGER = LogManager.getLogger(HttpPrefixFetchFilter.class);

    /** The normalised form of HTTP scheme, that is, all letters lowercase. */
    private static final String HTTP_SCHEME = "http";
    /** The normalised form of HTTPS scheme, that is, all letters lowercase. */
    private static final String HTTPS_SCHEME = "https";

    /** The port number that indicates that a port is the default of a scheme. */
    private static final int DEFAULT_PORT = -1;
    /**
     * The port number that indicates that a port is of an unknown scheme (that is, non HTTP and
     * HTTPS).
     */
    private static final int UNKNOWN_PORT = -2;
    /** The default port number of HTTP scheme. */
    private static final int DEFAULT_HTTP_PORT = 80;
    /** The default port number of HTTPS scheme. */
    private static final int DEFAULT_HTTPS_PORT = 443;

    /** The scheme used for filtering. Never {@code null}. */
    private final String scheme;

    /** The host used for filtering. Never {@code null}. */
    private final String host;

    /** The port used for filtering. */
    private final int port;

    /** The path used for filtering. Might be {@code null}. */
    private final char[] path;

    /**
     * Constructs a {@code HttpPrefixFetchFilter} using the given {@code URI} as prefix.
     *
     * <p>The user info, query component and fragment of the given {@code URI} are discarded. The
     * scheme and domain comparisons are done in a case insensitive way while the path component
     * comparison is case sensitive.
     *
     * @param prefix the {@code URI} that will be used as prefix
     * @throws IllegalArgumentException if any of the following conditions is {@code true}:
     *     <ul>
     *       <li>The given {@code prefix} is {@code null};
     *       <li>The given {@code prefix} has {@code null} scheme;
     *       <li>The scheme of the given {@code prefix} is not HTTP or HTTPS;
     *       <li>The given {@code prefix} has {@code null} host;
     *       <li>The given {@code prefix} has malformed host.
     *     </ul>
     */
    public HttpPrefixFetchFilter(URI prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Parameter prefix must not be null.");
        }

        char[] rawScheme = prefix.getRawScheme();
        if (rawScheme == null) {
            throw new IllegalArgumentException("Parameter prefix must have a scheme.");
        }
        String normalisedScheme = normalisedScheme(rawScheme);
        if (!isHttpOrHttps(normalisedScheme)) {
            throw new IllegalArgumentException("The prefix's scheme must be HTTP or HTTPS.");
        }
        scheme = normalisedScheme;

        if (prefix.getRawHost() == null) {
            throw new IllegalArgumentException("Parameter prefix must have a host.");
        }
        try {
            host = normalisedHost(prefix);
        } catch (URIException e) {
            throw new IllegalArgumentException("Failed to obtain the host from the prefix:", e);
        }

        port = normalisedPort(scheme, prefix.getPort());
        path = prefix.getRawPath();
    }

    /**
     * Returns the normalised form of the given {@code scheme}.
     *
     * <p>The normalisation process consists in converting the scheme to lowercase, if {@code null}
     * it is returned an empty {@code String}.
     *
     * @param scheme the scheme that will be normalised
     * @return a {@code String} with the host scheme, never {@code null}
     * @see URI#getRawScheme()
     */
    private static String normalisedScheme(char[] scheme) {
        if (scheme == null) {
            return "";
        }
        return new String(scheme).toLowerCase(Locale.ROOT);
    }

    /**
     * Tells whether or not the given {@code scheme} is HTTP or HTTPS.
     *
     * @param scheme the normalised scheme, might be {@code null}
     * @return {@code true} if the {@code scheme} is HTTP or HTTPS, {@code false} otherwise
     */
    private static boolean isHttpOrHttps(String scheme) {
        return isHttp(scheme) || isHttps(scheme);
    }

    /**
     * Tells whether or not the given {@code scheme} is HTTP.
     *
     * @param scheme the normalised scheme, might be {@code null}
     * @return {@code true} if the {@code scheme} is HTTP, {@code false} otherwise
     */
    private static boolean isHttp(String scheme) {
        return HTTP_SCHEME.equals(scheme);
    }

    /**
     * Tells whether or not the given {@code scheme} is HTTPS.
     *
     * @param scheme the normalised scheme, might be {@code null}
     * @return {@code true} if the {@code scheme} is HTTPS, {@code false} otherwise
     */
    private static boolean isHttps(String scheme) {
        return HTTPS_SCHEME.equals(scheme);
    }

    /**
     * Returns the normalised form of the host of the given {@code uri}.
     *
     * <p>The normalisation process consists in converting the host to lowercase, if {@code null} it
     * is returned an empty {@code String}.
     *
     * @param uri the URI whose host will be extracted and normalised
     * @return a {@code String} with the host normalised, never {@code null}
     * @throws URIException if the host of the given {@code uri} is malformed
     */
    private static String normalisedHost(URI uri) throws URIException {
        if (uri.getRawHost() == null) {
            return "";
        }
        return uri.getHost().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the normalised form of the given {@code port}, based on the given {@code scheme}.
     *
     * <p>If the port is non-default (as given by {@link #DEFAULT_PORT}), it's immediately returned.
     * Otherwise, for schemes HTTP and HTTPS it's returned 80 and 443, respectively, for any other
     * scheme it's returned {@link #UNKNOWN_PORT}.
     *
     * @param scheme the (normalised) scheme of the URI where the port was defined
     * @param port the port to normalise
     * @return the normalised port
     * @see #normalisedScheme(char[])
     * @see URI#getPort()
     */
    private static int normalisedPort(String scheme, int port) {
        if (port != DEFAULT_PORT) {
            return port;
        }

        if (isHttp(scheme)) {
            return DEFAULT_HTTP_PORT;
        }

        if (isHttps(scheme)) {
            return DEFAULT_HTTPS_PORT;
        }

        return UNKNOWN_PORT;
    }

    /**
     * Gets the prefix normalised, as it is used to filter the {@code URI}s.
     *
     * @return a {@code String} with the prefix normalised
     * @see #checkFilter(URI)
     */
    public String getNormalisedPrefix() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(scheme).append("://").append(host);
        if (!isDefaultHttpOrHttpsPort(scheme, port)) {
            strBuilder.append(':').append(port);
        }
        if (path != null) {
            strBuilder.append(path);
        }
        return strBuilder.toString();
    }

    /**
     * Tells whether or not the given {@code port} is the default for the given {@code scheme}.
     *
     * <p>The method returns always {@code false} for non HTTP or HTTPS schemes.
     *
     * @param scheme the scheme of a URI, might be {@code null}
     * @param port the port of a URI
     * @return {@code true} if the {@code port} is the default for the given {@code scheme}, {@code
     *     false} otherwise
     */
    private static boolean isDefaultHttpOrHttpsPort(String scheme, int port) {
        if (port == DEFAULT_HTTP_PORT && isHttp(scheme)) {
            return true;
        }
        if (port == DEFAULT_HTTPS_PORT && isHttps(scheme)) {
            return true;
        }
        return false;
    }

    /**
     * Filters any URI that does not start with the defined prefix.
     *
     * @return {@code FetchStatus.VALID} if the {@code uri} starts with the {@code prefix}, {@code
     *     FetchStatus.OUT_OF_SCOPE} otherwise
     */
    @Override
    public FetchStatus checkFilter(URI uri) {
        if (uri == null) {
            return FetchStatus.OUT_OF_SCOPE;
        }

        String otherScheme = normalisedScheme(uri.getRawScheme());
        if (port != normalisedPort(otherScheme, uri.getPort())) {
            return FetchStatus.OUT_OF_SCOPE;
        }

        if (!scheme.equals(otherScheme)) {
            return FetchStatus.OUT_OF_SCOPE;
        }

        if (!hasSameHost(uri)) {
            return FetchStatus.OUT_OF_SCOPE;
        }

        if (!startsWith(uri.getRawPath(), path)) {
            return FetchStatus.OUT_OF_SCOPE;
        }

        return FetchStatus.VALID;
    }

    /**
     * Tells whether or not the given {@code uri} has the same host as required by this prefix.
     *
     * <p>For malformed hosts it returns always {@code false}.
     *
     * @param uri the {@code URI} whose host will be checked
     * @return {@code true} if the host is same, {@code false} otherwise
     */
    private boolean hasSameHost(URI uri) {
        try {
            return host.equals(normalisedHost(uri));
        } catch (URIException e) {
            LOGGER.warn("Failed to normalise host: {}", Arrays.toString(uri.getRawHost()), e);
        }
        return false;
    }

    /**
     * Tells whether or not the given {@code array} starts with the given {@code prefix}.
     *
     * <p>The {@code prefix} might be {@code null} in which case it's considered that the {@code
     * array} starts with the prefix.
     *
     * @param array the array that will be tested if starts with the prefix, might be {@code null}
     * @param prefix the array used as prefix, might be {@code null}
     * @return {@code true} if the {@code array} starts with the {@code prefix}, {@code false}
     *     otherwise
     */
    private static boolean startsWith(char[] array, char[] prefix) {
        if (prefix == null) {
            return true;
        }

        if (array == null) {
            return false;
        }

        int length = prefix.length;
        if (array.length < length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            if (prefix[i] != array[i]) {
                return false;
            }
        }

        return true;
    }
}

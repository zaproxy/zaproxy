/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.bruteforce;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

public class ScanTarget implements Comparable<ScanTarget> {

    private static final String HTTPS_SCHEME = "https";
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final int HTTPS_DEFAULT_PORT = 443;

    private final URI uri;

    private final String scheme;
    private final String host;
    private final int port;

    private boolean scanned;

    private String stringRepresentation;
    private String htmlStringRepresentation;

    public ScanTarget(URI uri) {
        this.uri = copyURI(uri);

        this.scheme = uri.getScheme();

        try {
            this.host = uri.getHost();
        } catch (URIException e) {
            throw new IllegalArgumentException("Failed to get host from URI: " + e.getMessage(), e);
        }

        this.port = getPort(scheme, uri.getPort());

        try {
            this.uri.setPath(null);
            this.uri.setQuery(null);
            this.uri.setFragment(null);
        } catch (URIException ignore) {
            // It's safe to set the URI query, path and fragment components to null.
        }

        this.stringRepresentation = createHostPortString(host, port);
        buildHtmlStringRepresentation();
    }

    protected ScanTarget(String value) {
        this.uri = null;
        this.scheme = "";
        this.host = "";
        this.port = 0;

        this.htmlStringRepresentation = value;
    }

    public URI getURI() {
        return copyURI(uri);
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setScanned(boolean scanned) {
        if (this.scanned != scanned) {
            this.scanned = scanned;
            buildHtmlStringRepresentation();
        }
    }

    private void buildHtmlStringRepresentation() {
        if (scanned) {
            this.htmlStringRepresentation = htmlScannedTarget(stringRepresentation);
        } else {
            this.htmlStringRepresentation = htmlNotScannedTarget(stringRepresentation);
        }
    }

    private static URI copyURI(URI uri) {
        try {
            return (URI) uri.clone();
        } catch (CloneNotSupportedException ignore) {
            // Doesn't actually throw the exception.
            return null;
        }
    }

    private static String createHostPortString(String host, int port) {
        StringBuilder strBuilder = new StringBuilder(50);
        strBuilder.append(host).append(':').append(port);
        return strBuilder.toString();
    }

    private static int getPort(String scheme, int port) {
        if (port != -1) {
            return port;
        }

        if (HTTPS_SCHEME.equals(scheme)) {
            return HTTPS_DEFAULT_PORT;
        }
        return HTTP_DEFAULT_PORT;
    }

    private static String htmlScannedTarget(String site) {
        return "<html><b>" + site + "</b></html>";
    }

    private static String htmlNotScannedTarget(String site) {
        return "<html>" + site + "</html>";
    }

    public String toPlainString() {
        return stringRepresentation;
    }

    @Override
    public String toString() {
        return htmlStringRepresentation;
    }

    @Override
    public int hashCode() {
        return 31 + ((uri == null) ? 0 : uri.hashCode());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        ScanTarget other = (ScanTarget) object;
        if (uri == null) {
            if (other.uri != null) {
                return false;
            }
        } else if (!uri.equals(other.uri)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ScanTarget other) {
        return stringRepresentation.compareTo(other.stringRepresentation);
    }

}

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
package org.zaproxy.zap.network;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.cookie.CookieSpecBase;
import org.apache.commons.httpclient.cookie.MalformedCookieException;

/**
 * A {@link CookieSpecBase} that does not do any path validation.
 *
 * @since 2.7.0
 * @deprecated (2.12.0) Implementation details, do not use.
 */
@Deprecated
public class ZapCookieSpec extends CookieSpecBase {

    // ZAP: Same implementation as base class but without the path validation.
    @Override
    public void validate(String host, int port, String path, boolean secure, Cookie cookie)
            throws MalformedCookieException {
        LOG.trace("enter CookieSpecBase.validate(" + "String, port, path, boolean, Cookie)");
        if (host == null) {
            throw new IllegalArgumentException("Host of origin may not be null");
        }
        if (host.trim().equals("")) {
            throw new IllegalArgumentException("Host of origin may not be blank");
        }
        if (port < 0) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        if (path == null) {
            throw new IllegalArgumentException("Path of origin may not be null.");
        }
        host = host.toLowerCase();
        // check version
        if (cookie.getVersion() < 0) {
            throw new MalformedCookieException("Illegal version number " + cookie.getValue());
        }

        // security check... we mustn't allow the server to give us an
        // invalid domain scope

        // Validate the cookies domain attribute.  NOTE:  Domains without
        // any dots are allowed to support hosts on private LANs that don't
        // have DNS names.  Since they have no dots, to domain-match the
        // request-host and domain must be identical for the cookie to sent
        // back to the origin-server.
        if (host.indexOf(".") >= 0) {
            // Not required to have at least two dots.  RFC 2965.
            // A Set-Cookie2 with Domain=ajax.com will be accepted.

            // domain must match host
            if (!host.endsWith(cookie.getDomain())) {
                String s = cookie.getDomain();
                if (s.startsWith(".")) {
                    s = s.substring(1, s.length());
                }
                if (!host.equals(s)) {
                    throw new MalformedCookieException(
                            "Illegal domain attribute \""
                                    + cookie.getDomain()
                                    + "\". Domain of origin: \""
                                    + host
                                    + "\"");
                }
            }
        } else {
            if (!host.equals(cookie.getDomain())) {
                throw new MalformedCookieException(
                        "Illegal domain attribute \""
                                + cookie.getDomain()
                                + "\". Domain of origin: \""
                                + host
                                + "\"");
            }
        }
    }
}

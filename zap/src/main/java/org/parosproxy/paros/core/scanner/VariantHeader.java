/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

/**
 * A {@code Variant} for HTTP headers, allowing to attack the values of the headers.
 *
 * <p>Some headers are ignored as attacking them would have unwanted side effects (for example,
 * changing or removing a Proxy-Authorization header or Content-Length header).
 *
 * @since 2.2.0
 * @author andy
 * @see Variant
 * @see VariantCookie
 */
public class VariantHeader implements Variant {

    // might still be publicly used.
    @Deprecated
    public static final String[] injectableHeaders = {
        HttpRequestHeader.USER_AGENT, HttpRequestHeader.REFERER, HttpRequestHeader.HOST
    };

    // headers converted to lowercase to make comparison easier later.
    private static final String[] injectablesTempArray = {
        HttpRequestHeader.CONTENT_LENGTH.toLowerCase(
                Locale.ROOT), // scanning this would likely break the entire request
        HttpRequestHeader.PRAGMA.toLowerCase(
                Locale.ROOT), // unlikely to be picked up/used by the app itself.
        HttpRequestHeader.CACHE_CONTROL.toLowerCase(
                Locale.ROOT), // unlikely to be picked up/used by the app itself.
        HttpRequestHeader.COOKIE.toLowerCase(
                Locale.ROOT), // The Cookie header has its own variant that controls whether it is
        // scanned. Better not to scan it as a header.
        HttpRequestHeader.AUTHORIZATION.toLowerCase(
                Locale.ROOT), // scanning this would break authorisation
        HttpRequestHeader.PROXY_AUTHORIZATION.toLowerCase(
                Locale.ROOT), // scanning this would break authorisation
        HttpRequestHeader.CONNECTION.toLowerCase(
                Locale.ROOT), // scanning this would likely break the entire request
        HttpRequestHeader.PROXY_CONNECTION.toLowerCase(
                Locale.ROOT), // scanning this would likely break the entire request
        HttpRequestHeader.IF_MODIFIED_SINCE.toLowerCase(
                Locale.ROOT), // unlikely to be picked up/used by the app itself.
        HttpRequestHeader.IF_NONE_MATCH.toLowerCase(
                Locale.ROOT), // unlikely to be picked up/used by the app itself.
        HttpRequestHeader.X_CSRF_TOKEN.toLowerCase(
                Locale.ROOT), // scanning this would break authorisation
        HttpRequestHeader.X_CSRFTOKEN.toLowerCase(
                Locale.ROOT), // scanning this would break authorisation
        HttpRequestHeader.X_XSRF_TOKEN.toLowerCase(
                Locale.ROOT), // scanning this would break authorisation
        HttpRequestHeader.X_ZAP_SCAN_ID.toLowerCase(
                Locale.ROOT), // inserted by ZAP, so no need to scan it.
        HttpRequestHeader.X_ZAP_REQUESTID.toLowerCase(
                Locale.ROOT), // inserted by ZAP, so no need to scan it.
        HttpRequestHeader.X_SECURITY_PROXY.toLowerCase(
                Locale.ROOT), // unlikely to be picked up/used by the app itself.
    };
    // a hashset of (lowercase) headers that we can look up quickly and easily
    private static final HashSet<String> NON_INJECTABLE_HEADERS =
            new HashSet<>(Arrays.asList(injectablesTempArray));

    /**
     * The list of parameters (that is, headers) extracted from the request header of the message,
     * never {@code null}.
     */
    private List<NameValuePair> params = Collections.emptyList();

    private static final String SHORT_NAME = "header";

    @Override
    public String getShortName() {
        return SHORT_NAME;
    }

    /** @throws IllegalArgumentException if {@code message} is {@code null}. */
    @Override
    public void setMessage(HttpMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Parameter message must not be null.");
        }

        ArrayList<NameValuePair> extractedParameters = new ArrayList<>();
        List<HttpHeaderField> httpHeaders = message.getRequestHeader().getHeaders();
        for (HttpHeaderField header : httpHeaders) {
            if (!NON_INJECTABLE_HEADERS.contains(header.getName().toLowerCase(Locale.ROOT))) {
                extractedParameters.add(
                        new NameValuePair(
                                NameValuePair.TYPE_HEADER,
                                header.getName(),
                                header.getValue(),
                                extractedParameters.size()));
            }
        }

        if (extractedParameters.isEmpty()) {
            params = Collections.emptyList();
        } else {
            extractedParameters.trimToSize();
            params = Collections.unmodifiableList(extractedParameters);
        }
    }

    /**
     * Gets the list of parameters (that is, headers) extracted from the request header of the
     * message.
     *
     * @return an unmodifiable {@code List} containing the extracted parameters, never {@code null}.
     */
    @Override
    public List<NameValuePair> getParamList() {
        return params;
    }

    @Override
    public String setParameter(
            HttpMessage msg, NameValuePair originalPair, String name, String value) {
        msg.getRequestHeader().setHeader(originalPair.getName(), value);
        if (value == null) {
            return "";
        }
        return originalPair.getName() + ": " + value;
    }

    @Override
    public String setEscapedParameter(
            HttpMessage msg, NameValuePair originalPair, String name, String value) {
        return setParameter(msg, originalPair, name, value);
    }
}

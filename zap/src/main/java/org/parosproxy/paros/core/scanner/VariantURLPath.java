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
package org.parosproxy.paros.core.scanner;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Variant class used for URL path elements. For a URL like: {@literal
 * http://www.example.com/aaa/bbb/ccc?ddd=eee&fff=ggg} it will handle: aaa, bbb and ccc
 *
 * @author psiinon
 */
public class VariantURLPath implements Variant {

    private final Logger LOGGER = LogManager.getLogger(this.getClass());

    private static final char ESCAPE = '%';

    /*
     * The allowed characters in a path segment, from RFCs 3986 and 4234.
     */
    private static final BitSet PCHAR = new BitSet();

    static {
        PCHAR.set(':');
        PCHAR.set('@');
        for (int i = 'A'; i <= 'Z'; i++) {
            PCHAR.set(i);
        }
        for (int i = 'a'; i <= 'z'; i++) {
            PCHAR.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            PCHAR.set(i);
        }

        PCHAR.set('-');
        PCHAR.set('.');
        PCHAR.set('_');
        PCHAR.set('~');

        PCHAR.set('!');
        PCHAR.set('$');
        PCHAR.set('&');
        PCHAR.set('\'');
        PCHAR.set('(');
        PCHAR.set(')');
        PCHAR.set('*');
        PCHAR.set('+');
        PCHAR.set(',');
        PCHAR.set(';');
        PCHAR.set('=');
        PCHAR.set('[');
        PCHAR.set(']');
    }

    private final List<NameValuePair> stringParam = new ArrayList<>();
    private String[] segments;

    private static final String SHORT_NAME = "urlpath";

    @Override
    public String getShortName() {
        return SHORT_NAME;
    }

    @Override
    public void setMessage(HttpMessage msg) {
        /*
         * For a URL like: http://www.example.com/aaa/bbb/ccc?ddd=eee&fff=ggg
         * Add the following:
         * parameter	position
         *      aaa     1
         *      bbb     2
         *      ccc     3
         */
        String encodedPath = msg.getRequestHeader().getURI().getEscapedPath();
        if (encodedPath != null) {
            segments = encodedPath.split("/");
            int i = 0;
            for (String segment : segments) {
                if (segment.length() > 0) {
                    String decodedSegment = decode(segment);
                    stringParam.add(
                            new NameValuePair(
                                    NameValuePair.TYPE_URL_PATH,
                                    decodedSegment,
                                    decodedSegment,
                                    i));
                }

                i++;
            }
        }
    }

    // Adapted from URLCodec#decodeUrl
    private static String decode(String segment) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = segment.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if (b == ESCAPE) {
                int u = Character.digit(bytes[++i], 16);
                int l = Character.digit(bytes[++i], 16);
                baos.write((u << 4) + l);
            } else {
                baos.write(b);
            }
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    // Adapted from URLCodec#encodeUrl
    private static String encode(String segment) {
        if (segment == null || segment.isEmpty()) {
            return segment;
        }

        StringBuilder strBuilder = new StringBuilder();
        byte[] chars = segment.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < chars.length; i++) {
            int b = chars[i];
            if (b < 0) {
                b = 256 + b;
            }
            if (PCHAR.get(b)) {
                strBuilder.append((char) b);
            } else {
                strBuilder.append(ESCAPE).append(toHex(b >> 4)).append(toHex(b));
            }
        }
        return strBuilder.toString();
    }

    private static char toHex(int b) {
        return Character.toUpperCase(Character.forDigit(b & 0xF, 16));
    }

    @Override
    public List<NameValuePair> getParamList() {
        return stringParam;
    }

    @Override
    public String setParameter(
            HttpMessage msg, NameValuePair originalPair, String name, String value) {
        return setParameter(msg, originalPair, name, value, false);
    }

    @Override
    public String setEscapedParameter(
            HttpMessage msg, NameValuePair originalPair, String name, String value) {
        return setParameter(msg, originalPair, name, value, true);
    }

    private String setParameter(
            HttpMessage msg,
            NameValuePair originalPair,
            String name,
            String value,
            boolean escaped) {
        try {
            URI uri = msg.getRequestHeader().getURI();

            int position = originalPair.getPosition();
            if (position < segments.length) {

                String encodedValue = escaped ? value : encode(value);

                String originalValue = segments[position];
                segments[position] = encodedValue;
                String path = StringUtils.join(segments, "/");
                segments[position] = originalValue;

                try {
                    uri.setEscapedPath(path);

                } catch (URIException e) {
                    // Looks like it wasn't escaped after all
                    uri.setPath(path);
                }
            }

        } catch (URIException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return value;
    }
}

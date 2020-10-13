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
package org.zaproxy.zap.extension.httppanel.view.impl.models.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpBody;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;

public final class HttpPanelViewModelUtils {

    private static final Pattern GZIP_PATTERN =
            Pattern.compile("\\s*(?:x-)?gzip\\s*", Pattern.CASE_INSENSITIVE);

    private static final Logger logger = Logger.getLogger(HttpPanelViewModelUtils.class);

    private HttpPanelViewModelUtils() {}

    public static void updateRequestContentLength(HttpMessage message) {
        message.getRequestHeader().setContentLength(message.getRequestBody().length());
    }

    public static void updateResponseContentLength(HttpMessage message) {
        message.getResponseHeader().setContentLength(message.getResponseBody().length());
    }

    public static byte[] getBodyBytes(HttpHeader header, HttpBody body) {
        if (!isEncoded(header)) {
            return body.getBytes();
        }

        try {
            return decode(body);
        } catch (IOException e) {
            logger.debug("Failed to decode the body:", e);
            return body.getBytes();
        }
    }

    private static boolean isEncoded(HttpHeader header) {
        String encoding = header.getHeader(HttpHeader.CONTENT_ENCODING);
        return encoding != null && GZIP_PATTERN.matcher(encoding).matches();
    }

    private static byte[] decode(HttpBody body) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes());
                GZIPInputStream gis = new GZIPInputStream(bais);
                BufferedInputStream bis = new BufferedInputStream(gis);
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        }
    }

    public static String getBodyString(HttpHeader header, HttpBody body) {
        if (!isEncoded(header)) {
            return body.toString();
        }

        try {
            return new String(decode(body), body.getCharset());
        } catch (UnsupportedEncodingException ignore) {
            // Shouldn't happen, the body has a supported charset.
        } catch (IOException e) {
            logger.debug("Failed to decode the body:", e);
        }
        return body.toString();
    }

    public static void setBody(HttpHeader header, HttpBody body, String value) {
        body.setCharset(header.getCharset());

        if (!isEncoded(header)) {
            body.setBody(value);
            return;
        }

        try {
            setBodyGzip(header, body, value.getBytes(body.getCharset()));
        } catch (UnsupportedEncodingException ignore) {
            // Shouldn't happen, the body has a supported charset.
        }
    }

    private static void setBodyGzip(HttpHeader header, HttpBody body, byte[] value) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream gis = new GZIPOutputStream(baos, true)) {
            gis.write(value);
            gis.finish();
            body.setBody(baos.toByteArray());
        } catch (IOException e) {
            logger.error("Failed to encode and set the body:", e);
        }
    }

    public static void setBody(HttpHeader header, HttpBody body, byte[] value) {
        body.setCharset(header.getCharset());

        if (!isEncoded(header)) {
            body.setBody(value);
            return;
        }

        setBodyGzip(header, body, value);
    }
}

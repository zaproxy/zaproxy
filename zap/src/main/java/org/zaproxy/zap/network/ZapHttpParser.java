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
package org.zaproxy.zap.network;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A utility class to parse HTTP response headers.
 *
 * <p>Used to override the {@code HttpClient} behaviour to accept HTTP responses which contain
 * malformed HTTP header lines. <strong>Note:</strong> Malformed HTTP header lines are ignored
 * (instead of throwing an exception).
 *
 * @deprecated (2.12.0) Implementation details, do not use.
 */
@Deprecated
public class ZapHttpParser {

    private static final Logger logger = LogManager.getLogger(ZapHttpParser.class);

    private ZapHttpParser() {}

    /*
     * Implementation copied from HttpParser#parseHeaders(InputStream, String) except that no exception is thrown in case of
     * malformed HTTP header lines.
     */
    @SuppressWarnings({"rawtypes", "unchecked", "null"})
    public static Header[] parseHeaders(InputStream is, String charset) throws IOException {
        ArrayList headers = new ArrayList();
        String name = null;
        StringBuffer value = null;
        for (; ; ) {
            String line = HttpParser.readLine(is, charset);
            if ((line == null) || (line.trim().length() < 1)) {
                break;
            }

            // Parse the header name and value
            // Check for folded headers first
            // Detect LWS-char see HTTP/1.0 or HTTP/1.1 Section 2.2
            // discussion on folded headers
            if ((line.charAt(0) == ' ') || (line.charAt(0) == '\t')) {
                // we have continuation folded header
                // so append value
                if (value != null) {
                    value.append(' ');
                    value.append(line.trim());
                }
            } else {
                // make sure we save the previous name,value pair if present
                if (name != null) {
                    headers.add(new Header(name, value.toString()));
                }

                // Otherwise we should have normal HTTP header line
                // Parse the header name and value
                int colon = line.indexOf(":");
                if (colon < 0) {
                    // Do not thrown the exception ignore it instead
                    // throw new ProtocolException("Unable to parse header: " + line);
                    logger.warn("Ignoring malformed HTTP header line: \"{}\"", line);
                    name = null;
                    value = null;
                } else {
                    name = line.substring(0, colon).trim();
                    value = new StringBuffer(line.substring(colon + 1).trim());
                }
            }
        }

        // make sure we save the last name,value pair if present
        if (name != null) {
            headers.add(new Header(name, value.toString()));
        }

        return (Header[]) headers.toArray(new Header[headers.size()]);
    }
}

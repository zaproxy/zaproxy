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

import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * The {@link HttpEncoding} for the {@code deflate} coding.
 *
 * @since 2.10.0
 */
public class HttpEncodingDeflate extends AbstractStreamHttpEncoding {

    private static final HttpEncodingDeflate SINGLETON = new HttpEncodingDeflate();

    private HttpEncodingDeflate() {
        super(
                DeflaterOutputStream::new,
                is -> {
                    if (is.available() < 2) {
                        throw new IOException("Content malformed");
                    }

                    is.mark(0);
                    Inflater inflater = new Inflater(isNoWrap(is.read(), is.read()));
                    is.reset();
                    return new InflaterInputStream(is, inflater);
                });
    }

    // Logic from Apache HttpClient's DeflateInputStream.
    private static boolean isNoWrap(int i1, int i2) {
        int b1 = i1 & 0xFF;
        int compressionMethod = b1 & 0xF;
        int compressionInfo = b1 >> 4 & 0xF;
        int b2 = i2 & 0xFF;
        if (compressionMethod == 8 && compressionInfo <= 7 && ((b1 << 8) | b2) % 31 == 0) {
            return false;
        }
        return true;
    }

    /**
     * Gets the singleton.
     *
     * @return the GZIP content encoding.
     */
    public static HttpEncodingDeflate getSingleton() {
        return SINGLETON;
    }
}

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

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The {@link HttpEncoding} for the {@code gzip}/{@code x-gzip} coding.
 *
 * @since 2.10.0
 */
public class HttpEncodingGzip extends AbstractStreamHttpEncoding {

    private static final HttpEncodingGzip SINGLETON = new HttpEncodingGzip();

    private HttpEncodingGzip() {
        super(GZIPOutputStream::new, GZIPInputStream::new);
    }

    /**
     * Gets the singleton.
     *
     * @return the GZIP content encoding.
     */
    public static HttpEncodingGzip getSingleton() {
        return SINGLETON;
    }
}

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

abstract class AbstractStreamHttpEncoding implements HttpEncoding {

    private static final int BUFFER_SIZE = 2048;

    private final OutputStreamSupplier outputStreamSupplier;
    private final InputStreamSupplier inputStreamSupplier;

    protected AbstractStreamHttpEncoding(
            OutputStreamSupplier outputStreamSupplier, InputStreamSupplier inputStreamSupplier) {
        this.outputStreamSupplier = outputStreamSupplier;
        this.inputStreamSupplier = inputStreamSupplier;
    }

    @Override
    public byte[] encode(byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStream os = outputStreamSupplier.get(baos)) {
            os.write(content);
        }
        return baos.toByteArray();
    }

    @Override
    public byte[] decode(byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(content);
                InputStream is = inputStreamSupplier.get(bais)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        }
        return baos.toByteArray();
    }

    protected interface OutputStreamSupplier {
        OutputStream get(ByteArrayOutputStream os) throws IOException;
    }

    protected interface InputStreamSupplier {
        InputStream get(ByteArrayInputStream os) throws IOException;
    }
}

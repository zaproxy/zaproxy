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

/**
 * A HTTP encoding, for a given content coding.
 *
 * @since 2.10.0
 */
public interface HttpEncoding {

    /**
     * Encodes the given content.
     *
     * @param content the content to encode.
     * @return the content encoded.
     * @throws IOException if an error occurred while encoding the content.
     */
    byte[] encode(byte[] content) throws IOException;

    /**
     * Decodes the given content.
     *
     * @param content the content to decode.
     * @return the decoded content.
     * @throws IOException if an error occurred while decoding the content.
     */
    byte[] decode(byte[] content) throws IOException;
}

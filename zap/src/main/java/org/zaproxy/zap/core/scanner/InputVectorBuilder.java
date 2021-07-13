/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
package org.zaproxy.zap.core.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.parosproxy.paros.core.scanner.NameValuePair;
import org.zaproxy.zap.core.scanner.InputVector.PayloadFormat;

/**
 * {@code InputVectorBuilder} provides an easy way to build {@code InputVectors} and validates the
 * provided inputs.
 *
 * @author preetkaran20@gmail.com KSASAN
 * @since 2.11.0
 */
public class InputVectorBuilder {

    private Map<Integer, InputVector> inputVectorsMap = new HashMap<>();

    /**
     * Note: calling this method with same NameValuePair will overwrite the earlier name and value
     * with the newer one.
     *
     * @param nameValuePair
     * @param param
     * @param value
     * @param namePayloadFormat
     * @param valuePayloadFormat
     * @return {@code InputVectorBuilder}
     */
    public InputVectorBuilder setNameAndValue(
            NameValuePair nameValuePair,
            String param,
            String value,
            PayloadFormat namePayloadFormat,
            PayloadFormat valuePayloadFormat) {
        inputVectorsMap.put(
                nameValuePair.getPosition(),
                new InputVector(
                        nameValuePair.getPosition(),
                        param,
                        value,
                        namePayloadFormat,
                        valuePayloadFormat));
        return this;
    }

    /**
     * Note: calling this method with same NameValuePair will overwrite the earlier name and value
     * with the newer one.
     *
     * @param nameValuePair
     * @param value
     * @param payloadFormat
     * @return {@code InputVectorBuilder}
     */
    public InputVectorBuilder setValue(
            NameValuePair nameValuePair, String value, PayloadFormat payloadFormat) {
        return this.setNameAndValue(
                nameValuePair,
                nameValuePair.getName(),
                value,
                PayloadFormat.ALREADY_ESCAPED,
                payloadFormat);
    }

    /**
     * Note: calling this method with same NameValuePair will overwrite the earlier name and value
     * with the newer one.
     *
     * @param nameValuePair
     * @param param
     * @param payloadFormat
     * @return {@code InputVectorBuilder}
     */
    public InputVectorBuilder setName(
            NameValuePair nameValuePair, String param, PayloadFormat payloadFormat) {
        return this.setNameAndValue(
                nameValuePair,
                param,
                nameValuePair.getValue(),
                payloadFormat,
                PayloadFormat.ALREADY_ESCAPED);
    }

    public List<InputVector> build() {
        return new ArrayList<>(inputVectorsMap.values());
    }
}

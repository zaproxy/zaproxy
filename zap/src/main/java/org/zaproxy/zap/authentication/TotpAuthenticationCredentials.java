/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
package org.zaproxy.zap.authentication;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An {@link AuthenticationCredentials} that supports TOTP codes.
 *
 * @since 2.16.1
 */
public abstract class TotpAuthenticationCredentials implements AuthenticationCredentials {

    private static final Logger LOGGER = LogManager.getLogger(TotpAuthenticationCredentials.class);

    private static final String TOTP_ALGORITHM = "totpAlgorithm";
    private static final String TOTP_DIGITS = "totpDigits";
    private static final String TOTP_PERIOD = "totpPeriod";
    private static final String TOTP_SECRET = "totpSecret";

    private static final List<String> API_PARAMETERS =
            List.of(TOTP_ALGORITHM, TOTP_DIGITS, TOTP_PERIOD, TOTP_SECRET);

    private static TotpGenerator generator;

    private final boolean enabled;
    private TotpData totpData;

    protected TotpAuthenticationCredentials(boolean enabled) {
        this.enabled = enabled;
        totpData = TotpData.EMPTY;
    }

    public boolean isTotpEnabled() {
        return TotpAuthenticationCredentials.generator != null && enabled;
    }

    public TotpData getTotpData() {
        return totpData;
    }

    public void setTotpData(TotpData totpData) {
        this.totpData = totpData == null ? TotpData.EMPTY : totpData;
    }

    @Override
    public String getTotpCode(Instant when) {
        if (!enabled) {
            return null;
        }

        if (when == null) {
            throw new IllegalArgumentException("Parameter when must not be null.");
        }

        TotpGenerator impl = generator;
        if (impl != null) {
            try {
                return impl.generate(totpData, when);
            } catch (Exception e) {
                LOGGER.warn("An error occurred while generating the code:", e);
            }
        }
        return null;
    }

    protected void setTotpData(Map<String, Object> map) {
        if (!enabled) {
            return;
        }

        map.put(TOTP_SECRET, totpData.secret());
        map.put(TOTP_PERIOD, totpData.period());
        map.put(TOTP_DIGITS, totpData.digits());
        map.put(TOTP_ALGORITHM, totpData.algorithm());
    }

    protected void readTotpData(JSONObject params) {
        if (!enabled) {
            return;
        }

        String secret = params.optString(TOTP_SECRET);
        if (secret.isEmpty()) {
            return;
        }

        setTotpData(
                new TotpData(
                        secret,
                        params.optInt(TOTP_PERIOD, TotpData.EMPTY.period()),
                        params.optInt(TOTP_DIGITS, TotpData.EMPTY.digits()),
                        params.optString(TOTP_ALGORITHM, TotpData.EMPTY.algorithm())));
    }

    protected void encodeTotpData(StringBuilder out, String separator) {
        if (!enabled) {
            return;
        }

        out.append(base64Encode(totpData.secret()))
                .append(separator)
                .append(totpData.period())
                .append(separator)
                .append(totpData.digits())
                .append(separator)
                .append(base64Encode(totpData.algorithm()))
                .append(separator);
    }

    private static String base64Encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    protected void decodeTotpData(List<String> data) {
        if (!enabled || data.size() != 4) {
            return;
        }

        setTotpData(
                new TotpData(
                        base64Decode(data.get(0)),
                        getInt(data.get(1), TotpData.EMPTY.period()),
                        getInt(data.get(2), TotpData.EMPTY.digits()),
                        base64Decode(data.get(3))));
    }

    private static String base64Decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private static int getInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("An error occurred while parsing: {}", value, e);
        }
        return defaultValue;
    }

    public static record TotpData(String secret, int period, int digits, String algorithm) {

        public static final TotpData EMPTY = new TotpData("");

        public TotpData(String secret) {
            this(secret, 30, 6, "SHA1");
        }
    }

    public static List<String> getApiParameters() {
        return API_PARAMETERS;
    }

    public static void setTotpGenerator(TotpGenerator generator) {
        TotpAuthenticationCredentials.generator = generator;
    }

    static TotpGenerator getGenerator() {
        return generator;
    }

    /** The generator of TOTP codes. */
    public interface TotpGenerator {

        /**
         * Generates a TOTP code from the given data.
         *
         * @param data the data to use for the code generation.
         * @param when for when the code should be generated.
         * @return the code.
         */
        String generate(TotpData data, Instant when);

        /**
         * Gets the names of the supported algorithms.
         *
         * @return the names, never {@code null}.
         */
        List<String> getSupportedAlgorithms();
    }
}

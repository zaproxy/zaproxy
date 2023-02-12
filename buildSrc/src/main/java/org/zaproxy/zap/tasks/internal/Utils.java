/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.zaproxy.zap.tasks.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

public final class Utils {

    private static final String ADD_ON_ELEMENT = "addon";
    private static final String ADD_ON_NODE_PREFIX = "addon_";

    private Utils() {}

    public static Map<String, MarketplaceAddOn> getZapVersionsAddOns(Path zapVersionsFile)
            throws IOException {
        XMLConfiguration zapVersionsXml = new ZapXmlConfiguration();
        try {
            zapVersionsXml.load(zapVersionsFile.toFile());
        } catch (ConfigurationException e) {
            throw new IOException(e);
        }

        Map<String, MarketplaceAddOn> addOns = new HashMap<>();
        Stream.of(zapVersionsXml.getStringArray(ADD_ON_ELEMENT))
                .forEach(
                        id -> {
                            String key = ADD_ON_NODE_PREFIX + id;
                            String url = zapVersionsXml.getString(key + ".url");
                            String hash = zapVersionsXml.getString(key + ".hash");
                            addOns.put(id, new MarketplaceAddOn(id, url, hash));
                        });

        return addOns;
    }

    public static MainAddOnsData parseData(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file)) {
            return new ObjectMapper(new YAMLFactory()).readValue(reader, MainAddOnsData.class);
        }
    }

    public static String hash(Path file, MainAddOn addOn) throws IOException {
        String hash = addOn.getHash();
        String algorithm = hash.substring(0, hash.indexOf(':'));
        try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
            MessageDigest diggest = MessageDigest.getInstance(algorithm);

            int read = 0;
            byte[] buffer = new byte[4096];
            while ((read = is.read(buffer)) != -1) {
                diggest.update(buffer, 0, read);
            }

            StringBuilder sb = new StringBuilder(algorithm);
            sb.append(':');
            for (byte b : diggest.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }
}

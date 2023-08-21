/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.model;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @deprecated (2.14.0) The vulnerabilities were moved to Common Library add-on.
 */
@SuppressWarnings("removal")
@Deprecated(since = "2.14.0", forRemoval = true)
public final class Vulnerabilities {

    private static final Logger LOGGER = LogManager.getLogger(Vulnerabilities.class);

    private static Provider provider;

    private Vulnerabilities() {}

    public static void setProvider(Provider provider) {
        Vulnerabilities.provider = provider;
    }

    /**
     * Gets an unmodifiable {@code List} containing all the {@code Vulnerability} for the current
     * active Locale. They are loaded from a XML file.
     *
     * <p>An empty {@code List} is returned if any error occurred while opening/parsing the XML
     * file. The returned {@code List} is guaranteed to be <i>non</i> {@code null}.
     *
     * <p><b>Note:</b> Trying to modify the list will result in an {@code
     * UnsupportedOperationException}.
     *
     * @return an unmodifiable {@code List} containing all the {@code Vulnerability} loaded, never
     *     {@code null}.
     */
    public static List<Vulnerability> getAllVulnerabilities() {
        var local = provider;
        if (local == null) {
            logNoProvider();
            return List.of();
        }
        return local.getAll();
    }

    private static void logNoProvider() {
        LOGGER.error("No provider found.", new Exception());
    }

    /**
     * Returns the {@code Vulnerability} for the given WASC ID, or {@code null} if not available.
     *
     * <p>The WASC ID is in the form:
     *
     * <blockquote>
     *
     * "wasc_" + #ID
     *
     * </blockquote>
     *
     * <p>For example, "wasc_1", "wasc_2" or "wasc_48".
     *
     * @param id the WASC ID of the vulnerability, e.g. wasc_1
     * @return the {@code Vulnerability} for the given WASC ID, or {@code null} if not available
     */
    public static Vulnerability getVulnerability(String id) {
        var local = provider;
        if (local == null) {
            logNoProvider();
            return null;
        }
        return local.get(id);
    }

    public static String getDescription(Vulnerability vuln) {
        if (vuln != null) {
            return vuln.getDescription();
        }
        return "Failed to load vulnerability description from file";
    }

    public static String getSolution(Vulnerability vuln) {
        if (vuln != null) {
            return vuln.getSolution();
        }
        return "Failed to load vulnerability solution from file";
    }

    public static String getReference(Vulnerability vuln) {
        if (vuln != null) {
            StringBuilder sb = new StringBuilder();
            for (String ref : vuln.getReferences()) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(ref);
            }
            return sb.toString();
        }
        return "Failed to load vulnerability reference from file";
    }

    public interface Provider {

        List<Vulnerability> getAll();

        Vulnerability get(String id);
    }
}

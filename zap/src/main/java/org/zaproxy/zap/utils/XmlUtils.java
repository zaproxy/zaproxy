/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.utils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/** A class with utility methods related to XML parsing. */
public final class XmlUtils {

    private XmlUtils() {}

    /**
     * Returns a new {@code DocumentBuilderFactory} instance with XML External Entity (XXE)
     * processing disabled.
     *
     * @return the new {@code DocumentBuilderFactory} instance with XXE processing disabled
     * @throws ParserConfigurationException if an error occurred while disabling XXE processing
     * @see DocumentBuilderFactory
     */
    public static DocumentBuilderFactory newXxeDisabledDocumentBuilderFactory()
            throws ParserConfigurationException {
        // Disable XXE processing, not required by default.
        // https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        return factory;
    }
}

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

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zaproxy.zap.model.SessionStructure;

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

    public static String getXmlKeyString(String xmlString)
            throws ParserConfigurationException, SAXException, IOException {
        StringBuilder sb = new StringBuilder();

        DocumentBuilderFactory factory = newXxeDisabledDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlString));
        Document document = builder.parse(inputSource);

        appendXmlKeyString(document.getFirstChild(), sb);

        if (sb.length() > SessionStructure.MAX_NODE_NAME_SIZE) {
            return sb.substring(0, SessionStructure.MAX_NODE_NAME_SIZE - 3) + "...";
        }

        return sb.toString();
    }

    private static boolean ignoreChildNodes(Node node) {
        return node == null
                || !node.hasChildNodes()
                || (node.getChildNodes().getLength() == 1
                        && node.getFirstChild().getNodeType() == Node.TEXT_NODE);
    }

    private static void appendXmlKeyString(Node node, StringBuilder sb) {
        sb.append('<');
        sb.append(node.getNodeName());

        if (!ignoreChildNodes(node)) {
            Node ch = node.getFirstChild();
            String lastChild = null;
            String postfix = "..";
            while (ch != null && sb.length() < SessionStructure.MAX_NODE_NAME_SIZE) {
                if (ch.getNodeType() != Node.TEXT_NODE) {
                    StringBuilder sb2 = new StringBuilder();
                    appendXmlKeyString(ch, sb2);

                    if (lastChild == null) {
                        lastChild = sb2.toString();
                        sb.append(':');
                        sb.append(lastChild);
                    } else if (lastChild.equals(sb2.toString())) {
                        sb.append(postfix);
                        postfix = "";
                    } else {
                        lastChild = sb2.toString();
                        sb.append(',');
                        sb.append(lastChild);
                        postfix = "..";
                    }
                }
                ch = ch.getNextSibling();
            }
        }

        sb.append('>');
    }
}

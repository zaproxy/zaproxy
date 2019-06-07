/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A {@code XMLConfiguration} with character encoding always set to UTF-8 and delimiter parsing
 * always disabled.
 *
 * <p><strong>Note:</strong> This class should be used, <i>always</i>, to read/write from/to the
 * configurations' file used by ZAP, this way there shouldn't be any problems with character
 * encodings.
 *
 * @see #setDelimiterParsingDisabled(boolean)
 * @see XMLConfiguration
 */
public class ZapXmlConfiguration extends XMLConfiguration {

    private static final long serialVersionUID = -8598525138011232529L;

    /** Creates a new instance of {@code ZapXmlConfiguration}. */
    public ZapXmlConfiguration() {
        super();

        super.setEncoding("UTF-8");
        super.setDelimiterParsingDisabled(true);
    }

    /**
     * Creates a new instance of {@code ZapXmlConfiguration} with the configuration loaded from the
     * given input stream.
     *
     * @param in the input stream to load the configuration from.
     * @throws ConfigurationException if loading the configuration fails.
     * @since 2.8.0
     */
    public ZapXmlConfiguration(InputStream in) throws ConfigurationException {
        this();
        load(in);
    }

    /**
     * Creates a new instance of {@code ZapXmlConfiguration}. The configuration is loaded from the
     * file with the specified {@code fileName}.
     *
     * @param fileName the name of the file to load
     * @throws ConfigurationException if loading the configuration fails
     */
    public ZapXmlConfiguration(String fileName) throws ConfigurationException {
        this();
        setFileName(fileName);
        load();
    }

    /**
     * Creates a new instance of {@code ZapXmlConfiguration}. The configuration is loaded from the
     * specified {@code file}.
     *
     * @param file the file that has the configuration
     * @throws ConfigurationException if loading the configuration fails
     */
    public ZapXmlConfiguration(File file) throws ConfigurationException {
        this();
        setFile(file);
        load();
    }

    @Override
    protected DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = XmlUtils.newXxeDisabledDocumentBuilderFactory();

        // Same behaviour as base method:
        if (isValidating()) {
            factory.setValidating(true);
            if (isSchemaValidation()) {
                factory.setNamespaceAware(true);
                factory.setAttribute(
                        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                        "http://www.w3.org/2001/XMLSchema");
            }
        }

        DocumentBuilder result = factory.newDocumentBuilder();
        result.setEntityResolver(getEntityResolver());

        if (isValidating()) {
            result.setErrorHandler(
                    new DefaultHandler() {

                        @Override
                        public void error(SAXParseException ex) throws SAXException {
                            throw ex;
                        }
                    });
        }
        return result;
    }

    @Override
    protected Transformer createTransformer() throws TransformerException {
        Transformer transformer = super.createTransformer();
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        return transformer;
    }

    /**
     * Creates a new instance of {@code ZapXmlConfiguration}. The configuration is loaded from the
     * specified {@code url}.
     *
     * @param url the URL
     * @throws ConfigurationException if loading the configuration fails
     */
    public ZapXmlConfiguration(URL url) throws ConfigurationException {
        this();
        setURL(url);
        load();
    }

    /**
     * Calling this method has <strong>no</strong> effect. The character encoding used is always the
     * same, UTF-8.
     */
    @Override
    public void setEncoding(String encoding) {
        // Always uses UTF-8
    }

    /**
     * Calling this method has <strong>no</strong> effect. The delimiter parsing is always disabled.
     *
     * @see #setDelimiterParsingDisabled(boolean)
     */
    @Override
    public void setListDelimiter(char listDelimiter) {
        // Always use the default character, as calling
        // setDelimiterParsingDisabled as no effect, there is no need to change
        // the character.
    }

    /**
     * Calling this method has <strong>no</strong> effect. The delimiter parsing is always disabled.
     */
    @Override
    public void setDelimiterParsingDisabled(boolean delimiterParsingDisabled) {
        // Always disabled.
    }

    @Override
    public void load(InputStream in) throws ConfigurationException {
        super.load(in);
        postLoad();
    }

    @Override
    public void load(Reader in) throws ConfigurationException {
        super.load(in);
        postLoad();
    }

    private void postLoad() {
        // Ensure it's used a "clean" document for proper indentation of the configurations.
        // In newer Java versions (9+) the text nodes are indented as well, which would lead
        // to additional text nodes each time the configuration is loaded/saved.
        clearReferences(getRootNode());
        String rootName = getRootElementName();
        getDocument().removeChild(getDocument().getDocumentElement());
        getDocument().appendChild(getDocument().createElement(rootName));
    }
}

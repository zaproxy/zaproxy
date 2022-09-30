/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.spider.parser;

import java.io.ByteArrayInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.htmlparser.jericho.Source;
import org.apache.logging.log4j.LogManager;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.zaproxy.zap.utils.XmlUtils;

/**
 * SitemapXMLParser is used for parsing URLs from a sitemap.xml file, which sometimes (very
 * helpfully) resides in the web root.
 *
 * @author 70pointer
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class SpiderSitemapXMLParser extends SpiderParser {

    /** a pattern to match the sitemap.xml file name */
    private Pattern SITEMAP_XML_FILENAME_PATTERN = Pattern.compile("/sitemap\\.xml$");

    /** a pattern to match the sitemap.xml file.. hint: It's XML */
    private static final Pattern xmlPattern =
            Pattern.compile(
                    "^<\\?xml\\s+version\\s*=\\s*\"[0-9.]+\"\\s+encoding\\s*=\\s*\"[^\"]+\"\\s*\\?>");

    /** used to parse the XML based file format */
    private static DocumentBuilder dBuilder;

    /** an x path expression to match the "loc" tag in sitemap.xml */
    private static XPathExpression xpathLocationExpression;

    /** statically initialise the XML DocumentBuilderFactory and DocumentBuilder */
    static {
        try {
            dBuilder = XmlUtils.newXxeDisabledDocumentBuilderFactory().newDocumentBuilder();
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpathLocationExpression = xpath.compile("/urlset/url/loc/text()");
        } catch (ParserConfigurationException | XPathExpressionException e) {
            LogManager.getLogger(SpiderSitemapXMLParser.class).error(e);
        }
    }

    /**
     * Instantiates a new sitemap.xml parser.
     *
     * @param params the params
     * @throws NullPointerException if {@code params} is null.
     */
    public SpiderSitemapXMLParser(org.zaproxy.zap.spider.SpiderParam params) {
        super(params);
    }

    @Override
    public boolean parseResource(HttpMessage message, Source source, int depth) {

        getLogger().debug("Parsing a sitemap.xml resource...");

        if (message == null
                || !getSpiderParam().isParseSitemapXml()
                || !message.getResponseHeader().isXml()
                || HttpStatusCode.isClientError(message.getResponseHeader().getStatusCode())
                || HttpStatusCode.isServerError(message.getResponseHeader().getStatusCode())) {
            return false;
        }

        // Get the response content
        byte[] response = message.getResponseBody().getBytes();
        String baseURL = message.getRequestHeader().getURI().toString();
        Matcher xmlFormatMatcher = xmlPattern.matcher(new String(response));
        if (xmlFormatMatcher.find()) {

            getLogger().debug("The format matches XML");

            try {
                Document xmldoc =
                        dBuilder.parse(new InputSource(new ByteArrayInputStream(response)));
                NodeList locationNodes =
                        (NodeList) xpathLocationExpression.evaluate(xmldoc, XPathConstants.NODESET);
                for (int i = 0; i < locationNodes.getLength(); i++) {
                    processURL(message, depth, locationNodes.item(i).getNodeValue(), baseURL);
                }
            } catch (Exception e) {
                getLogger().error("An error occurred trying to parse sitemap.xml", e);
                return false;
            }
            // We consider the message fully parsed, so it doesn't get parsed by 'fallback' parsers
            return true;
        } else {
            // the file name is right, but the content is not. Pass it to another parser.
            getLogger()
                    .debug(
                            "The content of the response from '{}' does not match the expected content for a sitemap.xml file. Ignoring it.",
                            baseURL);
            return false;
        }
    }

    @Override
    public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyParsed) {
        getLogger().debug("canParseResource called on '{}'", path);
        // matches the file name of files that should be parsed with the sitemap.xml file parser
        Matcher matcher = SITEMAP_XML_FILENAME_PATTERN.matcher(path);
        return matcher.find();
    }
}

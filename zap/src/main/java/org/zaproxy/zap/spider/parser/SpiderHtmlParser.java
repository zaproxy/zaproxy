/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;
import org.parosproxy.paros.network.HttpMessage;

/**
 * The Class SpiderHtmlParser is used for parsing of HTML files, gathering resource urls from them.
 *
 * <p><strong>NOTE:</strong> Handling of HTML Forms is not done in this Parser. Instead see {@link
 * SpiderHtmlFormParser}.
 *
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class SpiderHtmlParser extends SpiderParser {

    /** The Constant URL_PATTERN defining the pattern for a meta URL. */
    static final Pattern URL_PATTERN =
            Pattern.compile(
                    "(?:url\\s*=|report-uri)\\s*[\"']?([^;'\"]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern PLAIN_COMMENTS_URL_PATTERN =
            Pattern.compile(
                    "(?:http(?:s?):)?//[^\\x00-\\x1f\"'\\s<>#()\\[\\]{}]+",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern INLINE_CONTENT_URL_PATTERN =
            Pattern.compile(
                    "(?:http(?:s?)://|(?:\\s|\\B)//?)[^\\x00-\\x1f\"'\\s<>#()\\[\\]{}]+",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern SRCSET_PATTERN =
            Pattern.compile("[^\"'=\\s,]+\\.[^\\s,]+", Pattern.CASE_INSENSITIVE);

    private static final List<String> elementsWithText =
            Arrays.asList(
                    HTMLElementName.P,
                    HTMLElementName.TITLE,
                    HTMLElementName.H1,
                    HTMLElementName.H2,
                    HTMLElementName.H3,
                    HTMLElementName.H4,
                    HTMLElementName.H5,
                    HTMLElementName.H6,
                    HTMLElementName.LI,
                    HTMLElementName.BLOCKQUOTE);
    /**
     * Functional interface that allows custom handling of URLs retrieved in attributes, to be
     * manipulated before the URL is sent to a {@link SpiderParser#processURL} call.
     */
    @FunctionalInterface
    private interface CustomUrlProcessor {
        void process(HttpMessage message, int depth, String localURL, String baseURL);
    }

    private static final String IMPORT_TAG = "IMPORT";

    private boolean baseTagSet;

    /**
     * Instantiates a new spider html parser.
     *
     * @param params the params
     * @throws NullPointerException if {@code params} is null.
     */
    public SpiderHtmlParser(org.zaproxy.zap.spider.SpiderParam params) {
        super(params);
    }

    /** @throws NullPointerException if {@code message} is null. */
    @Override
    public boolean parseResource(HttpMessage message, Source source, int depth) {

        // Prepare the source, if not provided
        if (source == null) {
            source = new Source(message.getResponseBody().toString());
        }

        // Get the context (base URL)
        String baseURL = message.getRequestHeader().getURI().toString();

        // Try to see if there's any BASE tag that could change the base URL
        Element base = source.getFirstElement(HTMLElementName.BASE);
        if (base != null) {
            getLogger().debug("Base tag was found in HTML: {}", base.getDebugInfo());
            String href = base.getAttributeValue("href");
            if (href != null && !href.isEmpty()) {
                baseURL = getCanonicalURL(href, baseURL);
                baseTagSet = true;
            }
        }

        // Parse the source
        parseSource(message, source, depth, baseURL);

        // Parse the comments
        if (getSpiderParam().isParseComments()) {
            List<StartTag> comments = source.getAllStartTags(StartTagType.COMMENT);
            for (StartTag comment : comments) {
                Source s = new Source(comment.getTagContent());
                if (!parseSource(message, s, depth, baseURL)) {
                    Matcher matcher = PLAIN_COMMENTS_URL_PATTERN.matcher(s.toString());
                    while (matcher.find()) {
                        processURL(message, depth, matcher.group(), baseURL);
                    }
                }
            }
        }

        // Parse the DOCTYPEs (should only be one, but you never know;)
        List<StartTag> doctypes = source.getAllStartTags(StartTagType.DOCTYPE_DECLARATION);
        for (StartTag doctype : doctypes) {
            for (String str : doctype.getTagContent().toString().split(" ")) {
                if (str.startsWith("\"") && str.endsWith("\"")) {
                    processURL(message, depth, str.substring(1, str.length() - 1), baseURL);
                }
            }
        }

        return false;
    }

    /**
     * Implements the CustomUrlProcessor to handle the special format of a srcset attribute element,
     * see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img#attr-srcset the srcset
     * attribute contains one or more strings separated by commas, indicating possible image sources
     * for the user agent to use.
     */
    private void srcSetProcessor(HttpMessage message, int depth, String localURL, String baseURL) {
        Matcher results = SRCSET_PATTERN.matcher(localURL);
        while (results.find()) {
            if (!results.group().isEmpty()) {
                processURL(message, depth, results.group(), baseURL);
            }
        }
    }

    /**
     * Parses the HTML Jericho source for the elements that contain references to other resources.
     *
     * @param message the message
     * @param source the source
     * @param depth the depth
     * @param baseURL the base URL
     * @return {@code true} if at least one URL was found, {@code false} otherwise.
     */
    private boolean parseSource(HttpMessage message, Source source, int depth, String baseURL) {
        getLogger().debug("Parsing an HTML message...");
        boolean resourcesfound = false;
        // Process A elements
        List<Element> elements = source.getAllElements(HTMLElementName.A);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "href");
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "ping");
        }

        // Process Applet elements
        elements = source.getAllElements(HTMLElementName.APPLET);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "archive");
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "codebase");
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "src");
        }

        // Process AREA elements
        elements = source.getAllElements(HTMLElementName.AREA);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "href");
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "ping");
        }

        // Process AUDIO elements
        elements = source.getAllElements(HTMLElementName.AUDIO);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "src");
        }

        // Process Embed Elements
        elements = source.getAllElements(HTMLElementName.EMBED);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "src");
        }

        // Process Frame Elements
        elements = source.getAllElements(HTMLElementName.FRAME);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "src");
        }

        // Process IFrame Elements
        elements = source.getAllElements(HTMLElementName.IFRAME);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "src");
        }

        // Process Input elements
        elements = source.getAllElements(HTMLElementName.INPUT);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "src");
        }

        // Process ISINDEX elements
        elements = source.getAllElements(HTMLElementName.ISINDEX);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "action");
        }

        // Process Link elements
        elements = source.getAllElements(HTMLElementName.LINK);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "href");
        }

        // Process Object elements
        elements = source.getAllElements(HTMLElementName.OBJECT);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "data");
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "codebase");
        }

        // Process Script elements with src
        elements = source.getAllElements(HTMLElementName.SCRIPT);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "src");
        }

        // Process Table elements
        elements = source.getAllElements(HTMLElementName.TABLE);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "background");
        }

        // Process TD elements
        elements = source.getAllElements(HTMLElementName.TD);
        for (Element src : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, src, "background");
        }

        // Process Video elements
        elements = source.getAllElements(HTMLElementName.VIDEO);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "src");
            List<Element> videoSourceElements = el.getAllElements(HTMLElementName.SOURCE);
            for (Element sourceElement : videoSourceElements) {
                resourcesfound |=
                        processAttributeElement(message, depth, baseURL, sourceElement, "src");
            }
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "poster");
        }

        // Process Img elements
        elements = source.getAllElements(HTMLElementName.IMG);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "src");
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "longdesc");
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "lowsrc");
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "dynsrc");
            resourcesfound |=
                    processAttributeElement(
                            message, depth, baseURL, el, "srcset", this::srcSetProcessor);
        }

        // Process IMPORT elements
        elements = source.getAllElements(IMPORT_TAG);
        for (Element el : elements) {
            resourcesfound |=
                    processAttributeElement(message, depth, baseURL, el, "implementation");
        }

        // Process content of container tags which hold text
        String baseUrlForText = baseURL;
        for (String tag : elementsWithText) {
            elements = source.getAllElements(tag);
            for (Element el : elements) {
                Matcher matcher =
                        INLINE_CONTENT_URL_PATTERN.matcher(
                                el.getContent().getRenderer().setMaxLineLength(0).toString());
                while (matcher.find()) {
                    String foundMatch = matcher.group().trim();
                    if (baseTagSet) {
                        if (!baseUrlForText.endsWith("/")) {
                            baseUrlForText += "/";
                        }
                        if (foundMatch.charAt(0) == '/' && foundMatch.indexOf("//") != 0) {
                            foundMatch = foundMatch.substring(1);
                        }
                    }
                    processURL(message, depth, foundMatch, baseUrlForText);
                    resourcesfound = true;
                }
            }
        }

        // Process META elements
        elements = source.getAllElements(HTMLElementName.META);
        for (Element el : elements) {
            // If we have http-equiv attribute, then urls can be found.
            String equiv = el.getAttributeValue("http-equiv");
            String name = el.getAttributeValue("name");
            String content = el.getAttributeValue("content");
            if (equiv != null && content != null) {

                // For the following cases:
                // http-equiv="refresh" content="0;URL=http://foo.bar/..."
                // http-equiv="location" content="url=http://foo.bar/..."
                if (equiv.equalsIgnoreCase("refresh")
                        || equiv.equalsIgnoreCase("location")
                        || equiv.equalsIgnoreCase("content-security-policy")) {
                    Matcher matcher = URL_PATTERN.matcher(content);
                    if (matcher.find()) {
                        String url = matcher.group(1);
                        processURL(message, depth, url, baseURL);
                        resourcesfound = true;
                    }
                }
            } else if ("msapplication-config".equalsIgnoreCase(name)
                    && content != null
                    && !content.equals("")
                    && !content.equalsIgnoreCase("none")) {
                processURL(message, depth, content, baseURL);
                resourcesfound = true;
            }
        }

        // Process HTML manifest elements
        elements = source.getAllElements(HTMLElementName.HTML);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "manifest");
        }

        // Process BODY background elements
        elements = source.getAllElements(HTMLElementName.BODY);
        for (Element el : elements) {
            resourcesfound |= processAttributeElement(message, depth, baseURL, el, "background");
        }

        return resourcesfound;
    }

    /**
     * Processes the attribute with the given name of a Jericho element, for an URL. If an URL is
     * found, notifies the listeners.
     *
     * @param message the message
     * @param depth the depth
     * @param baseURL the base URL
     * @param element the element
     * @param attributeName the attribute name
     * @return {@code true} if a URL was processed, {@code false} otherwise.
     */
    private boolean processAttributeElement(
            HttpMessage message, int depth, String baseURL, Element element, String attributeName) {
        return processAttributeElement(message, depth, baseURL, element, attributeName, null);
    }

    /**
     * Processes the attribute with the given name of a Jericho element, for an URL. If an URL is
     * found, notifies the listeners.
     *
     * @param message the message
     * @param depth the depth
     * @param baseURL the base URL
     * @param element the element
     * @param attributeName the attribute name
     * @param customUrlProcessor functional interface for custom manipulation of urls
     * @return {@code true} if a URL was processed, {@code false} otherwise.
     */
    private boolean processAttributeElement(
            HttpMessage message,
            int depth,
            String baseURL,
            Element element,
            String attributeName,
            CustomUrlProcessor customUrlProcessor) {
        // The URL as written in the attribute (can be relative or absolute)
        String localURL = element.getAttributeValue(attributeName);
        if (localURL == null) {
            return false;
        }

        if (customUrlProcessor != null) {
            customUrlProcessor.process(message, depth, localURL, baseURL);
        } else if (!attributeName.equalsIgnoreCase("ping")) {
            processURL(message, depth, localURL, baseURL);
        } else {
            for (String pingURL : localURL.split("\\s")) {
                if (!pingURL.isEmpty()) {
                    processURL(message, depth, pingURL, baseURL);
                }
            }
        }
        return true;
    }

    /** @throws NullPointerException if {@code message} is null. */
    @Override
    public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyConsumed) {
        // Fallback parser - if it's a HTML message which has not already been processed
        return !wasAlreadyConsumed && message.getResponseHeader().isHtml();
    }
}

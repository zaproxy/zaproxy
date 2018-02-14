/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Source;
import org.apache.commons.lang.StringEscapeUtils;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Used to parse OData content in Atom format.
 *
 * <p>It's derived from the SpiderTextParser. Even if the format of the file is XML we will process
 * it as a simple text file
 *
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class SpiderODataAtomParser extends SpiderParser {

    /** The Constant urlPattern defining the pattern for an url. */
    private static final Pattern patternURL = Pattern.compile("href=\\\"([\\w();&'/,=\\-]*)\\\"");

    /** the Constant patternBase defines the pattern for a base url */
    private static final Pattern patternBase =
            Pattern.compile("base=\"(http(s?)://[^\\x00-\\x1f\"'\\s<>#]+)\"");

    public SpiderODataAtomParser() {
        this(null);
    }

    public SpiderODataAtomParser(org.zaproxy.zap.spider.SpiderParam param) {
        super(param);
    }

    @Override
    public boolean parseResource(HttpMessage message, Source source, int depth) {
        getLogger().debug("Parsing an OData Atom resource.");

        // Get the context (base url)
        String baseURL = message.getRequestHeader().getURI().toString();

        // Use a simple pattern matcher to find urls (absolute and relative)

        String bodyAsStr = message.getResponseBody().toString();

        // Handle base tag if any
        // xml:base="http://myserver:8001/remoting/myapp.svc/"

        Matcher matcher = patternBase.matcher(bodyAsStr);
        if (matcher.find()) {
            baseURL = matcher.group(1);
            baseURL = StringEscapeUtils.unescapeXml(baseURL);
        }

        boolean foundAtLeastOneResult = false;
        matcher = patternURL.matcher(bodyAsStr);
        while (matcher.find()) {
            String s = matcher.group(1);
            s = StringEscapeUtils.unescapeXml(s);

            processURL(message, depth, s, baseURL);
            foundAtLeastOneResult = true;
        }

        // resource is consumed only if at least one link is found
        return foundAtLeastOneResult;
    }

    @Override
    public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyParsed) {
        // Fallback parser - if it's an XML message which has not already been processed
        return !wasAlreadyParsed && message.getResponseHeader().isXml();
    }
}

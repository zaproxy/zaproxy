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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Source;
import org.parosproxy.paros.network.HttpMessage;

/**
 * The Class SpiderTextParser is used for parsing of simple text (non-HTML) files, gathering
 * resource urls from them. For example it is used for parsing CSS, less, javascript files,
 * searching for urls.
 *
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class SpiderTextParser extends SpiderParser {

    /** The Constant urlPattern defining the pattern for an url. */
    private static final Pattern patternURL =
            Pattern.compile(
                    "\\W(http(s?)://[^\\x00-\\x1f\"'\\s<>#()\\[\\]{}]+)", Pattern.CASE_INSENSITIVE);

    public SpiderTextParser() {
        this(null);
    }

    public SpiderTextParser(org.zaproxy.zap.spider.SpiderParam params) {
        super(params);
    }

    @Override
    public boolean parseResource(HttpMessage message, Source source, int depth) {
        getLogger().debug("Parsing a non-HTML text resource.");

        String baseURL = message.getRequestHeader().getURI().toString();

        // Use a simple pattern matcher to find urls
        Matcher matcher = patternURL.matcher(message.getResponseBody().toString());
        while (matcher.find()) {
            String s = matcher.group(1);
            processURL(message, depth, s, baseURL);
        }

        return false;
    }

    @Override
    public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyConsumed) {
        // Fall-back parser - if it's a text, non-HTML response which has not already been processed
        return !wasAlreadyConsumed
                && message.getResponseHeader().isText()
                && !message.getResponseHeader().isHtml();
    }
}

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
package org.zaproxy.zap.spider.parser;

import java.util.regex.Matcher;
import net.htmlparser.jericho.Source;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;

/**
 * The Class SpiderHttpHeaderParser is used for parsing of HTTP headers that can include URLs.
 *
 * @see SpiderRedirectParser
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class SpiderHttpHeaderParser extends SpiderParser {

    public SpiderHttpHeaderParser(org.zaproxy.zap.spider.SpiderParam params) {
        super(params);
    }

    @Override
    public boolean parseResource(HttpMessage message, Source source, int depth) {
        String baseURL = message.getRequestHeader().getURI().toString();

        // Content-location header
        String location = message.getResponseHeader().getHeader(HttpHeader.CONTENT_LOCATION);
        if (location != null && !location.isEmpty()) {
            processURL(message, depth, location, baseURL);
        }
        // Refresh header
        String refresh = message.getResponseHeader().getHeader(HttpHeader.REFRESH);
        if (refresh != null && !refresh.isEmpty()) {
            Matcher matcher = SpiderHtmlParser.URL_PATTERN.matcher(refresh);
            if (matcher.find()) {
                String url = matcher.group(1);
                processURL(message, depth, url, baseURL);
            }
        }

        // Link header - potentially multiple absolute or relative URLs in < >
        String link = message.getResponseHeader().getHeader(HttpHeader.LINK);
        if (link != null && !link.isEmpty()) {
            int offset = 0;
            while (true) {
                int i = link.indexOf("<", offset);
                if (i < 0) {
                    break;
                }
                int j = link.indexOf(">", i);
                if (j < 0) {
                    break;
                }
                processURL(message, depth, link.substring(i + 1, j), baseURL);
                offset = j;
            }
        }
        // We do not consider the message fully parsed
        return false;
    }

    @Override
    public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyParsed) {
        return true;
    }
}

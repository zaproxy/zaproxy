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

import java.util.StringTokenizer;
import net.htmlparser.jericho.Source;
import org.parosproxy.paros.network.HttpMessage;

/**
 * The Class SpiderRobotstxtParser used for parsing Robots.txt files.
 *
 * @since 2.0.0
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class SpiderRobotstxtParser extends SpiderParser {

    private static final String COMMENT_TOKEN = "#";

    private static final String PATTERNS_DISALLOW = "(?i)Disallow:.*";
    private static final String PATTERNS_ALLOW = "(?i)Allow:.*";

    private static final int PATTERNS_DISALLOW_LENGTH = 9;
    private static final int PATTERNS_ALLOW_LENGTH = 6;

    /**
     * Instantiates a new spider robotstxt parser.
     *
     * @param params the params
     * @throws NullPointerException if {@code params} is null.
     */
    public SpiderRobotstxtParser(org.zaproxy.zap.spider.SpiderParam params) {
        super(params);
    }

    /** @throws NullPointerException if {@code message} is null. */
    @Override
    public boolean parseResource(HttpMessage message, Source source, int depth) {
        if (!getSpiderParam().isParseRobotsTxt()) {
            return false;
        }
        getLogger().debug("Parsing a robots.txt resource...");

        String baseURL = message.getRequestHeader().getURI().toString();

        StringTokenizer st = new StringTokenizer(message.getResponseBody().toString(), "\n");
        while (st.hasMoreTokens()) {
            String line = st.nextToken();

            int commentStart = line.indexOf(COMMENT_TOKEN);
            if (commentStart != -1) {
                line = line.substring(0, commentStart);
            }

            // remove HTML markup and clean
            line = line.replaceAll("<[^>]+>", "");
            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }
            getLogger().debug("Processing robots.txt line: {}", line);

            if (line.matches(PATTERNS_DISALLOW)) {
                processPath(message, depth, line.substring(PATTERNS_DISALLOW_LENGTH), baseURL);
            } else if (line.matches(PATTERNS_ALLOW)) {
                processPath(message, depth, line.substring(PATTERNS_ALLOW_LENGTH), baseURL);
            }
        }

        // We consider the message fully parsed, so it doesn't get parsed by 'fallback' parsers
        return true;
    }

    private void processPath(HttpMessage message, int depth, String path, String baseURL) {
        String processedPath = path.trim();
        if (processedPath.endsWith("*")) {
            processedPath = processedPath.substring(0, processedPath.length() - 1).trim();
        }

        if (!processedPath.isEmpty()) {
            processURL(message, depth, processedPath, baseURL);
        }
    }

    @Override
    public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyParsed) {
        // If it's a robots.txt file
        return "/robots.txt".equalsIgnoreCase(path);
    }
}

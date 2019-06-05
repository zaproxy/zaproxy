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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.split.response;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseHeaderStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.util.HttpTextViewUtils;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpResponseHeaderPanelSyntaxHighlightTextView
        extends HttpPanelSyntaxHighlightTextView {

    public HttpResponseHeaderPanelSyntaxHighlightTextView(
            ResponseHeaderStringHttpPanelViewModel model) {
        super(model);
    }

    @Override
    protected HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
        return new HttpResponseHeaderPanelSyntaxHighlightTextArea();
    }

    private static class HttpResponseHeaderPanelSyntaxHighlightTextArea
            extends HttpPanelSyntaxHighlightTextArea {

        private static final long serialVersionUID = 6197189781594557597L;

        // private static final String HTTP_RESPONSE_HEADER = "HTTP Response Header";

        // private static final String SYNTAX_STYLE_HTTP_RESPONSE_HEADER =
        // "text/http-response-header";

        private static ResponseHeaderTokenMakerFactory tokenMakerFactory = null;

        public HttpResponseHeaderPanelSyntaxHighlightTextArea() {
            // addSyntaxStyle(HTTP_RESPONSE_HEADER, SYNTAX_STYLE_HTTP_RESPONSE_HEADER);

            // setSyntaxEditingStyle(SYNTAX_STYLE_HTTP_RESPONSE_HEADER);
        }

        @Override
        public void search(Pattern p, List<SearchMatch> matches) {
            Matcher m = p.matcher(getText());
            while (m.find()) {

                int[] position =
                        HttpTextViewUtils.getViewToHeaderPosition(this, m.start(), m.end());
                if (position.length == 0) {
                    return;
                }

                matches.add(
                        new SearchMatch(
                                SearchMatch.Location.RESPONSE_HEAD, position[0], position[1]));
            }
        }

        @Override
        public void highlight(SearchMatch sm) {
            if (!SearchMatch.Location.RESPONSE_HEAD.equals(sm.getLocation())) {
                return;
            }

            int[] pos =
                    HttpTextViewUtils.getHeaderToViewPosition(
                            this,
                            sm.getMessage().getResponseHeader().toString(),
                            sm.getStart(),
                            sm.getEnd());
            if (pos.length == 0) {
                return;
            }
            highlight(pos[0], pos[1]);
        }

        @Override
        protected synchronized CustomTokenMakerFactory getTokenMakerFactory() {
            if (tokenMakerFactory == null) {
                tokenMakerFactory = new ResponseHeaderTokenMakerFactory();
            }
            return tokenMakerFactory;
        }

        private static class ResponseHeaderTokenMakerFactory extends CustomTokenMakerFactory {

            public ResponseHeaderTokenMakerFactory() {
                // String pkg = "";

                // putMapping(SYNTAX_STYLE_HTTP_RESPONSE_HEADER, pkg + "HttpResponseTokenMaker");
            }
        }
    }
}
